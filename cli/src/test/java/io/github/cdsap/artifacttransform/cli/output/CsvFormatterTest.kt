package io.github.cdsap.artifacttransform.cli.output

import io.github.cdsap.geapi.client.model.ArtifactTransform
import io.github.cdsap.geapi.client.model.ChangedAttributes
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class CsvFormatterTest {
    private val sampleTransforms =
        listOf(
            ArtifactTransform(
                "Execution1",
                "TransformType1",
                "Artifact1",
                "success",
                "executed_cacheable",
                "200",
                "100",
                "50",
                "300",
                arrayOf(ChangedAttributes("artifactType", "jar", "classpath-entry-snapshot")),
                "build1",
            ),
        )

    @Test
    fun `formats the CSV header and transform rows`() {
        val expected =
            "transformActionType,duration,avoidanceOutcome,buildScan,artifactTransformExecutionName," +
                "avoidanceSavings,fingerprintingDuration,changedAttributes.from,changedAttributes.to,cacheSize\n" +
                "TransformType1,200,executed_cacheable,build1,Execution1,100,50,jar,classpath-entry-snapshot,300\n"

        assertEquals(expected, CsvFormatter().format(sampleTransforms))
    }

    @Test
    fun `formats an empty transform list with only the header`() {
        assertEquals(
            "transformActionType,duration,avoidanceOutcome,buildScan,artifactTransformExecutionName," +
                "avoidanceSavings,fingerprintingDuration,changedAttributes.from,changedAttributes.to,cacheSize\n",
            CsvFormatter().format(emptyList()),
        )
    }
}
