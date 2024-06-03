package io.github.cdsap.artifacttransform.cli.view

import com.jakewharton.picnic.TextAlignment
import com.jakewharton.picnic.table
import io.github.cdsap.artifacttransform.extractName
import io.github.cdsap.artifacttransform.sortedByDurationAndType
import io.github.cdsap.artifacttransform.sortedByDurationDescending
import io.github.cdsap.geapi.client.model.ArtifactTransform
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class SlowestView(transforms: List<ArtifactTransform>) {
    private val slowDuration = transforms.sortedByDurationDescending().take(10)
    private val slowDurationByType = transforms.sortedByDurationAndType().take(10)

    fun print() {
        println(table())
    }

    fun generateReport() = table()

    private fun table() =
        table {
            cellStyle {
                border = true
                alignment = TextAlignment.MiddleLeft
                paddingLeft = 1
                paddingRight = 1
            }
            body {
                row {
                    cell("Slowest Artifact Transforms") {
                        columnSpan = 4
                        alignment = TextAlignment.MiddleCenter
                    }
                }
                row {
                    columnMetricHeaderWithSpan("Slowest Artifact transform")
                    columnMetricHeaderWithSpan("Slowest Artifact transform by type")
                }
                val maxSize = maxOf(slowDuration.size, slowDurationByType.size)
                for (i in 0 until maxSize) {
                    row {
                        if (i < slowDuration.size) {
                            labelMetric(
                                slowDuration[i].transformActionType.extractName() + "\n" + slowDuration[i].artifactTransformExecutionName.replace(
                                    " ",
                                    "\n"
                                )
                            )
                            valueMetric(
                                slowDuration[i].duration.toInt().roundMilliseconds()
                                    .toDuration(DurationUnit.MILLISECONDS)
                            )
                        } else {
                            cell("")
                            cell("")
                        }
                        if (i < slowDurationByType.size) {
                            labelMetric(
                                slowDurationByType[i]!!.transformActionType.extractName() + "\n" + slowDurationByType[i]!!.artifactTransformExecutionName.replace(
                                    " ",
                                    "\n"
                                )
                            )
                            valueMetric(
                                slowDurationByType[i]!!.duration.toInt().roundMilliseconds()
                                    .toDuration(DurationUnit.MILLISECONDS)
                            )
                        } else {
                            cell("")
                            cell("")
                        }
                    }
                }
            }
        }
}
