package io.github.cdsap.artifacttransform.cli.view

import com.jakewharton.picnic.RowDsl
import com.jakewharton.picnic.TextAlignment
import java.math.BigDecimal
import java.math.RoundingMode

fun Double.roundToTwoDecimalPlaces(): Double {
    return BigDecimal(this).setScale(2, RoundingMode.HALF_UP).toDouble()
}

fun RowDsl.valueMetric(value: Any) {
    cell(value) {
        alignment = TextAlignment.MiddleRight
    }
}

fun RowDsl.labelMetric(label: String) {
    cell(label) {
        alignment = TextAlignment.MiddleLeft
    }
}

fun Int.roundMilliseconds(): Int {
    if (this < 1000) return this
    return (this / 10) * 10
}

fun RowDsl.columnMetricHeaderWithSpan(text: String) {
    cell(text) {
        columnSpan = 2
        alignment = TextAlignment.MiddleCenter
    }
}

fun RowDsl.columnMetricHeader(text: String) {
    cell(text) {
        alignment = TextAlignment.MiddleCenter
    }
}

fun RowDsl.columnMetricHeaderWith3Span(text: String) {
    cell(text) {
        columnSpan = 3
        alignment = TextAlignment.MiddleCenter
    }
}
