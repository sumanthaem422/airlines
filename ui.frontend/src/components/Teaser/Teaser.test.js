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
import JoeyClubTeaser, { JoeyClubTeaserEditConfig } from './Teaser';

describe('JoeyClubTeaser ->', () => {
    const IMAGE = '/content/dam/airline/joey.png';
    const DESCRIPTION = '<p>Fly with <strong>Bluey</strong>.</p>';

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

    it('uses the editor label and the model empty flag', () => {
        expect(JoeyClubTeaserEditConfig.emptyLabel).toBe('Joey Club Teaser');
        expect(JoeyClubTeaserEditConfig.isEmpty({ empty: true, fileReference: IMAGE })).toBe(true);
        expect(JoeyClubTeaserEditConfig.isEmpty({
            empty: false,
            title: 'Joey Club'
        })).toBe(false);
        expect(JoeyClubTeaserEditConfig.isEmpty({
            image: { src: IMAGE },
            fileReference: IMAGE
        })).toBe(true);
        expect(JoeyClubTeaserEditConfig.isEmpty({
            description: DESCRIPTION
        })).toBe(false);
        expect(JoeyClubTeaserEditConfig.isEmpty({
            title: '   ',
            description: '  '
        })).toBe(true);
    });

    it('renders the heading tag from headingLevel and an external call to action', () => {
        ReactDOM.render(
            <JoeyClubTeaser
                id="teaser-1"
                title="Joey Club"
                description={DESCRIPTION}
                fileReference={IMAGE}
                alt="Bluey and Bingo beside the aircraft"
                headingLevel="h3"
                linkText="Explore aa"
                linkURL="https://www.aa.com"
            />,
            rootNode
        );

        const heading = rootNode.querySelector('h3');
        expect(heading).not.toBeNull();
        expect(heading.textContent).toBe('Joey Club');
        expect(heading.classList.contains('cmp-joeyclubteaser__title')).toBe(true);
        expect(rootNode.querySelector('h1')).toBeNull();
        expect(rootNode.querySelector('h2')).toBeNull();
        expect(rootNode.querySelector('.cmp-joeyclubteaser__description').innerHTML).toBe(DESCRIPTION);

        const anchor = rootNode.querySelector('a');
        expect(anchor.getAttribute('href')).toBe('https://www.aa.com');
        expect(anchor.getAttribute('target')).toBe('_blank');
        expect(anchor.getAttribute('rel')).toBe('noopener noreferrer');
        expect(anchor.textContent).toContain('Explore aa');
        expect(anchor.querySelector('.cmp-joeyclubteaser__arrow')).not.toBeNull();

        const image = rootNode.querySelector('img');
        expect(image.getAttribute('src')).toBe(IMAGE);
        expect(image.getAttribute('alt')).toBe('Bluey and Bingo beside the aircraft');
        expect(image.hasAttribute('aria-hidden')).toBe(false);
    });

    it('defaults a missing or illegal heading to h2 and omits an empty rel', () => {
        ReactDOM.render(
            <JoeyClubTeaser
                title="Joey Club"
                headingLevel="h1"
                linkText="Explore aa and Bluey"
                linkURL="/content/airline/joey"
                rel=""
            />,
            rootNode
        );

        expect(rootNode.querySelector('h2').textContent).toBe('Joey Club');
        expect(rootNode.querySelector('h1')).toBeNull();
        const anchor = rootNode.querySelector('a');
        expect(anchor.hasAttribute('target')).toBe(false);
        expect(anchor.hasAttribute('rel')).toBe(false);
    });

    it('renders the model alt, including an empty alt, and sets rel only when it is non-empty', () => {
        ReactDOM.render(
            <JoeyClubTeaser
                title="Joey Club"
                src={IMAGE}
                alt=""
            />,
            rootNode
        );

        const image = rootNode.querySelector('img');
        expect(image.getAttribute('src')).toBe(IMAGE);
        expect(image.hasAttribute('alt')).toBe(true);
        expect(image.getAttribute('alt')).toBe('');
        expect(image.hasAttribute('aria-hidden')).toBe(false);

        ReactDOM.render(
            <JoeyClubTeaser
                title="Joey Club"
                linkText="Explore aa and Bluey"
                linkURL="/content/airline/joey"
                rel="nofollow"
            />,
            rootNode
        );

        const anchor = rootNode.querySelector('a');
        expect(anchor.hasAttribute('target')).toBe(false);
        expect(anchor.getAttribute('rel')).toBe('nofollow');
    });

    it('pushes cmp:linkClick with the CTA title, url, and component id without blocking navigation', () => {
        window.adobeDataLayer = [];
        let defaultPrevented = false;

        act(() => {
            ReactDOM.render(
                <JoeyClubTeaser
                    id="teaser-joey"
                    title="Joey Club"
                    description={DESCRIPTION}
                    fileReference={IMAGE}
                    alt="Bluey illustration"
                    linkText="Explore aa and Bluey"
                    linkURL="https://www.aa.com/joey"
                />,
                rootNode
            );
        });

        function recordDefault(event) {
            defaultPrevented = event.defaultPrevented;
        }
        document.addEventListener('click', recordDefault);
        act(() => {
            rootNode.querySelector('a').dispatchEvent(
                new MouseEvent('click', { bubbles: true, cancelable: true })
            );
        });
        document.removeEventListener('click', recordDefault);

        expect(defaultPrevented).toBe(false);
        expect(window.adobeDataLayer).toEqual([
            {
                event: 'cmp:linkClick',
                link: {
                    title: 'Explore aa and Bluey',
                    url: 'https://www.aa.com/joey',
                    componentId: 'teaser-joey'
                }
            }
        ]);
        expect(JSON.stringify(window.adobeDataLayer[0])).not.toContain('enquiry');
    });

    it('does not render an image or heading when the model is empty', () => {
        ReactDOM.render(
            <JoeyClubTeaser
                empty={true}
                isInEditor={true}
                title=""
                description=""
                fileReference={IMAGE}
            />,
            rootNode
        );

        expect(rootNode.textContent).toContain('Joey Club Teaser');
        expect(rootNode.querySelector('img')).toBeNull();
        expect(rootNode.querySelector('h1, h2, h3, h4')).toBeNull();

        ReactDOM.render(
            <JoeyClubTeaser empty={true} isInEditor={false} title="" description="" fileReference="" />,
            rootNode
        );

        expect(rootNode.childElementCount).toBe(0);
        expect(rootNode.querySelector('img')).toBeNull();
    });

    it('renders the image from image.src and from fileReference', () => {
        ReactDOM.render(
            <JoeyClubTeaser
                title="aa and Bluey"
                image={{ src: '/content/dam/airline/from-core.png', alt: 'From core' }}
                fileReference={IMAGE}
            />,
            rootNode
        );

        let image = rootNode.querySelector('img');
        expect(image.getAttribute('src')).toBe('/content/dam/airline/from-core.png');
        expect(image.getAttribute('alt')).toBe('From core');

        ReactDOM.render(
            <JoeyClubTeaser
                title="aa and Bluey"
                fileReference={IMAGE}
                alt="Bluey and Bingo beside the aircraft"
            />,
            rootNode
        );

        image = rootNode.querySelector('img');
        expect(image.getAttribute('src')).toBe(IMAGE);
        expect(image.getAttribute('alt')).toBe('Bluey and Bingo beside the aircraft');

        ReactDOM.render(
            <JoeyClubTeaser
                title="aa and Bluey"
                {...{ ':items': { image: { src: '/content/dam/airline/from-items.png', alt: 'Nested' } } }}
            />,
            rootNode
        );

        image = rootNode.querySelector('img');
        expect(image.getAttribute('src')).toBe('/content/dam/airline/from-items.png');
        expect(image.getAttribute('alt')).toBe('Nested');

        ReactDOM.render(
            <JoeyClubTeaser title="aa and Bluey" description={DESCRIPTION} />,
            rootNode
        );

        expect(rootNode.querySelector('img')).toBeNull();
        expect(rootNode.querySelector('.cmp-joeyclubteaser__description').innerHTML).toBe(DESCRIPTION);
    });

    it('renders the call to action from actions and keeps a content path in the same tab', () => {
        ReactDOM.render(
            <JoeyClubTeaser
                title="aa and Bluey"
                description={DESCRIPTION}
                actions={[{
                    title: 'Explore aa and Bluey',
                    text: 'Secondary label',
                    url: '/content/airline/us/en/bluey'
                }]}
            />,
            rootNode
        );

        expect(rootNode.querySelector('.cmp-joeyclubteaser__description').innerHTML).toBe(DESCRIPTION);
        const anchor = rootNode.querySelector('a');
        expect(anchor.getAttribute('href')).toBe('/content/airline/us/en/bluey');
        expect(anchor.textContent).toContain('Explore aa and Bluey');
        expect(anchor.textContent).not.toContain('Secondary label');
        expect(anchor.hasAttribute('target')).toBe(false);
        expect(anchor.hasAttribute('rel')).toBe(false);
        expect(anchor.querySelector('.cmp-joeyclubteaser__arrow')).not.toBeNull();

        ReactDOM.render(
            <JoeyClubTeaser
                title="aa and Bluey"
                actions={[{ text: 'From text', url: '/content/airline/joey' }]}
            />,
            rootNode
        );

        expect(rootNode.querySelector('a').textContent).toContain('From text');
    });

    it('sets noopener noreferrer when the link is external or opens in a new tab', () => {
        ReactDOM.render(
            <JoeyClubTeaser
                title="aa and Bluey"
                actions={[{ title: 'Explore aa and Bluey', url: 'https://www.aa.com/joey' }]}
            />,
            rootNode
        );

        let anchor = rootNode.querySelector('a');
        expect(anchor.getAttribute('target')).toBe('_blank');
        expect(anchor.getAttribute('rel')).toBe('noopener noreferrer');

        ReactDOM.render(
            <JoeyClubTeaser
                title="aa and Bluey"
                linkText="Explore aa and Bluey"
                linkURL="/content/airline/joey"
                linkTarget="_blank"
            />,
            rootNode
        );

        anchor = rootNode.querySelector('a');
        expect(anchor.getAttribute('href')).toBe('/content/airline/joey');
        expect(anchor.getAttribute('target')).toBe('_blank');
        expect(anchor.getAttribute('rel')).toBe('noopener noreferrer');

        ReactDOM.render(
            <JoeyClubTeaser
                title="aa and Bluey"
                actions={[{ title: 'Explore', url: '/content/airline/joey', target: '_blank' }]}
                rel="nofollow"
            />,
            rootNode
        );

        anchor = rootNode.querySelector('a');
        expect(anchor.getAttribute('target')).toBe('_blank');
        expect(anchor.getAttribute('rel')).toBe('nofollow');
    });

    it('pushes cmp:linkClick for an actions call to action without blocking navigation', () => {
        window.adobeDataLayer = [];
        let defaultPrevented = false;

        act(() => {
            ReactDOM.render(
                <JoeyClubTeaser
                    id="teaser-core"
                    title="aa and Bluey"
                    actions={[{ title: 'Explore aa and Bluey', url: 'https://www.aa.com/joey' }]}
                />,
                rootNode
            );
        });

        function recordDefault(event) {
            defaultPrevented = event.defaultPrevented;
        }
        document.addEventListener('click', recordDefault);
        act(() => {
            rootNode.querySelector('a').dispatchEvent(
                new MouseEvent('click', { bubbles: true, cancelable: true })
            );
        });
        document.removeEventListener('click', recordDefault);

        expect(defaultPrevented).toBe(false);
        expect(window.adobeDataLayer).toEqual([
            {
                event: 'cmp:linkClick',
                link: {
                    title: 'Explore aa and Bluey',
                    url: 'https://www.aa.com/joey',
                    componentId: 'teaser-core'
                }
            }
        ]);
    });

    it('renders an h3 from titleType and never an h1', () => {
        ReactDOM.render(
            <JoeyClubTeaser title="aa and Bluey" titleType="h3" headingLevel="h2" />,
            rootNode
        );

        const heading = rootNode.querySelector('h3');
        expect(heading).not.toBeNull();
        expect(heading.textContent).toBe('aa and Bluey');
        expect(heading.classList.contains('cmp-joeyclubteaser__title')).toBe(true);
        expect(rootNode.querySelector('h2')).toBeNull();
        expect(rootNode.querySelector('h1')).toBeNull();

        ReactDOM.render(
            <JoeyClubTeaser title="aa and Bluey" titleType="h1" headingLevel="h4" />,
            rootNode
        );

        expect(rootNode.querySelector('h4').textContent).toBe('aa and Bluey');
        expect(rootNode.querySelector('h1')).toBeNull();
        expect(rootNode.querySelector('h3')).toBeNull();
    });

    it('is empty when title and description are missing even if an image exists', () => {
        ReactDOM.render(
            <JoeyClubTeaser
                isInEditor={true}
                title=""
                description=""
                image={{ src: IMAGE }}
                fileReference={IMAGE}
            />,
            rootNode
        );

        expect(rootNode.textContent).toContain('Joey Club Teaser');
        expect(rootNode.querySelector('img')).toBeNull();
        expect(rootNode.querySelector('h1, h2, h3, h4')).toBeNull();

        ReactDOM.render(
            <JoeyClubTeaser isInEditor={false} title="aa and Bluey" />,
            rootNode
        );

        expect(rootNode.querySelector('h2').textContent).toBe('aa and Bluey');
        expect(rootNode.querySelector('img')).toBeNull();
    });
});
