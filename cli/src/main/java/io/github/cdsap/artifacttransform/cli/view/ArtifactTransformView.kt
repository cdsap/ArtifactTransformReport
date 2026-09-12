package io.github.cdsap.artifacttransform.cli.view

import com.jakewharton.picnic.Table
import com.jakewharton.picnic.renderText
import io.github.cdsap.geapi.client.model.ArtifactTransform

class ArtifactTransformView(
    val transforms: List<ArtifactTransform>,
) {

    fun print() {
        orderedSections().forEach { it.print() }
    }

    fun renderSummaryText(): String =
        orderedSections().joinToString("\n") { it.generateReport().renderText() }

    private fun orderedSections(): List<ReportSection> {
        val outcomeView = OutcomeView(transforms)
        val avoidanceView = AvoidanceSavingsOutcomeView(transforms)
        val transformsByType = TransformsByType(transforms)
        val negativeAvoidanceView = NegativeAvoidanceView(transforms)
        val dependencyView = DependencyView(transforms)
        val cacheSizeView = CacheSizeView(transforms)
        val slowestView = SlowestView(transforms)
        val cacheEffectivenessView = CacheEffectivenessView(transforms)
        val attributeTransitionView = AttributeTransitionView(transforms)
        val buildScanView = BuildScanView(transforms)

        return listOf(
            ReportSection(outcomeView::print, outcomeView::generateReport),
            ReportSection(avoidanceView::print, avoidanceView::generateReport),
            ReportSection(transformsByType::print, transformsByType::generateReport),
            ReportSection(negativeAvoidanceView::print, negativeAvoidanceView::generateReport),
            ReportSection(dependencyView::print, dependencyView::generateReport),
            ReportSection(cacheSizeView::print, cacheSizeView::generateReport),
            ReportSection(slowestView::print, slowestView::generateReport),
            ReportSection(cacheEffectivenessView::print, cacheEffectivenessView::generateReport),
            ReportSection(attributeTransitionView::print, attributeTransitionView::generateReport),
            ReportSection(buildScanView::print, buildScanView::generateReport),
        )
    }

    private class ReportSection(
        private val printSection: () -> Unit,
        private val reportSection: () -> Table,
    ) {
        fun print() = printSection()

        fun generateReport() = reportSection()
    }
}
