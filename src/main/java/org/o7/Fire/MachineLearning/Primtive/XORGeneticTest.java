package org.o7.Fire.MachineLearning.Primtive;

import Atom.Time.Time;
import Atom.Utility.Random;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.o7.Fire.Framework.XYRealtimeChart;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class XORGeneticTest {
	static Gson gson = new GsonBuilder().setPrettyPrinting().create();
	static double[][] X = {{0F, 0F}, {1F, 0F}, {0F, 1F}, {1F, 1F}};
	static double[][] Y = {{0F}, {1F}, {1F}, {0F}};
	static long iteration = 0, sampleEvery = 1000;
	static boolean passed = false, training = false;
	static File model = new File("XOR-SimpleGenetic-NeuralNetwork.json");
	static XYRealtimeChart chart = new XYRealtimeChart("Genetic XOR", "Iteration", "Loss");
	static double lossTop = 100;
	
	public static void main(String[] args) throws IOException, ClassNotFoundException, CloneNotSupportedException {
		ComparableSimpleNeuralNet[] population = new ComparableSimpleNeuralNet[10];
		if (model.exists()) {
			//List<ComparableSimpleNeuralNet> c ;
			//c = = SerializeData.dataIn(model);
			//c = gson.fromJson(new FileReader(model),List.class);
			//c.toArray(population);
			population = gson.fromJson(new FileReader(model), population.getClass());
		}
		update(population);
		for (int i = 0; i < population.length; i++) {
			chart.getSeries("Neural Net " + i).setMaximumItemCount(100);
		}
		chart.setVisible(true);
		Time t = new Time(TimeUnit.MILLISECONDS);
		while (lossTop > 0.4f) {
			update(population);
			test(population);
			iteration++;
		}
		
		Arrays.sort(population);
		System.out.println("Top 1 Model:");
		System.out.println(population[0].visualizeString());
		System.out.println("Top 100 Model:");
		System.out.println(population[population.length - 1].visualizeString());
		System.out.println("Top 1 Last Error: " + population[0].lastError());
		System.out.println("Top 100 Last Error: " + population[99].lastError());
		System.out.println("Finished in: " + t.elapsedS());
		//List<ComparableSimpleNeuralNet> l = Arrays.asList(population);
		//Files.writeString(model.toPath(), gson.toJson(l), StandardOpenOption.WRITE, StandardOpenOption.CREATE);
		//SerializeData.dataOut(Arrays.asList(population),model);
		Files.writeString(model.toPath(), gson.toJson(population), StandardOpenOption.WRITE, StandardOpenOption.CREATE);
	}

	public static void test(ComparableSimpleNeuralNet[] arr) {
		int pop = 0;
		for (ComparableSimpleNeuralNet c : arr) {
			double loss = -1;
			for (int i = 0; i < X.length; i++) {
				loss = c.error(X[i], Y[i]);
			}
			if (loss < lossTop) lossTop = loss;
			chart.getSeries("Neural Net " + pop).add(iteration, loss);
			chart.repaint();
			pop++;
		}
		
	}
	
	public static void update(ComparableSimpleNeuralNet[] arr) throws CloneNotSupportedException {
		for (int i = 0; i < arr.length; i++) {
			
			int get = Random.getInt(0, arr.length / 4);//get random top population
			ComparableSimpleNeuralNet top;
			top = arr[get];
			if (top == null) {//fallback
				top = generate();
			}else if (i < 10 && arr[i] != null) {//keep top population
				break;
			}else if (i > 90) {//remove last 10
				top = generate();
			}else {//clone top population if exist
				top = top.clone();
				top.update();//modify a bit
			}
			arr[i] = top;
			
		}
	}
	
	public static ComparableSimpleNeuralNet generate() {
		ComparableSimpleNeuralNet basic;
		basic = new ComparableSimpleNeuralNet(2, Random.getInt(2, 6), (input, output) -> new SimpleLayer(input, output, NeuralFunction.Tanh));
		basic.addRandomLayer(3, 2, 6, NeuralFunction.Tanh);
		basic.add(1, NeuralFunction.Tanh);
		return basic;
	}
}
