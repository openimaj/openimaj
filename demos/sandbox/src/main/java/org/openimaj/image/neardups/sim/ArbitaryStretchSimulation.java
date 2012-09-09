package org.openimaj.image.neardups.sim;

import org.openimaj.image.MBFImage;
import org.openimaj.image.processing.resize.ResizeProcessor;

public class ArbitaryStretchSimulation extends Simulation {
	protected float minScaleFactor = 0.1f;
	protected float maxScaleFactor = 2.0f;
	
	public ArbitaryStretchSimulation(int seed) {
		super(seed);
	}

	@Override
	public MBFImage applySimulation(MBFImage input) {
		float sfx = randomFloatInRange(minScaleFactor, maxScaleFactor);
		float sfy = randomFloatInRange(minScaleFactor, maxScaleFactor);
		
		int newX = Math.round(sfx * input.getWidth());
		int newY = Math.round(sfy * input.getHeight());
		
		return input.process( new ResizeProcessor(newX, newY));
	}

}
