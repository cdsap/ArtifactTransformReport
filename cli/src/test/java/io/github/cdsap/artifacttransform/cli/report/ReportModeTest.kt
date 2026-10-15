package io.github.cdsap.artifacttransform.cli.report

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ReportModeTest {
    @Test
    fun `multi-build mode uses an empty filename prefix`() {
        assertEquals("", ReportMode.MultiBuild.fileNamePrefix)
    }

    @Test
    fun `single-build mode uses the single filename prefix`() {
        assertEquals("single-", ReportMode.SingleBuild.fileNamePrefix)
    }
}
