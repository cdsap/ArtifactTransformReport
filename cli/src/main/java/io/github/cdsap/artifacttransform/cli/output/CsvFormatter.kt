package io.github.cdsap.artifacttransform.cli.output

import io.github.cdsap.geapi.client.model.ArtifactTransform

class CsvFormatter {
    fun format(transforms: List<ArtifactTransform>): String =
        buildString {
            append(HEADER)
            transforms.forEach { transform ->
                append(
                    "${transform.transformActionType},${transform.duration},${transform.avoidanceOutcome}," +
                        "${transform.buildScanId},${transform.artifactTransformExecutionName}," +
                        "${transform.avoidanceSavings},${transform.fingerprintingDuration}," +
                        "${transform.changedAttributes[0].from},${transform.changedAttributes[0].to}," +
                        "${transform.cacheArtifactSize}\n",
                )
            }
        }

    private companion object {
        const val HEADER =
            "transformActionType,duration,avoidanceOutcome,buildScan,artifactTransformExecutionName," +
                "avoidanceSavings,fingerprintingDuration,changedAttributes.from,changedAttributes.to,cacheSize\n"
    }
}
