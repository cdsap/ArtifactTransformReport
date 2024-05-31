package io.github.cdsap.artifacttransform.cli.view

import com.jakewharton.picnic.TextAlignment
import com.jakewharton.picnic.table
import io.github.cdsap.artifacttransform.durationByTransformActionType
import io.github.cdsap.artifacttransform.extractName
import io.github.cdsap.artifacttransform.fingerprintingByTransformActionType
import io.github.cdsap.artifacttransform.totalByTransformActionType
import io.github.cdsap.geapi.client.model.ArtifactTransform
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class TransformsByType(transforms: List<ArtifactTransform>) {

    private val total = transforms.totalByTransformActionType().take(10)
    private val duration = transforms.durationByTransformActionType().take(10)
    private val finger = transforms.fingerprintingByTransformActionType().take(10)

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
                cell("Artifacts Transforms by Type") {
                    columnSpan = 6
                    alignment = TextAlignment.MiddleCenter
                }
            }
            row {
                columnMetricHeaderWithSpan("Transforms")
                columnMetricHeaderWithSpan("Duration")
                columnMetricHeaderWithSpan("Fingerprinting")
            }
            val maxSize = maxOf(total.size, duration.size, finger.size)
            for (i in 0 until maxSize) {
                row {
                    if (i < total.size) {
                        labelMetric(total[i].first.extractName())
                        valueMetric(total[i].second)
                    } else {
                        cell("")
                        cell("")
                    }
                    if (i < duration.size) {
                        labelMetric(duration[i].first.extractName())
                        valueMetric(
                            duration[i].second.roundMilliseconds().toDuration(DurationUnit.MILLISECONDS)
                        )
                    } else {
                        cell("")
                        cell("")
                    }
                    if (i < finger.size) {
                        labelMetric(finger[i].first.extractName())
                        valueMetric(finger[i].second.roundMilliseconds().toDuration(DurationUnit.MILLISECONDS))
                    } else {
                        cell("")
                        cell("")
                    }
                }
            }
        }
    }
}
