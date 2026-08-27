package io.github.cdsap.artifacttransform.cli.output

import io.github.cdsap.artifacttransform.cli.view.ArtifactTransformView
import io.github.cdsap.geapi.client.model.ArtifactTransform

class ArtifactTransformReportOutputs(
    private val transforms: List<ArtifactTransform>,
    private val single: Boolean,
    private val timestamp: Long
) {

    fun write() {
        ArtifactTransformView(transforms, single, timestamp).print()
        CsvOutput(transforms, single, timestamp).writeCsv()
        HtmlOutput(transforms, single, timestamp).writeHtml()
    }
}
