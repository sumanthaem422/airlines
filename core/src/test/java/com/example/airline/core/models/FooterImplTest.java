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
import com.example.airline.core.testcontext.AppAemContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(AemContextExtension.class)
class FooterImplTest {

    private static final String PAGE = "/content/airline/us/en/home";

    private static final String COPYRIGHT = "© 2026 Airline Travel Info. All rights reserved.";

    private final AemContext context = AppAemContext.newAemContext();

    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        context.addModelsForClasses(FooterImpl.class);
        context.create().page(PAGE);
        context.currentPage(PAGE);
    }

    @Test
    void copyrightTextIsExported() throws Exception {
        Footer model = footer("notice", COPYRIGHT);

        assertEquals(COPYRIGHT, model.getCopyrightText());

        JsonNode json = mapper.readTree(mapper.writeValueAsString(model));
        assertEquals(COPYRIGHT, json.get("copyrightText").asText());
        assertEquals(FooterImpl.RESOURCE_TYPE, json.get(":type").asText());
    }

    @Test
    void blankPropertyYieldsEmptyString() {
        assertEquals("", footer("missing", null).getCopyrightText());
        assertEquals("", footer("empty", "").getCopyrightText());
        assertEquals("", footer("whitespace", "   ").getCopyrightText());
    }

    @Test
    void exportedTypeIsAirlineFooter() {
        Footer model = footer("type", COPYRIGHT);
        assertEquals("airline/components/footer", model.getExportedType());

        ComponentExporter exporter = context.request().adaptTo(ComponentExporter.class);
        assertNotNull(exporter);
        assertEquals("airline/components/footer", exporter.getExportedType());
    }

    private Footer footer(String name, String copyright) {
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put("sling:resourceType", FooterImpl.RESOURCE_TYPE);
        if (copyright != null) {
            properties.put("copyrightText", copyright);
        }
        Resource resource = context.create().resource(PAGE + "/jcr:content/" + name, properties);
        context.currentResource(resource);
        Footer model = context.request().adaptTo(Footer.class);
        assertNotNull(model, "Footer should adapt for " + name);
        return model;
    }
}
