package org.o7.Fire.MachineLearning.PolicyMaker;

import Atom.File.FileUtility;
import Atom.Struct.PoolObject;
import Atom.Time.Timer;
import com.google.gson.Gson;
import io.jenetics.Optimize;
import io.jenetics.Phenotype;
import io.jenetics.engine.Engine;
import io.jenetics.engine.EvolutionResult;
import io.jenetics.engine.Limits;
import org.jfree.data.xy.XYSeries;
import org.o7.Fire.Experimental.Webhook;
import org.o7.Fire.Framework.Array.ArrayListCapped;
import org.o7.Fire.Framework.XYRealtimeChart;
import org.o7.Fire.MachineLearning.Framework.ByteGene;
import org.o7.Fire.MachineLearning.Framework.Reactor;

import java.awt.*;
import java.io.File;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public class PolicyJenetic implements Serializable {
    public static final Gson gson = new Gson().newBuilder().setPrettyPrinting().create();
    public long trainCount;
    protected transient PolicyPool policyPool;
    protected byte[] currentPolicy;
    protected transient PolicyEvaluator evaluator;
    protected int maxStateSize;
    protected transient ByteGene.GenotypeFactory genotypeFactory;
    public static final PoolObject<Reactor> reactorPool = new PoolObject<>() {
        @Override
        protected Reactor newObject() {
            return new Reactor();
        }
    };
    protected transient XYSeries fitnessChart = null;
    protected transient XYRealtimeChart chart;
    ;
    protected transient ArrayListCapped<Phenotype<ByteGene, Double>> resultTraining = new ArrayListCapped<>(100) {
        @Override
        public void trim() {
            List<Phenotype<ByteGene, Double>> l = subList(max, size() - 1);
            for (Phenotype<ByteGene, Double> s : l) genotypeFactory.free(s.genotype());
            l.clear();
        }
    };
    protected transient Timer logTimer = new Timer(TimeUnit.SECONDS, 2);
    
    public PolicyJenetic() {}
    
    public PolicyJenetic(int maxStateSize) {
        this(maxStateSize, null);
        evaluator = this::eval;
    }
    
    public PolicyJenetic(int maxStateSize, PolicyEvaluator evaluator) {
        policyPool = new PolicyPool(maxStateSize);
        currentPolicy = policyPool.obtain();
        this.evaluator = evaluator;
        this.maxStateSize = maxStateSize;
        this.genotypeFactory = new ByteGene.GenotypeFactory(maxStateSize);
    }
    
    public static void main(String[] args) {
        Reactor reactor = new Reactor();
        reactor.state();
        PolicyJenetic policyJenetic = new PolicyJenetic(reactor.maxState());
        if (!GraphicsEnvironment.isHeadless()){
            policyJenetic.enableGraph();
        }else{
            Webhook.hook();
        }
        policyJenetic.train();
        FileUtility.write(new File(policyJenetic.getClass().getCanonicalName() + ".json"), gson.toJson(policyJenetic).getBytes(StandardCharsets.UTF_8));
    }
    
    public void enableGraph() {
        chart = new XYRealtimeChart("Policy Jenetic", "Generation", "Value");
        fitnessChart = chart.getSeries("Fitness");
        fitnessChart.setMaximumItemCount(200);
        chart.setVisible(true);
    }
    
    private double eval(Function<Integer, Byte> integerByteFunction) {
        Reactor reactor = reactorPool.obtain();
        
        for (int i = 0; i < 20; i++) {
            byte b = integerByteFunction.apply(reactor.state());
            boolean none = b > -100 && b < 100, up = b > 0;
            if (!none){
                if (up) reactor.raiseControlRod();
                else reactor.lowerControlRod();
            }
            reactor.update();
            if (reactor.reactorFuckingExploded()) break;
        }
        return reactor.getPayout();
    }
    
    public void train() {
        train(new Engine.Builder<>(evaluator, genotypeFactory)//
                .optimize(Optimize.MAXIMUM)//
                .executor(Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() - 1))//
                .populationSize(20)//
                .build());
    }
    
    public void handleResult(EvolutionResult<ByteGene, Double> g) {
        Phenotype<ByteGene, Double> best = g.bestPhenotype();
        for (Phenotype<ByteGene, Double> pop : g.population()) {
            if (best == pop) continue;
            genotypeFactory.free(pop.genotype());
        }
    
        resultTraining.add(best);
        resultTraining.sort((o1, o2) -> o2.fitness().compareTo(o1.fitness()));
        if (logTimer.get()){
            System.out.println();
            System.out.printf("Generation: %s, Score: %s", g.generation(), g.bestFitness());
            System.out.println();
            System.out.printf("Top 1: %s", resultTraining.get(0).fitness());
            System.out.println();
            System.out.printf("Last: %s", resultTraining.get(resultTraining.size() - 1).fitness());
            System.out.println();
        }
        if (fitnessChart != null){
            fitnessChart.add(g.generation(), g.bestFitness());
            chart.repaint();
        }
    }
    
    public void train(Engine<ByteGene, Double> engine) {
        trainCount += engine.stream()//
                .limit(Limits.byExecutionTime(Duration.ofMinutes(2))).peek(this::handleResult).count();
        System.out.println();
        System.out.println("Training Finished");
        System.out.println("Count: " + trainCount);
        for (int i = 0; i < resultTraining.get(0).genotype().chromosome().length(); i++) {
            currentPolicy[i] = resultTraining.get(0).genotype().chromosome().get(i).allele();
        }
    }
    
    public byte getAction(int state) {
        return currentPolicy[state];
    }
}
