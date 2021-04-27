package org.o7.Fire.MachineLearning.Jenetic;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.o7.Fire.MachineLearning.Framework.RawBasicNeuralNet;
import org.o7.Fire.MachineLearning.Framework.RawNeuralNet;

import java.io.File;

public class XORTestGenetic {
	static File model = new File("XOR-Jenetic-NeuralNetwork.json");
	static Gson gson = new GsonBuilder().setPrettyPrinting().create();
	static double[][] X = {{0F, 0F}, {1F, 0F}, {0F, 1F}, {1F, 1F}};
	static double[][] Y = {{0F}, {1F}, {1F}, {0F}};
	static int[] structure = new int[]{5, 4, 5, 3, 1};//prob gonna do genetic for this too
	static int knob = RawNeuralNet.needRaw(2, structure);
	static int maxRange = 100;
	
	public static double eval(RawBasicNeuralNet net) {
		double dd = 0;
		for (int i = 0; i < X.length; i++) {
			dd += net.error(X[i], Y[i]);
		}
		return dd;
	}
	
	public static void main(String[] args) {
		System.out.println("Controllable knob: " + knob);
		System.out.println("Knob range: " + -maxRange + " to " + maxRange);
		
	}
	
	private static double eval(double[] gt) {
		RawBasicNeuralNet net = new RawBasicNeuralNet(gt, structure);
		return eval(net);
	}
}
