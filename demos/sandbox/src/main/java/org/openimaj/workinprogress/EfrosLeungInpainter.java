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
import java.util.Collections;
import java.util.List;

import org.openimaj.citation.annotation.Reference;
import org.openimaj.citation.annotation.ReferenceType;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.Image;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.pixel.FValuePixel;
import org.openimaj.image.pixel.Pixel;
import org.openimaj.image.processing.convolution.Gaussian2D;
import org.openimaj.image.processing.morphology.Erode;
import org.openimaj.image.processing.morphology.StructuringElement;
import org.openimaj.image.processing.restoration.inpainting.AbstractImageMaskInpainter;
import org.openimaj.image.processor.SinglebandImageProcessor;

/**
 * FIXME: Finish implementation (it works but is incredibly slow!)
 *
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 * @param <IMAGE>
 *            The type of image that this processor can process
 */
@Reference(
		type = ReferenceType.Inproceedings,
		author = { "Efros, Alexei A.", "Leung, Thomas K." },
		title = "Texture Synthesis by Non-Parametric Sampling",
		year = "1999",
		booktitle = "Proceedings of the International Conference on Computer Vision-Volume 2 - Volume 2",
		pages = { "1033" },
		url = "http://dl.acm.org/citation.cfm?id=850924.851569",
		publisher = "IEEE Computer Society",
		series = "ICCV '99",
		customData = {
				"isbn", "0-7695-0164-8",
				"acmid", "851569",
				"address", "Washington, DC, USA"
		})
public class EfrosLeungInpainter<IMAGE extends Image<?, IMAGE> & SinglebandImageProcessor.Processable<Float, FImage, IMAGE>>
		extends
		AbstractImageMaskInpainter<IMAGE>
{
	private static final float SIGMA_DIVISOR = 6.4f;
	private static final float ERR_THRESHOLD = 0.1f;
	private static final float INITIAL_MAX_ERR_THRESHOLD = 0.3f;

	int windowHalfSize;
	float sigma;
	FImage template;
	FImage gaussian;
	float templateWeight;

	public EfrosLeungInpainter(int windowSize) {
		this.windowHalfSize = windowSize / 2;
		this.sigma = windowSize / SIGMA_DIVISOR;
		this.gaussian = Gaussian2D.createKernelImage(windowSize, sigma);
	}

	List<FValuePixel> getUnfilledNeighbours() {
		final List<FValuePixel> pixels = new ArrayList<FValuePixel>();

		final FImage outside = mask.process(new Erode(StructuringElement.CROSS), true);

		for (int y = 0; y < mask.height; y++) {
			for (int x = 0; x < mask.width; x++) {
				final int band = (int) (outside.pixels[y][x] - mask.pixels[y][x]);
				if (band != 0) {
					final float nc = countValidNeighbours(x, y);
					if (nc > 0)
						pixels.add(new FValuePixel(x, y, nc));
				}
			}
		}

		Collections.shuffle(pixels);
		Collections.sort(pixels, FValuePixel.ReverseValueComparator.INSTANCE);

		return pixels;
	}

	private float countValidNeighbours(int x, int y) {
		int count = 0;
		for (int yy = Math.max(0, y - windowHalfSize); yy < Math.min(mask.height, y + windowHalfSize + 1); yy++)
			for (int xx = Math.max(0, x - windowHalfSize); xx < Math.min(mask.width, x + windowHalfSize + 1); xx++)
				count += (1 - mask.pixels[yy][xx]);

		return count;
	}

	@Override
	protected void performInpainting(IMAGE image) {
		if (image instanceof FImage)
			performInpainting((FImage) image);
	}

	protected void performInpainting(FImage image) {
		this.template = image.newInstance(windowHalfSize * 2 + 1, windowHalfSize * 2 + 1);
		float maxErrThreshold = INITIAL_MAX_ERR_THRESHOLD;

		while (true) {
			final List<FValuePixel> pixelList = getUnfilledNeighbours();

			if (pixelList.size() == 0)
				return;

			boolean progress = false;
			for (final Pixel p : pixelList) {
				// template = getNeighborhoodWindow(Pixel);
				setTemplate(p.x, p.y, image);
				final List<FValuePixel> bestMatches = findMatches(image);
				final FValuePixel bestMatch = bestMatches.get((int) (Math.random() * bestMatches.size()));
				if (bestMatch.value < maxErrThreshold) {
					image.pixels[p.y][p.x] = image.pixels[bestMatch.y][bestMatch.x];
					mask.pixels[p.y][p.x] = 0;
					progress = true;
					DisplayUtilities.displayName(image, "");
					System.out.println(p);
				}
			}

			if (!progress)
				maxErrThreshold *= 1.1;

		}

	}

	private void setTemplate(int x, int y, FImage image) {
		this.templateWeight = 0;
		template.fill(Float.MAX_VALUE);
		for (int j = 0, yy = y - windowHalfSize; yy < y + windowHalfSize + 1; yy++, j++) {
			for (int i = 0, xx = x - windowHalfSize; xx < x + windowHalfSize + 1; xx++, i++) {
				if (xx >= 0 && xx < mask.width && yy >= 0 && yy < mask.height &&
						mask.pixels[yy][xx] == 0)
				{
					template.pixels[j][i] = image.pixels[yy][xx];
					this.templateWeight += this.gaussian.pixels[j][i];
				}
			}
		}
	}

	List<FValuePixel> findMatches(FImage image) {
		final FImage ssd = new FImage(mask.width, mask.height);

		float minSSD = Float.MAX_VALUE;
		for (int y = windowHalfSize; y < mask.height - windowHalfSize - 1; y++) {
			for (int x = windowHalfSize; x < mask.width - windowHalfSize - 1; x++) {

				ssd.pixels[y][x] = 0;
				masked: for (int j = -windowHalfSize, jj = 0; j <= windowHalfSize; j++, jj++) {
					for (int i = -windowHalfSize, ii = 0; i <= windowHalfSize; i++, ii++) {
						final float tpix = template.pixels[jj][ii];
						final float ipix = image.pixels[y + j][x + i];

						if (mask.pixels[y + j][x + i] == 1) {
							ssd.pixels[y][x] = Float.MAX_VALUE;
							break masked;
						} else if (tpix != Float.MAX_VALUE) {
							ssd.pixels[y][x] += ((tpix - ipix) * (tpix - ipix)) * gaussian.pixels[jj][ii];
						}
					}
				}
				if (ssd.pixels[y][x] != Float.MAX_VALUE) {
					// if (ssd.pixels[y][x] == 0)
					// System.out.println("here");
					ssd.pixels[y][x] /= this.templateWeight;
					minSSD = Math.min(minSSD, ssd.pixels[y][x]);
				}
			}
		}

		final float thresh = minSSD * (1 + ERR_THRESHOLD);
		final List<FValuePixel> pixelList = new ArrayList<FValuePixel>();
		for (int y = windowHalfSize; y < mask.height - windowHalfSize - 1; y++) {
			for (int x = windowHalfSize; x < mask.width - windowHalfSize - 1; x++) {
				if (ssd.pixels[y][x] != Float.MAX_VALUE && ssd.pixels[y][x] <= thresh)
					pixelList.add(new FValuePixel(x, y, ssd.pixels[y][x]));
			}
		}
		return pixelList;
	}

	public static void main(String[] args) throws IOException {
		final EfrosLeungInpainter<FImage> ip = new EfrosLeungInpainter<FImage>(7);
		final FImage mask = ImageUtilities.readF(new File("/Users/jsh2/veg.PNG"));
		final FImage image = ImageUtilities.readF(new File("/Users/jsh2/veg-masked.png"));

		ip.setMask(mask);
		ip.processImage(image);

		DisplayUtilities.display(image);
	}
}
