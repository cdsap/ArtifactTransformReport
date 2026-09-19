package io.github.cdsap.artifacttransform.cli.report

import io.github.cdsap.geapi.client.model.ArtifactTransform
import io.github.cdsap.geapi.client.model.ChangedAttributes
import io.github.cdsap.geapi.client.model.Filter
import io.github.cdsap.geapi.client.repository.GradleEnterpriseRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintStream

class ArtifactTransformReportOrchestrationTest {
    private val timestamp = 1_700_000_000_099L

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
    fun `aggregate report delegates to replaced collaborator without writing files`() {
        val recording = RecordingReportOutput()
        val report = ArtifactTransformReport(Filter(), UnusedRepository, recording)

        val stdout =
            captureStdout {
                report.publishIfPresent(sampleTransforms)
            }

        assertEquals(listOf(PublishCall(sampleTransforms, false)), recording.calls)
        assertTrue(stdout.contains("Total Artifact transforms: 1"))
        assertTrue(stdout.contains("Build Scans with Artifact transforms: 1"))
        assertNoOutputFiles()
    }

    @Test
    fun `single report delegates to replaced collaborator without writing files`() {
        val recording = RecordingReportOutput()
        val report = SingleArtifactTransformReport("build1", UnusedRepository, recording)

        val stdout =
            captureStdout {
                report.publishIfPresent(sampleTransforms)
            }

        assertEquals(listOf(PublishCall(sampleTransforms, true)), recording.calls)
        assertTrue(stdout.contains("Build build1 - Total Artifact transforms: 1 "))
        assertNoOutputFiles()
    }

    @Test
    fun `empty results skip collaborator and write no files`() {
        val recording = RecordingReportOutput()
        ArtifactTransformReport(Filter(), UnusedRepository, recording).publishIfPresent(emptyList())
        SingleArtifactTransformReport("build1", UnusedRepository, recording).publishIfPresent(emptyList())

        assertTrue(recording.calls.isEmpty())
        assertNoOutputFiles()
    }

    private fun assertNoOutputFiles() {
        listOf(
            "summary-artifact-transforms-$timestamp.txt",
            "artifact-transforms-$timestamp.csv",
            "artifact-transforms-$timestamp.html",
            "single-summary-artifact-transforms-$timestamp.txt",
            "single-artifact-transforms-$timestamp.csv",
            "single-artifact-transforms-$timestamp.html",
        ).forEach { name ->
            assertFalse(File(name).exists(), "Expected no output file $name")
        }
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

    private data class PublishCall(
        val transforms: List<ArtifactTransform>,
        val singleReport: Boolean,
    )

    private class RecordingReportOutput : ArtifactTransformReportOutput() {
        val calls = mutableListOf<PublishCall>()

        override fun publish(
            transforms: List<ArtifactTransform>,
            singleReport: Boolean,
            timestamp: Long,
        ) {
            calls += PublishCall(transforms, singleReport)
        }
    }

    private object UnusedRepository : GradleEnterpriseRepository {
        private fun unused(): Nothing = error("repository should not be used in orchestration tests")

        override suspend fun getBuildScans(
            filter: Filter,
            buildId: String?,
        ) = unused()

        override suspend fun getBuildScansWithAdvancedQuery(
            filter: Filter,
            buildId: String?,
        ) = unused()

        override suspend fun getBuildScanGradleAttribute(id: String) = unused()

        override suspend fun getBuildScanMavenAttribute(id: String) = unused()

        override suspend fun getBuildScanGradleCachePerformance(id: String) = unused()

        override suspend fun getBuildScanMavenCachePerformance(id: String) = unused()

        override suspend fun getArtifactTransformRequest(id: String) = unused()

        override suspend fun getBuildScanGradlePerformance(id: String) = unused()

        override suspend fun getConfigurationCacheResult(id: String) = unused()

        override suspend fun getBuildProfileOverview(id: String) = unused()
    }
}
