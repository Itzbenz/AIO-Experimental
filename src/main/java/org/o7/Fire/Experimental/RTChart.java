package org.o7.Fire.Experimental;

import Atom.Reflect.UnThread;
import Atom.Utility.Pool;
import org.jfree.data.time.Millisecond;
import org.jfree.data.xy.XYSeries;
import org.o7.Fire.Framework.DynamicTimeChart;
import org.o7.Fire.Framework.TimeSeriesChart;
import org.o7.Fire.Framework.TimeSeriesSlightlyBetter;

import java.util.Date;

public class RTChart {
	public static class TimeSeriesRT {
		public static void main(String[] args) {
			
			TimeSeriesChart window = new TimeSeriesChart("assad", "Value");
			window.period = () -> new Millisecond(new Date());
			window.maxAge = 5000;
			TimeSeriesSlightlyBetter assadSer = window.getSeries("Sin");
			window.setVisible(true);
			Pool.daemon(() -> {
				double d = 0;
				while (!Thread.interrupted()) {
					assadSer.add(Math.sin(d += 0.1));
					window.repaint();
					UnThread.sleep(16 * 2);
				}
			}).start();
		}
	}
	
	public static class XYRTChart {
		public static void main(String[] args) {
			org.o7.Fire.Framework.XYRealtimeChart window = new org.o7.Fire.Framework.XYRealtimeChart("assad", "Result", "Value");
			XYSeries assadSer = window.getSeries("Sin");
			assadSer.setMaximumItemCount(100);
			window.setVisible(true);
			Pool.daemon(() -> {
				double count = 0;
				while (!Thread.interrupted()) {
					try {
						assadSer.add(count, Math.sin(count += 0.1));
						
						window.repaint();
						UnThread.sleep(16);
					}catch (Exception e) {
					
					}
				}
			}).start();
		}
	}
	
	public static class DynamicTimeRT {
		public static void main(String[] args) {
			DynamicTimeChart window = new DynamicTimeChart("assad", new Millisecond(), "Value");
			window.nMoments = 500;
			window.setVisible(true);
			Pool.daemon(() -> {
				int counter = 0;
				float count = 0;
				float[] assad = new float[1];
				window.getCollection().addSeries(assad, 0, "Sin");
				while (!Thread.interrupted()) {
					try {
						float res = (float) Math.sin(count += 0.1f);
						assad[0] = res;
						counter++;
						if (counter == window.nMoments) counter = 0;
						window.getCollection().advanceTime();
						window.getCollection().appendData(assad);
						window.repaint();
						UnThread.sleep(16 * 2);
					}catch (Exception e) {
					
					}
				}
			}).start();
		}
	}
}
