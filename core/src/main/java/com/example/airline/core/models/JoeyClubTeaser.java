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

import com.adobe.cq.wcm.core.components.models.Teaser;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * JSON contract for the Joey Club teaser. Heading level and image alt text
 * are resolved here so the React view only renders them.
 */
public interface JoeyClubTeaser extends Teaser {

    /**
     * @return the DAM path authored in {@code ./fileReference}
     */
    String getFileReference();

    /**
     * @return the same DAM path as {@link #getFileReference()}, used as the image {@code src}
     */
    String getSrc();

    /**
     * Alt text from the DAM asset {@code dc:description} when an image is present.
     * Empty when the asset or description is missing.
     *
     * @return alt text, never null
     */
    @JsonProperty("alt")
    String getAltText();

    /**
     * @return the CTA label authored in {@code ./linkText}
     */
    String getLinkText();

    /**
     * The call to action always opens in the same tab, so this is always empty.
     *
     * @return empty string
     */
    String getRel();

    /**
     * Heading tag from the template policy ({@code headingElement}, then {@code titleType}).
     * Only {@code h2}, {@code h3}, or {@code h4}.
     *
     * @return the heading element, defaulting to {@code h2}
     */
    @JsonProperty("headingLevel")
    default String getHeadingLevel() {
        return "h2";
    }

    /**
     * @return true when title, description, and image are all unauthored
     */
    @JsonProperty("empty")
    boolean isEmpty();
}
