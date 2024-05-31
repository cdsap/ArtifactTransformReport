package io.github.cdsap.artifacttransform.cli.view

import com.jakewharton.picnic.Table
import com.jakewharton.picnic.TextAlignment
import com.jakewharton.picnic.table
import io.github.cdsap.artifacttransform.durationNegativeAvoidanceSavingsByTransformArtifactType
import io.github.cdsap.artifacttransform.extractName
import io.github.cdsap.artifacttransform.longestNegativeAvoidanceSavings
import io.github.cdsap.artifacttransform.totalNegativeAvoidanceSavingsByTransformArtifactType
import io.github.cdsap.geapi.client.model.ArtifactTransform
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class NegativeAvoidanceView(transforms: List<ArtifactTransform>) {
    private val totalNegativeAvoidance = transforms.totalNegativeAvoidanceSavingsByTransformArtifactType().take(10)
    private val totalDuration = transforms.durationNegativeAvoidanceSavingsByTransformArtifactType().take(10)
    private val max = transforms.longestNegativeAvoidanceSavings().take(10)

    fun print() {
        if (totalNegativeAvoidance.isNotEmpty() || totalDuration.isNotEmpty() || max.isNotEmpty()) {
            println(
                table()
            )
        }
    }

    fun generateReport(): Table {
        return if (totalNegativeAvoidance.isNotEmpty() || totalDuration.isNotEmpty() || max.isNotEmpty()) {
            table()
        } else {
            table {}
        }
    }

    private fun table() = table {
        cellStyle {
            border = true
            alignment = TextAlignment.MiddleLeft
            paddingLeft = 1
            paddingRight = 1
        }
        body {
            row {
                cell("Negative Transforms by Type") {
                    columnSpan = 6
                    alignment = TextAlignment.MiddleCenter
                }
            }
            row {
                columnMetricHeaderWithSpan("Transforms")
                columnMetricHeaderWithSpan("Aggregated Negative Duration")
                columnMetricHeaderWithSpan("Slowest Transform")
            }
            val maxSize = maxOf(totalNegativeAvoidance.size, totalDuration.size, max.size)
            for (i in 0 until maxSize) {
                row {
                    if (i < totalNegativeAvoidance.size) {
                        labelMetric(totalNegativeAvoidance[i].first.extractName())
                        valueMetric(totalNegativeAvoidance[i].second)
                    } else {
                        cell("")
                        cell("")
                    }
                    if (i < totalDuration.size) {
                        labelMetric(totalDuration[i].first.extractName())
                        valueMetric(
                            totalDuration[i].second.roundMilliseconds()
                                .toDuration(DurationUnit.MILLISECONDS)
                        )
                    } else {
                        cell("")
                        cell("")
                    }
                    if (i < max.size) {
                        labelMetric(max[i].first)
                        valueMetric(max[i].second)
                    } else {
                        cell("")
                        cell("")
                    }
                }
            }
        }
    }
}
