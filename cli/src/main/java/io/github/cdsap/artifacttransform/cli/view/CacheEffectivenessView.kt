package io.github.cdsap.artifacttransform.cli.view

import com.jakewharton.picnic.TextAlignment
import com.jakewharton.picnic.table
import io.github.cdsap.artifacttransform.cacheEffectivenessByTransformActionType
import io.github.cdsap.artifacttransform.extractName
import io.github.cdsap.artifacttransform.overallCacheHitRate
import io.github.cdsap.geapi.client.model.ArtifactTransform
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class CacheEffectivenessView(transforms: List<ArtifactTransform>) {
    private val effectiveness = transforms.cacheEffectivenessByTransformActionType().take(10)
    private val hitRate = transforms.overallCacheHitRate()

    fun print() {
        println(table())
    }

    fun generateReport() = table()

    private fun percent(value: Double) = "${(value * 100).roundToTwoDecimalPlaces()}%"

    private fun table() = table {
        cellStyle {
            border = true
            alignment = TextAlignment.MiddleLeft
            paddingLeft = 1
            paddingRight = 1
        }
        body {
            row {
                cell("Cache Effectiveness (overall hit rate: ${percent(hitRate)})") {
                    columnSpan = 4
                    alignment = TextAlignment.MiddleCenter
                }
            }
            row {
                columnMetricHeader("Transform")
                columnMetricHeader("Hit rate")
                columnMetricHeader("Avoidable misses")
                columnMetricHeader("Avoidable miss duration")
            }
            effectiveness.forEach {
                row {
                    labelMetric(it.transformActionType.extractName())
                    valueMetric(percent(it.hitRate))
                    valueMetric(it.avoidableMisses)
                    valueMetric(
                        it.avoidableMissDuration.roundMilliseconds()
                            .toDuration(DurationUnit.MILLISECONDS)
                    )
                }
            }
        }
    }
}
