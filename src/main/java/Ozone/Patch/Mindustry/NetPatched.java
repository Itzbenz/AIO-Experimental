package Ozone.Patch.Mindustry;

import Atom.Reflect.Reflect;
import Atom.Reflect.UnThread;
import Atom.Utility.Pool;
import Ozone.Net.LoggableNet;
import arc.struct.IntMap;
import mindustry.Vars;
import mindustry.net.Net;
import mindustry.net.Packets;
import mindustry.net.Streamable;
import org.jfree.data.xy.XYSeries;
import org.o7.Fire.Experimental.Main;
import org.o7.Fire.Framework.XYRealtimeChart;

public class NetPatched extends LoggableNet {
	static final XYRealtimeChart pingChart = new XYRealtimeChart("Ping Chart", "Time (ms)", "ms");
	public static volatile String ip;
	public static volatile int port;
	static long l = 0, last = System.currentTimeMillis();
	static XYSeries pingClientChart = null, pingSeries = null;
	static int factorSleep = 2;
	static int sleep = 16 * factorSleep;
	static int max = (50 / factorSleep) * 30;
	
	static {
		pingChart.setVisible(true);
		Pool.daemon(() -> {
			while (true) {
				try {
					UnThread.sleep(sleep);
					if (ip == null) continue;
					if (!Vars.net.client()) continue;
					String current = ip + ":" + port;
					if (pingClientChart == null) {
						pingClientChart = pingChart.getSeries(current);
						pingClientChart.setMaximumItemCount(max);
						pingSeries = pingChart.getSeries(current + " Socket Ping");
						pingSeries.setMaximumItemCount(max);
						last = System.currentTimeMillis();
					}
					if (!pingClientChart.getKey().equals(current)) {
						pingChart.getCollection().removeSeries(pingClientChart);
						pingChart.getCollection().removeSeries(pingSeries);
						pingClientChart = null;
						pingSeries = null;
						l = 0;
						continue;
					}
					int i;
					try {
						i = Main.ping();
					}catch (Exception e) {
						i = 0;
					}
					pingSeries.add(System.currentTimeMillis() - last, i);
					pingClientChart.add(System.currentTimeMillis() - last, Vars.netClient.getPing());
					pingChart.repaint();
					
				}catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start();
	}
	
	public NetPatched(Net net) {
		super(net);
	}
	
	@Override
	public void connect(String ips, int ports, Runnable success) {
		if (Vars.ui != null && Vars.ui.loadfrag != null) {
			Vars.ui.loadfrag.setProgress(() -> 0.05f);
		}
		
		super.connect(ips, ports, () -> {
			ip = ips;
			port = ports;
			if (Vars.ui != null && Vars.ui.loadfrag != null) {
				Vars.ui.loadfrag.setProgress(() -> 1f);
			}
			success.run();
		});
	}
	
	@Override
	public void send(Object object, SendMode mode) {
		super.send(object, mode);
		if (!server()) {
			if (object instanceof Packets.ConnectPacket) if (Vars.ui != null && Vars.ui.loadfrag != null) {
				Vars.ui.loadfrag.setProgress(() -> 0.15f);
			}
		}
	}
	
	@Override
	public void handleClientReceived(Object object) {
		super.handleClientReceived(object);
		if (object instanceof Packets.Connect) {
			if (Vars.ui != null && Vars.ui.loadfrag != null) {
				Vars.ui.loadfrag.setProgress(() -> 0.1f);
			}
		}
		if (Vars.ui == null || Vars.ui.loadfrag == null) return;
		IntMap<Streamable.StreamBuilder> streams = Reflect.getField(Net.class, "streams", net);
		if (streams == null) return;
		if (object instanceof Packets.StreamBegin) {
			Streamable.StreamBuilder builder = streams.get(((Packets.StreamBegin) object).id);
			if (builder == null) return;
			Vars.ui.loadfrag.setProgress(builder::progress);
		}
		if (object instanceof Packets.StreamChunk) {
			Streamable.StreamBuilder builder = streams.get(((Packets.StreamChunk) object).id);
			if (builder == null) return;
			Vars.ui.loadfrag.setText("Downloading Map " + builder.stream.size() + "/" + builder.total);
		}
	}
}