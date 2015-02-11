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
package org.openimaj.image.analysis.algorithm.histogram;

import org.openimaj.image.FImage;
import org.openimaj.image.analyser.ImageAnalyser;
import org.openimaj.image.processor.ImageProcessor;
import org.openimaj.math.statistics.distribution.Histogram;

/**
 * An {@link ImageAnalyser} that processes an image and generates a
 * {@link Histogram}.
 * <p>
 * You can get the histogram for an image like so: <code><pre>
 * 	{@code
 * 		FImage img = new FImage( ... );
 * 		HistogramProcessor hp = new HistogramProcessor( 64 );
 * 		img.analyse( hp );
 * 		Histogram h = hp.getHistogram();
 * 	}
 * 	</pre></code>
 *
 * @see FImage#process(ImageProcessor)
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class HistogramAnalyser implements ImageAnalyser<FImage>
{
	/** The number of bins in the histogram */
	private int nbins;

	/** The histogram being built */
	private Histogram histogram;

	/**
	 * Default constructor that builds a histogram processor that will create
	 * histograms with the given number of bins.
	 *
	 * @param nbins
	 *            The number of bins.
	 */
	public HistogramAnalyser(int nbins) {
		this.nbins = nbins;
	}

	/**
	 * Computes the Histogram for this image. The assumption is that the image
	 * has been normalised to the range 0..1. Values greater than 1 will be
	 * placed in the top bin.
	 *
	 * @param image
	 *            The image from which to extract histogram
	 */
	@Override
	public void analyseImage(FImage image) {
		this.histogram = new Histogram(nbins);
		for (int r = 0; r < image.height; r++)
		{
			for (int c = 0; c < image.width; c++)
			{
				int bin = (int) (image.pixels[r][c] * nbins);
				if (bin > (nbins - 1))
					bin = nbins - 1;
				histogram.values[bin]++;
			}
		}
	}

	/**
	 * Returns the histogram that was built having run the processing function.
	 * This will return null if the processing has not yet been run.
	 *
	 * @return The {@link Histogram} that was built.
	 */
	public Histogram getHistogram()
	{
		return histogram;
	}

	/**
	 * Quickly create a histogram from an image.
	 *
	 * @param image
	 *            the image
	 * @param nbins
	 *            the number of bins per band
	 * @return a histogram
	 */
	public static Histogram getHistogram(FImage image, int nbins) {
		final HistogramAnalyser p = new HistogramAnalyser(nbins);
		image.analyseWith(p);
		return p.getHistogram();
	}
}
