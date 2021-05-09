package org.o7.Fire.Experimental;


import Atom.Time.Timer;
import Atom.Utility.Pool;
import Atom.Utility.Random;
import Ozone.Patch.Mindustry.NetPatched;
import arc.Core;
import arc.Events;
import arc.func.Prov;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.util.Log;
import arc.util.Time;
import arc.util.pooling.Pools;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.jenetics.Genotype;
import io.jenetics.IntegerChromosome;
import io.jenetics.IntegerGene;
import io.jenetics.Optimize;
import io.jenetics.engine.Engine;
import io.jenetics.engine.EvolutionResult;
import io.jenetics.engine.EvolutionStatistics;
import io.jenetics.engine.Limits;
import io.jenetics.stat.DoubleMomentStatistics;
import io.jenetics.util.Factory;
import mindustry.Vars;
import mindustry.game.EventType;
import mindustry.game.Team;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.mod.Mod;
import mindustry.net.Host;
import mindustry.net.Net;
import mindustry.net.NetworkIO;
import mindustry.net.Packets;
import org.jfree.data.xy.XYSeries;
import org.o7.Fire.Framework.XYRealtimeChart;
import org.o7.Fire.MachineLearning.Framework.RawBasicNeuralNet;
import org.o7.Fire.MachineLearning.Framework.RawNeuralNet;

import java.io.File;
import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.Arrays;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class Main extends Mod {
	static File model = new File("XOR-Jenetic-NeuralNetwork.json"), lastPopulation = new File("XOR-Jenetic-Population.obj");
	static Gson gson = new GsonBuilder().setPrettyPrinting().create();
	static int[] structure = new int[5];//prob gonna do genetic for this too
	static AddInputRawNeuralNet basicNeuralNet = null;
	static SocketAddress sa = InetSocketAddress.createUnresolved("18.221.225.153", 2080);
	static Proxy proxy = new Proxy(Proxy.Type.SOCKS, sa);
	static Prov<DatagramPacket> packetSupplier = () -> new DatagramPacket(new byte[512], 512);
	static final XYRealtimeChart pingChart = new XYRealtimeChart("Ping Chart", "Iteration", "ms");
	static final AtomicLong count = new AtomicLong();
	static Executor executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 100, r -> {
		Thread t = Executors.defaultThreadFactory().newThread(r);
		t.setDaemon(true);
		t.setPriority(1);
		return t;
	});
	static XYSeries series = null;
	
	public int ping() throws IOException {
		DatagramSocket socket = new DatagramSocket();
		long time = Time.millis();
		socket.send(new DatagramPacket(new byte[]{-2, 1}, 2, InetAddress.getByName(NetPatched.ip), NetPatched.port));
		socket.setSoTimeout(60000);
		
		DatagramPacket packet = packetSupplier.get();
		socket.receive(packet);
		
		ByteBuffer buffer = ByteBuffer.wrap(packet.getData());
		Host host = NetworkIO.readServerData((int) Time.timeSinceMillis(time), packet.getAddress().getHostAddress(), buffer);
		return host.ping;
	}
	
	@Override
	public void init() {
		Timer timer = new Timer(TimeUnit.SECONDS, 5);
		TextureRegion laser = Core.atlas.find("laser"), laserEnd = Core.atlas.find("laser-end");
		// 1.) Define the genotype (factory) suitable
		//     for the problem.
		Factory<Genotype<IntegerGene>> gtf = Genotype.of(IntegerChromosome.of(Byte.MIN_VALUE, Byte.MAX_VALUE, Random.getInt(32, 512)));
		
		Vars.net = new NetPatched(Vars.net);
		// 3.) Create the execution environment.
		Engine<IntegerGene, Integer> engine = Engine.builder(this::send, gtf).populationSize(1000).optimize(Optimize.MAXIMUM).executor(executor).build();
		
		// 4.) Start the execution (evolution) and
		//     collect the result.
		
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
			t.button("Stress Test", () -> {
				Vars.ui.showConfirm("Stress Tester", "Target: " + String.valueOf(NetPatched.ip) + ":" + String.valueOf(NetPatched.port), () -> {
					if (series != null) pingChart.getCollection().removeSeries(series);
					series = pingChart.getSeries(NetPatched.ip + ":" + NetPatched.port);
					series.setMaximumItemCount(500);
					count.set(0);
					Core.scene.clear();
					Pool.daemon(() -> {
						EvolutionStatistics<Integer, DoubleMomentStatistics> stat = EvolutionStatistics.ofNumber();
						Genotype<IntegerGene> result = engine.stream().limit(Limits.byExecutionTime(Duration.ofSeconds(30))).peek(stat).collect(EvolutionResult.toBestGenotype());
						System.out.println(result);
						System.out.println(stat);
					}).start();
				});
				
				
			}).growX().row();
		}).growX();
	}
	
	public int send(Genotype<IntegerGene> s) {
		
		Packets.InvokePacket packet = Pools.obtain(Packets.InvokePacket.class, Packets.InvokePacket::new);
		packet.type = 72;
		packet.priority = 0;
		
		byte[] bytes = new byte[s.chromosome().length()];
		for (int i = 0; i < s.chromosome().length(); i++) {
			bytes[i] = s.chromosome().get(i).byteValue();
		}
		packet.length = bytes.length;
		packet.bytes = bytes;
		int i = 0;
		try {
			Vars.net.send(packet, Net.SendMode.udp);
			i = ping();
		}catch (IOException e) {
			e.printStackTrace();
			i = 0;
		}
		
		series.add(count.getAndAdd(1), i);
		return i;
		
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
