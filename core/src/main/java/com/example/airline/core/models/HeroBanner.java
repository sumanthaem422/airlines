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

import java.util.List;

import com.adobe.cq.wcm.core.components.models.Component;
import com.adobe.cq.wcm.core.components.models.datalayer.ComponentData;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * JSON contract for the hero banner. Alt text, anchor normalisation, and the
 * Adobe Client Data Layer payload are resolved here.
 */
public interface HeroBanner extends Component {

    @Override
    String getId();

    /**
     * @return the component data layer object, or null when the data layer is disabled
     */
    @Override
    @JsonIgnore
    ComponentData getData();

    /**
     * Precomputed Adobe Client Data Layer JSON for {@code data-cmp-data-layer}.
     * Same bytes as {@link ComponentData#getJson()} when the layer is enabled.
     *
     * @return that JSON string, or null when the data layer is disabled
     */
    @JsonProperty("dataLayer")
    String getDataLayer();

    String getTitle();

    String getIntroduction();

    String getFileReference();

    /**
     * @return DAM path used as the image {@code src}
     */
    String getSrc();

    /**
     * @return resolved alt text, never null. Empty when the image is decorative.
     *         Otherwise the authored alt, then the DAM asset {@code dc:title},
     *         then the component title, using the first value that is non-blank.
     */
    String getAlt();

    boolean isDecorative();

    /**
     * @return true when the title and the image file reference are both blank
     */
    boolean isEmpty();

    List<JumpLink> getLinks();

    /**
     * One "On this page" jump link. {@code anchor} and {@code href} are the same normalised value.
     */
    interface JumpLink {

        String getText();

        /**
         * @return {@code #id}, or an empty string when the author left the anchor blank
         */
        String getAnchor();

        /**
         * @return the same value as {@link #getAnchor()}
         */
        String getHref();
    }
}
