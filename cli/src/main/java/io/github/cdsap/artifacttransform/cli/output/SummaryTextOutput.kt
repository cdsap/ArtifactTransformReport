package io.github.cdsap.artifacttransform.cli.output

import io.github.cdsap.artifacttransform.cli.view.ArtifactTransformView
import io.github.cdsap.geapi.client.model.ArtifactTransform
import java.io.File

class SummaryTextOutput(
    private val transforms: List<ArtifactTransform>,
    private val single: Boolean,
    private val timestamp: Long,
) {

    fun writeSummaryText() {
        val prefixFile = if (single) "single-" else ""
        val txt = "${prefixFile}summary-artifact-transforms-$timestamp.txt"
        val startTimestamp = System.currentTimeMillis()
        File(txt).writeText(renderSummaryText())
        val endTime = System.currentTimeMillis()
        println("File $txt created in ${endTime - startTimestamp} ms")
    }

    private fun renderSummaryText(): String =
        ArtifactTransformView(transforms).renderSummaryText()
}
