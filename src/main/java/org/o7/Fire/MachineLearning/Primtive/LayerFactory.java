package org.o7.Fire.MachineLearning.Primtive;

@FunctionalInterface
public interface LayerFactory<L extends Layer> {
	L produce(int input, int output);
}
