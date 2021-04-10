package org.o7.Fire.MachineLearning.Primtive;

import Atom.Time.Time;
import Atom.Utility.Random;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

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
	
	public static void main(String[] args) throws IOException, ClassNotFoundException, CloneNotSupportedException {
		ComparableSimpleNeuralNet[] population = new ComparableSimpleNeuralNet[100];
		if (model.exists()) {
			//List<ComparableSimpleNeuralNet> c ;
			//c = = SerializeData.dataIn(model);
			//c = gson.fromJson(new FileReader(model),List.class);
			//c.toArray(population);
			population = gson.fromJson(new FileReader(model), population.getClass());
		}
		
		Time t = new Time(TimeUnit.MILLISECONDS);
		for (int i = 0; i < 5; i++) {
			update(population);
			test(population);
		}
		
		Arrays.sort(population);
		System.out.println("Top 1 Last Error: " + population[0].lastError());
		System.out.println("Top 100 Last Error: " + population[99].lastError());
		System.out.println("Finished in: " + t.elapsedS());
		//List<ComparableSimpleNeuralNet> l = Arrays.asList(population);
		//Files.writeString(model.toPath(), gson.toJson(l), StandardOpenOption.WRITE, StandardOpenOption.CREATE);
		//SerializeData.dataOut(Arrays.asList(population),model);
		Files.writeString(model.toPath(), gson.toJson(population), StandardOpenOption.WRITE, StandardOpenOption.CREATE);
	}
	
	public static void test(ComparableSimpleNeuralNet[] arr) {
		for (ComparableSimpleNeuralNet c : arr) {
			for (int i = 0; i < X.length; i++) {
				c.error(X[i], Y[i]);
			}
		}
		
	}
	
	public static void update(ComparableSimpleNeuralNet[] arr) throws CloneNotSupportedException {
		for (int i = 0; i < arr.length; i++) {
			
			int get = Random.getInt(0, 10);//get random top population
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
