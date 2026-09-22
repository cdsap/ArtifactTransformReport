package io.github.cdsap.artifacttransform.cli.report

import io.github.cdsap.artifacttransform.cli.output.CsvOutput
import io.github.cdsap.artifacttransform.cli.output.HtmlOutput
import io.github.cdsap.artifacttransform.cli.output.ReportScope
import io.github.cdsap.artifacttransform.cli.output.SummaryTextOutput
import io.github.cdsap.artifacttransform.cli.view.ArtifactTransformView
import io.github.cdsap.geapi.client.model.ArtifactTransform

open class ArtifactTransformReportWriter {
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
