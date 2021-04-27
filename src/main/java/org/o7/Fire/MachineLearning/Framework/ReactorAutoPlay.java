package org.o7.Fire.MachineLearning.Framework;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYSeriesCollection;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class ReactorAutoPlay {
	static File model = new File("ReactorTestJenetic-NeuralNetwork-Best.json");
	static Gson gson = new GsonBuilder().setPrettyPrinting().create();
	
	public static void main(String[] args) throws FileNotFoundException {
		Reactor r = new Reactor();
		RawBasicNeuralNet neuralNet = gson.fromJson(new FileReader(model), RawBasicNeuralNet.class);
		int tick = 0;
		int max = 100 * 1000;
		ArrayList<XYDataItem> interfaceTimes = new ArrayList<>(max), heatOvertimes = new ArrayList<>(max), controlOvertimes = new ArrayList<>(max), reactorOutputOvertimes = new ArrayList<>(max), totalProfits = new ArrayList<>(max), totalProduceds = new ArrayList<>(max);
		while (!r.reactorFuckingExploded() && tick < max) {
			totalProfits.add(new XYDataItem(tick, r.getPayout()));
			totalProduceds.add(new XYDataItem(tick, r.getMegawattTotalOutput()));
			heatOvertimes.add(new XYDataItem(tick, r.getHeat()));
			controlOvertimes.add(new XYDataItem(tick, r.getControl()));
			reactorOutputOvertimes.add(new XYDataItem(tick, r.getPowerOutput()));
			tick++;
			long s = System.currentTimeMillis();
			neuralNet.process(r.factor());
			interfaceTimes.add(new XYDataItem(tick, (double) System.currentTimeMillis() - s));
			double[] output = neuralNet.process(r.factor());
			if (output[0] > 0.5f) {
				r.raiseControlRod();
			}
			if (output[1] > 0.5f) {
				r.lowerControlRod();
			}
			r.update();
		}
		
		XYSeries totalProfit = new XYSeries("Total Profit (Million Dollar)", totalProfits);
		XYSeries totalProduced = new XYSeries("Total Energy Produced (MW)", totalProduceds);
		XYSeries heatOvertime = new XYSeries("Heat", heatOvertimes);
		XYSeries controlOvertime = new XYSeries("Control", controlOvertimes);
		XYSeries reactorOutputOvertime = new XYSeries("Output", reactorOutputOvertimes);
		
		Chart total = new Chart("Total Report", "Tick", "Total"), log = new Chart("Log Report", "Tick", "Factor");
		XYSeriesCollection c1 = new XYSeriesCollection(), c2 = new XYSeriesCollection();
		c1.addSeries(totalProduced);
		c1.addSeries(totalProfit);
		c1.addSeries(new XYSeries("NN Interface Time (ms)", interfaceTimes));
		c2.addSeries(heatOvertime);
		c2.addSeries(controlOvertime);
		c2.addSeries(reactorOutputOvertime);
		
		total.setDataset(c1);
		log.setDataset(c2);
		total.spawn();
		log.spawn();
	}
	
	public static class XYSeries extends org.jfree.data.xy.XYSeries {
		
		
		public XYSeries(Comparable key, boolean autoSort, boolean allowDuplicateXValues, List<XYDataItem> list) {
			super(key, autoSort, allowDuplicateXValues);
			this.data = list;
		}
		
		
		public XYSeries(String profit, ArrayList<XYDataItem> items) {
			this(profit, false, true, items);
		}
	}
}