package Premain;

import arc.Core;
import arc.Events;
import arc.scene.Action;
import arc.scene.Scene;
import arc.scene.ui.Dialog;
import arc.util.Log;
import arc.util.OS;
import mindustry.Vars;
import mindustry.game.EventType;
import mindustry.gen.Icon;
import mindustry.mod.Mod;
import mindustry.ui.fragments.Fragment;

public class Main extends Mod {
	boolean stat;
	@Override
	public void init() {
		//do you like lambda ?
		Core.settings.getBoolOnce("Test", ()->{
			Events.on(EventType.ClientLoadEvent.class, s ->{
				new Dialog("Test"){{
					cont.button("Yeet", Icon.image, ()->{
						Vars.ui.showInfoFade("Yeet");
					}).growX();
					cont.row();
					cont.button( Icon.cancel, this::hide).growX();
				}}.show();
			});
		});
		Vars.ui.settings.main.table(t ->{
			t.check("Boolean", s->{
				stat = s;
				Log.info(s);
			});
		}).growX();
	}
}
