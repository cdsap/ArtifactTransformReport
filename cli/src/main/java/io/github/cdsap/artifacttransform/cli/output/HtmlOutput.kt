package io.github.cdsap.artifacttransform.cli.output

import com.google.gson.Gson
import io.github.cdsap.artifacttransform.aggregatedCacheSizeByTransformActionType
import io.github.cdsap.artifacttransform.artifactLevels
import io.github.cdsap.artifacttransform.averageDurationByTransformActionType
import io.github.cdsap.artifacttransform.maxDurationByTransformActionType
import io.github.cdsap.artifacttransform.p95DurationByAttributeTransition
import io.github.cdsap.artifacttransform.p95DurationByTransformActionType
import io.github.cdsap.artifacttransform.attributeTransitionEdges
import io.github.cdsap.artifacttransform.cacheHitRateByBuildScan
import io.github.cdsap.artifacttransform.cacheSizeByDependency
import io.github.cdsap.artifacttransform.cacheSizeBySourceCategory
import io.github.cdsap.artifacttransform.cacheSizeByTransformActionType
import io.github.cdsap.artifacttransform.countByBuildScan
import io.github.cdsap.artifacttransform.dependencyFamiliesFragmentedWithinBuild
import io.github.cdsap.artifacttransform.durationByAttributeTransition
import io.github.cdsap.artifacttransform.durationByAvoidanceOutcome
import io.github.cdsap.artifacttransform.durationByBuildScan
import io.github.cdsap.artifacttransform.durationByOutcome
import io.github.cdsap.artifacttransform.groupByAvoidanceOutcome
import io.github.cdsap.artifacttransform.groupByOutcome
import io.github.cdsap.artifacttransform.outlierBuildScans
import io.github.cdsap.artifacttransform.slowestTransformByBuildScan
import io.github.cdsap.artifacttransform.topTransformTypeByBuildScan
import io.github.cdsap.artifacttransform.durationByDependency
import io.github.cdsap.artifacttransform.durationByModule
import io.github.cdsap.artifacttransform.durationByProvider
import io.github.cdsap.artifacttransform.durationBySourceCategory
import io.github.cdsap.artifacttransform.durationByTransformActionType
import io.github.cdsap.artifacttransform.extractName
import io.github.cdsap.artifacttransform.medianDurationByTransformActionType
import io.github.cdsap.artifacttransform.overallCacheHitRate
import io.github.cdsap.artifacttransform.sortedByDurationDescending
import io.github.cdsap.artifacttransform.topNWithOther
import io.github.cdsap.artifacttransform.topologicalArtifactOrder
import io.github.cdsap.artifacttransform.totalAvoidanceSavings
import io.github.cdsap.artifacttransform.totalByTransformActionType
import io.github.cdsap.artifacttransform.totalDuration
import io.github.cdsap.geapi.client.model.ArtifactTransform
import java.io.File

/**
 * Writes a self-contained, interactive HTML report. Chart.js is bundled as a resource and inlined,
 * so the report renders offline with no external requests.
 */
class HtmlOutput(
    private val transforms: List<ArtifactTransform>,
    private val single: Boolean
) {
    private data class ChartSpec(
        val id: String,
        val type: String,
        val indexAxis: String,
        val title: String,
        val labels: List<String>,
        val data: List<Long>,
        val valueLabel: String
    )

    fun writeHtml() {
        val prefixFile = if (single) "single-" else ""
        val html = "${prefixFile}artifact-transforms-${System.currentTimeMillis()}.html"
        val startTimestamp = System.currentTimeMillis()
        File(html).writeText(render())
        val endTime = System.currentTimeMillis()
        println("File $html created in ${endTime - startTimestamp} ms")
    }

    private fun render(): String {
        val chartJs = javaClass.getResourceAsStream("/chart.umd.min.js")
            ?.bufferedReader()?.use { it.readText() }
            ?: error("chart.umd.min.js resource not found")

        val charts = buildCharts()
        val chartsJson = Gson().toJson(charts)

        val buildScans = transforms.groupBy { it.buildScanId }.count()
        val hitRate = (transforms.overallCacheHitRate() * 100).roundTo(2)
        val savings = transforms.totalAvoidanceSavings()
        val savingsStat = if (savings > 0) {
            """<div class="stat"><div class="v">${formatMsValue(savings)}</div><div class="l">Avoidance savings</div></div>"""
        } else {
            ""
        }

        return buildString {
            append(
                """
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
                  .grid { display:grid; grid-template-columns:repeat(auto-fit,minmax(420px,1fr)); gap:20px; padding:0 32px 40px; }
                  .card { background:var(--card); border:1px solid var(--border); border-radius:12px; padding:20px; }
                  .card h2 { margin:0 0 14px; font-size:14px; font-weight:600; }
                  .canvas-wrap { position:relative; height:320px; }
                  .card h2 { overflow-wrap:anywhere; }
                  .pipeline { margin:0 32px 20px; }
                  .pipeline .scroll { overflow:auto; }
                  .pipeline svg { display:block; }
                  .pipeline .hint { color:var(--muted); font-size:12px; margin:0 0 12px; }
                  table.frag { border-collapse:collapse; width:100%; font-size:13px; }
                  table.frag th, table.frag td { text-align:left; padding:7px 12px; border-bottom:1px solid var(--border); }
                  table.frag th { color:var(--muted); font-weight:600; text-transform:uppercase; font-size:11px; letter-spacing:.04em; }
                  table.frag td:nth-child(2) { color:var(--accent); font-weight:600; }
                  footer { color:var(--muted); font-size:12px; padding:0 32px 32px; }
                </style>
                <script>$chartJs</script>
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
                ${negativeAvoidanceSection()}
                ${buildLevelSection()}
                <main class="grid" id="grid"></main>
                ${versionFragmentationSection()}
                <footer>Charts are interactive (hover for full labels and values).</footer>
                <script>
                  const CHARTS = $chartsJson;
                  const palette = ['#6aa6ff','#5ad19a','#f7c948','#ef6f6c','#b48ef0','#4fd1c5','#f6995c','#7e8cff','#e879a6','#9ccc65'];
                  const grid = document.getElementById('grid');
                  CHARTS.forEach(function (c) {
                    const card = document.createElement('div'); card.className = 'card';
                    const h = document.createElement('h2'); h.textContent = c.title; card.appendChild(h);
                    const wrap = document.createElement('div'); wrap.className = 'canvas-wrap';
                    const canvas = document.createElement('canvas'); canvas.id = c.id;
                    wrap.appendChild(canvas); card.appendChild(wrap); grid.appendChild(card);
                    const horizontal = c.indexAxis === 'y';
                    function trunc(s) { return s.length > 28 ? s.slice(0, 27) + '…' : s; }
                    // Human-readable value formatting inferred from the chart's value label.
                    function fmtVal(v) {
                      var label = c.valueLabel || '';
                      if (label.indexOf('ms') !== -1) {
                        if (v >= 60000) return (v / 60000).toFixed(1) + ' min';
                        if (v >= 1000) return (v / 1000).toFixed(1) + ' s';
                        return v + ' ms';
                      }
                      if (label.indexOf('bytes') !== -1) { // data in raw bytes
                        if (v >= 1073741824) return (v / 1073741824).toFixed(1) + ' GB';
                        if (v >= 1048576) return (v / 1048576).toFixed(1) + ' MB';
                        if (v >= 1024) return (v / 1024).toFixed(1) + ' KB';
                        return v + ' B';
                      }
                      if (label.indexOf('%') !== -1) return v + '%';
                      return v;
                    }
                    const catTicks = {
                      color: '#9aa3b2', autoSkip: false,
                      callback: function (value) { return trunc(this.getLabelForValue(value)); }
                    };
                    const valTicks = { color: '#9aa3b2', callback: function (value) { return fmtVal(value); } };
                    let scales = {};
                    if (c.type !== 'doughnut') {
                      scales[horizontal ? 'y' : 'x'] = { ticks: catTicks, grid: { color: '#2a2e3a' } };
                      scales[horizontal ? 'x' : 'y'] = { ticks: valTicks, grid: { color: '#2a2e3a' }, beginAtZero: true };
                    }
                    new Chart(canvas, {
                      type: c.type,
                      data: {
                        labels: c.labels,
                        datasets: [{
                          label: c.valueLabel,
                          data: c.data,
                          backgroundColor: c.type === 'doughnut'
                            ? c.labels.map(function (_, i) { return palette[i % palette.length]; })
                            : palette[0],
                          borderWidth: c.type === 'doughnut' ? 1 : 0,
                          borderColor: '#1a1d27'
                        }]
                      },
                      options: {
                        indexAxis: horizontal ? 'y' : 'x',
                        responsive: true, maintainAspectRatio: false,
                        plugins: {
                          legend: { display: c.type === 'doughnut', labels: { color: '#9aa3b2' } },
                          tooltip: { callbacks: {
                            title: function (items) { return items[0].label; },
                            label: function (item) { return (c.valueLabel || '') + ': ' + fmtVal(item.parsed[horizontal ? 'x' : 'y']); }
                          } }
                        },
                        scales: scales
                      }
                    });
                  });
                </script>
                </body>
                </html>
                """.trimIndent()
            )
        }
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
            <section class="card pipeline">
              <h2>Dependencies transformed under multiple versions (${fragmented.size})</h2>
              <p class="hint">Each distinct version is fingerprinted and transformed separately; aligning versions removes duplicated transform work.</p>
              <div class="scroll"><table class="frag"><thead><tr><th>Family</th><th>Versions</th><th>Detail</th><th>Count</th><th>Duration</th><th>Cache size</th><th>Most expensive</th></tr></thead><tbody>$rows</tbody></table></div>
            </section>
        """.trimIndent()
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
        return """
            <section class="card pipeline">
              <h2>Negative avoidance savings by transform type</h2>
              <p class="hint">Transforms where reusing the cached output cost more time than re-executing it (negative savings). Total time lost and worst single case per type — candidates for fixing transform cacheability or disabling caching.</p>
              <div class="scroll"><table class="frag"><thead><tr><th>Transform type</th><th>Negative count</th><th>Aggregated lost</th><th>Worst single</th></tr></thead><tbody>$rows</tbody></table></div>
            </section>
        """.trimIndent()
    }

    private fun isMultiBuild(): Boolean = transforms.mapNotNull { it.buildScanId }.distinct().size > 1

    private fun buildLevelSection(): String {
        if (!isMultiBuild()) return ""
        val durations = transforms.durationByBuildScan().toMap()
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
            <section class="card pipeline">
              <h2>Build-level analysis (${transforms.mapNotNull { it.buildScanId }.distinct().size} build scans)</h2>
              $outlierNote
              <div class="scroll"><table class="frag"><thead><tr><th>Build scan</th><th>Duration</th><th>Count</th><th>Hit rate</th><th>Slowest transform</th><th>Top type</th></tr></thead><tbody>$rows</tbody></table></div>
            </section>
        """.trimIndent()
    }

    private fun pipelineSection(): String {
        val svg = pipelineSvg() ?: return ""
        return """
            <section class="card pipeline">
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

    private fun buildCharts(): List<ChartSpec> {
        val specs = mutableListOf<ChartSpec>()

        fun addSpec(id: String, type: String, indexAxis: String, title: String, labels: List<String>, data: List<Long>, valueLabel: String) {
            if (labels.isNotEmpty()) specs += ChartSpec(id, type, indexAxis, title, labels, data, valueLabel)
        }

        transforms.durationByTransformActionType().topNWithOther(10).let { data ->
            addSpec(
                "durationByType", "bar", "x", "Total duration by transform type",
                data.map { it.first.extractName() }, data.map { it.second.toLong() }, "Total duration (ms)"
            )
        }
        transforms.medianDurationByTransformActionType().take(10).let { data ->
            addSpec(
                "medianByType", "bar", "x", "Median duration by transform type",
                data.map { it.first.extractName() }, data.map { it.second.toLong() }, "Median duration (ms)"
            )
        }
        transforms.averageDurationByTransformActionType().take(10).let { data ->
            addSpec(
                "averageByType", "bar", "x", "Average duration by transform type",
                data.map { it.first.extractName() }, data.map { it.second.toLong() }, "Average duration (ms)"
            )
        }
        transforms.p95DurationByTransformActionType().take(10).let { data ->
            addSpec(
                "p95ByType", "bar", "x", "P95 duration by transform type",
                data.map { it.first.extractName() }, data.map { it.second.toLong() }, "P95 duration (ms)"
            )
        }
        transforms.maxDurationByTransformActionType().take(10).let { data ->
            addSpec(
                "maxByType", "bar", "x", "Max duration by transform type",
                data.map { it.first.extractName() }, data.map { it.second.toLong() }, "Max duration (ms)"
            )
        }
        transforms.totalByTransformActionType().take(10).let { data ->
            addSpec(
                "countByType", "bar", "x", "Count by transform type",
                data.map { it.first.extractName() }, data.map { it.second.toLong() }, "Count"
            )
        }
        transforms.groupByAvoidanceOutcome().toList().sortedByDescending { it.second }.let { data ->
            addSpec(
                "countByAvoidanceOutcome", "bar", "x", "Transforms by avoidance outcome",
                data.map { it.first.extractName() }, data.map { it.second.toLong() }, "Count"
            )
        }
        transforms.durationByAvoidanceOutcome().toList().sortedByDescending { it.second }.let { data ->
            addSpec(
                "durationByAvoidanceOutcome", "bar", "x", "Duration by avoidance outcome",
                data.map { it.first.extractName() }, data.map { it.second.toLong() }, "Duration (ms)"
            )
        }
        transforms.groupByOutcome().toList().sortedByDescending { it.second }.let { data ->
            addSpec(
                "countByOutcome", "bar", "x", "Transforms by outcome",
                data.map { it.first.extractName() }, data.map { it.second.toLong() }, "Count"
            )
        }
        transforms.durationByOutcome().toList().sortedByDescending { it.second }.let { data ->
            addSpec(
                "durationByOutcome", "bar", "x", "Duration by outcome",
                data.map { it.first.extractName() }, data.map { it.second.toLong() }, "Duration (ms)"
            )
        }
        transforms.durationByProvider().let { data ->
            addSpec(
                "durationByProvider", "bar", "x", "Transform duration by tool / plugin",
                data.map { it.first }, data.map { it.second.toLong() }, "Duration (ms)"
            )
        }
        transforms.durationBySourceCategory().let { data ->
            addSpec(
                "durationBySource", "bar", "x", "Transform duration by source category",
                data.map { it.first }, data.map { it.second.toLong() }, "Duration (ms)"
            )
        }
        transforms.durationByModule().topNWithOther(10).let { data ->
            addSpec(
                "durationByModule", "bar", "y", "Transform duration by first-party module",
                data.map { it.first }, data.map { it.second.toLong() }, "Duration (ms)"
            )
        }
        transforms.durationByAttributeTransition().topNWithOther(10).let { data ->
            addSpec(
                "attrTransitions", "bar", "y", "Duration by changed-attribute transition",
                data.map { it.first }, data.map { it.second.toLong() }, "Duration (ms)"
            )
        }
        transforms.p95DurationByAttributeTransition().take(10).let { data ->
            addSpec(
                "p95Transitions", "bar", "y", "P95 duration by changed-attribute transition",
                data.map { it.first }, data.map { it.second.toLong() }, "P95 duration (ms)"
            )
        }
        transforms.durationByDependency().topNWithOther(10).let { data ->
            addSpec(
                "byDependency", "bar", "y", "Duration by dependency",
                data.map { it.first }, data.map { it.second.toLong() }, "Duration (ms)"
            )
        }
        transforms.sortedByDurationDescending().take(10).let { data ->
            addSpec(
                "slowest", "bar", "y", "Slowest transforms",
                data.map { "${it.transformActionType.extractName()} ${it.artifactTransformExecutionName.substringBefore(" [")}" },
                data.map { it.duration.toIntOrNull()?.toLong() ?: 0L }, "Duration (ms)"
            )
        }
        if (isMultiBuild()) {
            transforms.durationByBuildScan().take(15).let { data ->
                addSpec(
                    "byBuildScan", "bar", "x", "Duration by build scan",
                    data.map { it.first }, data.map { it.second.toLong() }, "Duration (ms)"
                )
            }
            transforms.countByBuildScan().take(15).let { data ->
                addSpec(
                    "countByBuildScan", "bar", "x", "Transform count by build scan",
                    data.map { it.first }, data.map { it.second.toLong() }, "Count"
                )
            }
            transforms.cacheHitRateByBuildScan().take(15).let { data ->
                addSpec(
                    "hitRateByBuildScan", "bar", "x", "Cache hit rate by build scan",
                    data.map { it.first }, data.map { Math.round(it.second * 100) }, "Hit rate (%)"
                )
            }
        }
        transforms.aggregatedCacheSizeByTransformActionType().take(10).let { data ->
            addSpec(
                "cacheSizeByType", "bar", "x", "Cache size by transform type",
                data.map { it.first.extractName() }, data.map { it.second.toLong() }, "Cache size (bytes)"
            )
        }
        transforms.cacheSizeByTransformActionType().take(10).let { data ->
            addSpec(
                "heaviestCache", "bar", "y", "Heaviest cached transforms",
                data.map { it.first.substringBefore(" [") }, data.map { it.second.toLong() }, "Cache size (bytes)"
            )
        }
        transforms.cacheSizeByDependency().take(10).let { data ->
            addSpec(
                "cacheSizeByDependency", "bar", "y", "Cache size by dependency",
                data.map { it.first }, data.map { it.second.toLong() }, "Cache size (bytes)"
            )
        }
        transforms.cacheSizeBySourceCategory().let { data ->
            addSpec(
                "cacheSizeBySource", "bar", "x", "Cache size by source category",
                data.map { it.first }, data.map { it.second.toLong() }, "Cache size (bytes)"
            )
        }
        return specs
    }

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
