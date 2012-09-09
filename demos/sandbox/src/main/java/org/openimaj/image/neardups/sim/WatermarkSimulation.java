package org.openimaj.image.neardups.sim;

import java.io.IOException;

import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.processing.resize.ResizeProcessor;

public class WatermarkSimulation extends Simulation {
	protected final static float minAlpha = 0.4f;
	protected final static float maxAlpha = 1.0f;
	protected final String[] watermarks = { "sotonimages.png", "logo.png" };

	public WatermarkSimulation(int seed) {
		super(seed);
	}

	@Override
	public MBFImage applySimulation(MBFImage input) {
		try {
			final String wmark = watermarks[random.nextInt(watermarks.length)];

			final MBFImage watermark = ImageUtilities.readMBFAlpha(WatermarkSimulation.class
					.getResourceAsStream("/org/openimaj/image/neardups/" + wmark));

			if (watermark.getHeight() > input.getHeight()) {
				final int newY = (input.getHeight() - 10);
				final int newX = (int) (watermark.getWidth() * ((double) newY / (double) watermark.getHeight()));
				watermark.processInplace(new ResizeProcessor(newX, newY));
			}

			if (watermark.getWidth() > input.getWidth()) {
				final int newX = (input.getWidth() - 10);
				final int newY = (int) (watermark.getHeight() * ((double) newX / (double) watermark.getWidth()));
				watermark.processInplace(new ResizeProcessor(newX, newY));
			}

			final float alpha = randomFloatInRange(minAlpha, maxAlpha);
			watermark.getBand(3).multiplyInplace(alpha);
			final MBFImage output = input.clone();
			output.drawImage(watermark, (input.getWidth() / 2) - (watermark.getWidth() / 2), (input.getHeight() / 2)
					- (watermark.getHeight() / 2));
			return output;

		} catch (final IOException e) {
			throw new Error(e);
		}
	}
}
