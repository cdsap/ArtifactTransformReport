package io.github.cdsap.artifacttransform

import io.github.cdsap.geapi.client.model.ArtifactTransform
import io.github.cdsap.geapi.client.model.ChangedAttributes
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
    fun `test medianDurationByTransformActionType`() {
        // TransformType1: durations 200, 300 -> median 250; TransformType2: 150, 150 -> 150; TransformType3: 100
        val median = sampleTransforms.medianDurationByTransformActionType().toMap()
        assertEquals(250, median["TransformType1"])
        assertEquals(150, median["TransformType2"])
        assertEquals(100, median["TransformType3"])
    }

    @Test
    fun `test median helper`() {
        assertEquals(0, emptyList<Int>().median())
        assertEquals(3, listOf(3).median())
        assertEquals(3, listOf(1, 3, 5).median())
        assertEquals(4, listOf(1, 3, 5, 7).median())
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

    private val jarToSnapshot = arrayOf(ChangedAttributes("artifactType", "jar", "classpath-entry-snapshot"))

    private val cacheTransforms = listOf(
        ArtifactTransform("e1", "TypeA", "a.jar", "from_cache", "avoided_from_local_cache", "10", null, "1", "100", jarToSnapshot, "build1"),
        ArtifactTransform("e2", "TypeA", "b.jar", "success", "executed_cacheable", "100", null, "2", "200", jarToSnapshot, "build1"),
        ArtifactTransform("e3", "TypeA", "c.jar", "success", "executed_cacheable", "200", null, "3", "300", jarToSnapshot, "build2"),
        ArtifactTransform("e4", "TypeB", "d.jar", "success", "executed_not_cacheable", "50", null, "4", "400", arrayOf(), "build2"),
        ArtifactTransform("e5", "TypeB", "e.jar", "up_to_date", "avoided_up_to_date", "5", null, "5", "500", arrayOf(), "build2"),
    )

    @Test
    fun `test overallCacheHitRate`() {
        // avoided: e1, e5 = 2; executed: e2, e3, e4 = 3 -> 2 / 5
        assertEquals(0.4, cacheTransforms.overallCacheHitRate())
    }

    @Test
    fun `test totalAvoidableMissDuration`() {
        // executed_cacheable: e2 (100) + e3 (200)
        assertEquals(300, cacheTransforms.totalAvoidableMissDuration())
    }

    @Test
    fun `test cacheEffectivenessByTransformActionType`() {
        val effectiveness = cacheTransforms.cacheEffectivenessByTransformActionType()
        // sorted by avoidableMissDuration desc -> TypeA first
        val typeA = effectiveness[0]
        assertEquals("TypeA", typeA.transformActionType)
        assertEquals(3, typeA.total)
        assertEquals(1, typeA.avoided)
        assertEquals(2, typeA.executed)
        assertEquals(2, typeA.avoidableMisses)
        assertEquals(300, typeA.avoidableMissDuration)
        assertEquals(1.0 / 3, typeA.hitRate)

        val typeB = effectiveness[1]
        assertEquals("TypeB", typeB.transformActionType)
        assertEquals(0, typeB.avoidableMisses)
        assertEquals(0, typeB.avoidableMissDuration)
        assertEquals(0.5, typeB.hitRate)
    }

    @Test
    fun `test durationByAttributeTransition`() {
        val byTransition = cacheTransforms.durationByAttributeTransition()
        // jar -> classpath-entry-snapshot: e1 (10) + e2 (100) + e3 (200) = 310
        assertEquals("artifactType: jar -> classpath-entry-snapshot", byTransition[0].first)
        assertEquals(310, byTransition[0].second)
        // empty changed attributes grouped as n/a: e4 (50) + e5 (5) = 55
        assertEquals("n/a", byTransition[1].first)
        assertEquals(55, byTransition[1].second)
    }

    @Test
    fun `test countByAttributeTransition`() {
        val counts = cacheTransforms.countByAttributeTransition().toMap()
        assertEquals(3, counts["artifactType: jar -> classpath-entry-snapshot"])
        assertEquals(2, counts["n/a"])
    }

    @Test
    fun `test durationByBuildScan`() {
        val byBuild = cacheTransforms.durationByBuildScan()
        // build2: e3 (200) + e4 (50) + e5 (5) = 255; build1: e1 (10) + e2 (100) = 110
        assertEquals("build2", byBuild[0].first)
        assertEquals(255, byBuild[0].second)
        assertEquals("build1", byBuild[1].first)
        assertEquals(110, byBuild[1].second)
    }

    @Test
    fun `test countByBuildScan`() {
        val counts = cacheTransforms.countByBuildScan().toMap()
        assertEquals(3, counts["build2"])
        assertEquals(2, counts["build1"])
    }

    // aar -> exploded -> jar -> classes ; jar -> snapshot
    private fun edge(from: String, to: String, duration: String) =
        ArtifactTransform("e", "T", "a", "success", "executed_cacheable", duration, null, "0", "0", arrayOf(ChangedAttributes("artifactType", from, to)), "b")

    private val pipeline = listOf(
        edge("aar", "exploded", "10"),
        edge("aar", "exploded", "20"),
        edge("exploded", "jar", "30"),
        edge("jar", "classes", "40"),
        edge("jar", "snapshot", "5"),
    )

    @Test
    fun `test attributeTransitionEdges aggregates count and duration`() {
        val edges = pipeline.attributeTransitionEdges()
        val aarToExploded = edges.first { it.from == "aar" && it.to == "exploded" }
        assertEquals(2, aarToExploded.count)
        assertEquals(30, aarToExploded.totalDuration)
        // sorted by total duration descending: jar->classes (40) is first
        assertEquals("jar", edges[0].from)
        assertEquals("classes", edges[0].to)
    }

    @Test
    fun `test topologicalArtifactOrder`() {
        val order = pipeline.attributeTransitionEdges().topologicalArtifactOrder()
        // sources before sinks
        assert(order.indexOf("aar") < order.indexOf("exploded"))
        assert(order.indexOf("exploded") < order.indexOf("jar"))
        assert(order.indexOf("jar") < order.indexOf("classes"))
        assert(order.indexOf("jar") < order.indexOf("snapshot"))
    }

    @Test
    fun `test artifactLevels`() {
        val levels = pipeline.attributeTransitionEdges().artifactLevels()
        assertEquals(0, levels["aar"])
        assertEquals(1, levels["exploded"])
        assertEquals(2, levels["jar"])
        assertEquals(3, levels["classes"])
        assertEquals(3, levels["snapshot"])
    }
}
