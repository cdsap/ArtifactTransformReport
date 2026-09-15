package io.github.cdsap.artifacttransform.cli.output

import java.io.File

class SummaryTextOutput(
    private val summaryText: String,
    private val single: Boolean,
    private val timestamp: Long,
) {

    fun writeSummary() {
        val prefixFile = if (single) "single-" else ""
        val txt = "${prefixFile}summary-artifact-transforms-$timestamp.txt"
        val startTimestamp = System.currentTimeMillis()
        File(txt).writeText(summaryText)
        val endTime = System.currentTimeMillis()
        println("File $txt created in ${endTime - startTimestamp} ms")
    }
}
