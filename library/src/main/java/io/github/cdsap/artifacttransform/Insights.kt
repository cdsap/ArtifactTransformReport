package io.github.cdsap.artifacttransform

import io.github.cdsap.geapi.client.model.ArtifactTransform

enum class FindingKind {
    EXPENSIVE_BECAUSE_FREQUENT,
    EXPENSIVE_PER_EXECUTION,
    CACHE_OPPORTUNITY,
    VERSION_FRAGMENTATION,
    OUTLIER_BUILD
}

enum class Severity { HIGH, MEDIUM, LOW }

data class Finding(
    val kind: FindingKind,
    val severity: Severity,
    val subject: String,
    val message: String,
    val evidence: Int = 0
)

// Dataset-relative thresholds, kept conservative so small builds produce few or no findings.
private const val HIGH_SHARE = 0.15 // a transform type owning >=15% of total time is "high cost"
private const val P95_SLOW_FACTOR = 3 // p95 >= 3x the overall median is "slow per execution"
private const val LOW_HIT_RATE = 0.5

/**
 * Derived findings about where artifact transform cost concentrates and what is actionable.
 * Pure analytics: returns typed candidates; rendering is the caller's responsibility.
 */
fun List<ArtifactTransform>.findings(): List<Finding> {
    if (isEmpty()) return emptyList()
    val findings = mutableListOf<Finding>()

    val grandTotal = totalDuration().coerceAtLeast(1)
    val totals = durationByTransformActionType().toMap()
    val counts = totalByTransformActionType().toMap()
    val medians = medianDurationByTransformActionType().toMap()
    val p95s = p95DurationByTransformActionType().toMap()
    val overallMedian = map { it.duration.toMillisOrZero() }.median().coerceAtLeast(1)
    val effectiveness = cacheEffectivenessByTransformActionType().associateBy { it.transformActionType }

    totals.forEach { (type, total) ->
        val share = total.toDouble() / grandTotal
        val count = counts[type] ?: 0
        val median = medians[type] ?: 0
        val p95 = p95s[type] ?: 0
        val name = type.substringAfterLast(".")

        if (share >= HIGH_SHARE && count > 1 && median <= overallMedian) {
            findings += Finding(
                FindingKind.EXPENSIVE_BECAUSE_FREQUENT, Severity.MEDIUM, name,
                "$name dominates total transform time mostly because it runs $count times, " +
                    "not because each run is slow (median ${median}ms).",
                total
            )
        }
        if (p95 >= overallMedian * P95_SLOW_FACTOR && median > overallMedian) {
            findings += Finding(
                FindingKind.EXPENSIVE_PER_EXECUTION, Severity.MEDIUM, name,
                "$name is expensive per execution (p95 ${p95}ms) and may be worth investigating directly.",
                p95
            )
        }
        val eff = effectiveness[type]
        if (share >= HIGH_SHARE && eff != null && (eff.avoided + eff.executed) > 0 && eff.hitRate < LOW_HIT_RATE) {
            findings += Finding(
                FindingKind.CACHE_OPPORTUNITY, Severity.HIGH, name,
                "$name has high transform cost and low cache reuse " +
                    "(hit rate ${(eff.hitRate * 100).toInt()}%).",
                total
            )
        }
    }

    // One aggregate fragmentation finding (the fragmentation table enumerates the families in detail).
    val fragmented = dependencyFamilies().filter { it.versions.size > 1 }
    if (fragmented.isNotEmpty()) {
        val examples = fragmented.take(3).joinToString(", ") { it.family }
        findings += Finding(
            FindingKind.VERSION_FRAGMENTATION, Severity.LOW, "Version fragmentation",
            "${fragmented.size} dependency families are transformed under multiple versions " +
                "(e.g. $examples). Dependency alignment may reduce repeated work.",
            fragmented.sumOf { it.totalDuration }
        )
    }

    outlierBuildScans().forEach { build ->
        findings += Finding(
            FindingKind.OUTLIER_BUILD, Severity.HIGH, build,
            "Build $build has unusually high artifact transform cost compared with the other builds.",
            0
        )
    }

    return findings.sortedWith(compareBy({ it.severity.ordinal }, { -it.evidence })).take(15)
}
