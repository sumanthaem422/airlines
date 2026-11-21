/*
 *  Copyright 2015 Adobe Systems Incorporated
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.example.airline.core.models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Exporter;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ChildResource;
import org.apache.sling.models.annotations.injectorspecific.InjectionStrategy;
import org.apache.sling.models.annotations.injectorspecific.SlingObject;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

import com.adobe.cq.export.json.ComponentExporter;
import com.adobe.cq.export.json.ExporterConstants;
import com.adobe.cq.wcm.core.components.models.Component;
import com.adobe.cq.wcm.core.components.models.datalayer.ComponentData;
import com.adobe.cq.wcm.core.components.models.datalayer.builder.DataLayerBuilder;
import com.adobe.cq.wcm.core.components.util.ComponentUtils;
import com.day.cq.dam.api.Asset;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@Model(
        adaptables = { SlingHttpServletRequest.class, Resource.class },
        adapters = { HeroBanner.class, Component.class, ComponentExporter.class },
        resourceType = HeroBannerImpl.RESOURCE_TYPE,
        defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
@Exporter(
        name = ExporterConstants.SLING_MODEL_EXPORTER_NAME,
        extensions = ExporterConstants.SLING_MODEL_EXTENSION)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HeroBannerImpl implements HeroBanner {

    public static final String RESOURCE_TYPE = "airline/components/herobanner";

    static final String DAM_TITLE = "dc:title";

    @ValueMapValue
    private String title;

    @ValueMapValue
    private String introduction;

    @ValueMapValue
    private String fileReference;

    @ValueMapValue
    private String alt;

    @ValueMapValue
    private boolean decorative;

    @ChildResource(injectionStrategy = InjectionStrategy.OPTIONAL)
    private List<HeroBanner.JumpLink> links;

    @SlingObject
    private Resource resource;

    @SlingObject
    private ResourceResolver resourceResolver;

    private String resolvedAlt = "";

    private boolean empty;

    private String id;

    private ComponentData componentData;

    private List<HeroBanner.JumpLink> jumpLinks = Collections.emptyList();

    @PostConstruct
    protected void init() {
        resolvedAlt = resolveAlt();
        empty = isBlank(title) && isBlank(fileReference);
        jumpLinks = copyLinks(links);
        if (resource != null) {
            id = ComponentUtils.getId(resource, null, null);
            if (ComponentUtils.isDataLayerEnabled(resource)) {
                componentData = DataLayerBuilder.forComponent()
                        .withId(this::getId)
                        .withType(this::getExportedType)
                        .withTitle(this::titleForDataLayer)
                        .build();
            }
        }
    }

    @Override
    @JsonIgnore
    public String getAppliedCssClasses() {
        return null;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    @JsonIgnore
    public ComponentData getData() {
        return componentData;
    }

    @Override
    @JsonProperty("dataLayer")
    public String getDataLayer() {
        return componentData == null ? null : componentData.getJson();
    }

    private String titleForDataLayer() {
        String value = getTitle();
        return value.isEmpty() ? null : value;
    }

    @Override
    public String getTitle() {
        return blankToEmpty(title);
    }

    @Override
    public String getIntroduction() {
        return blankToEmpty(introduction);
    }

    @Override
    public String getFileReference() {
        return blankToEmpty(fileReference);
    }

    @Override
    public String getSrc() {
        return getFileReference();
    }

    @Override
    public String getAlt() {
        return resolvedAlt == null ? "" : resolvedAlt;
    }

    @Override
    public boolean isDecorative() {
        return decorative;
    }

    @Override
    @JsonProperty("empty")
    public boolean isEmpty() {
        return empty;
    }

    @Override
    public List<JumpLink> getLinks() {
        return jumpLinks;
    }

    @Override
    @JsonProperty(ExporterConstants.PN_TYPE)
    public String getExportedType() {
        return RESOURCE_TYPE;
    }

    /**
     * Non-decorative fallback: authored alt, then DAM {@code dc:title}, then the
     * component title ({@link #getTitle()}), else empty.
     */
    private String resolveAlt() {
        if (decorative) {
            return "";
        }
        if (!isBlank(alt)) {
            return alt.trim();
        }
        String damTitle = titleFromDam();
        if (!isBlank(damTitle)) {
            return damTitle;
        }
        return getTitle();
    }

    private String titleFromDam() {
        if (isBlank(fileReference) || resourceResolver == null) {
            return "";
        }
        Resource assetResource = resourceResolver.getResource(fileReference.trim());
        if (assetResource == null) {
            return "";
        }
        String fromAsset = titleFromAsset(assetResource);
        if (!isBlank(fromAsset)) {
            return fromAsset.trim();
        }
        return titleFromMetadata(assetResource);
    }

    private String titleFromAsset(Resource assetResource) {
        Asset asset = assetResource.adaptTo(Asset.class);
        if (asset == null) {
            return "";
        }
        String metadataValue = asset.getMetadataValue(DAM_TITLE);
        if (!isBlank(metadataValue)) {
            return metadataValue;
        }
        return asString(asset.getMetadata(DAM_TITLE));
    }

    private String titleFromMetadata(Resource assetResource) {
        Resource metadata = assetResource.getChild("jcr:content/metadata");
        if (metadata == null) {
            return "";
        }
        return asString(metadata.getValueMap().get(DAM_TITLE));
    }

    private static List<HeroBanner.JumpLink> copyLinks(List<HeroBanner.JumpLink> source) {
        if (source == null || source.isEmpty()) {
            return Collections.emptyList();
        }
        List<HeroBanner.JumpLink> copy = new ArrayList<HeroBanner.JumpLink>();
        for (HeroBanner.JumpLink link : source) {
            if (link != null) {
                copy.add(link);
            }
        }
        return Collections.unmodifiableList(copy);
    }

    private static String asString(Object value) {
        if (value instanceof String) {
            return ((String) value).trim();
        }
        if (value instanceof String[]) {
            String[] values = (String[]) value;
            for (String candidate : values) {
                if (!isBlank(candidate)) {
                    return candidate.trim();
                }
            }
            return "";
        }
        if (value == null) {
            return "";
        }
        return value.toString().trim();
    }

    private static String blankToEmpty(String value) {
        return isBlank(value) ? "" : value.trim();
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
