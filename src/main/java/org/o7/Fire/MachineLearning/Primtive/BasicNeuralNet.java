package org.o7.Fire.MachineLearning.Primtive;

import Atom.Utility.Random;
import arc.util.Nullable;

import java.io.Serializable;
import java.util.ArrayList;

public class BasicNeuralNet<L extends Layer> extends ArrayList<L> implements NeuralNet, Serializable {
	protected @Nullable
	LayerFactory<L> factory;
	
	public BasicNeuralNet() {
	
	}
	
	public BasicNeuralNet(int input, int output, LayerFactory<L> factory) {
		add(factory.produce(input, output));
		this.factory = factory;
	}
	
	public void setFactory(LayerFactory<L> factory) {
		this.factory = factory;
		
	}
	
	public void add(int output) {
		add(output, (NeuralFunction) null);
	}
	
	public void add(int output, NeuralFunction function) {
		if (factory == null) throw new IllegalStateException("No factory");
		L l = factory.produce(getOutputSize(), output);
		if (function != null) l.setActivation(function);
		add(l);
	}
	
	public int getInputSize() {
		return size() < 1 ? 0 : get(0).getInputSize();
	}
	
	public int getOutputSize() {
		return size() < 1 ? 0 : get(size() - 1).getOutputSize();
	}
	
	public void addRandomLayer(int howMuch, int min, int max, NeuralFunction function) {
		for (int i = 0; i < howMuch; i++) {
			add(Random.getInt(min, max), function);
		}
	}
	
	@Override
	public double[] process(double[] input) {
		for (Layer p : this) {
			input = p.process(input);
		}
		return input;
	}
	
	public String visualizeString() {
		StringBuilder sb = new StringBuilder();
		sb.append("#".repeat(getInputSize())).append(" ").append(getInputSize());
		for (Layer l : this) {
			sb.append(System.lineSeparator());
			int output = Math.max(0, l.getOutputSize());
			int input = Math.max(0, l.getInputSize());
			int direct = Math.min(input, output);
			int extension = Math.max(0, Math.abs(output - input));
			String ext = (input > output) ? "/" : "\\";
			sb.append("|".repeat(direct));
			sb.append(ext.repeat(extension));
			sb.append("(").append("Activation:").append(l.getActivation().name()).append(")");
			sb.append(System.lineSeparator());
			sb.append("#".repeat(output)).append(" ").append(output);
		}
		return sb.toString();
	}
	
	public void update() {
		for (Layer l : this) {
			NeuralFunction.assignRandom(l.getBias());
			double[][] weight = l.getWeight();
			for (double[] f : weight) {
				NeuralFunction.assignRandom(f);
			}
		}
	}
}
