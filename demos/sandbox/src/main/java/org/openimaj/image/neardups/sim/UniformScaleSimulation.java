package org.openimaj.image.neardups.sim;

import org.openimaj.image.MBFImage;
import org.openimaj.image.processing.resize.ResizeProcessor;

public class UniformScaleSimulation extends Simulation {
	protected final static float minScaleFactor = 0.1f;
	protected final static float maxScaleFactor = 2.0f;
	
	public UniformScaleSimulation(int seed) {
		super(seed);
	}

	@Override
	public MBFImage applySimulation(MBFImage input) {
		float sf = randomFloatInRange(minScaleFactor, maxScaleFactor); 
		
		int newX = Math.round(sf * input.getWidth());
		int newY = Math.round(sf * input.getHeight());
		
		return input.process( new ResizeProcessor(newX, newY, true));
	}
}
