package org.o7.Fire.Framework;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.general.Dataset;
import org.jfree.data.general.Series;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;

public abstract class RealtimeChart<F extends Dataset, S extends Series> extends JFrame {
	public static float multiplierRatio = 1f;
	public static float ratioW = (1.9f * multiplierRatio), ratioH = (2.16f * multiplierRatio);
	public String XLegend, YLegend;
	protected F collection;
	JFreeChart jFreeChart;
	ChartPanel chartPanel;
	protected int limit = Integer.MAX_VALUE - 1;
	
	public RealtimeChart(String title, String XLegend, String YLegend) throws HeadlessException {
		super(title);
		this.XLegend = XLegend;
		this.YLegend = YLegend;
		GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
		DisplayMode displayMode = env.getDefaultScreenDevice().getDisplayMode();
		setLocation(env.getCenterPoint());
		setSize((int) (displayMode.getWidth() / ratioW), (int) (displayMode.getHeight() / ratioH));
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLayout(new BorderLayout());
	}
	
	protected HashMap<String, S> series = new HashMap<>();
	
	public F getCollection() {
		return collection;
	}
	
	public RealtimeChart<F, S> setCollection(F collection) {
		this.collection = collection;
		return this;
	}
	
	public abstract S newSeries(String name);
	
	public S getSeries(String name) {
		if (!series.containsKey(name)) {
			S s = null;
			try {
				s = getSeries((Comparable<?>) name);
			}catch (Exception e) {
			
			}
			if (s != null) {
				series.put(name, s);
			}else {
				S sn = newSeries(name);
				series.put(name, sn);
				addSeries(sn);
			}
			
		}
		
		return series.get(name);
	}
	
	public abstract S getSeries(Comparable<?> c);
	
	public abstract RealtimeChart<F, S> addSeries(S series);
	
	public RealtimeChart<F, S> removeSeries(String key) {
		series.remove(key);
		removeSeries(key);
		return this;
	}
	
	public abstract RealtimeChart<F, S> removeSeries(Comparable<?> key);
	
	abstract JFreeChart createChart();
	
	@Override
	public void setVisible(boolean b) {
		if (b) {
			if (jFreeChart == null) {
				jFreeChart = createChart();
			}
			if (chartPanel == null) {
				chartPanel = new ChartPanel(jFreeChart);
				add(chartPanel);
			}
			repaint();
		}
		super.setVisible(b);
	}
}
