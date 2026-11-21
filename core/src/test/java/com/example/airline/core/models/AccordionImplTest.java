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

import org.apache.sling.api.resource.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.adobe.cq.export.json.ComponentExporter;
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
class AccordionImplTest {

    private static final String PAGE = "/content/airline/infants";

    private static final String RICH_TEXT = "<ul><li>Infants can travel on a lap.</li></ul>";

    private final AemContext context = AppAemContext.newAemContext();

    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        context.addModelsForClasses(AccordionImpl.class);
        context.create().page(PAGE);
        context.currentPage(PAGE);
    }

    @Test
    void authoredTitleIntroductionItemsAndLinksAreExported() throws Exception {
        Resource accordion = context.create().resource(PAGE + "/jcr:content/booking",
                "sling:resourceType", AccordionImpl.RESOURCE_TYPE,
                "title", "  Booking for infants (under 2 years)  ",
                "introduction", "  Learn how to book babies on a flight.  ");
        Resource domestic = context.create().resource(accordion.getPath() + "/items/domestic",
                "itemTitle", "  Infants on domestic flights  ",
                "itemText", "  " + RICH_TEXT + "  ");
        context.create().resource(domestic.getPath() + "/links/bassinet",
                "linkText", "  More about bassinets  ",
                "linkURL", "  /content/airline/bassinet  ");
        context.create().resource(domestic.getPath() + "/links/contact",
                "linkText", "  Contact us to make a booking  ",
                "linkURL", "  https://www.example.com/book  ");
        context.create().resource(accordion.getPath() + "/items/international",
                "itemTitle", "Infants on international flights",
                "itemText", "<p>Infants must have their own ticket.</p>");
        context.currentResource(accordion);

        Accordion model = context.request().adaptTo(Accordion.class);
        assertNotNull(model);
        assertEquals("Booking for infants (under 2 years)", model.getTitle());
        assertEquals("Learn how to book babies on a flight.", model.getIntroduction());
        assertEquals(AccordionImpl.RESOURCE_TYPE, model.getExportedType());
        assertFalse(model.isEmpty());
        assertNotNull(model.getId());
        assertFalse(model.getId().isEmpty());
        assertEquals(2, model.getItems().size());

        AccordionItem first = model.getItems().get(0);
        assertEquals("domestic", first.getId());
        assertEquals("Infants on domestic flights", first.getTitle());
        assertEquals(RICH_TEXT, first.getText());
        assertEquals(2, first.getLinks().size());

        AccordionItem.Link contentPath = first.getLinks().get(0);
        assertEquals("More about bassinets", contentPath.getText());
        assertEquals("/content/airline/bassinet", contentPath.getUrl());
        assertNull(contentPath.getTarget());
        assertNull(contentPath.getRel());

        AccordionItem.Link external = first.getLinks().get(1);
        assertEquals("Contact us to make a booking", external.getText());
        assertEquals("https://www.example.com/book", external.getUrl());
        assertEquals("_blank", external.getTarget());
        assertEquals("noopener noreferrer", external.getRel());

        AccordionItem second = model.getItems().get(1);
        assertEquals("international", second.getId());
        assertEquals("Infants on international flights", second.getTitle());
        assertTrue(second.getLinks().isEmpty());

        ComponentExporter exporter = context.request().adaptTo(ComponentExporter.class);
        assertTrue(exporter instanceof AccordionImpl);
        assertEquals(AccordionImpl.RESOURCE_TYPE, exporter.getExportedType());

        JsonNode exported = mapper.readTree(mapper.writeValueAsString(model));
        assertEquals("Booking for infants (under 2 years)", exported.get("title").asText());
        assertEquals("Learn how to book babies on a flight.", exported.get("introduction").asText());
        assertEquals("domestic", exported.get("items").get(0).get("id").asText());
        assertEquals("Infants on domestic flights", exported.get("items").get(0).get("title").asText());
        assertEquals(RICH_TEXT, exported.get("items").get(0).get("text").asText());
        assertEquals("/content/airline/bassinet", exported.get("items").get(0).get("links").get(0).get("url").asText());
        assertFalse(exported.get("items").get(0).get("links").get(0).has("target"));
        assertFalse(exported.get("items").get(0).get("links").get(0).has("rel"));
        assertEquals("https://www.example.com/book", exported.get("items").get(0).get("links").get(1).get("url").asText());
        assertEquals("_blank", exported.get("items").get(0).get("links").get(1).get("target").asText());
        assertEquals("noopener noreferrer", exported.get("items").get(0).get("links").get(1).get("rel").asText());
        assertEquals("international", exported.get("items").get(1).get("id").asText());
        assertFalse(exported.get("empty").asBoolean());
        assertEquals(model.getId(), exported.get("id").asText());
        assertEquals(AccordionImpl.RESOURCE_TYPE, exported.get(":type").asText());
        assertFalse(exported.toString().contains("core/wcm/components/accordion"), exported.toString());
    }

    @Test
    void emptyWhenNothingIsAuthored() {
        Resource blank = context.create().resource(PAGE + "/jcr:content/blank",
                "sling:resourceType", AccordionImpl.RESOURCE_TYPE);
        context.currentResource(blank);
        Accordion empty = context.request().adaptTo(Accordion.class);
        assertNotNull(empty);
        assertTrue(empty.isEmpty());
        assertEquals("", empty.getTitle());
        assertEquals("", empty.getIntroduction());
        assertTrue(empty.getItems().isEmpty());

        Resource whitespace = context.create().resource(PAGE + "/jcr:content/whitespace",
                "sling:resourceType", AccordionImpl.RESOURCE_TYPE,
                "title", "   ",
                "introduction", "  ");
        context.create().resource(whitespace.getPath() + "/items/item0",
                "itemTitle", "  ",
                "itemText", "   ");
        context.create().resource(whitespace.getPath() + "/items/item0/links/item0",
                "linkText", " ",
                "linkURL", "  ");
        context.currentResource(whitespace);
        Accordion blankFields = context.request().adaptTo(Accordion.class);
        assertNotNull(blankFields);
        assertTrue(blankFields.isEmpty());
        assertTrue(blankFields.getItems().isEmpty());

        Resource titleOnly = context.create().resource(PAGE + "/jcr:content/title-only",
                "sling:resourceType", AccordionImpl.RESOURCE_TYPE,
                "title", "Booking");
        context.currentResource(titleOnly);
        assertFalse(context.request().adaptTo(Accordion.class).isEmpty());

        Resource introductionOnly = context.create().resource(PAGE + "/jcr:content/intro-only",
                "sling:resourceType", AccordionImpl.RESOURCE_TYPE,
                "introduction", "A short intro.");
        context.currentResource(introductionOnly);
        assertFalse(context.request().adaptTo(Accordion.class).isEmpty());
    }

    @Test
    void singleLinkIsExported() throws Exception {
        Resource accordion = context.create().resource(PAGE + "/jcr:content/single",
                "sling:resourceType", AccordionImpl.RESOURCE_TYPE,
                "title", "Booking");
        Resource item = context.create().resource(accordion.getPath() + "/items/item0",
                "itemTitle", "Bassinet");
        context.create().resource(item.getPath() + "/links/item0",
                "linkText", "More about bassinets",
                "linkURL", "/content/airline/bassinet");
        context.currentResource(accordion);

        Accordion model = context.request().adaptTo(Accordion.class);
        assertNotNull(model);
        assertEquals(1, model.getItems().size());
        assertEquals("item0", model.getItems().get(0).getId());
        assertEquals(1, model.getItems().get(0).getLinks().size());
        assertEquals("More about bassinets", model.getItems().get(0).getLinks().get(0).getText());
        assertEquals("/content/airline/bassinet", model.getItems().get(0).getLinks().get(0).getUrl());
        assertNull(model.getItems().get(0).getLinks().get(0).getTarget());

        JsonNode exported = mapper.readTree(mapper.writeValueAsString(model));
        assertEquals(1, exported.get("items").get(0).get("links").size());
        assertEquals("/content/airline/bassinet", exported.get("items").get(0).get("links").get(0).get("url").asText());
        assertFalse(exported.get("items").get(0).get("links").get(0).has("target"));
    }

    @Test
    void absoluteHttpLinksAreDistinctFromContentPathsAndAreNotCapped() throws Exception {
        Resource accordion = context.create().resource(PAGE + "/jcr:content/many-links",
                "sling:resourceType", AccordionImpl.RESOURCE_TYPE,
                "title", "Booking");
        Resource item = context.create().resource(accordion.getPath() + "/items/item0",
                "itemTitle", "Bassinets");
        context.create().resource(item.getPath() + "/links/item0",
                "linkText", "Secure site",
                "linkURL", "https://www.example.com/bassinet");
        context.create().resource(item.getPath() + "/links/item1",
                "linkText", "Plain http",
                "linkURL", "http://example.com/help");
        context.create().resource(item.getPath() + "/links/item2",
                "linkText", "Same site",
                "linkURL", "/content/airline/help");
        context.currentResource(accordion);

        Accordion model = context.request().adaptTo(Accordion.class);
        assertNotNull(model);
        assertEquals(3, model.getItems().get(0).getLinks().size());

        AccordionItem.Link https = model.getItems().get(0).getLinks().get(0);
        assertEquals("_blank", https.getTarget());
        assertEquals("noopener noreferrer", https.getRel());

        AccordionItem.Link http = model.getItems().get(0).getLinks().get(1);
        assertEquals("http://example.com/help", http.getUrl());
        assertEquals("_blank", http.getTarget());
        assertEquals("noopener noreferrer", http.getRel());

        AccordionItem.Link contentPath = model.getItems().get(0).getLinks().get(2);
        assertEquals("/content/airline/help", contentPath.getUrl());
        assertNull(contentPath.getTarget());
        assertNull(contentPath.getRel());

        JsonNode links = mapper.readTree(mapper.writeValueAsString(model)).get("items").get(0).get("links");
        assertEquals(3, links.size());
        assertEquals("_blank", links.get(0).get("target").asText());
        assertEquals("noopener noreferrer", links.get(0).get("rel").asText());
        assertEquals("_blank", links.get(1).get("target").asText());
        assertFalse(links.get(2).has("target"), links.toString());
        assertFalse(links.get(2).has("rel"), links.toString());
    }
}
