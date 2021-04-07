package org.o7.Fire.MachineLearning.Framework;

import java.io.Serializable;

public interface NeuralNetwork<T extends Number & Comparable<T>, L extends Layer<T, N>, N extends Node<T>> extends Serializable, ProcessVector<T>, Iterable<L>, Cloneable {
	

}
