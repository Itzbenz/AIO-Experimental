package org.o7.Fire.MachineLearning.Jenetic;

import Atom.File.SerializeData;
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
	public File bestJsonModel = new File(this.getClass().getSimpleName() + "-NeuralNetwork-Best.json"), worstJsonModel = new File(this.getClass().getSimpleName() + "-NeuralNetwork-Worst.json"), lastPopulation = new File(this.getClass().getSimpleName() + "-Jenetic-Population.obj");
	public Gson gson = new GsonBuilder().setPrettyPrinting().create();
	public int[] structure = new int[]{5, 4, 5, 3, 2};//prob gonna do genetic for this too
	public float maxRange = 100;
	public int knob = RawNeuralNet.needRaw(new Reactor().factor().length, structure);
	public List<EvolutionResult<DoubleGene, Double>> list;
	public Engine<DoubleGene, Double> engine;
	public Executor executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() / 2, Pool.daemonFactory);
	public long sampleEvery = 100;
	public Supplier<Atom.Time.Timer> timerSupplier = () -> new Atom.Time.Timer(TimeUnit.SECONDS, 10);
	public ThreadLocal<Timer> timerThreadLocal = ThreadLocal.withInitial(timerSupplier);
	public Optimize optimize = Optimize.MAXIMUM;
	public Phenotype<DoubleGene, Double> top1 = null;
	protected Time t = new Time();
	EvolutionStatistics<Double, DoubleMomentStatistics> stat = EvolutionStatistics.ofNumber();
	
	public static void main(String[] args) throws IOException {
		ReactorTestJenetic jenetic = new ReactorTestJenetic();
		jenetic.prepare(10, 1000);
		jenetic.execute();
		jenetic.post();
	}
	
	public void prepare(int maxCollectedPopulation, int populationSize) {
		System.out.println("1. Making random population");
		System.out.println("2. Test population");
		System.out.println("3. Euthanasia/Modify unfit population");
		System.out.println("4. Back to 1");
		list = Collections.synchronizedList(new ArrayList<>() {
			@Override
			public boolean add(EvolutionResult<DoubleGene, Double> evolutionResult) {
				boolean b = super.add(evolutionResult);
				trim();
				return b;
			}
			
			private void trim() {
				if (size() > maxCollectedPopulation) {
					sort(Comparator.comparing(o -> o.bestPhenotype().fitness()));
					subList(maxCollectedPopulation, size() - 1).clear();
				}
			}
			
			@Override
			public boolean addAll(Collection<? extends EvolutionResult<DoubleGene, Double>> c) {
				boolean b = super.addAll(c);
				trim();
				return b;
			}
		});
		engine = Engine.builder(this::eval, Codecs.ofVector(DoubleRange.of(-maxRange, maxRange), knob))//
				.populationSize(populationSize).optimize(optimize)
				//.alterers(new Mutator<>(0.03), new MeanAlterer<>(0.6))//assad
				.executor(executor).build();
	}
	
	public double eval(RawBasicNeuralNet raw) {
		Reactor reactor = new Reactor();
		double reward = 1000f;
		//if(Random.getBool()) reactor.reset();
		for (int j = 0; j < 100; j++) {
			for (int i = 0; i < 100; i++) {
				double[] output = raw.process(reactor.factor());
				if (output[0] > 0.5f) reactor.raiseControlRod();
				if (output[1] > 0.5f) reactor.lowerControlRod();
				reactor.update();
				if (reactor.reactorFuckingExploded()) break;
			}
			reward = Math.min(reward, reactor.getPayout());
			if (reactor.reactorFuckingExploded()) break;
			reactor.reset();
		}
		
		
		return reward;
	}
	
	public double eval(double[] arr) {
		return eval(new RawBasicNeuralNet(arr, structure));
	}
	
	public void execute() {
		t = new Time(TimeUnit.MILLISECONDS);
		//RandomRegistry.with(new Random(123), r ->
		engine.stream()
				//.limit(Limits.bySteadyFitness(14))
				//.limit(this::timeOut)//assad
				.limit(Limits.bySteadyFitness(14)).limit(Limits.byExecutionTime(Duration.ofSeconds(60)))//assad
				//.filter(XORTestJenetic::evolutionNews)
				.peek(stat)//assad
				.peek(this::timeOut).sorted(Comparator.comparing(o -> o.bestPhenotype().fitness()))//assad
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
		System.out.println("Best fitness: " + best.bestPhenotype().fitness());
		System.out.println("Worst fitness: " + worst.bestPhenotype().fitness());
		System.out.println("Best according to practical: " + eval(bestNet));
		System.out.println("Worst according to practical: " + eval(worstNet));
		System.out.println("Population Left After Genocide: " + list.size());
		System.out.println("Took: " + t.elapsedS());
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
		
		
		Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
		for (Thread assad : threadSet)
			if (!assad.isDaemon())
				System.out.println(assad.getId() + ". " + assad.getName() + " alive ? " + assad.isAlive());
	}
	
	public boolean timeOut(EvolutionResult<DoubleGene, Double> evolutionResult) {
		//if (timerThreadLocal.get() == null) return false;
		boolean sample = (evolutionResult.generation() / sampleEvery) == sampleEvery;
		if (top1 == null || top1.fitness().compareTo(evolutionResult.bestPhenotype().fitness()) > 0 || sample) {
			top1 = evolutionResult.bestPhenotype();
			System.out.println();
			System.out.println("Generation: " + evolutionResult.generation());
			System.out.println((optimize == Optimize.MAXIMUM ? "Fitness" : "Loss") + ": " + evolutionResult.bestPhenotype().fitness());
			timerThreadLocal.get().reset();
		}
		//if (timerThreadLocal.get().get()) timerThreadLocal.set(null);
		return true;
	}
	
	public double eval(Genotype<DoubleGene> genotype) {
		RawBasicNeuralNet net = new RawBasicNeuralNet(genotype, structure).setFunction(NeuralFunction.Tanh);
		return eval(net);
	}
}
