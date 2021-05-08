package org.o7.Fire.Framework;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeriesCollection;

import java.awt.*;
import java.util.Date;
import java.util.function.Supplier;

public class TimeSeriesChart extends RealtimeChart<TimeSeriesCollection, TimeSeriesSlightlyBetter> {
	
	public Supplier<RegularTimePeriod> period = () -> new Second(new Date());
	public long maxAge = 59;
	
	public TimeSeriesChart(String title, String YLegend) throws HeadlessException {
		super(title, "Period", YLegend);
	}
	
	
	@Override
	public TimeSeriesCollection getCollection() {
		if (collection == null) collection = new TimeSeriesCollection();
		return super.getCollection();
	}
	
	
	@Override
	public TimeSeriesSlightlyBetter newSeries(String name) {
		TimeSeriesSlightlyBetter t = new TimeSeriesSlightlyBetter(name, period, maxAge);
		return t;
	}
	
	@Override
	public TimeSeriesSlightlyBetter getSeries(Comparable<?> c) {
		return (TimeSeriesSlightlyBetter) getCollection().getSeries(c);
	}
	
	@Override
	public RealtimeChart<TimeSeriesCollection, TimeSeriesSlightlyBetter> addSeries(TimeSeriesSlightlyBetter series) {
		getCollection().addSeries(series);
		return this;
	}
	
	
	@Override
	public RealtimeChart<TimeSeriesCollection, TimeSeriesSlightlyBetter> removeSeries(Comparable<?> key) {
		getCollection().removeSeries(getSeries(key));
		return this;
	}
	
	@Override
	JFreeChart createChart() {
		return ChartFactory.createTimeSeriesChart(getTitle(), period.get().getClass().getSimpleName(), YLegend, getCollection());
	}
}
