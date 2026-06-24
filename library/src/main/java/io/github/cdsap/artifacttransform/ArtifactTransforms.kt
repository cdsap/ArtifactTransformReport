package io.github.cdsap.artifacttransform

import io.github.cdsap.geapi.client.model.ArtifactTransform
import kotlin.time.DurationUnit
import kotlin.time.toDuration

fun List<ArtifactTransform>.sortedByDurationDescending() = this.sortedByDescending { it.duration.toInt() }

fun List<ArtifactTransform>.sortedByDurationAndType() = this
    .groupBy { it.transformActionType }
    .mapValues { entry -> entry.value.maxByOrNull { it.duration.toInt() } }
    .values
    .sortedByDescending { it!!.duration.toInt() }



fun List<ArtifactTransform>.totalByTransformActionType() = this.groupingBy { it.transformActionType }
    .eachCount()
    .toList()
    .sortedByDescending { it.second }
    .flatMap {
        listOf(Pair(it.first, it.second))
    }

fun List<ArtifactTransform>.durationByTransformActionType() = this.groupBy { it.transformActionType }
    .mapValues { (_, values) -> values.sumOf { it.duration.toInt() } }
    .toList()
    .sortedByDescending { it.second }
    .flatMap {
        listOf(Pair(it.first, it.second))
    }

fun List<ArtifactTransform>.medianDurationByTransformActionType(): List<Pair<String, Int>> =
    this.groupBy { it.transformActionType }
        .mapValues { (_, values) -> values.map { it.duration.toMillisOrZero() }.median() }
        .toList()
        .sortedByDescending { it.second }

internal fun List<Int>.median(): Int {
    if (isEmpty()) return 0
    val sorted = sorted()
    val mid = sorted.size / 2
    return if (sorted.size % 2 == 1) sorted[mid] else (sorted[mid - 1] + sorted[mid]) / 2
}

// --- Distribution-measure helpers (U1) ---

/** Nearest-rank percentile (e.g. percentile(95) = p95). Empty -> 0. */
internal fun List<Int>.percentile(p: Int): Int {
    if (isEmpty()) return 0
    val sorted = sorted()
    val rank = Math.ceil(p / 100.0 * sorted.size).toInt().coerceIn(1, sorted.size)
    return sorted[rank - 1]
}

internal fun List<Int>.averageInt(): Int = if (isEmpty()) 0 else Math.round(average()).toInt()

internal fun List<Int>.maxOrZero(): Int = maxOrNull() ?: 0

// --- Extended duration measures by transform type (U2) ---

private fun List<ArtifactTransform>.durationsByType(): Map<String, List<Int>> =
    this.groupBy { it.transformActionType }
        .mapValues { (_, values) -> values.map { it.duration.toMillisOrZero() } }

fun List<ArtifactTransform>.averageDurationByTransformActionType(): List<Pair<String, Int>> =
    durationsByType().mapValues { it.value.averageInt() }.toList().sortedByDescending { it.second }

fun List<ArtifactTransform>.p95DurationByTransformActionType(): List<Pair<String, Int>> =
    durationsByType().mapValues { it.value.percentile(95) }.toList().sortedByDescending { it.second }

fun List<ArtifactTransform>.maxDurationByTransformActionType(): List<Pair<String, Int>> =
    durationsByType().mapValues { it.value.maxOrZero() }.toList().sortedByDescending { it.second }

fun List<ArtifactTransform>.fingerprintingByTransformActionType() = this.groupBy { it.transformActionType }
    .mapValues { (_, values) -> values.sumOf { it.fingerprintingDuration.toInt() } }
    .toList()
    .sortedByDescending { it.second }
    .flatMap {
        listOf(Pair(it.first, it.second))
    }


fun List<ArtifactTransform>.totalNegativeAvoidanceSavingsByTransformArtifactType() =
    this.filter { it.avoidanceSavings != null && it.avoidanceSavings!!.toInt() < 0 }
        .groupingBy { it.transformActionType }
        .eachCount()
        .toList()
        .sortedByDescending { it.second }
        .flatMap {
            listOf(Pair(it.first, it.second))
        }

fun List<ArtifactTransform>.durationNegativeAvoidanceSavingsByTransformArtifactType() =
    this.filter { it.avoidanceSavings != null && it.avoidanceSavings!!.toInt() < 0 }
        .groupBy { it.transformActionType }
        .mapValues { (_, values) -> values.sumOf { it.avoidanceSavings!!.toInt() } }
        .toList()
        .sortedBy { it.second }
        .flatMap {
            listOf(Pair(it.first, it.second))
        }

fun List<ArtifactTransform>.longestNegativeAvoidanceSavings() =
    this.filter { it.avoidanceSavings != null && it.avoidanceSavings!!.toInt() < 0 }
        .toList()
        .sortedBy { it.avoidanceSavings!!.toInt() }
        .flatMap {
            listOf(
                Pair(
                    "${it.transformActionType.extractName()}-${it.inputArtifactName}",
                    it.avoidanceSavings!!.toInt().toDuration(DurationUnit.MILLISECONDS)
                )
            )
        }

fun List<ArtifactTransform>.dependencySortedByDuration() =
    this.groupBy { it.artifactTransformExecutionName.split(" [")[0] }
        .mapValues { (_, values) -> values.sumOf { it.duration.toInt() } }
        .toList()
        .sortedByDescending { it.second }
        .flatMap {
            listOf(Pair(it.first, it.second))
        }

fun List<ArtifactTransform>.dependencyByInputArtifactName() = this.groupBy { it.inputArtifactName.split(" [")[0] }
    .mapValues { (_, values) -> values.sumOf { it.duration.toInt() } }
    .toList()
    .sortedByDescending { it.second }
    .flatMap {
        listOf(Pair(it.first, it.second))
    }

fun List<ArtifactTransform>.cacheSizeByTransformActionType() = this.filter { it.cacheArtifactSize != null }
    .groupBy { it.transformActionType }
    .mapValues { entry -> entry.value.maxByOrNull { it.cacheArtifactSize.toInt() } }
    .values
    .sortedByDescending { it!!.cacheArtifactSize.toInt() }
    .flatMap { listOf(Pair(it!!.artifactTransformExecutionName, it.cacheArtifactSize.toInt())) }

fun List<ArtifactTransform>.aggregatedCacheSizeByTransformActionType() = this.filter { it.cacheArtifactSize != null }
    .groupBy { it.transformActionType }
    .mapValues { entry -> entry.value.sumOf { it.cacheArtifactSize.toInt() } }
    .toList()
    .sortedByDescending { it.second }
    .flatMap { listOf(Pair(it.first, it.second)) }


fun List<ArtifactTransform>.groupByAvoidanceOutcome() = this.groupingBy { it.avoidanceOutcome }.eachCount()

fun List<ArtifactTransform>.durationByAvoidanceOutcome() = this.groupBy { it.avoidanceOutcome }
    .mapValues { (_, values) -> values.sumOf { it.duration.toInt() } }

fun List<ArtifactTransform>.fingerprintingByAvoidanceOutcome() = this.groupBy { it.avoidanceOutcome }
    .mapValues { (_, values) -> values.sumOf { it.fingerprintingDuration.toInt() } }

fun List<ArtifactTransform>.groupByOutcome() = this.groupingBy { it.outcome }.eachCount()

fun List<ArtifactTransform>.durationByOutcome() = this.groupBy { it.outcome }
    .mapValues { (_, values) -> values.sumOf { it.duration.toInt() } }

fun List<ArtifactTransform>.fingerprintingByOutcome() = this.groupBy { it.outcome }
    .mapValues { (_, values) -> values.sumOf { it.fingerprintingDuration.toInt() } }


fun String.extractName(): String {
    return this.substringAfterLast(".")
}

internal fun String?.toMillisOrZero(): Int = this?.toIntOrNull() ?: 0

// --- Tier 1: cache effectiveness ---
// avoidanceOutcome values reported by Develocity are prefixed: `avoided_*` (cache hit / up-to-date)
// vs `executed_*` (the transform actually ran). `executed_cacheable` is an avoidable miss: it ran
// even though it could have been served from the cache.

fun ArtifactTransform.isAvoided(): Boolean = avoidanceOutcome.startsWith("avoided")

fun ArtifactTransform.isExecuted(): Boolean = avoidanceOutcome.startsWith("executed")

fun ArtifactTransform.isAvoidableMiss(): Boolean = avoidanceOutcome == "executed_cacheable"

data class CacheEffectiveness(
    val transformActionType: String,
    val total: Int,
    val avoided: Int,
    val executed: Int,
    val avoidableMisses: Int,
    val avoidableMissDuration: Int,
    val hitRate: Double
)

fun List<ArtifactTransform>.overallCacheHitRate(): Double {
    val avoided = count { it.isAvoided() }
    val executed = count { it.isExecuted() }
    val classified = avoided + executed
    return if (classified == 0) 0.0 else avoided.toDouble() / classified
}

fun List<ArtifactTransform>.totalAvoidableMissDuration(): Int =
    this.filter { it.isAvoidableMiss() }.sumOf { it.duration.toMillisOrZero() }

fun List<ArtifactTransform>.totalDuration(): Int = this.sumOf { it.duration.toMillisOrZero() }

/** Total time saved by avoided transforms (positive avoidanceSavings only), when available. */
fun List<ArtifactTransform>.totalAvoidanceSavings(): Int =
    this.filter { it.avoidanceSavings != null }
        .sumOf { it.avoidanceSavings.toMillisOrZero().coerceAtLeast(0) }

/** Keep the top n entries and roll the remainder into a single "Other" entry (when non-zero). */
fun List<Pair<String, Int>>.topNWithOther(n: Int): List<Pair<String, Int>> {
    if (size <= n) return this
    val other = drop(n).sumOf { it.second }
    return if (other > 0) take(n) + ("Other" to other) else take(n)
}

fun List<ArtifactTransform>.cacheEffectivenessByTransformActionType(): List<CacheEffectiveness> =
    this.groupBy { it.transformActionType }
        .map { (type, values) ->
            val avoided = values.count { it.isAvoided() }
            val executed = values.count { it.isExecuted() }
            val classified = avoided + executed
            val avoidableMisses = values.filter { it.isAvoidableMiss() }
            CacheEffectiveness(
                transformActionType = type,
                total = values.size,
                avoided = avoided,
                executed = executed,
                avoidableMisses = avoidableMisses.size,
                avoidableMissDuration = avoidableMisses.sumOf { it.duration.toMillisOrZero() },
                hitRate = if (classified == 0) 0.0 else avoided.toDouble() / classified
            )
        }
        .sortedByDescending { it.avoidableMissDuration }

// --- Tier 1: changed-attribute transitions ---

fun ArtifactTransform.attributeTransitionLabel(): String =
    if (changedAttributes.isEmpty()) {
        "n/a"
    } else {
        changedAttributes.joinToString(", ") { "${it.name}: ${it.from} -> ${it.to}" }
    }

fun List<ArtifactTransform>.durationByAttributeTransition(): List<Pair<String, Int>> =
    this.groupBy { it.attributeTransitionLabel() }
        .mapValues { (_, values) -> values.sumOf { it.duration.toMillisOrZero() } }
        .toList()
        .sortedByDescending { it.second }

fun List<ArtifactTransform>.countByAttributeTransition(): List<Pair<String, Int>> =
    this.groupingBy { it.attributeTransitionLabel() }
        .eachCount()
        .toList()
        .sortedByDescending { it.second }

private fun List<ArtifactTransform>.durationsByTransition(): Map<String, List<Int>> =
    this.groupBy { it.attributeTransitionLabel() }
        .mapValues { (_, values) -> values.map { it.duration.toMillisOrZero() } }

fun List<ArtifactTransform>.medianDurationByAttributeTransition(): List<Pair<String, Int>> =
    durationsByTransition().mapValues { it.value.median() }.toList().sortedByDescending { it.second }

fun List<ArtifactTransform>.p95DurationByAttributeTransition(): List<Pair<String, Int>> =
    durationsByTransition().mapValues { it.value.percentile(95) }.toList().sortedByDescending { it.second }

// --- Tier 1: per-build-scan aggregation ---

fun List<ArtifactTransform>.durationByBuildScan(): List<Pair<String, Int>> =
    this.groupBy { it.buildScanId ?: "unknown" }
        .mapValues { (_, values) -> values.sumOf { it.duration.toMillisOrZero() } }
        .toList()
        .sortedByDescending { it.second }

fun List<ArtifactTransform>.countByBuildScan(): List<Pair<String, Int>> =
    this.groupingBy { it.buildScanId ?: "unknown" }
        .eachCount()
        .toList()
        .sortedByDescending { it.second }

// --- Build-level analytics + outlier detection (U8) ---

fun List<ArtifactTransform>.cacheHitRateByBuildScan(): List<Pair<String, Double>> =
    this.groupBy { it.buildScanId ?: "unknown" }
        .mapValues { (_, values) -> values.overallCacheHitRate() }
        .toList()
        .sortedByDescending { it.second }

fun List<ArtifactTransform>.slowestTransformByBuildScan(): List<Pair<String, ArtifactTransform>> =
    this.groupBy { it.buildScanId ?: "unknown" }
        .mapValues { (_, values) -> values.maxByOrNull { it.duration.toMillisOrZero() } }
        .mapNotNull { (build, transform) -> transform?.let { build to it } }
        .sortedByDescending { it.second.duration.toMillisOrZero() }

fun List<ArtifactTransform>.topTransformTypeByBuildScan(): List<Pair<String, String>> =
    this.groupBy { it.buildScanId ?: "unknown" }
        .mapValues { (_, values) -> values.durationByTransformActionType().firstOrNull()?.first ?: "" }
        .toList()

/**
 * Build scans whose total transform duration is an outlier. Requires at least 3 builds and flags any
 * build exceeding twice the median total duration across builds.
 */
fun List<ArtifactTransform>.outlierBuildScans(): List<String> {
    val totals = durationByBuildScan()
    if (totals.size < 3) return emptyList()
    val median = totals.map { it.second }.median()
    if (median <= 0) return emptyList() // no meaningful baseline (e.g. all builds fully cached)
    val threshold = median * 2
    return totals.filter { it.second > threshold }.map { it.first }
}

// --- Prototype: tool/plugin attribution (#1) ---
// transformActionType is a fully-qualified class name; its package identifies the plugin/ecosystem
// that contributed the transform.

fun ArtifactTransform.provider(): String {
    val type = transformActionType
    return when {
        type.contains("kapt") -> "Kotlin kapt"
        type.contains("org.jetbrains.kotlin") -> "Kotlin"
        type.contains("com.android") -> "Android/AGP"
        type.contains("dagger") || type.contains("hilt") -> "Hilt/Dagger"
        type.startsWith("org.gradle") -> "Gradle"
        else -> type.substringBeforeLast(".", "").ifEmpty { "other" }
    }
}

fun List<ArtifactTransform>.durationByProvider(): List<Pair<String, Int>> =
    this.groupBy { it.provider() }
        .mapValues { (_, values) -> values.sumOf { it.duration.toMillisOrZero() } }
        .toList()
        .sortedByDescending { it.second }

fun List<ArtifactTransform>.countByProvider(): List<Pair<String, Int>> =
    this.groupingBy { it.provider() }
        .eachCount()
        .toList()
        .sortedByDescending { it.second }

// --- Prototype: source attribution (#2) ---
// artifactTransformExecutionName's prefix (before " [") is the logical source of the input:
//  - "project :path" for a first-party Gradle module
//  - a "group:name:version" coordinate for an external dependency
//  - otherwise a bare file name with no project/coordinate provenance (e.g. classes.jar, android.jar)

fun ArtifactTransform.transformSource(): String =
    artifactTransformExecutionName.substringBefore(" [")

fun ArtifactTransform.sourceModule(): String? =
    transformSource().takeIf { it.startsWith("project ") }?.removePrefix("project ")

// Conservative allowlist of platform/SDK/runtime inputs that appear as bare files (no GAV). Kept
// intentionally small to honor the spec's "do not guess attribution" rule.
private fun isSdkRuntimeArtifact(source: String): Boolean {
    val file = source.substringAfterLast('/')
    return file == "android.jar" ||
        file.startsWith("gradle-api-") ||
        file.startsWith("gradle-installation-beacon-") ||
        file == "rt.jar" ||
        file.startsWith("core-for-system-modules") ||
        file.contains("jdkImage", ignoreCase = true)
}

fun ArtifactTransform.sourceCategory(): String {
    val source = transformSource()
    return when {
        source.startsWith("project ") -> "First-party module"
        source.count { it == ':' } >= 2 -> "External dependency"
        isSdkRuntimeArtifact(source) -> "SDK/runtime artifact"
        source.isNotBlank() -> "Unattributed file"
        else -> "Unknown"
    }
}

fun List<ArtifactTransform>.durationBySourceCategory(): List<Pair<String, Int>> =
    this.groupBy { it.sourceCategory() }
        .mapValues { (_, values) -> values.sumOf { it.duration.toMillisOrZero() } }
        .toList()
        .sortedByDescending { it.second }

fun List<ArtifactTransform>.durationByModule(): List<Pair<String, Int>> =
    this.mapNotNull { transform -> transform.sourceModule()?.let { it to transform.duration.toMillisOrZero() } }
        .groupBy { it.first }
        .mapValues { (_, pairs) -> pairs.sumOf { it.second } }
        .toList()
        .sortedByDescending { it.second }

// --- Dependency coordinate + per-dependency aggregations (U4) ---
// For external dependencies the transformSource() prefix is the GAV coordinate (group:name:version).

fun ArtifactTransform.dependencyCoordinate(): String? =
    transformSource().takeIf { sourceCategory() == "External dependency" }

private fun List<ArtifactTransform>.durationsByDependency(): Map<String, List<Int>> =
    this.mapNotNull { t -> t.dependencyCoordinate()?.let { it to t.duration.toMillisOrZero() } }
        .groupBy({ it.first }, { it.second })

fun List<ArtifactTransform>.durationByDependency(): List<Pair<String, Int>> =
    durationsByDependency().mapValues { it.value.sum() }.toList().sortedByDescending { it.second }

fun List<ArtifactTransform>.countByDependency(): List<Pair<String, Int>> =
    durationsByDependency().mapValues { it.value.size }.toList().sortedByDescending { it.second }

fun List<ArtifactTransform>.medianDurationByDependency(): List<Pair<String, Int>> =
    durationsByDependency().mapValues { it.value.median() }.toList().sortedByDescending { it.second }

fun List<ArtifactTransform>.cacheSizeByDependency(): List<Pair<String, Int>> =
    this.filter { it.cacheArtifactSize != null }
        .mapNotNull { t -> t.dependencyCoordinate()?.let { it to t.cacheArtifactSize.toMillisOrZero() } }
        .groupBy({ it.first }, { it.second })
        .mapValues { it.value.sum() }
        .toList()
        .sortedByDescending { it.second }

fun List<ArtifactTransform>.cacheSizeBySourceCategory(): List<Pair<String, Int>> =
    this.filter { it.cacheArtifactSize != null }
        .groupBy { it.sourceCategory() }
        .mapValues { (_, values) -> values.sumOf { it.cacheArtifactSize.toMillisOrZero() } }
        .toList()
        .sortedByDescending { it.second }

// --- Dependency family aggregation (U5) ---
// Family = group:name (the coordinate with its version dropped); enables fragmentation detection.

data class DependencyFamily(
    val family: String,
    val versions: List<String>,
    val count: Int,
    val totalDuration: Int,
    val cacheSize: Int,
    val mostExpensiveVersion: String
)

// group:name from a "group:name:version[:classifier]" coordinate.
private fun String.gavFamily(): String = split(":").take(2).joinToString(":")

// version segment from a "group:name:version[:classifier]" coordinate (empty when absent).
private fun String.gavVersion(): String = split(":").getOrElse(2) { "" }

fun List<ArtifactTransform>.dependencyFamilies(): List<DependencyFamily> =
    this.mapNotNull { transform -> transform.dependencyCoordinate()?.let { transform to it } }
        .groupBy { it.second.gavFamily() }
        .map { (family, pairs) ->
            val mostExpensiveVersion = pairs
                .groupBy { it.second.gavVersion() }
                .mapValues { (_, p) -> p.sumOf { it.first.duration.toMillisOrZero() } }
                .maxByOrNull { it.value }?.key ?: ""
            DependencyFamily(
                family = family,
                versions = pairs.map { it.second.gavVersion() }.distinct().sorted(),
                count = pairs.size,
                totalDuration = pairs.sumOf { it.first.duration.toMillisOrZero() },
                cacheSize = pairs.filter { it.first.cacheArtifactSize != null }
                    .sumOf { it.first.cacheArtifactSize.toMillisOrZero() },
                mostExpensiveVersion = mostExpensiveVersion
            )
        }
        .sortedByDescending { it.totalDuration }

// inputArtifactName is the resolved artifact file; external dependencies follow
// <library>-<version>.<jar|aar>. Used to flag dependency version drift.

private val VERSIONED_ARTIFACT = Regex("^(.+?)-(\\d[A-Za-z0-9.\\-]*)\\.(jar|aar)$")

data class ParsedArtifact(val library: String, val version: String?) {
    val isExternalDependency: Boolean get() = version != null
}

fun ArtifactTransform.parsedInputArtifact(): ParsedArtifact {
    val match = VERSIONED_ARTIFACT.matchEntire(inputArtifactName)
    return if (match != null) {
        ParsedArtifact(match.groupValues[1], match.groupValues[2])
    } else {
        ParsedArtifact(inputArtifactName, null)
    }
}

private fun String.normalizedVersion(): String = removeSuffix("-runtime").removeSuffix("-api")

/** Libraries transformed under more than one (variant-normalized) version — dependency drift. */
fun List<ArtifactTransform>.librariesWithMultipleVersions(): List<Pair<String, List<String>>> =
    this.mapNotNull { transform -> transform.parsedInputArtifact().takeIf { it.isExternalDependency } }
        .groupBy { it.library }
        .mapValues { (_, parsed) -> parsed.map { it.version!!.normalizedVersion() }.distinct().sorted() }
        .filter { it.value.size > 1 }
        .toList()
        .sortedByDescending { it.second.size }

// --- Tier 1: artifact transform pipeline (changed-attribute graph) ---
// Each transform turns an input artifact type (`from`) into an output type (`to`). One transform's
// output type is consumed as the input of the next, so the aggregated from->to edges describe the
// build's artifact transform pipeline as a directed graph that can be ordered topologically.

data class TransitionEdge(
    val from: String,
    val to: String,
    val count: Int,
    val totalDuration: Int,
    val medianDuration: Int
)

fun List<ArtifactTransform>.attributeTransitionEdges(): List<TransitionEdge> =
    this.flatMap { transform ->
        transform.changedAttributes.map { Triple(it.from, it.to, transform.duration.toMillisOrZero()) }
    }
        .groupBy { it.first to it.second }
        .map { (key, grouped) ->
            val durations = grouped.map { it.third }
            TransitionEdge(key.first, key.second, grouped.size, durations.sum(), durations.median())
        }
        .sortedByDescending { it.totalDuration }

/**
 * Topological ordering of artifact types implied by the transition edges (sources first, sinks last).
 * Nodes left over in cycles are appended in stable order so the result always contains every node.
 */
fun List<TransitionEdge>.topologicalArtifactOrder(): List<String> {
    val nodes = (map { it.from } + map { it.to }).distinct()
    val outgoing = groupBy { it.from }
    val indegree = nodes.associateWith { node -> count { it.to == node } }.toMutableMap()
    val queue = ArrayDeque(nodes.filter { indegree[it] == 0 })
    val order = mutableListOf<String>()
    while (queue.isNotEmpty()) {
        val node = queue.removeFirst()
        order += node
        outgoing[node]?.forEach { edge ->
            val remaining = (indegree[edge.to] ?: 0) - 1
            indegree[edge.to] = remaining
            if (remaining == 0) queue.addLast(edge.to)
        }
    }
    nodes.filterNot { order.contains(it) }.forEach { order += it }
    return order
}

/**
 * Depth of each artifact type in the pipeline (longest path from a source), used to lay the graph
 * out in columns. Computed by relaxing edges in topological order.
 */
fun List<TransitionEdge>.artifactLevels(): Map<String, Int> {
    val order = topologicalArtifactOrder()
    val outgoing = groupBy { it.from }
    val level = order.associateWith { 0 }.toMutableMap()
    order.forEach { node ->
        val current = level[node] ?: 0
        outgoing[node]?.forEach { edge ->
            if ((level[edge.to] ?: 0) < current + 1) level[edge.to] = current + 1
        }
    }
    return level
}
