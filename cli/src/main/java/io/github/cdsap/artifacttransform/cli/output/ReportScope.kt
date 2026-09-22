package io.github.cdsap.artifacttransform.cli.output

enum class ReportScope {
    Aggregate,
    SingleBuild;

    val fileNamePrefix: String
        get() =
            when (this) {
                Aggregate -> ""
                SingleBuild -> "single-"
            }
}
