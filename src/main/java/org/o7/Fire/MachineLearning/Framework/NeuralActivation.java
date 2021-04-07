package org.o7.Fire.MachineLearning.Framework;

import java.io.Serializable;
import java.util.function.Function;

public enum NeuralActivation implements Serializable {
	Identity(NeuralActivation::Identity), Relu(NeuralActivation::Relu), Tanh(NeuralActivation::Tanh), Binary(NeuralActivation::Binary);
	
	transient Function<Number, Number> function;
	
	NeuralActivation(Function<Number, Number> f) {
		function = f;
	}
	
	private static Number Binary(Number number) {
		return number.intValue() > 0 ? 1 : 0;
	}
	
	public static Number Identity(Number f) {
		return f;
	}
	
	private static Number Relu(Number val) {
		return Math.min(Math.max(0, val.doubleValue()), 100);
	}
	
	private static Number Tanh(Number val) {
		return (2.0 / (1 + Math.pow(Math.E, -2 * val.doubleValue() / 10.0))) - 1;
	}
	
	public Function<Number, Number> getFunction() {
		return function;
	}
	
	public Number process(Number f) {
		return function.apply(f);
	}
}
