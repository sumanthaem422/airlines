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
import { pushLinkClick } from '../../utils/adobeDataLayer';
import extractModelId from '../../utils/extract-model-id';

require('./Teaser.css');

function trimmed(value) {
    return value ? String(value).trim() : '';
}

function firstNonEmpty() {
    for (let index = 0; index < arguments.length; index += 1) {
        const value = trimmed(arguments[index]);
        if (value) {
            return value;
        }
    }
    return '';
}

function objectField(value, name) {
    if (!value || typeof value !== 'object') {
        return '';
    }
    return value[name];
}

function resolveImageSrc(props) {
    const items = props[':items'];
    const nestedImage = items && typeof items === 'object' ? items.image : undefined;
    return firstNonEmpty(
        objectField(props.image, 'src'),
        objectField(nestedImage, 'src'),
        props.imageSrc,
        props.fileReference,
        props.src
    );
}

function altFrom(image) {
    if (!image || typeof image !== 'object' || typeof image.alt !== 'string') {
        return undefined;
    }
    return image.alt;
}

function resolveAlt(props) {
    if (typeof props.alt === 'string') {
        return props.alt;
    }
    const imageAlt = altFrom(props.image);
    if (imageAlt !== undefined) {
        return imageAlt;
    }
    const items = props[':items'];
    const nestedImage = items && typeof items === 'object' ? items.image : undefined;
    const nestedAlt = altFrom(nestedImage);
    if (nestedAlt !== undefined) {
        return nestedAlt;
    }
    return '';
}

function firstAction(props) {
    if (!Array.isArray(props.actions) || props.actions.length === 0) {
        return {};
    }
    const action = props.actions[0];
    if (!action || typeof action !== 'object') {
        return {};
    }
    return action;
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

function resolveComponentId(props) {
    return trimmed(props.id) || trimmed(extractModelId(props.cqPath));
}

export function isJoeyClubTeaserEmpty(props) {
    if (!props || props.empty === true) {
        return true;
    }
    return trimmed(props.title).length === 0 && trimmed(props.description).length === 0;
}

export const JoeyClubTeaserEditConfig = {
    emptyLabel: 'Joey Club Teaser',

    isEmpty: function (props) {
        return isJoeyClubTeaserEmpty(props);
    }
};

function headingTag(props) {
    const levels = [props.titleType, props.headingLevel];
    for (let index = 0; index < levels.length; index += 1) {
        const value = typeof levels[index] === 'string' ? levels[index].trim().toLowerCase() : '';
        if (value === 'h2' || value === 'h3' || value === 'h4') {
            return value;
        }
    }
    return 'h2';
}

function Teaser(props) {
    const safeProps = props || {};
    const empty = isJoeyClubTeaserEmpty(safeProps);

    if (empty) {
        if (safeProps.isInEditor) {
            return (
                <div className="cq-placeholder cmp-joeyclubteaser__placeholder">
                    Joey Club Teaser
                </div>
            );
        }
        return null;
    }

    const title = trimmed(safeProps.title);
    const description = trimmed(safeProps.description);
    const imageSrc = resolveImageSrc(safeProps);
    const altText = resolveAlt(safeProps);
    const action = firstAction(safeProps);
    const linkText = firstNonEmpty(action.title, action.text, safeProps.linkText, safeProps.linkLabel);
    const href = firstNonEmpty(action.url, safeProps.linkURL);
    const authoredTarget = firstNonEmpty(action.target, safeProps.linkTarget);
    const openInNewTab = authoredTarget.toLowerCase() === '_blank' || isAbsoluteHttpUrl(href);
    const rel = trimmed(safeProps.rel) || (openInNewTab ? 'noopener noreferrer' : '');
    const HeadingTag = headingTag(safeProps);
    const componentId = resolveComponentId(safeProps);

    function onCtaClick() {
        pushLinkClick({
            title: linkText,
            url: href,
            componentId: componentId
        });
    }

    return (
        <section id={componentId || undefined} className="cmp-joeyclubteaser">
            <div className="cmp-joeyclubteaser__inner">
            <div className="cmp-joeyclubteaser__content">
                {title ? <HeadingTag className="cmp-joeyclubteaser__title">{title}</HeadingTag> : null}
                {description ? (
                    <div
                        className="cmp-joeyclubteaser__description"
                        dangerouslySetInnerHTML={{ __html: description }}
                    />
                ) : null}
                {href ? (
                    <a
                        className="cmp-joeyclubteaser__cta"
                        href={href}
                        target={openInNewTab ? '_blank' : undefined}
                        rel={rel || undefined}
                        onClick={onCtaClick}
                    >
                        {linkText}<span className="cmp-joeyclubteaser__arrow" aria-hidden="true">→</span>
                    </a>
                ) : null}
            </div>
            {imageSrc ? (
                <figure className="cmp-joeyclubteaser__media">
                    <img
                        className="cmp-joeyclubteaser__image"
                        src={imageSrc}
                        alt={altText}
                    />
                </figure>
            ) : null}
            </div>
        </section>
    );
}

MapTo('airline/components/teaser')(Teaser, JoeyClubTeaserEditConfig);

export default Teaser;
