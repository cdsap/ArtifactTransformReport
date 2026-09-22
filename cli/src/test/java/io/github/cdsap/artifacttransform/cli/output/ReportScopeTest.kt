package io.github.cdsap.artifacttransform.cli.output

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ReportScopeTest {
    @Test
    fun `aggregate scope uses empty filename prefix`() {
        assertEquals("", ReportScope.Aggregate.fileNamePrefix)
    }

    @Test
    fun `single-build scope uses single filename prefix`() {
        assertEquals("single-", ReportScope.SingleBuild.fileNamePrefix)
    }
}
