package org.o7.Fire.MachineLearning.PolicyMaker;

import io.jenetics.Genotype;
import io.jenetics.Phenotype;
import io.jenetics.engine.Evaluator;
import io.jenetics.util.ISeq;
import io.jenetics.util.Seq;
import org.o7.Fire.MachineLearning.Framework.ByteGene;

import java.util.function.Function;

@FunctionalInterface
public interface PolicyEvaluator extends Evaluator<ByteGene, Double> {
    double eval(Function<Integer, Byte> policy);
    
    default double eval(byte... bytes) {
        Function<Integer, Byte> function = i -> bytes[i];
        return eval(function);
    }
    
    @Override
    default ISeq<Phenotype<ByteGene, Double>> eval(Seq<Phenotype<ByteGene, Double>> population) {
        Phenotype<ByteGene, Double>[] phenotypes = new Phenotype[population.size()];
        for (int i = 0; i < population.size(); i++) {
            Phenotype<ByteGene, Double> s = population.get(i);
            phenotypes[i] = s.withFitness(eval(s.genotype()));
        }
        
        return ISeq.of(phenotypes);
    }
    
    default double eval(Genotype<ByteGene> genotype) {
        Function<Integer, Byte> function = i -> genotype.chromosome().get(i).allele();
        return eval(function);
    }
}
