import { Page, withModel } from '@adobe/aem-react-editable-components';
import React from 'react';
import { pushPageShow } from './utils/adobeDataLayer';

class App extends Page {
  componentDidMount() {
    pushPageShow(this.props);
  }

  render() {
    return (
      <div className="airline-app">
        {/* childPages handles AppPage which renders all template content including
            locked XF header/footer + editable responsivegrid content.
            Do NOT also call childComponents here — that causes double render. */}
        {this.childPages}
      </div>
    );
  }
}

export default withModel(App);