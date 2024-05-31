package io.github.cdsap.artifacttransform

import io.github.cdsap.geapi.client.model.ArtifactTransform
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class ArtifactTransformTests {

    private val sampleTransforms = listOf(
        ArtifactTransform("Execution1", "TransformType1", "Artifact1", "Success", "Outcome1", "200", "100", "50", "300", arrayOf(), "build1"),
        ArtifactTransform("androidx.appcompat:appcompat-resources:1.6.1 [artifactType=android-manifest]", "TransformType2", "Artifact2", "Failure", "Outcome2", "150", "-50", "60", "150", arrayOf(), "build2"),
        ArtifactTransform("Execution3", "TransformType1", "groovy-ant-3.0.17.jar [artifactType=classpath-aar-snapshot]", "Success", "Outcome1", "300", "200", "70", "100", arrayOf(), "build3"),
        ArtifactTransform("Execution4", "TransformType3", "groovy-ant-3.0.17.jar [artifactType=classpath-entry-snapshot]", "Failure", "Outcome3", "100", "-150", "80", "250", arrayOf(), "build4"),
        ArtifactTransform("androidx.appcompat:appcompat-resources:1.6.1 [artifactType=android-aar]", "TransformType2", "Artifact5", "Failure", "Outcome3", "150", "-120", "60", "150", arrayOf(), "build5"),
    )

    @Test
    fun `test sortedByDurationDescending`() {
        val sortedList = sampleTransforms.sortedByDurationDescending()
        assertEquals("Execution3", sortedList[0].artifactTransformExecutionName)
        assertEquals("Execution1", sortedList[1].artifactTransformExecutionName)
    }

    @Test
    fun `test sortedByDurationAndType`() {
        val sortedList = sampleTransforms.sortedByDurationAndType()
        assertEquals("TransformType1", sortedList[0]!!.transformActionType)
        assertEquals("TransformType2", sortedList[1]!!.transformActionType)
    }

    @Test
    fun `test totalByTransformActionType`() {
        val totalList = sampleTransforms.totalByTransformActionType()
        assertEquals("TransformType1", totalList[0].first)
        assertEquals(2, totalList[0].second)
    }

    @Test
    fun `test durationByTransformActionType`() {
        val durationList = sampleTransforms.durationByTransformActionType()
        assertEquals("TransformType1", durationList[0].first)
        assertEquals(500, durationList[0].second)
    }

    @Test
    fun `test fingerprintingByTransformActionType`() {
        val fingerprintingList = sampleTransforms.fingerprintingByTransformActionType()
        assertEquals("TransformType1", fingerprintingList[0].first)
        assertEquals(120, fingerprintingList[0].second)
    }

    @Test
    fun `test totalNegativeAvoidanceSavingsByTransformArtifactType`() {
        val negativeSavingsList = sampleTransforms.totalNegativeAvoidanceSavingsByTransformArtifactType()
        assertEquals("TransformType2", negativeSavingsList[0].first)
        assertEquals(2, negativeSavingsList[0].second)
    }

    @Test
    fun `test durationNegativeAvoidanceSavingsByTransformArtifactType`() {
        val negativeSavingsDurationList = sampleTransforms.durationNegativeAvoidanceSavingsByTransformArtifactType()
        assertEquals("TransformType2", negativeSavingsDurationList[0].first)
        assertEquals(-170, negativeSavingsDurationList[0].second)
    }

    @Test
    fun `test longestNegativeAvoidanceSavings`() {
        val longestNegativeSavingsList = sampleTransforms.longestNegativeAvoidanceSavings()
        assertEquals("TransformType3-groovy-ant-3.0.17.jar [artifactType=classpath-entry-snapshot]", longestNegativeSavingsList[0].first)
        assertEquals((-150).toDuration(DurationUnit.MILLISECONDS), longestNegativeSavingsList[0].second)
    }

    @Test
    fun `test dependencySortedByDuration`() {
        val dependencyDurationList = sampleTransforms.dependencySortedByDuration()
        assertEquals("androidx.appcompat:appcompat-resources:1.6.1", dependencyDurationList[0].first)
        assertEquals(300, dependencyDurationList[0].second)
    }

    @Test
    fun `test dependencyByInputArtifactName`() {
        val dependencyByInputArtifactList = sampleTransforms.dependencyByInputArtifactName()
        assertEquals("groovy-ant-3.0.17.jar", dependencyByInputArtifactList[0].first)
        assertEquals(400, dependencyByInputArtifactList[0].second)
    }

    @Test
    fun `test cacheSizeByTransformActionType`() {
        val cacheSizeList = sampleTransforms.cacheSizeByTransformActionType()
        assertEquals("Execution1", cacheSizeList[0].first)
        assertEquals(300, cacheSizeList[0].second)
    }

    @Test
    fun `test aggregatedCacheSizeByTransformActionType`() {
        val aggregatedCacheSizeList = sampleTransforms.aggregatedCacheSizeByTransformActionType()
        assertEquals("TransformType1", aggregatedCacheSizeList[0].first)
        assertEquals(400, aggregatedCacheSizeList[0].second)
    }

    @Test
    fun `test groupByAvoidanceOutcome`() {
        val groupedByOutcome = sampleTransforms.groupByAvoidanceOutcome()
        assertEquals(2, groupedByOutcome["Outcome1"])
        assertEquals(1, groupedByOutcome["Outcome2"])
    }

    @Test
    fun `test durationByAvoidanceOutcome`() {
        val durationByOutcome = sampleTransforms.durationByAvoidanceOutcome()
        assertEquals(500, durationByOutcome["Outcome1"])
        assertEquals(150, durationByOutcome["Outcome2"])
    }

    @Test
    fun `test fingerprintingByAvoidanceOutcome`() {
        val fingerprintingByOutcome = sampleTransforms.fingerprintingByAvoidanceOutcome()
        assertEquals(120, fingerprintingByOutcome["Outcome1"])
        assertEquals(60, fingerprintingByOutcome["Outcome2"])
    }

    @Test
    fun `test groupByOutcome`() {
        val groupedByOutcome = sampleTransforms.groupByOutcome()
        assertEquals(2, groupedByOutcome["Success"])
        assertEquals(3, groupedByOutcome["Failure"])
    }

    @Test
    fun `test durationByOutcome`() {
        val durationByOutcome = sampleTransforms.durationByOutcome()
        assertEquals(500, durationByOutcome["Success"])
        assertEquals(400, durationByOutcome["Failure"])
    }

    @Test
    fun `test fingerprintingByOutcome`() {
        val fingerprintingByOutcome = sampleTransforms.fingerprintingByOutcome()
        assertEquals(120, fingerprintingByOutcome["Success"])
        assertEquals(200, fingerprintingByOutcome["Failure"])
    }
}
