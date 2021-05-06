package org.o7.Fire.Experimental;

import Atom.Bootstrap.AtomicBootstrap;
import Atom.File.FileUtility;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;

public class Start {
	
	public static void main(String[] args) throws Throwable {
		AtomicBootstrap bootstrap = new AtomicBootstrap();
		File mindustry = new File(new File(FileUtility.getAppdata(), "Mindustry"), "build/cache/Anuken/Mindustry/releases/download/v126.2/Mindustry.jar");
		if (!mindustry.exists()) throw new FileNotFoundException(mindustry.getAbsolutePath());
		bootstrap.loadCurrentClasspath();
		bootstrap.loadClasspath();
		bootstrap.getLoader().addURL(mindustry);
		bootstrap.loadMain("org.o7.Fire.Experimental.MindustryMain", args);
	}
	
	public static class Server {
		public static void main(String[] args) {
			try {
				AtomicBootstrap bootstrap = new AtomicBootstrap();
				args = new String[]{"host", "Ancient_Caldera", "sandbox"};
				bootstrap.loadClasspath();
				String version = "v126.2";
				bootstrap.getLoader().addURL(new URL("https://github.com/Anuken/Mindustry/releases/download/" + version + "/server-release.jar"));
				bootstrap.loadMain("mindustry.server.ServerLauncher", args);
			}catch (Throwable t) {
				t.printStackTrace();
				
				System.exit(1);
			}
		}
	}
}
