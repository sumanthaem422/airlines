# Using the agent on the SPA template editor

## How I set the agent up before it wrote anything

The repo already constrained the agent. This is AEM as a Cloud Service on the Java stack, built with Maven. `CLAUDE.md` only points at `AGENTS.md`, which names the modules, local SDK install, and Cloud Manager full-stack deployment. `.cloudmanager/java-version` is `21`. There is no `.cursor/rules` directory. The SPA editor UI is the React app in `ui.frontend` (`@adobe/aem-react-editable-components`). Page HTL is in `ui.apps`. `spa-page-template` is in `ui.content`, and `airline/components/page` extends `spa-project-core/components/page`.

I handed over the diagnosis before any edit to `customheaderlibs.html`. Structure mode (`editor.html/.../spa-page-template/structure.html`) was blank, and the network tab never requested `model.json`. I had checked that `structure.model.json` returns 200 with JSON, and that the rendered meta `cq:pagemodel_root_url` has no `content` attribute. The cause was `content="${page.hierarchyRootJsonExportUrl}"`. `Page.getHierarchyRootJsonExportUrl()` returns null on `/conf`, HTL drops a null attribute, and `@adobe/aem-spa-page-model-manager` aborts when `getAttribute('content')` is missing. I asked for an inspection, the diff, then a small edit: keep the hierarchy URL on `/content` pages, and on template pages fall back to the current resource path plus `.model.json`. `editor.html` and `request.requestURI` were ruled out in that instruction.

## Where AI helped on the design as well as the build

`SOLUTION_DESIGN.md` and `AUTHORING.md` are still empty. This session did not produce a design document.

The design help was the page-model contract. The SPA editor reads `cq:pagemodel_root_url` and does not guess a URL. On a `/content` page that value is `hierarchyRootJsonExportUrl`. On a `/conf` template page the getter is null, so the meta tag must still emit `content` or the structure iframe stays blank even when the JSON endpoint is healthy. The fallback is the resource being rendered (`structure` or `initial`), the URL I had already loaded by hand.

The build was two files: `ui.apps/.../components/page/customheaderlibs.html` (used by `spa-page-template`) and the same meta in `xfpage/customheaderlibs.html`. Both now set `data-sly-attribute.content="${page.hierarchyRootJsonExportUrl || '{0}.model.json' @ format=request.requestPathInfo.resourcePath, context='uri'}"`. The `||` keeps a real hierarchy URL. `format` fills `{0}` only in the fallback. The CRA shell in `ui.frontend/public/index.html` still uses `%REACT_APP_PAGE_MODEL_PATH%` and was left alone.

## One thing it got wrong and how I caught it

I cannot point to a wrong fallback that I rejected in a browser. AEM was not opened again after the edit, and the expression that landed matches the resource-path fallback I asked for.

What I can point to is the order of work. I asked for the diff before any write. The agent started the edit and said the diff would follow once the change was in. Both header files were already modified when the before-and-after showed up. I caught that from the turn order, not from a failed render.

The same bug is easy to mis-fix by dropping `content` again, or by pointing the tag at `editor.html` or `request.requestURI`. I did not see the agent apply either. The written expression uses `request.requestPathInfo.resourcePath`.

## One task I deliberately did not hand to AI, and why

Live author verification stayed with me: reload structure mode, and publish the template. The model URL inside the editor iframe cannot be confirmed from the HTL diff. A 200 on `structure.model.json` is separate from whether the editor requests that JSON after install. That check was not run. Commit and Cloud Manager deployment stayed out of the handoff. The two HTL files are uncommitted.

## How these documents were produced

I combined four sources into `SOLUTION_DESIGN.md` and `AUTHORING.md`: the candidate brief (`Anchora_SFSE_AEM_Technical_Exercise_Candidate_Brief.docx`), the remaining-deliverables document (`doc.docx`), the draw.io diagram (`Airline_Travel_Info_SD03.drawio`, one page named SD-03 Architecture), and the Claude solution-design document (`Claude- Solution design.docx`). I read the three markdown files here before writing. `AGENTS.md` was project context only.

Identifiers came from the brief, those two design documents, and the draw.io file. I did not invent them. The brief supplies the SD identifiers and the Part B identifiers. The Claude design supplies the A, INT, DR, R, and S identifiers. The remaining-deliverables document supplies ADR-01 to ADR-03. The diagram reuses INT-01 to INT-07. Where one identifier means two different things, both meanings stay in `SOLUTION_DESIGN.md` and the conflict is marked. That applies to INT-03 through INT-07, and to SE-02.

`SOLUTION_DESIGN.md` is the combined design: the identifier index, the architecture, and the trace from each requirement to the section that covers it. `AUTHORING.md` covers the template, experience fragments, policies, and dialog fields the sources or this repository define.

Anything the sources did not specify was left out rather than filled in with a guess. The brief asks for screenshots in the authoring guide, and the sources had none, so the guide has steps and no images.

---

## Component development prompt

### Task: Build an Enterprise AEM + React SPA Component to Project Standards

### 1. Component Specification
- **Component Name**: [e.g., Joey Club Teaser / Accordion / Hero Banner]
- **Resource Type**: [e.g., airline/components/teaser]
- **Component Group**: airline - Content
- **Base / Heritage**: [Custom Built OR Extends Core Component via Delegation Pattern]
- **Authorable Fields**:
  - [Field 1: e.g., title (Textfield, required)]
  - [Field 2: e.g., description (RichText, optional)]
  - [Field 3: e.g., fileReference (DAM image pathfield)]
  - [Field 4: e.g., alt (Textfield with DAM description fallback)]
  - [Field 5: e.g., isDecorative (Checkbox)]
  - [Field 6: e.g., items (Multifield with label and target)]
- **Interactive Events (ACDL)**: [e.g., cmp:linkClick / cmp:accordionToggle]
- **Accessibility Requirements**: [e.g., WCAG 2.1 AA, semantic markup, aria-expanded, keyboard Tab/Enter/Space]

### 2. Mandatory Architectural Standards to Implement:

#### Layer 1: Java Interface (`core/src/main/java/com/airline/core/models/`)
- Define a clean Java interface extending `ComponentExporter`.
- Declare getters for all authored fields and an `isEmpty()` boolean method.

#### Layer 2: Java Sling Model Implementation (`core/.../impl/`)
- Annotate with `@Model(adaptables = SlingHttpServletRequest.class, adapters = {MyInterface.class, ComponentExporter.class}, resourceType = "airline/components/mycomponent", defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)`.
- Annotate with `@Exporter(name = ExporterConstants.SLING_MODEL_EXPORTER_NAME, extensions = ExporterConstants.SLING_MODEL_EXTENSION)`.
- Implement `getExportedType()` returning the resource type string.
- If image asset is present: implement the **DAM description fallback** when `alt` is blank and not decorative (`Asset.getMetadataValue("dc:description")`).
- Keep business logic strictly in Java—**zero business logic in HTL or React**.

#### Layer 3: Touch UI Dialog (`ui.apps/.../components/mycomponent/_cq_dialog/.content.xml`)
- Use Coral 3 / Granite UI components (`granite/ui/components/coral/foundation/...`).
- Ensure all parameters match Sling Model injection field names.
- If external links are allowed, enforce `rel="noopener noreferrer"`.

#### Layer 4: React SPA Component (`ui.frontend/src/components/MyComponent/`)
- Create `MyComponent.tsx` (or `.jsx`).
- Implement `MyComponentEditConfig` with `emptyLabel` and `isEmpty` logic to ensure AEM Editor displays a clean placeholder without broken markup when unauthored.
- Map the component using `export default MapTo('airline/components/mycomponent')(MyComponent, MyComponentEditConfig);`.
- Connect to Adobe Client Data Layer (`window.adobeDataLayer`): push interaction events on click/toggle with component ID and title.

#### Layer 5: Responsive SCSS (`ui.frontend/src/components/MyComponent/MyComponent.scss`)
- Use BEM naming conventions (`.airline-my-component__element--modifier`).
- Provide responsive layouts tested for **375px mobile** and **1280px desktop**.
- Maintain accessible focus states (`:focus-visible`).

#### Layer 6: Unit Tests with AEM Mocks (`core/src/test/java/.../MyComponentImplTest.java`)
- Use JUnit 5 and `io.wcm.testing.mock.aem.junit5.AemContext`.
- Test cases must cover:
  1. Standard authored content.
  2. Fallback logic (e.g., DAM metadata fallback when alt is empty).
  3. Edge cases (decorative image, empty model returning `isEmpty() == true`).
  4. Multifield parsing (if applicable).

### Output Requirements:
1. Provide the complete code for all 5 layers with explicit file paths.
2. Confirm that `mvn clean install` will compile with zero errors and passing unit tests.

---

## Design and build log

Team log of the asks that drove the SPA editor fix, the hero, the Joey Club teaser, and the data layer. Each subsection keeps the ask, then what is in the repo now. Uncommitted work is an agent change unless Manual changes says otherwise. `mvn clean install` was not run for the whole reactor.

### Editable template structure mode

**Ask.** Editable Template structure mode was blank. `customheaderlibs.html` for the page and for `xfpage`. The `cq:pagemodel_root_url` `content` attribute was dropped because `hierarchyRootJsonExportUrl` is null under `/conf`. Fix: `data-sly-attribute.content` with the hierarchy URL or `{0}.model.json` from `request.requestPathInfo.resourcePath`. Files: `ui.apps/.../page/customheaderlibs.html` and `xfpage/customheaderlibs.html`.

**What landed.**

- Page header uses the requested expression: `data-sly-attribute.content="${page.hierarchyRootJsonExportUrl || '{0}.model.json' @ format=request.requestPathInfo.resourcePath, context='uri'}"`.
- `xfpage/customheaderlibs.html` still contains that expression, commented out. The live tag is `content="${pageModelUrl}"`, and `pageModelUrl` is `page.hierarchyRootJsonExportUrl || '{0}.model.json' @ format=[currentPage.path]`. That divergence is in `git diff`. See Manual changes.

### Load React clientlib

**Ask.** Category `airline.react` from `ui.frontend/clientlib.config.js`. Page `customfooterlibs.html` loads `airline.base` and `airline.react`, loads `cq.authoring.pagemodel.messaging` with its own `data-sly-use.clientlib` (HTL `data-sly-use` is scoped to that element), and includes `if (window._satellite) { _satellite.pageBottom(); }`. Page `customheaderlibs.html` CSS categories include `airline.base` and `airline.react`. `ModelManager.initialize` is in `ui.frontend/src/index.js`. The xfpage header and footer were not given the react category.

**What landed.**

- `clientlib.config.js` sets `categories: ['airline.react']` on `clientlib-react`.
- Page footer matches the ask, including a second `data-sly-use.clientlib` on the messaging `<sly>`. The earlier `async=true` on `airline.base` is gone from that file.
- Page header CSS categories are `airline.base` and `airline.react`.
- `ui.frontend/src/index.js` calls `ModelManager.initialize`. This working tree also passes `pageModel.title` and `pageModel.id` into `App`.
- xfpage header CSS is `airline.base` only. xfpage footer is unchanged from the initial commit and still loads `testing-frontend-react.base` plus `cq.authoring.pagemodel.messaging`.

### Hero Banner build (HB-01, HB-02, HB-03, PF-01, EN-03)

**Ask.** Resource type `airline/components/herobanner`. The title is the only `h1` inside the hero. That is not a page-wide guarantee. Fields: intro, DAM image, alt, decorative, and a multifield of jump links stored as `./text` and `./anchor` (those names, not `label` / `anchorId`). Dialog checkbox is `./decorative`, not `isDecorative`. Eager image: `fetchpriority=high` and `loading=eager`. Sling model behind the `HeroBanner` interface. JUnit `HeroBannerImplTest`. React `MapTo`. Empty editor placeholder. Later layout change: vertical stack, no grey card, image `aspect-ratio` 16/7, `border-bottom-right-radius` 96px, intro max-width 780px.

**What landed.**

- `ui.apps/.../herobanner`, component group `Airline Travel Info - Content`, resource type `airline/components/herobanner`. Package `com.example.airline.core.models`. `HeroBannerImpl` adapts to `HeroBanner`, `Component`, and `ComponentExporter`.
- Dialog fields: `./title`, `./introduction`, `./fileReference`, `./alt`, `./decorative`, multifield `./text` and `./anchor`. `HeroBannerLinkImpl` exports `text`, and normalises `anchor` / `href` to `#id`.
- React renders `h1.cmp-herobanner__title` only when the hero title is non-blank.
- The `<img>` sets `fetchpriority="high"` and `loading="eager"`.
- `MapTo('airline/components/herobanner')` runs in `HeroBanner.js`. `import-components.js` imports that module and does not call `MapTo` for the hero again.
- `HeroBannerEditConfig.emptyLabel` is `Configure Hero Banner`.
- CSS is a column, background transparent, image `aspect-ratio: 16 / 7`, `border-bottom-right-radius: 96px`, intro `max-width: min(780px, 100%)`.
- The dialog still says the title is "the only H1 on the page" and that a blank alt uses the DAM description. The model follows the later alt override below, and the audit recorded the page-wide `h1` gap.

### Hero alt rule override

**Original brief (HB-02).** If the author leaves alt blank and the image is not decorative, use DAM `dc:description`.

**Later override, requested after that brief.** If the author leaves alt blank and the image is not decorative, use DAM `dc:title`, then the component title, else `""`. Decorative still returns `""`. React reads `props.alt` only. `dc:description` is no longer the hero alt source.

**What landed.** `HeroBannerImpl.resolveAlt` follows the override (`dc:title`, then `getTitle()`, else `""`; decorative returns `""`). `HeroBanner.getAlt` documents that order. `HeroBanner.js` uses `props.alt` when it is a string, otherwise `""`. It does not read `decorative` or DAM metadata. The dialog field description still tells authors that a blank alt uses the DAM asset description. Both rules are recorded here because they conflict.

### Data layer events in code

**Ask.** List every event the code actually pushes. `cmp:show` on page load. Hero component data layer JSON on `data-cmp-data-layer` (`ComponentData`, `id`, `@type` `airline/components/herobanner`, `dc:title`). `cmp:jumpLinkClick` with `link.label` and `link.anchorId`. `cmp:accordionToggle` with `accordion.id`, `accordion.title`, and `accordion.expanded`. `cmp:linkClick` with `link.title`, `link.url`, and `link.componentId` when passed. DL-01 is named in `SOLUTION_DESIGN.md` and is not specified there. Joey Club had no component when the helper was first added. `cmp:linkClick` is now attached to the teaser CTA.

**What landed.** Payloads are in the Events subsection. DL-01 appears only in assumption A-03 of `SOLUTION_DESIGN.md` (Tags listens to the Adobe Client Data Layer; event names are agreed with analytics). That file has no DL-01 event list. `pushLinkClick` is in `ui.frontend/src/utils/adobeDataLayer.js`. `Teaser.js` calls it from the CTA `onClick`.

### Hero image placeholder

**Ask.** If the image is not authored, the editor shows `Configure hero image` and does not render an empty `img`. Publish omits the image slot. The whole hero is empty only when title and image are both blank. This is separate from the teaser empty rule.

**What landed.** `isHeroBannerEmpty` is true when `props.empty === true`, or when the title and `src` / `fileReference` are both blank. Java `isEmpty` is the same pair: title and `fileReference`. In the editor, a hero that has a title and no image renders the text `Configure hero image` and no `<img>`. Outside the editor that slot is omitted. A hero with both title and image blank renders the `Configure Hero Banner` placeholder in the editor and renders nothing on publish. The teaser empty check is a different rule: Java is empty when title, description, and image are all blank; the React teaser edit config checks title and description.

### HB-01 / HB-02 / HB-03 audit

**Ask.** Record the audit results. The audit itself did not change code.

**What landed.** There is no audit-only diff. These fails are still visible in the current files:

- The page can have another `h1` from Title or Text. The hero only owns the `h1` inside the hero. The dialog still says the title is the only H1 on the page.
- Multifield names are `./text` and `./anchor`.
- The checkbox name is `./decorative`.
- Hero alt fallback is DAM `dc:title`, then the component title. The hero does not use `dc:description`. The dialog description still says DAM description.
- React does not set `aria-hidden` from `decorative`. `HeroBanner.js` never reads `decorative`. A decorative image is `alt=""`.
- `HeroBannerEditConfig.emptyLabel` is `Configure Hero Banner`, not `Hero Banner`.
- React still decides emptiness, the missing-image placeholder, the jump-link click payload, and the eager image attributes.

### Component development prompt

The component-development prompt is already in this file: [Component development prompt](#component-development-prompt). It is not repeated here.

### Joey Club Teaser build

**Ask.** Resource type `airline/components/teaser`. `sling:resourceSuperType` `core/wcm/components/teaser/v2/teaser`. Delegation `@Self` `@Via` `ResourceSuperType`. `getExportedType` returns `airline/components/teaser`. Package `com.example.airline.core.models`. Group `Airline Travel Info - Content`. The first cut had extra fields (`alt`, `isDecorative`, `linkTarget`). The authoring dialog keeps five fields: `./title`, `./description`, `./fileReference`, `./linkText`, `./linkURL`. Alt for the teaser image comes from DAM `dc:description`, not an author field. Same tab by default. An absolute `http(s)` URL or `_blank` gets `rel="noopener noreferrer"`. Heading comes from policy `headingElement` / `titleType`, allowed `h2` / `h3` / `h4`, default `h2`. Layout: desktop 45/55, card background `#aae0fa`, `border-radius` `20px 20px 20px 72px`, padding `2.5rem`, CTA `#ba1200` with an arrow, stack at 375px. The React file is `Teaser.js` (renamed from `JoeyClubTeaser.js`). `MapTo` once in `Teaser.js`. `import-components.js` must not register the teaser a second time.

**What landed.**

- `teaser/.content.xml` has the super type, group `Airline Travel Info - Content`, and title `Joey Club Teaser`. Git shows the title change from `Teaser` on the initial component.
- `JoeyClubTeaserImpl` delegates with `@Self` `@Via(type = ResourceSuperType.class)` and `getExportedType()` returns `airline/components/teaser`.
- The dialog sets `sling:hideProperties="*"` and keeps `./title`, `./description`, `./fileReference`, `./linkText`, `./linkURL`. There is no alt, decorative, or link-target field.
- `getAltText` reads DAM `dc:description` when an image is present, otherwise `""`.
- Java `getRel()` is always `""`. React sets `target="_blank"` and `rel="noopener noreferrer"` when `actions[0].target` or `props.linkTarget` is `_blank`, or when the href is absolute `http:` / `https:`. A content path stays in the same tab and omits `rel`, unless `props.rel` is already set.
- Java reads policy `headingElement`, then `titleType`, keeps `h2` / `h3` / `h4`, defaults to `h2`, and exports that value as both `headingLevel` and `titleType`. React uses the first of `titleType` or `headingLevel` in that allowed set, otherwise `h2`.
- `Teaser.css`: column by default, so 375px stacks. From `min-width: 768px` the text column is 45% and the media column is 55%. Background `#aae0fa`, radius `20px 20px 20px 72px`, and padding `2.5rem` sit on `cmp-joeyclubteaser__media`. The CTA is `#ba1200` with a `→` arrow. `aria-hidden="true"` is on that arrow only.
- `MapTo('airline/components/teaser')` is only in `Teaser.js`. `import-components.js` side-effect-imports `./Teaser/Teaser`. The previous `MapTo` onto core `TeaserV1` is removed.
- `teaser.html` renders the core placeholder from `com.example.airline.core.models.JoeyClubTeaser`. The SPA view is the React file.
- Java `isEmpty` is title, description, and image all blank. React `isJoeyClubTeaserEmpty` checks title and description.

### Core JSON compatibility

**Ask.** React also reads `image.src`, `:items.image.src`, `actions[0].url` / `title` / `text`, and `titleType`. Java exports both the flat fields and nested `image` / `actions`. Flat `linkText` / `linkURL` win over Core actions children when non-blank.

**What landed.**

- Java `getImage()` exports `{ src, fileReference }` when a file reference exists. `getExportedActions()` exports `{ title, text, url, target }`. Flat `linkText`, `linkURL`, `fileReference`, `src`, and `alt` are exported too.
- `resolveActions` keeps a non-blank flat `linkText` or `linkURL` and fills only a blank side from the first Core actions child. When both flat fields are blank, actions children are used, then the core delegate. `backfillLinkFromActions` copies the winning action back onto the flat fields.
- React image order is `image.src`, `:items.image.src`, `imageSrc`, `fileReference`, `src`. CTA label order is `actions[0].title`, `actions[0].text`, `linkText`, `linkLabel`. Href order is `actions[0].url`, then `linkURL`. A model from `JoeyClubTeaserImpl` already applied the flat-field win inside `actions`. A props object that carries a different `actions[0]` and flat fields will render the action.

### Bundle fix

**Ask.** `airline.core` did not start because `Import-Package` was too new for this SDK (core components models 12.33 vs 12.29, `com.day.cq.wcm.api` 1.33 vs 1.32, jackson 2.22 vs 2.17). `core/pom.xml` import ranges were relaxed. Until the bundle was Active, the Core Teaser model kept serving the page and ignored the custom dialog.

**What landed.** `core/pom.xml` now imports `com.adobe.cq.wcm.core.components.models;version="[12.29,14)"`, `com.day.cq.wcm.api;version="[1.27,2)"`, `com.fasterxml.jackson.annotation;version="[2.12,3)"`, and `*`. In the same diff, `core.wcm.components.core` moved from `test` scope to `provided`. The version clash above is the reported reason the bundle stayed down. This pass did not re-query the OSGi resolver.

### Build fixes

**Ask.** `MapTo` was used without an import in `Teaser.js` and `HeroBanner.js`. `Teaser.test.js` expected an old Joey Club href and label and was updated to the authored values under test. These are build fixes, not design.

**What landed.** Both files import `MapTo` from `@adobe/aem-react-editable-components`. Current `Teaser.test.js` expects `Explore aa` / `https://www.aa.com` and `Explore aa and Bluey` / `https://www.aa.com/joey`. Those frontend files are untracked, so git has no earlier test blob. See Manual changes.

Last recorded run, not a full-reactor `mvn clean install`: `HeroBannerImplTest` passed, `JoeyClubTeaserImplTest` passed (13 tests), and the `ui.frontend` suite passed (9 suites, 52 tests).

### Manual changes

`git status` is the initial commit plus a dirty working tree. Nothing here is a second commit, and no blame author is attached to the uncommitted lines.

- `ui.apps/.../xfpage/customheaderlibs.html` is the file that does not match the resourcePath ask. `git diff` shows the `request.requestPathInfo.resourcePath` expression commented out, with a live fallback of `format=[currentPage.path]`. It is an uncommitted working-tree edit. It is not attributed to a named person.
- `ui.frontend/src/components/Teaser/Teaser.js` is the current file name. There is no `JoeyClubTeaser.js` in the tree, and git has no rename because the file is untracked. The name is not attributed to a person.
- `Explore aa` and `https://www.aa.com` are the current strings in untracked `Teaser.test.js`. Git has no previous test file, so those strings are not attributed to a person.

The other files covered above match the asks and are agent changes.

### Events

Copy-ready list of what the code pushes to `window.adobeDataLayer`. The array is created if it is missing. Blank strings are omitted. Enquiry and other personal fields are not copied.

| Event | When | Payload |
| --- | --- | --- |
| `cmp:show` | `App.componentDidMount` and the mapped page in `Page.js` (`AppPage.componentDidMount`), both through `pushPageShow` | `event`, `eventInfo.path` = `page`, `eventInfo.type` = `page`. `page.title` and `page.id` only when the model value is non-blank. |
| Hero component object | Hero `useEffect` when the banner is not empty and `props.dataLayer` is a JSON string. The same string is the `data-cmp-data-layer` attribute. | Parsed `ComponentData`: one property keyed by `id`. Value has `@type` `airline/components/herobanner` and `dc:title`. This object has no `event` name. Jump-link label and anchor are not in it. |
| `cmp:jumpLinkClick` | Click on a hero jump link | `link.label` from `link.text`. `link.anchorId` is the href with the leading `#` characters removed. |
| `cmp:accordionToggle` | Click on an accordion title button (`Accordion.js`) | `accordion.id`, `accordion.title`, `accordion.expanded` (boolean for the state after the click). |
| `cmp:linkClick` | Teaser CTA click, through `pushLinkClick` | `link.title`, `link.url`. `link.componentId` only when the caller passes a non-blank id. |

`if (window._satellite) { _satellite.pageBottom(); }` in the page footer is the Adobe Launch hook. It is not an `adobeDataLayer` push.
