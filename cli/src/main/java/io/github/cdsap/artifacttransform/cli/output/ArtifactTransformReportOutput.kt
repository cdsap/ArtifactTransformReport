package io.github.cdsap.artifacttransform.cli.output

import io.github.cdsap.artifacttransform.cli.view.ArtifactTransformView
import io.github.cdsap.geapi.client.model.ArtifactTransform

open class ArtifactTransformReportOutput {
    open fun write(
        transforms: List<ArtifactTransform>,
        reportScope: ReportScope,
        timestamp: Long = System.currentTimeMillis(),
    ) {
        if (transforms.isEmpty()) {
            return
        }
        val rendered = ArtifactTransformView(transforms).render()
        rendered.print()
        SummaryTextOutput(rendered.asText(), reportScope, timestamp).writeSummary()
        CsvOutput(transforms, reportScope, timestamp).writeCsv()
        HtmlOutput(transforms, reportScope, timestamp).writeHtml()
    }
}
