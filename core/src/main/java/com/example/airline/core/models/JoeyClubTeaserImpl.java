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
import java.util.Locale;
import java.util.function.Supplier;

import javax.annotation.PostConstruct;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Exporter;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.Via;
import org.apache.sling.models.annotations.injectorspecific.InjectionStrategy;
import org.apache.sling.models.annotations.injectorspecific.ScriptVariable;
import org.apache.sling.models.annotations.injectorspecific.Self;
import org.apache.sling.models.annotations.injectorspecific.SlingObject;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;
import org.apache.sling.models.annotations.via.ResourceSuperType;

import com.adobe.cq.export.json.ComponentExporter;
import com.adobe.cq.export.json.ExporterConstants;
import com.adobe.cq.wcm.core.components.commons.link.Link;
import com.adobe.cq.wcm.core.components.models.ListItem;
import com.adobe.cq.wcm.core.components.models.Teaser;
import com.adobe.cq.wcm.core.components.models.datalayer.ComponentData;
import com.adobe.cq.wcm.core.components.util.ComponentUtils;
import com.day.cq.dam.api.Asset;
import com.day.cq.wcm.api.designer.Style;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@Model(
        adaptables = SlingHttpServletRequest.class,
        adapters = { JoeyClubTeaser.class, Teaser.class, ComponentExporter.class },
        resourceType = JoeyClubTeaserImpl.RESOURCE_TYPE,
        defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
@Exporter(
        name = ExporterConstants.SLING_MODEL_EXPORTER_NAME,
        extensions = ExporterConstants.SLING_MODEL_EXTENSION)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JoeyClubTeaserImpl implements JoeyClubTeaser {

    public static final String RESOURCE_TYPE = "airline/components/teaser";

    static final String DAM_DESCRIPTION = "dc:description";

    private static final String HEADING_ELEMENT = "headingElement";

    private static final String TITLE_TYPE = "titleType";

    @Self(injectionStrategy = InjectionStrategy.OPTIONAL)
    @Via(type = ResourceSuperType.class)
    private Teaser delegate;

    @ValueMapValue
    private String title;

    @ValueMapValue
    private String description;

    @ValueMapValue
    private String fileReference;

    @ValueMapValue
    private String linkURL;

    @ValueMapValue
    private String linkText;

    @ScriptVariable(injectionStrategy = InjectionStrategy.OPTIONAL)
    private Style currentStyle;

    @SlingObject
    private Resource resource;

    @SlingObject
    private ResourceResolver resourceResolver;

    private String resolvedTitle = "";

    private String resolvedDescription = "";

    private String resolvedFileReference = "";

    private String resolvedAlt = "";

    private String resolvedLinkUrl = "";

    private String resolvedLinkText = "";

    private String resolvedRel = "";

    private String headingLevel = "h2";

    private boolean empty;

    private String id;

    private List<ExportedAction> exportedActions = Collections.emptyList();

    @PostConstruct
    protected void init() {
        resolvedTitle = firstNonEmpty(title, property("jcr:title"));
        if (resolvedTitle.isEmpty() && booleanProperty(Teaser.PN_TITLE_FROM_PAGE)) {
            resolvedTitle = delegateString(delegate == null ? null : delegate::getTitle);
        }
        resolvedDescription = firstNonEmpty(description, property("jcr:description"));
        if (resolvedDescription.isEmpty() && booleanProperty(Teaser.PN_DESCRIPTION_FROM_PAGE)) {
            resolvedDescription = delegateString(delegate == null ? null : delegate::getDescription);
        }
        resolvedFileReference = blankToEmpty(fileReference);
        if (resolvedFileReference.isEmpty()) {
            resolvedFileReference = imageChildFileReference();
        }
        if (resolvedFileReference.isEmpty()) {
            resolvedFileReference = delegateImageFileReference();
        }
        resolvedLinkUrl = blankToEmpty(linkURL);
        resolvedLinkText = blankToEmpty(linkText);
        exportedActions = resolveActions();
        backfillLinkFromActions();
        resolvedRel = "";
        resolvedAlt = resolveAlt();
        empty = resolvedTitle.isEmpty() && resolvedDescription.isEmpty() && resolvedFileReference.isEmpty();
        headingLevel = resolveHeading();
        if (resource != null) {
            id = ComponentUtils.getId(resource, null, null);
        }
    }

    @Override
    @JsonIgnore
    public String getAppliedCssClasses() {
        return null;
    }

    @Override
    @JsonProperty("id")
    public String getId() {
        return id;
    }

    @Override
    @JsonIgnore
    public ComponentData getData() {
        return delegate == null ? null : fromDelegate(delegate::getData);
    }

    @Override
    @JsonProperty("title")
    public String getTitle() {
        return resolvedTitle;
    }

    @Override
    @JsonProperty("description")
    public String getDescription() {
        return resolvedDescription;
    }

    @Override
    @JsonProperty("fileReference")
    public String getFileReference() {
        return resolvedFileReference;
    }

    @Override
    @JsonProperty("src")
    public String getSrc() {
        return resolvedFileReference;
    }

    @Override
    @JsonProperty("alt")
    public String getAltText() {
        return resolvedAlt;
    }

    @Override
    @JsonProperty("linkText")
    public String getLinkText() {
        return resolvedLinkText;
    }

    @Override
    @JsonProperty("linkURL")
    @SuppressWarnings("deprecation")
    public String getLinkURL() {
        return resolvedLinkUrl;
    }

    @Override
    @JsonProperty("rel")
    public String getRel() {
        return resolvedRel;
    }

    @Override
    @JsonProperty("headingLevel")
    public String getHeadingLevel() {
        return headingLevel;
    }

    @Override
    @JsonProperty(TITLE_TYPE)
    public String getTitleType() {
        return getHeadingLevel();
    }

    /**
     * Core teaser image object. {@code src} is the DAM path already exported as {@code fileReference}.
     */
    @JsonProperty("image")
    public ExportedImage getImage() {
        if (resolvedFileReference.isEmpty()) {
            return null;
        }
        return new ExportedImage(resolvedFileReference);
    }

    /**
     * Call-to-action list in the core shape {@code {title, text, url}}.
     * Omitted when the teaser has no action and no flat link fields.
     */
    @JsonProperty("actions")
    public List<ExportedAction> getExportedActions() {
        if (exportedActions == null || exportedActions.isEmpty()) {
            return null;
        }
        return exportedActions;
    }

    @Override
    @JsonProperty("empty")
    public boolean isEmpty() {
        return empty;
    }

    @Override
    @JsonProperty(ExporterConstants.PN_TYPE)
    public String getExportedType() {
        return RESOURCE_TYPE;
    }

    @Override
    public String getPretitle() {
        return delegate == null ? null : fromDelegate(delegate::getPretitle);
    }

    @Override
    public boolean isActionsEnabled() {
        Boolean enabled = delegate == null ? null : fromDelegate(delegate::isActionsEnabled);
        return Boolean.TRUE.equals(enabled);
    }

    @Override
    @JsonIgnore
    public List<ListItem> getActions() {
        return delegate == null ? null : fromDelegate(delegate::getActions);
    }

    @Override
    @JsonIgnore
    @SuppressWarnings("rawtypes")
    public Link getLink() {
        return delegate == null ? null : fromDelegate(delegate::getLink);
    }

    @Override
    @JsonIgnore
    public Resource getImageResource() {
        return delegate == null ? null : fromDelegate(delegate::getImageResource);
    }

    @Override
    public boolean isImageLinkHidden() {
        Boolean hidden = delegate == null ? null : fromDelegate(delegate::isImageLinkHidden);
        return Boolean.TRUE.equals(hidden);
    }

    @Override
    public boolean isTitleLinkHidden() {
        Boolean hidden = delegate == null ? null : fromDelegate(delegate::isTitleLinkHidden);
        return Boolean.TRUE.equals(hidden);
    }

    /**
     * Flat dialog fields win. Core-authored {@code jcr:title}, {@code jcr:description},
     * the image child, and the actions child fill the same contract when those fields are blank.
     * The linked page title is used only when {@code titleFromPage} is set and no title was authored.
     */
    private <T> T fromDelegate(Supplier<T> supplier) {
        if (delegate == null || supplier == null) {
            return null;
        }
        try {
            return supplier.get();
        } catch (RuntimeException ex) {
            return null;
        }
    }

    private String resolveHeading() {
        String headingElement = allowedHeading(styleString(HEADING_ELEMENT));
        if (!headingElement.isEmpty()) {
            return headingElement;
        }
        String titleType = allowedHeading(styleString(TITLE_TYPE));
        if (!titleType.isEmpty()) {
            return titleType;
        }
        return "h2";
    }

    private String styleString(String name) {
        if (currentStyle == null || name == null) {
            return "";
        }
        try {
            return blankToEmpty(currentStyle.get(name, String.class));
        } catch (RuntimeException ex) {
            return "";
        }
    }

    private static String allowedHeading(String value) {
        String normalized = value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
        if ("h2".equals(normalized) || "h3".equals(normalized) || "h4".equals(normalized)) {
            return normalized;
        }
        return "";
    }

    /**
     * When an image is present, alt text is the DAM asset {@code dc:description}.
     * A missing asset or description yields an empty string.
     */
    private String resolveAlt() {
        if (resolvedFileReference.isEmpty() || resourceResolver == null) {
            return "";
        }
        Resource assetResource = resourceResolver.getResource(resolvedFileReference);
        if (assetResource == null) {
            return "";
        }
        Asset asset = assetResource.adaptTo(Asset.class);
        if (asset == null) {
            return "";
        }
        return blankToEmpty(asset.getMetadataValue(DAM_DESCRIPTION));
    }

    private static String blankToEmpty(String value) {
        return isBlank(value) ? "" : value.trim();
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String delegateString(Supplier<String> supplier) {
        return blankToEmpty(fromDelegate(supplier));
    }

    private String property(String name) {
        if (resource == null || name == null) {
            return "";
        }
        return blankToEmpty(resource.getValueMap().get(name, String.class));
    }

    private boolean booleanProperty(String name) {
        if (resource == null || name == null) {
            return false;
        }
        Object value = resource.getValueMap().get(name);
        if (value instanceof Boolean) {
            return ((Boolean) value).booleanValue();
        }
        if (value instanceof String) {
            return "true".equalsIgnoreCase(((String) value).trim());
        }
        return false;
    }

    private String imageChildFileReference() {
        if (resource == null) {
            return "";
        }
        Resource image = resource.getChild("image");
        if (image == null) {
            return "";
        }
        return blankToEmpty(image.getValueMap().get("fileReference", String.class));
    }

    private String delegateImageFileReference() {
        Resource imageResource = fromDelegate(delegate == null ? null : delegate::getImageResource);
        if (imageResource == null) {
            return "";
        }
        return blankToEmpty(imageResource.getValueMap().get("fileReference", String.class));
    }

    /**
     * Non-blank flat {@code linkText} and {@code linkURL} win. A blank side of that
     * pair is filled from the first Core actions child. When both flat fields are
     * blank, Core actions children are used, then the core delegate.
     * A teaser link used only for "title from linked page" does not replace an authored action.
     */
    private List<ExportedAction> resolveActions() {
        List<ExportedAction> fromChild = readActionChildren();
        if (!resolvedLinkText.isEmpty() || !resolvedLinkUrl.isEmpty()) {
            List<ExportedAction> fromFields = new ArrayList<ExportedAction>();
            fromFields.add(actionFromFlatFields(fromChild));
            return fromFields;
        }
        if (!fromChild.isEmpty()) {
            return fromChild;
        }
        return readDelegateActions();
    }

    /**
     * Flat text and URL stay when they are non-blank. The first action child fills
     * only a blank side. Its target is kept only when the URL itself comes from that child.
     */
    private ExportedAction actionFromFlatFields(List<ExportedAction> fromChild) {
        String text = resolvedLinkText;
        String url = resolvedLinkUrl;
        String target = "";
        if (fromChild != null && !fromChild.isEmpty()) {
            ExportedAction first = fromChild.get(0);
            if (text.isEmpty()) {
                text = firstNonEmpty(first.getTitle(), first.getText());
            }
            if (url.isEmpty()) {
                url = blankToEmpty(first.getUrl());
                target = blankToEmpty(first.getTarget());
            }
        }
        return new ExportedAction(text, text, url, target);
    }

    private void backfillLinkFromActions() {
        if (exportedActions == null || exportedActions.isEmpty()) {
            return;
        }
        ExportedAction first = exportedActions.get(0);
        if (resolvedLinkText.isEmpty()) {
            resolvedLinkText = firstNonEmpty(first.getTitle(), first.getText());
        }
        if (resolvedLinkUrl.isEmpty()) {
            resolvedLinkUrl = blankToEmpty(first.getUrl());
        }
    }

    private List<ExportedAction> readActionChildren() {
        if (resource == null) {
            return Collections.emptyList();
        }
        Resource actions = resource.getChild(Teaser.NN_ACTIONS);
        if (actions == null) {
            return Collections.emptyList();
        }
        List<ExportedAction> items = new ArrayList<ExportedAction>();
        for (Resource child : actions.getChildren()) {
            ValueMap map = child.getValueMap();
            String text = actionText(map);
            String url = actionUrl(map);
            if (text.isEmpty() && url.isEmpty()) {
                continue;
            }
            items.add(new ExportedAction(text, text, url, actionTarget(map)));
        }
        return items;
    }

    @SuppressWarnings("deprecation")
    private List<ExportedAction> readDelegateActions() {
        List<ListItem> items = fromDelegate(delegate == null ? null : delegate::getActions);
        if (items == null || items.isEmpty()) {
            return Collections.emptyList();
        }
        List<ExportedAction> exported = new ArrayList<ExportedAction>();
        for (ListItem item : items) {
            if (item == null) {
                continue;
            }
            String text = "";
            String url = "";
            try {
                text = blankToEmpty(item.getTitle());
                url = blankToEmpty(item.getURL());
            } catch (RuntimeException ex) {
                continue;
            }
            if (text.isEmpty() && url.isEmpty()) {
                continue;
            }
            exported.add(new ExportedAction(text, text, url, ""));
        }
        return exported;
    }

    private static String actionText(ValueMap map) {
        String text = blankToEmpty(map.get(Teaser.PN_ACTION_TEXT, String.class));
        if (!text.isEmpty()) {
            return text;
        }
        return blankToEmpty(map.get("title", String.class));
    }

    private static String actionUrl(ValueMap map) {
        String url = blankToEmpty(map.get(Teaser.PN_ACTION_LINK, String.class));
        if (!url.isEmpty()) {
            return url;
        }
        url = blankToEmpty(map.get("url", String.class));
        if (!url.isEmpty()) {
            return url;
        }
        return blankToEmpty(map.get("linkURL", String.class));
    }

    private static String actionTarget(ValueMap map) {
        String target = blankToEmpty(map.get("linkTarget", String.class));
        if (!target.isEmpty()) {
            return target;
        }
        return blankToEmpty(map.get("target", String.class));
    }

    private static String firstNonEmpty(String... values) {
        if (values == null) {
            return "";
        }
        for (String value : values) {
            String trimmed = blankToEmpty(value);
            if (!trimmed.isEmpty()) {
                return trimmed;
            }
        }
        return "";
    }

    /**
     * Nested image object exported beside the flat {@code fileReference} and {@code src} fields.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static final class ExportedImage {

        private final String src;

        private final String fileReference;

        ExportedImage(String src) {
            this.src = src;
            this.fileReference = src;
        }

        public String getSrc() {
            return src;
        }

        public String getFileReference() {
            return fileReference;
        }
    }

    /**
     * One call to action. {@code title} and {@code text} carry the same label so either core name resolves.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static final class ExportedAction {

        private final String title;

        private final String text;

        private final String url;

        private final String target;

        ExportedAction(String title, String text, String url, String target) {
            this.title = blankToEmpty(title);
            this.text = blankToEmpty(text);
            this.url = blankToEmpty(url);
            this.target = blankToEmpty(target);
        }

        public String getTitle() {
            return title;
        }

        public String getText() {
            return text;
        }

        public String getUrl() {
            return url;
        }

        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        public String getTarget() {
            return target;
        }
    }
}
