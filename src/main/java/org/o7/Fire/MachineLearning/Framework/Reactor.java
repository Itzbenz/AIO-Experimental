package org.o7.Fire.MachineLearning.Framework;

import Atom.Struct.PoolObject;
import Atom.Utility.Random;

public class Reactor implements PoolObject.Object {
	public static int factorPower = 100;
	protected double heat = 0, powerOutput = 0.1f, control = 0.1f;//range 0 - 1
	protected double heatGain = 0.3f;
	protected double heatDissipation = 0.2f;//environment settings range 0 - 1
	protected double totalOutput = 0;
	protected long interaction = 0, iteration = 0;
	
	public void resetRandom() {
		heat = 0;
		powerOutput = 0.1f;
		control = 0;//range 0 - 1
		heatGain = Random.getDouble();//range 0 - 1
		heatDissipation = Random.getDouble(0, heatGain - 0.01f);//range 0 - 1
		totalOutput = 0;
	}
	
	public void reset() {
		heat = 0;
		powerOutput = 0.1f;
		control = 0.1f;//range 0 - 1
		heatGain = 0.3f;
		heatDissipation = 0.2f;//environment settings range 0 - 1
		totalOutput = 0;
	}
	
	public double[] factor() {
		return new double[]{heat, control, heatGain, heatDissipation};
	}
	
	public boolean reactorFuckingExploded() {
		return getHeat() == Double.MIN_NORMAL;
	}
	
	public void update() {
		heat += heatGain * control;
		powerOutput = heat;
		if (Double.isNaN(powerOutput)) powerOutput = 0;
		totalOutput += powerOutput;
		heat -= (heat * heatDissipation) / heatGain * heatDissipation;//more heat = more dissipation
		if (heat > 1) {
			powerOutput = Double.MIN_NORMAL;
			heat = Double.MIN_NORMAL;
			control = Double.MIN_NORMAL;
		}else if (heat < 0) heat = 0.1f;
		iteration++;
	}
	
	public long getInteraction() {
		return interaction;
	}
	
	public long getIteration() {
		return iteration;
	}
	
	public double getTotalOutput() {
		return totalOutput;
	}
	
	public int getMegawattTotalOutput() {
		return (int) (totalOutput * factorPower);
	}
	
	public int getMegawattOutput() {
		return (int) (getPowerOutput() * factorPower);
	}
	
	public String about() {
		StringBuilder sb = new StringBuilder();
		sb.append("Heat Gain Index: ").append(heatGain).append(System.lineSeparator());
		sb.append("Heat Dissipation Index: ").append(heatDissipation).append(System.lineSeparator());
		sb.append("Reactor Capacity: ").append(factorPower).append(" MW").append(System.lineSeparator());
		sb.append("Total Produced: ").append(getMegawattTotalOutput()).append(" MW").append(System.lineSeparator());
		sb.append("Total Profit: ").append((int) (getPayout() * factorPower)).append(" Million Dollar").append(System.lineSeparator());
		return sb.toString();
	}
	
	public double getHeat() {
		return heat;
	}
	
	public void lowerControlRod() {
		setControl(control - 0.1f);
	}
	
	public void raiseControlRod() {
		setControl(control + 0.1f);
	}
	
	public double getPowerOutput() {
		return powerOutput;
	}
	
	public double getControl() {
		return control;
	}
	
	public Reactor setControl(double control) {
		interaction++;
		control = Math.min(control, 1);
		control = Math.max(0, control);
		this.control = control;
		return this;
	}
	
	public Reactor setHeatGain(double heatGain) {
		this.heatGain = heatGain;
		return this;
	}
	
	
	public Reactor setHeatDissipation(double heatDissipation) {
		this.heatDissipation = heatDissipation;
		return this;
	}
	
	public double getPayout() {
		return reactorFuckingExploded() ? -100f / getIteration() : getTotalOutput() - ((double) getIteration() / factorPower);
	}
}
