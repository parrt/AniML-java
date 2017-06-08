/*
 * Copyright (c) 2017 Terence Parr. All rights reserved.
 * Use of this file is governed by the BSD 3-clause license that
 * can be found in the LICENSE file in the project root.
 */

package us.parr.animl.play;

import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.XYSeries;
import org.knowm.xchart.style.Styler;

import static us.parr.lib.ParrtStats.normal;

public class TestXChart {
	public static void main(String[] args) {
		XYChart chart = new XYChartBuilder().width(800).height(600).build();

		// Customize Chart
		chart.getStyler().setDefaultSeriesRenderStyle(XYSeries.XYSeriesRenderStyle.Scatter);
		chart.getStyler().setChartTitleVisible(false);
		chart.getStyler().setLegendPosition(Styler.LegendPosition.InsideSW);
		chart.getStyler().setXAxisMax(5.0);
		chart.getStyler().setXAxisMin(-5.0);
		chart.getStyler().setYAxisMax(5.0);
		chart.getStyler().setYAxisMin(-5.0);
		chart.getStyler().setMarkerSize(2);

		// Series
		double[] xData = normal(0, 1, 1000);
		double[] yData = normal(0, 1, 1000);
		chart.addSeries("Gaussian Blob", xData, yData);
		new SwingWrapper<>(chart).displayChart();
	}
}
