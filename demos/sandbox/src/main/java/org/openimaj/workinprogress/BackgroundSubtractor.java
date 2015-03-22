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
package org.openimaj.workinprogress;

import java.io.File;
import java.io.IOException;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.processing.algorithm.FilterSupport;
import org.openimaj.image.processing.algorithm.MedianFilter;
import org.openimaj.image.processing.resize.ResizeProcessor;
import org.openimaj.video.xuggle.XuggleVideo;

public class BackgroundSubtractor {
	public static void main(String[] args) throws IOException {
		final XuggleVideo xv = new XuggleVideo(new File("/Users/jon/Desktop/merlin/tunnel.mp4"));
		final FImage bg = ResizeProcessor.halfSize(
				ImageUtilities.readF(new File("/Users/jon/Desktop/merlin/tunnel-background.png"))
				);

		// final XuggleVideoWriter xvw = new
		// XuggleVideoWriter("/Users/jon/Desktop/merlin/tunnel-proc.mp4",
		// bg.width,
		// bg.height, xv.getFPS());
		for (final MBFImage frc : xv) {
			final FImage fr = ResizeProcessor.halfSize(frc.flatten());
			final MBFImage diff = diff(bg, fr);

			// xvw.addFrame(diff);
			DisplayUtilities.displayName(diff, "");
		}
		// xvw.close();
	}

	static MBFImage diff(FImage bg, FImage fg) {
		final FImage df = new FImage(bg.getWidth(), bg.getHeight());
		final float[][] dff = df.pixels;

		final float[][] bgfr = bg.pixels;
		final float[][] fgfr = fg.pixels;

		for (int y = 0; y < df.getHeight(); y++) {
			for (int x = 0; x < df.getWidth(); x++) {
				final float dr = bgfr[y][x] - fgfr[y][x];
				final float ssd = dr * dr;

				if (ssd < 0.03) {
					dff[y][x] = 0;
				} else {
					dff[y][x] = 1;
				}
			}
		}

		// Dilate.dilate(df, 1);
		// Erode.erode(df, 2);
		df.processInplace(new MedianFilter(FilterSupport.createBlockSupport(3, 3)));
		df.processInplace(new MedianFilter(FilterSupport.createBlockSupport(3, 3)));

		return df.toRGB();
	}
}
