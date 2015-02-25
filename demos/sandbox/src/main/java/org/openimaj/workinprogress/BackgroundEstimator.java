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
import java.util.ArrayList;
import java.util.List;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.math.util.FloatArrayStatsUtils;
import org.openimaj.video.xuggle.XuggleVideo;

public class BackgroundEstimator {
	public static void main(String[] args) throws IOException {
		final XuggleVideo xv = new XuggleVideo(new File("/Users/jon/Desktop/merlin/tunnel.mp4"));

		final List<MBFImage> frameSample = new ArrayList<MBFImage>();
		int count = 0;
		for (final MBFImage fr : xv) {
			if (count % 30 == 0)
				System.out.println(count / 30);

			if (count % 30 == 0)
				frameSample.add(fr.clone());
			count++;
		}

		final MBFImage img = new MBFImage(frameSample.get(0).getWidth(), frameSample.get(0).getHeight());
		final float[] vec = new float[frameSample.size()];
		for (int y = 0; y < img.getHeight(); y++) {
			for (int x = 0; x < img.getWidth(); x++) {
				for (int b = 0; b < 3; b++) {
					for (int i = 0; i < frameSample.size(); i++)
						vec[i] = frameSample.get(i).getBand(b).pixels[y][x];

					img.bands.get(b).pixels[y][x] = FloatArrayStatsUtils.median(vec);
				}
			}
		}

		DisplayUtilities.display(img);
		ImageUtilities.write(img, new File("/Users/jon/Desktop/merlin/tunnel-background.png"));
	}
}
