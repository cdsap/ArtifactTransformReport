package io.github.cdsap.artifacttransform

import io.github.cdsap.geapi.client.model.ArtifactTransform
import io.github.cdsap.geapi.client.model.ChangedAttributes
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class InsightsTests {

    private fun t(
        execName: String,
        type: String,
        avoid: String,
        duration: String,
        buildScanId: String = "b",
        changed: Array<ChangedAttributes> = arrayOf()
    ) = ArtifactTransform(execName, type, "a.jar", "success", avoid, duration, null, "0", "0", changed, buildScanId)

    @Test
    fun `empty input yields no findings`() {
        assertEquals(emptyList<Finding>(), emptyList<ArtifactTransform>().findings())
    }

    @Test
    fun `frequent-cheap type vs slow type are classified distinctly`() {
        val sample = List(10) { t("a:b:1 [x]", "TypeFreq", "avoided_from_local_cache", "10") } +
            t("c:d:1 [x]", "TypeSlow", "executed_not_cacheable", "200")
        val findings = sample.findings()

        val freq = findings.filter { it.kind == FindingKind.EXPENSIVE_BECAUSE_FREQUENT }
        assertTrue(freq.any { it.subject == "TypeFreq" }, "TypeFreq should be flagged as frequent")
        assertTrue(freq.none { it.subject == "TypeSlow" }, "TypeSlow should not be flagged as frequent")

        val perExec = findings.filter { it.kind == FindingKind.EXPENSIVE_PER_EXECUTION }
        assertTrue(perExec.any { it.subject == "TypeSlow" }, "TypeSlow should be flagged as expensive per execution")
    }

    @Test
    fun `low hit rate on a high-cost type yields a cache opportunity`() {
        val sample = List(5) { t("a:b:1 [x]", "TypeMiss", "executed_cacheable", "100") }
        val findings = sample.findings()
        assertTrue(findings.any { it.kind == FindingKind.CACHE_OPPORTUNITY && it.subject == "TypeMiss" })
    }

    @Test
    fun `high hit rate yields no cache opportunity`() {
        val sample = List(5) { t("a:b:1 [x]", "TypeHit", "avoided_from_local_cache", "100") }
        assertTrue(sample.findings().none { it.kind == FindingKind.CACHE_OPPORTUNITY })
    }

    @Test
    fun `multi-version family yields a fragmentation finding`() {
        val sample = listOf(
            t("androidx.appcompat:appcompat:1.7.0 [x]", "T", "executed_cacheable", "10"),
            t("androidx.appcompat:appcompat:1.6.0 [x]", "T", "executed_cacheable", "10"),
        )
        val fragmentation = sample.findings().filter { it.kind == FindingKind.VERSION_FRAGMENTATION }
        assertEquals(1, fragmentation.size) // aggregated into a single finding
        assertTrue(fragmentation[0].message.contains("androidx.appcompat:appcompat"))
    }

    @Test
    fun `outlier build is flagged only in multi-build reports`() {
        val multi = listOf(
            t("a:b:1 [x]", "T", "executed_cacheable", "100", "b1"),
            t("a:b:1 [x]", "T", "executed_cacheable", "110", "b2"),
            t("a:b:1 [x]", "T", "executed_cacheable", "1000", "b3"),
        )
        val outliers = multi.findings().filter { it.kind == FindingKind.OUTLIER_BUILD }
        assertEquals(listOf("b3"), outliers.map { it.subject })

        // single build -> never an outlier finding
        val single = List(3) { t("a:b:1 [x]", "T", "executed_cacheable", "100", "only") }
        assertTrue(single.findings().none { it.kind == FindingKind.OUTLIER_BUILD })
    }
}
