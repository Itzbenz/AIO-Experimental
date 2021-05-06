package org.o7.Fire.Experimental;


import Atom.Time.Timer;
import Atom.Utility.Random;
import arc.Core;
import arc.Events;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.util.Log;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import mindustry.Vars;
import mindustry.game.EventType;
import mindustry.game.Team;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.mod.Mod;
import org.o7.Fire.MachineLearning.Framework.RawBasicNeuralNet;
import org.o7.Fire.MachineLearning.Framework.RawNeuralNet;

import java.io.File;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.SocketAddress;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class Main extends Mod {
	static File model = new File("XOR-Jenetic-NeuralNetwork.json"), lastPopulation = new File("XOR-Jenetic-Population.obj");
	static Gson gson = new GsonBuilder().setPrettyPrinting().create();
	static int[] structure = new int[5];//prob gonna do genetic for this too
	static AddInputRawNeuralNet basicNeuralNet = null;
	static SocketAddress sa = InetSocketAddress.createUnresolved("18.221.225.153", 2080);
	static Proxy proxy = new Proxy(Proxy.Type.SOCKS, sa);
	
	@Override
	public void init() {
		Timer timer = new Timer(TimeUnit.SECONDS, 5);
		TextureRegion laser = Core.atlas.find("laser"), laserEnd = Core.atlas.find("laser-end");
		Events.run(EventType.Trigger.draw, () -> {
			Draw.draw(Layer.overlayUI, () -> {
				if (timer.get() || basicNeuralNet == null) {
					for (int i = 0; i < structure.length; i++) {
						structure[i] = Random.getInt(1, 7);
					}
					int knob = RawNeuralNet.needRaw(2, structure);
					double[] randomArray = new double[knob];
					for (int i = 0; i < randomArray.length; i++) {
						randomArray[i] = Random.getDouble(-100, 100);
					}
					Log.info("Structure: " + Arrays.toString(structure));
					Log.info("Knob: " + knob);
					RawBasicNeuralNet raw = new RawBasicNeuralNet(randomArray, structure);
					basicNeuralNet = new AddInputRawNeuralNet(raw, 2);
				}
				float scale = 2f;
				float xCenter = Core.camera.position.getX() + Core.camera.width / (64 * scale), yCenter = Core.camera.position.getY() + Core.camera.height / (32 * scale);
				float xEdgeLeft = xCenter - Core.camera.width / 2f, yEdgeBottom = yCenter - Core.camera.height / 2F;
				float verticalSpace = ((float) 24 / Vars.renderer.getDisplayScale()) * scale;
				float horizontalSpace = verticalSpace * 2f;
				float x1 = xEdgeLeft, y1 = yEdgeBottom;
				float[][] lastPosX = new float[basicNeuralNet.size()][], lastPosY = new float[basicNeuralNet.size()][];
				for (int i = 0; i < basicNeuralNet.size(); i++) {
					lastPosX[i] = new float[basicNeuralNet.getOutput(i)];
					lastPosY[i] = new float[basicNeuralNet.getOutput(i)];
				}
				int index = 0;
				float layerMaxSize = 0;
				for (int i = 0; i < basicNeuralNet.size(); i++) {
					layerMaxSize = Math.max(basicNeuralNet.getOutput(i), layerMaxSize);
				}
				layerMaxSize = layerMaxSize * verticalSpace;
				for (int i = 0; i < basicNeuralNet.size(); i++) {//for layer
					int nodeCount = basicNeuralNet.getOutput(i);
					float offset = layerMaxSize - nodeCount * verticalSpace;//get layer max box size subtract by current layer box size
					if (offset > 0) {//6 - 2 = 2
						offset = offset / 2;//2 / 2 = 1
						y1 += offset;//offset 1 to center current layer box
					}
					for (int j = 0; j < nodeCount; j++) {//for node in layer
						Drawf.circles(x1, y1, (6f * scale) / Vars.renderer.getDisplayScale());//draw node
						if (i != 0) {
							for (int i1 = 0; i1 < lastPosX[i - 1].length; i1++) {
								double weight = basicNeuralNet.getRaw(index);//100 - -100
								boolean negative = weight < 0;
								weight = Math.abs(weight);
								weight = weight / 98f;//100 is max number, subtract by 2 because 0,001 alpha is just gay
								weight = weight * ((scale / 2f));
								float n = negative ? 0.5f : 1;//wow retard use float
								Draw.color(255, n, n, (float) weight);
								Drawf.laser(Team.derelict, laser, laserEnd, lastPosX[i - 1][i1], lastPosY[i - 1][i1], x1, y1, ((scale / 2.2f) / Vars.renderer.getDisplayScale()));
								Draw.color();
								index++;//weight
							}
							index++;//bias
						}
						
						lastPosX[i][j] = x1;
						lastPosY[i][j] = y1;
						y1 += verticalSpace;
					}
					
					
					x1 += horizontalSpace;
					y1 = yEdgeBottom;
				}
			});
			
			
		});
		Vars.ui.settings.game.row().table(t -> {
			t.button("Neural Net Render", () -> {
				
			
			}).growX().row();
			String[] assad = new String[]{"127.0.0.1:2020"};
			t.button("Proxy", () -> {
				String[] prox = Random.getRandom(assad).split(":");
				String h = prox[0], p = prox[1];
				Vars.ui.showConfirm("Proxy", "Assad: " + Arrays.toString(prox), () -> {
					System.setProperty("socksProxyHost", h);
					System.setProperty("socksProxyPort", p);
				});
				
			}).growX();
		});
	}
	
	public static class AddInputRawNeuralNet implements RawNeuralNet {
		RawNeuralNet raw;
		int input;
		
		public AddInputRawNeuralNet(RawNeuralNet raw, int input) {
			super();
			this.raw = raw;
			this.input = input;
		}
		
		@Override
		public double activation(double d) {
			return raw.activation(d);
		}
		
		@Override
		public int size() {
			return raw.size() + 1;
		}
		
		@Override
		public int getOutput(int index) {
			return index == 0 ? input : raw.getOutput(index - 1);
		}
		
		@Override
		public double getRaw(int index) {
			return raw.getRaw(index);
		}
	}
}
