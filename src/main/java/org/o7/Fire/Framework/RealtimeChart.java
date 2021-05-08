package org.o7.Fire.Framework;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.awt.*;

public class RealtimeChart extends JFrame {
	public static float multiplierRatio = 1f;
	public static float ratioW = (1.9f * multiplierRatio), ratioH = (1.6f * multiplierRatio);
	public String XLegend, YLegend;
	protected XYSeriesCollection collection;
	JFreeChart jFreeChart;
	ChartPanel chartPanel;
	
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
	
	public XYSeriesCollection getCollection() {
		if (collection == null) collection = new XYSeriesCollection();
		return collection;
	}
	
	public RealtimeChart setCollection(XYSeriesCollection collection) {
		this.collection = collection;
		return this;
	}
	
	@Override
	public void setVisible(boolean b) {
		if (b) {
			if (jFreeChart == null) {
				jFreeChart = ChartFactory.createXYLineChart("Line Chart", XLegend, YLegend, collection);
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
