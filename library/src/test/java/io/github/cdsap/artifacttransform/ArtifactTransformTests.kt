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
    fun `test distribution helpers`() {
        assertEquals(0, emptyList<Int>().percentile(95))
        assertEquals(0, emptyList<Int>().averageInt())
        assertEquals(0, emptyList<Int>().maxOrZero())
        assertEquals(5, listOf(5).percentile(95))
        assertEquals(19, (1..20).toList().percentile(95)) // nearest-rank: ceil(.95*20)=19 -> sorted[18]
        assertEquals(4, listOf(1, 3, 5, 7).averageInt())
        assertEquals(7, listOf(1, 3, 5, 7).maxOrZero())
    }

    @Test
    fun `test extended measures by transform type`() {
        // TransformType1 durations: 200, 300
        assertEquals(250, sampleTransforms.averageDurationByTransformActionType().toMap()["TransformType1"])
        assertEquals(300, sampleTransforms.p95DurationByTransformActionType().toMap()["TransformType1"])
        assertEquals(300, sampleTransforms.maxDurationByTransformActionType().toMap()["TransformType1"])
        // TransformType3 single execution: 100
        assertEquals(100, sampleTransforms.maxDurationByTransformActionType().toMap()["TransformType3"])
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
    fun `test median and p95 by attribute transition`() {
        // jar -> classpath-entry-snapshot durations: 10, 100, 200
        val median = cacheTransforms.medianDurationByAttributeTransition().toMap()
        assertEquals(100, median["artifactType: jar -> classpath-entry-snapshot"])
        val p95 = cacheTransforms.p95DurationByAttributeTransition().toMap()
        assertEquals(200, p95["artifactType: jar -> classpath-entry-snapshot"])
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

    // 3-build fixture for build-level analytics + outlier detection
    private fun b(buildId: String, type: String, avoid: String, duration: String) =
        ArtifactTransform("e", type, "a", "success", avoid, duration, null, "0", "0", arrayOf(), buildId)

    private val buildSample = listOf(
        b("b1", "T1", "avoided_from_local_cache", "50"),
        b("b1", "T1", "executed_cacheable", "50"), // b1 total 100, hit rate 1/2
        b("b2", "T1", "avoided_up_to_date", "110"), // b2 total 110, hit rate 1/1
        b("b3", "T2", "executed_cacheable", "1000"), // b3 total 1000 -> outlier; top type T2
    )

    @Test
    fun `test cacheHitRateByBuildScan`() {
        val rates = buildSample.cacheHitRateByBuildScan().toMap()
        assertEquals(0.5, rates["b1"])
        assertEquals(1.0, rates["b2"])
        assertEquals(0.0, rates["b3"])
    }

    @Test
    fun `test slowest and top type by build scan`() {
        assertEquals("b3", buildSample.slowestTransformByBuildScan()[0].first)
        assertEquals("T2", buildSample.topTransformTypeByBuildScan().toMap()["b3"])
        assertEquals("T1", buildSample.topTransformTypeByBuildScan().toMap()["b1"])
    }

    @Test
    fun `test topNWithOther`() {
        val data = listOf("a" to 50, "b" to 40, "c" to 30, "d" to 20, "e" to 10)
        val bucketed = data.topNWithOther(2)
        assertEquals("a" to 50, bucketed[0])
        assertEquals("b" to 40, bucketed[1])
        assertEquals("Other" to 60, bucketed[2]) // 30 + 20 + 10
        // n >= size returns unchanged
        assertEquals(data, data.topNWithOther(5))
    }

    @Test
    fun `test totalDuration and totalAvoidanceSavings`() {
        // sampleTransforms durations: 200+150+300+100+150 = 900
        assertEquals(900, sampleTransforms.totalDuration())
        // avoidanceSavings: 100, -50, 200, -150, -120 -> positive sum 300
        assertEquals(300, sampleTransforms.totalAvoidanceSavings())
    }

    @Test
    fun `test outlierBuildScans`() {
        // median total across [100, 110, 1000] is 110; threshold 220; only b3 exceeds it
        assertEquals(listOf("b3"), buildSample.outlierBuildScans())
        // fewer than 3 builds -> no outliers
        assertEquals(emptyList<String>(), cacheTransforms.outlierBuildScans())
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
        assertEquals(15, aarToExploded.medianDuration) // median of [10, 20]
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

    private fun transform(action: String, input: String, duration: String) =
        ArtifactTransform("e", action, input, "success", "executed_cacheable", duration, null, "0", "0", arrayOf(), "b")

    private val providerSample = listOf(
        transform("com.android.build.gradle.internal.dependency.AarTransform", "foo-1.0.aar", "100"),
        transform("com.android.build.gradle.internal.dependency.AarToClassTransform", "bar-1.0.aar", "50"),
        transform("org.jetbrains.kotlin.gradle.internal.transforms.ClasspathEntrySnapshotTransform", "kotlin-stdlib-2.0.jar", "30"),
        transform("org.jetbrains.kotlin.gradle.internal.kapt.incremental.KaptTransform", "classes.jar", "5"),
        transform("dagger.hilt.android.plugin.transform.AggregatedPackagesTransform", "classes.jar", "20"),
    )

    @Test
    fun `test provider classification`() {
        assertEquals("Android/AGP", providerSample[0].provider())
        assertEquals("Kotlin", providerSample[2].provider())
        assertEquals("Kotlin kapt", providerSample[3].provider())
        assertEquals("Hilt/Dagger", providerSample[4].provider())
    }

    @Test
    fun `test durationByProvider`() {
        val byProvider = providerSample.durationByProvider().toMap()
        assertEquals(150, byProvider["Android/AGP"]) // 100 + 50
        assertEquals(30, byProvider["Kotlin"])
        assertEquals(5, byProvider["Kotlin kapt"])
        assertEquals(20, byProvider["Hilt/Dagger"])
    }

    @Test
    fun `test parsedInputArtifact`() {
        assertEquals(ParsedArtifact("groovy-ant", "4.0.29"), transform("X", "groovy-ant-4.0.29.jar", "1").parsedInputArtifact())
        assertEquals(ParsedArtifact("appcompat-resources", "1.7.1-runtime"), transform("X", "appcompat-resources-1.7.1-runtime.jar", "1").parsedInputArtifact())
        // project/module outputs have no version -> first-party
        val firstParty = transform("X", "classes.jar", "1").parsedInputArtifact()
        assertEquals(ParsedArtifact("classes.jar", null), firstParty)
        assertEquals(false, firstParty.isExternalDependency)
    }

    // execName prefix carries the source: project module, GAV, or bare file
    private fun sourced(execName: String, duration: String) =
        ArtifactTransform(execName, "T", "classes.jar", "success", "executed_cacheable", duration, null, "0", "0", arrayOf(), "b")

    private val sourceSample = listOf(
        sourced("project :core:cart [artifactType=android-classes]", "100"),
        sourced("project :core:cart [artifactType=android-dex]", "50"),
        sourced("project :feature:home [artifactType=android-classes]", "30"),
        sourced("androidx.appcompat:appcompat:1.7.0 [artifactType=jar]", "200"),
        sourced("classes.jar [artifactType=android-classes]", "5"),
    )

    @Test
    fun `test source classification`() {
        assertEquals(":core:cart", sourceSample[0].sourceModule())
        assertEquals("First-party module", sourceSample[0].sourceCategory())
        assertEquals(null, sourceSample[3].sourceModule())
        assertEquals("External dependency", sourceSample[3].sourceCategory())
        assertEquals("Unattributed file", sourceSample[4].sourceCategory())
    }

    @Test
    fun `test SDK runtime and unknown source categories`() {
        assertEquals("SDK/runtime artifact", sourced("android.jar [artifactType=android-classes]", "1").sourceCategory())
        assertEquals("SDK/runtime artifact", sourced("gradle-api-9.5.1.jar [artifactType=jar]", "1").sourceCategory())
        assertEquals("Unattributed file", sourced("classes.jar [artifactType=android-classes]", "1").sourceCategory())
        assertEquals("Unknown", sourced("", "1").sourceCategory()) // no source prefix at all
    }

    @Test
    fun `test cacheSizeBySourceCategory`() {
        val byCategory = depSample.cacheSizeBySourceCategory().toMap()
        // depSample dependencies have cache 500+500+200+0 = 1200 under External dependency
        assertEquals(1200, byCategory["External dependency"])
    }

    @Test
    fun `test durationBySourceCategory`() {
        val byCategory = sourceSample.durationBySourceCategory().toMap()
        assertEquals(180, byCategory["First-party module"]) // 100 + 50 + 30
        assertEquals(200, byCategory["External dependency"])
        assertEquals(5, byCategory["Unattributed file"])
    }

    @Test
    fun `test durationByModule`() {
        val byModule = sourceSample.durationByModule()
        assertEquals(":core:cart" to 150, byModule[0]) // 100 + 50
        assertEquals(":feature:home" to 30, byModule[1])
    }

    // GAV-coordinate sources for dependency aggregation + family fragmentation
    private fun dep(coordinate: String, duration: String, cacheSize: String = "0") =
        ArtifactTransform("$coordinate [artifactType=jar]", "T", "a.jar", "success", "executed_cacheable", duration, null, "0", cacheSize, arrayOf(), "b")

    private val depSample = listOf(
        dep("androidx.appcompat:appcompat:1.7.0", "100", "500"),
        dep("androidx.appcompat:appcompat:1.7.0", "60", "500"),
        dep("androidx.appcompat:appcompat:1.6.0", "30", "200"),
        dep("com.squareup.okio:okio:3.9.0", "40"),
        dep("project :app", "10"), // not a GAV (one colon segment after 'project ') -> excluded
    )

    @Test
    fun `test dependencyCoordinate`() {
        assertEquals("androidx.appcompat:appcompat:1.7.0", depSample[0].dependencyCoordinate())
        assertEquals(null, depSample[4].dependencyCoordinate()) // project module -> not a dependency
    }

    @Test
    fun `test per-dependency aggregations`() {
        assertEquals(160, depSample.durationByDependency().toMap()["androidx.appcompat:appcompat:1.7.0"]) // 100 + 60
        assertEquals(2, depSample.countByDependency().toMap()["androidx.appcompat:appcompat:1.7.0"])
        assertEquals(80, depSample.medianDurationByDependency().toMap()["androidx.appcompat:appcompat:1.7.0"]) // median of [100,60]
        assertEquals(1000, depSample.cacheSizeByDependency().toMap()["androidx.appcompat:appcompat:1.7.0"]) // 500 + 500
    }

    @Test
    fun `test dependencyFamilies`() {
        val families = depSample.dependencyFamilies().associateBy { it.family }
        val appcompat = families["androidx.appcompat:appcompat"]!!
        assertEquals(listOf("1.6.0", "1.7.0"), appcompat.versions)
        assertEquals(3, appcompat.count) // two 1.7.0 + one 1.6.0
        assertEquals(190, appcompat.totalDuration) // 100 + 60 + 30
        assertEquals(1200, appcompat.cacheSize) // 500 + 500 + 200
        assertEquals("1.7.0", appcompat.mostExpensiveVersion) // 160 > 30
        // single-version family present but not fragmented
        assertEquals(listOf("3.9.0"), families["com.squareup.okio:okio"]!!.versions)
    }

    @Test
    fun `test librariesWithMultipleVersions normalizes variants`() {
        val sample = listOf(
            transform("X", "transition-1.5.0.jar", "1"),
            transform("X", "transition-1.5.0-api.jar", "1"),
            transform("X", "transition-1.6.0.jar", "1"),
            transform("X", "appcompat-1.7.1.jar", "1"),
            transform("X", "appcompat-1.7.1-runtime.jar", "1"),
            transform("X", "classes.jar", "1"),
        )
        val drift = sample.librariesWithMultipleVersions().toMap()
        // transition has genuine drift 1.5.0 vs 1.6.0
        assertEquals(listOf("1.5.0", "1.6.0"), drift["transition"])
        // appcompat only differs by -runtime variant -> normalized to one version -> not reported
        assertEquals(null, drift["appcompat"])
    }
}
