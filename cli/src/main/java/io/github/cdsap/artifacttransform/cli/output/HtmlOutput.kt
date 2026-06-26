package io.github.cdsap.artifacttransform.cli.output

import io.github.cdsap.artifacttransform.aggregatedCacheSizeByTransformActionType
import io.github.cdsap.artifacttransform.artifactLevels
import io.github.cdsap.artifacttransform.attributeTransitionEdges
import io.github.cdsap.artifacttransform.cacheEffectivenessByTransformActionType
import io.github.cdsap.artifacttransform.cacheHitRateByBuildScan
import io.github.cdsap.artifacttransform.cacheSizeByTransformActionType
import io.github.cdsap.artifacttransform.countByAttributeTransition
import io.github.cdsap.artifacttransform.countByBuildScan
import io.github.cdsap.artifacttransform.dependencyByInputArtifactName
import io.github.cdsap.artifacttransform.dependencyFamiliesFragmentedWithinBuild
import io.github.cdsap.artifacttransform.dependencySortedByDuration
import io.github.cdsap.artifacttransform.durationByAttributeTransition
import io.github.cdsap.artifacttransform.durationByAvoidanceOutcome
import io.github.cdsap.artifacttransform.durationByBuildScan
import io.github.cdsap.artifacttransform.durationByOutcome
import io.github.cdsap.artifacttransform.durationByProvider
import io.github.cdsap.artifacttransform.durationBySourceCategory
import io.github.cdsap.artifacttransform.durationByTransformActionType
import io.github.cdsap.artifacttransform.extractName
import io.github.cdsap.artifacttransform.fingerprintingByAvoidanceOutcome
import io.github.cdsap.artifacttransform.fingerprintingByOutcome
import io.github.cdsap.artifacttransform.fingerprintingByTransformActionType
import io.github.cdsap.artifacttransform.groupByAvoidanceOutcome
import io.github.cdsap.artifacttransform.groupByOutcome
import io.github.cdsap.artifacttransform.outlierBuildScans
import io.github.cdsap.artifacttransform.overallCacheHitRate
import io.github.cdsap.artifacttransform.slowestTransformByBuildScan
import io.github.cdsap.artifacttransform.sortedByDurationAndType
import io.github.cdsap.artifacttransform.sortedByDurationDescending
import io.github.cdsap.artifacttransform.topTransformTypeByBuildScan
import io.github.cdsap.artifacttransform.topologicalArtifactOrder
import io.github.cdsap.artifacttransform.totalAvoidanceSavings
import io.github.cdsap.artifacttransform.totalByTransformActionType
import io.github.cdsap.artifacttransform.totalDuration
import io.github.cdsap.geapi.client.model.ArtifactTransform
import java.io.File

/**
 * Writes a self-contained, static HTML report. The body mirrors the command-line tables (the same
 * sections produced by the console views) and keeps the inferred artifact transform pipeline as an
 * inline SVG diagram. No JavaScript or external resources, so it renders offline.
 */
class HtmlOutput(
    private val transforms: List<ArtifactTransform>,
    private val single: Boolean,
    private val timestamp: Long
) {

    fun writeHtml() {
        val prefixFile = if (single) "single-" else ""
        val html = "${prefixFile}artifact-transforms-$timestamp.html"
        val startTimestamp = System.currentTimeMillis()
        File(html).writeText(render())
        val endTime = System.currentTimeMillis()
        println("File $html created in ${endTime - startTimestamp} ms")
    }

    private fun render(): String {
        val buildScans = transforms.groupBy { it.buildScanId }.count()
        val hitRate = (transforms.overallCacheHitRate() * 100).roundTo(2)
        val savings = transforms.totalAvoidanceSavings()
        val savingsStat = if (savings > 0) {
            """<div class="stat"><div class="v">${formatMsValue(savings)}</div><div class="l">Avoidance savings</div></div>"""
        } else {
            ""
        }

        return """
            <!DOCTYPE html>
            <html lang="en">
            <head>
            <meta charset="utf-8">
            <meta name="viewport" content="width=device-width, initial-scale=1">
            <title>Artifact Transform Report</title>
            <style>
              :root { --bg:#0f1117; --card:#1a1d27; --border:#2a2e3a; --text:#e5e9f0; --muted:#9aa3b2; --accent:#6aa6ff; }
              * { box-sizing: border-box; }
              body { margin:0; background:var(--bg); color:var(--text);
                font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif; }
              header { padding:24px 32px; border-bottom:1px solid var(--border); }
              h1 { margin:0; font-size:20px; font-weight:600; }
              .sub { color:var(--muted); font-size:13px; margin-top:4px; }
              .stats { display:flex; flex-wrap:wrap; gap:16px; padding:24px 32px; }
              .stat { background:var(--card); border:1px solid var(--border); border-radius:10px;
                padding:16px 20px; min-width:160px; }
              .stat .v { font-size:24px; font-weight:700; }
              .stat .l { color:var(--muted); font-size:12px; margin-top:4px; text-transform:uppercase; letter-spacing:.04em; }
              .card { background:var(--card); border:1px solid var(--border); border-radius:12px; padding:20px;
                margin:0 32px 20px; }
              .card h2 { margin:0 0 8px; font-size:14px; font-weight:600; overflow-wrap:anywhere; }
              .card .scroll { overflow:auto; }
              .card svg { display:block; }
              .card .hint { color:var(--muted); font-size:12px; margin:0 0 12px; }
              table.frag { border-collapse:collapse; width:100%; font-size:13px; }
              table.frag th, table.frag td { text-align:left; padding:7px 12px; border-bottom:1px solid var(--border);
                white-space:nowrap; }
              table.frag th { color:var(--muted); font-weight:600; text-transform:uppercase; font-size:11px; letter-spacing:.04em; }
              table.frag tbody tr:hover { background:rgba(255,255,255,0.03); }
              footer { color:var(--muted); font-size:12px; padding:0 32px 32px; }
            </style>
            </head>
            <body>
            <header>
              <h1>Artifact Transform Report</h1>
              <div class="sub">${if (single) "Single build scan" else "Aggregated builds"} &middot; generated by artifacttransform</div>
            </header>
            <section class="stats">
              <div class="stat"><div class="v">${transforms.size}</div><div class="l">Total transforms</div></div>
              <div class="stat"><div class="v">$buildScans</div><div class="l">Build scans</div></div>
              <div class="stat"><div class="v">${formatMsValue(transforms.totalDuration())}</div><div class="l">Total duration</div></div>
              <div class="stat"><div class="v">$hitRate%</div><div class="l">Cache hit rate</div></div>
              <div class="stat"><div class="v">${formatBytes(totalCacheSizeBytes())}</div><div class="l">Total cache size</div></div>
              $savingsStat
            </section>
            ${pipelineSection()}
            ${outcomeTablesSection()}
            ${transformsByTypeSection()}
            ${cacheEffectivenessSection()}
            ${dependencySection()}
            ${cacheSizeSection()}
            ${slowestSection()}
            ${attributeTransitionSection()}
            ${attributionSection()}
            ${negativeAvoidanceSection()}
            ${buildLevelSection()}
            ${versionFragmentationSection()}
            <footer>Generated by artifacttransform. Hover a pipeline node or edge for its full label.</footer>
            </body>
            </html>
        """.trimIndent()
    }

    // --- generic table helpers ---------------------------------------------------------------

    /** A card wrapping a table; returns "" when there are no rows so empty sections are hidden. */
    private fun card(title: String, hint: String, headers: List<String>, rows: String): String {
        if (rows.isBlank()) return ""
        val head = headers.joinToString("") { "<th>${it.escapeXml()}</th>" }
        val hintHtml = if (hint.isNotBlank()) """<p class="hint">${hint.escapeXml()}</p>""" else ""
        return """
            <section class="card">
              <h2>${title.escapeXml()}</h2>
              $hintHtml
              <div class="scroll"><table class="frag"><thead><tr>$head</tr></thead><tbody>$rows</tbody></table></div>
            </section>
        """.trimIndent()
    }

    /**
     * Build rows from N independent label/value columns laid out side by side — the two/three-pane
     * layout the console tables use (each column is its own top-N ranking, row i unrelated across
     * columns). Labels are escaped; values are assumed already formatted and safe.
     */
    private fun panes(vararg cols: List<Pair<String, String>>): String {
        val n = cols.maxOfOrNull { it.size } ?: 0
        return (0 until n).joinToString("") { i ->
            "<tr>" + cols.joinToString("") { col ->
                val e = col.getOrNull(i)
                "<td>${e?.first?.escapeXml() ?: ""}</td><td>${e?.second ?: ""}</td>"
            } + "</tr>"
        }
    }

    // --- command-line-equivalent sections ----------------------------------------------------

    private fun transformsByTypeSection(): String {
        val count = transforms.totalByTransformActionType().take(10).map { it.first.extractName() to it.second.toString() }
        val duration = transforms.durationByTransformActionType().take(10).map { it.first.extractName() to formatMsValue(it.second) }
        val finger = transforms.fingerprintingByTransformActionType().take(10).map { it.first.extractName() to formatMsValue(it.second) }
        return card(
            "Artifact transforms by type",
            "Top transform types by count, total duration and total fingerprinting time (each column ranked independently).",
            listOf("Transform", "Count", "Transform", "Duration", "Transform", "Fingerprinting"),
            panes(count, duration, finger)
        )
    }

    private fun cacheEffectivenessSection(): String {
        val effectiveness = transforms.cacheEffectivenessByTransformActionType().take(10)
        val rows = effectiveness.joinToString("") {
            """<tr><td>${it.transformActionType.extractName().escapeXml()}</td>""" +
                """<td>${(it.hitRate * 100).roundTo(2)}%</td><td>${it.avoidableMisses}</td>""" +
                """<td>${formatMsValue(it.avoidableMissDuration)}</td></tr>"""
        }
        return card(
            "Cache effectiveness (overall hit rate: ${(transforms.overallCacheHitRate() * 100).roundTo(2)}%)",
            "Per type: cache hit rate, avoidable misses (cacheable transforms that ran), and time lost to them.",
            listOf("Transform", "Hit rate", "Avoidable misses", "Avoidable miss duration"),
            rows
        )
    }

    private fun dependencySection(): String {
        val byDependency = transforms.dependencySortedByDuration().take(10).map { it.first to formatMsValue(it.second) }
        val byInput = transforms.dependencyByInputArtifactName().take(10).map { it.first to formatMsValue(it.second) }
        return card(
            "Duration by artifact transform dependency",
            "Aggregated transform duration grouped by dependency and by input artifact.",
            listOf("Dependency", "Duration", "Input artifact", "Duration"),
            panes(byDependency, byInput)
        )
    }

    private fun cacheSizeSection(): String {
        val heaviest = transforms.cacheSizeByTransformActionType().take(10).map { it.first to formatBytes(it.second.toLong()) }
        val aggregated = transforms.aggregatedCacheSizeByTransformActionType().take(10)
            .map { it.first.extractName() to formatBytes(it.second.toLong()) }
        return card(
            "Cache size",
            "Heaviest individual cached transform outputs, and aggregated cache size by transform type.",
            listOf("Heaviest cached output", "Size", "Transform type", "Aggregated size"),
            panes(heaviest, aggregated)
        )
    }

    private fun slowestSection(): String {
        fun label(t: ArtifactTransform) =
            "${t.transformActionType.extractName()} ${t.artifactTransformExecutionName.substringBefore(" [")}"
        val slowest = transforms.sortedByDurationDescending().take(10)
            .map { label(it) to formatMsValue(it.duration.toIntOrNull() ?: 0) }
        val slowestByType = transforms.sortedByDurationAndType().filterNotNull().take(10)
            .map { label(it) to formatMsValue(it.duration.toIntOrNull() ?: 0) }
        return card(
            "Slowest artifact transforms",
            "The slowest individual transforms, overall and one-per-type.",
            listOf("Slowest transform", "Duration", "Slowest by type", "Duration"),
            panes(slowest, slowestByType)
        )
    }

    private fun attributeTransitionSection(): String {
        val byDuration = transforms.durationByAttributeTransition().take(10).map { it.first to formatMsValue(it.second) }
        val byCount = transforms.countByAttributeTransition().take(10).map { it.first to it.second.toString() }
        return card(
            "Artifact transforms by changed attributes",
            "Changed-attribute transitions (from -> to), aggregated by duration and by count.",
            listOf("Transition", "Duration", "Transition", "Count"),
            panes(byDuration, byCount)
        )
    }

    private fun attributionSection(): String {
        val byProvider = transforms.durationByProvider().map { it.first to formatMsValue(it.second) }
        val bySource = transforms.durationBySourceCategory().map { it.first to formatMsValue(it.second) }
        return card(
            "Transform duration by tool / plugin and source",
            "Total transform duration attributed to the contributing tool/plugin and to the input's source category.",
            listOf("Tool / plugin", "Duration", "Source category", "Duration"),
            panes(byProvider, bySource)
        )
    }

    private fun versionFragmentationSection(): String {
        val fragmented = transforms.dependencyFamiliesFragmentedWithinBuild()
        if (fragmented.isEmpty()) return ""
        val rows = fragmented.joinToString("") { family ->
            """<tr><td>${family.family.escapeXml()}</td><td>${family.versions.size}</td>""" +
                """<td>${family.versions.joinToString(", ").escapeXml()}</td><td>${family.count}</td>""" +
                """<td>${formatMsValue(family.totalDuration)}</td><td>${formatBytes(family.cacheSize.toLong())}</td>""" +
                """<td>${family.mostExpensiveVersion.escapeXml()}</td></tr>"""
        }
        return """
            <section class="card">
              <h2>Dependencies transformed under multiple versions (${fragmented.size})</h2>
              <p class="hint">Each distinct version is fingerprinted and transformed separately; aligning versions removes duplicated transform work.</p>
              <div class="scroll"><table class="frag"><thead><tr><th>Family</th><th>Versions</th><th>Detail</th><th>Count</th><th>Duration</th><th>Cache size</th><th>Most expensive</th></tr></thead><tbody>$rows</tbody></table></div>
            </section>
        """.trimIndent()
    }

    /**
     * Console-style outcome tables: one row per outcome with count, duration, average duration,
     * fingerprinting and average fingerprinting side by side — mirroring the OutcomeView and
     * AvoidanceSavingsOutcomeView text tables.
     */
    private fun outcomeTablesSection(): String {
        fun table(
            title: String,
            hint: String,
            counts: Map<String, Int>,
            durations: Map<String, Int>,
            fingerprints: Map<String, Int>
        ): String {
            if (counts.isEmpty()) return ""
            val rows = counts.entries.sortedByDescending { it.value }.joinToString("") { (outcome, count) ->
                val duration = durations[outcome] ?: 0
                val finger = fingerprints[outcome] ?: 0
                val avgDuration = if (count > 0) duration / count else 0
                val avgFinger = if (count > 0) finger / count else 0
                """<tr><td>${outcome.escapeXml()}</td><td>$count</td>""" +
                    """<td>${formatMsValue(duration)}</td><td>${formatMsValue(avgDuration)}</td>""" +
                    """<td>${formatMsValue(finger)}</td><td>${formatMsValue(avgFinger)}</td></tr>"""
            }
            return card(
                title,
                hint,
                listOf("Outcome", "Count", "Duration", "Avg duration", "Fingerprinting", "Avg fingerprinting"),
                rows
            )
        }
        return table(
            "Transforms by outcome",
            "Execution outcome of each transform (from_cache, up_to_date, success, ...).",
            transforms.groupByOutcome(),
            transforms.durationByOutcome(),
            transforms.fingerprintingByOutcome()
        ) + table(
            "Transforms by avoidance outcome",
            "How transforms resolved against the cache (avoided_* = reused, executed_* = ran). " +
                "Total and per-transform-average duration and fingerprinting time.",
            transforms.groupByAvoidanceOutcome(),
            transforms.durationByAvoidanceOutcome(),
            transforms.fingerprintingByAvoidanceOutcome()
        )
    }

    /**
     * Transforms whose `avoidanceSavings` is negative — reusing the cached output cost more time than
     * re-executing it. Aggregated per transform type with the count, total time lost, and worst single
     * case, so the report flags caching that is a net loss. Hidden entirely when there are none.
     */
    private fun negativeAvoidanceSection(): String {
        val negativeByType = transforms
            .mapNotNull { t -> t.avoidanceSavings?.toIntOrNull()?.let { t to it } }
            .filter { it.second < 0 }
            .groupBy { it.first.transformActionType }
        if (negativeByType.isEmpty()) return ""

        val rows = negativeByType
            .map { (type, pairs) -> Triple(type, pairs.size, pairs.sumOf { it.second }) }
            .sortedBy { it.third }
            .joinToString("") { (type, count, aggregated) ->
                val worst = negativeByType.getValue(type).minOf { it.second }
                """<tr><td>${type.extractName().escapeXml()}</td><td>$count</td>""" +
                    """<td>${formatMsValue(aggregated)}</td><td>${formatMsValue(worst)}</td></tr>"""
            }
        return card(
            "Negative avoidance savings by transform type",
            "Transforms where reusing the cached output cost more time than re-executing it (negative savings). " +
                "Total time lost and worst single case per type — candidates for fixing transform cacheability.",
            listOf("Transform type", "Negative count", "Aggregated lost", "Worst single"),
            rows
        )
    }

    private fun isMultiBuild(): Boolean = transforms.mapNotNull { it.buildScanId }.distinct().size > 1

    private fun buildLevelSection(): String {
        if (!isMultiBuild()) return ""
        val counts = transforms.countByBuildScan().toMap()
        val hitRates = transforms.cacheHitRateByBuildScan().toMap()
        val slowest = transforms.slowestTransformByBuildScan().toMap()
        val topType = transforms.topTransformTypeByBuildScan().toMap()
        val outliers = transforms.outlierBuildScans().toSet()

        val rows = transforms.durationByBuildScan().joinToString("") { (build, duration) ->
            val flag = if (build in outliers) " ⚠" else ""
            val slowestLabel = slowest[build]?.let {
                "${it.transformActionType.substringAfterLast(".")} (${formatMsValue(it.duration.toIntOrNull() ?: 0)})"
            } ?: "—"
            """<tr><td>${build.escapeXml()}$flag</td><td>${formatMsValue(duration)}</td><td>${counts[build] ?: 0}</td>""" +
                """<td>${((hitRates[build] ?: 0.0) * 100).roundTo(1)}%</td><td>${slowestLabel.escapeXml()}</td>""" +
                """<td>${(topType[build] ?: "").substringAfterLast(".").escapeXml()}</td></tr>"""
        }
        val outlierNote = if (outliers.isNotEmpty()) {
            """<p class="hint">⚠ Outlier build(s): ${outliers.joinToString(", ").escapeXml()} — total transform duration over 2× the median across builds.</p>"""
        } else {
            ""
        }
        return """
            <section class="card">
              <h2>Build-level analysis (${transforms.mapNotNull { it.buildScanId }.distinct().size} build scans)</h2>
              $outlierNote
              <div class="scroll"><table class="frag"><thead><tr><th>Build scan</th><th>Duration</th><th>Count</th><th>Hit rate</th><th>Slowest transform</th><th>Top type</th></tr></thead><tbody>$rows</tbody></table></div>
            </section>
        """.trimIndent()
    }

    private fun pipelineSection(): String {
        val svg = pipelineSvg() ?: return ""
        return """
            <section class="card">
              <h2>Artifact transform pipeline</h2>
              <p class="hint">Inferred from changed-attribute transitions: each box is an artifact type, each arrow a transform turning one type into another (left to right = processing order). Arrow thickness reflects total duration.</p>
              <div class="scroll">$svg</div>
            </section>
        """.trimIndent()
    }

    private fun pipelineSvg(): String? {
        val edges = transforms.attributeTransitionEdges().take(24)
        if (edges.isEmpty()) return null

        val levels = edges.artifactLevels()
        val order = edges.topologicalArtifactOrder()
        val nodes = (edges.map { it.from } + edges.map { it.to }).distinct()
        val byLevel = nodes.groupBy { levels[it] ?: 0 }.toSortedMap()

        val colW = 240
        val rowH = 60
        val nodeW = 185
        val nodeH = 36
        val marginX = 16
        val marginY = 16
        val maxLevel = byLevel.keys.maxOrNull() ?: 0
        val maxRows = byLevel.values.maxOf { it.size }
        val width = marginX * 2 + maxLevel * colW + nodeW
        val height = marginY * 2 + (maxRows - 1) * rowH + nodeH

        val pos = HashMap<String, Pair<Int, Int>>()
        byLevel.forEach { (level, levelNodes) ->
            levelNodes.sortedBy { order.indexOf(it) }.forEachIndexed { i, node ->
                pos[node] = (marginX + level * colW) to (marginY + i * rowH)
            }
        }

        val palette = listOf("#6aa6ff", "#5ad19a", "#f7c948", "#ef6f6c", "#b48ef0", "#4fd1c5", "#f6995c")
        val maxDuration = edges.maxOf { it.totalDuration }.coerceAtLeast(1)

        val sb = StringBuilder()
        sb.append("""<svg width="$width" height="$height" viewBox="0 0 $width $height" xmlns="http://www.w3.org/2000/svg" font-family="sans-serif">""")
        sb.append("""<defs><marker id="arrow" markerWidth="8" markerHeight="8" refX="7" refY="4" orient="auto"><path d="M0,0 L8,4 L0,8 z" fill="#5a6072"/></marker></defs>""")

        edges.forEach { edge ->
            val from = pos[edge.from] ?: return@forEach
            val to = pos[edge.to] ?: return@forEach
            val x1 = from.first + nodeW
            val y1 = from.second + nodeH / 2
            val x2 = to.first
            val y2 = to.second + nodeH / 2
            val cx1 = x1 + colW / 3
            val cx2 = x2 - colW / 3
            val strokeWidth = 1.0 + 5.0 * edge.totalDuration / maxDuration
            val edgeTitle = "${edge.from} → ${edge.to} · ${edge.count} transforms · ${edge.totalDuration}ms total · ${edge.medianDuration}ms median"
            sb.append(
                """<path d="M$x1,$y1 C$cx1,$y1 $cx2,$y2 ${x2 - 7},$y2" """ +
                    """fill="none" stroke="#5a6072" stroke-width="${"%.1f".format(strokeWidth)}" stroke-opacity="0.55" marker-end="url(#arrow)">""" +
                    """<title>${edgeTitle.escapeXml()}</title></path>"""
            )
        }

        nodes.forEach { node ->
            val (x, y) = pos[node] ?: return@forEach
            val color = palette[(levels[node] ?: 0) % palette.size]
            val label = if (node.length > 24) node.take(23) + "…" else node
            // wrap node in a <g> with a <title> so hovering anywhere on the box shows the full name
            sb.append("""<g><title>${node.escapeXml()}</title>""")
            sb.append("""<rect x="$x" y="$y" width="$nodeW" height="$nodeH" rx="7" fill="#1f2330" stroke="$color" stroke-width="1.5"/>""")
            sb.append(
                """<text x="${x + nodeW / 2}" y="${y + nodeH / 2 + 4}" text-anchor="middle" """ +
                    """fill="#e5e9f0" font-size="12">${label.escapeXml()}</text></g>"""
            )
        }
        sb.append("</svg>")
        return sb.toString()
    }

    private fun String.escapeXml(): String =
        replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")

    private fun totalCacheSizeBytes(): Long =
        transforms.filter { it.cacheArtifactSize != null }
            .sumOf { it.cacheArtifactSize.toLongOrNull() ?: 0L }

    private fun formatBytes(bytes: Long): String = when {
        bytes >= 1_073_741_824 -> "${(bytes / 1_073_741_824.0).roundTo(1)} GB"
        bytes >= 1_048_576 -> "${(bytes / 1_048_576.0).roundTo(1)} MB"
        bytes >= 1_024 -> "${(bytes / 1_024.0).roundTo(1)} KB"
        else -> "$bytes B"
    }

    private fun formatMsValue(ms: Int): String {
        val abs = Math.abs(ms)
        val formatted = when {
            abs >= 60_000 -> "${(abs / 60_000.0).roundTo(1)} min"
            abs >= 1_000 -> "${(abs / 1_000.0).roundTo(1)} s"
            else -> "$abs ms"
        }
        return if (ms < 0) "-$formatted" else formatted
    }

    private fun Double.roundTo(decimals: Int): Double {
        var factor = 1.0
        repeat(decimals) { factor *= 10 }
        return Math.round(this * factor) / factor
    }
}
