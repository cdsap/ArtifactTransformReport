package io.github.cdsap.artifacttransform.cli.view

import com.jakewharton.picnic.TextAlignment
import com.jakewharton.picnic.table
import io.github.cdsap.artifacttransform.durationByOutcome
import io.github.cdsap.artifacttransform.extractName
import io.github.cdsap.artifacttransform.fingerprintingByOutcome
import io.github.cdsap.artifacttransform.groupByOutcome
import io.github.cdsap.geapi.client.model.ArtifactTransform
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class OutcomeView(transforms: List<ArtifactTransform>) {
    private val outcome = transforms.groupByOutcome()

    private val durationOutcome = transforms.durationByOutcome()

    private val fingerOutcome = transforms.fingerprintingByOutcome()

    private val outcomesTotal = outcome
        .toList()
        .sortedByDescending { it.second }
        .flatMap {
            listOf(Pair(it.first, it.second))
        }

    private val durationOutcomeList = durationOutcome
        .toList()
        .sortedByDescending { it.second }
        .flatMap {
            listOf(Pair(it.first, it.second))
        }

    private val fingerOutcomeList = fingerOutcome
        .toList()
        .sortedByDescending { it.second }
        .flatMap {
            listOf(Pair(it.first, it.second))
        }

    private val averageDurationsOutcome = outcome.map { (outcome, count) ->
        val totalDuration = durationOutcome[outcome] ?: 0
        val averageDuration = totalDuration.toDouble() / count
        outcome to averageDuration.roundToTwoDecimalPlaces()
    }

    private val averageFingerOutcome = outcome.map { (outcome, count) ->
        val totalDuration = fingerOutcome[outcome] ?: 0
        val averageDuration = totalDuration.toDouble() / count
        outcome to averageDuration.roundToTwoDecimalPlaces()
    }

    private val sortedByAverageDurationOutcome = averageDurationsOutcome
        .sortedByDescending { it.second }
        .toList()

    private val sortedByAverageDurationFinger = averageFingerOutcome
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
                cell("Artifacts Transforms by outcome") {
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
            for (i in 0 until outcome.size) {
                row {
                    labelMetric(outcomesTotal[i].first.extractName())
                    valueMetric(outcomesTotal[i].second)
                    labelMetric(durationOutcomeList[i].first.extractName())
                    valueMetric(
                        durationOutcomeList[i].second.roundMilliseconds()
                            .toDuration(DurationUnit.MILLISECONDS)
                    )
                    labelMetric(sortedByAverageDurationOutcome[i].first.extractName())
                    valueMetric(sortedByAverageDurationOutcome[i].second.toDuration(DurationUnit.MILLISECONDS))
                    labelMetric(fingerOutcomeList[i].first.extractName())
                    valueMetric(
                        fingerOutcomeList[i].second.roundMilliseconds()
                            .toDuration(DurationUnit.MILLISECONDS)
                    )
                    labelMetric(sortedByAverageDurationFinger[i].first.extractName())
                    valueMetric(sortedByAverageDurationFinger[i].second.toDuration(DurationUnit.MILLISECONDS))
                }
            }
        }
    }
}
