package io.github.cdsap.artifacttransform.cli.report

import io.github.cdsap.artifacttransform.cli.output.CsvOutput
import io.github.cdsap.artifacttransform.cli.output.HtmlOutput
import io.github.cdsap.artifacttransform.cli.output.SummaryTextOutput
import io.github.cdsap.artifacttransform.cli.view.ArtifactTransformView
import io.github.cdsap.geapi.client.model.ArtifactTransform

object ArtifactTransformReportPublisher {

    fun publish(
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
