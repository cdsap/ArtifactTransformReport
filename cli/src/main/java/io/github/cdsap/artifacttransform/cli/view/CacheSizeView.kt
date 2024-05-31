package io.github.cdsap.artifacttransform.cli.view

import com.jakewharton.picnic.Table
import com.jakewharton.picnic.TextAlignment
import com.jakewharton.picnic.table
import io.github.cdsap.artifacttransform.aggregatedCacheSizeByTransformActionType
import io.github.cdsap.artifacttransform.cacheSizeByTransformActionType
import io.github.cdsap.artifacttransform.extractName
import io.github.cdsap.geapi.client.model.ArtifactTransform

class CacheSizeView(transforms: List<ArtifactTransform>) {
    private val cacheSizes = transforms.cacheSizeByTransformActionType().take(10)
    private val aggregatedSize = transforms.aggregatedCacheSizeByTransformActionType().take(10)

    fun print() {
        if (cacheSizes.isNotEmpty() || aggregatedSize.isNotEmpty()) {
            println(table())
        }
    }

    fun generateReport(): Table {
        return if (cacheSizes.isNotEmpty() || aggregatedSize.isNotEmpty()) {
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
                cell("Cache Size") {
                    columnSpan = 4
                    alignment = TextAlignment.MiddleCenter
                }
            }
            row {
                columnMetricHeaderWithSpan("Heavier cache transform outputs")
                columnMetricHeaderWithSpan("Aggregated Size by Transform")
            }
            val maxSize = maxOf(cacheSizes.size, aggregatedSize.size)
            for (i in 0 until maxSize) {
                row {
                    if (i < cacheSizes.size) {
                        labelMetric(cacheSizes[i].first)
                        valueMetric(
                            formatBytes(cacheSizes[i].second.toLong())
                        )
                    } else {
                        cell("")
                        cell("")
                    }
                    if (i < aggregatedSize.size) {
                        labelMetric(aggregatedSize[i].first.extractName())
                        valueMetric(
                            formatBytes(
                                aggregatedSize[i].second.toLong()
                            )
                        )
                    } else {
                        cell("")
                        cell("")
                    }
                }
            }
        }
    }

    private fun formatBytes(bytes: Long): String {
        val bytesInAKilobyte = 1_024.0
        val bytesInAMegabyte = bytesInAKilobyte * 1_024
        val bytesInAGigabyte = bytesInAMegabyte * 1_024

        return when {
            bytes >= bytesInAGigabyte -> String.format("%.2f GB", bytes / bytesInAGigabyte)
            bytes >= bytesInAMegabyte -> String.format("%.2f MB", bytes / bytesInAMegabyte)
            else -> String.format("%.2f KB", bytes / bytesInAKilobyte)
        }
    }
}
