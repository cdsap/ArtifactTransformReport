package io.github.cdsap.artifacttransform.cli.report

import io.github.cdsap.artifacttransform.GetSingleArtifactTransform
import io.github.cdsap.artifacttransform.cli.output.ArtifactTransformOutputs
import io.github.cdsap.geapi.client.model.ArtifactTransform
import io.github.cdsap.geapi.client.repository.GradleEnterpriseRepository

class SingleArtifactTransformReport(
    private val buildScanId: String,
    private val repository: GradleEnterpriseRepository,
    private val reportOutputs: ArtifactTransformOutputs = ArtifactTransformOutputs(singleBuild = true),
) {
    suspend fun process() {
        publishIfPresent(GetSingleArtifactTransform(repository).get(buildScanId))
    }

    internal fun publishIfPresent(transforms: List<ArtifactTransform>) {
        reportOutputs.writeIfPresent(transforms, onPresent = {
            println("Build $buildScanId - Total Artifact transforms: ${transforms.size} ")
        })
    }
}
