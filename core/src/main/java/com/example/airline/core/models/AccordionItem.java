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

/**
 * One authored accordion row. {@code id} is the child node name under {@code items}.
 */
public interface AccordionItem {

    String getId();

    String getTitle();

    /**
     * Rich text HTML authored in {@code ./itemText}.
     */
    String getText();

    List<Link> getLinks();

    /**
     * A call to action under an item. Authors can add any number of these.
     * Absolute http(s) URLs also expose {@code target} and {@code rel}.
     */
    interface Link {

        String getText();

        String getUrl();

        /**
         * @return {@code _blank} for an absolute http(s) URL, otherwise null
         */
        String getTarget();

        /**
         * @return {@code noopener noreferrer} for an absolute http(s) URL, otherwise null
         */
        String getRel();
    }
}
