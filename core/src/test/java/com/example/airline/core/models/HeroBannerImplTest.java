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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.testing.mock.caconfig.MockContextAwareConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.adobe.cq.wcm.core.components.internal.DataLayerConfig;
import com.adobe.cq.wcm.core.components.models.datalayer.ComponentData;
import com.adobe.cq.wcm.core.components.util.ComponentUtils;
import com.example.airline.core.testcontext.AppAemContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(AemContextExtension.class)
class HeroBannerImplTest {

    private static final String DESCRIBED_ASSET = "/content/dam/airline/described.jpg";
    private static final String UNDESCRIBED_ASSET = "/content/dam/airline/undescribed.jpg";
    private static final String TITLED_ASSET = "/content/dam/airline/titled.jpg";
    private static final String DAM_TITLE = "Family in the cabin";
    private static final String DAM_DESCRIPTION = "Children seated with a parent";

    private final AemContext context = AppAemContext.newAemContext();

    @BeforeEach
    void setUp() {
        context.addModelsForClasses(HeroBannerImpl.class, HeroBannerLinkImpl.class);
        context.create().resource("/content/airline", "sling:configRef", "/conf/airline");
        MockContextAwareConfig.writeConfiguration(context, "/content/airline", DataLayerConfig.class,
                "enabled", true);
        ensureDamAsset(DESCRIBED_ASSET, null, DAM_DESCRIPTION);
        ensureDamAsset(UNDESCRIBED_ASSET, null, null);
        ensureDamAsset(TITLED_ASSET, DAM_TITLE, DAM_DESCRIPTION);
    }

    @Test
    void authoredAltIsReturnedWhenSetAndNotDecorative() {
        HeroBanner model = model("authored-alt", props()
                .title("Flying with children")
                .introduction("Plan the journey before you fly.")
                .fileReference(TITLED_ASSET)
                .alt("Author provided alt")
                .decorative(false));

        assertEquals("Author provided alt", model.getAlt());
        assertEquals("Flying with children", model.getTitle());
        assertEquals("Plan the journey before you fly.", model.getIntroduction());
        assertEquals(TITLED_ASSET, model.getFileReference());
        assertEquals(TITLED_ASSET, model.getSrc());
        assertFalse(model.isDecorative());
        assertFalse(model.isEmpty());
        assertEquals(HeroBannerImpl.RESOURCE_TYPE, model.getExportedType());
    }

    @Test
    void blankAltUsesDamTitleEvenWhenDescriptionDiffers() {
        HeroBanner missingAlt = model("dam-alt-missing", props()
                .title("Flying with children")
                .fileReference(TITLED_ASSET)
                .decorative(false));
        HeroBanner emptyAlt = model("dam-alt-empty", props()
                .title("Flying with children")
                .fileReference(TITLED_ASSET)
                .alt("   ")
                .decorative(false));

        assertEquals(DAM_TITLE, missingAlt.getAlt());
        assertEquals(DAM_TITLE, emptyAlt.getAlt());
    }

    @Test
    void blankAltUsesComponentTitleWhenDamTitleIsMissing() {
        HeroBanner descriptionOnly = model("description-only", props()
                .title("Flying with children")
                .fileReference(DESCRIBED_ASSET)
                .alt("")
                .decorative(false));
        HeroBanner missingAsset = model("missing-asset", props()
                .title("Flying with children")
                .fileReference("/content/dam/airline/does-not-exist.jpg")
                .alt("   ")
                .decorative(false));

        assertEquals("Flying with children", descriptionOnly.getAlt());
        assertEquals("Flying with children", missingAsset.getAlt());
    }

    @Test
    void blankAltIsEmptyStringWhenNoDamTitleOrComponentTitle() {
        HeroBanner missingAsset = model("no-asset", props()
                .fileReference("/content/dam/airline/does-not-exist.jpg")
                .alt("")
                .decorative(false));
        HeroBanner undescribed = model("undescribed", props()
                .fileReference(UNDESCRIBED_ASSET)
                .decorative(false));
        HeroBanner descriptionOnly = model("description-only-no-title", props()
                .fileReference(DESCRIBED_ASSET)
                .alt("   ")
                .decorative(false));

        assertEquals("", missingAsset.getAlt());
        assertEquals("", undescribed.getAlt());
        assertEquals("", descriptionOnly.getAlt());
        assertNotNull(missingAsset.getAlt());
        assertNotNull(undescribed.getAlt());
        assertNotNull(descriptionOnly.getAlt());
    }

    @Test
    void decorativeImageForcesEmptyAlt() {
        HeroBanner withAuthoredAlt = model("decorative", props()
                .title("Flying with children")
                .fileReference(TITLED_ASSET)
                .alt("Author provided alt")
                .decorative(true));
        HeroBanner blankAlt = model("decorative-blank-alt", props()
                .title("Flying with children")
                .fileReference(TITLED_ASSET)
                .alt("")
                .decorative(true));

        assertEquals("", withAuthoredAlt.getAlt());
        assertEquals("", blankAlt.getAlt());
        assertTrue(withAuthoredAlt.isDecorative());
        assertTrue(blankAlt.isDecorative());
    }

    @Test
    void multifieldWithNoLinksIsAnEmptyList() {
        HeroBanner model = model("no-links", props()
                .title("Flying with children")
                .fileReference(DESCRIBED_ASSET));

        assertNotNull(model.getLinks());
        assertTrue(model.getLinks().isEmpty());
    }

    @Test
    void multifieldWithOneLinkNormalizesAnchor() {
        HeroBanner model = model("one-link", props()
                .title("Flying with children")
                .fileReference(DESCRIBED_ASSET),
                link("Fares", "fares"));

        List<HeroBanner.JumpLink> links = model.getLinks();
        assertEquals(1, links.size());
        assertEquals("Fares", links.get(0).getText());
        assertEquals("#fares", links.get(0).getAnchor());
        assertEquals("#fares", links.get(0).getHref());
    }

    @Test
    void multifieldWithSeveralLinksNormalizesAnchors() {
        HeroBanner model = model("several-links", props()
                .title("Flying with children")
                .fileReference(DESCRIBED_ASSET),
                link("Fares", "fares"),
                link("Baggage", "#baggage"),
                link("Seating", "  ##seating  "));

        List<HeroBanner.JumpLink> links = model.getLinks();
        assertEquals(3, links.size());
        assertEquals("#fares", links.get(0).getHref());
        assertEquals("#baggage", links.get(1).getAnchor());
        assertEquals("#seating", links.get(2).getHref());
        assertEquals("Baggage", links.get(1).getText());
        assertEquals("Seating", links.get(2).getText());
    }

    @Test
    void emptyWhenTitleAndImageAreBlank() {
        HeroBanner bothBlank = model("empty", props().introduction("Only an introduction"));
        HeroBanner whitespace = model("whitespace", props().title("   ").fileReference("  ").alt("unused"));

        assertTrue(bothBlank.isEmpty());
        assertEquals("", bothBlank.getTitle());
        assertEquals("", bothBlank.getSrc());
        assertEquals("", bothBlank.getAlt());
        assertTrue(whitespace.isEmpty());
        assertEquals("", whitespace.getTitle());
        assertEquals("", whitespace.getSrc());
        assertEquals("unused", whitespace.getAlt());
    }

    @Test
    void notEmptyWhenTitleOrImageIsPresent() {
        HeroBanner titleOnly = model("title-only", props().title("Flying with children"));
        HeroBanner imageOnly = model("image-only", props().fileReference(DESCRIBED_ASSET));

        assertFalse(titleOnly.isEmpty());
        assertEquals("", titleOnly.getSrc());
        assertEquals("Flying with children", titleOnly.getAlt());
        assertFalse(imageOnly.isEmpty());
        assertEquals("", imageOnly.getTitle());
        assertEquals(DESCRIBED_ASSET, imageOnly.getSrc());
        assertEquals("", imageOnly.getAlt());
    }

    @Test
    void dataLayerIsPresentWhenTheBannerHasContentAndTheIdIsStable() throws Exception {
        HeroBanner model = model("data-layer", props()
                .title("Flying with children")
                .introduction("Plan the journey before you fly.")
                .fileReference(DESCRIBED_ASSET),
                link("Fares", "fares"));
        Resource resource = context.resourceResolver().getResource("/content/airline/herobanner/data-layer");
        String expectedId = ComponentUtils.getId(resource, null, null);

        assertEquals(expectedId, model.getId());
        ComponentData data = model.getData();
        assertNotNull(data);
        assertEquals(expectedId, data.getId());
        assertEquals(HeroBannerImpl.RESOURCE_TYPE, data.getType());
        assertEquals("Flying with children", data.getTitle());
        assertNull(data.getLinkUrl());

        String json = model.getDataLayer();
        assertNotNull(json);
        assertEquals(data.getJson(), json);

        JsonNode root = new ObjectMapper().readTree(json);
        assertEquals(1, root.size(), json);
        JsonNode component = root.get(expectedId);
        assertNotNull(component, json);
        assertEquals(HeroBannerImpl.RESOURCE_TYPE, component.get("@type").asText());
        assertEquals("Flying with children", component.get("dc:title").asText());
        assertEquals(2, component.size(), json);
        assertFalse(component.has("xdm:linkURL"));
        assertFalse(json.contains("#fares"));
        assertFalse(json.contains("Fares"));

        HeroBanner again = resource.adaptTo(HeroBanner.class);
        assertNotNull(again);
        assertEquals(model.getId(), again.getId());
        assertEquals(model.getDataLayer(), again.getDataLayer());

        JsonNode exported = new ObjectMapper().readTree(new ObjectMapper().writeValueAsString(model));
        assertEquals(expectedId, exported.get("id").asText());
        assertTrue(exported.get("dataLayer").isTextual(), exported.toString());
        assertEquals(json, exported.get("dataLayer").asText());
        assertFalse(exported.has("appliedCssClassNames"), exported.toString());
    }

    @Test
    void dataLayerIsAbsentWhenTheCoreComponentsConfigIsDisabled() {
        Resource resource = context.create().resource("/content/other/herobanner",
                "sling:resourceType", HeroBannerImpl.RESOURCE_TYPE,
                "title", "Flying with children",
                "fileReference", DESCRIBED_ASSET);

        HeroBanner model = resource.adaptTo(HeroBanner.class);

        assertNotNull(model);
        assertNotNull(model.getId());
        assertEquals(ComponentUtils.getId(resource, null, null), model.getId());
        assertNull(model.getData());
        assertNull(model.getDataLayer());
    }

    @Test
    void requestAdaptationMatchesResourceAdaptation() {
        Resource resource = context.create().resource("/content/airline/herobanner/request",
                "sling:resourceType", HeroBannerImpl.RESOURCE_TYPE,
                "title", "From the request",
                "fileReference", DESCRIBED_ASSET,
                "alt", "Request alt");
        context.currentResource(resource);

        HeroBanner fromResource = resource.adaptTo(HeroBanner.class);
        HeroBanner fromRequest = context.request().adaptTo(HeroBanner.class);

        assertNotNull(fromResource);
        assertNotNull(fromRequest);
        assertEquals(fromResource.getTitle(), fromRequest.getTitle());
        assertEquals("Request alt", fromRequest.getAlt());
        assertEquals(DESCRIBED_ASSET, fromRequest.getSrc());
        assertFalse(fromRequest.isEmpty());
    }

    private void ensureDamAsset(String path, String title, String description) {
        if (context.resourceResolver().getResource(path) != null) {
            return;
        }
        context.create().resource(path, "jcr:primaryType", "dam:Asset");
        context.create().resource(path + "/jcr:content", "jcr:primaryType", "dam:AssetContent");
        Map<String, Object> metadata = new HashMap<String, Object>();
        metadata.put("jcr:primaryType", "nt:unstructured");
        if (title != null) {
            metadata.put("dc:title", title);
        }
        if (description != null) {
            metadata.put("dc:description", description);
        }
        context.create().resource(path + "/jcr:content/metadata", metadata);
    }

    private HeroBanner model(String name, Props properties, Link... links) {
        Resource resource = context.create().resource("/content/airline/herobanner/" + name, properties.values);
        if (links.length > 0) {
            Resource linksNode = context.create().resource(resource, "links");
            for (int i = 0; i < links.length; i++) {
                context.create().resource(linksNode, "item" + i,
                        "text", links[i].text,
                        "anchor", links[i].anchor);
            }
        }
        HeroBanner model = resource.adaptTo(HeroBanner.class);
        assertNotNull(model, "HeroBanner should adapt for " + name);
        return model;
    }

    private static Props props() {
        return new Props();
    }

    private static Link link(String text, String anchor) {
        return new Link(text, anchor);
    }

    private static final class Props {
        private final Map<String, Object> values = new HashMap<String, Object>();

        private Props() {
            values.put("sling:resourceType", HeroBannerImpl.RESOURCE_TYPE);
        }

        private Props title(String title) {
            values.put("title", title);
            return this;
        }

        private Props introduction(String introduction) {
            values.put("introduction", introduction);
            return this;
        }

        private Props fileReference(String fileReference) {
            values.put("fileReference", fileReference);
            return this;
        }

        private Props alt(String alt) {
            values.put("alt", alt);
            return this;
        }

        private Props decorative(boolean decorative) {
            values.put("decorative", decorative);
            return this;
        }
    }

    private static final class Link {
        private final String text;
        private final String anchor;

        private Link(String text, String anchor) {
            this.text = text;
            this.anchor = anchor;
        }
    }
}
