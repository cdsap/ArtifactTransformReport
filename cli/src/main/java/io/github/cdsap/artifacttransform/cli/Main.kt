package io.github.cdsap.artifacttransform.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.check
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.multiple
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.int
import io.github.cdsap.artifacttransform.cli.report.ArtifactTransformReport
import io.github.cdsap.artifacttransform.cli.report.SingleArtifactTransformReport
import io.github.cdsap.geapi.client.model.ClientType
import io.github.cdsap.geapi.client.model.Filter
import io.github.cdsap.geapi.client.network.GEClient
import io.github.cdsap.geapi.client.repository.impl.GradleRepositoryImpl
import kotlinx.coroutines.runBlocking

fun main(args: Array<String>) {
    ArtifactTransformCli().main(args)
}

class ArtifactTransformCli : CliktCommand() {
    private val apiKey: String by option().required()
    private val url by option().required()
    private val maxBuilds by option().int().default(100).check("max builds to process 50000") { it <= 50000 }
    private val project: String? by option()
    private val tags: List<String> by option().multiple(default = emptyList())
    private val requestedTask by option()
    private val includeFailedBuilds by option().flag(default = true)
    private val user: String? by option()
    private val sinceBuildId: String? by option()
    private val buildScanId: String? by option()
    private val exclusiveTags by option().flag(default = true)

    override fun run() {
        if (buildScanId == null) {
            if (project == null) {
                throw IllegalArgumentException("tags and project can't be empty")
            }
        }

        val filter = Filter(
            maxBuilds = maxBuilds,
            project = project,
            tags = tags.map { it.replaceFirst("not:", "!") },
            user = user,
            includeFailedBuilds = includeFailedBuilds,
            requestedTask = requestedTask,
            sinceBuildId = sinceBuildId,
            exclusiveTags = exclusiveTags,
            clientType = ClientType.CLI
        )

        val repository = GradleRepositoryImpl(
            GEClient(apiKey, url)
        )
        runBlocking {
            if (buildScanId != null) {
                SingleArtifactTransformReport(buildScanId!!, repository).process()
            } else {
                ArtifactTransformReport(filter, repository).process()
            }
        }
    }
}
