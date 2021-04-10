package org.o7.Fire.MachineLearning.Primtive;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;

public class ComparableSimpleNeuralNet extends SimpleNeuralNet implements Comparable<ComparableSimpleNeuralNet>, Serializable {
	protected transient HashMap<Integer, Double> last = new HashMap<>();
	
	public ComparableSimpleNeuralNet() {
	
	}
	
	public ComparableSimpleNeuralNet(int input, int output, LayerFactory<SimpleLayer> factory) {
		super(input, output, factory);
	}
	
	@Override
	public double error(double[] input, double[] expected) {
		double err = super.error(input, expected);
		last.put(Arrays.hashCode(input), err);
		return err;
	}
	
	public double lastError() {
		double err = 0;
		for (Double d : last.values())
			err += d;
		return err;
	}
	
	@Override
	public int compareTo(ComparableSimpleNeuralNet o) {
		if (o == null) return 0;
		return Double.compare(lastError(), o.lastError());
	}
	
	
	public ComparableSimpleNeuralNet clone() throws CloneNotSupportedException {
		ComparableSimpleNeuralNet c = (ComparableSimpleNeuralNet) super.clone();
		if (c.last == null) c.last = new HashMap<>();
		c.last.clear();
		return c;
	}
}
