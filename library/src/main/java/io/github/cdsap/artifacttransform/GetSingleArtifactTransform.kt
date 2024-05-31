package io.github.cdsap.artifacttransform

import io.github.cdsap.geapi.client.domain.impl.GetSingleBuildArtifactTransformRequest
import io.github.cdsap.geapi.client.model.ArtifactTransform
import io.github.cdsap.geapi.client.repository.GradleEnterpriseRepository

class GetSingleArtifactTransform(
    private val repository: GradleEnterpriseRepository
) {

    suspend fun get(buildScanId: String): List<ArtifactTransform> {
        return GetSingleBuildArtifactTransformRequest(repository).get(buildScanId)
    }
}
