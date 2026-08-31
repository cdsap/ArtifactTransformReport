package io.github.cdsap.artifacttransform.cli.output

import io.github.cdsap.artifacttransform.cli.view.ArtifactTransformView
import io.github.cdsap.geapi.client.model.ArtifactTransform

class ArtifactTransformReportOutputs(
    private val transforms: List<ArtifactTransform>,
    private val singleReport: Boolean,
    private val timestamp: Long
) {

    fun write() {
        ArtifactTransformView(transforms, singleReport, timestamp).print()
        CsvOutput(transforms, singleReport, timestamp).writeCsv()
        HtmlOutput(transforms, singleReport, timestamp).writeHtml()
    }
}
