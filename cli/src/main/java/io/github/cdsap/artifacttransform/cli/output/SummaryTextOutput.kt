package io.github.cdsap.artifacttransform.cli.output

import com.jakewharton.picnic.renderText
import io.github.cdsap.artifacttransform.cli.view.AttributeTransitionView
import io.github.cdsap.artifacttransform.cli.view.AvoidanceSavingsOutcomeView
import io.github.cdsap.artifacttransform.cli.view.BuildScanView
import io.github.cdsap.artifacttransform.cli.view.CacheEffectivenessView
import io.github.cdsap.artifacttransform.cli.view.CacheSizeView
import io.github.cdsap.artifacttransform.cli.view.DependencyView
import io.github.cdsap.artifacttransform.cli.view.NegativeAvoidanceView
import io.github.cdsap.artifacttransform.cli.view.OutcomeView
import io.github.cdsap.artifacttransform.cli.view.SlowestView
import io.github.cdsap.artifacttransform.cli.view.TransformsByType
import io.github.cdsap.geapi.client.model.ArtifactTransform
import java.io.File

class SummaryTextOutput(
    private val transforms: List<ArtifactTransform>,
    private val single: Boolean,
    private val timestamp: Long,
) {

    fun writeSummaryText() {
        val prefixFile = if (single) "single-" else ""
        val txt = "${prefixFile}summary-artifact-transforms-$timestamp.txt"
        val startTimestamp = System.currentTimeMillis()
        File(txt).writeText(renderSummaryText())
        val endTime = System.currentTimeMillis()
        println("File $txt created in ${endTime - startTimestamp} ms")
    }

    private fun renderSummaryText(): String {
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

        return outcomeView.generateReport().renderText() +
            "\n" +
            avoidanceView.generateReport().renderText() +
            "\n" +
            transformsByType.generateReport().renderText() +
            "\n" +
            negativeAvoidanceView.generateReport().renderText() +
            "\n" +
            dependencyView.generateReport().renderText() +
            "\n" +
            cacheSizeView.generateReport().renderText() +
            "\n" +
            slowestView.generateReport().renderText() +
            "\n" +
            cacheEffectivenessView.generateReport().renderText() +
            "\n" +
            attributeTransitionView.generateReport().renderText() +
            "\n" +
            buildScanView.generateReport().renderText()
    }
}
