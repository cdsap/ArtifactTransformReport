package io.github.cdsap.artifacttransform.cli

import com.github.ajalt.clikt.core.main
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class MainTest {
    @Test
    fun `cli passes credentials to the injected repository provider`() {
        var receivedCredentials: Pair<String, String>? = null
        val expectedFailure =
            assertThrows(RepositoryRequested::class.java) {
                ArtifactTransformCli { apiKey: String, url: String ->
                    receivedCredentials = apiKey to url
                    throw RepositoryRequested()
                }.main(
                    arrayOf(
                        "--api-key",
                        "test-api-key",
                        "--url",
                        "https://example.test",
                        "--build-scan-id",
                        "build-123",
                    ),
                )
            }

        assertEquals(RepositoryRequested::class, expectedFailure::class)
        assertEquals("test-api-key" to "https://example.test", receivedCredentials)
    }

    private class RepositoryRequested : RuntimeException()
}
