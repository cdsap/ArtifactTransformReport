package io.github.cdsap.artifacttransform.cli.view

import com.jakewharton.picnic.TextAlignment
import com.jakewharton.picnic.table
import io.github.cdsap.artifacttransform.durationByAvoidanceOutcome
import io.github.cdsap.artifacttransform.extractName
import io.github.cdsap.artifacttransform.fingerprintingByAvoidanceOutcome
import io.github.cdsap.artifacttransform.groupByAvoidanceOutcome
import io.github.cdsap.geapi.client.model.ArtifactTransform
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class AvoidanceSavingsOutcomeView(transforms: List<ArtifactTransform>) {

    private val avoidanceOutcome = transforms.groupByAvoidanceOutcome()
    private val durationAvoidanceOutcomes = transforms.durationByAvoidanceOutcome()
    private val fingerAvoidanceOutcomes = transforms.fingerprintingByAvoidanceOutcome()

    private val avoidanceOutcomesList = avoidanceOutcome
        .toList()
        .sortedByDescending { it.second }
        .flatMap {
            listOf(Pair(it.first, it.second))
        }
    private val durationAvoidanceOutcomesList = durationAvoidanceOutcomes
        .toList()
        .sortedByDescending { it.second }
        .flatMap {
            listOf(Pair(it.first, it.second))
        }

    private val fingerAvoidanceOutcomesList = fingerAvoidanceOutcomes
        .toList()
        .sortedByDescending { it.second }
        .flatMap {
            listOf(Pair(it.first, it.second))
        }
    private val averageAvoidanceOutcomeDurations = avoidanceOutcome.map { (outcome, count) ->
        val totalDuration = durationAvoidanceOutcomes[outcome] ?: 0
        val averageDuration = totalDuration.toDouble() / count
        outcome to averageDuration.roundToTwoDecimalPlaces()
    }

    private val averageAvoidanceOutcomesFinger = avoidanceOutcome.map { (outcome, count) ->
        val totalDuration = fingerAvoidanceOutcomes[outcome] ?: 0
        val averageDuration = totalDuration.toDouble() / count
        outcome to averageDuration.roundToTwoDecimalPlaces()
    }

    private val sortedByAverageAvoidanceOutcomeDurations = averageAvoidanceOutcomeDurations
        .sortedByDescending { it.second }
        .toList()

    private val sortedByAverageAvoidanceOutcomeFinger = averageAvoidanceOutcomesFinger
        .sortedByDescending { it.second }
        .toList()

    fun print() {
        println(table())
    }

    fun generateReport() = table()

    private fun table() = table {
        cellStyle {
            border = true
            alignment = TextAlignment.MiddleLeft
            paddingLeft = 1
            paddingRight = 1
        }
        body {
            row {
                cell("Artifacts Transforms by Avoidance Savings Outcome") {
                    columnSpan = 10
                    alignment = TextAlignment.MiddleCenter
                }
            }
            row {
                columnMetricHeaderWithSpan("Outcome")
                columnMetricHeaderWithSpan("Duration")
                columnMetricHeaderWithSpan("Avg Duration")
                columnMetricHeaderWithSpan("Fingerprinting")
                columnMetricHeaderWithSpan("Avg Fingerprinting")
            }
            for (i in 0 until avoidanceOutcome.size) {
                row {
                    labelMetric(avoidanceOutcomesList[i].first.extractName())
                    valueMetric(avoidanceOutcomesList[i].second)
                    labelMetric(durationAvoidanceOutcomesList[i].first.extractName())
                    valueMetric(
                        durationAvoidanceOutcomesList[i].second.roundMilliseconds()
                            .toDuration(DurationUnit.MILLISECONDS)
                    )
                    labelMetric(sortedByAverageAvoidanceOutcomeDurations[i].first.extractName())
                    valueMetric(sortedByAverageAvoidanceOutcomeDurations[i].second.toDuration(DurationUnit.MILLISECONDS))
                    labelMetric(fingerAvoidanceOutcomesList[i].first.extractName())
                    valueMetric(
                        fingerAvoidanceOutcomesList[i].second.roundMilliseconds()
                            .toDuration(DurationUnit.MILLISECONDS)
                    )
                    labelMetric(sortedByAverageAvoidanceOutcomeFinger[i].first.extractName())
                    valueMetric(sortedByAverageAvoidanceOutcomeFinger[i].second.toDuration(DurationUnit.MILLISECONDS))
                }
            }
        }
    }
}
