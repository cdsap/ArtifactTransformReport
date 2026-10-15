package io.github.cdsap.artifacttransform.cli.output

import io.github.cdsap.artifacttransform.cli.report.ReportMode
import io.github.cdsap.geapi.client.model.ArtifactTransform
import java.io.BufferedWriter
import java.io.File

class CsvOutput(
    private val transforms: List<ArtifactTransform>,
    private val reportMode: ReportMode,
    private val timestamp: Long
) {

    fun writeCsv() {
        val csv = "${reportMode.fileNamePrefix}artifact-transforms-$timestamp.csv"
        val content = CsvFormatter().format(transforms)
        val startTimestamp = System.currentTimeMillis()
        File(csv).bufferedWriter().use { out: BufferedWriter ->
            out.write(content)
        }
        val endTime = System.currentTimeMillis()
        println("File $csv created in ${endTime - startTimestamp} ms")
    }
}
