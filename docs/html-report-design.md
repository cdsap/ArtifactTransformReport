# Artifact Transform HTML report — design rationale

This document explains *why* the HTML report (`cli/.../output/HtmlOutput.kt`) is built the way it is.
It complements the text and CSV outputs; the goal here is a **visual, explorable** view of a build's
artifact transforms that a developer can open and act on without extra tooling.

## Guiding principles

1. **Self-contained and offline.** The report is a single `.html` file with **Chart.js inlined** from a
   bundled resource (`cli/src/main/resources/chart.umd.min.js`) and all data embedded as JSON. No CDN, no
   external requests. This matters because reports are produced in CI and opened later as build artifacts,
   often on machines with no network or behind firewalls. The tradeoff (a larger file, ~200 KB of library)
   is deliberate and worth it for "double-click and it just works."

2. **The library computes, the CLI renders.** Every number on the page comes from a pure extension
   function in `:library` (`ArtifactTransforms.kt`), unit-tested in isolation. `HtmlOutput` only shapes
   those results into chart specs and SVG. This keeps the analytics testable and reusable, and keeps
   presentation decisions out of the data layer.

3. **Derived from data the endpoint already returns.** Nothing here needs a second API call. Everything is
   computed from the one `gradle-artifact-transform-executions` payload, so the report stays cheap.

4. **Show a metric only when it's meaningful.** Empty datasets are skipped (`addSpec` no-ops on empty
   labels), so a fully-cached build doesn't render an empty "avoidable misses" card, and the
   per-build-scan chart only appears for multi-build runs. Absence of a card is information too.

## Page structure (and why this order)

The report reads top-down from **"how healthy / how big"** to **"where is the cost"** to **"what is the
pipeline doing"**:

1. **Summary stats** — Total transforms, Build scans, Cache hit rate, Total cache size. The four numbers a
   reader wants first: scale, and whether caching is working.
2. **Pipeline graph** — the structural overview (what feeds what) before drilling into magnitudes.
3. **Version fragmentation table** — a targeted "fix this" finding, shown only when drift exists.
4. **Charts** — the detailed breakdowns, grouped by the question they answer (below).

## Chart groups (the questions they answer)

- **By transform type — Total / Median / Count.** Total duration alone is misleading: a type can dominate
  the total simply because it runs thousands of times while each run is trivial. Pairing **total** with
  **median** (typical per-run cost) and **count** lets the reader tell "expensive because frequent" from
  "expensive per run." This is why median was added as a first-class chart rather than an average buried in
  a table.
- **By attribution — Tool/plugin / Source category / First-party module.** "Which transform type" is not
  actionable on its own; "which *plugin* and which *module/dependency*" is. `transformActionType` carries the
  owning plugin in its package (Android/AGP, Kotlin, kapt, Hilt/Dagger), and the
  `artifactTransformExecutionName` prefix carries the origin — a `project :path` module, a `group:name:version`
  dependency, or an unattributable bare file. These three charts turn "transforms are slow" into "AGP
  transforms of these dependencies / these modules are slow."
- **By artifact flow — Attribute transitions / Dependency / Slowest.** Where specific time goes:
  which `from → to` conversions, which dependency, and the individual slowest executions.
- **By cache footprint — Cache size by type / Heaviest cached transforms.** Storage cost of the cache,
  complementing the time view.
- **By build — Duration by build scan.** Only for aggregated runs; spots an outlier build.

## The pipeline graph

The most opinionated piece. Each transform converts one artifact type into another
(`changedAttributes: from → to`), and one transform's output type is another's input — so the set of
`from → to` edges *is* a directed graph of the build's artifact processing.

- Edges are aggregated per `(from, to)` with count and total duration.
- Nodes are laid out in **topological order** (Kahn's algorithm; cycle-safe), columned by **longest-path
  depth**, so the graph reads left→right in processing order.
- Arrow thickness encodes total duration, so the hot paths are visible at a glance.
- It's rendered as **inline SVG** (not a chart library) because this is a bespoke layout; nodes and edges
  carry `<title>` elements so hovering reveals full names and `from → to · N transforms · duration`.

Rationale: the raw transition list is hard to reason about; the graph makes the *shape* of the pipeline and
its bottlenecks legible.

## Interaction & legibility decisions

- **Truncate, but never lose information.** Long category-axis labels are truncated with an ellipsis so
  charts stay readable, and the **full label is always available on hover** (Chart.js tooltip title; SVG
  `<title>` on graph nodes/edges). We never show a label that's silently clipped by the canvas edge.
- **Horizontal bars for long labels** (dependencies, transitions, modules, slowest), vertical bars for
  short categorical labels (types, providers). Keeps text readable without rotation gymnastics.
- **Dark theme, card grid.** Functional, not decorative: high-contrast on a dark background for long
  reading sessions, responsive `auto-fit` grid so it works at any width.

## Things we deliberately removed

- **Avoidance-outcome pie/doughnut** and the **"avoidable miss duration" summary stat** were removed on
  review: the pie added little over the bar breakdowns, and the avoidable-miss stat read as a confusing
  "0ms" on the common case (caching not enabled / fully cached), where it's not actionable.

## Known limitations that shaped the design

- **Three useful fields are unavailable.** `cacheKey`, `nonCacheabilityCategory`, and
  `nonCacheabilityReason` are returned by the endpoint but not modeled by the `geapi-data` dependency, so
  the report can't yet explain *why* a transform wasn't cached. When geapi-data exposes them, a
  "non-cacheability reasons" breakdown is the natural next chart.
- **`avoidanceSavings` / `cacheArtifactSize` are conditional** (present only on avoided/cached records),
  so cache-size aggregations guard against nulls.
- **The "Unattributed file" bucket is irreducible.** Executions whose `artifactTransformExecutionName`
  carries neither a `project :` path nor a GAV (bare `classes.jar`, `R.jar`, SDK/runtime jars) cannot be
  attributed to a module or dependency from any field — so we surface them honestly as their own category
  rather than guessing.
