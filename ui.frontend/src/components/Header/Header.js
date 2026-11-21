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

require('./Header.css');

const LOGO_HREF = '/content/airline/us/en/home.html';
const MENU_ID = 'airline-header-menu';

function LogoMark() {
    return (
        <svg
            className="airline-header__mark"
            width="36"
            height="36"
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

const HeaderEditConfig = {
    emptyLabel: 'Site Header',
    isEmpty: () => false
};

function trimmed(value) {
    return typeof value === 'string' ? value.trim() : '';
}

function navigationItems(items) {
    if (!Array.isArray(items)) {
        return [];
    }
    return items.filter(function(item) {
        return item && trimmed(item.title).length > 0;
    });
}

function Header(props) {
    const safeProps = props || {};
    const [menuOpen, setMenuOpen] = React.useState(false);
    const items = navigationItems(safeProps.items);

    function toggleMenu() {
        setMenuOpen(function(open) {
            return !open;
        });
    }

    const listClassName = menuOpen
        ? 'airline-header__list airline-header__list--open'
        : 'airline-header__list';

    return (
        <header role="banner" className="airline-header">
            <a href="#main-content" className="airline-header__skip-link">Skip to main content</a>
            <div className="airline-header__bar">
                <a className="airline-header__logo" href={LOGO_HREF}>{LogoMark()}<span className="airline-header__word"><span className="airline-header__wordmark">Airline Travel Info</span><span className="airline-header__rule" aria-hidden="true" /></span></a>
                <button
                    type="button"
                    className="airline-header__menu-button"
                    aria-expanded={menuOpen}
                    aria-controls={MENU_ID}
                    aria-label={menuOpen ? 'Close menu' : 'Open menu'}
                    onClick={toggleMenu}
                >
                    ☰
                </button>
                <nav className="airline-header__nav" role="navigation" aria-label="Main navigation">
                    <ul id={MENU_ID} className={listClassName}>
                        {items.map(function(item, index) {
                            const title = trimmed(item.title);
                            const url = item.url || '';
                            return (
                                <li className="airline-header__item" key={(url || title) + '-' + index}>
                                    <a className="airline-header__link" href={item.url}>{title}</a>
                                </li>
                            );
                        })}
                    </ul>
                </nav>
            </div>
        </header>
    );
}

MapTo('airline/components/header')(Header, HeaderEditConfig);

export default Header;
