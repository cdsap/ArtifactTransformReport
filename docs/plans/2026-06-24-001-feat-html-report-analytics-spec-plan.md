---
title: "feat: Complete the Artifact Transform HTML report analytics spec"
status: completed
date: 2026-06-24
origin: docs/new_req.md
type: feat
depth: deep
branch: tier1-analytics
---

# feat: Complete the Artifact Transform HTML report analytics spec

## Summary

`docs/new_req.md` specifies the full analytics model for the Artifact Transform HTML report. The
branch `tier1-analytics` already implements most of the spec's Phase 1 and Phase 2 (summary stats,
transform-type total/median/count, tool/plugin + source-category + module attribution,
dependency/transition/slowest charts, cache-size charts, version-fragmentation table, the inferred
pipeline SVG DAG, the per-build-scan chart, label truncation + hover, and offline-inlined Chart.js).

This plan closes the remaining delta: the extra distribution measures (average, p95, max), a real
dependency-*coordinate* and dependency-*family* model, expanded source categories (SDK/runtime,
Unknown), cache footprint by dependency and source category, the full multi-build build-level analysis
section with outlier detection, human-readable units, top-N + "Other" bucketing, and the derived
insight layer. The cache-explainability fields (`cacheKey`, `nonCacheabilityReason`,
`nonCacheabilityCategory`) remain blocked on the `geapi-data` dependency and stay deferred.

---

## Problem Frame

Today the report answers "where is the cost" reasonably well but has gaps against the spec's success
criteria:

- It can't yet distinguish *frequency-driven* cost from *expensive-per-execution* cost beyond median
  (no p95/max/average), so question 3 of the spec ("frequency or expensive individual transforms?") is
  only partially answerable.
- Dependencies are surfaced via the raw `artifactTransformExecutionName` prefix (mixing GAVs and module
  paths) and the fragmentation table is a flat version list, not a per-family aggregation — so question
  7 ("version fragmentation") is shallow.
- There is no SDK/runtime category, so platform-artifact cost is mislabeled as "Unattributed file".
- Multi-build reports only show duration/count by build scan; there's no hit-rate-by-build, per-build
  slowest/top-type, or outlier detection — so question 8 ("which builds are outliers?") is unanswered.
- There is no derived insight layer, so question 9 ("what should I investigate first?") relies entirely
  on the reader interpreting charts.

The work is bounded by an explicit spec and an existing, well-factored codebase. It is mechanical
analytics plus one opinionated layer (insights), not architectural change.

---

## Requirements traceability

All requirement references below point to sections of `docs/new_req.md` (the origin spec).

| Req | Spec section | Status | Addressed by |
|---|---|---|---|
| R1 — distribution measures (avg, p95, max) | Measures and aggregations | gap | U1, U2, U3, U5 |
| R2 — dependency coordinate + per-dependency aggregations | Derived dimensions → Dependency | gap | U4 |
| R3 — dependency family + fragmentation aggregation | Derived dimensions → Dependency family; §3 | partial | U5 |
| R4 — source category incl. SDK/runtime + Unknown | Derived dimensions → Source category | partial | U6 |
| R5 — cache footprint by dependency and source category | §7 Cache footprint analysis | gap | U7 |
| R6 — build-level analysis + outlier detection | §8 Build-level analysis | gap | U8, U9 |
| R7 — human-readable units | Presentation rules → Units | gap | U10 |
| R8 — top-N + "Other" bucketing | Filtering and sorting → Top-N | gap | U11 |
| R9 — total duration / optional avoidance savings summary | §1 Summary | partial | U11 |
| R10 — derived insight layer | §Insight generation | gap | U12, U13 |
| R11 — pipeline edge median in tooltip | §2 Pipeline graph | partial | U3 (edge measures) |
| (done) summary, type total/median/count, attribution, transitions, slowest, cache size, fragmentation list, pipeline DAG, per-build chart, truncation/hover, offline packaging | Phases 1–2 | done | existing branch work |

Deferred (blocked upstream): non-cacheability reason/category/cacheKey breakdowns — origin "Current
limitations → Missing cache explainability fields" and "Suggested implementation phases → Phase 4".

---

## Key Technical Decisions

- **KTD1 — Distribution measures via a single generic helper.** Add one internal
  `List<Int>.percentile(p)` / `.average()` / `.max()` set and a small reusable per-group stat shape
  rather than bespoke functions per measure per group. The existing `median()` helper is the precedent.
  Rationale: the spec lists the same measure family across many groups; one helper keeps it DRY and
  unit-tested once. (User decision: add avg + p95 + max everywhere the spec lists them.)
- **KTD2 — Dependency identity comes from the execName prefix, not the filename.** Per the earlier
  investigation, `artifactTransformExecutionName`'s prefix is the GAV for external deps and
  `project :path` for modules; `inputArtifactName` is just the bare file. The new `dependencyCoordinate()`
  parses the GAV from the prefix; the legacy `parsedInputArtifact()` (filename-based) is kept only for
  the family/version regex. Rationale: matches the spec's "Dependency derived from GAV coordinates in
  artifactTransformExecutionName" and avoids collapsing modules into `classes.jar`.
- **KTD3 — Dependency family = `group:name` (version stripped from the coordinate).** Family
  aggregation replaces the current filename-based `librariesWithMultipleVersions` for the report table;
  the table becomes per-family (versions, count, duration, cache size, most expensive version).
- **KTD4 — SDK/runtime detection is a conservative allowlist.** Match a small known set (`android.jar`,
  `gradle-api-*`, `kotlin-stdlib`/`kotlin-compiler` runtime jars, JDK/`rt.jar`/jdk-image inputs). Anything
  unmatched stays `Unattributed file`; `Unknown` is reserved for sources that match no rule at all.
  Rationale: honors the spec's "do not guess attribution" rule. (User decision.)
- **KTD5 — Units stay numeric in chart data; formatting is presentation-only.** Chart.js needs numeric
  axes, so library functions keep returning `Int` ms / bytes. Human-readable units (s/min, KB/MB/GB) are
  applied in `HtmlOutput` via axis-tick and tooltip callbacks plus the existing `formatBytes`. Rationale:
  keeps the library pure and avoids lossy pre-formatting.
- **KTD6 — Insights live in their own library file and emit data, not strings-with-logic.** A new
  `Insights.kt` exposes pure functions returning a `List<Finding>` (typed: kind, severity, subject,
  message, evidence). `HtmlOutput` only renders them. Rationale: spec's implementation guidance
  ("library owns analytics … generating insight candidates"; "CLI only renders").
- **KTD7 — Single-vs-multi scope is one boolean derived from distinct `buildScanId` count.** Build-level
  section and per-build charts render only when `> 1` build scan. Rationale: spec's build-scope rules;
  generalizes the existing `byBuildScan` `size > 1` guard.
- **KTD8 — Scope is the HTML report only.** Console/txt `*View` parity for the new analytics is deferred
  to follow-up; the spec document is titled "HTML Report" and the library functions are reusable if we
  later want console parity.

---

## High-Level Technical Design

Layering (unchanged separation, new pieces in **bold**):

```text
:library  (pure, unit-tested)
  ArtifactTransforms.kt   existing aggregations
    + percentile/average/max helpers            (U1)
    + per-group extended measures               (U2,U3)
    + dependencyCoordinate / by-dependency       (U4)
    + dependencyFamilies (group:name agg)         (U5)
    + sourceCategory incl. SDK/runtime + Unknown  (U6)
    + cacheSizeByDependency / bySourceCategory    (U7)
    + per-build analytics + outlier detection     (U8)
    + topNWithOther bucketing helper              (U11)
  Insights.kt  (new)
    + Finding model + rules                       (U12)
        |
        v   (results only — no logic crosses down)
:cli
  HtmlOutput.kt
    + new charts/sections, units formatting,
      build-level section, findings section       (U2,U3,U4,U5,U6,U7,U9,U10,U11,U13)
```

Report section order in the rendered HTML (spec §"Report sections"), new/changed marked:

```text
Summary stats           (+ total duration, optional savings)      U11
Findings / Insights     (new — surfaced near the top)             U13
Pipeline graph          (edge tooltip gains median)               U3
Version fragmentation   (now per dependency-family)               U5
Transform type          (+ avg/p95/max charts)                    U2
Attribution             (source category gains SDK/runtime)       U6
Artifact flow           (transition + avg/p95/max)                U3
Cache footprint         (+ by dependency, by source category)     U7
Build-level analysis    (multi-build only; outliers)              U8,U9
```

---

## Implementation Units

> All units preserve the library-computes / CLI-renders split and add unit tests in
> `library/src/test/java/io/github/cdsap/artifacttransform/ArtifactTransformTests.kt`
> (and a new `InsightsTests.kt` for U12). Files are repo-relative.

### U1. Distribution-measure helpers

**Goal:** Provide reusable `average`, `p95`, and `max` over `List<Int>` durations, alongside the
existing `median`.
**Requirements:** R1.
**Dependencies:** none.
**Files:**
- `library/src/main/java/io/github/cdsap/artifacttransform/ArtifactTransforms.kt`
- `library/src/test/java/io/github/cdsap/artifacttransform/ArtifactTransformTests.kt`
**Approach:** Add `internal fun List<Int>.percentile(p: Int): Int` (nearest-rank on sorted values,
empty → 0), `internal fun List<Int>.averageInt(): Int`, `internal fun List<Int>.maxOrZero(): Int`.
Mirror the existing `median()` style and `toMillisOrZero()` null-safety.
**Patterns to follow:** existing `median()` in `ArtifactTransforms.kt`.
**Test scenarios:**
- p95 of a known 20-element list returns the nearest-rank element; p95 of single element returns it.
- p95/average/max of empty list return 0.
- average rounds as documented (integer ms); max returns the largest.

### U2. Extended measures by transform type + charts

**Goal:** Add average, p95, max duration by transform type and render them.
**Requirements:** R1.
**Dependencies:** U1.
**Files:**
- `library/.../ArtifactTransforms.kt`, `library/.../ArtifactTransformTests.kt`
- `cli/src/main/java/io/github/cdsap/artifacttransform/cli/output/HtmlOutput.kt`
**Approach:** Add `averageDurationByTransformActionType`, `p95DurationByTransformActionType`,
`maxDurationByTransformActionType` (same grouping shape as `medianDurationByTransformActionType`). In
`HtmlOutput.buildCharts`, add "P95 duration by transform type" and "Max duration by transform type"
charts next to the existing total/median/count; add average where it reads as a distinct signal.
**Patterns to follow:** `medianDurationByTransformActionType` + its chart spec block.
**Test scenarios:**
- For the existing `sampleTransforms` fixture, p95/avg/max per type match hand-computed values.
- Empty input → empty list (no chart rendered via the existing `addSpec` empty-skip).

### U3. Extended measures + median for attribute transitions and pipeline edges

**Goal:** Add count/total already exist; add median + p95 (+ max) by attribute transition, and surface
median per edge in the pipeline tooltip.
**Requirements:** R1, R11.
**Dependencies:** U1.
**Files:** `library/.../ArtifactTransforms.kt`, `library/.../ArtifactTransformTests.kt`,
`cli/.../HtmlOutput.kt`.
**Approach:** Add `medianDurationByAttributeTransition` / `p95DurationByAttributeTransition`. Extend
`TransitionEdge` (or a parallel computation) with `medianDuration` so the SVG edge `<title>` shows
`from → to · N transforms · total · median`. Add a transition median/p95 chart.
**Patterns to follow:** `durationByAttributeTransition`, `attributeTransitionEdges`, the edge-title
builder in `HtmlOutput.pipelineSvg`.
**Test scenarios:**
- median/p95 by transition for a fixture with a multi-execution transition.
- `TransitionEdge.medianDuration` correct for an edge aggregating ≥3 executions.
- edge title string includes the median segment.

### U4. Dependency coordinate (GAV) model + per-dependency aggregations

**Goal:** Derive the dependency GAV from the execName prefix and aggregate by it.
**Requirements:** R2.
**Dependencies:** none (parallel with U1).
**Files:** `library/.../ArtifactTransforms.kt`, `library/.../ArtifactTransformTests.kt`,
`cli/.../HtmlOutput.kt`.
**Approach:** Add `ArtifactTransform.dependencyCoordinate(): String?` returning the execName prefix when
it is GAV-shaped (`group:name:version`, i.e. `sourceCategory() == "External dependency"`), else null.
Add `durationByDependency`, `countByDependency`, `medianDurationByDependency`,
`cacheSizeByDependency` (all over non-null coordinates). Add a "Duration by dependency (coordinate)"
chart; keep the existing execName-prefix "Duration by dependency" or replace it — decide during
implementation to avoid duplication (see Deferred note on dedupe).
**Patterns to follow:** `transformSource()`, `sourceCategory()`, `durationByModule()`.
**Test scenarios:**
- GAV prefix → coordinate parsed; module prefix and bare file → null.
- `durationByDependency` sums per coordinate and sorts desc.
- `cacheSizeByDependency` skips null `cacheArtifactSize` (guard like the existing cache functions).

### U5. Dependency-family aggregation + fragmentation table

**Goal:** Replace the flat version list with a per-family (`group:name`) aggregation.
**Requirements:** R1, R3.
**Dependencies:** U4.
**Files:** `library/.../ArtifactTransforms.kt`, `library/.../ArtifactTransformTests.kt`,
`cli/.../HtmlOutput.kt`.
**Approach:** Add `data class DependencyFamily(family, versions, count, totalDuration, cacheSize,
mostExpensiveVersion)` and `dependencyFamilies(): List<DependencyFamily>` keyed on `group:name` from the
coordinate (variant `-api`/`-runtime` normalized as today). Render the version-fragmentation section as
a table with columns: family, versions, count, total duration, cache size, most expensive version —
shown only when at least one family has >1 version.
**Patterns to follow:** current `librariesWithMultipleVersions` + `versionFragmentationSection()`.
**Test scenarios:**
- family groups two coordinates of the same `group:name`; `versions` lists both; `mostExpensiveVersion`
  is the one with the highest total duration.
- a family with a single version is excluded from the fragmentation table but present in `dependencyFamilies`.
- variant-only differences (`-api`/`-runtime`) collapse to one version (no false fragmentation).

### U6. Expand source categories: SDK/runtime + Unknown

**Goal:** Add the SDK/runtime category (conservative allowlist) and an explicit Unknown bucket.
**Requirements:** R4.
**Dependencies:** none.
**Files:** `library/.../ArtifactTransforms.kt`, `library/.../ArtifactTransformTests.kt`,
`cli/.../HtmlOutput.kt`.
**Approach:** Extend `sourceCategory()` ordering: module → external dependency → **SDK/runtime**
(allowlist match on the source/inputArtifactName: `android.jar`, `gradle-api-*`, `kotlin-stdlib*`,
`kotlin-compiler*` runtime, `rt.jar`/jdk-image) → unattributed file → **Unknown** (matched no rule).
Update `durationBySourceCategory` consumers automatically (it groups by the function). No new chart
needed — the existing source-category chart picks up the new buckets.
**Patterns to follow:** existing `sourceCategory()` `when` block + its test.
**Test scenarios:**
- `android.jar`, `gradle-api-9.5.1.jar`, `kotlin-stdlib-2.0.jar` (as a runtime input) → "SDK/runtime".
- a bare `classes.jar` stays "Unattributed file"; a module/GAV unchanged.
- a source matching no rule → "Unknown".
- regression: the existing `durationBySourceCategory` test still passes with categories added.

### U7. Cache footprint by dependency and source category

**Goal:** Add the two cache-footprint breakdowns the spec lists.
**Requirements:** R5.
**Dependencies:** U4, U6.
**Files:** `library/.../ArtifactTransforms.kt`, `library/.../ArtifactTransformTests.kt`,
`cli/.../HtmlOutput.kt`.
**Approach:** Add `cacheSizeBySourceCategory` and reuse `cacheSizeByDependency` (U4). Add charts under
the cache-footprint group; the whole group already only renders when cache-size data exists (extend the
existing empty-skip behavior to these).
**Patterns to follow:** `aggregatedCacheSizeByTransformActionType`, `cacheSizeByType` chart.
**Test scenarios:**
- cache size summed per source category, nulls skipped.
- when no record has `cacheArtifactSize`, both charts are skipped (no empty cards).

### U8. Per-build analytics + outlier detection

**Goal:** Compute the build-level measures and an outlier flag.
**Requirements:** R6.
**Dependencies:** U1 (for thresholds), existing per-build functions.
**Files:** `library/.../ArtifactTransforms.kt`, `library/.../ArtifactTransformTests.kt`.
**Approach:** Add `cacheHitRateByBuildScan`, `slowestTransformByBuildScan`, `topTransformTypeByBuildScan`,
and `outlierBuildScans()` (a build is an outlier when its total transform duration exceeds, e.g., median
+ k·IQR or a documented multiplier of the median across builds — fix the rule in implementation, default
documented in code). All keyed on `buildScanId`.
**Patterns to follow:** `durationByBuildScan`, `overallCacheHitRate`.
**Test scenarios:**
- hit rate per build correct for a 2-build fixture with mixed avoidance outcomes.
- slowest transform / top type per build correct.
- outlier detection flags a build whose duration is far above the others and flags none when builds are
  uniform.

### U9. Build-level analysis section (multi-build only)

**Goal:** Render the build-level section, hidden for single-build reports.
**Requirements:** R6.
**Dependencies:** U8, U7 (hit-rate chart), U10 (units).
**Files:** `cli/.../HtmlOutput.kt`.
**Approach:** Introduce an `isMultiBuild` boolean (distinct `buildScanId` > 1). Add a build-level section
with: total duration by build (exists), count by build (exists), cache hit rate by build, plus a small
table of per-build slowest transform + top transform type, and an outlier call-out. Skip the whole
section when single-build.
**Patterns to follow:** `versionFragmentationSection()` / `pipelineSection()` conditional-section pattern.
**Test scenarios:** none — pure presentation. `Test expectation: none -- rendering only; logic covered by U8.`
**Verification:** single-build report shows no build-level section; a ≥2 build run shows hit-rate-by-build
and the per-build table; an injected outlier build is called out.

### U10. Human-readable units

**Goal:** Show durations as ms/s/min and sizes as KB/MB/GB in charts and tooltips.
**Requirements:** R7.
**Dependencies:** none.
**Files:** `cli/.../HtmlOutput.kt`.
**Approach:** Add a JS duration formatter mirroring the Kotlin `formatBytes`, wire it into Chart.js axis
`ticks.callback` and tooltip `label` callbacks for duration and size charts. Keep numeric values in the
data (KTD5). Percentages to one decimal (already done for hit rate).
**Patterns to follow:** the existing tooltip/`trunc` callback block and `formatBytes` in `HtmlOutput`.
**Test scenarios:** none — presentation only. `Test expectation: none -- Chart.js formatting; verified visually.`
**Verification:** a large aggregate duration axis reads `1.2 min` not `72000`; cache axis reads `MB`;
tooltips show formatted values; numeric sorting unchanged.

### U11. Top-N + "Other" bucketing and summary completeness

**Goal:** Add an "Other" rollup for long-tail categories and complete the summary stats.
**Requirements:** R8, R9.
**Dependencies:** none.
**Files:** `library/.../ArtifactTransforms.kt`, `library/.../ArtifactTransformTests.kt`,
`cli/.../HtmlOutput.kt`.
**Approach:** Add `List<Pair<String,Int>>.topNWithOther(n)` that keeps the top n and sums the remainder
into an `"Other"` entry (only when the remainder is non-empty and doesn't hide a near-top value — keep
the rule simple and documented). Apply to high-cardinality charts (type, dependency, module,
transition). Add a "Total transform duration" summary stat (sum of `duration`), and render "Total
avoidance savings" only when the summed savings is meaningful (>0).
**Patterns to follow:** the `.take(10)` chart blocks; the summary stat cards in `render()`.
**Test scenarios:**
- `topNWithOther(2)` on 5 entries keeps 2 and produces an `Other` equal to the sum of the rest.
- no `Other` entry when entries ≤ n.
- total-duration sum correct; avoidance-savings stat omitted when total is 0 (guard against the
  "don't show 0ms" rule).

### U12. Insight generation (library)

**Goal:** Emit derived findings as typed data.
**Requirements:** R10.
**Dependencies:** U1, U2, U5, U8.
**Files:**
- `library/src/main/java/io/github/cdsap/artifacttransform/Insights.kt` (new)
- `library/src/test/java/io/github/cdsap/artifacttransform/InsightsTests.kt` (new)
**Approach:** Define `enum FindingKind { EXPENSIVE_BECAUSE_FREQUENT, EXPENSIVE_PER_EXECUTION,
CACHE_OPPORTUNITY, VERSION_FRAGMENTATION, OUTLIER_BUILD }`, `data class Finding(kind, severity, subject,
message, evidenceMs/value)`, and `List<ArtifactTransform>.findings(): List<Finding>` applying the spec's
conditions:
- expensive-because-frequent: high total + high count + low/moderate median (thresholds relative to the
  dataset, e.g. top-quartile total & count, below-median per-run);
- expensive-per-execution: high median or p95 + moderate/low count;
- cache-opportunity: high total + low hit rate (where cache data exists);
- version-fragmentation: any family with >1 version (from U5);
- outlier-build: multi-build + a build flagged by U8.
Thresholds are documented constants; findings sorted by severity then evidence.
**Patterns to follow:** the pure-extension style of `ArtifactTransforms.kt`; reuse U1/U2/U5/U8 outputs
rather than recomputing.
**Test scenarios:**
- a fixture with one frequent-cheap type yields exactly an EXPENSIVE_BECAUSE_FREQUENT finding, not
  EXPENSIVE_PER_EXECUTION.
- a fixture with a rare-but-slow type yields EXPENSIVE_PER_EXECUTION.
- low hit rate + high total yields CACHE_OPPORTUNITY; high hit rate yields none.
- a multi-version family yields VERSION_FRAGMENTATION.
- single-build input never yields OUTLIER_BUILD; a multi-build outlier does.
- empty input yields no findings (no exceptions).

### U13. Findings section (HTML)

**Goal:** Render findings near the top of the report.
**Requirements:** R10.
**Dependencies:** U12.
**Files:** `cli/.../HtmlOutput.kt`.
**Approach:** Add a `findingsSection()` rendering each `Finding` as a card/row (severity color, subject,
message), placed after the summary stats. Skip the section when there are no findings.
**Patterns to follow:** the stat-card and `versionFragmentationSection()` markup/styling.
**Test scenarios:** none — presentation only. `Test expectation: none -- rendering; logic covered by U12.`
**Verification:** a build with a clear frequent-cheap type and low hit rate shows the corresponding
findings; a clean/fully-cached single build shows no findings section.

---

## Scope Boundaries

### In scope
- All analytics gaps listed in Requirements traceability (U1–U13), rendered in the **HTML report**.
- Unit tests for every feature-bearing library function.

### Deferred to Follow-Up Work
- **Console/txt `*View` parity** for the new analytics (the spec is HTML-focused; library functions are
  reusable later). 
- **Dependency-chart dedupe**: the existing execName-prefix "Duration by dependency" chart vs. the new
  coordinate-based one (U4) — consolidate during U4 implementation; if non-trivial, land U4's chart and
  remove the old one in a follow-up.
- **CSV multi-attribute fix**: `CsvOutput` still reads `changedAttributes[0]` only; honest multi-attribute
  CSV output is tracked separately (overlaps the original code-review list).

### Outside this product's identity (blocked upstream)
- **Cache explainability** (`nonCacheabilityReason`/`Category`, `cacheKey` grouping): not modeled by
  `io.github.cdsap:geapi-data`; requires a geapi-data release. Origin Phase 4. Tracked, not planned here.

---

## Risks & Dependencies

- **R-A — Threshold tuning for insights (U12).** Dataset-relative thresholds can produce noisy or empty
  findings on small builds. Mitigation: keep thresholds conservative, require both total and count
  signals, and unit-test the boundaries; treat absence of findings as acceptable (spec: prefer absence
  over noise).
- **R-B — SDK/runtime allowlist drift (U6).** The allowlist will miss some platform artifacts. Accepted:
  conservative-by-design per the spec's "don't guess" rule; unmatched inputs stay "Unattributed file".
- **R-C — Outlier rule choice (U8).** A naive multiplier mislabels small-sample builds. Mitigation:
  require ≥3 builds before flagging and document the rule; unit-test uniform vs. spiked inputs.
- **R-D — Chart proliferation.** Adding avg/p95/max everywhere risks a cluttered report. Mitigation:
  group related measures in the same section; rely on the existing empty-skip; revisit chart count after
  U2/U3 against a real multi-build example.
- **Dependency:** all rendering units depend on the existing offline-inlined Chart.js packaging and the
  `addSpec` empty-skip already on the branch.

---

## Verification Strategy

- `./gradlew test` green after each unit (library tests are the primary gate; CI runs `test`).
- `./gradlew :cli:fatBinary` builds; the binary regenerates the example reports against
  `ge.solutions-team.gradle.com` for the single build `r4nwadz72jivm` (45% cached — exercises hit-rate,
  fragmentation, source categories) and a `--project nowinandroid --max-builds 15` aggregate (exercises
  the multi-build/build-level/outlier paths).
- Visual check via headless-Chrome screenshot of the generated HTML (the loop used throughout this branch).

---

## Suggested Sequencing

Phase A (measures): U1 → U2, U3.
Phase B (dependency): U4 → U5.
Phase C (categories & cache): U6 → U7.
Phase D (build-level): U8 → U9.
Phase E (presentation): U10, U11.
Phase F (insights): U12 → U13.

U1 unblocks the most; U10/U11 can land any time after their charts exist; U12/U13 land last since they
consume U2/U5/U8.
