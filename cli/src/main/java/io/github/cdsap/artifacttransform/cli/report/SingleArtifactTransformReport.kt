package io.github.cdsap.artifacttransform.cli.report

import io.github.cdsap.artifacttransform.GetSingleArtifactTransform
import io.github.cdsap.artifacttransform.cli.output.ArtifactTransformReportOutput
import io.github.cdsap.artifacttransform.cli.output.ReportScope
import io.github.cdsap.geapi.client.model.ArtifactTransform
import io.github.cdsap.geapi.client.repository.GradleEnterpriseRepository

class SingleArtifactTransformReport(
    private val buildScanId: String,
    private val repository: GradleEnterpriseRepository,
    private val reportOutput: ArtifactTransformReportOutput = ArtifactTransformReportOutput(),
) {
    suspend fun process() {
        publishIfPresent(GetSingleArtifactTransform(repository).get(buildScanId))
    }

    internal fun publishIfPresent(transforms: List<ArtifactTransform>) {
        if (transforms.isNotEmpty()) {
            println("Build $buildScanId - Total Artifact transforms: ${transforms.size} ")
            reportOutput.write(transforms, ReportScope.SingleBuild)
        }
    }
}
