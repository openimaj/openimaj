package org.openimaj.image.neardups.sim;

import org.openimaj.image.MBFImage;
import org.openimaj.image.processing.transform.ProjectionProcessor;
import org.openimaj.math.geometry.transforms.TransformUtilities;

public class ArbitaryRotateSimulation extends Simulation {
	public ArbitaryRotateSimulation(int seed) {
		super(seed);
	}

	@Override
	public MBFImage applySimulation(MBFImage input) {
		float angle = randomFloatInRange(0, (float) (2*Math.PI));
		
		float [] background = new float[input.numBands()];

		for (int i=0; i<input.numBands(); i++) {
			background[i] = random.nextFloat();
		}
		
		
		return ProjectionProcessor.project(input, TransformUtilities.rotationMatrix(angle));
	}

}
