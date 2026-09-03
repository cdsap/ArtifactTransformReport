package io.github.cdsap.artifacttransform.cli.output

import io.github.cdsap.artifacttransform.cli.view.ArtifactTransformView
import io.github.cdsap.geapi.client.model.ArtifactTransform

class ArtifactTransformReportOutput(
    private val transforms: List<ArtifactTransform>,
    private val singleReport: Boolean,
    private val timestamp: Long = System.currentTimeMillis(),
    private val summary: (() -> Unit)? = null
) {

    fun write() {
        if (transforms.isEmpty()) {
            return
        }
        summary?.invoke()
        ArtifactTransformView(transforms, singleReport, timestamp).print()
        CsvOutput(transforms, singleReport, timestamp).writeCsv()
        HtmlOutput(transforms, singleReport, timestamp).writeHtml()
    }
}
