package org.o7.Fire.MachineLearning.Framework;

import java.io.Serializable;

public interface Node<T extends Number> extends Serializable, Cloneable {
	T[] weight();
	
	void weight(int i, T t);
	
	default T weight(int index) {
		return weight()[index];
	}
	
	T bias();
	
	void bias(T t);
	
	default T activation(T input) {
		return input;
	}
	
	T process(T[] input);
}
