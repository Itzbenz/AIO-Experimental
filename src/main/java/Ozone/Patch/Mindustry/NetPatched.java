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

import java.io.IOException;

public class NetPatched extends LoggableNet {
	static final XYRealtimeChart pingChart = new XYRealtimeChart("Ping Chart", "Iteration", "ms");
	public static volatile String ip;
	public static volatile int port;
	static long l = 0;
	static XYSeries series = null;
	
	static {
		pingChart.setVisible(true);
		Pool.daemon(() -> {
			while (true) {
				try {
					if (ip == null) return;
					String current = ip + ":" + port;
					if (series == null) {
						series = pingChart.getSeries(current);
						series.setMaximumItemCount(500);
					}
					if (!series.getKey().equals(current)) {
						pingChart.getCollection().removeSeries(series);
						series = null;
						l = 0;
						return;
					}
					int i = 0;
					try {
						i = Main.ping();
					}catch (IOException e) {
						i = 0;
					}
					series.add(l++, i);
					pingChart.repaint();
					UnThread.sleep(16 * 2);
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