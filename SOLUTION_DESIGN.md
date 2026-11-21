# Solution design: infants and children travel information

Audience: an Anchora architect and a client technical lead. Client: a fictional airline. Platform: AEM as a Cloud Service (AEMaaCS) with a React single-page application authored in the SPA Editor.

## Sources

All four sources were read.

| Source | File | What it contributes |
| --- | --- | --- |
| Brief | `Anchora_SFSE_AEM_Technical_Exercise_Candidate_Brief.docx` | Purpose, assumed current state, SD-01 to SD-14, and the Part B requirement identifiers |
| Design doc | `doc.docx` (Airline AEM SPA — Remaining Deliverables) | Build-oriented accordion, fragment, GraphQL, JSON-LD, and authoring notes; ADR-01 to ADR-03 |
| Drawio | `Airline_Travel_Info_SD03.drawio` | One diagram page, `SD-03 Architecture`, with INT-01 to INT-07 |
| Claude notes | `Claude- Solution design.docx` | Full design: A-01 to A-14, INT-01 to INT-14, DR-01 to DR-05, R-01 to R-12, S-01 to S-18 |

Identifiers below are taken from those files. None were added.

The draw.io file has one page. Its diagram id is `airline-sd03`. That string is the file's internal diagram id, not a requirement identifier. Shape ids (`authors`, `e1`, and so on) are likewise not requirement identifiers.

## Purpose and scope (SD-01)

### Problem

The airline is rebuilding the "Flying with infants and children" travel information page. The public reference for structure, layout, and behaviour is `https://www.qantas.com/en-au/travel-information/flying-infants-children`. The exercise is not pixel-perfect. Qantas logos, photography, illustrations, and brand fonts stay out; images are placeholders. Headings may follow the brief. Panel copy may be summarised.

The page has no reusable components today. A central team of six authors maintains travel information. Legal reviews any change to the infant travel rules. The page promotes Joey Club membership. Australia is the market now. A New Zealand version is planned for the following quarter. Search engines and AI crawlers must be able to read the page.

This design delivers the page as a React SPA on AEMaaCS, authored in the AEM SPA Editor. AEM owns the content, the content rules, and the JSON contract. React owns rendering and interaction. The page reaches React as the AEM page model (`.model.json`). The same travel questions are exposed to non-AEM consumers through a persisted GraphQL query. A thin HTL shell carries page metadata and FAQPage JSON-LD generated on the server from the same Content Fragments as the accordion.

Part A is this design, including integrations that are designed and not built. Part B is the local build: hero banner, Joey Club teaser, accordion, structured data, data layer events, Dispatcher configuration, and tests. The design is meant to drive the build. Where a build choice in the design doc departs from this design, both are recorded.

### Assumed current state

From the brief. Every candidate starts here. Impact in SD-04 is measured against this baseline.

| Area | Assumption |
| --- | --- |
| Platform | AEMaaCS for AEM Sites and AEM Assets, with Adobe's managed CDN in front of publish |
| Existing site | A global header and footer exist as experience fragments. The infants and children page is being rebuilt. No reusable components exist for it |
| Analytics | Adobe Analytics is live, using the Adobe Client Data Layer through Adobe Experience Platform Tags |
| CRM | Salesforce holds Joey Club membership. A REST API exists for a membership enquiry. It is not connected to AEM |
| Markets | The page serves Australia now. A New Zealand version is planned for the following quarter |
| Authors | Six authors in a central content team. Legal reviews any change to the infant travel rules |

The remaining-deliverables README draft describes the project as "AEM 6.5 + SPA Editor". The brief, the Claude design, and this repository (AEM as a Cloud Service, Java 21, Cloud Manager full-stack pipelines) describe AEMaaCS. This design follows the brief.

### In and out of scope

The Claude design marks several Part B items as built. The remaining-deliverables document marks the accordion, GraphQL, JSON-LD,(has not implemented cause of the accessing CF) and unit tests and marks the template, hero, teaser, and a partial data layer as done. Both status claims are kept.

| Area                    | Item                                                                                                                     | Status in the sources                                                                |
| ----------------------- | ------------------------------------------------------------------------------------------------------------------------ | ------------------------------------------------------------------------------------ |
| In scope                | SPA project, templates, policies, header and footer placeholders                                                         | built. Design doc: PT-01 and PT-02 done                                              |
| In scope                | Hero banner, Joey Club teaser, accordion: Sling Models with JSON exporters, mapped to React                              | built. Design doc: hero and teaser done; accordion not started                       |
| In scope                | Content Fragment model, six travel-question fragments, persisted GraphQL query                                           | Has not implemented cause of access issue, but adding those points how to implement. |
| In scope                | Server-generated FAQPage JSON-LD, page metadata, data layer events                                                       | Design doc: JSON-LD not started; data layer partial                                  |
| In scope                | Dispatcher configuration for the page, its model JSON, and the persisted query; unit, React, and UI tests; documentation | built. Design doc: EN-03 and EN-06 not started; docs not started                     |
| In scope, designed only | Joey Club enquiry to Salesforce through AEM (SD-11)                                                                      | Designed only                                                                        |
| In scope, designed only | New Zealand rollout (SD-10)                                                                                              | Designed only                                                                        |
| In scope, designed only | Cloud Manager pipeline detail, CDN caching and invalidation rules                                                        | Designed only                                                                        |
| Out of scope            | Server-side rendering of the React app (A-08), a decoupled front-end pipeline, Next.js, remote SPA                       | Not covered                                                                          |
| Out of scope            | A real Salesforce connection and member login                                                                            | Not covered                                                                          |
| Out of scope            | Global header and footer content, brand design, photography                                                              | Placeholders only                                                                    |
| Out of scope            | Content migration, translation, production deployment                                                                    | Not covered                                                                          |
| Out of scope            | Adobe Analytics report suite and Tags rule configuration                                                                 | Owned by the analytics team                                                          |

|     |
| --- |

## Identifier index

Every identifier found in the four sources is in the tables below. The source column lists where it appears: brief, design doc, drawio, claude notes.

### Identifier conflicts

Same identifier, two meanings. Both are kept.

| ID     | Meaning A                                                                                                         | Meaning B                                                                                                                                              |
| ------ | ----------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------ |
| INT-03 | Drawio: one id on three edges, Visitor Browser to CDN to Dispatcher to AEM Publish, labelled "HTTPS page + model" | Visitor browser to CDN, page shell and React bundle only                                                                                               |
| INT-04 | Drawio: Visitor Browser to Adobe Analytics, "Data Layer events" (dashed)                                          | CDN to Dispatcher, origin pull                                                                                                                         |
| INT-05 | Drawio: AEM Sling Servlet to Salesforce, "HTTPS REST / OAuth"                                                     | Dispatcher to AEM Publish                                                                                                                              |
| INT-06 | Drawio: AEM Publish to Search Engines and AI Crawlers, "Public HTML + JSON-LD" (dashed)                           | Visitor browser to CDN, `page.model.json`                                                                                                              |
| INT-07 | Drawio: Cloud Manager to AEM Publish, "CI/CD" (dashed)                                                            | Non-AEM consumer to the persisted GraphQL query                                                                                                        |
| SE-02  | Brief notes: page metadata. Title, meta description, and canonical URL, authorable through page properties        | Design doc: "One mainEntity per CF item" inside FAQPage JSON-LD. The same document's status line also groups SE-01 and SE-02 together as "FAQ JSON-LD" |

INT-01 and INT-02 agree in direction across the drawio and the notes (authoring into AEM Author; activate from Author to Publish). INT-08 to INT-14 appear only in the notes. The drawio legend says INT-01 to INT-07 are reused in the SD-06 register. That reuse collides with the register from INT-03 onward. The SD-06 section below prints both registers.

Related mismatches that are not a second meaning of the same identifier:

| Topic | What the sources say |
| --- | --- |
| ADR versus DR | ADR-01 to ADR-03 are only in the design doc. DR-01 to DR-05 are only in the Claude notes. ADR-01 is the hero's decision to skip a Core Component super type. DR-01 is the HTL versus React decision. They are different records |
| AC-01 and HD-01 comments | The brief gives AC-01 the accordion behaviour (section heading, six items, `aria-expanded`, `aria-controls`, Tab, Enter, Space) and gives HD-01 the Content Fragment model, the six fragments, and selection by folder or tag. The design doc's Java comments assign CF sourcing to AC-01, the section heading to HD-01, and the persisted query to HD-02. The design doc's own step title still groups AC-01, HD-01, and HD-02 as "Accordion from Content Fragments" |
| DL-01 event names | Claude notes: `cmp:show` on expand, `cmp:hide` on collapse, `cmp:click` on the teaser link. Design doc React sample: `cmp:accordionToggle` with id, question, and expanded. The brief says the event names match the design and carry the component id and title |
| HB-02 metadata property | Brief, Claude notes, the design doc README row, and the hero dialog description in this repository: blank alt uses DAM `dc:description`. A unit-test comment in the same design doc says the fallback returns `dc:title` |
| Resource type names | Claude notes: `airline/components/hero` and `airline/components/travelaccordion`. Design doc and this repository: `airline/components/herobanner` and `airline/components/accordion` |
| Fragment folder and tags | Claude notes: `/content/dam/airline/travel-info`, tags under `airline:travel-info` and `airline:market`, plus a `displayOrder` field. Design doc: `/content/dam/airline/travel-questions`, tag `airline:travel/infants`, fields `question`, `answer`, `tags` |
| Client library name | Claude notes call the bundle `ui.frontend.react`. This repository's category, in `ui.frontend/clientlib.config.js`, is `airline.react` |

### Part A design identifiers (brief and Claude notes)

| ID    | One-line description                                                                          | Source                                                                                                                                                       |
| ----- | --------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| SD-01 | Problem, in and out of scope, and assumptions beyond the current state                        | brief notes                                                                                                                                                  |
| SD-02 | Context and container architecture. Integrations numbered. Actors named in the brief          | brief notes                                                                                                                                                  |
| SD-03 | Diagram source a reviewer can change and re-render                                            | brief, drawio (page name [SD-03 Architecture](https://app.diagrams.net/#G1l3EfEQKzoblirFJ9DVheNdhZ2k1x1JhT#%7B%22pageId%22%3A%22gM9QZN0FGNaMou5Q7kvT%22%7D)) |
| SD-04 | Every element and integration marked existing, new, or changed, with a legend                 | brief notes                                                                                                                                                  |
| SD-05 | Component register: purpose, resource type, Core or custom, data source, caching              | brief notes                                                                                                                                                  |
| SD-06 | Integration register: source, target, protocol, direction, auth, frequency, errors, status    | brief, drawio (legend points INT-01 to INT-07 at this register)                                                                                              |
| SD-07 | Template, policies, Content Fragment model, tags, DAM metadata                                | brief notes                                                                                                                                                  |
| SD-08 | Numeric targets for LCP, CLS, INP, weight, WCAG 2.1 AA, cache, and security headers           | brief notes                                                                                                                                                  |
| SD-09 | Decision records. Two set topics: HTL versus React, and page content versus Content Fragments | brief notes                                                                                                                                                  |
| SD-10 | How New Zealand is supported, and what it would cost to change later                          | brief notes                                                                                                                                                  |
| SD-11 | Joey Club enquiry to Salesforce through AEM. Designed only                                    | brief notes                                                                                                                                                  |
| SD-12 | Risk and open-question register with owner and next step                                      | brief notes                                                                                                                                                  |
| SD-13 | Stories, estimates in days, sequence, dependencies, and what a mid-level engineer can take    | brief notes                                                                                                                                                  |
| SD-14 | How a team of five uses AI inside the client's terms, and what is not delegated               | brief notes                                                                                                                                                  |

### Part B requirement identifiers

| ID    | One-line description                                                                                                                                                       | Source                  |
| ----- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ----------------------- |
| HB-01 | Hero: title (only H1), introduction, DAM image, alt text, "On this page" jump links                                                                                        | brief, design doc notes |
| HB-02 | Blank alt uses the DAM asset description. The author can mark the image decorative                                                                                         | brief, design doc notes |
| HB-03 | Sling Model behind an interface. No business logic in HTL. Empty state shows an editor placeholder                                                                         | brief, design doc notes |
| TS-01 | Teaser: title, rich text, image, link label, link target. Heading level from policy. New-tab links use `rel="noopener noreferrer"`                                         | brief, design doc notes |
| TS-02 | Core Teaser proxy with delegation, or a custom build, justified in a decision record                                                                                       | brief, design doc notes |
| AC-01 | Accordion. Section heading "Booking for infants (under 2 years)". Six items. Header buttons expose `aria-expanded` and `aria-controls` and work with Tab, Enter, and Space | brief, design doc notes |
| PT-01 | Editable template. Header and footer locked. Only the components this page needs. Header and footer are placeholder experience fragments                                   | brief, design doc notes |
| PT-02 | Header, nav, main, and footer landmarks, a skip link, and a layout that reads at 375 and 1280 pixels                                                                       | brief, design doc notes |
| HD-01 | Content Fragment model for travel questions (question, answer, tags). Six fragments. Accordion selects them by folder or tag                                               | brief, design doc notes |
| HD-02 | A persisted GraphQL query returns the same items to a non-AEM consumer, with an example request and response                                                               | brief, design doc notes |
| SE-01 | FAQPage JSON-LD, generated on the server from the same fragments. Passes the Schema.org validator                                                                          | brief, design doc notes |
| SE-02 | See the conflict table. Brief and Claude notes: title, meta description, canonical URL. Design doc: one `mainEntity` per fragment                                          | brief, design doc notes |
| DL-01 | Accordion expand or collapse, and the teaser link click, push Adobe Client Data Layer events with component id and title. Names match the design                           | brief, design doc notes |
| DP-01 | Dispatcher filters allow only the selectors, extensions, and endpoints the page needs, including the persisted query. Cache rules match SD-08. SDK validator passes        | brief notes             |
| PF-01 | Measured against the SD-08 targets in preview on a mobile profile. Hero image is not lazy-loaded. Other images are. Misses are recorded                                    | brief notes             |
| EN-01 | Project generated from the AEM Project Archetype for AEMaaCS. Version and parameters recorded                                                                              | brief notes             |
| EN-02 | `mvn clean install -PautoInstallSinglePackage` deploys to a clean local author, including the sample page and fragments                                                    | brief notes             |
| EN-03 | JUnit 5 and AEM Mocks for every Sling Model, fragment selection, and structured data. The build fails when a test fails                                                    | brief, design doc notes |
| EN-04 |                                                                                                                                                                            | brief notes             |
| EN-05 | Work merged through at least three small pull requests                                                                                                                     | brief notes             |
| EN-06 | Loading the page produces no browser console errors and no `error.log` entries from this code                                                                              | brief, design doc notes |
| AI-01 | Context or rules files, reusable prompts, skills, or agent definitions are committed                                                                                       | brief notes             |
| AI-02 | One artefact for a mid-level engineer, with a short note on how to use it                                                                                                  | brief notes             |
| AI-03 | `AI_NOTES.md`, one page at most, covering setup, design and build help, one mistake, and one task not handed over                                                          | brief notes             |
| AI-04 | The author of the submission can explain every line and every decision                                                                                                     | brief notes             |

| ID | One-line description | Source |
| --- | --- | --- |
| A-01 | Legal approval before publish is assumed and not built | claude notes |
| A-02 | Fragments live in one DAM folder and publish separately from the page | claude notes |
| A-03 | Tags already listens to the Adobe Client Data Layer | claude notes |
| A-04 | The managed CDN is the only CDN | claude notes |
| A-05 | Salesforce enquiry API is REST with OAuth 2.0 client credentials; limits unknown | claude notes |
| A-06 | Enquiry personal data is never logged, sent to the data layer, or stored in the JCR | claude notes |
| A-07 | The team can maintain React and TypeScript and wants one JSON contract | claude notes |
| A-08 | Browser rendering only. Crawlers that skip JavaScript rely on shell JSON-LD | claude notes |
| A-09 | The React bundle ships inside the AEM package as a client library | claude notes |
| A-10 | English only. New Zealand differs by legal content, not by language | claude notes |
| A-11 | Evergreen browsers, current and previous major. Mobile is primary | claude notes |
| A-12 | No traffic figures. Targets assume a cached, read-heavy page | claude notes |
| A-13 | AI tools may see code and synthetic content, not personal data or credentials | claude notes |
| A-14 | Design roots are `/content/airline/au/en` and `/content/airline/nz/en`. Local build uses `/content/airline/us/en` | claude notes |
| INT-01 | Authors (and, in the Claude notes, Legal) to AEM Author. Both sources: authoring. Existing | claude notes, drawio |
| INT-02 | AEM Author to AEM Publish. Both sources: activate or publish. Existing | claude notes, drawio |
| INT-03 | Conflict. Drawio: browser, CDN, Dispatcher, and Publish as one page-and-model path. Claude notes: page shell and bundle only | claude notes, drawio |
| INT-04 | Conflict. Drawio: data layer events to Adobe Analytics. Claude notes: CDN to Dispatcher | claude notes, drawio |
| INT-05 | Conflict. Drawio: servlet to Salesforce over HTTPS REST with OAuth. Claude notes: Dispatcher to Publish | claude notes, drawio |
| INT-06 | Conflict. Drawio: public HTML and JSON-LD to crawlers. Claude notes: `page.model.json` | claude notes, drawio |
| INT-07 | Conflict. Drawio: Cloud Manager CI/CD to Publish. Claude notes: persisted query for a non-AEM consumer | claude notes, drawio |
| INT-08 | Search engines and AI crawlers fetch the public shell from the CDN | claude notes |
| INT-09 | React pushes Adobe Client Data Layer events to Tags | claude notes |
| INT-10 | Tags sends the analytics beacon to Adobe Analytics | claude notes |
| INT-11 | Developers trigger Cloud Manager with a Git push | claude notes |
| INT-12 | Cloud Manager deploys code and Dispatcher config to AEMaaCS | claude notes |
| INT-13 | Visitor posts the Joey Club enquiry to the servlet on Publish. Designed only | claude notes |
| INT-14 | The servlet calls Salesforce with OAuth 2.0 client credentials. Designed only | claude notes |
| DR-01 | Decision: React SPA in the AEM SPA Editor, with an HTL shell for metadata and JSON-LD | claude notes |
| DR-02 | Decision: accordion items are Content Fragments, not child page components | claude notes |
| DR-03 | Decision: Joey Club teaser is a Core Teaser proxy plus a delegating Sling Model (TS-02) | claude notes |
| DR-04 | Decision: 300-second TTL, and authors republish the page when a fragment changes | claude notes |
| DR-05 | Decision: a servlet on Publish calls Salesforce. Designed only. A queue is rejected because it would store personal data | claude notes |
| ADR-01 | Hero does not set `sling:resourceSuperType` to a Core Component | design doc |
| ADR-02 | Accordion reads fragments by JCR traversal. The persisted query stays available for other consumers | design doc |
| ADR-03 | FAQ JSON-LD is rendered by HTL so it is in the first HTML response | design doc |
| R-01 | Crawlers that do not run JavaScript may miss client-rendered accordion content | claude notes |
| R-02 | Client rendering may miss the LCP target on a slow mobile profile | claude notes |
| R-03 | A five-minute staleness window may be unacceptable to Legal | claude notes |
| R-04 | SPA Editor support on AEMaaCS may change | claude notes |
| R-05 | Salesforce limits, credential model, and IP allowlisting are unknown | claude notes |
| R-06 | Personal-data handling and consent wording are not signed off | claude notes |
| R-07 | FAQ rich results are not guaranteed by search engines | claude notes |
| R-08 | The CSP may block Tags, or Tags rules may miss the new event names | claude notes |
| R-09 | New Zealand legal content may differ by more than the tag filter | claude notes |
| R-10 | Authors may find the SPA Editor harder than HTL editing | claude notes |
| R-11 | Traffic figures are unknown, so cache sizing is ungrounded | claude notes |
| R-12 | A Core Components upgrade may change the Teaser model the proxy delegates to | claude notes |
| S-01 | Archetype scaffold, CI, README skeleton, AI context files. 1.0 day. EN-01, AI-01 | claude notes |
| S-02 | SPA template, policies, header and footer placeholders, landmarks, skip link. 1.5 days. PT-01, PT-02 | claude notes |
| S-03 | Fragment model, six fragments, tags, DAM metadata schema. 1.0 day. HD-01 | claude notes |
| S-04 | Fragment selection service and unit tests. 1.5 days. HD-01, EN-03 | claude notes |
| S-05 | Hero model, dialog, React component, tests. 2.0 days. HB-01, HB-02, HB-03 | claude notes |
| S-06 | Accordion model, dialog, accessible React component, tests. 2.5 days. AC-01, HD-01 | claude notes |
| S-07 | Teaser proxy, delegation, policy, React component, tests. 1.5 days. TS-01, TS-02 | claude notes |
| S-08 | Persisted query and example request and response. 0.5 day. HD-02 | claude notes |
| S-09 | FAQPage JSON-LD model, shell, validation. 1.0 day. SE-01 | claude notes |
| S-10 | Page metadata and canonical URL. 0.5 day. SE-02 in the brief and Claude sense | claude notes |
| S-11 | Data layer events from React. 1.0 day. DL-01 | claude notes |
| S-12 | Dispatcher filters, cache rules, security headers, validator. 2.0 days. DP-01 | claude notes |
| S-13 | UI and accessibility tests. 1.5 days. EN-04 | claude notes |
| S-14 | Performance measurement and tuning. 1.0 day. PF-01 | claude notes |
| S-15 | README, authoring guide, handover. 1.0 day | claude notes |
| S-16 | Cloud Manager pipeline gates. 1.0 day. INT-11, INT-12 | claude notes |
| S-17 | Joey Club form, servlet, mock Salesforce, tests. 4.0 days. SD-11. Designed scope, not the exercise build | claude notes |
| S-18 | New Zealand live copy and market configuration. 2.0 days. SD-10. Designed scope | claude notes |

## Architecture (SD-02, SD-03, SD-04)

### How the page is served

Authors work in the SPA Editor on AEM Author. Each component is a Sling Model that exports JSON and is mapped to a React component with `MapTo`. The resource type string is the same in the Java model, the component definition, and the React mapping.

AEM Publish serves three things through Dispatcher and the managed CDN:

1. A thin HTL page shell: metadata, canonical URL, FAQPage JSON-LD, and the client library include. The shell has no business logic.
2. The React bundle.
3. The page model, `.model.json`, produced by the Sling Model exporters.

The browser runs the React SPA. Interaction events go to the Adobe Client Data Layer. Tags forwards them to Adobe Analytics. Search and AI crawlers that do not run JavaScript get headings and FAQPage JSON-LD from the shell (A-08, INT-08 in the Claude numbering, INT-06 on the drawio).

This repository's names, where the sources or the repo use them:

| Role                          | Name in this repository                                                                                                                            |
| ----------------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------- |
| Content project               | `airline` under `/conf/airline` and `/content/airline`                                                                                             |
| SPA app root                  | `/content/airline/us/en`, template `spa-page-template`, resource type `airline/components/spa` (extends `airline/components/page`)                 |
| Page                          | Template `spa-page-template` ("SPA Page"), resource type `airline/components/page` (extends `spa-project-core/components/page`)                    |
| Layout in the template        | `wcm/foundation/components/responsivegrid`. `airline/components/container` exists and is used inside the header experience fragment                |
| Header and footer on the page | `airline/components/experiencefragment`, variations under `/content/experience-fragments/airline/us/en/site/header/master` and `.../footer/master` |
| Experience fragment page      | `airline/components/xfpage`                                                                                                                        |
| React client library          | Category `airline.react` (`ui.frontend/clientlib.config.js`). Base category `airline.base` on `clientlib-base`                                     |
| Front end                     | `ui.frontend`, React, `@adobe/aem-react-editable-components`                                                                                       |

`airline/components/spa` in the Claude register matches the repository. `airline/components/page` matches. The Claude register's layout container `airline/components/container` matches a component in `ui.apps`, and the SPA page template structure uses the foundation responsive grid for the editable band.

### Current implementation: SPA editor and template structure

Tied to the SPA Editor page-model contract and to template structure mode. These are facts in this repository, not extra requirements from the brief.

- `airline/components/page/customheaderlibs.html` sets `cq:pagemodel_root_url` with `data-sly-attribute.content="${page.hierarchyRootJsonExportUrl || '{0}.model.json' @ format=request.requestPathInfo.resourcePath, context='uri'}"`. On a `/content` page the hierarchy URL is used. On a `/conf` template page the hierarchy URL is null, and the tag still emits the current resource path plus `.model.json`, so structure mode can request a model.
- The same page file loads CSS categories `airline.base` and `airline.react`.
- `airline/components/page/customfooterlibs.html` loads JavaScript categories `airline.base` and `airline.react` through `/libs/granite/sightly/templates/clientlib.html`, loads `cq.authoring.pagemodel.messaging` with its own `data-sly-use` when the mode is edit or preview, and calls `_satellite.pageBottom()` only when `window._satellite` exists.
- `airline/components/xfpage/customheaderlibs.html` also emits `cq:pagemodel_root_url`. Its fallback formats `currentPage.path`, and its CSS categories are `airline.base` and `airline.react`.
- `airline/components/xfpage/customfooterlibs.html` loads `airline.base` and `airline.react`, the messaging client library in edit or preview, and the same `_satellite` guard.
- `ui.frontend/src/index.js` calls `ModelManager.initialize`.
- The SPA page policy in `ui.content` (`airline/components/page/policy_spa`) lists client library `airline.react`.

The Claude container view names the bundle `ui.frontend.react`. The category this repository builds is `airline.react`.

### Drawio page `SD-03 Architecture`

One page. Title on the canvas: "AIRLINE TRAVEL INFO — C4 CONTAINER / INTEGRATION VIEW". The legend on the canvas colours systems by kind (blue AEM and application, green content stores, purple delivery, orange external or operational). Dashed lines are logical or designed interactions. That legend is a system-type legend. It does not mark existing, changed, or new. Impact coding for SD-04 is in the Claude notes, in the table later in this section.

| Shape | Label |
| --- | --- |
| Content Authors | Six authors |
| Visitor Browser | React SPA |
| Search Engines & AI Crawlers | Public page discovery |
| AEM Author | SPA Editor, page and template, dialogs |
| AEM Assets (DAM) | Images, alt text, metadata |
| Content Fragments | TravelQuestion model, six FAQ items |
| CDN | Edge cache |
| Dispatcher | Filter, cache |
| AEM Publish | HTL shell, `.model.json`, GraphQL |
| Sling Models / JSON Exporters | FAQ selection, component data, JSON-LD |
| Adobe Analytics | Data Layer events (no PII) |
| AEM Sling Servlet + Java Service | Server-side validation, OAuth |
| Salesforce | Lead / membership enquiry |
| Cloud Manager | Build, test, deploy |

| Edge label | From | To | Line |
| --- | --- | --- | --- |
| INT-01 · Authoring | Content Authors | AEM Author | Solid |
| INT-02 · Activate / publish | AEM Author | AEM Publish | Solid |
| INT-03 · HTTPS page + model | Visitor Browser | CDN | Solid |
| INT-03 | CDN | Dispatcher | Solid |
| INT-03 | Dispatcher | AEM Publish | Solid |
| Page Model / component export | AEM Publish | Sling Models / JSON Exporters | Solid. No INT id |
| DAM authoring | AEM Assets | AEM Author | Dashed. No INT id |
| FAQ fragments | Content Fragments | Sling Models / JSON Exporters | Solid. No INT id |
| INT-04 · Data Layer events | Visitor Browser | Adobe Analytics | Dashed |
| POST enquiry | Visitor Browser | AEM Sling Servlet | Solid. No INT id. Claude notes number this INT-13 |
| INT-05 · HTTPS REST / OAuth | AEM Sling Servlet | Salesforce | Solid |
| INT-06 · Public HTML + JSON-LD | AEM Publish | Search Engines & AI Crawlers | Dashed |
| INT-07 · CI/CD | Cloud Manager | AEM Publish | Dashed |

The drawio does not draw Legal reviewers, a non-AEM consumer, Tags as a separate box, or Developers. Those actors are in the Claude context view and in the brief's SD-02 list (content authors, AEM author, AEM publish, Dispatcher, the CDN, AEM Assets, Content Fragments, Salesforce, Adobe Analytics, Cloud Manager, and search engines and AI crawlers).

### Impact against the assumed current state (SD-04)

Colour in the Claude diagrams: grey existing, amber changed, green new and built, white with a dashed border for new and designed only. Line style there: thin existing or changed, thick new, dotted designed only.

| Element | Impact | What changes |
| --- | --- | --- |
| Content authors, Legal reviewers | Existing | No change. A Legal approval step is assumed (A-01) |
| AEM Author | Changed | New template, policies, components, and fragment model |
| AEM Assets | Changed | New DAM folder. Description metadata becomes mandatory for images the hero uses |
| Content Fragments | New | `travel-question` model and six fragments |
| AEM Publish | Changed | New components, models, persisted query, shell, and the designed servlet |
| React SPA bundle | New | Hero, accordion, and teaser renderers, and the data layer hook |
| Page shell and JSON-LD | New | Metadata, canonical URL, FAQPage JSON-LD |
| Sling Models and exporters | New | Page model JSON for each component |
| GraphQL persisted query | New | `travel-questions` for non-AEM consumers |
| Header and footer experience fragments | Existing | Referenced and locked in the template. Content is not redesigned |
| Dispatcher | Changed | New filters, cache rules, and security headers |
| Adobe managed CDN | Changed | New cache lifetimes through response headers. No new CDN |
| Tags | Changed | Rules for the new event names. Owned by the analytics team |
| Adobe Analytics | Existing | No change |
| Salesforce | Existing | The API is not connected to AEM. The enquiry call is designed only |
| Cloud Manager | Existing | Pipeline gains Dispatcher validation. Designed only |
| Search engines and AI crawlers | Existing | They now read FAQPage JSON-LD |
| Non-AEM consumer | New | Example consumer of the persisted query |
| Joey Club enquiry servlet | New, designed only | SD-11 |

Integration impact is in the Claude register below. The drawio does not mark existing, new, or changed on each shape.

## Component register (SD-05)

Resource types in the  notes are listed first. Where this repository has a component for the same role, its resource type is in the last column. Caching numbers are the  targets (SD-08).

| Component | Purpose | Resource type in the Claude notes | Build | Data source | Caching | Resource type in this repository |
| --- | --- | --- | --- | --- | --- | --- |
| SPA app root page | Hosts the React app and loads the page model | `airline/components/spa` | Archetype page component | Page content | Shell: 5 min at the CDN | `airline/components/spa` |
| SPA page | Child page route with metadata and the layout container | `airline/components/page` | Archetype page component, extended for JSON-LD and metadata | Page properties, Content Fragments | Shell and model: 5 min | `airline/components/page` |
| Layout container | Holds the page components | `airline/components/container` | Core Container proxy | Page content | With the page model | Template band is `wcm/foundation/components/responsivegrid`. `airline/components/container` exists |
| Header and footer | Global navigation and footer, locked in the template | `airline/components/experiencefragment` | Core Experience Fragment proxy | Experience fragments | With the page model | `airline/components/experiencefragment` (Core v2 proxy) |
| Hero banner | H1 title, introduction, image, "On this page" links | `airline/components/hero` | Custom. ADR-01: no Core super type | Page content and DAM | Model: 5 min. Image rendition: 24 h at the CDN | `airline/components/herobanner`. No `sling:resourceSuperType`. Group `Airline Travel Info - Content` |
| Travel accordion | Section heading and the six question items | `airline/components/travelaccordion` | Custom, because items come from fragments (DR-02) | Content Fragments and tags | Model: 5 min | `airline/components/accordion` exists. Its dialog is page content, not the fragment dialog. See SD-07 |
| Joey Club teaser | Title, rich text, image, link | `airline/components/teaser` | Core Teaser proxy with Sling Model delegation (DR-03, TS-02) | Page content and DAM | Model: 5 min | `airline/components/teaser`, super type `core/wcm/components/teaser/v2/teaser`, `imageDelegate` `airline/components/image` |
| Fragment selection | Selects and orders fragments once for the accordion and the JSON-LD | OSGi service `TravelQuestionService` | Custom | Content Fragments | Per request, no state | Not present under that name in the sources' file list for this repo. The design doc reads fragments by JCR child traversal inside `AccordionModelImpl` (ADR-02) |
| FAQ JSON-LD | Builds FAQPage JSON-LD for the shell | Sling Model `FaqJsonLd` | Custom | Same fragment service | With the shell: 5 min | Design doc: HTL `faqschema.html` included from `accordion.html` (ADR-03). That HTL is specified in the design doc, not as a file confirmed here |
| Persisted query | Returns the questions to non-AEM consumers | `airline/travel-questions` | AEM GraphQL | Content Fragments | CDN 5 min, set on the query | Same query name in the design doc |
| Data layer hook | Pushes events from React | `useDataLayer` hook | Custom, on the Core Components data layer | Component model ids | Client side | Design doc sample pushes from the accordion component. This repository also has `ui.frontend/src/utils/adobeDataLayer.js` |
| Joey Club form and servlet | Posts an enquiry to Salesforce | `airline/components/joeyclubform`, servlet | Custom, designed only | Nothing stored | `no-store` | Designed only. Not a component in `ui.apps` |

The design doc's accordion definition uses super type `core/wcm/components/accordion/v1/accordion`, title "Airline Accordion (Content Fragments)", group "Airline - Content", resource type `airline/components/accordion`. The component of that name in this repository has no super type, title "Accordion", and group "Airline Travel Info - Content".

Inside the experience fragments, the repository uses `airline/components/header` (Core Navigation v1 proxy, group hidden) and `airline/components/footer` (group hidden). The brief treats header and footer content as existing placeholders. Those two resource types are implementation of the placeholder fragments, not a new global navigation design.

## Integration register (SD-06)

Read INT-03 to INT-07 in both columns. The drawio column is the edge label on page `SD-03 Architecture`. The Claude column is the register in that design.

| ID | Drawio | Claude notes |
| --- | --- | --- |
| INT-01 | Content Authors to AEM Author. Authoring | Authors and Legal to AEM Author. HTTPS, Sites, SPA Editor, Assets. Adobe sign-in. Existing, not rebuilt |
| INT-02 | AEM Author to AEM Publish. Activate / publish | Sling Content Distribution. On publish. Existing, not rebuilt |
| INT-03 | Browser to CDN to Dispatcher to Publish. HTTPS page + model. Same id on three edges | Browser to CDN. Page shell and bundle. HTTPS GET. Changed. Built |
| INT-04 | Browser to Adobe Analytics. Data layer events. Dashed | CDN to Dispatcher. Origin pull. Changed. Built as config |
| INT-05 | Servlet to Salesforce. HTTPS REST / OAuth | Dispatcher to AEM Publish. Changed. Built as config. 5xx responses are not cached |
| INT-06 | Publish to crawlers. Public HTML + JSON-LD. Dashed | Browser to CDN. `page.model.json`. New. Built. React shows an error state and retries. The shell stays readable |
| INT-07 | Cloud Manager to Publish. CI/CD. Dashed | Non-AEM consumer to the persisted query. HTTPS GET JSON. New. Built. Consumer retries with backoff on 5xx |
| INT-08 | Not on the diagram | Crawlers to CDN. HTTPS GET. Changed. Built. The shell carries JSON-LD and headings without JavaScript |
| INT-09 | Not on the diagram. The drawio data-layer edge is INT-04 | React to the Adobe Client Data Layer to Tags. `adobeDataLayer.push`. Changed. Built. A failed push never breaks the page |
| INT-10 | Not on the diagram | Tags to Adobe Analytics. HTTPS beacon. Existing, not rebuilt |
| INT-11 | Not on the diagram | Developers to Cloud Manager. Git push. Existing, not rebuilt. Pipeline fails on tests, code quality, or Dispatcher validation |
| INT-12 | The drawio CI/CD edge is INT-07, and it ends at Publish only | Cloud Manager to AEMaaCS (author, publish, and Dispatcher). Changed. Designed only. Rolling deployment with rollback |
| INT-13 | The drawio labels this edge "POST enquiry" and gives it no INT id | Browser to the Joey Club servlet on Publish, through CDN and Dispatcher. HTTPS POST JSON. CSRF token. 422 for field errors, 503 when the caller should retry. New. Designed only |
| INT-14 | The drawio numbers the Salesforce hop INT-05 | Servlet to Salesforce. REST over HTTPS. OAuth 2.0 client credentials (A-05). 2 s connect, 5 s read, 2 retries, idempotency key, circuit breaker. New. Designed only |

Claude error handling for the other rows, in short: INT-01 uses AEM versions and session timeout. INT-02 uses managed distribution retries. INT-03 relies on the page cache to mask a short origin outage. INT-04 can serve a cached file if Publish errors.

## Content model (SD-07)

### Template and policies

Template name in the Claude notes and in this repository: `spa-page-template`.

| Item | Claude design | This repository |
| --- | --- | --- |
| Structure | Header experience fragment, locked. Layout container, editable. Footer experience fragment, locked | `spa-page-template/structure`: `experiencefragment-header`, then `responsivegrid` with `editable=true`, then `experiencefragment-footer`. The two fragments do not set `editable` |
| Initial | Not drawn separately in the Claude notes | `spa-page-template/initial` contains the root responsive grid and an empty child responsive grid. Header and footer are not repeated in initial content |
| Container policy | Allows only the hero, the travel accordion, and the Joey Club teaser | Policy `wcm/foundation/components/responsivegrid/spa-default` allows the component group `Airline Travel Info - Content` |
| Hero policy | Title is the only H1. The level is not editable | Hero dialog field description says the title is the only H1. There is no separate policy file that locks the level |
| Teaser policy | Heading level H2 or H3, default H2. Link target options allowed. New-tab links get `rel="noopener noreferrer"` | Design dialog `headingElement` allows h2, h3, and h4. The component dialog hides inherited properties and does not show a target field |
| Accordion policy | Fragment root folder restricted to `/content/dam/airline/travel-info`. Item buttons render inside H3 elements | No such policy in the template files read for this guide |
| Page policy | Adobe Client Data Layer enabled | `airline/components/page/policy_spa` sets clientlibs to `airline.react` |
| Page properties | Title, meta description, canonical URL (SE-02 in the brief sense). Canonical defaults to the market URL of the page | Sample home page sets `jcr:title`. The page component definition does not add a canonical field |

Header fragment path: `/content/experience-fragments/airline/us/en/site/header/master`. Footer: `/content/experience-fragments/airline/us/en/site/footer/master`. Both use template `xf-web-variation` and resource type `airline/components/xfpage`.

The sample page in `ui.content` is `/content/airline/us/en/home`, template `spa-page-template`, resource type `airline/components/page`, title "Airline Travel Info Home Page". Its grid currently holds `airline/components/text`, which is outside the three components the Claude container policy allows.

### Content Fragment model `travel-question`  **(Pending Item), but this are the instructions added both development and authoring**

Store the model at `/conf/airline/settings/dam/cfm/models/travel-question`. The design doc creates it in the airline folder from the Content Fragment model editor. The model name `travel-question` agrees. The fields do not.

| Field | Claude notes | Design doc |
| --- | --- | --- |
| `question` | Single line text, required. Accordion title and FAQ question | Single-line text |
| `answer` | Multi line rich text, required. Limited to a safe HTML subset. Plain text is derived for JSON-LD | Multi-line rich text |
| `displayOrder` | Number, required. Fixed order, added because folder and tag selection alone cannot order items | Not in that document |
| `tags` | Tags, optional. Selection and market filtering | Tags field |

The drawio labels the store "TravelQuestion model · six FAQ items". It does not list fields.

### Tags

| Claude notes | Design doc |
| --- | --- |
| `airline:travel-info/infants` for questions about infants under 2 | `airline:travel/infants` on each of the six fragments |
| `airline:travel-info/booking` for booking questions | Not listed |
| `airline:market/shared`, `airline:market/au`, `airline:market/nz` for the New Zealand rollout | Not listed |

Claude selection rule: the accordion selects by folder, or by tags set in the dialog (any match), then orders by `displayOrder`. The brief (HD-01) says the dialog selects by folder or tag. The design doc dialog has a folder path and no tag picker. Its Java reads every fragment child of that folder and does not filter on tags.

### Six fragments in the design doc

Folder in that document: `/content/dam/airline/travel-questions`. The Claude folder for the same role is `/content/dam/airline/travel-info`.

The brief says the six item titles match the reference page and does not list them. The design doc lists these six, and also uses "Booking for infants (under 2 years)" as the section heading (AC-01). In that document the same sentence is both the section heading and the first question.

| Slug | Question | Answer summary |
| --- | --- | --- |
| `booking-infants` | Booking for infants (under 2 years) | Infants under 2 travel on a parent's lap. Book by calling reservations |
| `infant-fees` | Are there fees for infants? | Infant fees vary by route. Domestic routes are free; international routes incur a tax |
| `lap-infant-safety` | Is it safe for my infant to travel on my lap? | Lap infants are secured with a loop belt attached to the adult seatbelt |
| `infant-meals` | Can I request infant meals? | Infant meals are available on flights over 2 hours. Request at least 24 hours in advance |
| `bassinets` | Are bassinets available on board? | Bassinets are available on wide-body aircraft. Request when booking; subject to availability |
| `children-fares` | What fares apply to children aged 2–11? | Children aged 2–11 must have their own seat and are charged a child fare |

### DAM metadata the page relies on

| Metadata | Use |
| --- | --- |
| `dc:description` | Alt text fallback when the author leaves alt blank (HB-02). The Claude notes make it mandatory for images in the page folder through the metadata schema |
| `dc:title` | Editor display in the Claude notes. A unit-test comment in the design doc treats it as the alt fallback. See the HB-02 conflict |
| Image width and height | Read by the model so React reserves space and limits layout shift (Claude notes, SD-08) |

## Data and model contract

### Page model

The SPA Editor reads `cq:pagemodel_root_url` and requests that URL. Publish exposes the page as `.model.json` (Claude INT-06). Sling Models implement `ComponentExporter` and the Sling Model exporter. React `MapTo` uses the same resource type the model returns from `getExportedType()`.

The design doc's accordion exporter returns items with `question`, `answer` (HTML), `path` (fragment path, stable id), and `id` (sanitised, prefixed `acc-`). Empty when the folder is missing or has no questions. Default section heading "Booking for infants (under 2 years)". Default folder `/content/dam/airline/travel-questions`. Default heading level `h2`.

### Persisted query (HD-02)

Name in both design documents: `airline/travel-questions`.

Design doc endpoint: `http://localhost:4502/graphql/execute.json/airline/travel-questions`.

Design doc persist request (author instance):

```bash
curl -X PUT \
  "http://localhost:4502/graphql/persist/airline/travel-questions" \
  -H "Content-Type: application/json" \
  -u admin:admin \
  -d '{"query":"{ travelQuestionList(filter: { _path: { _expressions: [{ value: \"/content/dam/airline/travel-questions\", _operator: STARTS_WITH }] } }) { items { question answer { html } _path } } }"}'
```

The same document's authoring section shows a shorter query without the path filter: `{ travelQuestionList { items { question answer { html } _path } } }`. Both are from that source. The filtered form matches the folder that document authors into.

ADR-02: the Sling Model reads the JCR. It does not call this endpoint on each render. The endpoint is the contract for a non-AEM consumer (HD-02, Claude INT-07). The Java comment in the design doc also says the model falls back to the persisted endpoint if folder traversal fails.

### JSON-LD (SE-01, and SE-02 in the design-doc sense)

Generated on the server from the same fragments as the accordion (brief SE-01, ADR-03, DR-01). Type `FAQPage`. Each item is a `Question` with `name` from the question and `acceptedAnswer.text` from the answer. The design doc's second meaning of SE-02 is one `mainEntity` per fragment. The Claude model name for this output is `FaqJsonLd`, included in the page shell. The design doc includes `faqschema.html` from the accordion HTL and states that the React bundle does not emit the JSON-LD.

The brief's SE-02 (page metadata) is a different contract: `jcr:title` or page title, meta description, and canonical URL, rendered from page properties. The Claude notes default the canonical URL to the market URL of the page.

### Data layer (DL-01)

Payload always includes the component id and title. Personal data and enquiry field values stay out (A-06). The outcome of a designed-only enquiry may be pushed as success or failure, with no field values (SD-11).

| Source | Event | When | Payload |
| --- | --- | --- | --- |
| Claude notes | `cmp:show` | Accordion item expanded | Component id, item id, title (the question) |
| Claude notes | `cmp:hide` | Accordion item collapsed | Component id, item id, title |
| Claude notes | `cmp:click` | Teaser link clicked | Component id, title, link URL |
| Design doc | `cmp:accordionToggle` | Accordion header clicked | `eventInfo.path`, `id`, `question`, `expanded` |

A-03: the names used in production are agreed with the analytics team. R-08: Tags rules have to match whichever names are chosen.

### Hero and teaser fields the models export

Hero, from the brief and from the dialog in this repository: `title`, `introduction`, `fileReference`, `alt`, `decorative`, and a multifield of jump links with `text` and `anchor`. HB-03: with no title or image, the component shows an editor placeholder and renders nothing broken.

Teaser, from the brief and from the dialog in this repository: `title`, `description` (rich text), `fileReference`, `linkText`, `linkURL`. TS-02 is satisfied by DR-03: proxy of `core/wcm/components/teaser/v2/teaser` plus delegation. The design doc records the same delegation (`@Self` with `@Via(ResourceSuperType.class)`).

## Non-functional targets (SD-08)

Measured in preview on a mobile profile (Slow 4G, 4x CPU slowdown). Field data replaces lab data after launch. PF-01 records the result against these numbers. The hero image is not lazy-loaded. Other images are. The Claude notes preload the hero image from the shell.

| Target | Value | Reason |
| --- | --- | --- |
| LCP | 2.5 s or less | The "good" Core Web Vitals threshold. A client-rendered page also fetches the bundle and the model |
| CLS | 0.05 or less | Hero dimensions come from DAM metadata. Panels start collapsed |
| INP | 150 ms or less | Accordion toggling is a small state change. The threshold cited is 200 ms |
| Page weight | 500 KB or less on first load, excluding Tags | Mobile visitors |
| Hero image | 120 KB or less, WebP, responsive sizes | Largest element |
| JavaScript | 150 KB gzipped or less | React, the SPA SDK, and the app |
| WCAG 2.1 AA | No serious or critical axe-core issues. Keyboard operable. Contrast 4.5:1 | Checked in UI tests (EN-04) |

### Cache lifetimes and invalidation

| Content | Browser | CDN | Invalidation |
| --- | --- | --- | --- |
| Page shell, `.html` | 5 min | `s-maxage 300` | TTL. Republish flushes the Dispatcher copy of the page path |
| `page.model.json` | 5 min | `s-maxage 300` | Same lifetime as the shell so they stay aligned |
| Versioned client libraries | 1 year, immutable | 1 year | The URL changes with the content hash |
| DAM renditions | 1 hour | 24 h | TTL |
| Persisted query | 5 min | 5 min | TTL, set on the query |
| Joey Club POST | `no-store` | Not cached | Not applicable |

DR-04 is the reason for five minutes, and the limit that creates for fragment edits.

### Security headers

Set on Publish through Dispatcher. The CSP is not applied on Author, because the SPA Editor loads pages in a frame.

| Header | Value | Reason |
| --- | --- | --- |
| Content-Security-Policy | Scripts from self and the Tags host only. Inline scripts avoided. `frame-ancestors 'self'` | Limits script injection and clickjacking |
| Strict-Transport-Security | 1 year, include subdomains | Forces HTTPS |
| X-Content-Type-Options | `nosniff` | Stops MIME sniffing |
| Referrer-Policy | `strict-origin-when-cross-origin` | Limits referrer leakage |
| Permissions-Policy | Camera, microphone, and geolocation disabled | The page does not use them |

DP-01: Dispatcher filters allow the selectors, extensions, and endpoints this page needs, including the persisted query, and the configuration passes the SDK validator. The filter list itself is not specified beyond that sentence in the sources.

## Decision records (SD-09)

### DR-01: Server-rendered HTL, a React SPA in AEM, or a headless React app

Context. The page must be reusable by other channels (A-07), editable in context by six authors, readable by crawlers, and deployed in one command (EN-02, A-09).

| Criterion | A. Server-rendered HTL | B. React SPA in the AEM SPA Editor | C. Headless React hosted outside AEM |
| --- | --- | --- | --- |
| In-context authoring | Yes | Yes, through the SPA SDK | No |
| LCP and crawlers | Best. HTML arrives complete | Weaker. Needs the bundle and the model. The shell carries JSON-LD | Weaker, unless server rendering is added |
| One JSON contract | Needs a second API | Yes. The page model | Yes |
| Single deployment | Yes | Yes | No. A separate pipeline |
| Team fit (A-07) | Less natural | Natural | Natural |
| Complexity | Lowest | Medium. Java and React must agree | Highest |

Decision. Option B. Sling Models export JSON. React renders it. A thin HTL shell carries metadata and JSON-LD.

Consequences. One contract serves the page and other consumers. Authors keep in-context editing. One Maven build deploys the package. Client rendering costs LCP and INP. Crawlers that skip JavaScript depend on the shell (A-08, R-01). Resource types and JSON fields are tested on both sides. The shell preloads the hero. Bundle size is capped in SD-08. Revisit if lab LCP stays above 2.5 s, or if a no-JavaScript fetch cannot see the FAQ content. Reversal cost is low to medium: models, dialogs, templates, and content stay.

### DR-02: Accordion items as page content or as Content Fragments

Context. The six answers are reviewed by Legal, appear in the accordion and in the JSON-LD, are needed by non-AEM consumers, and will be reused for New Zealand.

Options. A. Core Accordion with child components. B. Content Fragments selected by a custom component.

Decision. Option B.

Consequences. One source feeds the HTML, the JSON-LD, and the persisted query. Legal can review one fragment at a time. New Zealand can share fragments. Authors learn fragments. A fragment publishes separately from the page (A-02), which creates the staleness window in DR-04. Revisit if authors cannot run that workflow.

### DR-03: Teaser build (TS-02)

Context. The teaser needs a title, rich text, an image, a link, and a heading level from policy. The Core Teaser already provides most of this.

Options. A. Custom teaser. B. Core Teaser proxy with no extra code. C. Proxy plus a Sling Model that delegates to the Core Teaser model.

Decision. Option C. The delegating model adds `rel="noopener noreferrer"` for new-tab links, and the component id and title for the data layer.

Consequences. Core fixes and policies are inherited. The model is coupled to the Core Teaser API, so upgrades need a test run (R-12). Revisit if the delegating model starts overriding most Core behaviour.

### DR-04: Caching of fragment-driven content

Context. The shell and the page model include fragment content. Publishing a fragment flushes the fragment path, not the pages that show it. Legal changes must reach visitors quickly, and not necessarily instantly.

| Option | Staleness | Cost |
| --- | --- | --- |
| A. Short TTL only | Up to the TTL | None |
| B. TTL plus an author rule: republish the page when a fragment changes | Up to the TTL for page-path caches | Process only |
| C. Listener that invalidates pages when a referenced fragment is published | Seconds | Custom code |
| D. CDN purge on publish | Seconds | Purge integration and credentials |

Decision. A and B. Shell, page model, and persisted query use a 300-second lifetime. The authoring guide tells authors to republish the page with the fragment for Legal changes.

Consequences. Simple and testable. A visitor can see an old answer for up to five minutes. Legal has to accept that (R-03). Revisit for immediate takedown: add C or D and an emergency purge runbook.

### DR-05: How the enquiry reaches Salesforce (designed only)

Context. The enquiry holds personal data (A-06). The browser must not hold Salesforce credentials.

Options. A. Browser calls Salesforce. B. A servlet on AEM Publish calls Salesforce. C. Middleware or a gateway with a queue.

Decision. Option B, behind a small interface, so C can replace it later.

Consequences. Secrets stay on the server. Validation on the server is authoritative. Personal data passes through Publish in memory. Retries are synchronous and short, because a Sling job would store the payload in the JCR, which A-06 forbids. Durable retries require option C.

### ADR-01, ADR-02, ADR-03 (design doc only)

| ID | Decision |
| --- | --- |
| ADR-01 | The hero does not set `sling:resourceSuperType` to a Core Component. The dialog and the Sling Model own the field contract. This matches the repository hero, which has no super type |
| ADR-02 | The accordion reads fragments by walking the folder in the JCR. The persisted query is documented for other clients. A live GraphQL call from the model is described as a fallback if traversal fails, and as something that could move client-side if pagination is required |
| ADR-03 | FAQ JSON-LD is rendered with HTL so it is in the initial HTML without JavaScript. This agrees with SE-01 and with the shell in DR-01 |

## New Zealand rollout (SD-10)

Design. The Australian tree is the Multi Site Manager blueprint. The New Zealand tree is a live copy. One React bundle and one set of templates serve both roots, so the rollout does not need a new build.

| Concern | Approach |
| --- | --- |
| Content structure | `/content/airline/au/en` and `/content/airline/nz/en`, each an SPA app root |
| Inheritance | New Zealand inherits by default. Authors cancel inheritance on one component when a market differs |
| Fragments | One shared DAM folder. Fragments tagged `market/shared`, `market/au`, or `market/nz`. The New Zealand accordion breaks inheritance on its tag filter only |
| Legal differences | New Zealand fragments tagged `market/nz`, reviewed by Legal for that market |
| URLs and SEO | Market domains on Dispatcher virtual hosts. Canonical URL per market. `hreflang` for `en-AU` and `en-NZ` |
| JSON-LD and analytics | Language and market come from page properties |

Language copies are set aside because the language is the same, so there is nothing to translate and no inheritance benefit. An independent site copy would make every Australian change a manual repeat.

Cost to change later. Moving from a live copy to independent copies is a detach: content stays, about one to two days. Moving from independent copies back to a live copy means matching and merging every page by hand. Moving paths after launch breaks URLs and needs redirects, so the path structure is fixed now.

A-14: the local build uses `/content/airline/us/en` instead of `/content/airline/au/en`. That root is the one in this repository.

## Joey Club enquiry, designed only (SD-11)

Sequence in the Claude notes. The drawio shows the same machines with different numbers: the browser-to-servlet edge is unnumbered ("POST enquiry"), and the servlet-to-Salesforce edge is INT-05. This section uses the Claude numbers.

1. The React form validates for usability only.
2. The browser sends INT-13: HTTPS POST with a CSRF token and an idempotency key, through the CDN and Dispatcher. That path is the only POST Dispatcher allows. The response is `Cache-Control: no-store`.
3. The servlet validates: schema, lengths, email format, required consent, unknown fields rejected, a honeypot, and a per-client rate limit. Failure returns 422. Logs do not echo personal data.
4. On success the servlet reads the OAuth client id and secret from Cloud Manager secret environment variables (one set per environment, read through OSGi configuration). They are never in Git, the JCR, logs, or the browser. Local development uses a mock endpoint and dummy values.
5. INT-14: OAuth 2.0 client credentials (A-05). The access token is cached in memory until it expires. The enquiry POST sends the same idempotency key.
6. Timeouts are 2 s connect and 5 s read. Up to two retries on 5xx or timeout, backoff 200 ms then 800 ms. No retry on 4xx. A circuit breaker opens after repeated failures and returns 503.
7. 2xx returns 200 to the visitor. Exhausted retries return 503 and a friendly message. Nothing is queued or stored. A Salesforce 4xx returns 502 with a generic message. The status code is logged. The body is not.
8. Personal data travels over TLS from the browser to the CDN, to Dispatcher, to the servlet in memory, to Salesforce.
9. Personal data does not go to logs, the JCR, the data layer, Adobe Analytics, any cache, error pages, URLs, or query strings.
10. Logs carry a correlation id, HTTP status, and duration. Counters cover submissions, validation failures, Salesforce errors, and latency. Alert when the error rate stays above 5 percent for 5 minutes.
11. The data layer receives the outcome only.

Salesforce still validates last. Same origin, so the design adds no CORS.

## Risks and open questions (SD-12)

| ID | Risk or question | Impact | Owner | Next step |
| --- | --- | --- | --- | --- |
| R-01 | Crawlers that skip JavaScript may miss accordion content | Search and AI visibility | Engineering lead | Fetch with JavaScript disabled. Keep JSON-LD and headings in the shell. Revisit server rendering if that test fails |
| R-02 | Client rendering may miss the LCP target on slow mobile | Page speed | Engineering lead | Measure in PF-01. Preload and the bundle cap. Revisit DR-01 if the miss remains |
| R-03 | Five minutes of staleness may be unacceptable to Legal | Compliance | Legal, content lead | Written acceptance. Emergency purge runbook |
| R-04 | SPA Editor limits on AEMaaCS may change | Authoring approach | Engineering lead | Confirm current Adobe guidance. Keep Sling Models renderer-neutral |
| R-05 | Salesforce limits, credentials, and IP allowlisting are unknown | Integration design | Salesforce owner | Obtain the API contract. Check dedicated egress IPs |
| R-06 | Consent wording and personal-data handling are not signed off | Privacy | Privacy, security | Review SD-11. Agree retention and consent text |
| R-07 | FAQ rich results may never be shown | Benefit expected from SE-01 | SEO lead | Treat JSON-LD as machine-readable content. Check current policy |
| R-08 | CSP may block Tags, or rules may miss the event names | Analytics gaps | Analytics lead | Agree names and Tags hosts. Test in a staging property |
| R-09 | New Zealand legal content may differ by more than expected | Rollout cost | Legal, content lead | List known differences before the quarter starts |
| R-10 | Authors may find the SPA Editor harder than HTL | Adoption | Content lead | Short training. One author works from the authoring guide |
| R-11 | Traffic figures are unknown | Cache and capacity | Platform lead | Obtain current page traffic |
| R-12 | A Core Teaser release may change the delegated model | Teaser regressions | Engineering lead | Tests on the delegating model, run on each upgrade |

## Delivery plan (SD-13)

Estimates are ideal engineer-days. The Claude notes add 20 percent contingency for the plan as a whole. Total 26.5 days, of which 19.5 days are S-01 to S-15 (the exercise build). With contingency the full plan is about 32 days.

| ID | Story | Days | Depends on | Mid-level engineer | Requirement |
| --- | --- | --- | --- | --- | --- |
| S-01 | Archetype scaffold, CI, README skeleton, AI context files | 1.0 | None | No | EN-01, AI-01 |
| S-02 | SPA template, policies, header and footer placeholders, landmarks, skip link | 1.5 | S-01 | With review | PT-01, PT-02 |
| S-03 | Fragment model, six fragments, tags, DAM metadata schema | 1.0 | S-01 | Yes | HD-01 |
| S-04 | Fragment selection service with unit tests | 1.5 | S-03 | No | HD-01, EN-03 |
| S-05 | Hero: model, dialog, React, tests | 2.0 | S-02 | Yes, with review | HB-01, HB-02, HB-03 |
| S-06 | Accordion: model, dialog, accessible React, tests | 2.5 | S-04 | No | AC-01, HD-01 |
| S-07 | Teaser: proxy, delegation, policy, React, tests | 1.5 | S-02 | Yes, with review | TS-01, TS-02 |
| S-08 | Persisted query and a sample request and response | 0.5 | S-03 | Yes | HD-02 |
| S-09 | FAQPage JSON-LD model, shell, validation | 1.0 | S-04 | No | SE-01 |
| S-10 | Page metadata and canonical URL | 0.5 | S-02 | Yes | SE-02 (brief and Claude meaning) |
| S-11 | Data layer events from React | 1.0 | S-05, S-06, S-07 | Yes, with review | DL-01 |
| S-12 | Dispatcher filters, cache, security headers, validator | 2.0 | S-06, S-08, S-09 | No | DP-01 |
| S-13 | UI and accessibility tests | 1.5 | S-11 | Yes | EN-04 |
| S-14 | Performance measurement and tuning | 1.0 | S-12 | No | PF-01 |
| S-15 | README, authoring guide, handover | 1.0 | S-13, S-14 | Yes | Documentation |
| S-16 | Cloud Manager pipeline gates and environment configuration | 1.0 | S-12 | No | INT-11, INT-12 |
| S-17 | Enquiry form, servlet, mock Salesforce, tests | 4.0 | S-06, S-12 | Pair, not solo | SD-11 |
| S-18 | New Zealand live copy and market configuration | 2.0 | S-06 | Yes, with review | SD-10 |

Sequence. S-01 first. S-02 and S-03 in parallel. Then S-04, S-05, S-07, S-08, and S-10 can run in parallel. S-06, S-09, and S-11 follow. S-12 to S-14 harden the build. S-15 and S-16 close it. S-17 and S-18 wait until the accordion and Dispatcher are stable.

Stories the Claude notes would hand to a mid-level engineer: S-03, S-05 (with review), S-07 (with review), S-08, S-10, S-11 (with review), S-13, S-15, S-18 (with review). S-17 can be paired. S-01, S-04, S-06, S-09, S-12, S-14, and S-16 stay with a senior engineer. S-02 is with review.

EN-02, EN-05, and EN-06 are covered by the scaffold, the pull-request practice in SD-14, and the clean-runtime check. They do not have their own story row beyond S-01 and the test stories. EN-05 is three small pull requests. EN-06 is a clean browser console and a clean `error.log`.

## AI-enabled delivery (SD-14)

Team of five: one architect and tech lead, two senior AEM engineers, one mid-level engineer, one QA engineer. AI use stays inside A-13. Approved tools only. No client personal data or credentials in prompts. Synthetic fixtures for tests.

| Area | Approach |
| --- | --- |
| Context files | A repository `AGENTS.md` and `CLAUDE.md` with the stack, versions, AEMaaCS constraints, naming, the resource type convention, and the definition of done. Scoped rules for `core`, the React front end, and Dispatcher. Reviewed like code. This matches AI-01 |
| Reusable prompts and skills | `new-aem-component`: Sling Model with exporter, dialog, React component, `MapTo`, data layer hook, and tests. `model-test`: AEM Mocks for a Sling Model. `dispatcher-change`: checklist and validator. `design-consistency`: diagram ids, registers, and decision records agree. The component skill is the AI-02 artefact |
| Where AI is used | Boilerplate and tests, first drafts of design tables, pull-request review for missed edge cases, accessibility checklists, log summaries. Design and build both |
| Review gates | A human approves every pull request. CI runs unit tests, lint, the Dispatcher validator, the axe scan, and secret scanning. Pull requests say where AI wrote code. Security-relevant changes need two reviewers. AI does not merge or deploy |
| Not delegated | Client personal data or credentials. Security decisions: secrets, the CSP, authentication. Legal wording on infant travel. Final architecture sign-off. Production deployment approval. Estimates committed to the client. Screen reader acceptance testing |
| Learning loop | Wrong outputs are recorded and the context files are updated. Skills are versioned in the repository. The mid-level engineer uses `new-aem-component` first, which is also how the skill is tested |

AI-03 is this repository's `AI_NOTES.md`. AI-04 is ownership of every line and decision in the submission.

## Traceability

Each Part A identifier is the section of the same number. Each Part B identifier is mapped to the design that satisfies it.

| Requirement | Satisfied in |
| --- | --- |
| SD-01 | Purpose and scope |
| SD-02 | Architecture, including the drawio page and the container list |
| SD-03 | Diagram as code |
| SD-04 | Impact table |
| SD-05 | Component register |
| SD-06 | Integration register, both numberings |
| SD-07 | Content model |
| SD-08 | Non-functional targets, cache, security headers |
| SD-09 | Decision records DR-01 to DR-05, plus ADR-01 to ADR-03 from the design doc |
| SD-10 | New Zealand rollout |
| SD-11 | Joey Club enquiry |
| SD-12 | Risks and open questions |
| SD-13 | Delivery plan |
| SD-14 | AI-enabled delivery |
| HB-01, HB-02, HB-03 | Component register (hero), DAM metadata, hero field contract, S-05 |
| TS-01, TS-02 | Component register (teaser), template policies, DR-03, S-07 |
| AC-01 | Component register (accordion), content model, DR-02, S-06. Keyboard and ARIA are the brief's acceptance for this id |
| HD-01 | Content Fragment model, six fragments, tags, DR-02, S-03, S-04, S-06. The design doc's Java comments also use this id for the section heading |
| HD-02 | Persisted query contract, Claude INT-07, S-08 |
| SE-01 | JSON-LD contract, Claude INT-08, ADR-03, S-09 |
| SE-02 | Brief and Claude: page properties in the content model, S-10. Design doc: one `mainEntity` per fragment in the JSON-LD contract |
| PT-01, PT-02 | Template and policies, S-02. Landmarks and the 375 / 1280 layout are PT-02. The sources require header, nav, main, footer, and a skip link. They do not specify the skip-link markup |
| DL-01 | Data layer contract, Claude INT-09, S-11 |
| DP-01 | Cache and security header tables, Claude INT-04 and INT-05, S-12 |
| PF-01 | Non-functional targets, DR-01, S-14 |
| EN-01 | S-01. Archetype for AEMaaCS |
| EN-02 | DR-01 deployment row, A-09, S-01 |
| EN-03 | S-04 and the test expectation on every Sling Model, the fragment selection, and the JSON-LD |
| EN-04 | SD-08 WCAG row, S-13 |
| EN-05 | SD-14 review gates. At least three small pull requests |
| EN-06 | Clean runtime. No story of its own beyond the build and the UI tests |
| AI-01, AI-02, AI-03, AI-04 | SD-14, S-01 |
