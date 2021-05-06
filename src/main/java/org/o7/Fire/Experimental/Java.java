package org.o7.Fire.Experimental;

import Atom.String.WordGenerator;
import Atom.Utility.Pool;
import Atom.Utility.Random;
import Atom.Utility.Utility;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class Java {
	//95.217.132.133:3128
	public static void main(String[] args) throws IOException {
		SocketAddress sa = InetSocketAddress.createUnresolved("127.0.0.1", 2080);
		Proxy proxy = new Proxy(Proxy.Type.SOCKS, sa);
		Socket sc = new Socket(proxy);
		int serverPort = Random.getInt(100, 65000);
		ServerSocket server = new ServerSocket(serverPort);
		Pool.daemon(() -> {
			try {
				Socket cl = server.accept();
				System.out.println("Receiving Address: " + cl.getRemoteSocketAddress());
				System.out.println("Server Port: " + cl.getPort());
				System.out.println("Server Local Port: " + cl.getLocalPort());
				System.out.println("Server Received Word: " + new String(cl.getInputStream().readAllBytes()));
			}catch (IOException e) {
				e.printStackTrace();
				return;
			}
		}).start();
		sc.connect(new InetSocketAddress("127.0.0.1", serverPort));
		System.out.println("Connected to: " + sc.getRemoteSocketAddress());
		System.out.println("Client local port: " + sc.getLocalPort());
		System.out.println("Client port:" + sc.getPort());
		sc.getOutputStream().write(WordGenerator.randomWord().getBytes(StandardCharsets.UTF_8));
		sc.getOutputStream().flush();
		sc.close();
		Utility.convertThreadToInputListener("Assad ?\n", s -> {
			s = "org.o7.Fire." + s.trim();
			try {
				Java.class.getClassLoader().loadClass(s).getMethod("main", String[].class).invoke(null, (Object) args);
			}catch (Throwable e) {
				e.printStackTrace();
			}
		});
	}
}
