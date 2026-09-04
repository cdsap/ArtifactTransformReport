package io.github.cdsap.artifacttransform.cli.report

import io.github.cdsap.artifacttransform.GetArtifactTransforms
import io.github.cdsap.geapi.client.model.Filter
import io.github.cdsap.geapi.client.repository.GradleEnterpriseRepository

class ArtifactTransformReport(
    private val filter: Filter,
    private val repository: GradleEnterpriseRepository
) {

    suspend fun process() {
        val transforms = GetArtifactTransforms(filter, repository).get()
        if (transforms.isNotEmpty()) {
            println("Total Artifact transforms: ${transforms.size}")
            println("Build Scans with Artifact transforms: ${transforms.groupBy { it.buildScanId }.count()}")
            ArtifactTransformReportEmitter.emit(transforms, false)
        }
    }
}
