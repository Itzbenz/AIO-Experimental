package org.o7.Fire.Experimental;


import arc.Core;
import arc.Events;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import mindustry.Vars;
import mindustry.game.EventType;
import mindustry.game.Team;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.mod.Mod;
import org.o7.Fire.MachineLearning.Jenetic.RawBasicNeuralNet;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class Main extends Mod {
	static File model = new File("XOR-Jenetic-NeuralNetwork.json"), lastPopulation = new File("XOR-Jenetic-Population.obj");
	static Gson gson = new GsonBuilder().setPrettyPrinting().create();
	
	@Override
	public void init() {
		String assad = "";
		try {
			assad = Files.readString(model.toPath());
		}catch (IOException e) {
			e.printStackTrace();
			return;
		}
		RawBasicNeuralNet basicNeuralNet = gson.fromJson(assad, RawBasicNeuralNet.class);
		
		//mere mortal add node
		
		TextureRegion laser = Core.atlas.find("laser"), laserEnd = Core.atlas.find("laser-end");
		Events.run(EventType.Trigger.draw, () -> {
			Draw.draw(Layer.overlayUI, () -> {
				float xCenter = Core.camera.position.getX(), yCenter = Core.camera.position.getY();
				float xEdgeLeft = xCenter - Core.camera.width / 2f, yEdgeBottom = yCenter - Core.camera.height / 2F;
				Draw.rect("circle-shadow", xCenter, yCenter, xEdgeLeft, yEdgeBottom);
				float laserScale = (float) 1 / Vars.renderer.getDisplayScale();
				float verticalSpace = (float) 15 / Vars.renderer.getDisplayScale(), horizontalSpace = (float) 20 / Vars.renderer.getDisplayScale();
				float x1 = xEdgeLeft, y1 = yEdgeBottom, x2 = x1 + horizontalSpace, y2 = yEdgeBottom;
				float[][] lastPosX = new float[basicNeuralNet.size()][], lastPosY = new float[basicNeuralNet.size()][];
				for (int i = 0; i < basicNeuralNet.size(); i++) {
					for (int j = 0; j < basicNeuralNet.getOutput(i); j++) {
						Drawf.circles(x1, y1, laserScale * 0.2f);
						if (j != 0) {
							for (int i1 = 0; i1 < lastPosX[i - 1].length; i1++) {
								Drawf.laser(Team.derelict, laser, laserEnd, lastPosX[i - 1][i1], lastPosY[i - 1][i1], x1, y1, laserScale);
							}
						}
						lastPosX[i][j] = x1;
						lastPosY[i][j] = y1;
						y1 += verticalSpace;
						
						
					}
					x1 += horizontalSpace;
					x2 += horizontalSpace;
					if (y1 > yCenter) {
						y1 = yEdgeBottom;
						continue;
					}
					
				}
			});
			
			
		});
		Vars.ui.settings.game.row().table(t -> {
			t.button("Neural Net Render", () -> {
				
			
			});
		});
	}
}
