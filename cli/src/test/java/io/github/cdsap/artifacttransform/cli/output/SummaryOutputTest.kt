package io.github.cdsap.artifacttransform.cli.output

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintStream

class SummaryOutputTest {
    private val timestamp = 1_700_000_000_083L
    private val createdFiles = mutableListOf<File>()
    private val sampleSummary =
        """
        Artifacts Transforms by outcome
        Artifacts Transforms by Avoidance Savings Outcome
        Artifact transforms by Build Scan
        """.trimIndent()

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
                SummaryOutput(sampleSummary, false, timestamp).writeSummary()
            }

        val txt = File("summary-artifact-transforms-$timestamp.txt").also { createdFiles += it }
        assertTrue(txt.exists())
        assertEquals(sampleSummary, txt.readText())
        assertTrue(stdout.contains("File summary-artifact-transforms-$timestamp.txt created"))
        assertTrue(stdout.contains(" ms"))
    }

    @Test
    fun `writes summary text file with single prefix`() {
        val stdout =
            captureStdout {
                SummaryOutput(sampleSummary, true, timestamp).writeSummary()
            }

        val txt = File("single-summary-artifact-transforms-$timestamp.txt").also { createdFiles += it }
        assertTrue(txt.exists())
        assertEquals(sampleSummary, txt.readText())
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
