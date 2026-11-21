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
import java.util.Map;

import org.apache.sling.api.resource.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.adobe.cq.export.json.ComponentExporter;
import com.adobe.cq.wcm.core.components.models.Teaser;
import com.example.airline.core.testcontext.AppAemContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(AemContextExtension.class)
@SuppressWarnings("deprecation")
class JoeyClubTeaserImplTest {

    private static final String PAGE = "/content/airline/joey-club";
    private static final String DESCRIBED_ASSET = "/content/dam/airline/joey-described.jpg";
    private static final String UNDESCRIBED_ASSET = "/content/dam/airline/joey-undescribed.jpg";
    private static final String DAM_DESCRIPTION = "Bluey and Bingo beside the aircraft";
    private static final String RICH_TEXT = "<p>Join the <strong>Joey Club</strong>.</p>";

    private final AemContext context = AppAemContext.newAemContext();

    @BeforeEach
    void setUp() {
        context.addModelsForClasses(JoeyClubTeaserImpl.class);
        context.create().page(PAGE);
        context.currentPage(PAGE);
        if (context.resourceResolver().getResource("/apps/airline/components/teaser") == null) {
            context.create().resource("/apps/airline/components/teaser",
                    "jcr:primaryType", "cq:Component",
                    "sling:resourceSuperType", "core/wcm/components/teaser/v2/teaser");
        }
        ensureDamAsset(DESCRIBED_ASSET, DAM_DESCRIPTION);
        ensureDamAsset(UNDESCRIBED_ASSET, null);
    }

    @Test
    void spaExporterAdaptsAirlineTeaserToProjectModel() throws Exception {
        context.addModelsForClasses(com.adobe.cq.wcm.core.components.internal.models.v2.TeaserImpl.class);
        model("spa-export", props()
                .title("sdsd")
                .description("<p>sfdsfd</p>")
                .fileReference(DESCRIBED_ASSET)
                .linkText("sdsd")
                .linkURL("google.com"));

        Teaser teaser = context.request().adaptTo(Teaser.class);
        assertTrue(teaser instanceof JoeyClubTeaserImpl, "airline/components/teaser must not use the core teaser model");
        assertEquals("sdsd", teaser.getTitle());
        assertEquals("<p>sfdsfd</p>", teaser.getDescription());
        assertEquals(JoeyClubTeaserImpl.RESOURCE_TYPE, teaser.getExportedType());

        ComponentExporter exporter = context.request().adaptTo(ComponentExporter.class);
        assertTrue(exporter instanceof JoeyClubTeaserImpl);
        assertEquals(JoeyClubTeaserImpl.RESOURCE_TYPE, exporter.getExportedType());

        JsonNode exported = new ObjectMapper().readTree(new ObjectMapper().writeValueAsString(exporter));
        assertEquals("sdsd", exported.get("title").asText());
        assertEquals("<p>sfdsfd</p>", exported.get("description").asText());
        assertEquals(DESCRIBED_ASSET, exported.get("fileReference").asText());
        assertEquals(DESCRIBED_ASSET, exported.get("image").get("src").asText());
        assertEquals("sdsd", exported.get("linkText").asText());
        assertEquals("google.com", exported.get("linkURL").asText());
        assertEquals("sdsd", exported.get("actions").get(0).get("title").asText());
        assertEquals("google.com", exported.get("actions").get(0).get("url").asText());
        assertEquals(JoeyClubTeaserImpl.RESOURCE_TYPE, exported.get(":type").asText());
    }

    @Test
    void authoredTitleRichTextAndCtaAreExported() throws Exception {
        JoeyClubTeaser model = model("authored", props()
                .title("  Joey Club  ")
                .description("  " + RICH_TEXT + "  ")
                .fileReference(DESCRIBED_ASSET)
                .linkText(" Explore Qantas and Bluey ")
                .linkURL(" https://www.qantas.com/joey ")
                .property("alt", "Author provided alt")
                .property("isDecorative", true)
                .property("linkTarget", "_blank"));

        assertEquals("Joey Club", model.getTitle());
        assertEquals(RICH_TEXT, model.getDescription());
        assertEquals("Explore Qantas and Bluey", model.getLinkText());
        assertEquals("https://www.qantas.com/joey", model.getLinkURL());
        assertEquals("", model.getRel());
        assertEquals(DESCRIBED_ASSET, model.getFileReference());
        assertEquals(DESCRIBED_ASSET, model.getSrc());
        assertEquals(DAM_DESCRIPTION, model.getAltText());
        assertFalse(model.isEmpty());
        assertEquals("h2", model.getHeadingLevel());
        assertEquals(JoeyClubTeaserImpl.RESOURCE_TYPE, model.getExportedType());
        assertNotNull(model.getId());
        assertFalse(model.getId().isEmpty());
        assertDoesNotThrow(model::getActions);
        assertDoesNotThrow(model::getLink);
        assertDoesNotThrow(model::getImageResource);
        assertDoesNotThrow(model::getPretitle);

        JsonNode exported = new ObjectMapper().readTree(new ObjectMapper().writeValueAsString(model));
        assertEquals("Joey Club", exported.get("title").asText());
        assertEquals(RICH_TEXT, exported.get("description").asText());
        assertEquals("Explore Qantas and Bluey", exported.get("linkText").asText());
        assertEquals("https://www.qantas.com/joey", exported.get("linkURL").asText());
        assertEquals(DESCRIBED_ASSET, exported.get("fileReference").asText());
        assertEquals(DESCRIBED_ASSET, exported.get("src").asText());
        assertEquals(DAM_DESCRIPTION, exported.get("alt").asText());
        assertEquals("", exported.get("rel").asText());
        assertEquals("h2", exported.get("headingLevel").asText());
        assertEquals("h2", exported.get("titleType").asText());
        assertEquals(DESCRIBED_ASSET, exported.get("image").get("src").asText());
        assertEquals(DESCRIBED_ASSET, exported.get("image").get("fileReference").asText());
        assertEquals("Explore Qantas and Bluey", exported.get("actions").get(0).get("title").asText());
        assertEquals("Explore Qantas and Bluey", exported.get("actions").get(0).get("text").asText());
        assertEquals("https://www.qantas.com/joey", exported.get("actions").get(0).get("url").asText());
        assertFalse(exported.get("empty").asBoolean());
        assertEquals(model.getId(), exported.get("id").asText());
        assertEquals(JoeyClubTeaserImpl.RESOURCE_TYPE, exported.get(":type").asText());
        assertFalse(exported.has("decorative"), exported.toString());
        assertFalse(exported.has("linkTarget"), exported.toString());
        assertFalse(exported.toString().contains("noopener"), exported.toString());
        assertFalse(exported.toString().contains("_blank"), exported.toString());
    }

    @Test
    void storedLinkTargetDoesNotOpenANewTab() throws Exception {
        JoeyClubTeaser model = model("stored-target", props()
                .title("Joey Club")
                .linkText("Explore Qantas and Bluey")
                .linkURL("/content/airline/joey")
                .property("linkTarget", "_blank"));

        assertEquals("", model.getRel());

        JsonNode exported = new ObjectMapper().readTree(new ObjectMapper().writeValueAsString(model));
        assertEquals("", exported.get("rel").asText());
        assertFalse(exported.has("linkTarget"), exported.toString());
        assertFalse(exported.toString().contains("_blank"), exported.toString());
        assertFalse(exported.toString().contains("noopener"), exported.toString());
    }

    @Test
    void altUsesDamDescriptionWhenImageIsPresent() {
        JoeyClubTeaser fromDam = model("dam-alt", props()
                .title("Joey Club")
                .fileReference(DESCRIBED_ASSET)
                .property("alt", "   "));
        JoeyClubTeaser ignoresAuthoredAlt = model("ignores-authored-alt", props()
                .title("Joey Club")
                .fileReference(DESCRIBED_ASSET)
                .property("alt", "  Kids with Bluey  ")
                .property("isDecorative", true));
        JoeyClubTeaser undescribed = model("undescribed", props()
                .title("Joey Club")
                .fileReference(UNDESCRIBED_ASSET));
        JoeyClubTeaser missingAsset = model("missing-asset", props()
                .title("Joey Club")
                .fileReference("/content/dam/airline/does-not-exist.jpg"));
        JoeyClubTeaser noImage = model("no-image", props()
                .title("Joey Club")
                .property("alt", "Kids with Bluey"));

        assertEquals(DAM_DESCRIPTION, fromDam.getAltText());
        assertEquals(DAM_DESCRIPTION, ignoresAuthoredAlt.getAltText());
        assertEquals("", undescribed.getAltText());
        assertEquals("", missingAsset.getAltText());
        assertEquals("", noImage.getAltText());
    }

    @Test
    void headingElementPolicyReturnsH3() {
        policy("headingElement", "h3", "titleType", "h2");

        JoeyClubTeaser model = model("policy-h3", props().title("Joey Club"));

        assertEquals("h3", model.getHeadingLevel());
        assertEquals("h3", model.getTitleType());
    }

    @Test
    void missingPolicyDefaultsHeadingToH2() {
        JoeyClubTeaser model = model("policy-missing", props().title("Joey Club"));

        assertEquals("h2", model.getHeadingLevel());
    }

    @Test
    void illegalHeadingPolicyDefaultsToH2() {
        policy("headingElement", "h1");

        JoeyClubTeaser model = model("policy-illegal", props().title("Joey Club"));

        assertEquals("h2", model.getHeadingLevel());
        assertEquals("h2", model.getTitleType());
    }

    @Test
    void titleTypeIsUsedWhenHeadingElementIsMissing() {
        policy("titleType", "h4");

        JoeyClubTeaser model = model("policy-title-type", props().title("Joey Club"));

        assertEquals("h4", model.getHeadingLevel());
    }

    @Test
    void emptyWhenTitleDescriptionAndImageAreBlank() {
        JoeyClubTeaser blank = model("empty", props());
        JoeyClubTeaser whitespace = model("whitespace", props()
                .title("   ")
                .description("  ")
                .fileReference("   ")
                .linkText("Explore"));
        JoeyClubTeaser titleOnly = model("title-only", props().title("Joey Club"));
        JoeyClubTeaser descriptionOnly = model("description-only", props().description("<p>Bluey</p>"));
        JoeyClubTeaser imageOnly = model("image-only", props().fileReference(DESCRIBED_ASSET));

        assertTrue(blank.isEmpty());
        assertTrue(whitespace.isEmpty());
        assertEquals("", whitespace.getTitle());
        assertEquals("", whitespace.getDescription());
        assertEquals("", whitespace.getFileReference());
        assertFalse(titleOnly.isEmpty());
        assertFalse(descriptionOnly.isEmpty());
        assertFalse(imageOnly.isEmpty());
    }

    @Test
    void coreAuthoredPropertiesAreExportedAsFlatFieldsAndCoreShape() throws Exception {
        Resource resource = context.create().resource(PAGE + "/jcr:content/core-authored",
                "sling:resourceType", JoeyClubTeaserImpl.RESOURCE_TYPE,
                "jcr:title", "Qantas and Bluey",
                "jcr:description", RICH_TEXT);
        context.create().resource(resource.getPath() + "/image",
                "fileReference", DESCRIBED_ASSET);
        context.create().resource(resource.getPath() + "/actions/item0",
                "text", "Explore Qantas and Bluey",
                "link", "/content/airline/us/en/bluey");
        context.currentResource(resource);

        JoeyClubTeaser model = context.request().adaptTo(JoeyClubTeaser.class);
        assertNotNull(model);
        assertEquals("Qantas and Bluey", model.getTitle());
        assertEquals(RICH_TEXT, model.getDescription());
        assertEquals(DESCRIBED_ASSET, model.getFileReference());
        assertEquals(DESCRIBED_ASSET, model.getSrc());
        assertEquals(DAM_DESCRIPTION, model.getAltText());
        assertEquals("Explore Qantas and Bluey", model.getLinkText());
        assertEquals("/content/airline/us/en/bluey", model.getLinkURL());
        assertFalse(model.isEmpty());

        JsonNode exported = new ObjectMapper().readTree(new ObjectMapper().writeValueAsString(model));
        assertEquals("Qantas and Bluey", exported.get("title").asText());
        assertEquals(RICH_TEXT, exported.get("description").asText());
        assertEquals(DESCRIBED_ASSET, exported.get("fileReference").asText());
        assertEquals(DESCRIBED_ASSET, exported.get("src").asText());
        assertEquals(DESCRIBED_ASSET, exported.get("image").get("src").asText());
        assertEquals(DESCRIBED_ASSET, exported.get("image").get("fileReference").asText());
        assertEquals("Explore Qantas and Bluey", exported.get("actions").get(0).get("title").asText());
        assertEquals("Explore Qantas and Bluey", exported.get("actions").get(0).get("text").asText());
        assertEquals("/content/airline/us/en/bluey", exported.get("actions").get(0).get("url").asText());
        assertEquals("h2", exported.get("titleType").asText());
        assertEquals("h2", exported.get("headingLevel").asText());
        assertFalse(exported.toString().contains("_blank"), exported.toString());
        assertFalse(exported.toString().contains("noopener"), exported.toString());

        Resource customWins = context.create().resource(PAGE + "/jcr:content/custom-wins",
                "sling:resourceType", JoeyClubTeaserImpl.RESOURCE_TYPE,
                "title", "Custom title",
                "jcr:title", "Page title",
                "description", "<p>Custom</p>",
                "jcr:description", "<p>Core</p>",
                "fileReference", DESCRIBED_ASSET);
        context.currentResource(customWins);
        JoeyClubTeaser custom = context.request().adaptTo(JoeyClubTeaser.class);
        assertNotNull(custom);
        assertEquals("Custom title", custom.getTitle());
        assertEquals("<p>Custom</p>", custom.getDescription());
        assertEquals(DESCRIBED_ASSET, custom.getFileReference());
    }

    @Test
    void flatDialogFieldsWinOverCoreActionChildren() throws Exception {
        Resource resource = context.create().resource(PAGE + "/jcr:content/flat-wins",
                "sling:resourceType", JoeyClubTeaserImpl.RESOURCE_TYPE,
                "title", "Custom title",
                "jcr:title", "Core title",
                "description", "<p>Custom</p>",
                "jcr:description", "<p>Core</p>",
                "fileReference", DESCRIBED_ASSET,
                "linkText", " Custom CTA ",
                "linkURL", " /content/airline/custom ",
                "actionsEnabled", true);
        context.create().resource(resource.getPath() + "/image",
                "fileReference", UNDESCRIBED_ASSET);
        context.create().resource(resource.getPath() + "/actions/item0",
                "text", "Core CTA",
                "link", "/content/airline/core",
                "linkTarget", "_blank");
        context.currentResource(resource);

        JoeyClubTeaser model = context.request().adaptTo(JoeyClubTeaser.class);
        assertNotNull(model);
        assertEquals("Custom title", model.getTitle());
        assertEquals("<p>Custom</p>", model.getDescription());
        assertEquals(DESCRIBED_ASSET, model.getFileReference());
        assertEquals(DESCRIBED_ASSET, model.getSrc());
        assertEquals("Custom CTA", model.getLinkText());
        assertEquals("/content/airline/custom", model.getLinkURL());

        JsonNode exported = new ObjectMapper().readTree(new ObjectMapper().writeValueAsString(model));
        assertEquals("Custom title", exported.get("title").asText());
        assertEquals("<p>Custom</p>", exported.get("description").asText());
        assertEquals(DESCRIBED_ASSET, exported.get("fileReference").asText());
        assertEquals("Custom CTA", exported.get("linkText").asText());
        assertEquals("/content/airline/custom", exported.get("linkURL").asText());
        assertEquals(1, exported.get("actions").size());
        assertEquals("Custom CTA", exported.get("actions").get(0).get("title").asText());
        assertEquals("Custom CTA", exported.get("actions").get(0).get("text").asText());
        assertEquals("/content/airline/custom", exported.get("actions").get(0).get("url").asText());
        assertFalse(exported.get("actions").get(0).has("target"), exported.toString());
        assertFalse(exported.toString().contains("Core CTA"), exported.toString());
        assertFalse(exported.toString().contains("_blank"), exported.toString());
    }

    @Test
    void blankFlatLinkSideIsFilledFromCoreAction() throws Exception {
        Resource textOnly = context.create().resource(PAGE + "/jcr:content/flat-text",
                "sling:resourceType", JoeyClubTeaserImpl.RESOURCE_TYPE,
                "title", "Joey Club",
                "linkText", "Custom CTA");
        context.create().resource(textOnly.getPath() + "/actions/item0",
                "text", "Core CTA",
                "link", "/content/airline/core",
                "linkTarget", "_blank");
        context.currentResource(textOnly);

        JoeyClubTeaser textModel = context.request().adaptTo(JoeyClubTeaser.class);
        assertNotNull(textModel);
        assertEquals("Custom CTA", textModel.getLinkText());
        assertEquals("/content/airline/core", textModel.getLinkURL());

        JsonNode textExported = new ObjectMapper().readTree(new ObjectMapper().writeValueAsString(textModel));
        assertEquals("Custom CTA", textExported.get("actions").get(0).get("title").asText());
        assertEquals("/content/airline/core", textExported.get("actions").get(0).get("url").asText());
        assertEquals("_blank", textExported.get("actions").get(0).get("target").asText());

        Resource urlOnly = context.create().resource(PAGE + "/jcr:content/flat-url",
                "sling:resourceType", JoeyClubTeaserImpl.RESOURCE_TYPE,
                "title", "Joey Club",
                "linkURL", "/content/airline/custom");
        context.create().resource(urlOnly.getPath() + "/actions/item0",
                "text", "Core CTA",
                "link", "/content/airline/core",
                "linkTarget", "_blank");
        context.currentResource(urlOnly);

        JoeyClubTeaser urlModel = context.request().adaptTo(JoeyClubTeaser.class);
        assertNotNull(urlModel);
        assertEquals("Core CTA", urlModel.getLinkText());
        assertEquals("/content/airline/custom", urlModel.getLinkURL());

        JsonNode urlExported = new ObjectMapper().readTree(new ObjectMapper().writeValueAsString(urlModel));
        assertEquals("Core CTA", urlExported.get("actions").get(0).get("title").asText());
        assertEquals("/content/airline/custom", urlExported.get("actions").get(0).get("url").asText());
        assertFalse(urlExported.get("actions").get(0).has("target"), urlExported.toString());
        assertFalse(urlExported.toString().contains("_blank"), urlExported.toString());
    }

    @Test
    void blankFlatLinkFieldsStillReadCoreActions() throws Exception {
        Resource resource = context.create().resource(PAGE + "/jcr:content/blank-flat",
                "sling:resourceType", JoeyClubTeaserImpl.RESOURCE_TYPE,
                "title", "Joey Club",
                "linkText", "   ",
                "linkURL", "  ",
                "actionsEnabled", true);
        context.create().resource(resource.getPath() + "/actions/item0",
                "text", "Core CTA",
                "link", "/content/airline/core");
        context.currentResource(resource);

        JoeyClubTeaser model = context.request().adaptTo(JoeyClubTeaser.class);
        assertNotNull(model);
        assertEquals("Core CTA", model.getLinkText());
        assertEquals("/content/airline/core", model.getLinkURL());

        JsonNode exported = new ObjectMapper().readTree(new ObjectMapper().writeValueAsString(model));
        assertEquals("Core CTA", exported.get("actions").get(0).get("title").asText());
        assertEquals("Core CTA", exported.get("actions").get(0).get("text").asText());
        assertEquals("/content/airline/core", exported.get("actions").get(0).get("url").asText());
    }

    private void policy(String name, String value, String... more) {
        context.currentPage(PAGE);
        Object[] properties = new Object[2 + more.length];
        properties[0] = name;
        properties[1] = value;
        System.arraycopy(more, 0, properties, 2, more.length);
        context.contentPolicyMapping(JoeyClubTeaserImpl.RESOURCE_TYPE, properties);
    }

    private void ensureDamAsset(String path, String description) {
        if (context.resourceResolver().getResource(path) != null) {
            return;
        }
        context.create().resource(path, "jcr:primaryType", "dam:Asset");
        context.create().resource(path + "/jcr:content", "jcr:primaryType", "dam:AssetContent");
        Map<String, Object> metadata = new HashMap<String, Object>();
        metadata.put("jcr:primaryType", "nt:unstructured");
        if (description != null) {
            metadata.put("dc:description", description);
        }
        context.create().resource(path + "/jcr:content/metadata", metadata);
    }

    private JoeyClubTeaser model(String name, Props properties) {
        Resource resource = context.create().resource(PAGE + "/jcr:content/" + name, properties.values);
        context.currentResource(resource);
        JoeyClubTeaser model = context.request().adaptTo(JoeyClubTeaser.class);
        assertNotNull(model, "JoeyClubTeaser should adapt for " + name);
        return model;
    }

    private static Props props() {
        return new Props();
    }

    private static final class Props {
        private final Map<String, Object> values = new HashMap<String, Object>();

        private Props() {
            values.put("sling:resourceType", JoeyClubTeaserImpl.RESOURCE_TYPE);
        }

        private Props title(String title) {
            values.put("title", title);
            return this;
        }

        private Props description(String description) {
            values.put("description", description);
            return this;
        }

        private Props fileReference(String fileReference) {
            values.put("fileReference", fileReference);
            return this;
        }

        private Props linkURL(String linkURL) {
            values.put("linkURL", linkURL);
            return this;
        }

        private Props linkText(String linkText) {
            values.put("linkText", linkText);
            return this;
        }

        private Props property(String name, Object value) {
            values.put(name, value);
            return this;
        }
    }
}
