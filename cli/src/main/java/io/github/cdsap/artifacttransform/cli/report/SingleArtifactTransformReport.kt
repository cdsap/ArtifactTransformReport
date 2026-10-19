package io.github.cdsap.artifacttransform.cli.report

import io.github.cdsap.artifacttransform.GetSingleArtifactTransform
import io.github.cdsap.artifacttransform.cli.output.ArtifactTransformOutput
import io.github.cdsap.geapi.client.model.ArtifactTransform
import io.github.cdsap.geapi.client.repository.GradleEnterpriseRepository

class SingleArtifactTransformReport(
    private val buildScanId: String,
    private val repository: GradleEnterpriseRepository,
    private val reportOutput: ArtifactTransformOutput = ArtifactTransformOutput(),
) {
    suspend fun process() {
        publishIfPresent(GetSingleArtifactTransform(repository).get(buildScanId))
    }

    internal fun publishIfPresent(transforms: List<ArtifactTransform>) {
        reportOutput.writeIfPresent(transforms, ReportMode.SingleBuild, onPresent = {
            println("Build $buildScanId - Total Artifact transforms: ${transforms.size} ")
        })
    }
}
