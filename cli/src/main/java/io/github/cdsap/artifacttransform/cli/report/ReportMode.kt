package io.github.cdsap.artifacttransform.cli.report

enum class ReportMode {
    MultiBuild,
    SingleBuild;

    val fileNamePrefix: String
        get() =
            when (this) {
                MultiBuild -> ""
                SingleBuild -> "single-"
            }
}
