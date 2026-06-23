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

// --- Tier 1: artifact transform pipeline (changed-attribute graph) ---
// Each transform turns an input artifact type (`from`) into an output type (`to`). One transform's
// output type is consumed as the input of the next, so the aggregated from->to edges describe the
// build's artifact transform pipeline as a directed graph that can be ordered topologically.

data class TransitionEdge(
    val from: String,
    val to: String,
    val count: Int,
    val totalDuration: Int
)

fun List<ArtifactTransform>.attributeTransitionEdges(): List<TransitionEdge> =
    this.flatMap { transform ->
        transform.changedAttributes.map { Triple(it.from, it.to, transform.duration.toMillisOrZero()) }
    }
        .groupBy { it.first to it.second }
        .map { (key, grouped) -> TransitionEdge(key.first, key.second, grouped.size, grouped.sumOf { it.third }) }
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
