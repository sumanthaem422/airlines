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

import { MapTo } from '@adobe/aem-react-editable-components';
import React from 'react';
import { pushAdobeDataLayer, pushLinkClick } from '../../utils/adobeDataLayer';

require('./Accordion.css');

function trimmed(value) {
    return typeof value === 'string' ? value.trim() : '';
}

function isAbsoluteHttpUrl(url) {
    try {
        const parsed = new URL(trimmed(url));
        const protocol = parsed.protocol;
        return (protocol === 'http:' || protocol === 'https:') && parsed.host.length > 0;
    } catch (error) {
        return false;
    }
}

function normalizeLink(link) {
    if (!link || typeof link !== 'object') {
        return null;
    }
    const text = trimmed(link.text);
    const url = trimmed(link.url);
    if (!text && !url) {
        return null;
    }
    return {
        text: text,
        url: url
    };
}

function itemTitle(item) {
    return trimmed(item.question) || trimmed(item.title);
}

function itemBody(item) {
    const answer = typeof item.answer === 'string' ? item.answer.trim() : '';
    if (answer) {
        return answer;
    }
    return typeof item.text === 'string' ? item.text.trim() : '';
}

function normalizeItem(item, index) {
    if (!item || typeof item !== 'object') {
        return null;
    }
    const title = itemTitle(item);
    const text = itemBody(item);
    const links = [];
    const sourceLinks = Array.isArray(item.links) ? item.links : [];
    for (let linkIndex = 0; linkIndex < sourceLinks.length; linkIndex += 1) {
        const link = normalizeLink(sourceLinks[linkIndex]);
        if (link) {
            links.push(link);
        }
    }
    if (!title && !text && links.length === 0) {
        return null;
    }
    return {
        id: trimmed(item.id) || ('item-' + index),
        title: title,
        text: text,
        links: links
    };
}

function normalizeItems(value) {
    if (!Array.isArray(value)) {
        return [];
    }
    const items = [];
    for (let index = 0; index < value.length; index += 1) {
        const item = normalizeItem(value[index], index);
        if (item) {
            items.push(item);
        }
    }
    return items;
}

function sectionTitle(props) {
    if (!props) {
        return '';
    }
    return trimmed(props.sectionHeading) || trimmed(props.title);
}

function clampHeadingRank(rank) {
    if (rank < 2) {
        return 2;
    }
    if (rank > 6) {
        return 6;
    }
    return rank;
}

/**
 * Section heading is h2–h6. Panel headings sit one level lower and never use h1.
 * h6 stays h6 because there is no h7.
 */
function resolveHeadingRanks(headingLevel) {
    const text = typeof headingLevel === 'string' ? headingLevel.trim().toLowerCase() : '';
    const match = text.match(/^h(\d+)$/);
    const section = match ? clampHeadingRank(parseInt(match[1], 10)) : 2;
    const panel = section + 1 > 6 ? 6 : section + 1;
    return {
        section: section,
        panel: panel
    };
}

export function isAccordionEmpty(props) {
    if (!props || props.empty === true || props.isEmpty === true) {
        return true;
    }
    return sectionTitle(props).length === 0 && normalizeItems(props.items).length === 0;
}

export const AccordionEditConfig = {
    emptyLabel: 'Accordion',

    isEmpty: function(props) {
        return isAccordionEmpty(props);
    }
};

function controlId(componentId, itemId, index, suffix) {
    const prefix = String(componentId || 'accordion').replace(/[^A-Za-z0-9_-]/g, '') || 'accordion';
    const item = String(itemId || index).replace(/[^A-Za-z0-9_-]/g, '') || String(index);
    return prefix + '-' + item + '-' + index + '-' + suffix;
}

function pushToggle(item, expanded) {
    pushAdobeDataLayer({
        event: 'cmp:accordionToggle',
        accordion: {
            id: item.id,
            title: item.title,
            expanded: expanded
        }
    });
}

function headerRefFor(headerRefs, itemId) {
    if (!headerRefs.current[itemId]) {
        headerRefs.current[itemId] = React.createRef();
    }
    return headerRefs.current[itemId];
}

function focusHeaderAt(headerRefs, items, index) {
    if (!items.length) {
        return;
    }
    const count = items.length;
    const nextIndex = ((index % count) + count) % count;
    const item = items[nextIndex];
    const ref = item && headerRefs.current[item.id];
    if (ref && ref.current) {
        ref.current.focus();
    }
}

function onHeaderKeyDown(event, index, headerRefs, items) {
    const key = event.key;
    let targetIndex = null;
    if (key === 'ArrowDown') {
        targetIndex = index + 1;
    } else if (key === 'ArrowUp') {
        targetIndex = index - 1;
    } else if (key === 'Home') {
        targetIndex = 0;
    } else if (key === 'End') {
        targetIndex = items.length - 1;
    } else {
        return;
    }
    event.preventDefault();
    focusHeaderAt(headerRefs, items, targetIndex);
}

function Accordion(props) {
    const safeProps = props || {};
    const items = normalizeItems(safeProps.items);
    const title = sectionTitle(safeProps);
    const introduction = trimmed(safeProps.introduction);
    const componentId = trimmed(safeProps.id);
    const headingRanks = resolveHeadingRanks(safeProps.headingLevel);
    const SectionHeading = 'h' + headingRanks.section;
    const PanelHeading = 'h' + headingRanks.panel;
    const [expandedIds, setExpandedIds] = React.useState(function() {
        return new Set();
    });
    const headerRefs = React.useRef({});
    const empty = isAccordionEmpty(props);

    if (empty) {
        if (safeProps.isInEditor) {
            return (
                <div
                    className="cq-placeholder cmp-accordion__placeholder"
                    role="status"
                    aria-live="polite"
                >
                    Accordion
                </div>
            );
        }
        return null;
    }

    const allOpen = items.length > 0 && items.every(function(item) {
        return expandedIds.has(item.id);
    });

    function toggleItem(item) {
        const willExpand = !expandedIds.has(item.id);
        const next = new Set(expandedIds);
        if (willExpand) {
            next.add(item.id);
        } else {
            next.delete(item.id);
        }
        setExpandedIds(next);
        pushToggle(item, willExpand);
    }

    function setEveryItem(open) {
        const next = new Set();
        for (let index = 0; index < items.length; index += 1) {
            const item = items[index];
            const currentlyOpen = expandedIds.has(item.id);
            if (open) {
                next.add(item.id);
            }
            if (currentlyOpen !== open) {
                pushToggle(item, open);
            }
        }
        setExpandedIds(open ? next : new Set());
    }

    function onToggleAll() {
        setEveryItem(!allOpen);
    }

    return (
        <section id={componentId || undefined} className="cmp-accordion">
            <div className="cmp-accordion__inner">
            <div className="cmp-accordion__layout">
                {(title || introduction) ? (
                    <div className="cmp-accordion__summary">
                        {title ? <SectionHeading className="cmp-accordion__title">{title}</SectionHeading> : null}
                        {introduction ? (
                            <p className="cmp-accordion__introduction">{introduction}</p>
                        ) : null}
                    </div>
                ) : null}
                {items.length > 0 ? (
                    <div className="cmp-accordion__panels">
                        <div className="cmp-accordion__toolbar">
                            <button
                                type="button"
                                className="cmp-accordion__expand"
                                onClick={onToggleAll}
                            >
                                {allOpen ? 'Collapse all items' : 'Expand all items'}
                            </button>
                        </div>
                        <div className="cmp-accordion__items">
                            {items.map(function(item, index) {
                                const open = expandedIds.has(item.id);
                                const buttonId = controlId(componentId, item.id, index, 'button');
                                const panelId = controlId(componentId, item.id, index, 'panel');
                                return (
                                    <div
                                        key={item.id + '-' + index}
                                        className={'cmp-accordion__item' + (open ? ' cmp-accordion__item--open' : '')}
                                    >
                                        <PanelHeading className="cmp-accordion__heading">
                                            <button
                                                type="button"
                                                className="cmp-accordion__trigger"
                                                id={buttonId}
                                                ref={headerRefFor(headerRefs, item.id)}
                                                aria-expanded={open ? 'true' : 'false'}
                                                aria-controls={panelId}
                                                onClick={function() {
                                                    toggleItem(item);
                                                }}
                                                onKeyDown={function(event) {
                                                    onHeaderKeyDown(event, index, headerRefs, items);
                                                }}
                                            >
                                                <span className="cmp-accordion__item-title">{item.title}</span>
                                                <span
                                                    className={'cmp-accordion__chevron' + (open ? ' cmp-accordion__chevron--open' : '')}
                                                    aria-hidden="true"
                                                />
                                            </button>
                                        </PanelHeading>
                                        <div
                                            className="cmp-accordion__panel"
                                            id={panelId}
                                            role="region"
                                            aria-labelledby={buttonId}
                                            hidden={open ? undefined : true}
                                        >
                                            {item.text ? (
                                                <div
                                                    className="cmp-accordion__text"
                                                    dangerouslySetInnerHTML={{ __html: item.text }}
                                                />
                                            ) : null}
                                            {item.links.length > 0 ? (
                                                <div className="cmp-accordion__links">
                                                    {item.links.map(function(link, linkIndex) {
                                                        if (!link.url) {
                                                            return null;
                                                        }
                                                        const external = isAbsoluteHttpUrl(link.url);
                                                        const label = link.text || link.url;
                                                        return (
                                                            <a
                                                                key={link.url + '-' + linkIndex}
                                                                className="cmp-accordion__link"
                                                                href={link.url}
                                                                target={external ? '_blank' : undefined}
                                                                rel={external ? 'noopener noreferrer' : undefined}
                                                                onClick={function() {
                                                                    pushLinkClick({
                                                                        title: label,
                                                                        url: link.url,
                                                                        componentId: componentId
                                                                    });
                                                                }}
                                                            >
                                                                <span className="cmp-accordion__link-text">{label}</span>
                                                                <span className="cmp-accordion__arrow" aria-hidden="true">→</span>
                                                            </a>
                                                        );
                                                    })}
                                                </div>
                                            ) : null}
                                        </div>
                                    </div>
                                );
                            })}
                        </div>
                    </div>
                ) : null}
            </div>
            </div>
        </section>
    );
}

MapTo('airline/components/accordion')(Accordion, AccordionEditConfig);

export default Accordion;
