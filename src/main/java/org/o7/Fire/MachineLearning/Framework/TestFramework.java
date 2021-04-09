package org.o7.Fire.MachineLearning.Framework;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;

public class TestFramework {
	static Gson gson = new GsonBuilder().setPrettyPrinting().create();
	static float[][] X = {{0F, 0F}, {1F, 0F}, {0F, 1F}, {1F, 1F}};
	static float[][] Y = {{0F}, {1F}, {1F}, {0F}};
	static long epoch = 0;
	static boolean passed = false, training = false;
	
	public static void main(File model) {
	
	}
}
