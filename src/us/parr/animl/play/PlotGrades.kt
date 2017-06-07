/*
 * Copyright (c) 2017 Terence Parr. All rights reserved.
 * Use of this file is governed by the BSD 3-clause license that
 * can be found in the LICENSE file in the project root.
 */

package us.parr.animl.play

import org.knowm.xchart.CategoryChart
import org.knowm.xchart.CategoryChartBuilder
import org.knowm.xchart.Histogram
import org.knowm.xchart.SwingWrapper
import org.knowm.xchart.style.Styler
import java.util.Collections.min

fun main(args: Array<String>) {

    // Create Chart
    val chart = CategoryChartBuilder().width(800).height(600).title("Score Histogram").xAxisTitle("Mean").yAxisTitle("Count").build()

    // Customize Chart
    chart.styler.legendPosition = Styler.LegendPosition.InsideNW
    chart.styler.availableSpaceFill = .96
    chart.styler.isOverlapped = true
    chart.styler.setXAxisDecimalPattern("#00.0")

    val grades = doubleArrayOf(
            92.65, 93.87, 74.06, 86.94, 92.26, 94.46, 92.94, 80.65, 92.86,
            85.94, 91.79, 95.23, 85.37, 87.85, 87.71, 93.03
    )
    val histogram1 = Histogram(grades.toList(), 30, min(grades.toList()), 100.0)
    chart.addSeries("histogram 1", histogram1.getxAxisData(), histogram1.getyAxisData())
    SwingWrapper<CategoryChart>(chart).displayChart()
}