package org.openimaj.image.neardups.sim;

import org.openimaj.image.MBFImage;

/**
 * Simulate rotations by 90,180,270,0 degrees
 * 
 * @author jsh2
 *
 */
public class Rotate90Simulation extends Simulation {
	public Rotate90Simulation(int seed) {
		super(seed);
	}

	@Override
	public MBFImage applySimulation(MBFImage input) {
		int angle = random.nextInt(4);
		MBFImage output = null;
		
		switch (angle) {
		case 0:
			output = input.clone();
			break;
		case 1:
			output = new MBFImage(input.getHeight(), input.getWidth(), input.numBands());
			for (int j=0; j<output.getHeight(); j++) {
				for (int i=0; i<output.getWidth(); i++) {
					output.setPixel(i, j, input.getPixel(j, i));
				}
			}
			break;
		case 2:
			output = new MBFImage(input.getWidth(), input.getHeight(), input.numBands());
			for (int j=0; j<output.getHeight(); j++) {
				for (int i=0; i<output.getWidth(); i++) {
					output.setPixel(i, j, input.getPixel(i, input.getHeight() - j - 1));
				}
			}
			break;
		case 3:
			output = new MBFImage(input.getHeight(), input.getWidth(), input.numBands());
			for (int j=0; j<output.getHeight(); j++) {
				for (int i=0; i<output.getWidth(); i++) {
					output.setPixel(i, j, input.getPixel(input.getWidth() - j - 1, i));
				}
			}
			break;
		}
		
		return output;
	}

}
