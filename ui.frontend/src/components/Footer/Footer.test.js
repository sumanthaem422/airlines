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
import Footer from './Footer';

describe('Footer ->', () => {
    const HOME = '/content/airline/us/en/home.html';
    const FALLBACK_COPYRIGHT = '© 2026 Airline Travel Info. All rights reserved.';

    let rootNode;

    beforeEach(() => {
        rootNode = document.createElement('div');
        document.body.appendChild(rootNode);
    });

    afterEach(() => {
        ReactDOM.unmountComponentAtNode(rootNode);
        document.body.removeChild(rootNode);
    });

    function links() {
        return rootNode.querySelectorAll('.airline-footer__link');
    }

    it('renders fallback links and copyright when items and copyrightText are absent', () => {
        ReactDOM.render(<Footer />, rootNode);

        const footer = rootNode.querySelector('footer.airline-footer');
        expect(footer.getAttribute('role')).toBe('contentinfo');
        expect(rootNode.querySelector('.airline-footer__name').textContent).toBe('Airline Travel Info');
        expect(rootNode.querySelector('.airline-footer__tagline').textContent).toBe(
            'Helping families travel smarter.'
        );

        const anchors = links();
        expect(anchors.length).toBe(3);
        expect(anchors[0].textContent).toBe('Flying with infants');
        expect(anchors[0].getAttribute('href')).toBe(HOME);
        expect(anchors[1].textContent).toBe('Travel requirements');
        expect(anchors[1].getAttribute('href')).toBe(HOME);
        expect(anchors[2].textContent).toBe('Contact us');
        expect(anchors[2].getAttribute('href')).toBe(HOME);
        expect(rootNode.querySelector('.airline-footer__copyright').textContent).toBe(FALLBACK_COPYRIGHT);
    });

    it('renders props.items and props.copyrightText when provided', () => {
        ReactDOM.render(
            <Footer
                items={[
                    { title: 'Baggage', url: '/content/airline/us/en/baggage.html' },
                    { title: 'Fares', url: '/content/airline/us/en/fares.html' }
                ]}
                copyrightText="© 2026 Custom notice."
            />,
            rootNode
        );

        const anchors = links();
        expect(anchors.length).toBe(2);
        expect(anchors[0].textContent).toBe('Baggage');
        expect(anchors[0].getAttribute('href')).toBe('/content/airline/us/en/baggage.html');
        expect(anchors[1].textContent).toBe('Fares');
        expect(anchors[1].getAttribute('href')).toBe('/content/airline/us/en/fares.html');
        expect(rootNode.querySelector('.airline-footer__copyright').textContent).toBe('© 2026 Custom notice.');
    });

    it('uses the fallback copyright when copyrightText is blank', () => {
        ReactDOM.render(<Footer copyrightText="   " items={[]} />, rootNode);

        expect(links().length).toBe(3);
        expect(rootNode.querySelector('.airline-footer__copyright').textContent).toBe(FALLBACK_COPYRIGHT);
    });
});
