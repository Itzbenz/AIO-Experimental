package org.o7.Fire.MachineLearning.Jenetic;

import Atom.Time.Time;
import Atom.Time.Timer;
import Atom.Utility.Pool;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.jenetics.DoubleGene;
import io.jenetics.Genotype;
import io.jenetics.Optimize;
import io.jenetics.Phenotype;
import io.jenetics.engine.*;
import io.jenetics.stat.DoubleMomentStatistics;
import io.jenetics.util.DoubleRange;
import org.jfree.data.xy.XYSeries;
import org.o7.Fire.Framework.XYRealtimeChart;
import org.o7.Fire.MachineLearning.Framework.RawBasicNeuralNet;
import org.o7.Fire.MachineLearning.Framework.RawNeuralNet;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class XORTestJenetic {
	static File model = new File("XOR-Jenetic-NeuralNetwork.json"), lastPopulation = new File("XOR-Jenetic-Population.obj");
	static Gson gson = new GsonBuilder().setPrettyPrinting().create();
	static double[][] X = {{0F, 0F}, {1F, 0F}, {0F, 1F}, {1F, 1F}};
	static double[][] Y = {{0F}, {1F}, {1F}, {0F}};
	static int[] structure = new int[]{5, 4, 5, 3, 1};//prob gonna do genetic for this too
	static int knob = RawNeuralNet.needRaw(2, structure);
	static int maxRange = 100;
	static Phenotype<DoubleGene, Double> beest = null;
	static long sampleEvery = 100;
	static Supplier<Timer> timerSupplier = () -> new Timer(TimeUnit.SECONDS, 20);
	static ThreadLocal<Timer> timerThreadLocal = ThreadLocal.withInitial(timerSupplier);
	static XYRealtimeChart chart = new XYRealtimeChart("XOR Jenetic", "Generation", "Loss");
	
	static XYSeries bestEval = chart.getSeries("Best Eval"), worstEval = chart.getSeries("Worst Eval"), eval = chart.getSeries("Eval");
	static Genotype<DoubleGene> best = null, worst = null;
	
	public static double eval(RawBasicNeuralNet net) {
		double dd = 0;
		for (int i = 0; i < X.length; i++) {
			dd += net.error(X[i], Y[i]);//or try to do action then return reward and you get neural network reinforcement learning genetic algorithm
		}
		return dd;
	}
	
	public static double eval(double[] gt) {
		RawBasicNeuralNet net = new RawBasicNeuralNet(gt, structure);
		return eval(net);
	}
	
	static {
		int count = 100;
		bestEval.setMaximumItemCount(count);
		worstEval.setMaximumItemCount(count);
		eval.setMaximumItemCount(count);
		chart.setVisible(true);
	}
	
	public static void main(String[] args) throws IOException, ClassNotFoundException {
		
		System.out.println("Controllable knob: " + knob);
		System.out.println("Knob range: " + -maxRange + " to " + maxRange);
		// 1.) Define the genotype (factory) suitable
		//     for the problem.
		
		
		Executor executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() - 1, Pool.daemonFactory);
		
		// Codecs.ofVector(DoubleRange.of(-maxRange,maxRange), knob);
		EvolutionStatistics<Double, DoubleMomentStatistics> stat = EvolutionStatistics.ofNumber();
		// 3.) Create the execution environment.
		Optimize optimize = Optimize.MINIMUM;
		Engine<DoubleGene, Double> engine = Engine.builder(XORTestJenetic::eval, Codecs.ofVector(DoubleRange.of(-maxRange, maxRange), knob))//
				.populationSize(500)//
				.optimize(optimize)//because lower is better
				//.alterers(new Mutator<>(0.8), new MeanAlterer<>(0.9))//assad
				.maximalPhenotypeAge(100).executor(executor).build();
		// 4.) Start the execution (evolution) and
		//     collect the result.
		Time t = new Time(TimeUnit.MILLISECONDS);
		//RandomRegistry.with(new Random(123), r ->
		long count = engine.stream()
				//.limit(XORTestJenetic::timeOut)//assad
				.limit(Limits.byExecutionTime(Duration.ofSeconds(60)))//play time
				//.filter(XORTestJenetic::evolutionNews)
				.peek(stat)//end of scenario result
				.peek(assad -> {//collector
					if (best == null) best = assad.bestPhenotype().genotype();
					if (worst == null) worst = assad.worstPhenotype().genotype();
					double bestScore = eval(assad.bestPhenotype().genotype()), worstScore = eval(assad.worstPhenotype().genotype());
					if (optimize.compare(eval(best), bestScore) == -1) best = assad.bestPhenotype().genotype();
					if (optimize.compare(eval(worst), worstScore) == 1) worst = assad.worstPhenotype().genotype();
					bestEval.add(assad.generation(), eval(best));
					worstEval.add(assad.generation(), eval(worst));
					eval.add(assad.generation(), (bestScore + worstScore) / 2F);
					try {
						chart.repaint();
					}catch (Exception ignored) {}
					;
				}).count();
		//)
		//wtf
		
		System.out.println();
		
		RawBasicNeuralNet bestNet = new RawBasicNeuralNet(best, structure);
		System.out.println("Best loss: " + eval(best));
		System.out.println("Worst loss: " + eval(worst));
		System.out.println("Took: " + t.elapsedS());
		System.out.println("Count: " + count);
		System.out.println("Note: lower better");
		System.out.println();
		
		System.out.println("Stat for nerd:");
		System.out.println(stat);
		System.out.println("Note: lower better");
		model.delete();
		lastPopulation.delete();
		
		//for(Map.Entry<Long, Double> s : generationScore.entrySet()){
		//	System.out.print(s.getKey()+","+s.getValue()+",");
		//	score.add(s.getKey(),s.getValue());
		//}
		
		
		Files.writeString(model.toPath(), gson.toJson(bestNet), StandardOpenOption.WRITE, StandardOpenOption.CREATE);
		Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
		for (Thread thread : threadSet)
			if (!thread.isDaemon())
				System.out.println(thread.getId() + ". " + thread.getName() + " alive ? " + thread.isAlive());
	}

	public static boolean timeOut(EvolutionResult<DoubleGene, Double> evolutionResult) {
		if (timerThreadLocal.get() == null) return false;
		boolean sample = (evolutionResult.generation() / sampleEvery) == sampleEvery;
		if (beest == null || beest.fitness().compareTo(evolutionResult.bestPhenotype().fitness()) > 0 || sample) {
			beest = evolutionResult.bestPhenotype();
			/*
			System.out.println();
			System.out.println("Generation: " + evolutionResult.generation());
			System.out.println("Loss: " + evolutionResult.bestPhenotype().fitness());
			
			
			RawBasicNeuralNet net = new RawBasicNeuralNet(evolutionResult.bestPhenotype().genotype(), structure);
			for (int i = 0; i < X.length; i++) {
				System.out.println(i + ". XOR: " + Arrays.toString(X[i]) + ", Output: " + net.process(X[i])[0] + ", Expected: " + Y[i][0]);
			}
			 */
			//generationScore.put(evolutionResult.generation(), evolutionResult.bestFitness());
			timerThreadLocal.get().reset();
		}
		if (timerThreadLocal.get().get()) timerThreadLocal.set(null);
		return true;
	}
	
	public static boolean evolutionNews(EvolutionResult<DoubleGene, Double> evolutionResult) {
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
			return true;
		}
		return false;
	}
	
	private static double eval(Genotype<DoubleGene> genotype) {
		RawBasicNeuralNet net = new RawBasicNeuralNet(genotype, structure);
		return eval(net);
	}
}
