import { pushAdobeDataLayer, pushLinkClick, pushPageShow } from './adobeDataLayer';

describe('adobeDataLayer ->', () => {
  afterEach(() => {
    delete window.adobeDataLayer;
  });

  it('creates the array and appends a plain object', () => {
    delete window.adobeDataLayer;

    pushAdobeDataLayer({ event: 'cmp:show', eventInfo: { path: 'page', type: 'page' } });
    pushAdobeDataLayer({ event: 'cmp:jumpLinkClick', link: { label: 'Fares', anchorId: 'fares' } });

    expect(window.adobeDataLayer).toEqual([
      { event: 'cmp:show', eventInfo: { path: 'page', type: 'page' } },
      { event: 'cmp:jumpLinkClick', link: { label: 'Fares', anchorId: 'fares' } }
    ]);
  });

  it('pushes a page show event with title and id when the model has them', () => {
    pushPageShow({
      title: '  Flying with children  ',
      id: 'page-flying',
      email: 'person@example.com',
      enquiry: 'Do not send this'
    });

    expect(window.adobeDataLayer).toEqual([
      {
        event: 'cmp:show',
        eventInfo: { path: 'page', type: 'page' },
        page: { title: 'Flying with children', id: 'page-flying' }
      }
    ]);
    expect(JSON.stringify(window.adobeDataLayer[0])).not.toContain('person@example.com');
    expect(JSON.stringify(window.adobeDataLayer[0])).not.toContain('Do not send this');
  });

  it('pushes a link click with the CTA title and url only', () => {
    pushLinkClick({
      title: ' Join Joey Club ',
      url: ' https://example.com/joey-club ',
      email: 'person@example.com',
      enquiry: 'Do not send this'
    });

    expect(window.adobeDataLayer).toEqual([
      {
        event: 'cmp:linkClick',
        link: {
          title: 'Join Joey Club',
          url: 'https://example.com/joey-club'
        }
      }
    ]);
    expect(JSON.stringify(window.adobeDataLayer[0])).not.toContain('person@example.com');
    expect(JSON.stringify(window.adobeDataLayer[0])).not.toContain('Do not send this');
  });

  it('includes componentId on a link click when it is provided', () => {
    pushLinkClick({
      title: ' Explore Qantas and Bluey ',
      url: ' https://www.qantas.com/joey ',
      componentId: ' teaser-joey ',
      email: 'person@example.com',
      enquiry: 'Do not send this'
    });

    expect(window.adobeDataLayer).toEqual([
      {
        event: 'cmp:linkClick',
        link: {
          title: 'Explore Qantas and Bluey',
          url: 'https://www.qantas.com/joey',
          componentId: 'teaser-joey'
        }
      }
    ]);
    expect(JSON.stringify(window.adobeDataLayer[0])).not.toContain('person@example.com');
    expect(JSON.stringify(window.adobeDataLayer[0])).not.toContain('Do not send this');
  });

  it('omits the page object when title and id are blank', () => {
    pushPageShow({ title: '   ', id: '' });

    expect(window.adobeDataLayer).toEqual([
      {
        event: 'cmp:show',
        eventInfo: { path: 'page', type: 'page' }
      }
    ]);
  });
});
