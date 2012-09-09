package org.openimaj.image.neardups.sim;

import org.openimaj.image.FImage;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.Transforms;

public class GreyscaleSimulation extends Simulation {

	public GreyscaleSimulation(int seed) {
		super(seed);
	}

	@Override
	public MBFImage applySimulation(MBFImage input) {
		FImage gs;
		
		if (random.nextBoolean())
			gs = Transforms.calculateIntensityNTSC(input);
		else
			gs = Transforms.calculateIntensity(input);

		return new MBFImage(gs, gs, gs);
	}
}
