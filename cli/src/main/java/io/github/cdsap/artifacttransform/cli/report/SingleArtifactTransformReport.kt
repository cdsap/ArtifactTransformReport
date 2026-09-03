package io.github.cdsap.artifacttransform.cli.report

import io.github.cdsap.artifacttransform.GetSingleArtifactTransform
import io.github.cdsap.geapi.client.repository.GradleEnterpriseRepository

class SingleArtifactTransformReport(
    private val buildScanId: String,
    private val repository: GradleEnterpriseRepository
) {

    suspend fun process() {
        val transforms = GetSingleArtifactTransform(repository).get(buildScanId)
        ArtifactTransformReportWriter(transforms, true) {
            println("Build $buildScanId - Total Artifact transforms: ${transforms.size} ")
        }.write()
    }
}
