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
import { pushAdobeDataLayer } from '../../utils/adobeDataLayer';

require('./HeroBanner.css');

export function isHeroBannerEmpty(props) {
    if (!props || props.empty === true) {
        return true;
    }
    const title = props.title ? String(props.title).trim() : '';
    const imageSource = props.src || props.fileReference || '';
    return title.length === 0 && String(imageSource).trim().length === 0;
}

export const HeroBannerEditConfig = {
    emptyLabel: 'Configure Hero Banner',

    isEmpty: function(props) {
        return isHeroBannerEmpty(props);
    }
};

function linkHref(link) {
    if (!link) {
        return '';
    }
    return link.href || link.anchor || '';
}

function anchorIdFromHref(href) {
    return String(href || '').trim().replace(/^#+/, '');
}

function pushJumpLinkClick(link) {
    const href = linkHref(link);
    pushAdobeDataLayer({
        event: 'cmp:jumpLinkClick',
        link: {
            label: link && link.text != null ? String(link.text) : '',
            anchorId: anchorIdFromHref(href)
        }
    });
}

function modelDataLayer(value) {
    return typeof value === 'string' ? value : '';
}

function HeroBanner(props) {
    const safeProps = props || {};
    const dataLayer = modelDataLayer(safeProps.dataLayer);
    const empty = isHeroBannerEmpty(safeProps);

    React.useEffect(function() {
        if (empty || !dataLayer) {
            return undefined;
        }
        pushAdobeDataLayer(JSON.parse(dataLayer));
        return undefined;
    }, [dataLayer, empty]);

    if (empty) {
        if (safeProps.isInEditor) {
            return (
                <div className="cq-placeholder cmp-herobanner__placeholder">
                    Configure Hero Banner
                </div>
            );
        }
        return null;
    }

    const title = safeProps.title ? String(safeProps.title).trim() : '';
    const introduction = safeProps.introduction ? String(safeProps.introduction).trim() : '';
    const imageSrc = String(safeProps.src || safeProps.fileReference || '').trim();
    const alt = typeof safeProps.alt === 'string' ? safeProps.alt : '';
    const showImagePlaceholder = !imageSrc && !!safeProps.isInEditor;
    const links = Array.isArray(safeProps.links) ? safeProps.links : [];
    const visibleLinks = links.filter(function(link) {
        return link && (link.text || linkHref(link));
    });

    const titleBlock = title ? (
        <div className="cmp-herobanner__inner">
            <h1 className="cmp-herobanner__title">{title}</h1>
        </div>
    ) : null;

    let mediaBlock = null;
    if (imageSrc) {
        mediaBlock = (
            <figure className="cmp-herobanner__media">
                <img
                    className="cmp-herobanner__image"
                    src={imageSrc}
                    alt={alt}
                    fetchpriority="high"
                    loading="eager"
                />
            </figure>
        );
    } else if (showImagePlaceholder) {
        mediaBlock = (
            <div className="cmp-herobanner__image-placeholder">Configure hero image</div>
        );
    }

    const copyBlock = (introduction || visibleLinks.length > 0) ? (
        <div className="cmp-herobanner__inner">
            {introduction ? <p className="cmp-herobanner__intro">{introduction}</p> : null}
            {visibleLinks.length > 0 ? (
                <nav className="cmp-herobanner__on-this-page" aria-label="On this page">
                    <p className="cmp-herobanner__on-this-page-label">On this page</p>
                    <ul className="cmp-herobanner__links">
                        {visibleLinks.map(function(link, index) {
                            const href = linkHref(link);
                            return (
                                <li key={(href || link.text || 'link') + '-' + index}>
                                    <a href={href || '#'} onClick={function() {
                                        pushJumpLinkClick(link);
                                    }}>{link.text}</a>
                                </li>
                            );
                        })}
                    </ul>
                </nav>
            ) : null}
        </div>
    ) : null;

    return (
        <section
            id={safeProps.id || undefined}
            className="cmp-herobanner"
            data-cmp-data-layer={dataLayer || undefined}
        >
            {titleBlock}
            {mediaBlock}
            {copyBlock}
        </section>
    );
}

MapTo('airline/components/herobanner')(HeroBanner, HeroBannerEditConfig);

export default HeroBanner;
