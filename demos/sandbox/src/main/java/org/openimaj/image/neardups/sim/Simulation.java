package org.openimaj.image.neardups.sim;

import java.util.Random;

import org.openimaj.image.MBFImage;


public abstract class Simulation {
	Random random;
	
	public Simulation(int seed) {
		this.random = new Random(seed);
	}
	
	protected final float randomFloatInRange(float min, float max) {
		float rnd = random.nextFloat() * (max - min);
		return rnd + min;
	}
	
	protected final int randomIntInRange(int min, int max) {
		return random.nextInt(max-min) + min;
	}
	
	public abstract MBFImage applySimulation(MBFImage input);
}
