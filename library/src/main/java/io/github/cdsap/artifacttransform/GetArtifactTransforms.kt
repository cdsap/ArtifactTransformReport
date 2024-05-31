package io.github.cdsap.artifacttransform

import io.github.cdsap.geapi.client.domain.impl.GetBuildsFromQueryWithAttributesRequest
import io.github.cdsap.geapi.client.domain.impl.GetBuildsWithArtifactTransformRequest
import io.github.cdsap.geapi.client.model.ArtifactTransform
import io.github.cdsap.geapi.client.model.Filter
import io.github.cdsap.geapi.client.repository.GradleEnterpriseRepository

class GetArtifactTransforms(
    private val filter: Filter,
    private val repository: GradleEnterpriseRepository
) {

    suspend fun get(): List<ArtifactTransform> {
        val getBuildScans = GetBuildsFromQueryWithAttributesRequest(repository)
        val transforms = GetBuildsWithArtifactTransformRequest(repository)
        val buildScansFiltered = getBuildScans.get(filter)
        return transforms.get(buildScansFiltered, filter)
    }
}
