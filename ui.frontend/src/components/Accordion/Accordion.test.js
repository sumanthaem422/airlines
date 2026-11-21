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
import Accordion, { AccordionEditConfig } from './Accordion';

describe('Accordion ->', () => {
    const RICH_TEXT = '<ul><li>Lap infants can travel on an adult ticket.</li></ul>';

    const accordionProps = {
        id: 'accordion-infants',
        title: 'Booking for infants',
        introduction: 'Learn how to book babies on a flight.',
        items: [
            {
                id: 'domestic',
                title: 'Domestic flights',
                text: RICH_TEXT,
                links: [
                    { text: 'More about bassinets', url: 'https://www.example.com/bassinet' },
                    {
                        text: 'Contact us',
                        url: '/content/airline/contact',
                        target: '_blank',
                        rel: 'noopener noreferrer'
                    }
                ]
            },
            {
                id: 'international',
                title: 'International flights',
                text: '<p>Own ticket required.</p>',
                links: []
            }
        ]
    };

    let rootNode;

    beforeEach(() => {
        rootNode = document.createElement('div');
        document.body.appendChild(rootNode);
        delete window.adobeDataLayer;
    });

    afterEach(() => {
        ReactDOM.unmountComponentAtNode(rootNode);
        document.body.removeChild(rootNode);
        delete window.adobeDataLayer;
    });

    function renderAccordion(extraProps) {
        act(() => {
            ReactDOM.render(
                <Accordion {...accordionProps} {...extraProps} />,
                rootNode
            );
        });
    }

    function headerButtons() {
        return rootNode.querySelectorAll('.cmp-accordion__trigger');
    }

    function click(element) {
        act(() => {
            element.dispatchEvent(new MouseEvent('click', { bubbles: true, cancelable: true }));
        });
    }

    it('uses the editor label and treats a missing title and items as empty', () => {
        expect(AccordionEditConfig.emptyLabel).toBe('Accordion');
        expect(AccordionEditConfig.isEmpty({ empty: true, title: 'Booking', items: accordionProps.items })).toBe(true);
        expect(AccordionEditConfig.isEmpty({ isEmpty: true, title: 'Booking', items: accordionProps.items })).toBe(true);
        expect(AccordionEditConfig.isEmpty({ title: 'Booking' })).toBe(false);
        expect(AccordionEditConfig.isEmpty({ sectionHeading: 'Booking' })).toBe(false);
        expect(AccordionEditConfig.isEmpty({ introduction: 'Only an introduction' })).toBe(true);
        expect(AccordionEditConfig.isEmpty({ items: [] })).toBe(true);
        expect(AccordionEditConfig.isEmpty(undefined)).toBe(true);
        expect(AccordionEditConfig.isEmpty({
            items: [{ id: 'domestic', title: 'Domestic flights' }]
        })).toBe(false);
    });

    it('renders title, introduction, and item titles from props', () => {
        renderAccordion();

        expect(rootNode.querySelector('h1')).toBeNull();
        expect(rootNode.querySelector('h2').textContent).toBe('Booking for infants');
        expect(rootNode.querySelector('.cmp-accordion__introduction').textContent)
            .toBe('Learn how to book babies on a flight.');
        const buttons = headerButtons();
        expect(buttons.length).toBe(2);
        expect(buttons[0].textContent).toContain('Domestic flights');
        expect(buttons[1].textContent).toContain('International flights');
        expect(buttons[0].tagName).toBe('BUTTON');
        expect(buttons[0].getAttribute('aria-expanded')).toBe('false');
        expect(rootNode.textContent).not.toContain('Infants on domestic flights');
        expect(rootNode.textContent).not.toContain('Booking a seat for your infant');
        expect(rootNode.textContent).not.toContain('How soon can infants travel after birth?');
        expect(rootNode.textContent).not.toContain("My infant isn't born yet, how do I book?");
        expect(rootNode.querySelector('.cmp-accordion__expand').textContent).toBe('Expand all items');
    });

    it('opens a panel from its header and leaves the other panel collapsed', () => {
        renderAccordion();

        const button = headerButtons()[0];
        const panel = document.getElementById(button.getAttribute('aria-controls'));
        expect(panel.getAttribute('role')).toBe('region');
        expect(panel.getAttribute('aria-labelledby')).toBe(button.id);
        expect(panel.hasAttribute('hidden')).toBe(true);

        click(button);

        expect(button.getAttribute('aria-expanded')).toBe('true');
        expect(panel.hasAttribute('hidden')).toBe(false);
        expect(panel.querySelector('.cmp-accordion__text').innerHTML).toBe(RICH_TEXT);
        expect(headerButtons()[1].getAttribute('aria-expanded')).toBe('false');
        expect(button.querySelector('.cmp-accordion__chevron').className).toContain('cmp-accordion__chevron--open');
    });

    it('expand all opens every item and collapse all closes them', () => {
        renderAccordion();

        click(rootNode.querySelector('.cmp-accordion__expand'));

        expect(rootNode.querySelector('.cmp-accordion__expand').textContent).toBe('Collapse all items');
        headerButtons().forEach(function(button) {
            expect(button.getAttribute('aria-expanded')).toBe('true');
        });

        click(rootNode.querySelector('.cmp-accordion__expand'));

        expect(rootNode.querySelector('.cmp-accordion__expand').textContent).toBe('Expand all items');
        headerButtons().forEach(function(button) {
            expect(button.getAttribute('aria-expanded')).toBe('false');
        });
    });

    it('reads Collapse all items after every panel is opened manually', () => {
        renderAccordion();

        click(headerButtons()[0]);
        click(headerButtons()[1]);

        expect(headerButtons()[0].getAttribute('aria-expanded')).toBe('true');
        expect(headerButtons()[1].getAttribute('aria-expanded')).toBe('true');
        expect(rootNode.querySelector('.cmp-accordion__expand').textContent).toBe('Collapse all items');
    });

    it('pushes accordionToggle with the item id, title, and new expanded state', () => {
        renderAccordion();

        click(headerButtons()[0]);

        expect(window.adobeDataLayer).toEqual([
            {
                event: 'cmp:accordionToggle',
                accordion: { id: 'domestic', title: 'Domestic flights', expanded: true }
            }
        ]);

        click(headerButtons()[0]);

        expect(window.adobeDataLayer[1]).toEqual({
            event: 'cmp:accordionToggle',
            accordion: { id: 'domestic', title: 'Domestic flights', expanded: false }
        });
    });

    it('pushes one toggle event per item whose state changes', () => {
        renderAccordion();

        click(headerButtons()[0]);
        click(rootNode.querySelector('.cmp-accordion__expand'));

        expect(window.adobeDataLayer).toEqual([
            {
                event: 'cmp:accordionToggle',
                accordion: { id: 'domestic', title: 'Domestic flights', expanded: true }
            },
            {
                event: 'cmp:accordionToggle',
                accordion: { id: 'international', title: 'International flights', expanded: true }
            }
        ]);
        expect(rootNode.querySelector('.cmp-accordion__expand').textContent).toBe('Collapse all items');
    });

    it('gives an external call to action a new tab and records the click without blocking navigation', () => {
        renderAccordion();

        click(headerButtons()[0]);

        const external = rootNode.querySelector('a[href="https://www.example.com/bassinet"]');
        const internal = rootNode.querySelector('a[href="/content/airline/contact"]');
        expect(external.getAttribute('target')).toBe('_blank');
        expect(external.getAttribute('rel')).toBe('noopener noreferrer');
        expect(external.textContent).toContain('More about bassinets');
        expect(external.textContent).toContain('→');
        expect(internal.hasAttribute('target')).toBe(false);
        expect(internal.hasAttribute('rel')).toBe(false);

        let defaultPrevented = false;
        function recordDefault(event) {
            defaultPrevented = event.defaultPrevented;
        }
        document.addEventListener('click', recordDefault);
        click(external);
        document.removeEventListener('click', recordDefault);

        expect(defaultPrevented).toBe(false);
        expect(window.adobeDataLayer[window.adobeDataLayer.length - 1]).toEqual({
            event: 'cmp:linkClick',
            link: {
                title: 'More about bassinets',
                url: 'https://www.example.com/bassinet',
                componentId: 'accordion-infants'
            }
        });
    });

    it('renders no list when the model is empty or nothing was authored', () => {
        act(() => {
            ReactDOM.render(<Accordion />, rootNode);
        });
        expect(rootNode.querySelector('ul')).toBeNull();
        expect(rootNode.querySelector('button')).toBeNull();
        expect(rootNode.textContent).not.toContain('Domestic flights');

        act(() => {
            ReactDOM.render(
                <Accordion empty title="Booking" items={accordionProps.items} />,
                rootNode
            );
        });
        expect(rootNode.querySelector('ul')).toBeNull();
        expect(rootNode.querySelector('.cmp-accordion__item')).toBeNull();
        expect(rootNode.textContent).not.toContain('Domestic flights');

        act(() => {
            ReactDOM.render(<Accordion isInEditor />, rootNode);
        });
        const placeholder = rootNode.querySelector('.cmp-accordion__placeholder');
        expect(rootNode.querySelector('ul')).toBeNull();
        expect(rootNode.querySelector('dl, dt, dd')).toBeNull();
        expect(placeholder.getAttribute('role')).toBe('status');
        expect(placeholder.getAttribute('aria-live')).toBe('polite');
        expect(placeholder.textContent).toContain('Accordion');
    });

    it('wraps each panel button in a heading one level below the section title', () => {
        renderAccordion();

        expect(rootNode.querySelector('.cmp-accordion__title').tagName).toBe('H2');
        expect(rootNode.querySelector('dl, dt, dd')).toBeNull();
        headerButtons().forEach(function(button) {
            expect(button.parentElement.tagName).toBe('H3');
            expect(button.closest('.cmp-accordion__item').tagName).toBe('DIV');
        });

        renderAccordion({ headingLevel: 'h3' });
        expect(rootNode.querySelector('.cmp-accordion__title').tagName).toBe('H3');
        expect(headerButtons()[0].parentElement.tagName).toBe('H4');

        renderAccordion({ headingLevel: 'h1' });
        expect(rootNode.querySelector('.cmp-accordion__title').tagName).toBe('H2');
        expect(headerButtons()[0].parentElement.tagName).toBe('H3');
        expect(rootNode.querySelector('h1')).toBeNull();

        renderAccordion({ headingLevel: 'h6' });
        expect(rootNode.querySelector('.cmp-accordion__title').tagName).toBe('H6');
        expect(headerButtons()[0].parentElement.tagName).toBe('H6');
    });

    function pressKey(element, key) {
        let event;
        act(() => {
            event = new KeyboardEvent('keydown', {
                key: key,
                bubbles: true,
                cancelable: true
            });
            element.dispatchEvent(event);
        });
        return event;
    }

    it('moves focus to the next header button on ArrowDown and wraps with ArrowUp', () => {
        renderAccordion();
        const buttons = headerButtons();

        act(() => {
            buttons[0].focus();
        });
        const down = pressKey(buttons[0], 'ArrowDown');

        expect(document.activeElement).toBe(buttons[1]);
        expect(down.defaultPrevented).toBe(true);
        expect(buttons[0].getAttribute('aria-expanded')).toBe('false');
        expect(buttons[1].getAttribute('aria-expanded')).toBe('false');

        const up = pressKey(document.activeElement, 'ArrowUp');
        expect(document.activeElement).toBe(buttons[0]);
        expect(up.defaultPrevented).toBe(true);

        pressKey(buttons[0], 'ArrowUp');
        expect(document.activeElement).toBe(buttons[1]);
    });

    it('moves focus to the first header on Home and the last on End', () => {
        renderAccordion();
        const buttons = headerButtons();

        act(() => {
            buttons[1].focus();
        });
        const home = pressKey(document.activeElement, 'Home');
        expect(document.activeElement).toBe(buttons[0]);
        expect(home.defaultPrevented).toBe(true);

        const end = pressKey(document.activeElement, 'End');
        expect(document.activeElement).toBe(buttons[1]);
        expect(end.defaultPrevented).toBe(true);
    });

    it('exposes the editor placeholder as a polite status when isEmpty is set', () => {
        act(() => {
            ReactDOM.render(
                <Accordion isInEditor isEmpty title="Booking" items={accordionProps.items} />,
                rootNode
            );
        });

        const placeholder = rootNode.querySelector('.cmp-accordion__placeholder');
        expect(placeholder.getAttribute('role')).toBe('status');
        expect(placeholder.getAttribute('aria-live')).toBe('polite');
        expect(placeholder.textContent).toContain('Accordion');
        expect(rootNode.querySelector('.cmp-accordion__trigger')).toBeNull();
        expect(rootNode.textContent).not.toContain('Domestic flights');
    });

    it('reads sectionHeading, question, and answer without dropping links', () => {
        act(() => {
            ReactDOM.render(
                <Accordion
                    id="accordion-alias"
                    sectionHeading="Travel help"
                    introduction="Short intro"
                    items={[
                        {
                            id: 'bags',
                            question: 'Cabin bags',
                            answer: '<p>One bag.</p>',
                            links: [{ text: 'Baggage', url: '/content/airline/bags' }]
                        }
                    ]}
                />,
                rootNode
            );
        });

        expect(rootNode.querySelector('.cmp-accordion__title').textContent).toBe('Travel help');
        expect(rootNode.querySelector('.cmp-accordion__title').tagName).toBe('H2');
        expect(rootNode.querySelector('.cmp-accordion__introduction').textContent).toBe('Short intro');
        const button = headerButtons()[0];
        expect(button.parentElement.tagName).toBe('H3');
        expect(button.textContent).toContain('Cabin bags');
        click(button);
        expect(rootNode.querySelector('.cmp-accordion__text').innerHTML).toBe('<p>One bag.</p>');
        expect(rootNode.querySelector('a[href="/content/airline/bags"]').textContent).toContain('Baggage');
        expect(window.adobeDataLayer[0]).toEqual({
            event: 'cmp:accordionToggle',
            accordion: { id: 'bags', title: 'Cabin bags', expanded: true }
        });
    });
});
