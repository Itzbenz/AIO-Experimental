package org.o7.Fire.MachineLearning.Framework;

import org.o7.Fire.MachineLearning.Primtive.NeuralFunction;

import java.io.Serializable;

//FUCKING RAW
public interface RawNeuralNet extends Serializable {
	
	static int needRaw(int input, int[] output) {
		int index = 0;
		for (int i : output) {//layer
			for (int i1 = 0; i1 < i; i1++) {//node
				for (int i2 = 0; i2 < input; i2++) {//input summation
					index++;//weight
				}
				index++;//bias
			}
			input = i;//previous output == new input
		}
		
		return index;
	}
	
	double activation(double d);
	
	default double[] process(double[] input) {
		final int[] index = new int[]{0};
		for (int i = 0; i < size(); i++) {
			int outputSize = getOutput(i);
			input = subProcess(input, outputSize, index);
		}
		return input;
	}
	
	//so slow
	default double[] subProcess(double[] array, int outputSize, int[] index) {
		double[] output = new double[outputSize];
		for (int i = 0; i < outputSize; i++) {//for node
			float node = 0;
			for (double v : array) {//node input summation
				node += v * getRaw(index[0]);//weight
				index[0] = index[0] + 1;
			}
			node += getRaw(index[0]);//bias
			index[0] = index[0] + 1;
			output[i] = activation(node);//activation
		}
		return output;
	}
	
	int size();
	
	int getOutput(int index);
	
	double getRaw(int index);
	
	default double error(double[] input, double[] expected) {
		double[] output = process(input);
		return NeuralFunction.loss(output, expected);
	}
}
