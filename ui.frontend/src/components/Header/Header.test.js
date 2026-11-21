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

import React from 'react';
import ReactDOM from 'react-dom';
import { act } from 'react-dom/test-utils';
import Header from './Header';

describe('Header ->', () => {
    let rootNode;

    beforeEach(() => {
        rootNode = document.createElement('div');
        document.body.appendChild(rootNode);
    });

    afterEach(() => {
        ReactDOM.unmountComponentAtNode(rootNode);
        document.body.removeChild(rootNode);
    });

    function click(element) {
        act(() => {
            element.dispatchEvent(new MouseEvent('click', { bubbles: true, cancelable: true }));
        });
    }

    it('renders the logo, skip link, and one navigation item', () => {
        ReactDOM.render(
            <Header
                items={[
                    { title: 'Flying with infants', url: '/content/airline/us/en/flying.html' },
                    { url: '/content/airline/us/en/untitled.html' }
                ]}
            />,
            rootNode
        );

        const header = rootNode.querySelector('header.airline-header');
        expect(header.getAttribute('role')).toBe('banner');

        const logo = rootNode.querySelector('.airline-header__logo');
        expect(logo.getAttribute('href')).toBe('/content/airline/us/en/home.html');
        expect(logo.textContent).toBe('Airline Travel Info');

        const skip = rootNode.querySelector('.airline-header__skip-link');
        expect(skip.getAttribute('href')).toBe('#main-content');
        expect(skip.textContent).toBe('Skip to main content');
        expect(header.firstElementChild).toBe(skip);

        const nav = rootNode.querySelector('nav');
        expect(nav.getAttribute('role')).toBe('navigation');
        expect(nav.getAttribute('aria-label')).toBe('Main navigation');

        const links = nav.querySelectorAll('a');
        expect(links.length).toBe(1);
        expect(links[0].getAttribute('href')).toBe('/content/airline/us/en/flying.html');
        expect(links[0].textContent).toBe('Flying with infants');
    });

    it('toggles the menu button label and expanded state', () => {
        ReactDOM.render(
            <Header items={[{ title: 'Home', url: '/content/airline/us/en/home.html' }]} />,
            rootNode
        );

        const button = rootNode.querySelector('.airline-header__menu-button');
        const list = rootNode.querySelector('.airline-header__list');
        expect(button.textContent).toBe('☰');
        expect(button.getAttribute('aria-expanded')).toBe('false');
        expect(button.getAttribute('aria-label')).toBe('Open menu');
        expect(button.getAttribute('aria-controls')).toBe(list.id);
        expect(list.className).toBe('airline-header__list');

        click(button);

        expect(button.getAttribute('aria-expanded')).toBe('true');
        expect(button.getAttribute('aria-label')).toBe('Close menu');
        expect(list.classList.contains('airline-header__list--open')).toBe(true);

        click(button);

        expect(button.getAttribute('aria-expanded')).toBe('false');
        expect(button.getAttribute('aria-label')).toBe('Open menu');
        expect(list.classList.contains('airline-header__list--open')).toBe(false);
    });
});
