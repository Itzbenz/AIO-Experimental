package org.o7.Fire.Experimental;

import Atom.Time.Time;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.o7.Fire.MachineLearning.Framework.NeuralActivation;
import org.o7.Fire.MachineLearning.Java.VectorLayer;
import org.o7.Fire.MachineLearning.Java.VectorNeuralNetwork;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class Test {
	static Gson gson = new GsonBuilder().setPrettyPrinting().create();
	static float[][] X = {{0F, 0F}, {1F, 0F}, {0F, 1F}, {1F, 1F}};
	static Float[][] XF = {{0F, 0F}, {1F, 0F}, {0F, 1F}, {1F, 1F}};
	static float[][] Y = {{0F}, {1F}, {1F}, {0F}};
	static long epoch = 0;
	static double[] err;
	static boolean passed = false, training = false;
	static File model = new File("XOR-Vector-NeuralNetwork.json");
	static VectorNeuralNetwork basic;
	
	
	public static void main(String[] args) throws IOException {
		if (!model.exists()) {
			//basic = VectorNeuralNetwork.random(NeuralActivation.Tanh, 2, 10, 2, 3, 3);
			basic = new VectorNeuralNetwork();
			basic.add(new VectorLayer(2, 4, NeuralActivation.Tanh));
			basic.add(new VectorLayer(4, 3, NeuralActivation.Tanh));
			basic.add(new VectorLayer(3, 1, NeuralActivation.Binary));
		}else {
			System.out.println("Loading from: " + model.getPath());
			basic = gson.fromJson(Files.readString(model.toPath()), VectorNeuralNetwork.class);
		}
		System.out.println(basic.visualizeString());
		System.out.println();
		Time time = new Time(TimeUnit.MILLISECONDS);
		while (!passed) {
			passed = true;
			err = new double[X.length];
			for (int i = 0; i < X.length; i++) {
				//System.out.println("XOR: " + Arrays.toString(X[i]) + ", Output: " + basic.process(X[i])[0] + ", Expected: " + Y[i][0]);
				double f = basic.error(X[i], Y[i])[0];
				err[i] = f;
				if ((int) f == 1) {
					passed = false;
				}
			}
			if (!passed) {
				training = true;
				basic.updateBias();
				basic.updateWeight();
			}
			epoch++;
		}
		if (training) {
			System.out.println("Training took: " + epoch + " epoch");
			System.out.println("Training took: " + time.elapsedS());
		}
		
		if (!model.exists()) Files.writeString(model.toPath(), gson.toJson(basic));
		for (int i = 0; i < X.length; i++) {
			System.out.println("XOR: " + Arrays.toString(X[i]) + ", Output: " + basic.process(XF[i])[0] + ", Expected: " + Y[i][0]);
		}
		
	}
}
