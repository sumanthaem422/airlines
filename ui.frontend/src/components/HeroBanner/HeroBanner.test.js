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
import HeroBanner from './HeroBanner';

describe('HeroBanner ->', () => {
    const IMAGE = '/content/dam/airline/hero.jpg';

    let rootNode;

    beforeEach(() => {
        rootNode = document.createElement('div');
        document.body.appendChild(rootNode);
    });

    afterEach(() => {
        ReactDOM.unmountComponentAtNode(rootNode);
        document.body.removeChild(rootNode);
        delete window.adobeDataLayer;
    });

    it('renders the title as the only h1, the intro, an eager image, and jump links', () => {
        ReactDOM.render(
            <HeroBanner
                title="Flying with children"
                introduction="Plan the journey before you fly."
                src={IMAGE}
                alt="Children seated with a parent"
                links={[
                    { text: 'Fares', href: '#fares', anchor: '#fares' },
                    { text: 'Baggage', href: '#baggage', anchor: '#baggage' }
                ]}
            />,
            rootNode
        );

        const headings = rootNode.querySelectorAll('h1');
        expect(headings.length).toBe(1);
        expect(headings[0].textContent).toBe('Flying with children');
        expect(rootNode.querySelectorAll('h2, h3, h4, h5, h6').length).toBe(0);
        expect(rootNode.querySelector('.cmp-herobanner__intro').textContent).toBe(
            'Plan the journey before you fly.'
        );

        const image = rootNode.querySelector('img');
        expect(image.getAttribute('src')).toBe(IMAGE);
        expect(image.getAttribute('alt')).toBe('Children seated with a parent');
        expect(image.getAttribute('loading')).toBe('eager');
        expect(image.getAttribute('fetchpriority')).toBe('high');
        expect(image.getAttribute('loading')).not.toBe('lazy');

        const nav = rootNode.querySelector('nav');
        expect(nav.getAttribute('aria-label')).toBe('On this page');
        const anchors = nav.querySelectorAll('a');
        expect(anchors.length).toBe(2);
        expect(anchors[0].getAttribute('href')).toBe('#fares');
        expect(anchors[0].textContent).toBe('Fares');
        expect(anchors[1].getAttribute('href')).toBe('#baggage');
    });

    it('uses the file reference when src is absent and keeps an empty alt attribute', () => {
        ReactDOM.render(
            <HeroBanner
                title="Flying with children"
                fileReference={IMAGE}
                alt=""
                decorative={true}
            />,
            rootNode
        );

        const image = rootNode.querySelector('img');
        expect(image.getAttribute('src')).toBe(IMAGE);
        expect(image.hasAttribute('alt')).toBe(true);
        expect(image.getAttribute('alt')).toBe('');
        expect(image.getAttribute('loading')).toBe('eager');
        expect(rootNode.querySelector('nav')).toBeNull();
    });

    it('does not render a broken image or a nav when those fields are absent', () => {
        ReactDOM.render(
            <HeroBanner title="Flying with children" links={[]} src="" fileReference="   " />,
            rootNode
        );

        expect(rootNode.querySelector('h1').textContent).toBe('Flying with children');
        expect(rootNode.querySelector('img')).toBeNull();
        expect(rootNode.querySelector('img[src=""]')).toBeNull();
        expect(rootNode.querySelector('.cmp-herobanner__image-placeholder')).toBeNull();
        expect(rootNode.querySelector('nav')).toBeNull();
    });

    it('shows an image placeholder in the editor when the image has not been authored', () => {
        ReactDOM.render(
            <HeroBanner
                title="Flying with children"
                introduction="Plan the journey before you fly."
                isInEditor={true}
                links={[{ text: 'Fares', href: '#fares', anchor: '#fares' }]}
            />,
            rootNode
        );

        const section = rootNode.querySelector('.cmp-herobanner');
        const ordered = section.querySelectorAll(
            'h1, .cmp-herobanner__image-placeholder, .cmp-herobanner__intro, nav'
        );
        expect(rootNode.querySelector('img')).toBeNull();
        expect(ordered.length).toBe(4);
        expect(ordered[0].tagName).toBe('H1');
        expect(ordered[0].closest('.cmp-herobanner__inner')).not.toBeNull();
        expect(ordered[1].classList.contains('cmp-herobanner__image-placeholder')).toBe(true);
        expect(ordered[1].textContent).toBe('Configure hero image');
        expect(ordered[1].closest('.cmp-herobanner__inner')).toBeNull();
        expect(ordered[2].classList.contains('cmp-herobanner__intro')).toBe(true);
        expect(ordered[2].closest('.cmp-herobanner__inner')).not.toBeNull();
        expect(ordered[3].tagName).toBe('NAV');
        expect(rootNode.textContent).not.toContain('Configure Hero Banner');
    });

    it('reads the image alt only from props.alt', () => {
        ReactDOM.render(
            <HeroBanner
                title="Flying with children"
                src={IMAGE}
                alt="Children seated with a parent"
                decorative={false}
                dcDescription="DAM description that React must ignore"
            />,
            rootNode
        );

        const image = rootNode.querySelector('img');
        expect(image.getAttribute('src')).toBe(IMAGE);
        expect(image.getAttribute('alt')).toBe('Children seated with a parent');
        expect(rootNode.textContent).not.toContain('DAM description that React must ignore');
    });

    it('shows the editor placeholder when the banner is empty', () => {
        ReactDOM.render(<HeroBanner empty={true} isInEditor={true} />, rootNode);

        expect(rootNode.textContent).toContain('Configure Hero Banner');
        expect(rootNode.querySelector('h1')).toBeNull();
        expect(rootNode.querySelector('img')).toBeNull();
    });

    it('puts the model data layer string on the banner and pushes that payload', () => {
        const id = 'herobanner-0123456789';
        const dataLayer = JSON.stringify({
            [id]: {
                '@id': id,
                '@type': 'airline/components/herobanner',
                'dc:title': 'Flying with children'
            }
        });
        window.adobeDataLayer = [];

        act(() => {
            ReactDOM.render(
                <HeroBanner
                    id={id}
                    title="Flying with children"
                    src={IMAGE}
                    alt="Children seated with a parent"
                    links={[{ text: 'Fares', href: '#fares', anchor: '#fares' }]}
                    dataLayer={dataLayer}
                />,
                rootNode
            );
        });

        const section = rootNode.querySelector('section');
        expect(section.id).toBe(id);
        expect(section.getAttribute('data-cmp-data-layer')).toBe(dataLayer);
        expect(window.adobeDataLayer).toEqual([JSON.parse(dataLayer)]);
        expect(section.getAttribute('data-cmp-data-layer')).not.toContain('#fares');

        let defaultPrevented = false;
        function recordDefault(event) {
            defaultPrevented = event.defaultPrevented;
        }
        document.addEventListener('click', recordDefault);
        act(() => {
            section.querySelector('a').dispatchEvent(
                new MouseEvent('click', { bubbles: true, cancelable: true })
            );
        });
        document.removeEventListener('click', recordDefault);

        expect(defaultPrevented).toBe(false);
        expect(window.adobeDataLayer[1]).toEqual({
            event: 'cmp:jumpLinkClick',
            link: { label: 'Fares', anchorId: 'fares' }
        });
        expect(section.getAttribute('data-cmp-data-layer')).toBe(dataLayer);
    });

    it('pushes a jump link click with the anchor id and does not render an image without a src', () => {
        window.adobeDataLayer = [];

        act(() => {
            ReactDOM.render(
                <HeroBanner
                    title="Flying with children"
                    introduction="Plan the journey before you fly."
                    links={[
                        { text: 'Fares', href: '#fares' },
                        { text: 'Baggage', anchor: '##baggage' }
                    ]}
                />,
                rootNode
            );
        });

        expect(rootNode.querySelector('img')).toBeNull();
        const anchors = rootNode.querySelectorAll('a');
        act(() => {
            anchors[1].dispatchEvent(new MouseEvent('click', { bubbles: true, cancelable: true }));
        });

        expect(window.adobeDataLayer).toEqual([
            {
                event: 'cmp:jumpLinkClick',
                link: { label: 'Baggage', anchorId: 'baggage' }
            }
        ]);
    });

    it('does not invent a data layer when the model omitted it', () => {
        window.adobeDataLayer = [];

        act(() => {
            ReactDOM.render(<HeroBanner title="Flying with children" />, rootNode);
        });

        const section = rootNode.querySelector('section');
        expect(section.hasAttribute('data-cmp-data-layer')).toBe(false);
        expect(section.hasAttribute('id')).toBe(false);
        expect(window.adobeDataLayer).toEqual([]);
    });

    it('does not publish the data layer from an empty editor placeholder', () => {
        const dataLayer = '{"herobanner-x":{"@type":"airline/components/herobanner"}}';
        window.adobeDataLayer = [];

        act(() => {
            ReactDOM.render(
                <HeroBanner empty={true} isInEditor={true} dataLayer={dataLayer} id="herobanner-x" />,
                rootNode
            );
        });

        expect(rootNode.querySelector('[data-cmp-data-layer]')).toBeNull();
        expect(window.adobeDataLayer).toEqual([]);
    });

    it('renders nothing on publish when the banner is empty', () => {
        ReactDOM.render(
            <HeroBanner empty={true} isInEditor={false} title="" fileReference="" />,
            rootNode
        );

        expect(rootNode.childElementCount).toBe(0);
        expect(rootNode.querySelector('h1')).toBeNull();
        expect(rootNode.querySelector('img')).toBeNull();
    });
});
