package org.o7.Fire.Experimental;

import Atom.Reflect.UnThread;
import com.github.chen0040.rl.learning.sarsa.SarsaLearner;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.deeplearning4j.rl4j.policy.DQNPolicy;
import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.o7.Fire.Framework.XYRealtimeChart;
import org.o7.Fire.MachineLearning.Framework.Reactor;
import org.o7.Fire.MachineLearning.RL4J.ReactorDQN;
import org.o7.Fire.MachineLearning.RL4J.ReactorMDP;
import org.o7.Fire.MachineLearning.RL4J.ReactorMDPDiscrete;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Set;

public class ReactorAutoPlay {
	public static HashMap<Integer, Set<Integer>> cachedAction = new HashMap<>();
	static Gson gson = new GsonBuilder().setPrettyPrinting().create();
	static File model = new File("Sarsa-Reactor.json");
	static String modelJson = null;
	static int stateCount = 100;
	static int actionCount = 3;
	static long saveEvery = 1000;
	static SarsaLearner learner = new SarsaLearner(stateCount, actionCount);
	static XYRealtimeChart chart = new XYRealtimeChart("Sarsa Learner", "Iteration", "Value");
	static XYSeries stateC = chart.getSeries("Current State ID"), actionC = chart.getSeries("Current Action ID"), rewardC = chart.getSeries("Reward");
	static int lastState = 0, lastAction = 0;
	
	public static DQNPolicy<ReactorMDP.ReactorObserver> DQN = null;
	
	static {
		cachedAction.put(0, Set.of(0, 2));
		cachedAction.put(1, Set.of(0, 1, 2));
		cachedAction.put(2, Set.of(0, 1));
	}
	
	static {
		if (model.exists()) try {
			System.out.println("Loading: " + model.getAbsolutePath());
			learner = SarsaLearner.fromJson(Files.readString(model.toPath()));
		}catch(Throwable e){
			//throw new RuntimeException(e);
		}
		int maxItem = 500;
		stateC.setMaximumItemCount(maxItem);
		actionC.setMaximumItemCount(maxItem);
		rewardC.setMaximumItemCount(maxItem);
		chart.setVisible(true);
	}
	
	public static Set<Integer> availableAction(Reactor r) {
		int state = (int) (r.getHeat() * 100);
		if (state > 98) return cachedAction.get(2);
		if (state <= 1) return cachedAction.get(0);
		return cachedAction.get(1);
	}
	
	public static void main(String[] args) throws Exception {
		Reactor r = new Reactor();
		//RawBasicNeuralNet neuralNet = gson.fromJson(new FileReader(model), RawBasicNeuralNet.class);
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
			
			//sarsa(r, tick);
			DQN(r, tick);
			
			totalProfit.add(new XYDataItem(tick, r.getPayout()));
			totalProduced.add(new XYDataItem(tick, r.getMegawattTotalOutput()));
			heatOvertime.add(new XYDataItem(tick, r.getHeat()));
			controlOvertime.add(new XYDataItem(tick, r.getControl()));
			reactorOutputOvertime.add(new XYDataItem(tick, r.getPowerOutput()));
			tick++;
			total.repaint();
			log.repaint();
			UnThread.sleep(16);
		}
		
		
	}
	
	public static void DQN(Reactor r, long iteration) throws IOException {
		if (DQN == null){
			System.out.println("Loading: " + ReactorDQN.save.getAbsolutePath());
			DQN = DQNPolicy.load(ReactorDQN.save.getAbsoluteFile().getAbsolutePath());
			System.out.println("Loaded");
		}
		
		INDArray p = Nd4j.create(r.factor(), 1, r.factor().length);
		int i = DQN.nextAction(p);
		ReactorMDPDiscrete.doAction(r, i);
		r.update();
		stateC.add(iteration, r.getControl() * 100);
		actionC.add(iteration, i * 30);
		rewardC.add(iteration, r.getPowerOutput() * 100);
	}
	
	public static void sarsa(Reactor r, long iteration) {
		int state = (int) (r.getHeat() * 100);
		int action = learner.selectAction(state).getIndex();
		if (action == -1) action = 0;
		if (action == 0){
		
		}else if (action == 1){
			r.lowerControlRod();
		}else if (action == 2){
			r.raiseControlRod();
		}else{
			throw new IllegalArgumentException("Action: " + action);
		}
		r.update();
		double reward = r.reactorFuckingExploded() ? -10 : (r.getHeat() * 100) > 95 ? 100 - state : r.getPowerOutput() * 100;
		
		stateC.add(iteration, state);
		actionC.add(iteration, action * 30);
		rewardC.add(iteration, reward);
		if (iteration != 0) {
			learner.update(lastState, lastAction, state, action, reward);
			lastAction = action;
			lastState = state;
		}
		if (r.reactorFuckingExploded()) r.reset();
		if (iteration % saveEvery == 0) {
			try {
				Files.writeString(model.toPath(), learner.toJson(), StandardOpenOption.WRITE, StandardOpenOption.CREATE);
			}catch (IOException e) {
				e.printStackTrace();
			}
		}
		chart.repaint();
		/*
		neuralNet.process(r.factor());
		double[] output = neuralNet.process(r.factor());
		if (output[0] > 0.5f) {
			r.raiseControlRod();
		}
		if (output[1] > 0.5f) {
			r.lowerControlRod();
		}
		
		 */
	}
	
	
}
