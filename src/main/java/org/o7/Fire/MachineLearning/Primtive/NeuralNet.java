package org.o7.Fire.MachineLearning.Primtive;

public interface NeuralNet {
	
	double[] process(double[] input);
	
	default double error(double[] input, double[] expected) {
		double[] output = process(input);
		return NeuralFunction.loss(output, expected);
	}
	
}
