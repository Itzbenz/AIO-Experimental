package org.o7.Fire.Experimental;

import Atom.Reflect.UnThread;
import Atom.Utility.Pool;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.awt.*;

public class RealtimeChart {
	public static void main(String[] args) {
		XYSeries xySeries = new XYSeries("Rate");
		xySeries.add(0.1f, 0.1f);
		xySeries.add(0.2f, 0.3f);
		XYSeriesCollection collection = new XYSeriesCollection(xySeries);
		JFreeChart chart = ChartFactory.createXYLineChart("Chart", "Second", "Rate", collection);
		JFrame window = new JFrame("Assad");
		window.setSize(600, 400);
		window.setLayout(new BorderLayout());
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		//chart.setBackgroundPaint(Color.DARK_GRAY);
		ChartPanel c = new ChartPanel(chart);
		//c.setBackground(Color.darkGray);
		window.add(c, BorderLayout.CENTER);
		window.setBackground(Color.darkGray);
		window.setVisible(true);
		window.repaint();
		Pool.daemon(() -> {
			int i = 0;
			while (!Thread.interrupted()) {
				xySeries.add(i, (double) Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory());
				i++;
				window.repaint();
				UnThread.sleep(1000);
			}
		}).start();
	}
}
