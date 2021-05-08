package org.o7.Fire.Framework;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import java.awt.*;

public class XYRealtimeChart extends RealtimeChart<XYSeriesCollection, XYSeries> {
	public XYRealtimeChart(String title, String XLegend, String YLegend) throws HeadlessException {
		super(title, XLegend, YLegend);
	}
	
	@Override
	public XYSeriesCollection getCollection() {
		if (collection == null) collection = new XYSeriesCollection();
		return super.getCollection();
	}
	
	@Override
	public XYSeries newSeries(String name) {
		return new XYSeries(name);
	}
	
	@Override
	public XYSeries getSeries(Comparable<?> c) {
		return getCollection().getSeries(c);
	}
	
	@Override
	public RealtimeChart<XYSeriesCollection, XYSeries> addSeries(XYSeries series) {
		getCollection().addSeries(series);
		return this;
	}
	
	@Override
	public RealtimeChart<XYSeriesCollection, XYSeries> removeSeries(Comparable<?> key) {
		getCollection().removeSeries(getCollection().getSeries(key));
		return this;
	}
	
	
	@Override
	JFreeChart createChart() {
		return jFreeChart = ChartFactory.createXYLineChart(getTitle(), XLegend, YLegend, getCollection());
	}
	
	
}
