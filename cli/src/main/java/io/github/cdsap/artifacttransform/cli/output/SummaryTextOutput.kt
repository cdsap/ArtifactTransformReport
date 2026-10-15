package io.github.cdsap.artifacttransform.cli.output

import io.github.cdsap.artifacttransform.cli.report.ReportMode
import java.io.File

class SummaryTextOutput(
    private val summaryText: String,
    private val reportMode: ReportMode,
    private val timestamp: Long,
) {

    fun writeSummary() {
        val txt = "${reportMode.fileNamePrefix}summary-artifact-transforms-$timestamp.txt"
        val startTimestamp = System.currentTimeMillis()
        File(txt).writeText(summaryText)
        val endTime = System.currentTimeMillis()
        println("File $txt created in ${endTime - startTimestamp} ms")
    }
}
