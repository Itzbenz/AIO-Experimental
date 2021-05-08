package org.o7.Fire.Experimental;

import Atom.Reflect.UnThread;
import Atom.Utility.Pool;
import Atom.Utility.Random;
import org.jfree.data.xy.XYSeries;
import org.o7.Fire.Framework.RealtimeChart;

public class RTChart {
	public static void main(String[] args) {
		XYSeries xySeries = new XYSeries("Float"), assadSer = new XYSeries("Double");
		RealtimeChart window = new RealtimeChart("Asasd", "Value", "Second");
		window.getCollection().addSeries(xySeries);
		window.getCollection().addSeries(assadSer);
		window.setVisible(true);
		Pool.daemon(() -> {
			int i = 0;
			while (!Thread.interrupted()) {
				xySeries.add(i, Random.getFloat());
				assadSer.add(i, Random.getDouble());
				i++;
				window.repaint();
				UnThread.sleep(1000);
			}
		}).start();
	}
}
