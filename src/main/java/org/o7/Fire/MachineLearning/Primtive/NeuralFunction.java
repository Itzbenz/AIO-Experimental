package org.o7.Fire.MachineLearning.Primtive;

import Atom.Utility.Random;

import java.util.function.Function;

import static Atom.Utility.Random.getBool;
import static Atom.Utility.Random.getDouble;

public enum NeuralFunction {
	Identity(NeuralFunction::Identity), Relu(NeuralFunction::Relu), Tanh(NeuralFunction::Thanh), Binary(NeuralFunction::Binary), Sigmoid(NeuralFunction::Sigmoid), Normalize(NeuralFunction::normalize);
	
	transient Function<Double, Double> function;
	
	NeuralFunction(Function<Double, Double> f) {
		function = f;
	}
	
	public static double Sigmoid(double val) {
		return 1.0 / (1.0 + Math.exp(-1.0 * val));
	}
	
	public static double Binary(double val) {
		return val > 0 ? 1 : 0;
	}
	
	public static double Identity(double val) {
		return val;
	}
	
	public static double Relu(double val) {
		return Math.min(Math.max(0, val), 100);
	}
	
	public static double Thanh(double val) {
		return (2.0 / (1 + Math.pow(Math.E, -2 * val / 10.0))) - 1;
	}
	
	public static double normalize(double val) {
		if (val > 1) return 1;
		if (val < -1) return 0;
		return val;
	}
	
	public static double Cost(double expected, double output) {
		return Math.pow((double) 1 / 2 * (expected - output), 2);
	}
	
	public static double loss(double[] output, double[] expected) {
		double d = 0;
		for (int i = 0; i < output.length; i++) {
			d += Math.abs(output[i] - expected[i]);
		}
		return d;
	}
	
	public double process(double f) {
		return function.apply(f);
	}
	
	public static void assignRandom(int[] d, int min, int max) {
		for (int i = 0; i < d.length; i++) {
			d[i] = Random.getInt(min, max);
		}
	}
	
	public static void assignRandom(double[] d) {
		for (int i = 0; i < d.length; i++) {
			double r = getDouble();
			d[i] = d[i] + (getBool() ? r : -r);
		}
	}
}
