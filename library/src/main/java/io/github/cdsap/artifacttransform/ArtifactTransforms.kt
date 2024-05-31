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
