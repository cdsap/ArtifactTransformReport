package io.github.cdsap.artifacttransform.cli.output

import java.io.File

class SummaryTextOutput(
    private val summaryText: String,
    private val reportScope: ReportScope,
    private val timestamp: Long,
) {

    fun writeSummary() {
        val txt = "${reportScope.fileNamePrefix}summary-artifact-transforms-$timestamp.txt"
        val startTimestamp = System.currentTimeMillis()
        File(txt).writeText(summaryText)
        val endTime = System.currentTimeMillis()
        println("File $txt created in ${endTime - startTimestamp} ms")
    }
}
