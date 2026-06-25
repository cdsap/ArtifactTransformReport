package io.github.cdsap.artifacttransform.cli.view

import com.jakewharton.picnic.renderText
import io.github.cdsap.geapi.client.model.ArtifactTransform
import java.io.File

class ArtifactTransformView(
    val transforms: List<ArtifactTransform>,
    val singleReport: Boolean,
    val timestamp: Long
) {

    fun print() {
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

        outcomeView.print()
        avoidanceView.print()
        transformsByType.print()
        negativeAvoidanceView.print()
        dependencyView.print()
        cacheSizeView.print()
        slowestView.print()
        cacheEffectivenessView.print()
        attributeTransitionView.print()
        buildScanView.print()

        val prefixFile = if (singleReport) "single-" else ""
        val txt = "${prefixFile}summary-artifact-transforms-$timestamp.txt"
        val startTimestamp = System.currentTimeMillis()
        File(txt).writeText(
            outcomeView.generateReport().renderText() +
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
        )
        val endTime = System.currentTimeMillis()
        println("File $txt created in ${endTime - startTimestamp} ms")
    }
}
