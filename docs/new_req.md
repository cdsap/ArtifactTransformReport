# Artifact Transform HTML Report — Analytics Specification

## Purpose

The Artifact Transform HTML report should help a developer understand what happened during artifact transform execution, where the cost is, how caching behaved, and which areas are actionable.

The report is not only a visualization layer. It should organize the available endpoint data into meaningful dimensions, aggregations, and derived insights so the user can move from:

> "Artifact transforms were slow"

to:

> "These transform types, dependencies, modules, or attribute transitions are responsible for most of the cost, and this is how cache behavior affected the result."

## Data source

The report is computed from the `gradle-artifact-transform-executions` endpoint.

The report should not require additional API calls. All metrics, charts, and tables must be derived from the artifact transform execution payload already available to the CLI.

The report must remain cheap to generate and safe to open later as a build artifact.

## Build scope: single-build vs multi-build reports

The report must support two scopes:

1. **Single-build report**
   - The data comes from one build scan.
   - The report explains what happened inside that build.
   - Build-level comparison charts should be hidden.
   - The focus is on transform types, dependencies, modules, attribute transitions, cache behavior, and slowest executions within that build.

2. **Multi-build report**
   - The data comes from multiple build scans.
   - The report explains both aggregate behavior and variation between builds.
   - Build-level comparison charts should be shown.
   - The report should help identify whether artifact transform cost is consistent across builds or caused by one or more outliers.

The same analytics model should be used for both scopes. Each artifact transform execution is still treated as one fact row, with `buildScanId` as an optional grouping dimension.

When only one build scan is present, build-level charts should not be rendered because they do not add useful information. When multiple build scans are present, the report should add a build-level analysis section showing:

- Total transform duration by build scan
- Transform count by build scan
- Cache hit rate by build scan
- Slowest transform per build
- Top transform type per build
- Optional outlier detection

This distinction keeps the report useful in both common workflows:

- A developer opens one CI build artifact and wants to understand why that build was slow.
- A user aggregates several builds and wants to understand trends, repeated costs, or outlier builds.

## Main user questions

The report should answer these questions:

1. **How much artifact transform work happened?**
   - Total transform executions
   - Number of build scans
   - Total execution duration
   - Total cache footprint

2. **Was caching effective?**
   - Cache hit rate
   - Avoided vs executed transforms
   - Cache artifact size
   - Avoidance savings, when available

3. **Where is the time going?**
   - By transform type
   - By plugin/tool owner
   - By dependency
   - By first-party project module
   - By attribute transition
   - By individual slowest execution

4. **Is the cost caused by frequency or by expensive individual transforms?**
   - Total duration
   - Median duration
   - Optional average duration
   - Optional p95/max duration
   - Count

5. **What does the artifact pipeline look like?**
   - Attribute `from → to` transitions
   - Hot edges by total duration
   - Frequently repeated conversions
   - Potential pipeline fragmentation

6. **Are there version or dependency fragmentation issues?**
   - Same dependency family transformed across multiple versions
   - Same transform type repeated across similar artifacts
   - Cost caused by dependency drift

7. **What can the user act on?**
   - Expensive transform types
   - Expensive dependencies
   - Expensive first-party modules
   - Repeated transitions
   - Cache misses or missing cache data
   - Version fragmentation

---

# Data model

## Raw execution fields

Each artifact transform execution should be treated as a fact row.

Useful raw fields include:

| Field | Purpose |
|---|---|
| `buildScanId` / build scan reference | Groups executions by build |
| `artifactTransformExecutionName` | Main source for module/dependency attribution |
| `transformActionType` | Used to infer transform type and owning plugin/tool |
| `changedAttributes` | Defines the artifact flow, usually `from → to` |
| `duration` | Main execution cost metric |
| `avoidanceOutcome` | Indicates whether work was executed, avoided, loaded from cache, or otherwise skipped |
| `avoidanceSavings` | Time saved when the transform was avoided, when available |
| `cacheArtifactSize` | Cache footprint, when available |
| `cacheKey` | Future use, once modeled by `geapi-data` |
| `nonCacheabilityCategory` | Future use, once modeled by `geapi-data` |
| `nonCacheabilityReason` | Future use, once modeled by `geapi-data` |

## Derived dimensions

The report should derive user-facing dimensions from the raw fields.

### Transform type

Derived from `transformActionType`.

Examples:

- `DexingTransform`
- `JetifyTransform`
- `AarTransform`
- `MergeJavaResTransform`
- `JdkImageTransform`

Purpose:

- Shows which transform classes dominate time, count, or cache size.
- Useful for identifying Android Gradle Plugin, Kotlin, kapt, Hilt/Dagger, Gradle, or custom plugin behavior.

### Tool / plugin owner

Derived from the package prefix of `transformActionType`.

Example mapping:

| Package / type pattern | Owner |
|---|---|
| `com.android.*` | Android Gradle Plugin |
| `org.jetbrains.kotlin.*` | Kotlin |
| `dagger.*`, `hilt.*` | Dagger / Hilt |
| `org.gradle.*` | Gradle |
| Unknown/custom package | Custom / Other |

Purpose:

- Helps users understand which ecosystem or plugin is responsible for the transform cost.
- More actionable than only showing the transform class name.

### Source category

Derived from `artifactTransformExecutionName`.

Suggested categories:

| Category | Detection |
|---|---|
| First-party project module | Contains a Gradle project path, for example `project :app` |
| External dependency | Contains a GAV-like coordinate, for example `group:name:version` |
| Unattributed file | Bare file or generated artifact without project/dependency identity |
| SDK / runtime artifact | Known SDK, JDK, Android runtime, or platform artifact |
| Unknown | Anything that cannot be confidently classified |

Purpose:

- Separates cost caused by application modules from cost caused by external dependencies or platform artifacts.
- Avoids guessing when attribution is not supported by the data.

### First-party module

Derived from project paths in `artifactTransformExecutionName`.

Examples:

- `:app`
- `:feature:login`
- `:core:network`

Purpose:

- Shows which internal modules are creating the most transform work.
- Useful for large Android builds with many modules.

### Dependency

Derived from GAV coordinates in `artifactTransformExecutionName`.

Examples:

- `com.squareup.okio:okio:3.9.0`
- `androidx.appcompat:appcompat:1.7.0`

Purpose:

- Shows which external dependencies create the most transform duration, transform count, or cache footprint.

### Dependency family

Derived from dependency coordinates without the version.

Example:

- Full dependency: `androidx.appcompat:appcompat:1.7.0`
- Family: `androidx.appcompat:appcompat`

Purpose:

- Enables version fragmentation detection.
- Helps identify the same dependency being transformed multiple times across different versions.

### Attribute transition

Derived from `changedAttributes`.

Examples:

- `aar → android-classes-jar`
- `jar → dex`
- `classes → instrumented-classes`

Purpose:

- Describes the artifact processing pipeline.
- Useful for identifying hot transformation paths.

### Build scan

Derived from build scan identity.

Purpose:

- Allows aggregated reports across multiple builds.
- Helps identify outlier builds when the report contains more than one build scan.

---

# Measures and aggregations

## Core measures

| Measure | Definition | Why it matters |
|---|---|---|
| Transform count | Number of transform executions | Shows scale and repetition |
| Total duration | Sum of execution duration | Shows total cost |
| Median duration | Median execution duration | Shows typical per-run cost |
| Average duration | Mean execution duration | Useful, but can be skewed by outliers |
| Max duration | Slowest single execution | Helps find extreme outliers |
| P95 duration | 95th percentile duration | Better outlier-aware performance signal |
| Cache hit count | Count of avoided or cache-loaded executions | Shows caching behavior |
| Cache miss count | Count of executed transforms that could have been avoided | Shows missed reuse opportunity |
| Cache hit rate | Cache hits / cacheable or cache-relevant executions | High-level cache effectiveness |
| Avoidance savings | Sum of avoided execution time, when available | Shows time saved by caching |
| Cache artifact size | Sum of cache artifact sizes, when available | Shows storage/network footprint |

## Recommended aggregation groups

### By transform type

Aggregations:

- Count
- Total duration
- Median duration
- P95 duration
- Cache hit rate
- Cache size

Use case:

- Distinguish frequent cheap transforms from individually expensive transforms.

### By plugin/tool owner

Aggregations:

- Count
- Total duration
- Median duration
- Cache size

Use case:

- Explain whether cost is mostly from AGP, Kotlin, Gradle, Dagger/Hilt, or custom plugins.

### By source category

Aggregations:

- Count
- Total duration
- Cache hit rate
- Cache size

Use case:

- Separate first-party module cost from external dependency cost.

### By first-party module

Aggregations:

- Count
- Total duration
- Median duration
- Slowest transform
- Top transform types for that module

Use case:

- Identify internal modules that create heavy artifact transform work.

### By dependency

Aggregations:

- Count
- Total duration
- Median duration
- Cache size
- Attribute transitions

Use case:

- Identify external dependencies that are expensive to transform.

### By dependency family

Aggregations:

- Number of versions
- Versions detected
- Total duration across versions
- Count across versions
- Cache size across versions
- Most expensive version

Use case:

- Detect version fragmentation and dependency drift.

### By attribute transition

Aggregations:

- Count
- Total duration
- Median duration
- P95 duration

Use case:

- Identify hot paths in the artifact processing pipeline.

### By build scan

Only useful when the report contains multiple build scans.

Aggregations:

- Total duration
- Transform count
- Cache hit rate
- Slowest transform
- Top transform type
- Top dependency
- Top first-party module

Use case:

- Helps identify outlier builds.
- Useful when comparing local builds, CI builds, branches, or repeated executions.

---

# Report sections

## 1. Summary

The summary should show the smallest set of high-value numbers:

- Total artifact transforms
- Number of build scans
- Total transform duration
- Cache hit rate
- Total cache artifact size
- Optional: total avoidance savings, only when meaningful

Rules:

- Do not show a metric if the underlying data is unavailable or misleading.
- Avoid showing `0ms` for unavailable avoidance savings.
- Prefer absence over noisy or confusing cards.

## 2. Pipeline graph

The pipeline graph should visualize `changedAttributes` as a directed graph.

Each edge is:

```text
from attribute → to attribute
```

Edges should be aggregated by:

- Count
- Total duration
- Median duration

Visual encoding:

- Node: artifact attribute/type
- Edge: transition
- Edge thickness: total duration
- Tooltip: full transition, count, total duration, median duration

Purpose:

- Shows the structure of the artifact processing pipeline.
- Makes repeated or expensive conversion paths visible.
- Helps users understand how artifacts flow through the build.

Implementation notes:

- Nodes should be laid out in topological order where possible.
- The layout should be cycle-safe.
- Column placement can use longest-path depth.
- The graph should be rendered as inline SVG.
- Nodes and edges should include `<title>` elements for hover details.

## 3. Version fragmentation

This section should appear only when multiple versions of the same dependency family are detected.

Group by:

```text
group:name
```

Show:

- Versions detected
- Total transform count
- Total duration
- Cache size
- Most expensive version

Purpose:

- Makes dependency drift visible.
- Helps users identify cases where alignment or dependency cleanup may reduce repeated transform work.

## 4. Transform type analysis

Charts:

- Total duration by transform type
- Median duration by transform type
- Count by transform type
- Optional: P95 duration by transform type

Purpose:

- Total duration shows overall cost.
- Median duration shows typical cost.
- Count shows repetition.
- P95 shows expensive tail behavior.

This avoids the common mistake of treating high total duration as meaning "each execution is expensive."

## 5. Attribution analysis

Charts:

- Duration by plugin/tool owner
- Duration by source category
- Duration by first-party module
- Duration by dependency

Purpose:

- Makes the report actionable.
- Moves from technical transform names to ownership and origin.

Example insight:

```text
Most transform time is coming from AGP transforms of external dependencies, not from first-party modules.
```

## 6. Artifact flow analysis

Charts:

- Duration by attribute transition
- Count by attribute transition
- Slowest individual executions

Purpose:

- Shows which conversions dominate the artifact pipeline.
- Helps users identify repeated expensive transformations.

## 7. Cache footprint analysis

Charts:

- Cache size by transform type
- Heaviest cached transforms
- Cache size by dependency
- Cache size by source category

Purpose:

- Complements the duration view.
- Helps explain storage and network cost.

Important:

- Cache size fields are conditional.
- Only show this section when cache artifact size data exists.

## 8. Build-level analysis

This section should only appear when the report includes multiple build scans.

Charts:

- Total transform duration by build scan
- Transform count by build scan
- Cache hit rate by build scan
- Slowest transform per build
- Top transform type per build

Purpose:

- Identify outlier builds.
- Compare artifact transform cost across local builds, CI builds, branches, or repeated executions.
- Distinguish stable transform cost from build-specific spikes.

If the report contains only one build scan, this section should be skipped. In that case, the report should focus entirely on the internal breakdown of that build.

---

# Insight generation

The report should include simple derived findings when possible.

## Expensive because frequent

Condition:

- High total duration
- High count
- Low or moderate median duration

Message:

```text
This transform type dominates total time mostly because it runs many times, not because each execution is individually slow.
```

## Expensive per execution

Condition:

- High median or P95 duration
- Moderate or low count

Message:

```text
This transform type is expensive per execution and may be worth investigating directly.
```

## Cache opportunity

Condition:

- High total duration
- Low cache hit rate
- Cache-related data available

Message:

```text
This area has high transform cost and low cache reuse.
```

## Version fragmentation

Condition:

- Same dependency family appears with multiple versions

Message:

```text
Multiple versions of this dependency are being transformed. Dependency alignment may reduce repeated work.
```

## Outlier build

Condition:

- Multi-build report
- One build scan has significantly higher transform duration than the others

Message:

```text
This build has unusually high artifact transform cost compared with the other builds in the report.
```

---

# Filtering and sorting behavior

## Default sorting

Use descending total duration as the default sort for most charts and tables.

Reason:

- The first view should focus on the biggest cost.

## Secondary sorting

When total duration is equal or similar, sort by:

1. Count
2. Median duration
3. Name

## Top-N behavior

For readability:

- Show top 10 or top 15 entries in charts.
- Group the rest as `Other` only when it does not hide important detail.
- Tables may show more rows than charts.

## Empty data behavior

Do not render empty charts.

Examples:

- No cache size data, skip cache footprint charts.
- Single build scan, skip build-level comparison.
- No dependency versions repeated, skip version fragmentation.
- No avoidable misses, skip avoidable miss breakdown.

---

# Presentation rules

## Labels

Long labels should be truncated visually but preserved in tooltips.

Rules:

- Chart labels may be shortened.
- Tooltip title must show the full label.
- SVG nodes and edges must include `<title>` elements.
- Never silently clip labels at the canvas edge.

## Chart orientation

Use horizontal bars for long labels:

- Dependencies
- Modules
- Attribute transitions
- Slowest executions

Use vertical bars for short labels:

- Transform types
- Plugin/tool owners
- Source categories

## Units

Use human-readable units:

- Durations: `ms`, `s`, `min`
- Sizes: `KB`, `MB`, `GB`
- Percentages: one decimal place when useful
- Counts: integer values

## Theme and packaging

The report should remain self-contained and offline.

Requirements:

- Single HTML file
- Inline Chart.js from bundled resource
- Inline JSON data
- Inline SVG for the pipeline graph
- No CDN
- No external requests

This is important because reports are often produced in CI and opened later as build artifacts, sometimes on machines without network access or behind firewalls.

---

# Current limitations

## Missing cache explainability fields

The endpoint returns these fields, but they are not currently modeled by `geapi-data`:

- `cacheKey`
- `nonCacheabilityCategory`
- `nonCacheabilityReason`

Because of this, the report cannot yet explain why a transform was not cacheable.

Future section once available:

- Non-cacheability reasons by transform type
- Non-cacheability reasons by plugin/tool
- Non-cacheability reasons by dependency/module
- Top non-cacheable transform executions

## Conditional cache fields

Fields such as `avoidanceSavings` and `cacheArtifactSize` are only present for some records.

The report must guard against nulls and avoid presenting missing data as zero.

## Attribution limits

Some executions cannot be confidently attributed to a project module or dependency.

Examples:

- Bare `classes.jar`
- `R.jar`
- SDK/runtime jars
- Generated intermediate files

These should be shown as `Unattributed file` or `SDK/runtime artifact`.

The report should not guess attribution when the data does not support it.

---

# Implementation guidance

## Library responsibilities

The `:library` module should own all analytics and derived calculations.

Examples:

- Grouping by transform type
- Grouping by dependency
- Parsing project paths
- Parsing dependency coordinates
- Computing medians and percentiles
- Computing cache hit rates
- Building pipeline graph data
- Detecting version fragmentation
- Generating insight candidates

The library should expose pure, unit-tested functions.

## CLI / HTML responsibilities

The CLI output layer should only render already-computed results.

Responsibilities:

- Convert analytics results into chart specs
- Render cards
- Render tables
- Render inline SVG
- Embed JSON data
- Skip empty sections
- Apply truncation and tooltip behavior

The HTML output should not contain business logic beyond presentation-specific formatting.

---

# Suggested implementation phases

## Phase 1 — Core report

- Summary stats
- Transform type charts
- Source category charts
- Dependency and module attribution
- Slowest executions
- Cache size charts
- Multi-build scan chart when applicable

## Phase 2 — Pipeline view

- Attribute transition aggregation
- Inline SVG graph
- Edge thickness by total duration
- Node/edge tooltips
- Cycle-safe layout

## Phase 3 — Insight layer

- Expensive because frequent
- Expensive per execution
- Cache opportunity
- Version fragmentation
- Outlier build detection

## Phase 4 — Future cache explainability

After `geapi-data` exposes the missing fields:

- Non-cacheability reason breakdown
- Non-cacheability category charts
- Cache key grouping
- Better cache miss explanations

---

# Success criteria

The report is successful if a user can answer these questions without extra tooling:

1. What was the artifact transform cost?
2. Which transform types dominated the cost?
3. Was the cost caused by frequency or expensive individual transforms?
4. Which plugins, modules, dependencies, or attribute transitions were responsible?
5. Was caching effective?
6. Which cached transforms consumed the most storage?
7. Is there dependency version fragmentation?
8. If this is a multi-build report, which builds are outliers?
9. What should I investigate first?

The final goal is to make artifact transform behavior visible, explainable, and actionable from a single offline HTML artifact.
