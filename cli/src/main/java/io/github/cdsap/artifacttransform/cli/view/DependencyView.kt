package io.github.cdsap.artifacttransform.cli.view

import com.jakewharton.picnic.TextAlignment
import com.jakewharton.picnic.table
import io.github.cdsap.artifacttransform.dependencyByInputArtifactName
import io.github.cdsap.artifacttransform.dependencySortedByDuration
import io.github.cdsap.geapi.client.model.ArtifactTransform
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class DependencyView(transforms: List<ArtifactTransform>) {
    private val dependencyDuration = transforms.dependencySortedByDuration().take(10)
    private val dependencyInputName = transforms.dependencyByInputArtifactName().take(10)

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
                cell("Duration by Artifact transform dependency") {
                    columnSpan = 4
                    alignment = TextAlignment.MiddleCenter
                }
            }
            row {
                columnMetricHeaderWithSpan("Aggregated duration by dependency")
                columnMetricHeaderWithSpan("Aggregated duration by input artifact")
            }
            val maxSize = maxOf(dependencyDuration.size, dependencyInputName.size)
            for (i in 0 until maxSize) {
                row {
                    if (i < dependencyDuration.size) {
                        labelMetric(dependencyDuration[i].first)
                        valueMetric(
                            dependencyDuration[i].second.roundMilliseconds()
                                .toDuration(DurationUnit.MILLISECONDS)
                        )
                    } else {
                        cell("")
                        cell("")
                    }
                    if (i < dependencyInputName.size) {
                        labelMetric(dependencyInputName[i].first)
                        valueMetric(
                            dependencyInputName[i].second.roundMilliseconds()
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
