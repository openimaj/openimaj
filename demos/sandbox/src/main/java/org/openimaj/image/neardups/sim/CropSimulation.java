package org.openimaj.image.neardups.sim;

import org.openimaj.image.MBFImage;

public class CropSimulation extends Simulation {
	protected float minWidth = 0.1f;
	protected float minHeight = 0.1f;
	
	public CropSimulation(int seed) {
		super(seed);
	}
	
	public CropSimulation(int seed, float minWidth, float minHeight) {
		super(seed);
		this.minHeight = minHeight;
		this.minWidth = minWidth;
	}

	@Override
	public MBFImage applySimulation(MBFImage input) {
		int width = randomIntInRange((int) (minWidth*input.getWidth()), input.getWidth());
		int height = randomIntInRange((int) (minHeight*input.getHeight()), input.getHeight());
		
		int x = random.nextInt(input.getWidth() - width);
		int y = random.nextInt(input.getHeight() - height);
		
		return input.extractROI(x, y, width, height);
	}
}
