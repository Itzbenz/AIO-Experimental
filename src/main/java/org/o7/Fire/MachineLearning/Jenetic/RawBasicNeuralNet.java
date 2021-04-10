package org.o7.Fire.MachineLearning.Jenetic;

import io.jenetics.DoubleGene;
import io.jenetics.Genotype;
import org.o7.Fire.MachineLearning.Primtive.NeuralFunction;

public class RawBasicNeuralNet implements RawNeuralNet {
	public final double[] raw;
	public final int[] output;
	
	public RawBasicNeuralNet(Genotype<DoubleGene> raw, int[] output) {
		double[] gen = new double[raw.chromosome().length()];
		for (int i = 0; i < gen.length; i++) {
			gen[i] = raw.chromosome().get(i).doubleValue();
		}
		this.raw = gen;
		this.output = output;
	}
	
	public RawBasicNeuralNet(double[] raw, int[] output) {
		this.raw = raw;
		this.output = output;
	}
	
	@Override
	public double activation(double d) {
		return NeuralFunction.Tanh(d);
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
	
}
