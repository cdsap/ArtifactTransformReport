package io.github.cdsap.artifacttransform.cli.output

import io.github.cdsap.artifacttransform.cli.view.ArtifactTransformView
import io.github.cdsap.geapi.client.model.ArtifactTransform

open class ArtifactTransformReportOutput {
    open fun publish(
        transforms: List<ArtifactTransform>,
        singleReport: Boolean,
        timestamp: Long = System.currentTimeMillis(),
    ) {
        if (transforms.isEmpty()) {
            return
        }
        val view = ArtifactTransformView(transforms)
        view.print()
        SummaryTextOutput(view.renderSummaryText(), singleReport, timestamp).writeSummary()
        CsvOutput(transforms, singleReport, timestamp).writeCsv()
        HtmlOutput(transforms, singleReport, timestamp).writeHtml()
    }
}
