package org.o7.Fire.Experimental;

import Atom.Bootstrap.AtomicBootstrap;
import Atom.File.FileUtility;

import java.io.File;
import java.io.FileNotFoundException;

public class Start {
	
	public static void main(String[] args) throws Throwable {
		AtomicBootstrap bootstrap = new AtomicBootstrap();
		File mindustry = new File(new File(FileUtility.getAppdata(), "Mindustry"), "build/cache/Anuken/Mindustry/releases/download/v126.2/Mindustry.jar");
		if (!mindustry.exists()) throw new FileNotFoundException(mindustry.getAbsolutePath());
		bootstrap.loadCurrentClasspath();
		bootstrap.loadClasspath();
		bootstrap.getLoader().addURL(mindustry);
		bootstrap.loadMain("Premain.MindustryMain", args);
	}
}
