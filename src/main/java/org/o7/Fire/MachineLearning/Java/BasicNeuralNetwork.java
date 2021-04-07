package org.o7.Fire.MachineLearning.Java;

import org.o7.Fire.MachineLearning.Framework.Layer;
import org.o7.Fire.MachineLearning.Framework.NeuralNetwork;
import org.o7.Fire.MachineLearning.Framework.Node;

import java.util.ArrayList;

public class BasicNeuralNetwork<L extends Layer<Float, N>, N extends Node<Float>> extends ArrayList<L> implements NeuralNetwork<Float, L, N> {
	@Override
	public int getInputSize() {
		return get(0).getInputSize();
	}
	
	@Override
	public int getOutputSize() {
		return get(size() - 1).getOutputSize();
	}
	
	public String visualizeString() {
		StringBuilder sb = new StringBuilder();
		sb.append("#".repeat(getInputSize()));
		for (Layer<Float, N> l : this) {
			sb.append(System.lineSeparator());
			int output = Math.max(0, l.getOutputSize());
			int input = Math.max(0, l.getInputSize());
			int direct = Math.min(input, output);
			int extension = Math.max(0, Math.abs(output - input));
			String ext = (input > output) ? "/" : "\\";
			sb.append("|".repeat(direct));
			sb.append(ext.repeat(extension));
			sb.append(System.lineSeparator());
			sb.append("#".repeat(output));
		}
		return sb.toString();
	}
	
	public double[] error(float[] input, float[] expected) {
		if (input.length != getInputSize()) throw new IllegalArgumentException("Input size not same");
		if (expected.length != getOutputSize()) throw new IllegalArgumentException("Output size not same");
		
		Float[] fuckingJava = new Float[input.length];
		for (int i = 0; i < input.length; i++) {
			fuckingJava[i] = input[i];
		}
		
		Float[] output = process(fuckingJava);
		
		double[] error = new double[output.length];
		for (int i = 0; i < output.length; i++) {
			error[i] = Math.abs((double) output[i] - expected[i]);
		}
		return error;
	}
	
	public void train(float[] input, float[] expected) {
	
	}
	
	@Override
	public Float[] process(Float[] array) {
		Float[] output = array;
		for (Layer<Float, N> p : this) {
			output = p.process(output);
		}
		return output;
	}
	
	
}
