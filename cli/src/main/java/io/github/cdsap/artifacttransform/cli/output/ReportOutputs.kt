package io.github.cdsap.artifacttransform.cli.output

import io.github.cdsap.artifacttransform.cli.view.ArtifactTransformView
import io.github.cdsap.geapi.client.model.ArtifactTransform

object ReportOutputs {

    fun emit(
        transforms: List<ArtifactTransform>,
        singleReport: Boolean,
        timestamp: Long = System.currentTimeMillis(),
        summaryLines: List<String> = emptyList(),
    ) {
        if (transforms.isEmpty()) {
            return
        }
        summaryLines.forEach { println(it) }
        ArtifactTransformView(transforms).print()
        SummaryTextOutput(transforms, singleReport, timestamp).writeSummaryText()
        CsvOutput(transforms, singleReport, timestamp).writeCsv()
        HtmlOutput(transforms, singleReport, timestamp).writeHtml()
    }
}
