package io.github.cdsap.artifacttransform.cli.view

import io.github.cdsap.geapi.client.model.ArtifactTransform
import io.github.cdsap.geapi.client.model.ChangedAttributes
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintStream

class ArtifactTransformViewTest {
    private val timestamp = 1_700_000_000_083L

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

    private val sectionMarkers =
        listOf(
            "Artifacts Transforms by outcome",
            "Artifacts Transforms by Avoidance Savings Outcome",
            "Artifacts Transforms by Type",
            "Negative Transforms by Type",
            "Duration by Artifact transform dependency",
            "Cache Size",
            "Slowest Artifact Transforms",
            "Cache Effectiveness",
            "Artifact transforms by changed attributes",
            "Artifact transforms by Build Scan",
        )

    @Test
    fun `prints console sections without writing summary file`() {
        val stdout =
            captureStdout {
                ArtifactTransformView(sampleTransforms).print()
            }

        assertTrue(stdout.contains("Artifacts Transforms by outcome"))
        assertTrue(stdout.contains("Artifacts Transforms by Avoidance Savings Outcome"))
        assertFalse(stdout.contains("File "))
        assertFalse(File("summary-artifact-transforms-$timestamp.txt").exists())
        assertFalse(File("single-summary-artifact-transforms-$timestamp.txt").exists())
    }

    @Test
    fun `console and summary text share the same section order`() {
        val view = ArtifactTransformView(sampleTransforms)
        val console =
            captureStdout {
                view.print()
            }
        val summary = view.renderSummaryText()

        assertEquals(appearingSectionOrder(console), appearingSectionOrder(summary))
        assertTrue(appearingSectionOrder(summary).size >= 9)
    }

    private fun appearingSectionOrder(text: String): List<String> =
        sectionMarkers
            .filter { text.contains(it) }
            .sortedBy { text.indexOf(it) }

    private fun captureStdout(block: () -> Unit): String {
        val original = System.out
        val buffer = ByteArrayOutputStream()
        System.setOut(PrintStream(buffer))
        try {
            block()
        } finally {
            System.setOut(original)
        }
        return buffer.toString()
    }
}
