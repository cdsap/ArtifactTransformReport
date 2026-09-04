package io.github.cdsap.artifacttransform.cli.report

import io.github.cdsap.geapi.client.model.ArtifactTransform
import io.github.cdsap.geapi.client.model.ChangedAttributes
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintStream

class ArtifactTransformReportEmitterTest {
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
            "artifact-transforms-$timestamp.csv",
            "artifact-transforms-$timestamp.html",
            "single-summary-artifact-transforms-$timestamp.txt",
            "single-artifact-transforms-$timestamp.csv",
            "single-artifact-transforms-$timestamp.html",
        ).forEach { File(it).delete() }
    }

    @Test
    fun `multi-build path writes text csv and html without single prefix`() {
        val stdout =
            captureStdout {
                // Mirror ArtifactTransformReport preamble + shared emit
                println("Total Artifact transforms: ${sampleTransforms.size}")
                println("Build Scans with Artifact transforms: ${sampleTransforms.groupBy { it.buildScanId }.count()}")
                ArtifactTransformReportEmitter.emit(sampleTransforms, false, timestamp)
            }

        val txt = File("summary-artifact-transforms-$timestamp.txt").also { createdFiles += it }
        val csv = File("artifact-transforms-$timestamp.csv").also { createdFiles += it }
        val html = File("artifact-transforms-$timestamp.html").also { createdFiles += it }

        assertTrue(txt.exists())
        assertTrue(csv.exists())
        assertTrue(html.exists())
        assertTrue(csv.readText().startsWith("transformActionType,"))
        assertTrue(html.readText().contains("<html"))
        assertTrue(stdout.contains("Total Artifact transforms: 1"))
        assertTrue(stdout.contains("Build Scans with Artifact transforms: 1"))
        assertOutputOrder(
            stdout,
            "summary-artifact-transforms-$timestamp.txt",
            "artifact-transforms-$timestamp.csv",
            "artifact-transforms-$timestamp.html",
        )
        assertTrue(
            stdout.indexOf("Total Artifact transforms: 1") <
                stdout.indexOf("File summary-artifact-transforms-$timestamp.txt created"),
        )
    }

    @Test
    fun `single-build path writes text csv and html with single prefix`() {
        val stdout =
            captureStdout {
                // Mirror SingleArtifactTransformReport preamble + shared emit
                println("Build build1 - Total Artifact transforms: ${sampleTransforms.size} ")
                ArtifactTransformReportEmitter.emit(sampleTransforms, true, timestamp)
            }

        val txt = File("single-summary-artifact-transforms-$timestamp.txt").also { createdFiles += it }
        val csv = File("single-artifact-transforms-$timestamp.csv").also { createdFiles += it }
        val html = File("single-artifact-transforms-$timestamp.html").also { createdFiles += it }

        assertTrue(txt.exists())
        assertTrue(csv.exists())
        assertTrue(html.exists())
        assertTrue(stdout.contains("Build build1 - Total Artifact transforms: 1 "))
        assertOutputOrder(
            stdout,
            "single-summary-artifact-transforms-$timestamp.txt",
            "single-artifact-transforms-$timestamp.csv",
            "single-artifact-transforms-$timestamp.html",
        )
    }

    @Test
    fun `empty transforms skip file outputs`() {
        val stdout =
            captureStdout {
                ArtifactTransformReportEmitter.emit(emptyList(), false, timestamp)
            }

        assertFalse(File("summary-artifact-transforms-$timestamp.txt").exists())
        assertFalse(File("artifact-transforms-$timestamp.csv").exists())
        assertFalse(File("artifact-transforms-$timestamp.html").exists())
        assertFalse(stdout.contains("File "))
    }

    private fun assertOutputOrder(
        stdout: String,
        vararg fileNames: String,
    ) {
        val indices =
            fileNames.map { name ->
                val idx = stdout.indexOf("File $name created")
                assertTrue(idx >= 0, "Expected creation message for $name in:\n$stdout")
                idx
            }
        assertEquals(indices.sorted(), indices, "Outputs must be written in text, CSV, then HTML order")
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
