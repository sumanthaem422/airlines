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

require('./Footer.css');

const HOME = '/content/airline/us/en/home.html';

function LogoMark() {
    return (
        <svg
            className="airline-footer__mark"
            width="28"
            height="28"
            viewBox="0 0 32 32"
            aria-hidden="true"
            focusable="false"
        >
            <rect width="32" height="32" rx="8" fill="#aae0fa" />
            <path
                fill="#102033"
                fillRule="evenodd"
                d="M16 6.5 8.2 26h3.4l1.45-3.7h5.9l1.45 3.7h3.4L16 6.5zm0 6.4 1.85 4.7h-3.7L16 12.9z"
            />
            <rect x="13.1" y="17.15" width="5.8" height="2" rx="0.4" fill="#ba1200" />
        </svg>
    );
}
const FALLBACK_COPYRIGHT = '© 2026 Airline Travel Info. All rights reserved.';
const FALLBACK_LINKS = [
    { title: 'Flying with infants', url: HOME },
    { title: 'Travel requirements', url: HOME },
    { title: 'Contact us', url: HOME }
];

const FooterEditConfig = {
    emptyLabel: 'Site Footer',
    isEmpty: () => false
};

function trimmed(value) {
    return typeof value === 'string' ? value.trim() : '';
}

function navigationItems(items) {
    if (!Array.isArray(items) || items.length === 0) {
        return FALLBACK_LINKS;
    }
    return items.filter(function(item) {
        return item && trimmed(item.title).length > 0;
    });
}

function copyrightLine(value) {
    const text = trimmed(value);
    return text || FALLBACK_COPYRIGHT;
}

function Footer(props) {
    const safeProps = props || {};
    const items = navigationItems(safeProps.items);
    const copyrightText = copyrightLine(safeProps.copyrightText);

    return (
        <footer role="contentinfo" className="airline-footer">
            <div className="airline-footer__columns">
                <div className="airline-footer__brand">
                    <div className="airline-footer__heading">
                        {LogoMark()}
                        <div className="airline-footer__title">
                            <p className="airline-footer__name">Airline Travel Info</p>
                            <span className="airline-footer__rule" aria-hidden="true" />
                        </div>
                    </div>
                    <p className="airline-footer__tagline">Helping families travel smarter.</p>
                </div>
                <nav className="airline-footer__nav" aria-label="Footer">
                    <ul className="airline-footer__list">
                        {items.map(function(item, index) {
                            const title = trimmed(item.title);
                            const url = item.url || '';
                            return (
                                <li className="airline-footer__item" key={(url || title) + '-' + index}>
                                    <a className="airline-footer__link" href={item.url}>{title}</a>
                                </li>
                            );
                        })}
                    </ul>
                </nav>
                <p className="airline-footer__copyright">{copyrightText}</p>
            </div>
        </footer>
    );
}

MapTo('airline/components/footer')(Footer, FooterEditConfig);

export default Footer;
