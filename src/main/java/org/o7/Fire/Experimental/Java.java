package org.o7.Fire.Experimental;

import Atom.Utility.Utility;

import java.io.IOException;

public class Java {
	//95.217.132.133:3128
	public static void main(String[] args) throws IOException {
		Utility.convertThreadToInputListener("Assad ?\n", s -> {
			if (s == null) return;
			if (s.isEmpty()) return;
			s = "org.o7.Fire." + s.trim();
			try {
				Java.class.getClassLoader().loadClass(s).getMethod("main", String[].class).invoke(null, (Object) args);
			}catch(Throwable e){
				e.printStackTrace();
			}
		});
	}
}
