package org.o7.Fire.MachineLearning.Primtive;

import java.io.Serializable;

public interface Layer extends Serializable {
	int getInputSize();
	
	int getOutputSize();
	
	NeuralFunction getActivation();
	
	double[] getBias();
	
	double[][] getWeight();
	
	void setActivation(NeuralFunction function);
	
	default double[] process(double[] array) {
		if (array.length != getInputSize())
			throw new IllegalArgumentException("Input size expected: " + getInputSize() + ", get: " + array.length);
		double[] output = new double[getOutputSize()];
		for (int i = 0; i < getOutputSize(); i++) {
			float node = 0;
			for (int j = 0; j < array.length; j++) {
				node += array[j] * getWeight()[i][j];
			}
			node += getBias()[i];
			output[i] = getActivation().process(node);
		}
		return output;
	}
}
