package org.o7.Fire.MachineLearning.Java;

import Atom.Utility.Random;
import org.o7.Fire.MachineLearning.Framework.ActivationFunction;

import java.io.Serializable;

public class VectorNeuralNetwork extends BasicNeuralNetwork<VectorLayer, NodeLayer> implements Serializable {
	public static VectorNeuralNetwork random(int input, int maxNode, int minNode, int output, int layer) {
		return random(ActivationFunction.Identity, input, maxNode, minNode, output, layer);
	}
	
	public static VectorNeuralNetwork random(ActivationFunction activation, int input, int maxNode, int minNode, int output, int layer) {
		VectorNeuralNetwork basic = new VectorNeuralNetwork();
		addRandomLayer(activation, basic, input, maxNode, minNode, output, layer);
		return basic;
	}
	
	public static void addRandomLayer(ActivationFunction activation, VectorNeuralNetwork basic, int input, int maxNode, int minNode, int output, int layer) {
		int nInput = Random.getInt(minNode, maxNode);
		int nOutput = Random.getInt(minNode, maxNode);
		basic.add(new VectorLayer(input, nInput, activation));// 2, 6 ?
		for (int i = 0; i < layer - 2; i++) {
			if (i != 0) {
				nInput = nOutput;
				nOutput = Random.getInt(minNode, maxNode);
			}
			basic.add(new VectorLayer(nInput, nOutput, activation));
			
		}
		basic.add(new VectorLayer(nOutput, output, activation));
	}
	
	public void updateBias() {
		for (VectorLayer v : this)
			v.updateBias();
	}
	
	public void updateWeight() {
		for (VectorLayer v : this)
			v.updateWeight();
	}
	
	@Override
	public String visualizeString() {
		StringBuilder sb = new StringBuilder();
		sb.append("#".repeat(getInputSize())).append(" ").append(getInputSize());
		for (VectorLayer l : this) {
			sb.append(System.lineSeparator());
			int output = Math.max(0, l.getOutputSize());
			int input = Math.max(0, l.getInputSize());
			int direct = Math.min(input, output);
			int extension = Math.max(0, Math.abs(output - input));
			String ext = (input > output) ? "/" : "\\";
			sb.append("|".repeat(direct));
			sb.append(ext.repeat(extension));
			sb.append("(").append("Activation:").append(l.activation.name()).append(")");
			sb.append(System.lineSeparator());
			sb.append("#".repeat(output)).append(" ").append(output);
		}
		return sb.toString();
	}
	
}
