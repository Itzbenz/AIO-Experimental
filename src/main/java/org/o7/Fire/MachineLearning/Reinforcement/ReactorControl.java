package org.o7.Fire.MachineLearning.Reinforcement;

import Atom.Utility.Random;
import Atom.Utility.Utility;
import org.o7.Fire.MachineLearning.Framework.Reactor;
import org.o7.Fire.MachineLearning.Jenetic.RawBasicNeuralNet;

import java.io.IOException;
import java.util.function.Consumer;

public class ReactorControl {
	public final static Reactor reactor = new Reactor();
	public final static String welcome = "RBMK Reactor Control Version: " + Random.getInt(1, 10) + "." + Random.getInt(0, 5) + "." + Random.getInt(0, 3);
	public static Consumer<String> log = System.out::println;//interesting syntax
	static RawBasicNeuralNet neuralNet;
	
	public static void prepareAI() {
	
	}
	
	public static void lowerControlRod() {
		reactor.lowerControlRod();
		log.accept("Lowering control rod");
	}
	
	public static void raiseControlRod() {
		reactor.raiseControlRod();
		log.accept("Raising control rod");
	}
	
	public static String welcomes() {
		return welcome + "\n" + reactor.about();
	}
	
	public static void AI() {
	
	}
	
	public static void welcome() {
		log.accept(welcome);
		log.accept(reactor.about());
	}
	
	public static void update() {
		
		StringBuilder sb = new StringBuilder();
		if (reactor.reactorFuckingExploded()) {
			sb.append(welcome).append(System.lineSeparator());
			sb.append(reactor.about());
			sb.append(System.lineSeparator());
			sb.append("Reactor exploded https://tenor.com/view/vasu-gif-5203484").append(System.lineSeparator());
			sb.append("Total Reactor Output: ").append(reactor.getMegawattTotalOutput()).append(" MW").append(System.lineSeparator());
			sb.append("Total Interaction: ").append(reactor.getInteraction()).append(System.lineSeparator());
			sb.append("Total Iteration: ").append(reactor.getIteration()).append(System.lineSeparator());
		}else {
			reactor.update();
			sb.append(welcome).append(System.lineSeparator());
			sb.append(reactor.about());
			sb.append(System.lineSeparator());
			sb.append("Reactor Heat: ").append((int) (reactor.getHeat() * 100)).append(" %").append(System.lineSeparator());
			sb.append("Reactor Output: ").append((int) (reactor.getMegawattOutput())).append(" MW").append(System.lineSeparator());
			sb.append("Reactor Control: ").append((int) (reactor.getControl() * 100)).append(" %").append(System.lineSeparator());
			
		}
		log.accept(sb.toString());
	}
	
	public static void main(String[] args) throws IOException {
		welcome();
		Utility.convertThreadToInputListener(">", s -> {
			if (s != null) {
				if (s.equalsIgnoreCase("a")) {
					lowerControlRod();
				}else {
					raiseControlRod();
				}
			}
			update();
			System.out.println();
		});
	}
}
