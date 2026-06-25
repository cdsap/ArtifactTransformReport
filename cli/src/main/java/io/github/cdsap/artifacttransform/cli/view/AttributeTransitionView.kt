package io.github.cdsap.artifacttransform.cli.view

import com.jakewharton.picnic.TextAlignment
import com.jakewharton.picnic.table
import io.github.cdsap.artifacttransform.countByAttributeTransition
import io.github.cdsap.artifacttransform.durationByAttributeTransition
import io.github.cdsap.geapi.client.model.ArtifactTransform
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class AttributeTransitionView(transforms: List<ArtifactTransform>) {
    private val byDuration = transforms.durationByAttributeTransition().take(10)
    private val byCount = transforms.countByAttributeTransition().take(10)

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
                cell("Artifact transforms by changed attributes") {
                    columnSpan = 4
                    alignment = TextAlignment.MiddleCenter
                }
            }
            row {
                columnMetricHeaderWithSpan("Aggregated duration by transition")
                columnMetricHeaderWithSpan("Count by transition")
            }
            val maxSize = maxOf(byDuration.size, byCount.size)
            for (i in 0 until maxSize) {
                row {
                    if (i < byDuration.size) {
                        labelMetric(byDuration[i].first)
                        valueMetric(
                            byDuration[i].second.roundMilliseconds()
                                .toDuration(DurationUnit.MILLISECONDS)
                        )
                    } else {
                        cell("")
                        cell("")
                    }
                    if (i < byCount.size) {
                        labelMetric(byCount[i].first)
                        valueMetric(byCount[i].second)
                    } else {
                        cell("")
                        cell("")
                    }
                }
            }
        }
    }
}
