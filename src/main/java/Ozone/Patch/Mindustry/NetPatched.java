package Ozone.Patch.Mindustry;

import Atom.Reflect.Reflect;
import Ozone.Net.LoggableNet;
import arc.struct.IntMap;
import mindustry.Vars;
import mindustry.net.Net;
import mindustry.net.Packets;
import mindustry.net.Streamable;

public class NetPatched extends LoggableNet {
	public static String ip;
	public static int port;
	
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