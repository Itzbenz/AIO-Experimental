package org.o7.Fire.MachineLearning.Java;

import org.o7.Fire.MachineLearning.Framework.Node;

//not actually used
public class NodeLayer implements Node<Float> {
	protected int index;
	protected VectorLayer layer;
	
	public NodeLayer(VectorLayer vectorLayer, int index) {
		this.layer = vectorLayer;
		this.index = index;
	}
	
	@Override
	public Float[] weight() {
		return layer.weight[index];
	}
	
	@Override
	public Float activation(Float input) {
		return layer.activation.process(input).floatValue();
	}
	
	@Override
	public void weight(int i, Float aFloat) {
		layer.weight[index][i] = aFloat;
	}
	
	@Override
	public Float bias() {
		return layer.getBias(index);
	}
	
	@Override
	public void bias(Float aFloat) {
		layer.bias[index] = aFloat;
	}
	
	//Fallback
	@Override
	public Float process(Float[] raw) {
		float proces = 0;
		for (int i = 0; i < raw.length; i++) {
			proces += raw[i] * weight(i);
		}
		proces += bias();
		return activation(proces);
	}
}
