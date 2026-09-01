package io.github.cdsap.artifacttransform.cli.report

import io.github.cdsap.artifacttransform.cli.output.ArtifactTransformReportOutputs
import io.github.cdsap.geapi.client.model.ArtifactTransform

class ReportOutputWriter(
    private val transforms: List<ArtifactTransform>,
    private val single: Boolean,
    private val timestamp: Long = System.currentTimeMillis(),
    private val summary: (() -> Unit)? = null
) {

    fun write() {
        if (transforms.isEmpty()) {
            return
        }
        summary?.invoke()
        ArtifactTransformReportOutputs(transforms, single, timestamp).write()
    }
}
