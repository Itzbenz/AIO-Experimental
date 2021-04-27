package org.o7.Fire.MachineLearning.Jenetic;

import Atom.File.SerializeData;
import Atom.Struct.PoolObject;
import Atom.Time.Time;
import Atom.Time.Timer;
import Atom.Utility.Pool;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.jenetics.DoubleChromosome;
import io.jenetics.DoubleGene;
import io.jenetics.Genotype;
import io.jenetics.Optimize;
import io.jenetics.engine.Engine;
import io.jenetics.engine.EvolutionResult;
import io.jenetics.engine.EvolutionStatistics;
import io.jenetics.engine.Limits;
import io.jenetics.stat.DoubleMomentStatistics;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.o7.Fire.MachineLearning.Framework.ArrayListCapped;
import org.o7.Fire.MachineLearning.Framework.Chart;
import org.o7.Fire.MachineLearning.Framework.Reactor;
import org.o7.Fire.MachineLearning.Primtive.NeuralFunction;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class ReactorTestJenetic {
	public static final PoolObject<Reactor> reactorPool = new PoolObject<Reactor>() {
		@Override
		protected Reactor newObject() {
			return new Reactor();
		}
	};
	public File bestJsonModel = new File(this.getClass().getSimpleName() + "-NeuralNetwork-Best.json"), worstJsonModel = new File(this.getClass().getSimpleName() + "-NeuralNetwork-Worst.json"), lastPopulation = new File(this.getClass().getSimpleName() + "-Jenetic-Population.obj");
	public Gson gson = new GsonBuilder().setPrettyPrinting().create();
	public int[] structure = new int[]{5, 4, 5, 3, 2};//prob gonna do genetic for this too
	public float maxRange = 100;
	public int knob = RawNeuralNet.needRaw(new Reactor().factor().length, structure);
	public static Map<Integer, Double> evalCache = Collections.synchronizedMap(new HashMap<>());
	public final XYSeries evalScore = new XYSeries("Eval Score"), fitnessScore = new XYSeries("Fitness");
	public List<EvolutionResult<DoubleGene, Double>> list;
	public Engine<DoubleGene, Double> engine;
	public final PoolObject<Genotype<DoubleGene>> genePool = new PoolObject<>() {
		@Override
		protected Genotype<DoubleGene> newObject() {
			return Genotype.of(DoubleChromosome.of(-maxRange, maxRange, knob));
		}
	};
	public Executor executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() == 1 ? 1 : Runtime.getRuntime().availableProcessors() - 1, Pool.daemonFactory);
	public long sampleEvery = 100, duration = 10;
	public ThreadLocal<Timer> timerThreadLocal = ThreadLocal.withInitial(timerSupplier);
	public Optimize optimize = Optimize.MAXIMUM;
	public Supplier<Atom.Time.Timer> timerSupplier = () -> new Atom.Time.Timer(TimeUnit.SECONDS, duration);
	protected Time t = new Time();
	EvolutionStatistics<Double, DoubleMomentStatistics> stat = EvolutionStatistics.ofNumber();
	public double top1 = 0;
	
	public static void main(String[] args) throws IOException {
		ReactorTestJenetic jenetic = new ReactorTestJenetic();
		jenetic.prepare(10, 1000);
		jenetic.execute();
		jenetic.post();
	}
	
	public String assad = optimize == Optimize.MAXIMUM ? "Fitness" : "Loss";
	
	public static double eval(RawBasicNeuralNet raw) {
		if (evalCache.containsKey(raw.hashCode())) return evalCache.get(raw.hashCode());
		Reactor reactor = reactorPool.obtain();
		double reward = 1000f;
		int iteration = 20;
		for (int j = 0; j < iteration; j++) {
			double[] output = raw.process(reactor.factor());
			if (output[0] > 0.5f) reactor.raiseControlRod();
			if (output[1] > 0.5f) reactor.lowerControlRod();
			reactor.update();
			if (reactor.reactorFuckingExploded()) break;
		}
		reward = Math.min(reward, reactor.getPayout());
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
		evalCache.put(raw.hashCode(), reward);
		return reward;
	}
	
	public double eval(double[] arr) {
		return eval(new RawBasicNeuralNet(arr, structure));
	}
	
	public void prepare(int maxCollectedPopulation, int populationSize) {
		System.out.println("Preparing Engine");
		System.out.println("Max collected population: " + maxCollectedPopulation);
		System.out.println("Population Size: " + populationSize);
		list = Collections.synchronizedList(new ArrayListCapped<>(maxCollectedPopulation) {
			@Override
			public void trim() {
				super.trim();
			}
		});
		engine = Engine.builder(this::eval, genePool::obtain)//
				.populationSize(populationSize)//mfw
				.optimize(optimize)//higher better or lower better
				//.alterers(new Mutator<>(0.63), new MeanAlterer<>(0.6))//assad
				.executor(executor)//should use gpu
				.build();
		
	}
	
	public void execute() {
		t = new Time(TimeUnit.MILLISECONDS);
		//RandomRegistry.with(new Random(123), r ->
		engine.stream()
				//.limit(Limits.bySteadyFitness(14))
				.limit(this::steadyFitness)//assad
				//.limit(Limits.bySteadyFitness(100))//assad
				.limit(Limits.byExecutionTime(Duration.ofSeconds(duration * 2)))//assad
				.filter(this::theBest)//
				//.filter(XORTestJenetic::evolutionNews)
				.peek(stat)//assad
				.collect(Collectors.toCollection(() -> list));//assad
		//)
		//wtf
	}
	
	public void post() throws IOException {
		System.out.println();
		EvolutionResult<DoubleGene, Double> pop0 = list.get(0), pop100 = list.get(list.size() - 1);
		EvolutionResult<DoubleGene, Double> best = (optimize == Optimize.MAXIMUM ? pop100 : pop0);
		EvolutionResult<DoubleGene, Double> worst = (optimize == Optimize.MAXIMUM ? pop0 : pop100);
		RawBasicNeuralNet bestNet = new RawBasicNeuralNet(best.bestPhenotype().genotype(), structure), worstNet = new RawBasicNeuralNet(worst.bestPhenotype().genotype(), structure);
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
		SerializeData.dataOut(best.population(), lastPopulation);
		System.out.println("Note: " + (optimize == Optimize.MAXIMUM ? "higher" : "lower") + " better");
		System.out.println();
		System.out.println("Stat for nerd:");
		System.out.println(stat);
		System.out.println("Note: " + (optimize == Optimize.MAXIMUM ? "higher" : "lower") + " better");
		
		Chart chart = new Chart(assad + " Overtime", "Generation", assad);
		XYSeriesCollection collection = new XYSeriesCollection(evalScore);
		collection.addSeries(fitnessScore);
		chart.setDataset(collection);
		chart.initUI();
		chart.setVisible(true);
		
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
		fitnessScore.add(evolutionResult.generation(), evolutionResult.bestPhenotype().fitness());
		evalScore.add(evolutionResult.generation(), eval);
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
		RawBasicNeuralNet net = new RawBasicNeuralNet(genotype, structure).setFunction(NeuralFunction.Tanh);
		assert net.size() == knob : "Net size is: " + net.size();
		return eval(net);
	}
}
