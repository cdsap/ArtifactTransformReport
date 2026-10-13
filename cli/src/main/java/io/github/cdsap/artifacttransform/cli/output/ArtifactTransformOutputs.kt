package io.github.cdsap.artifacttransform.cli.output

import io.github.cdsap.artifacttransform.cli.view.ArtifactTransformView
import io.github.cdsap.geapi.client.model.ArtifactTransform

open class ArtifactTransformOutputs(
    private val singleBuild: Boolean,
) {
    private val reportScope: ReportScope = if (singleBuild) ReportScope.SingleBuild else ReportScope.Aggregate

    open fun writeIfPresent(
        transforms: List<ArtifactTransform>,
        onPresent: () -> Unit,
        timestamp: Long = System.currentTimeMillis(),
    ) {
        if (transforms.isEmpty()) {
            return
        }
        onPresent()
        write(transforms, timestamp)
    }

    open fun write(
        transforms: List<ArtifactTransform>,
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
