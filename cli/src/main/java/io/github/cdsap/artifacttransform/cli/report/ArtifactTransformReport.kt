package io.github.cdsap.artifacttransform.cli.report

import io.github.cdsap.artifacttransform.GetArtifactTransforms
import io.github.cdsap.artifacttransform.cli.output.ArtifactTransformOutputs
import io.github.cdsap.geapi.client.model.ArtifactTransform
import io.github.cdsap.geapi.client.model.Filter
import io.github.cdsap.geapi.client.repository.GradleEnterpriseRepository

class ArtifactTransformReport(
    private val filter: Filter,
    private val repository: GradleEnterpriseRepository,
    private val reportOutputs: ArtifactTransformOutputs = ArtifactTransformOutputs(singleBuild = false),
) {
    suspend fun process() {
        publishIfPresent(GetArtifactTransforms(filter, repository).get())
    }

    internal fun publishIfPresent(transforms: List<ArtifactTransform>) {
        reportOutputs.writeIfPresent(transforms, onPresent = {
            println("Total Artifact transforms: ${transforms.size}")
            println("Build Scans with Artifact transforms: ${transforms.groupBy { it.buildScanId }.count()}")
        })
    }
}
