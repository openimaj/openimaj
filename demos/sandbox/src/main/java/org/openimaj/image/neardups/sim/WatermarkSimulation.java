/**
 * Copyright (c) 2011, The University of Southampton and the individual contributors.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *   * 	Redistributions of source code must retain the above copyright notice,
 * 	this list of conditions and the following disclaimer.
 *
 *   *	Redistributions in binary form must reproduce the above copyright notice,
 * 	this list of conditions and the following disclaimer in the documentation
 * 	and/or other materials provided with the distribution.
 *
 *   *	Neither the name of the University of Southampton nor the names of its
 * 	contributors may be used to endorse or promote products derived from this
 * 	software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
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
