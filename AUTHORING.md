# Authoring guide

For an author who has used AEM and has not used this project. The page is the airline's "Flying with infants and children" travel information page, edited in the SPA Editor.

The brief asks this guide to include screenshots. The sources contained no screenshot files, so the steps below have no images.

Design detail and the identifier index are in `SOLUTION_DESIGN.md`. This guide uses only templates, components, and fields that the sources or this repository define.

## What you are editing

| Item | Where |
| --- | --- |
| Local site root in this repository | `/content/airline/us/en` (A-14). Resource type `airline/components/spa`. Template `spa-app-template` |
| Design roots, when New Zealand is rolled out (SD-10) | `/content/airline/au/en` and `/content/airline/nz/en` |
| Sample page in the content package | `/content/airline/us/en/home`. Template `spa-page-template`. Resource type `airline/components/page`. Title "Airline Travel Info Home Page" |
| Page template | `/conf/airline/settings/wcm/templates/spa-page-template`. Title "SPA Page" |
| Header experience fragment | `/content/experience-fragments/airline/us/en/site/header/master` |
| Footer experience fragment | `/content/experience-fragments/airline/us/en/site/footer/master` |

Open the sample page in the editor at `/editor.html/content/airline/us/en/home`. Preview without the editor chrome uses `?wcmmode=disabled` on the `.html` URL.

The sample page's editable area currently contains a Text component (`airline/components/text`). The design for this page uses the hero, the accordion, and the Joey Club teaser.

## Template: structure and initial (PT-01)

`spa-page-template` is an editable template.

Structure (`structure/.content.xml`) is the locked skeleton of every page:

1. Header, resource type `airline/components/experiencefragment`, variation `/content/experience-fragments/airline/us/en/site/header/master`.
2. Main band, resource type `wcm/foundation/components/responsivegrid`, with `editable` set to true.
3. Footer, resource type `airline/components/experiencefragment`, variation `/content/experience-fragments/airline/us/en/site/footer/master`.

The header and footer nodes do not set `editable`. On a page created from this template they are locked. Change them in the experience fragment, not on the page.

Initial content (`initial/.content.xml`) is what a new page starts with: the root responsive grid and an empty child responsive grid. It does not repeat the header or the footer. Those come from structure.

The page component is `airline/components/page` ("Airline Travel Info Page"), which extends `spa-project-core/components/page`. The SPA root page above it is `airline/components/spa`.

### Policies

| Policy | What the sources specify | What this repository configures |
| --- | --- | --- |
| Container | Claude notes (SD-07): only the hero, the travel accordion, and the Joey Club teaser | `wcm/foundation/components/responsivegrid/spa-default` allows the component group `Airline Travel Info - Content` |
| Hero | Title is the only H1. The level is not editable (HB-01) | The hero dialog description says the title is rendered as the only H1 |
| Teaser | Heading level H2 or H3, default H2. New-tab links get `rel="noopener noreferrer"` (TS-01) | The teaser design dialog allows h2, h3, and h4 on `./headingElement` |
| Accordion | Fragment folder restricted to `/content/dam/airline/travel-info`. Item buttons render inside H3 | No accordion policy with that folder restriction is in the template |
| Page | Adobe Client Data Layer enabled | `airline/components/page/policy_spa` sets the client library category `airline.react` |

Place the hero, the Joey Club teaser, and the accordion in the editable band. Leave the header and footer locked.

## Experience fragments

Header and footer already exist. They are placeholders (PT-01). The brief does not ask authors to redesign global navigation.

| Fragment | Editor | What is inside, in this repository |
| --- | --- | --- |
| Header | `/editor.html/content/experience-fragments/airline/us/en/site/header/master` | Template `xf-web-variation`. Page resource type `airline/components/xfpage`. A container (`airline/components/container`) holds `airline/components/header` (Site Header, a Core Navigation proxy). `navigationRoot` is `/content/airline/us/en` |
| Footer | `/editor.html/content/experience-fragments/airline/us/en/site/footer/master` | Same template and `xfpage` resource type. A container holds `airline/components/footer` (Site Footer) with `copyrightText` |

Edit the fragment master, then publish the fragment. Pages that reference it pick up the published variation. The page itself does not unlock the fragment.

## Create or open the page

1. In Sites, open **Airline / us / en**. That page is the SPA app root.
2. Use the existing **home** page, or create a page with template **SPA Page** (`spa-page-template`).
3. Open the page in the editor.
4. Confirm the header and footer show a lock. Edit those only from the fragment URLs above.
5. In the editable responsive grid, add components from **Airline Travel Info - Content**:
   - **Hero Banner** — `airline/components/herobanner`
   - **Joey Club Teaser** — `airline/components/teaser`
   - **Accordion** — `airline/components/accordion`
6. The Claude container policy allows only those three. The repository policy allows the whole component group, so other components in that group can also be dropped. For this page, use the three above.
7. Publish the page when Legal has accepted the infant-travel wording (A-01). The approval step itself is not built.

Check the layout at 375 pixels wide and at 1280 pixels wide (PT-02). The brief also requires a skip link and landmarks for header, nav, main, and footer. Those are part of the build. This guide does not invent markup for them.

## Hero banner (HB-01, HB-02, HB-03)

Resource type in this repository: `airline/components/herobanner`. The Claude notes call the same role `airline/components/hero`. The dialog below is the one in `ui.apps`.

Open the component and edit.

| Dialog label | Property | Required | What to enter |
| --- | --- | --- | --- |
| Title | `./title` | Yes | Plain text. The dialog says this is rendered as the only H1 on the page (HB-01) |
| Introduction | `./introduction` | No | Short paragraph under the title |
| Image | `./fileReference` | No | DAM path. Root of the browser is `/content/dam`. The design-doc authoring note recommends a wide landscape, at least 1440 by 600 |
| Alt text | `./alt` | No | Describe the image. The field description says a blank value uses the DAM asset description, and that alt is ignored when the image is decorative (HB-02) |
| Decorative image | `./decorative` | No | Check this when the image has no meaning. Output is an empty alt |
| On this page | `./links` | No | Multifield. Each row has **Link label** (`./text`) and **Anchor** (`./anchor`). The anchor is a section id on this page, with or without a leading `#` |

HB-02 in the brief, and the dialog text, use the DAM **description** (`dc:description`) when alt is blank and the image is not decorative. Fill alt in when the description metadata is missing or wrong.

HB-03: if the hero has no title and no image, the editor shows a placeholder and the published page does not render a broken hero. The design-doc empty message is not specified for the hero beyond that rule. Give the hero a title before you publish.

The hero image is the one image that must not be lazy-loaded (PF-01). Authors do not set that flag. Choose an image that can meet the design target of 120 KB or less, WebP, with width and height in the asset metadata so the page can reserve space (SD-08).

## Joey Club teaser (TS-01, TS-02)

Resource type: `airline/components/teaser`. Title in the repository: "Joey Club Teaser". It extends `core/wcm/components/teaser/v2/teaser` (TS-02, DR-03). The dialog sets `sling:hideProperties="*"` and then shows five fields.

| Dialog label | Property | What to enter |
| --- | --- | --- |
| Title | `./title` | Section heading. The level comes from the template policy, not from this dialog |
| Description | `./description` | Rich text |
| Image | `./fileReference` | DAM image. Alt for this image is the DAM description. There is no alt field on this dialog |
| Link text | `./linkText` | Label of the call to action |
| Link URL | `./linkURL` | Content path or URL |

Heading level: the Claude policy allows H2 or H3, default H2. This repository's design dialog (`_cq_design_dialog`) stores `./headingElement` and offers h2, h3, and h4. H1 is not in that list. A template author changes it under the template policy. A page author does not.

TS-01: a link that opens in a new tab carries `rel="noopener noreferrer"`. This dialog has no target field. The design-doc authoring note says external URLs receive that `rel` automatically, and that "Get title from linked page" on the Core Teaser should be unchecked so the authored title is the one that shows. Those inherited Core fields are hidden on this dialog.

## Accordion and travel questions (AC-01, HD-01, HD-02)

Two authoring contracts exist in the sources. They do not match each other, and the dialog in this repository matches neither fragment contract.

### Contract in the Claude notes

- Model `travel-question` at `/conf/airline/settings/dam/cfm/models/travel-question`.
- Fields: `question` (required), `answer` (required rich text), `displayOrder` (required number), `tags` (optional).
- Folder `/content/dam/airline/travel-info`.
- Tags: `airline:travel-info/infants`, `airline:travel-info/booking`, and market tags `airline:market/shared`, `airline:market/au`, `airline:market/nz`.
- The component selects by folder or by tags in the dialog (any match), then orders by `displayOrder` (HD-01).
- Resource type in that design: `airline/components/travelaccordion`.

### Contract in the remaining-deliverables document

Model name `travel-question`, created from the Content Fragment model editor into the airline folder.

| Field label | Field name | Type |
| --- | --- | --- |
| Question | `question` | Single-line text |
| Answer | `answer` | Multi-line rich text |
| Tags | `tags` | Tags |

There is no `displayOrder` field in that document.

Create the six fragments under `/content/dam/airline/travel-questions` (**Assets / Files / airline / travel-questions**, Create, Content Fragment, model `travel-question`). Tag each with `airline:travel/infants`.

| Slug | Question | Answer to summarise from |
| --- | --- | --- |
| `booking-infants` | Booking for infants (under 2 years) | Infants under 2 travel on a parent's lap. Book by calling reservations |
| `infant-fees` | Are there fees for infants? | Infant fees vary by route. Domestic routes are free; international routes incur a tax |
| `lap-infant-safety` | Is it safe for my infant to travel on my lap? | Lap infants are secured with a loop belt attached to the adult seatbelt |
| `infant-meals` | Can I request infant meals? | Available on flights over 2 hours. Request at least 24 hours ahead |
| `bassinets` | Are bassinets available on board? | Wide-body aircraft. Request when booking. Subject to availability |
| `children-fares` | What fares apply to children aged 2–11? | Own seat, child fare |

The answers in the table are the summaries from that document. Legal reviews the wording before publish (A-01). Publish each fragment. A fragment is published separately from the page (A-02).

That document's component dialog, for resource type `airline/components/accordion`:

| Dialog label | Property | Notes |
| --- | --- | --- |
| Section Heading | `./sectionHeading` | Required. Example: "Booking for infants (under 2 years)". This is the AC-01 section heading. In that document the same sentence is also the first fragment's question |
| Content Fragment Folder | `./contentFragmentFolder` | Path field rooted at `/content/dam/airline`. Point it at `/content/dam/airline/travel-questions` |
| Heading Level | `./headingLevel` | Select H2, H3, or H4. Default in the model is `h2` |

After save, that design fills the accordion from every fragment in the folder. There is no tag field on this dialog. The brief allows folder or tag. This dialog implements folder only.

Empty state in that document's React component: "Accordion — author a Content Fragment folder to populate items."

### Dialog on `airline/components/accordion` in this repository

The component title is "Accordion". It has no Core Component super type. Its dialog does not select fragments.

| Dialog label | Property | Notes |
| --- | --- | --- |
| Title | `./title` | Described as an H2. The hero owns the H1 |
| Introduction | `./introduction` | Short text under the heading |
| Items | `./items` | Multifield of topics |
| Item title | `./itemTitle` | Panel heading |
| Item text | `./itemText` | Rich text. Bold, italic, links, lists |
| Links | `./links` | Nested multifield. Zero or more calls to action |
| Link text | `./linkText` | |
| Link URL | `./linkURL` | Content path or absolute http(s) URL. The field description says absolute URLs open in a new tab |

Authors using the fragment design follow the Claude notes or the remaining-deliverables dialog, not this multifield. Authors using the component that is installed follow the multifield. The six reference questions can be typed into **Item title** and **Item text** until a fragment-backed dialog is what the page shows.

AC-01 also requires header buttons with `aria-expanded` and `aria-controls`, operable with Tab, Enter, and Space. That is behaviour of the component, not a dialog field. Keyboard-check the published page.

### Persisted query (HD-02)

Authors do not run this for ordinary editing. It is how a non-AEM consumer reads the same questions. Endpoint from the design doc:

`http://localhost:4502/graphql/execute.json/airline/travel-questions`

The query name is `airline/travel-questions`. The design doc's persist command filters on `/content/dam/airline/travel-questions` and returns `question`, `answer.html`, and `_path`. A second sample in that document's authoring section omits the path filter. Details are in `SOLUTION_DESIGN.md`.

The page accordion, in that document's ADR-02, reads the fragments from the folder in the repository. It does not depend on the query being called at render time. The query still has to exist for HD-02.

## Page metadata (SE-02)

In the brief and the notes, SE-02 is page properties:

| Property | Where |
| --- | --- |
| Title | Page properties. The sample page uses `jcr:title` |
| Meta description | Page properties |
| Canonical URL | Page properties. The Claude notes default it to the market URL of the page |

The page component in this repository does not define its own canonical field. Use the page properties the template already exposes, and record a canonical value when that field is present.

The remaining-deliverables document uses SE-02 for a different thing: one `mainEntity` per fragment inside the FAQPage JSON-LD (SE-01). Authors do not type JSON-LD. It is generated on the server from the same questions as the accordion. Publishing the fragments and the page is what updates it.

## Header, footer, and the rest of the page (PT-02)

- Header and footer on the page are locked. Edit the experience fragments.
- The editable band is the main area of the page.
- The brief requires landmarks for header, nav, main, and footer, and a skip link. Confirm them in preview. The sources do not list authorable fields for the skip link.
- Widths to check: 375 and 1280 pixels.

## Publish, and fragment changes (DR-04)

1. Publish each travel-question fragment you changed.
2. Publish the page as well when the change is to infant travel rules.

The shell, the page model, and the persisted query are cached for five minutes (SD-08). Publishing a fragment does not by itself flush the pages that display it. Republishing the page is the author step that shortens that window. A visitor can still see the previous answer until the cache expires. Legal has to accept that window (R-03). There is no emergency-purge control in the authoring UI described by the sources.

Personal data for a Joey Club enquiry is designed only (SD-11). There is no enquiry component to author in the sources' build scope, and no `airline/components/joeyclubform` in this repository.

## Data layer (DL-01)

Authors do not configure event names. Expanding or collapsing an accordion item, and clicking the teaser link, push events that include the component id and title. The Claude notes name those events `cmp:show`, `cmp:hide`, and `cmp:click`. The remaining-deliverables accordion sample uses `cmp:accordionToggle`. Production names are agreed with the analytics team (A-03). Do not put membership or other personal details into component text that would be copied into an event.

| Role | Claude resource type | Design-doc resource type | Resource type in this repository |
| --- | --- | --- | --- |
| Hero | `airline/components/hero` | `airline/components/herobanner` | `airline/components/herobanner` |
| Teaser | `airline/components/teaser` | `airline/components/teaser` | `airline/components/teaser` |
| Accordion | `airline/components/travelaccordion` | `airline/components/accordion` | `airline/components/accordion` |
| Experience fragment | `airline/components/experiencefragment` | Locked XF in the template structure | `airline/components/experiencefragment` |
| Page | `airline/components/page` | `spa-page-template` | `airline/components/page` |
| SPA root | `airline/components/spa` | Not given a separate type in that document | `airline/components/spa` |
