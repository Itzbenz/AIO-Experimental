package org.o7.Fire.MachineLearning.Primtive;

public interface NeuralNet<L extends Layer> {
	
	double[] process(double[] input);
	
	void add(L produce);
	
	default double error(double[] input, double[] expected) {
		double[] output = process(input);
		return NeuralFunction.loss(output, expected);
	}
	
}
