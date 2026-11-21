/*~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 ~ Copyright 2020 Adobe Systems Incorporated
 ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");
 ~ you may not use this file except in compliance with the License.
 ~ You may obtain a copy of the License at
 ~
 ~     http://www.apache.org/licenses/LICENSE-2.0
 ~
 ~ Unless required by applicable law or agreed to in writing, software
 ~ distributed under the License is distributed on an "AS IS" BASIS,
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 ~ See the License for the specific language governing permissions and
 ~ limitations under the License.
 ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~*/

function trimmedString(value) {
  return typeof value === 'string' ? value.trim() : '';
}

/**
 * Ensures window.adobeDataLayer is an array and appends one plain object.
 * Callers must not pass enquiry or other personal data.
 */
export function pushAdobeDataLayer(payload) {
  if (typeof window === 'undefined' || payload == null) {
    return;
  }
  const layer = (window.adobeDataLayer = window.adobeDataLayer || []);
  layer.push(payload);
}

/**
 * Page-load event. Title and id are copied only when the page model provides them.
 */
export function pushPageShow(model) {
  const source = model || {};
  const title = trimmedString(source.title);
  const id = trimmedString(source.id);
  const payload = {
    event: 'cmp:show',
    eventInfo: {
      path: 'page',
      type: 'page'
    }
  };
  const page = {};
  if (title) {
    page.title = title;
  }
  if (id) {
    page.id = id;
  }
  if (title || id) {
    payload.page = page;
  }
  pushAdobeDataLayer(payload);
}

/**
 * CTA click. Only the label, href, and optional component id are copied.
 * Enquiry and other personal fields are ignored.
 */
export function pushLinkClick(link) {
  const source = link || {};
  const payloadLink = {
    title: trimmedString(source.title),
    url: trimmedString(source.url)
  };
  const componentId = trimmedString(source.componentId);
  if (componentId) {
    payloadLink.componentId = componentId;
  }
  pushAdobeDataLayer({
    event: 'cmp:linkClick',
    link: payloadLink
  });
}
