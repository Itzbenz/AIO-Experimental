package org.o7.Fire.MachineLearning.Reinforcement;

import Atom.Reflect.UnThread;
import com.github.chen0040.rl.learning.sarsa.SarsaLearner;
import org.jfree.data.xy.XYSeries;
import org.o7.Fire.Framework.XYRealtimeChart;
import org.o7.Fire.MachineLearning.Framework.Reactor;

public class MaxSarsa {
	public static void main(String[] args) {
		int stateCount = 100;
		int actionCount = 100;
		
		SarsaLearner learner = new SarsaLearner(stateCount, actionCount);
		Reactor reactor = new Reactor();
		double reward = 0; // reward gained by transiting from prevState to currentState
		
		int currentStateId = 0;
		int currentActionId = learner.selectAction(currentStateId).getIndex();
		XYRealtimeChart chart = new XYRealtimeChart("Sarsa Learner", "Iteration", "Value");
		XYSeries state = chart.getSeries("Current State ID"), action = chart.getSeries("Current Action ID"), rewardS = chart.getSeries("Reward");
		int maxItem = 500;
		state.setMaximumItemCount(maxItem);
		action.setMaximumItemCount(maxItem);
		rewardS.setMaximumItemCount(maxItem);
		chart.setVisible(true);
		double rewardBound = actionCount * 2.2f;
		int incrementState = 1;
		for (int time = 0; time < maxItem * 100; ++time) {
			
			//System.out.println("Controller does action-"+currentActionId);
			if (currentStateId >= 100) incrementState = -1;
			if (currentStateId <= 0) incrementState = 1;
			int newStateId = currentStateId + incrementState;
			
			reward = reactor.totalPower();
			state.add(time, currentStateId);
			action.add(time, currentActionId);
			rewardS.add(time, reward);
			chart.repaint();
			//System.out.println("Now the new state is " + newStateId);
			//System.out.println("Controller receives Reward = " + reward);
			
			int futureActionId = learner.selectAction(newStateId).getIndex();
			
			//System.out.println("Controller is expected to do action-"+futureActionId);
			
			learner.update(currentStateId, currentActionId, newStateId, futureActionId, reward);
			
			currentStateId = newStateId;
			currentActionId = futureActionId;
			UnThread.sleep(16 * 2);
		}
	}
}
