package org.o7.Fire.Experimental;

import Atom.Reflect.UnThread;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.o7.Fire.Framework.XYRealtimeChart;
import org.o7.Fire.MachineLearning.Framework.RawBasicNeuralNet;
import org.o7.Fire.MachineLearning.Framework.Reactor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

public class ReactorAutoPlay {
	static File model = new File("ReactorTestJenetic-NeuralNetwork-Best.json");
	static Gson gson = new GsonBuilder().setPrettyPrinting().create();
	
	public static void main(String[] args) throws FileNotFoundException {
		Reactor r = new Reactor();
		RawBasicNeuralNet neuralNet = gson.fromJson(new FileReader(model), RawBasicNeuralNet.class);
		int tick = 0;
		int max = 100 * 1000;
		XYSeries totalProfit = new XYSeries("Total Profit (Million Dollar)");
		XYSeries totalProduced = new XYSeries("Total Energy Produced (MW)");
		XYSeries heatOvertime = new XYSeries("Heat");
		XYSeries controlOvertime = new XYSeries("Control");
		XYSeries reactorOutputOvertime = new XYSeries("Output");
		XYRealtimeChart total = new XYRealtimeChart("Total Report", "Tick", "Total"), log = new XYRealtimeChart("Log Report", "Tick", "Factor");
		XYSeriesCollection c1 = new XYSeriesCollection(), c2 = new XYSeriesCollection();
		c1.addSeries(totalProduced);
		c1.addSeries(totalProfit);
		c2.addSeries(heatOvertime);
		c2.addSeries(controlOvertime);
		c2.addSeries(reactorOutputOvertime);
		int maxItem = 100, maxItemReport = 5000;
		totalProduced.setMaximumItemCount(maxItemReport);
		totalProfit.setMaximumItemCount(maxItemReport);
		heatOvertime.setMaximumItemCount(maxItem);
		controlOvertime.setMaximumItemCount(maxItem);
		reactorOutputOvertime.setMaximumItemCount(maxItem);
		total.setCollection(c1);
		log.setCollection(c2);
		total.setVisible(true);
		log.setVisible(true);
		while (!r.reactorFuckingExploded() && tick < max) {
			
			neuralNet.process(r.factor());
			double[] output = neuralNet.process(r.factor());
			if (output[0] > 0.5f) {
				r.raiseControlRod();
			}
			if (output[1] > 0.5f) {
				r.lowerControlRod();
			}
			r.update();
			totalProfit.add(new XYDataItem(tick, r.getPayout()));
			totalProduced.add(new XYDataItem(tick, r.getMegawattTotalOutput()));
			heatOvertime.add(new XYDataItem(tick, r.getHeat()));
			controlOvertime.add(new XYDataItem(tick, r.getControl()));
			reactorOutputOvertime.add(new XYDataItem(tick, r.getPowerOutput()));
			tick++;
			total.repaint();
			log.repaint();
			UnThread.sleep(16 * 2);
		}
		
		
	}
	
	
}
