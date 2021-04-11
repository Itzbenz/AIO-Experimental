package org.o7.Fire.Experimental;


import arc.Core;
import arc.Events;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import mindustry.Vars;
import mindustry.game.EventType;
import mindustry.game.Team;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.mod.Mod;

public class Main extends Mod {
	
	@Override
	public void init() {
		//mere mortal add node
		TextureRegion laser = Core.atlas.find("laser"), laserEnd = Core.atlas.find("laser-end");
		Events.run(EventType.Trigger.draw, () -> {
			Draw.draw(Layer.overlayUI, () -> {
				float xCenter = Core.camera.position.getX(), yCenter = Core.camera.position.getY();
				float xEdgeLeft = xCenter - Core.camera.width / 2f, yEdgeBottom = yCenter - Core.camera.height / 2F;
				Draw.rect("circle-shadow", xCenter, yCenter, xEdgeLeft, yEdgeBottom);
				float scale = (float) 1 / Vars.renderer.getDisplayScale();
				float increment = (float) 15 / Vars.renderer.getDisplayScale();
				for (int i = 0; i < 10; i++) {
					Drawf.laser(Team.derelict, laser, laserEnd, xEdgeLeft, yEdgeBottom, xCenter, yCenter, scale);
					yEdgeBottom += increment;
				}
			});
			
			
		});
		Vars.ui.settings.game.row().table(t -> {
			t.button("Neural Net Render", () -> {
				
			
			});
		});
	}
}
