package io.github.cdsap.artifacttransform.cli.report

import io.github.cdsap.artifacttransform.cli.output.CsvOutput
import io.github.cdsap.artifacttransform.cli.output.HtmlOutput
import io.github.cdsap.artifacttransform.cli.view.ArtifactTransformView
import io.github.cdsap.geapi.client.model.ArtifactTransform

object TransformReportPublisher {

    fun publish(
        transforms: List<ArtifactTransform>,
        singleReport: Boolean,
        timestamp: Long = System.currentTimeMillis(),
    ) {
        if (transforms.isEmpty()) {
            return
        }
        ArtifactTransformView(transforms, singleReport, timestamp).print()
        CsvOutput(transforms, singleReport, timestamp).writeCsv()
        HtmlOutput(transforms, singleReport, timestamp).writeHtml()
    }
}
