package org.o7.Fire.MachineLearning.Primtive;

public class SimpleLayer implements Layer {
	int inputSize, outputSize;
	NeuralFunction activation;
	double[] bias;
	double[][] weight;
	
	public SimpleLayer(Layer previous, int outputSize, NeuralFunction activation) {
		this(previous.getOutputSize(), outputSize, activation);
	}
	
	public SimpleLayer(Layer previous, int outputSize) {
		this(previous, outputSize, previous.getActivation());
	}
	
	public SimpleLayer(int inputSize, int outputSize) {
		this(inputSize, outputSize, NeuralFunction.Identity);
	}
	
	public SimpleLayer(int inputSize, int outputSize, NeuralFunction activation) {
		this.inputSize = inputSize;
		this.outputSize = outputSize;
		this.activation = activation;
		bias = new double[outputSize];
		weight = new double[outputSize][inputSize];
		NeuralFunction.assignRandom(bias);
		for (double[] d : weight)
			NeuralFunction.assignRandom(d);
	}
	
	
	@Override
	public int getInputSize() {
		return inputSize;
	}
	
	@Override
	public int getOutputSize() {
		return outputSize;
	}
	
	@Override
	public NeuralFunction getActivation() {
		return activation;
	}
	
	@Override
	public void setActivation(NeuralFunction function) {
		activation = function;
	}
	
	@Override
	public double[] getBias() {
		return bias;
	}
	
	@Override
	public double[][] getWeight() {
		return weight;
	}
	
	
}
