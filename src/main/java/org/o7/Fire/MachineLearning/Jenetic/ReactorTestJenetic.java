package org.o7.Fire.MachineLearning.Jenetic;

import Atom.Struct.PoolObject;
import Atom.Time.Time;
import Atom.Time.Timer;
import Atom.Utility.Pool;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.jenetics.*;
import io.jenetics.engine.Engine;
import io.jenetics.engine.EvolutionResult;
import io.jenetics.engine.EvolutionStatistics;
import io.jenetics.engine.Limits;
import io.jenetics.stat.DoubleMomentStatistics;
import org.jfree.data.xy.XYSeries;
import org.o7.Fire.Framework.XYRealtimeChart;
import org.o7.Fire.MachineLearning.Framework.RawBasicNeuralNet;
import org.o7.Fire.MachineLearning.Framework.RawNeuralNet;
import org.o7.Fire.MachineLearning.Framework.Reactor;
import org.o7.Fire.MachineLearning.Primtive.NeuralFunction;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ReactorTestJenetic {
	public static final PoolObject<Reactor> reactorPool = new PoolObject<Reactor>() {
		@Override
		protected Reactor newObject() {
			return new Reactor();
		}
	};
	
	public File bestJsonModel = new File(this.getClass().getSimpleName() + "-NeuralNetwork-Best.json"), worstJsonModel = new File(this.getClass().getSimpleName() + "-NeuralNetwork-Worst.json"), lastPopulation = new File(this.getClass().getSimpleName() + "-Jenetic-Population.obj");
	public Gson gson = new GsonBuilder().setPrettyPrinting().create();
	static XYRealtimeChart neuralChart = new XYRealtimeChart("Neural Network", "Iteration", "Value");
	public float maxRange = 1;
	public int knob = RawNeuralNet.needRaw(new Reactor().factor().length, structure);
	public static Map<Integer, Double> evalCache = Collections.synchronizedMap(new HashMap<>());
	static XYSeries control = neuralChart.getSeries("Control"), heat = neuralChart.getSeries("Heat"), output1 = neuralChart.getSeries("Output 1"), output2 = neuralChart.getSeries("Output 2");
	public Engine<DoubleGene, Double> engine;
	public final PoolObject<Genotype<DoubleGene>> genePool = new PoolObject<>() {
		@Override
		protected Genotype<DoubleGene> newObject() {
			return Genotype.of(DoubleChromosome.of(-maxRange, maxRange, knob));
		}
	};
	public Executor executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() == 1 ? 1 : Runtime.getRuntime().availableProcessors() - 1, Pool.daemonFactory);
	public long sampleEvery = 100, duration = 10;
	public ThreadLocal<Timer> timerThreadLocal = ThreadLocal.withInitial(() -> new Atom.Time.Timer(TimeUnit.SECONDS, duration));
	public Optimize optimize = Optimize.MAXIMUM;
	protected Time t = new Time();
	EvolutionStatistics<Double, DoubleMomentStatistics> stat = EvolutionStatistics.ofNumber();
	public double top1 = 0;
	static long universalIteration = 0;
	
	static {
		int count = 40;
		control.setMaximumItemCount(count);
		heat.setMaximumItemCount(count);
		output1.setMaximumItemCount(count);
		output2.setMaximumItemCount(count);
		neuralChart.setVisible(true);
	}
	
	public int[] structure = new int[]{5, 10, 3, 2};//prob gonna do genetic for this too
	public final PoolObject<RawBasicNeuralNet> neuralPool = new PoolObject<RawBasicNeuralNet>() {
		@Override
		protected RawBasicNeuralNet newObject() {
			return new RawBasicNeuralNet(new double[knob], structure).setFunction(NeuralFunction.Relu);
		}
	};
	public String assad = optimize == Optimize.MAXIMUM ? "Fitness" : "Loss";
	XYRealtimeChart chart = new XYRealtimeChart("Reactor Jenetic", "Generation", assad);
	XYSeries bestEval = chart.getSeries("Best Eval"), worstEval = chart.getSeries("Worst Eval"), eval = chart.getSeries("Eval");
	Genotype<DoubleGene> best = null, worst = null;
	
	{
		int count = 100;
		bestEval.setMaximumItemCount(count);
		worstEval.setMaximumItemCount(count);
		eval.setMaximumItemCount(count);
		chart.setVisible(true);
	}
	
	public static void main(String[] args) throws IOException {
		ReactorTestJenetic jenetic = new ReactorTestJenetic();
		jenetic.prepare(1000);
		jenetic.execute();
		jenetic.post();
	}
	
	public static double eval(RawBasicNeuralNet raw) {
		//if (evalCache.containsKey(raw.hashCode())) return evalCache.get(raw.hashCode());
		Reactor reactor = reactorPool.obtain();
		double reward;
		int iteration = 40;
		for (int j = 0; j < iteration; j++) {
			double[] output = raw.process(reactor.factor());
			if (output[0] > 0.4f) reactor.raiseControlRod();
			if (output[1] > 0.4f) reactor.lowerControlRod();
			//reactor.setControl(reactor.getControl()+output[0]);
			//reactor.setControl(reactor.getControl()-output[1]);
			reactor.update();
			if (universalIteration % 80 < iteration) {
				heat.add(universalIteration, reactor.getHeat());
				control.add(universalIteration, reactor.getControl());
				output1.add(universalIteration, output[0]);
				output2.add(universalIteration, output[1]);
				neuralChart.repaint();
				universalIteration++;
			}
			if (reactor.reactorFuckingExploded()) break;
		}
		reward = reactor.reactorFuckingExploded() ? reactor.getIteration() - iteration : reactor.totalPower();
		/*
		int batch = batch = 10;
		double increment = (double) 1 / iteration;
		//if(Random.getBool()) reactor.reset();
	
		double heatGain = 0;
		for (int i = 0; i < batch; i++) {
			double heatDissipation = 0;
			for (int j = 0; j < batch; j++) {
				for (int ia = 0; ia < iteration; ia++) {
					double[] output = raw.process(reactor.factor());
					if (output[0] > 0.5f) reactor.raiseControlRod();
					if (output[1] > 0.5f) reactor.lowerControlRod();
					reactor.update();
					if (reactor.reactorFuckingExploded()) break;
				}
				reward = Math.min(reward, reactor.getPayout());
				if (reactor.reactorFuckingExploded()) break;
				reactor.reset();
				reactor.setHeatDissipation(heatDissipation).setHeatGain(heatGain);
				heatDissipation += increment;
			}
			heatGain += increment;
		}
		
		 */
		reactorPool.free(reactor);
		//evalCache.put(raw.hashCode(), reward);
		
		return reward;
	}
	
	public double eval(double[] arr) {
		RawBasicNeuralNet net = neuralPool.obtain();
		net.raw = arr;
		double d = eval(net);
		neuralPool.free(net);
		return d;
	}
	
	public void prepare(int populationSize) {
		System.out.println("Preparing Engine");
		System.out.println("Population Size: " + populationSize);
		System.out.println("Knob : " + knob);
		System.out.println("Range Knob: " + maxRange + " - " + -maxRange);
		engine = Engine.builder(this::eval, genePool::obtain)//
				.populationSize(populationSize)//mfw
				.optimize(optimize)//higher better or lower better
				.alterers(new Mutator<>(0.8), new MeanAlterer<>(0.6))//assad
				.executor(executor)//should use gpu
				.build();
		
	}

	public void execute() {
		t = new Time(TimeUnit.MILLISECONDS);
		//RandomRegistry.with(new Random(123), r ->
		long count = engine.stream()
				//.limit(Limits.bySteadyFitness(14))
				//.limit(this::steadyFitness)//assad
				//.limit(Limits.bySteadyFitness(100))//assad
				.limit(Limits.byExecutionTime(Duration.ofSeconds(120)))//assad
				//.filter(this::theBest)//
				//.filter(XORTestJenetic::evolutionNews)
				//.peek(stat)//assad
				.peek(assad -> {//collector
					if (best == null) best = assad.bestPhenotype().genotype();
					if (worst == null) worst = assad.worstPhenotype().genotype();
					double bestScore = eval(assad.bestPhenotype().genotype()), worstScore = eval(assad.worstPhenotype().genotype());
					double topBestScore = eval(best), topWorstScore = eval(worst);
					if (optimize.compare(topBestScore, bestScore) == -1) best = assad.bestPhenotype().genotype();
					if (optimize.compare(topWorstScore, worstScore) == 1) worst = assad.worstPhenotype().genotype();
					bestEval.add(assad.generation(), Optimize.MAXIMUM == optimize ? Math.max(topBestScore, bestScore) : Math.min(topBestScore, bestScore));
					worstEval.add(assad.generation(), Optimize.MAXIMUM == optimize ? Math.min(topWorstScore, worstScore) : Math.max(topWorstScore, worstScore));
					eval.add(assad.generation(), (bestScore + worstScore) / 2F);
					try {
						chart.repaint();
					}catch (Exception ignored) {}
					;
				}).count();
		//)
		//wtf
		System.out.println("Total Population: " + count);
	}
	
	public void post() throws IOException {
		System.out.println();
		
		RawBasicNeuralNet bestNet = new RawBasicNeuralNet(best, structure), worstNet = new RawBasicNeuralNet(worst, structure);
		/*
		System.out.println("Best fitness: " + best.bestPhenotype().fitness());
		System.out.println("Worst fitness: " + worst.bestPhenotype().fitness());
		System.out.println("Best according to practical: " + eval(bestNet));
		System.out.println("Worst according to practical: " + eval(worstNet));
		System.out.println("Population Left After Genocide: " + list.size());
	
		
		
		 */
		System.out.println("Took: " + t.elapsedS());
		System.out.println("Gene Created: " + genePool.getPeak());
		System.out.println("Reactor Created: " + reactorPool.getPeak());
		worstJsonModel.delete();
		bestJsonModel.delete();
		lastPopulation.delete();
		Files.writeString(bestJsonModel.toPath(), gson.toJson(bestNet), StandardOpenOption.WRITE, StandardOpenOption.CREATE);
		Files.writeString(worstJsonModel.toPath(), gson.toJson(worstNet), StandardOpenOption.WRITE, StandardOpenOption.CREATE);
		System.out.println("Note: " + (optimize == Optimize.MAXIMUM ? "higher" : "lower") + " better");
		System.out.println();
		System.out.println("Stat for nerd:");
		System.out.println(stat);
		System.out.println("Note: " + (optimize == Optimize.MAXIMUM ? "higher" : "lower") + " better");
		Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
		for (Thread assad : threadSet)
			if (!assad.isDaemon())
				System.out.println(assad.getId() + ". " + assad.getName() + " alive ? " + assad.isAlive());
	}
	
	public boolean steadyFitness(EvolutionResult<DoubleGene, Double> evolutionResult) {
		if (timerThreadLocal.get() == null) return false;
		double eval = eval(evolutionResult.bestPhenotype().genotype());
		boolean best = optimize.compare(top1, eval) == -1 || top1 == 0;
		
		if (best) timerThreadLocal.get().reset();
		if (timerThreadLocal.get().get()) timerThreadLocal.set(null);
		return true;
	}
	
	public boolean theBest(EvolutionResult<DoubleGene, Double> evolutionResult) {
		double eval = eval(evolutionResult.bestPhenotype().genotype());
		boolean sample = (evolutionResult.generation() / sampleEvery) == sampleEvery;
		boolean best = optimize.compare(top1, eval) == -1 || top1 == 0;
		if (best || sample) {
			if (best) {
				top1 = eval;
			}
			/*
			System.out.println();
			System.out.println("Generation: " + evolutionResult.generation());
			System.out.println(assad + ": " + evolutionResult.bestPhenotype().fitness());
			System.out.println("Eval: " + eval);
			
			 */
			//timerThreadLocal.get().reset();
			return best;
		}
		//if (timerThreadLocal.get().get()) timerThreadLocal.set(null);
		return false;
	}

	public double eval(Genotype<DoubleGene> genotype) {
		RawBasicNeuralNet net = neuralPool.obtain();
		net.assignRaw(genotype);
		double d = eval(net);
		neuralPool.free(net);
		return d;
	}
}
