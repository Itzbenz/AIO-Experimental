package org.o7.Fire.MachineLearning.Framework;

import java.io.Serializable;
import java.util.function.Function;

public enum ActivationFunction implements Serializable {
	Identity(ActivationFunction::Identity), Relu(ActivationFunction::Relu), Tanh(ActivationFunction::Tanh), Binary(ActivationFunction::Binary), Sigmoid(ActivationFunction::Sigmoid);
	
	transient Function<Number, Number> function;
	
	ActivationFunction(Function<Number, Number> f) {
		function = f;
	}
	
	public static Number Sigmoid(Number val) {
		return 1.0 / (1.0 + Math.exp(-1.0 * val.doubleValue()));
	}
	
	public static Number Binary(Number number) {
		return number.intValue() > 0 ? 1 : 0;
	}
	
	public static Number Identity(Number f) {
		return f;
	}
	
	public static Number Relu(Number val) {
		return Math.min(Math.max(0, val.doubleValue()), 100);
	}
	
	public static Number Tanh(Number val) {
		return (2.0 / (1 + Math.pow(Math.E, -2 * val.doubleValue() / 10.0))) - 1;
	}
	
	public static Number Cost(Number expected, Number output) {
		return Math.pow((double) 1 / 2 * (expected.doubleValue() - output.doubleValue()), 2);
	}
	
	public Function<Number, Number> getFunction() {
		return function;
	}
	
	public Number process(Number f) {
		return function.apply(f);
	}
}
