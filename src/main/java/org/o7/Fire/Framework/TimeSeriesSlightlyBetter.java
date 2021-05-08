package org.o7.Fire.Framework;

import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.TimeSeries;

import java.util.function.Supplier;

public class TimeSeriesSlightlyBetter extends TimeSeries {
	public Supplier<RegularTimePeriod> period;
	public long maxAge;
	
	public TimeSeriesSlightlyBetter(Comparable name, Supplier<RegularTimePeriod> period, long maxAge) {
		super(name);
		this.period = period;
		this.maxAge = maxAge;
		setMaximumItemAge(maxAge);
	}
	
	public void add(Number data) {
		addOrUpdate(period.get(), data);
	}
	
}
