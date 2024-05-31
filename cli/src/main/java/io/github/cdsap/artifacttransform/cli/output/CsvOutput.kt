package io.github.cdsap.artifacttransform.cli.output

import io.github.cdsap.geapi.client.model.ArtifactTransform
import java.io.BufferedWriter
import java.io.File

class CsvOutput(
    private val transforms: List<ArtifactTransform>,
    private val single: Boolean
) {

    fun writeCsv() {
        val prefixFile = if (single) "single-" else ""
        val csv = "${prefixFile}artifact-transforms-${System.currentTimeMillis()}.csv"
        val headers =
            "transformActionType,duration,avoidanceOutcome,buildScan,artifactTransformExecutionName,avoidanceSavings,fingerprintingDuration,changedAttributes.from,changedAttributes.to,cacheSize\n"
        val startTimestamp = System.currentTimeMillis()
        File(csv).bufferedWriter().use { out: BufferedWriter ->
            out.write(headers)
            transforms.forEach { transform ->
                val line =
                    "${transform.transformActionType},${transform.duration},${transform.avoidanceOutcome},${transform.buildScanId},${transform.artifactTransformExecutionName},${transform.avoidanceSavings},${transform.fingerprintingDuration},${transform.changedAttributes[0].from},${transform.changedAttributes[0].to},${transform.cacheArtifactSize}\n"
                out.write(line)
            }
        }
        val endTime = System.currentTimeMillis()
        println("File $csv created in ${endTime - startTimestamp} ms")
    }
}
