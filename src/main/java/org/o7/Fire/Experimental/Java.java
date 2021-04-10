package org.o7.Fire.Experimental;

import Atom.Utility.Utility;

import java.io.IOException;

public class Java {
	public static void main(String[] args) throws IOException {
		
		Utility.convertThreadToInputListener("Assad ?", s -> {
			s = "org.o7.Fire." + s.trim();
			try {
				Java.class.getClassLoader().loadClass(s).getMethod("main", String[].class).invoke(null, (Object) args);
			}catch (Throwable e) {
				e.printStackTrace();
			}
		});
	}
}
