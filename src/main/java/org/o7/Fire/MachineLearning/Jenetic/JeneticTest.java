package org.o7.Fire.MachineLearning.Jenetic;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;

public abstract class JeneticTest {
	public static Gson gson = new GsonBuilder().setPrettyPrinting().create();
	public File bestJsonModel = new File(this.getClass().getSimpleName() + "-NeuralNetwork-Best.json"), worstJsonModel = new File(this.getClass().getSimpleName() + "-NeuralNetwork-Worst.json"), lastPopulation = new File(this.getClass().getSimpleName() + "-Jenetic-Population.obj");
}
