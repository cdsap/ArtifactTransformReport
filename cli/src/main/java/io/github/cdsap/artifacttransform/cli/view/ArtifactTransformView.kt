package io.github.cdsap.artifacttransform.cli.view

import com.jakewharton.picnic.Table
import com.jakewharton.picnic.renderText
import io.github.cdsap.geapi.client.model.ArtifactTransform

class ArtifactTransformView(
    val transforms: List<ArtifactTransform>,
) {

    fun render(): RenderedArtifactTransformReport =
        RenderedArtifactTransformReport(orderedSectionTables().map { it.renderText() })

    fun print() {
        render().print()
    }

    fun renderSummaryText(): String = render().asText()

    private fun orderedSectionTables(): List<Table> {
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
            outcomeView.generateReport(),
            avoidanceView.generateReport(),
            transformsByType.generateReport(),
            negativeAvoidanceView.generateReport(),
            dependencyView.generateReport(),
            cacheSizeView.generateReport(),
            slowestView.generateReport(),
            cacheEffectivenessView.generateReport(),
            attributeTransitionView.generateReport(),
            buildScanView.generateReport(),
        )
    }
}

class RenderedArtifactTransformReport(
    private val sections: List<String>,
) {
    fun print() {
        sections.filter { it.isNotBlank() }.forEach { println(it) }
    }

    fun asText(): String = sections.joinToString("\n")
}
