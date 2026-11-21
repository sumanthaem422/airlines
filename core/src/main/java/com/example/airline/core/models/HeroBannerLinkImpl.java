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
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

@Model(
        adaptables = Resource.class,
        adapters = HeroBanner.JumpLink.class,
        defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class HeroBannerLinkImpl implements HeroBanner.JumpLink {

    @ValueMapValue
    private String text;

    @ValueMapValue
    private String anchor;

    @Override
    public String getText() {
        return text == null ? "" : text;
    }

    @Override
    public String getAnchor() {
        return normalizeAnchor(anchor);
    }

    @Override
    public String getHref() {
        return getAnchor();
    }

    /**
     * Authors may type {@code fares} or {@code #fares}. The exported href is always {@code #id}.
     */
    static String normalizeAnchor(String raw) {
        if (raw == null) {
            return "";
        }
        String value = raw.trim();
        while (value.startsWith("#")) {
            value = value.substring(1).trim();
        }
        if (value.isEmpty()) {
            return "";
        }
        return "#" + value;
    }
}
