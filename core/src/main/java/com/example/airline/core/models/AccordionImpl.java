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

import javax.annotation.PostConstruct;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Exporter;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.SlingObject;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

import com.adobe.cq.export.json.ComponentExporter;
import com.adobe.cq.export.json.ExporterConstants;
import com.adobe.cq.wcm.core.components.util.ComponentUtils;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@Model(
        adaptables = SlingHttpServletRequest.class,
        adapters = { Accordion.class, ComponentExporter.class },
        resourceType = AccordionImpl.RESOURCE_TYPE,
        defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
@Exporter(
        name = ExporterConstants.SLING_MODEL_EXPORTER_NAME,
        extensions = ExporterConstants.SLING_MODEL_EXTENSION)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AccordionImpl implements Accordion {

    public static final String RESOURCE_TYPE = "airline/components/accordion";

    static final String TARGET_BLANK = "_blank";

    static final String REL_EXTERNAL = "noopener noreferrer";

    @ValueMapValue
    private String title;

    @ValueMapValue
    private String introduction;

    @SlingObject
    private Resource resource;

    private String resolvedTitle = "";

    private String resolvedIntroduction = "";

    private List<AccordionItem> items = Collections.emptyList();

    private boolean empty;

    private String id;

    @PostConstruct
    protected void init() {
        resolvedTitle = blankToEmpty(title);
        resolvedIntroduction = blankToEmpty(introduction);
        items = readItems(resource);
        empty = resolvedTitle.isEmpty() && resolvedIntroduction.isEmpty() && items.isEmpty();
        if (resource != null) {
            id = ComponentUtils.getId(resource, null, null);
        }
    }

    @Override
    public String getTitle() {
        return resolvedTitle;
    }

    @Override
    public String getIntroduction() {
        return resolvedIntroduction;
    }

    @Override
    public List<AccordionItem> getItems() {
        return items;
    }

    @Override
    @JsonProperty("empty")
    public boolean isEmpty() {
        return empty;
    }

    @Override
    @JsonProperty("id")
    public String getId() {
        return id;
    }

    @Override
    @JsonProperty(ExporterConstants.PN_TYPE)
    public String getExportedType() {
        return RESOURCE_TYPE;
    }

    private static List<AccordionItem> readItems(Resource resource) {
        if (resource == null) {
            return Collections.emptyList();
        }
        Resource itemsResource = resource.getChild("items");
        if (itemsResource == null) {
            return Collections.emptyList();
        }
        List<AccordionItem> collected = new ArrayList<AccordionItem>();
        for (Resource child : itemsResource.getChildren()) {
            if (!isContentNode(child)) {
                continue;
            }
            Item item = Item.from(child);
            if (item != null) {
                collected.add(item);
            }
        }
        return Collections.unmodifiableList(collected);
    }

    private static List<AccordionItem.Link> readLinks(Resource item) {
        Resource linksResource = item == null ? null : item.getChild("links");
        if (linksResource == null) {
            return Collections.emptyList();
        }
        List<AccordionItem.Link> collected = new ArrayList<AccordionItem.Link>();
        for (Resource child : linksResource.getChildren()) {
            if (!isContentNode(child)) {
                continue;
            }
            Link link = Link.from(child);
            if (link != null) {
                collected.add(link);
            }
        }
        return Collections.unmodifiableList(collected);
    }

    private static boolean isContentNode(Resource child) {
        if (child == null || child.getName() == null) {
            return false;
        }
        String name = child.getName();
        return !name.startsWith("jcr:") && !name.startsWith("rep:") && !name.startsWith("cq:");
    }

    private static String property(Resource resource, String name) {
        if (resource == null || name == null) {
            return "";
        }
        return blankToEmpty(resource.getValueMap().get(name, String.class));
    }

    /**
     * True when the URL has an http or https scheme and a host.
     * Content paths and other relative values stay in the same tab.
     */
    static boolean isAbsoluteHttpUrl(String url) {
        if (url == null) {
            return false;
        }
        String value = url.trim();
        int schemeSep = value.indexOf("://");
        if (schemeSep < 1) {
            return false;
        }
        String scheme = value.substring(0, schemeSep).toLowerCase(Locale.ROOT);
        if (!"http".equals(scheme) && !"https".equals(scheme)) {
            return false;
        }
        String remainder = value.substring(schemeSep + 3);
        if (remainder.isEmpty()) {
            return false;
        }
        int hostEnd = remainder.length();
        for (int index = 0; index < remainder.length(); index++) {
            char character = remainder.charAt(index);
            if (character == '/' || character == '?' || character == '#') {
                hostEnd = index;
                break;
            }
        }
        return hostEnd > 0;
    }

    private static String blankToEmpty(String value) {
        return isBlank(value) ? "" : value.trim();
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static final class Item implements AccordionItem {

        private final String id;

        private final String title;

        private final String text;

        private final List<AccordionItem.Link> links;

        private Item(String id, String title, String text, List<AccordionItem.Link> links) {
            this.id = id;
            this.title = title;
            this.text = text;
            this.links = links;
        }

        static Item from(Resource resource) {
            String itemTitle = property(resource, "itemTitle");
            String itemText = property(resource, "itemText");
            List<AccordionItem.Link> itemLinks = readLinks(resource);
            if (itemTitle.isEmpty() && itemText.isEmpty() && itemLinks.isEmpty()) {
                return null;
            }
            return new Item(resource.getName(), itemTitle, itemText, itemLinks);
        }

        @Override
        public String getId() {
            return id;
        }

        @Override
        public String getTitle() {
            return title;
        }

        @Override
        public String getText() {
            return text;
        }

        @Override
        public List<Link> getLinks() {
            return links;
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static final class Link implements AccordionItem.Link {

        private final String text;

        private final String url;

        private final String target;

        private final String rel;

        private Link(String text, String url) {
            this.text = text;
            this.url = url;
            if (isAbsoluteHttpUrl(url)) {
                this.target = TARGET_BLANK;
                this.rel = REL_EXTERNAL;
            } else {
                this.target = null;
                this.rel = null;
            }
        }

        static Link from(Resource resource) {
            String linkText = property(resource, "linkText");
            String linkURL = property(resource, "linkURL");
            if (linkText.isEmpty() && linkURL.isEmpty()) {
                return null;
            }
            return new Link(linkText, linkURL);
        }

        @Override
        public String getText() {
            return text;
        }

        @Override
        public String getUrl() {
            return url;
        }

        @Override
        public String getTarget() {
            return target;
        }

        @Override
        public String getRel() {
            return rel;
        }
    }
}
