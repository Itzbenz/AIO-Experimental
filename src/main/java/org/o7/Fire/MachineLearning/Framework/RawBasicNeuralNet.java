package org.o7.Fire.MachineLearning.Framework;

import io.jenetics.DoubleGene;
import io.jenetics.Genotype;
import org.o7.Fire.MachineLearning.Primtive.NeuralFunction;

import java.util.Arrays;

public class RawBasicNeuralNet implements RawNeuralNet {
	public double[] raw;
	public int[] output;
	
	public RawBasicNeuralNet(Genotype<DoubleGene> raw, int[] output) {
		double[] gen = new double[raw.chromosome().length()];
		for (int i = 0; i < gen.length; i++) {
			gen[i] = raw.chromosome().get(i).doubleValue();
		}
		this.raw = gen;
		this.output = output;
	}
	
	protected NeuralFunction function = NeuralFunction.Identity;
	
	public RawBasicNeuralNet(double[] raw, int[] structure) {
		this.raw = raw;
		this.output = structure;
	}
	
	public RawBasicNeuralNet setFunction(NeuralFunction function) {
		this.function = function;
		return this;
	}
	
	@Override
	public double activation(double d) {
		return function.process(d);
	}
	
	@Override
	public int size() {
		return output.length;
	}
	
	@Override
	public int getOutput(int index) {
		return output[index];
	}
	
	@Override
	public double getRaw(int index) {
		return raw[index];
	}
	
	protected int hashCode = 0;
	
	@Override
	public int hashCode() {
		if (hashCode != 0) return hashCode;
		return hashCode = Arrays.hashCode(raw);
	}
}
