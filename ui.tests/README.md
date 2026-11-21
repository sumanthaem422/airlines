# Airline Travel Info

AEM as a Cloud Service project (Java, Maven, React SPA Editor). This file is the clone-and-run guide for the repository. The `ui.tests` module is also the Cypress UI test module Cloud Manager runs in the Custom UI Testing step.

Commands below are the ones in this repository. This document does not record a build result.

## Prerequisites and versions

| Item | Version or value | Where it is set |
| --- | --- | --- |
| Cloud Manager Java | 21 | `.cloudmanager/java-version`. `AGENTS.md` says to use the same Java for local builds |
| Compiler release | 11 | Root `pom.xml` (`maven-compiler-plugin`, `release` 11). The enforcer requires Java 11 or higher and Maven 3.3.9 or higher |
| AEM SDK API | `2026.11.29643.20261114T000343Z-261000` | Root `pom.xml` property `aem.sdk.api` |
| Core Components | `2.28.0` | Root `pom.xml` property `core.wcm.components.version` |
| SPA project core | `1.3.16` | Root `pom.xml` property `spa.project.core.version` |
| Maven frontend Node / npm | `v16.17.0` / `8.15.0` | Root `pom.xml` `frontend-maven-plugin` |
| React | `^16.12.0` | `ui.frontend/package.json` |
| `@adobe/aem-react-editable-components` | `^1.1.6` | `ui.frontend/package.json` |
| `react-scripts` | `4.0.3` | `ui.frontend/package.json` |
| JUnit Jupiter | `5.8.2` (`junit-bom`) | Root `pom.xml` |
| AEM Mocks | `io.wcm.testing.aem-mock.junit5` `5.5.4` | Root `pom.xml` |
| Cypress | `^13.14.1`; Docker image `cypress/included:13.14.1` | `ui.tests/test-module/package.json`, `ui.tests/Dockerfile` |
| UI-test lint Node / npm | `v18.14.2` / `9.5.0` | `ui.tests/pom.xml` profile `ui-tests-check` |
| Integration-test client | `aem-cloud-testing-clients` `1.3.0`, JUnit `4.13.2` | `it.tests/pom.xml` |
| Author / publish | `localhost:4502` / `localhost:4503`, user `admin` | Root `pom.xml` |

Local Node 24: `ui.frontend/.npmrc` sets `legacy-peer-deps=true` because `sass-loader@6` peers `node-sass`, which cannot build on Node 24. Install with `npm ci`. `ui.frontend/package.json` runs start, build, and test through `scripts/with-openssl-legacy.sh`, which adds `--openssl-legacy-provider` when Node is 17 or newer. The Maven frontend plugin installs its own Node `v16.17.0` and does not need that wrapper.

`core/pom.xml` still widens the bundle `Import-Package` for the local SDK: `javax.annotation;version=0.0.0`, `com.adobe.cq.wcm.core.components.models;version="[12.29,14)"`, `com.day.cq.wcm.api;version="[1.27,2)"`, `com.fasterxml.jackson.annotation;version="[2.12,3)"`, and `*`.

## Setup and deploy (clone to a rendered page)

1. Clone the repository and work from its root. A clone [URL](https://github.com/sumanthaem422/airlines) is not recorded in the project README.
2. Use Java 21 (`.cloudmanager/java-version`) and Maven 3.3.9 or higher.
3. Start a local AEM SDK author on port 4502. `AGENTS.md` describes testing against a local Quickstart. This repository does not include the Quickstart jar.
4. Build every module:
   ```
     mvn clean install
   ```
5. Build and deploy the `all` package to that author (`aem.host` `localhost`, `aem.port` `4502`, user `admin`):
   ```
     mvn clean install -PautoInstallSinglePackage
   ```

     Publish uses `-PautoInstallSinglePackagePublish` or `-Daem.port=4503`. One module: `mvn clean install -pl <module> -PautoInstallPackage`. The bundle only: `mvn clean install -pl <module> -PautoInstallBundle`.
6. Open the sample page on author:

   `http://localhost:4502/content/airline/us/en/home.html`

     The page is `/content/airline/us/en/home`, template `spa-page-template`, resource type `airline/components/page`, title "Airline Travel Info Home Page". The SPA root is `/content/airline/us/en` (`airline/components/spa`). Template structure adds the header and footer experience fragments. The editable grid in the sample page contains `airline/components/text` with "Hello World!".
7. Frontend dev server (AEM must already be on 4502):
   ```
     cd ui.frontend && npm ci && npm start
   ```

     `AGENTS.md` documents port 3000. `package.json` proxies `http://localhost:4502`. `.env.development` sets `REACT_APP_PROXY_ENABLED=true`, `REACT_APP_API_HOST=http://localhost:4502`, `REACT_APP_PAGE_MODEL_PATH=/content/airline/us/en.model.json`, and `REACT_APP_ROOT=/content/airline/us/en/home.html`. In development, `public/index.html` redirects `/` to that root. Frontend build only: `cd ui.frontend && npm run build`.

`AGENTS.md` documents Dispatcher validation as `cd dispatcher && ./bin/validate.sh src`. That script is not in the repo. The configuration that is present lives under `dispatcher/src`.

## Project structure

| Module | Role |
| --- | --- |
| `core` | OSGi bundle: Sling Models, JUnit 5 |
| `ui.apps` | Components, HTL, client libraries (`airline.react` from `ui.frontend/clientlib.config.js`) |
| `ui.apps.structure` | Repository structure package |
| `ui.config` | OSGi configuration |
| `ui.content` | Sample content, templates, experience fragments |
| `ui.frontend` | React SPA (`@adobe/aem-react-editable-components`). The build is copied into `ui.apps` as a client library |
| `ui.tests` | Cypress UI tests. Cloud Manager Custom UI Testing |
| `it.tests` | Java integration tests (`*IT.java`). Cloud Manager Custom Functional Testing |
| `dispatcher` | Cloud Dispatcher configuration |
| `all` | Single FileVault package that embeds the others |

Group `com.example.airline`, artifact `airline`, version `1.0.0-SNAPSHOT`. `archetype.properties` records `aemVersion=cloud`, `frontendModule=react`, `includeDispatcherConfig=y`, `datalayer=y`, and `sdkVersion=latest`. A numeric archetype version is not in that file.

## Component reference

`ui.frontend/src/index.css` sets `--air-gutter-mobile` to `1rem`, `--air-gutter-tablet` to `1.5rem`, `--air-gutter-desktop` to `2rem`, and `--air-max-w` to `1280px`.

| Component | Resource type | Dialog | React |
| --- | --- | --- | --- |
| Header | `airline/components/header`, super type `core/wcm/components/navigation/v1/navigation`, group hidden | No dialog in this component | `ui.frontend/src/components/Header/Header.js` |
| Footer | `airline/components/footer`, no super type, group hidden | No dialog in this component | `ui.frontend/src/components/Footer/Footer.js` |
| Hero banner | `airline/components/herobanner`, no super type, group `Airline Travel Info - Content` | `herobanner/_cq_dialog` | `ui.frontend/src/components/HeroBanner/HeroBanner.js` |
| Joey Club teaser | `airline/components/teaser`, super type `core/wcm/components/teaser/v2/teaser`, `imageDelegate` `airline/components/image` | `teaser/_cq_dialog`; design dialog `teaser/_cq_design_dialog` | `ui.frontend/src/components/Teaser/Teaser.js` |
| Accordion | `airline/components/accordion`, no super type, group `Airline Travel Info - Content` | `accordion/_cq_dialog` | `ui.frontend/src/components/Accordion/Accordion.js` |

Header. Logo links to `/content/airline/us/en/home.html`. Skip link points at `#main-content`. Navigation items come from the Core Navigation model (`items`, root `/content/airline/us/en` on the header experience fragment). The menu button is hidden from `768px` up (`Header.css` shows it at `max-width: 767px`).

Footer. `FooterImpl` exports `copyrightText` (`airline/components/footer`). The footer fragment sets that text. React falls back to "© 2026 Airline Travel Info. All rights reserved." and three links to the home page when the model has no items.

Hero banner. Title is an `h1`. Introduction is a paragraph. The image uses `loading="eager"` and `fetchpriority="high"`. `HeroBannerImpl` resolves alt as the authored `alt`, then DAM `dc:title`, then the component title. A decorative image returns an empty alt. Jump links are a multifield `links` with `text` and `anchor`. The model exports `anchor` and `href` as the same `#id` value. React `emptyLabel` is `Configure Hero Banner`. HTL `herobanner.html` only renders the Core Components empty placeholder.

Teaser. Dialog fields are `title`, `description`, `fileReference`, `linkText`, and `linkURL`. There is no author alt field. `JoeyClubTeaserImpl` delegates with `@Self` and `@Via(ResourceSuperType.class)` and takes alt from DAM `dc:description`. The design dialog `headingElement` allows `h2`, `h3`, and `h4`. React defaults the title to `h2` and sets `rel="noopener noreferrer"` when the link opens in a new tab. React `emptyLabel` is `Joey Club Teaser`.

Accordion. Custom component. Dialog fields are `title`, `introduction`, and `items` (`itemTitle`, rich text `itemText`, nested `links` with `linkText` and `linkURL`). Items are child nodes of the component, not Content Fragments. React puts each item button inside a heading (`h3` when the section heading is the default `h2`). Buttons expose `aria-expanded` and `aria-controls`. Arrow Up, Arrow Down, Home, and End move focus between those buttons.

`airline/components/page` extends `spa-project-core/components/page`. `airline/components/xfpage` extends `spa-project-core/components/xf-page`. Both are shells. The header and footer fragments use `xfpage` and template `xf-web-variation`.

## How to run each test suite

### Core (JUnit 5, AEM Mocks)

From the repository root:

```
mvn -pl core test
```

One class:

```
mvn -pl core test -Dtest=HeroBannerImplTest
mvn -pl core test -Dtest=JoeyClubTeaserImplTest
mvn -pl core test -Dtest=AccordionImplTest
mvn -pl core test -Dtest=FooterImplTest
```

Those four classes are in `core/src/test/java/com/example/airline/core/models/`. They use `AemContextExtension`. The root README also documents `mvn clean test` for the reactor unit tests.

### ui.frontend (Jest)

```
cd ui.frontend && npm ci && CI=true npm test -- --watchAll=false
```

`npm test` is `react-scripts test` wrapped by `scripts/with-openssl-legacy.sh`. `CI=true` stops watch mode.

### ui.tests (Cypress)

Cloud Manager runs this module in the Custom UI Testing step. Tests live in `test-module`. The specs there are `basic.cy.js`, `login.cy.js`, `assets.cy.js`, and `console_error.cy.js`. They sign in to AEM and exercise author shell and asset upload. They do not open the airline hero, accordion, or teaser.

Do not modify `Dockerfile`, `pom.xml`, or `assembly-ui-test-docker-context.xml`. The image is `cypress/included:13.14.1`. `run.sh` is the container entrypoint. If several Cypress processes start X11 servers at once, start one X11 server and pass its address with `DISPLAY`. Reports from the Docker run are written to `./target/reports`. Details of the reporters are in [test-module/README.md](test-module/README.md).

Environment variables (defaults from `test-module/cypress.config.js` and the UI test convention):

| envvar | default |
| --- | --- |
| `AEM_AUTHOR_URL` | `http://localhost:4502` |
| `AEM_AUTHOR_USERNAME` | `admin` |
| `AEM_AUTHOR_PASSWORD` | `admin` |
| `AEM_PUBLISH_URL` | `http://localhost:4503` |
| `AEM_PUBLISH_USERNAME` | `admin` |
| `AEM_PUBLISH_PASSWORD` | `admin` |
| `REPORTS_PATH` | `cypress/results` |

The `ui.tests` pom sets the author URL and the four credentials. It does not set `AEM_PUBLISH_URL`; the Cypress config supplies `http://localhost:4503` when the variable is absent. Profile `ui-tests-check` is on by default and runs `npm run lint` in `test-module` (Node `v18.14.2`). It does not run Cypress.

Docker image, from `ui.tests`:

```
mvn clean package -Pui-tests-docker-build
```

Run that image:

```
mvn verify -Pui-tests-docker-execution -DAEM_AUTHOR_URL=https://author.my-deployment.com -DAEM_AUTHOR_USERNAME=<PASS> -DAEM_AUTHOR_PASSWORD=<PASS>
```

Against a local author, pass `-DAEM_AUTHOR_URL=http://localhost:4502` and the publish URL the same way (`-DAEM_PUBLISH_URL=http://localhost:4503`).

From `ui.tests/test-module`, after `npm install`:

```
npm test
npm run test-chrome
npm run test-firefox
npx cypress run --headed --no-exit --browser chrome
```

`npm test` is `cypress run --browser electron`. JUnit output uses `cypress-multi-reporters` and `reporter.config.js`. Videos go to `$REPORTS_PATH/videos` and failure screenshots to `$REPORTS_PATH/screenshots`.

### it.tests

From the repository root, with author and publish running:

```
mvn clean verify -Plocal
```

The `local` profile is on `it.tests`. Failsafe runs `**/*IT.java` from `src/main/java`. The classes in the repo are `CreatePageIT` and `GetPageIT`. Defaults are author `http://localhost:4502` and publish `http://localhost:4503`, user `admin`. Override with `it.author.url`, `it.author.user`, `it.author.password`, `it.publish.url`, `it.publish.user`, and `it.publish.password`. Cloud Manager runs this module in the Custom Functional Testing step.

## Persisted query example

`SOLUTION_DESIGN.md` (section "Persisted query (HD-02)") names the query `airline/travel-questions` and gives this execute URL:

`http://localhost:4502/graphql/execute.json/airline/travel-questions`

The same section quotes a shorter query from the design doc authoring notes:

```
{ travelQuestionList { items { question answer { html } _path } } }
```

It also quotes a filtered persist body aimed at `/content/dam/airline/travel-questions`:

```
{ travelQuestionList(filter: { _path: { _expressions: [{ value: "/content/dam/airline/travel-questions", _operator: STARTS_WITH }] } }) { items { question answer { html } _path } } }
```

A persisted query resource is not in the repo. Dispatcher `default_filters.any` allows `GET`, `POST`, and `OPTIONS` on `/graphql/execute.json*`.

## Requirement trace

| ID | What is in this repository |
| --- | --- |
| HB-01 | Hero model, dialog, and React: `h1` title, introduction, DAM image (`loading="eager"`), jump links under "On this page". The dialog says the title is the only H1. No hero policy locks that. The sample home page does not contain a hero |
| HB-02 | Dialog text says a blank alt uses the DAM asset description. `HeroBannerImpl` uses the authored alt, then DAM `dc:title`, then the component title. Decorative returns an empty alt. `HeroBanner.js` does not read `decorative` and does not set `aria-hidden` from it |
| HB-03 | `HeroBanner` / `HeroBannerImpl`. HTL has no business logic. Empty state placeholder label in React is `Configure Hero Banner` |
| TS-01 | Teaser dialog has the five fields above. Heading level comes from the design dialog (`h2`, `h3`, `h4`, default `h2` in React). New-tab links get `rel="noopener noreferrer"` |
| TS-02 | Core Teaser v2 proxy plus `JoeyClubTeaserImpl` delegation |
| AC-01 | Custom accordion. Buttons sit inside headings and expose `aria-expanded` and `aria-controls`. Arrow, Home, and End keys are handled. Items are authored on the component. The six Content Fragment questions and the heading "Booking for infants (under 2 years)" are not in the repo |
| PT-01 | `spa-page-template` structure: header experience fragment, editable responsive grid, footer experience fragment. Fragments: `/content/experience-fragments/airline/us/en/site/header/master` (`airline/components/header`) and `.../footer/master` (`airline/components/footer`). Policy `spa-default` allows the group `Airline Travel Info - Content`, which is wider than hero, accordion, and teaser |
| PT-02 | Header `role="banner"`, nav, footer `role="contentinfo"`, skip link to `#main-content`. Gutters and `1280px` max width are the CSS tokens above. No element in this repo has `id="main-content"`. A container policy can set a `main` element; the SPA template uses `wcm/foundation/components/responsivegrid/spa-default`, which does not |
| HD-01 | A `travel-question` Content Fragment model and the six fragments are not in the repo |
| HD-02 | Example query is in `SOLUTION_DESIGN.md` only. See the previous section |
| SE-01 | FAQPage JSON-LD is not in the repo |
| SE-02 | The home page sets `jcr:title`. The page component does not add a canonical field. The design-doc meaning (one `mainEntity` per fragment) is not in the repo |
| DL-01 | `pushPageShow` pushes `cmp:show`. Hero jump links push `cmp:jumpLinkClick`. Accordion pushes `cmp:accordionToggle`. Teaser and accordion links push `cmp:linkClick` through `pushLinkClick`. The hero sets `data-cmp-data-layer` from `HeroBannerImpl.getDataLayer()` and pushes that JSON. `cmp:hide` and `cmp:click` are not pushed |
| DP-01 | `dispatcher/src` is the project Dispatcher config, including the GraphQL execute allow rule. `dispatcher/bin/validate.sh` is not in the repo. The HSTS line in `default_rewrite.rules` is commented out |
| PF-01 | The hero image is not lazy-loaded (`loading="eager"`). A performance measurement record is not in the repo |
| EN-01 | `archetype.properties` records the generation parameters listed under Project structure. A numeric archetype version is not in that file |
| EN-02 | `mvn clean install -PautoInstallSinglePackage` is the deploy command. The sample page grid is the text component, not the hero, accordion, teaser, or fragments |
| EN-03 | JUnit 5 and AEM Mocks cover `HeroBannerImpl`, `JoeyClubTeaserImpl`, `AccordionImpl`, and `FooterImpl`. Fragment selection and structured-data tests are not in the repo |
| EN-04 | The identifier row in `SOLUTION_DESIGN.md` is empty. Story S-13 maps EN-04 to UI and accessibility tests. The Cypress specs are the archetype samples |
| EN-05, EN-06 | Pull-request count and a clean browser console / `error.log` are not recorded in the repo |
| SD-01 to SD-14 | Design sections in `SOLUTION_DESIGN.md`. The built slice is the SPA Editor app, header and footer fragments, and the hero, teaser, and accordion models. SD-10 (New Zealand) and SD-11 (Joey Club enquiry to Salesforce) are design only |
| A-14 | Content root in the repo is `/content/airline/us/en` |

## Departures from the design

- Hero alt in code is authored alt, then DAM `dc:title`, then the component title. The dialog description still says a blank alt uses the DAM asset description (`dc:description` in HB-02).
- React does not set `aria-hidden` from the hero `decorative` flag. The model returns an empty alt when that flag is set.
- Hero React `emptyLabel` is `Configure Hero Banner`.
- Jump-link JCR names are `text` and `anchor`. The click payload uses `label` and `anchorId`.
- Teaser alt is DAM `dc:description`. The dialog has no alt field.
- Accordion is custom, with no Core super type. Items are page content. Buttons are inside headings. Arrow Up, Arrow Down, Home, and End are implemented.
- Data layer events that are pushed: `cmp:show`, `cmp:jumpLinkClick`, `cmp:accordionToggle`, `cmp:linkClick`, plus the hero `data-cmp-data-layer` JSON. `cmp:hide` and `cmp:click` from the Claude notes are not pushed.
- Header and footer are experience fragments on `spa-page-template` (PT-01), at the `us/en` paths above.
- The sample home page editable area is the archetype text component.
- Content fragments, FAQPage JSON-LD, and a persisted query resource are design text in `SOLUTION_DESIGN.md`. They are not in the repo.
- The SPA content policy allows the whole `Airline Travel Info - Content` group.
- The local content root is `/content/airline/us/en` (A-14 in `SOLUTION_DESIGN.md`).

## Assumptions

From `SOLUTION_DESIGN.md`:

| ID | Assumption |
| --- | --- |
| A-01 | Legal approval before publish is assumed and not built |
| A-02 | Fragments live in one DAM folder and publish separately from the page |
| A-03 | Tags already listens to the Adobe Client Data Layer |
| A-04 | The managed CDN is the only CDN |
| A-05 | Salesforce enquiry API is REST with OAuth 2.0 client credentials; limits unknown |
| A-06 | Enquiry personal data is never logged, sent to the data layer, or stored in the JCR |
| A-07 | The team can maintain React and TypeScript and wants one JSON contract |
| A-08 | Browser rendering only. Crawlers that skip JavaScript rely on shell JSON-LD |
| A-09 | The React bundle ships inside the AEM package as a client library |
| A-10 | English only. New Zealand differs by legal content, not by language |
| A-11 | Evergreen browsers, current and previous major. Mobile is primary |
| A-12 | No traffic figures. Targets assume a cached, read-heavy page |
| A-13 | AI tools may see code and synthetic content, not personal data or credentials |
| A-14 | Design roots are `/content/airline/au/en` and `/content/airline/nz/en`. The local build uses `/content/airline/us/en` |

Project assumptions stated in `AGENTS.md` and the root `pom.xml`: this is AEM as a Cloud Service, local checks use an AEM SDK Quickstart, and the author listens on `http://localhost:4502`.

## Next steps

- The sample page in `ui.content` still holds `airline/components/text` ("Hello World!"). Hero, accordion, and teaser are not on `/content/airline/us/en/home`.
- The persisted query from HD-02 is not in the repo.
- The `travel-question` model, the six fragments, and FAQPage JSON-LD are not in the repo.
- Cypress specs are not wired to the header, footer, hero, teaser, or accordion.
- The hero dialog describes the title as the only H1. Nothing in the hero policy enforces a single `h1`.
- The skip link targets `#main-content`, and no component in this repo renders that id.
- `dispatcher/bin/validate.sh` is not in the repo, so the validation command in `AGENTS.md` cannot be run from this checkout.
