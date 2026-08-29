package io.github.cdsap.artifacttransform.cli

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.File

class BuildWorkflowProcessWatcherTest {
    @Test
    fun gradleBuildJobsUseBuildProcessWatcher() {
        val workflow = readBuildWorkflow()
        val watcherUses =
            Regex("""uses:\s*cdsap/build-process-watcher@v[\d.]+""")
                .findAll(workflow)
                .toList()

        assertEquals(
            2,
            watcherUses.size,
            "Expected build-process-watcher in assemble and fatbinary",
        )
        assertTrue(
            workflow.contains("uses: cdsap/build-process-watcher@v0.6.2"),
            "Expected build-process-watcher action in GHA gradle build workflow",
        )
        assertTrue(
            workflow.contains("remote_monitoring: 'true'"),
            "Expected remote_monitoring enabled for build-process-watcher",
        )
        assertTrue(
            workflow.contains("export_to_bigquery: 'true'"),
            "Expected BigQuery export enabled for build-process-watcher",
        )

        val watcherIndex = workflow.indexOf("uses: cdsap/build-process-watcher@v0.6.2")
        val gradleTestIndex = workflow.indexOf("./gradlew test")
        val gradleFatBinaryIndex = workflow.indexOf("./gradlew :cli:fatBinary")
        assertTrue(watcherIndex >= 0, "build-process-watcher step missing")
        assertTrue(gradleTestIndex >= 0, "gradle test step missing")
        assertTrue(gradleFatBinaryIndex >= 0, "gradle fatBinary step missing")
        assertTrue(
            watcherIndex < gradleTestIndex,
            "build-process-watcher must run before ./gradlew test",
        )
        assertTrue(
            workflow.lastIndexOf("uses: cdsap/build-process-watcher@v0.6.2") < gradleFatBinaryIndex,
            "build-process-watcher must run before ./gradlew :cli:fatBinary",
        )
    }

    private fun readBuildWorkflow(): String {
        var dir = File(System.getProperty("user.dir")).absoluteFile
        repeat(6) {
            val candidate = File(dir, ".github/workflows/build.yml")
            if (candidate.isFile) {
                return candidate.readText()
            }
            dir = dir.parentFile ?: return@repeat
        }
        error("Could not locate .github/workflows/build.yml from ${System.getProperty("user.dir")}")
    }
}
