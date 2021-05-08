package org.o7.Fire.Framework;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.data.general.Series;
import org.jfree.data.time.DynamicTimeSeriesCollection;
import org.jfree.data.time.RegularTimePeriod;

import java.awt.*;

public class DynamicTimeChart extends RealtimeChart<DynamicTimeSeriesCollection, Series> {
	public int nSeries = 1, nMoments = 120;
	public RegularTimePeriod period;
	
	public DynamicTimeChart(String title, RegularTimePeriod period, String YLegend) throws HeadlessException {
		super(title, period.getClass().getSimpleName(), YLegend);
		this.period = period;
	}
	
	@Override
	public DynamicTimeSeriesCollection getCollection() {
		if (collection == null) {
			collection = new DynamicTimeSeriesCollection(nSeries, nMoments, period);
			collection.setTimeBase(period);
		}
		return super.getCollection();
	}
	
	@Override
	public Series newSeries(String name) {
		return null;
	}
	
	@Override
	public Series getSeries(Comparable<?> c) {
		return null;
	}
	
	@Override
	public RealtimeChart<DynamicTimeSeriesCollection, Series> addSeries(Series series) {
		return null;
	}
	
	@Override
	public RealtimeChart<DynamicTimeSeriesCollection, Series> removeSeries(Comparable<?> key) {
		return null;
	}
	
	@Override
	JFreeChart createChart() {
		return ChartFactory.createTimeSeriesChart(getTitle(), XLegend, YLegend, getCollection());
	}
	
	
}
