package io.github.cdsap.artifacttransform.cli.output

import io.github.cdsap.geapi.client.model.ArtifactTransform
import io.github.cdsap.geapi.client.model.ChangedAttributes
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintStream

class SummaryTextOutputTest {
    private val timestamp = 1_700_000_000_083L
    private val createdFiles = mutableListOf<File>()

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

    @AfterEach
    fun cleanup() {
        createdFiles.forEach { it.delete() }
        createdFiles.clear()
        listOf(
            "summary-artifact-transforms-$timestamp.txt",
            "single-summary-artifact-transforms-$timestamp.txt",
        ).forEach { File(it).delete() }
    }

    @Test
    fun `writes summary text file without single prefix`() {
        val stdout =
            captureStdout {
                SummaryTextOutput(sampleTransforms, false, timestamp).writeSummaryText()
            }

        val txt = File("summary-artifact-transforms-$timestamp.txt").also { createdFiles += it }
        assertTrue(txt.exists())
        assertTrue(stdout.contains("File summary-artifact-transforms-$timestamp.txt created"))
        val contents = txt.readText()
        assertTrue(contents.contains("Artifacts Transforms by outcome"))
        assertTrue(contents.contains("Artifacts Transforms by Avoidance Savings Outcome"))
        assertTrue(contents.contains("Artifact transforms by Build Scan"))
        assertTrue(
            contents.indexOf("Artifacts Transforms by outcome") <
                contents.indexOf("Artifacts Transforms by Avoidance Savings Outcome"),
        )
        assertTrue(
            contents.indexOf("Artifacts Transforms by Avoidance Savings Outcome") <
                contents.indexOf("Artifact transforms by Build Scan"),
        )
    }

    @Test
    fun `writes summary text file with single prefix`() {
        val stdout =
            captureStdout {
                SummaryTextOutput(sampleTransforms, true, timestamp).writeSummaryText()
            }

        val txt = File("single-summary-artifact-transforms-$timestamp.txt").also { createdFiles += it }
        assertTrue(txt.exists())
        assertFalse(File("summary-artifact-transforms-$timestamp.txt").exists())
        assertTrue(stdout.contains("File single-summary-artifact-transforms-$timestamp.txt created"))
    }

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
