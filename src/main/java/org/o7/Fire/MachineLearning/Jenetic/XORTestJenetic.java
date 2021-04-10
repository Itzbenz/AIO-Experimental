package org.o7.Fire.MachineLearning.Jenetic;

import Atom.File.SerializeData;
import Atom.Time.Time;
import Atom.Time.Timer;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.jenetics.*;
import io.jenetics.engine.*;
import io.jenetics.stat.DoubleMomentStatistics;
import io.jenetics.util.DoubleRange;
import io.jenetics.util.Factory;
import io.jenetics.util.ISeq;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class XORTestJenetic {
	static File model = new File("XOR-Jenetic-NeuralNetwork.json"), lastPopulation = new File("XOR-Jenetic-Population.obj");
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
	
	static Phenotype<DoubleGene, Double> beest = null;
	static long sampleEvery = 100;
	
	public static double eval(double[] gt) {
		RawBasicNeuralNet net = new RawBasicNeuralNet(gt, structure);
		return eval(net);
	}
	
	static Supplier<Timer> timerSupplier = () -> new Timer(TimeUnit.SECONDS, 4);
	static ThreadLocal<Timer> timerThreadLocal = ThreadLocal.withInitial(timerSupplier);
	
	public static void main(String[] args) throws IOException, ClassNotFoundException {
		
		System.out.println("Controllable knob: " + knob);
		System.out.println("Knob range: " + -maxRange + " to " + maxRange);
		// 1.) Define the genotype (factory) suitable
		//     for the problem.
		
		Genotype<DoubleGene> best = null, worst = null;
		ISeq<Phenotype<DoubleGene, Double>> lastPop = null;
		if (lastPopulation.exists()) {
			System.out.println("Loading last population: " + lastPopulation.getAbsolutePath());
			lastPop = SerializeData.dataIn(lastPopulation);
			
		}
		System.out.println("1. Making random population");
		Factory<Genotype<DoubleGene>> gtf = Genotype.of(DoubleChromosome.of(-maxRange, maxRange, knob));
		// Codecs.ofVector(DoubleRange.of(-maxRange,maxRange), knob);
		EvolutionStatistics<Double, DoubleMomentStatistics> stat = EvolutionStatistics.ofNumber();
		// 3.) Create the execution environment.
		Engine<DoubleGene, Double> engine = Engine.builder(XORTestJenetic::eval, Codecs.ofVector(DoubleRange.of(-maxRange, maxRange), knob))//
				.populationSize(20)//
				.optimize(Optimize.MINIMUM)//because lower is better
				.alterers(new Mutator<>(0.03), new MeanAlterer<>(0.6))//assad
				.build();
		
		System.out.println("2. Testing population");
		System.out.println("3. Euthanasie/Modify unfit population");
		System.out.println("4. Back to 1");
		// 4.) Start the execution (evolution) and
		//     collect the result.
		Time t = new Time(TimeUnit.MILLISECONDS);
		List<EvolutionResult<DoubleGene, Double>> list =
				//RandomRegistry.with(new Random(123), r ->
				engine.stream()
						//.limit(Limits.bySteadyFitness(14))
						.limit(XORTestJenetic::timeOut)//assad
						.limit(Limits.byExecutionTime(Duration.ofSeconds(20))).peek(stat).sorted(Comparator.comparing(o -> o.bestPhenotype().fitness()))//assad
						.collect(Collectors.toList())//assad
				//)
				;//wtf
		
		System.out.println();
		EvolutionResult<DoubleGene, Double> bestPop = list.get(0);
		lastPop = bestPop.population();
		best = bestPop.bestPhenotype().genotype();
		worst = list.get(list.size() - 1).bestPhenotype().genotype();
		RawBasicNeuralNet bestNet = new RawBasicNeuralNet(best, structure);
		System.out.println("Best loss: " + eval(best));
		System.out.println("Worst loss: " + eval(worst));
		System.out.println("Total Population: " + list.size());
		System.out.println("Took: " + t.elapsedS());
		System.out.println("Note: lower better");
		System.out.println();
		
		System.out.println("Stat for nerd:");
		System.out.println(String.valueOf(stat));
		System.out.println("Note: lower better");
		model.delete();
		lastPopulation.delete();
		Files.writeString(model.toPath(), gson.toJson(bestNet), StandardOpenOption.WRITE, StandardOpenOption.CREATE);
		SerializeData.dataOut(bestPop.population(), lastPopulation);
	}
	
	public static boolean timeOut(EvolutionResult<DoubleGene, Double> evolutionResult) {
		if (timerThreadLocal.get() == null) return false;
		boolean sample = (evolutionResult.generation() / sampleEvery) == sampleEvery;
		if (beest == null || beest.fitness().compareTo(evolutionResult.bestPhenotype().fitness()) > 0 || sample) {
			beest = evolutionResult.bestPhenotype();
			System.out.println();
			System.out.println("Generation: " + evolutionResult.generation());
			System.out.println("Fitness: " + evolutionResult.bestPhenotype().fitness());
			RawBasicNeuralNet net = new RawBasicNeuralNet(evolutionResult.bestPhenotype().genotype(), structure);
			for (int i = 0; i < X.length; i++) {
				System.out.println(i + ". XOR: " + Arrays.toString(X[i]) + ", Output: " + net.process(X[i])[0] + ", Expected: " + Y[i][0]);
			}
			timerThreadLocal.get().reset();
		}
		if (timerThreadLocal.get().get()) timerThreadLocal.set(null);
		return true;
	}
	
	public static void evolutionNews(EvolutionResult<DoubleGene, Double> evolutionResult) {
		boolean sample = (evolutionResult.generation() / sampleEvery) == sampleEvery;
		if (beest == null || beest.fitness().compareTo(evolutionResult.bestPhenotype().fitness()) > 0 || sample) {
			beest = evolutionResult.bestPhenotype();
			System.out.println();
			System.out.println("Generation: " + evolutionResult.generation());
			System.out.println("Fitness: " + evolutionResult.bestPhenotype().fitness());
			RawBasicNeuralNet net = new RawBasicNeuralNet(evolutionResult.bestPhenotype().genotype(), structure);
			for (int i = 0; i < X.length; i++) {
				System.out.println(i + ". XOR: " + Arrays.toString(X[i]) + ", Output: " + net.process(X[i])[0] + ", Expected: " + Y[i][0]);
			}
			
		}
	}
	
	private static double eval(Genotype<DoubleGene> genotype) {
		RawBasicNeuralNet net = new RawBasicNeuralNet(genotype, structure);
		return eval(net);
	}
}
