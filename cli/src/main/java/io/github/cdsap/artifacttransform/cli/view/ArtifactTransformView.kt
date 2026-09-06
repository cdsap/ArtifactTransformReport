package io.github.cdsap.artifacttransform.cli.view

import io.github.cdsap.geapi.client.model.ArtifactTransform

class ArtifactTransformView(
    val transforms: List<ArtifactTransform>,
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
    }
}
