package org.o7.Fire.MachineLearning.Primtive;

import Atom.Time.Time;
import Atom.Utility.Random;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.jfree.data.xy.XYSeries;
import org.o7.Fire.MachineLearning.Framework.Chart;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class XORTest {
	static File model = new File("XOR-Simple-NeuralNetwork.json");
	static Gson gson = new GsonBuilder().setPrettyPrinting().create();
	static double[][] X = {{0F, 0F}, {1F, 0F}, {0F, 1F}, {1F, 1F}};
	static double[][] Y = {{0F}, {1F}, {1F}, {0F}};
	static long iteration = 0, sampleEvery = 1000;
	static boolean passed = false, training = false;
	
	static SimpleNeuralNet basic;
	
	public static final XYSeries fitnessScore = new XYSeries("Fitness");
	
	public static void main(String[] args) throws IOException {
		if (!model.exists()) {
			/*
			basic = new SimpleNeuralNet(2, 2, (input, output) -> new SimpleLayer(input,output, NeuralFunction.Tanh));
			basic.add(2);
			basic.add(1, NeuralFunction.Binary);
			 */
			basic = new SimpleNeuralNet(2, Random.getInt(2, 6), (input, output) -> new SimpleLayer(input, output, NeuralFunction.Tanh));
			basic.addRandomLayer(3, 2, 6, NeuralFunction.Tanh);
			basic.add(1, NeuralFunction.Binary);
		}else {
			System.out.println("Loading from: " + model.getPath());
			basic = gson.fromJson(Files.readString(model.toPath()), SimpleNeuralNet.class);
		}
		System.out.println(basic.visualizeString());
		System.out.println();
		Time time = new Time(TimeUnit.MILLISECONDS);
		while (!passed) {
			passed = true;
			double f = 0;
			for (int i = 0; i < X.length; i++) {
				//System.out.println("XOR: " + Arrays.toString(X[i]) + ", Output: " + basic.process(X[i])[0] + ", Expected: " + Y[i][0]);
				f += basic.error(X[i], Y[i]);
				if (f > 0.5) {
					passed = false;
				}
			}
			fitnessScore.add(iteration, f);
			if ((iteration % sampleEvery) == 0) {
				System.out.println("Iteration: " + iteration);
				for (int i = 0; i < X.length; i++) {
					System.out.println(i + ". XOR: " + Arrays.toString(X[i]) + ", Output: " + basic.process(X[i])[0] + ", Expected: " + Y[i][0]);
				}
			}
			if (!passed) {
				training = true;
				basic.update();
			}
			iteration++;
		}
		if (training) {
			System.out.println("Training took: " + iteration + " iteration");
			System.out.println("Training took: " + time.elapsedS());
		}
		
		if (!model.exists()) Files.writeString(model.toPath(), gson.toJson(basic));
		for (int i = 0; i < X.length; i++) {
			System.out.println("XOR: " + Arrays.toString(X[i]) + ", Output: " + basic.process(X[i])[0] + ", Expected: " + Y[i][0]);
		}
		System.out.println();
		System.out.println(basic.visualizeString());
		String assad = "Loss";
		Chart chart = new Chart(assad + " Overtime", "Generation", assad);
		chart.setSeries(fitnessScore);
		chart.spawn();
	}
}
