package org.o7.Fire.MachineLearning.Java;

import Atom.Time.Time;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.o7.Fire.MachineLearning.Framework.ActivationFunction;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class XORTestObject {
	static Gson gson = new GsonBuilder().setPrettyPrinting().create();
	static float[][] X = {{0F, 0F}, {1F, 0F}, {0F, 1F}, {1F, 1F}};
	static Float[][] XF = {{0F, 0F}, {1F, 0F}, {0F, 1F}, {1F, 1F}};
	static float[][] Y = {{0F}, {1F}, {1F}, {0F}};
	static long iteration = 0, sampleEvery = 1000;
	static boolean passed = false, training = false;
	static File model = new File("XOR-Vector-NeuralNetwork.json");
	static VectorNeuralNetwork basic;
	
	public static void main(String[] args) throws IOException {
		if (!model.exists()) {
			basic = VectorNeuralNetwork.random(ActivationFunction.Tanh, 2, 10, 2, 3, 3);
			/*
			basic = new VectorNeuralNetwork();
			basic.add(new VectorLayer(2, 2, ActivationFunction.Relu));
			basic.add(new VectorLayer(2, 2, ActivationFunction.Relu));
			basic.add(new VectorLayer(2, 1, ActivationFunction.Binary));
			*/
			basic.add(new VectorLayer(3, 1, ActivationFunction.Binary));
		}else {
			System.out.println("Loading from: " + model.getPath());
			basic = gson.fromJson(Files.readString(model.toPath()), VectorNeuralNetwork.class);
		}
		System.out.println(basic.visualizeString());
		System.out.println();
		Time time = new Time(TimeUnit.MILLISECONDS);
		while (!passed) {
			passed = true;
			for (int i = 0; i < X.length; i++) {
				//System.out.println("XOR: " + Arrays.toString(X[i]) + ", Output: " + basic.process(X[i])[0] + ", Expected: " + Y[i][0]);
				double f = basic.error(X[i], Y[i])[0];
				if ((int) f == 1) {
					passed = false;
					break;
				}
			}
			if ((iteration % sampleEvery) == 0) {
				System.out.println("Iteration: " + iteration);
				for (int i = 0; i < X.length; i++) {
					System.out.println(i + ". XOR: " + Arrays.toString(X[i]) + ", Output: " + basic.process(XF[i])[0] + ", Expected: " + Y[i][0]);
				}
			}
			if (!passed) {
				training = true;
				basic.updateBias();
				basic.updateWeight();
			}
			iteration++;
		}
		if (training) {
			System.out.println("Training took: " + iteration + " epoch");
			System.out.println("Training took: " + time.elapsedS());
		}
		
		if (!model.exists()) Files.writeString(model.toPath(), gson.toJson(basic));
		for (int i = 0; i < X.length; i++) {
			System.out.println(i + ". XOR: " + Arrays.toString(X[i]) + ", Output: " + basic.process(XF[i])[0] + ", Expected: " + Y[i][0]);
		}
		System.out.println();
		System.out.println(basic.visualizeString());
	}
}
