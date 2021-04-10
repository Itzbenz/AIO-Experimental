package org.o7.Fire.MachineLearning.Primtive;

public class SimpleNeuralNet extends BasicNeuralNet<SimpleLayer> implements Cloneable {
	public SimpleNeuralNet() {
		super();
	}
	
	public SimpleNeuralNet(int input, int output, LayerFactory<SimpleLayer> factory) {
		super(input, output, factory);
	}
	
	
}
