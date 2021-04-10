package org.o7.Fire.MachineLearning.Jenetic;

import Atom.Time.Time;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.jenetics.DoubleChromosome;
import io.jenetics.DoubleGene;
import io.jenetics.Genotype;
import io.jenetics.Optimize;
import io.jenetics.engine.*;
import io.jenetics.stat.DoubleMomentStatistics;
import io.jenetics.util.DoubleRange;
import io.jenetics.util.Factory;
import io.jenetics.util.RandomRegistry;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class XORTestJenetic {
	static File model = new File("XOR-Jenetic-NeuralNetwork.json");
	static Gson gson = new GsonBuilder().setPrettyPrinting().create();
	static double[][] X = {{0F, 0F}, {1F, 0F}, {0F, 1F}, {1F, 1F}};
	static double[][] Y = {{0F}, {1F}, {1F}, {0F}};
	static int[] structure = new int[]{5, 4, 5, 3, 1};//prob gonna do genetic for this too
	static int knob = RawNeuralNet.needRaw(2, structure);
	static int maxRange = 100;
	
	public static double eval(RawBasicNeuralNet net) {
		double dd = 0;
		for (int i = 0; i < X.length; i++) {
			dd += net.error(X[i], Y[i]);
		}
		return dd;
	}
	
	private static double eval(double[] gt) {
		RawBasicNeuralNet net = new RawBasicNeuralNet(gt, structure);
		return eval(net);
	}
	
	public static void main(String[] args) throws IOException {
		System.out.println("Controllable knob: " + knob);
		System.out.println("Knob range: " + -maxRange + " to " + maxRange);
		// 1.) Define the genotype (factory) suitable
		//     for the problem.
		System.out.println("1. Making random population");
		Factory<Genotype<DoubleGene>> gtf = Genotype.of(DoubleChromosome.of(-maxRange, maxRange, knob));
		// Codecs.ofVector(DoubleRange.of(-maxRange,maxRange), knob);
		EvolutionStatistics<Double, DoubleMomentStatistics> stat = EvolutionStatistics.ofNumber();
		// 3.) Create the execution environment.
		Engine<DoubleGene, Double> engine = Engine.builder(XORTestJenetic::eval, Codecs.ofVector(DoubleRange.of(-maxRange, maxRange), knob)).populationSize(500).optimize(Optimize.MINIMUM)//because lower is better
				//				.alterers(new Mutator<>(0.03),new MeanAlterer<>(0.6))
				.build();
		
		System.out.println("2. Testing population");
		System.out.println("3. Euthanasie/Modify unfit population");
		System.out.println("4. Back to 1");
		// 4.) Start the execution (evolution) and
		//     collect the result.
		Time t = new Time(TimeUnit.MILLISECONDS);
		List<EvolutionResult<DoubleGene, Double>> list = RandomRegistry.with(new Random(123), r -> engine.stream().limit(Limits.bySteadyFitness(7)).peek(stat).sorted(Comparator.comparing(o -> o.bestPhenotype().fitness())).collect(Collectors.toList()));
		System.out.println();
		Genotype<DoubleGene> best = list.get(0).bestPhenotype().genotype(), worst = list.get(list.size() - 1).bestPhenotype().genotype();
		RawBasicNeuralNet bestNet = new RawBasicNeuralNet(best, structure);
		System.out.println("Best loss: " + eval(best));
		System.out.println("Worst loss: " + eval(worst));
		System.out.println("Took: " + t.elapsedS());
		System.out.println("Note: lower better");
		System.out.println();
		
		System.out.println("Stat for nerd:");
		System.out.println(stat);
		System.out.println("Note: lower better");
		Files.writeString(model.toPath(), gson.toJson(bestNet), StandardOpenOption.WRITE, StandardOpenOption.CREATE);
	}
	
	private static double eval(Genotype<DoubleGene> genotype) {
		RawBasicNeuralNet net = new RawBasicNeuralNet(genotype, structure);
		return eval(net);
	}
}
