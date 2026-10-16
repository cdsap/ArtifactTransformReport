package io.github.cdsap.artifacttransform.cli.report

import io.github.cdsap.artifacttransform.cli.output.CsvOutput
import io.github.cdsap.artifacttransform.cli.output.HtmlOutput
import io.github.cdsap.artifacttransform.cli.output.SummaryTextOutput
import io.github.cdsap.artifacttransform.cli.view.ArtifactTransformView
import io.github.cdsap.geapi.client.model.ArtifactTransform

open class ArtifactTransformReportOutput {
    open fun writeIfPresent(
        transforms: List<ArtifactTransform>,
        reportMode: ReportMode,
        onPresent: () -> Unit,
        timestamp: Long = System.currentTimeMillis(),
    ) {
        if (transforms.isEmpty()) {
            return
        }
        onPresent()
        write(transforms, reportMode, timestamp)
    }

    open fun write(
        transforms: List<ArtifactTransform>,
        reportMode: ReportMode,
        timestamp: Long = System.currentTimeMillis(),
    ) {
        if (transforms.isEmpty()) {
            return
        }
        val rendered = ArtifactTransformView(transforms, reportMode).render()
        rendered.print()
        SummaryTextOutput(rendered.asText(), reportMode, timestamp).writeSummary()
        CsvOutput(transforms, reportMode, timestamp).writeCsv()
        HtmlOutput(transforms, reportMode, timestamp).writeHtml()
    }
}
