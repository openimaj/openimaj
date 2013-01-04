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

package org.openimaj.image.analysis.algorithm;

import java.util.ArrayList;
import java.util.List;

import org.openimaj.feature.DoubleFV;
import org.openimaj.feature.FeatureVectorProvider;
import org.openimaj.feature.MultidimensionalDoubleFV;
import org.openimaj.image.FImage;
import org.openimaj.image.analyser.ImageAnalyser;
import org.openimaj.image.pixel.ConnectedComponent;
import org.openimaj.image.processing.edges.CannyEdgeDetector2;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.math.statistics.distribution.Histogram;

/**
 * Uses the Edge Direction Coherence Histograms to attempt to classify an image
 * as city or landscape. This uses the coherent edge histogram technique
 * described in "On Image Classification: City Images vs. Landscapes" by
 * Vailaya, Jain and Zhang, Michigan State University.
 * 
 * @author David Dupplaw (dpd@ecs.soton.ac.uk), 7th July 2005
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
@SuppressWarnings("deprecation")
public class EdgeDirectionCoherenceVector
		implements ImageAnalyser<FImage>, FeatureVectorProvider<DoubleFV>
{
	/**
	 * An edge direction histogram. Contains two histograms: one for coherent
	 * edges and one for incoherent edges.
	 * 
	 * @author David Dupplaw (dpd@ecs.soton.ac.uk)
	 * @created 10 Jun 2011
	 * 
	 */
	public class EdgeDirectionCoherenceHistogram
	{
		/** The coherent part of the histogram */
		public Histogram coherentHistogram = null;

		/** The incoherent part of the histogram */
		public Histogram incoherentHistogram = null;

		/**
		 * Get the histogram (coherent followed by incoherent) as a double
		 * vector.
		 * 
		 * @return A {@link DoubleFV} feature vector
		 */
		public DoubleFV asDoubleFV()
		{
			final double[] d = new double[coherentHistogram.values.length +
					incoherentHistogram.values.length];
			int i = 0;
			for (final double dd : coherentHistogram.asDoubleVector())
				d[i++] = dd;
			for (final double dd : incoherentHistogram.asDoubleVector())
				d[i++] = dd;
			return new DoubleFV(d);
		}

		/**
		 * Get the histogram as a multidimensional vector, where the coherent
		 * and incoherent histograms occupy different dimensions. So the vector
		 * will be 2xnBins.
		 * 
		 * @return A {@link MultidimensionalDoubleFV}
		 */
		public MultidimensionalDoubleFV asMultidimensionalDoubleFV()
		{
			final double[][] d = new double[2][coherentHistogram.values.length];
			int i = 0;
			for (final double dd : coherentHistogram.asDoubleVector())
				d[0][i++] = dd;
			i = 0;
			for (final double dd : incoherentHistogram.asDoubleVector())
				d[1][i++] = dd;
			return new MultidimensionalDoubleFV(d);
		}
	}

	/** The calculated direction histograms */
	private EdgeDirectionCoherenceHistogram coDirHist = null;

	/** Number of bins in each histogram */
	private int numberOfDirBins = 72;

	/** The direction threshold for considering an edge is coherent */
	private float directionThreshold = 360 / numberOfDirBins;

	/** The connect mode for tracing edges */
	private ConnectedComponent.ConnectMode mode =
			ConnectedComponent.ConnectMode.CONNECT_8;

	/**
	 * The factor of the image are size over which edges are considered
	 * coherent. In other words, an edge is coherent if the number of pixels in
	 * the edge is greater than image_width * image_height * coherenceFactor.
	 */
	private double coherenceFactor = 0.00002;

	/** The edge detector used */
	private CannyEdgeDetector2 cannyEdgeDetector = null;

	/**
	 * Default constructor
	 */
	public EdgeDirectionCoherenceVector()
	{
		cannyEdgeDetector = new CannyEdgeDetector2();
	}

	/**
	 * @return numberOfDirBins
	 */
	public int getNumberOfDirBins()
	{
		return numberOfDirBins;
	}

	/**
	 * Set the number of bins.
	 * 
	 * @param nb
	 *            the number of bins
	 */
	public void setNumberOfBins(int nb)
	{
		this.numberOfDirBins = nb;
		this.directionThreshold = 360 / numberOfDirBins;
	}

	/**
	 * @return coDirHist
	 */
	public EdgeDirectionCoherenceHistogram getLastHistogram()
	{
		return coDirHist;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openimaj.image.analyser.ImageAnalyser#analyseImage(org.openimaj.image
	 * .Image)
	 */
	@Override
	public void analyseImage(FImage image)
	{
		final int w = image.getWidth();
		final int h = image.getHeight();

		// Calculate the edge image.
		final FImage edgeImage = image.clone();
		cannyEdgeDetector.processImage(edgeImage);

		final float[] mags = cannyEdgeDetector.getMagnitude();
		final float[] dirs = cannyEdgeDetector.getOrientation();

		// Check we've got some stuff to work on
		if (mags == null || dirs == null)
			System.out.println("Canny Edge Detector did not " +
					"return magnitude or direction.");

		// Histogram definition. We add a bin for non-edges
		final int numberOfBins = numberOfDirBins + 1;

		// -- THE HISTOGRAM --
		final double[] dirHist = new double[numberOfBins];

		// Count the number of non-edge pixels. Edges are white from this
		// CannyEdgeDetector2, non-edges value 0.
		int nonEdgeCount = 0;
		for (int y = 0; y < edgeImage.getHeight(); y++)
			for (int x = 0; x < edgeImage.getWidth(); x++)
				if (edgeImage.getPixel(x, y) == 0)
					nonEdgeCount++;
		dirHist[0] = nonEdgeCount;

		// Bin all the directions. We use bin 0 for non-edge pixels
		// and bin i+1 for the direction i. We then back-project on to an
		// image so that we can trace the edges later.
		final FImage directionImage = new FImage(w, h);
		for (int j = 0; j < w * h; j++)
		{
			final int x = j % w;
			final int y = j / w;

			if (edgeImage.getPixel(x, y) > 0)
			{
				// dirs[j] is between -180 and 180
				// Bin the direction of the pixel
				final int dirBin = (int) ((dirs[j] + 180) * numberOfDirBins / 360f) % numberOfDirBins;
				dirHist[dirBin + 1]++;

				final float v = (dirs[j] + 180);
				directionImage.setPixel(x, y, v);
			}
			// Set the non-edge pixel to -1
			else
				directionImage.setPixel(x, y, -1f);
		}

		final int numberOfEdgePix = w * h - nonEdgeCount;

		// -- NORMALISE HISTOGRAM --
		for (int j = 0; j < numberOfDirBins; j++)
			dirHist[j + 1] /= numberOfEdgePix;
		dirHist[0] /= w * h;

		// Now to work out the coherency of the edge pixels.
		// To do this we go to a random edge pixel, and attempt
		// to trace from there to somewhere else. We check that
		// the direction is within 5 degrees of the first pixel.
		// We keep a vector of these pixels, and when the iteration
		// finished (run out of edge pixels, or it goes outside our
		// bin), then we determine whether it's coherent or not, based
		// on the number of pixels within the connected set.
		//
		// To make all this easier, we back projected the direction
		// histogram onto another image (bi). As we use a pixel we
		// remove it from bi, so that we don't get caught in loops, etc.
		// We can't check the BP-image intensities directly (although
		// it seems at first pragmatic) because of the "binning-problem"
		// where pixels may sit right on the edge of a histogram bin.

		// -- THE COHERENCE HISTOGRAM --
		// 0 is incoherent
		// 1 is coherent
		coDirHist = new EdgeDirectionCoherenceHistogram();
		coDirHist.coherentHistogram = new Histogram(numberOfDirBins);
		coDirHist.incoherentHistogram = new Histogram(numberOfDirBins);

		// Coherent Edge Image (only coherent edges displayed)
		final FImage outputImage = new FImage(w, h);

		// First we find an edge pixel
		for (int j = 0; j < w * h; j++)
		{
			final int x = j % w;
			final int y = j / w;

			// Get the back projected edge pixel
			final float p = directionImage.getPixel(x, y);

			// in bi, non-edge pixels are set to 0x00000000 (transparent black)
			// which allows discretion between non-transparent black edge pixels
			if (p != -1)
			{
				// Get the edges connected to the current point.
				final List<Point2d> v = getConnectedEdges(x, y, w, h, p,
						numberOfBins, directionImage, dirs, mode);

				// dirs[j] is between -180 and 180
				final int dirBin = (int) ((dirs[j] + 180)
						* numberOfDirBins / 360f) % numberOfDirBins;

				// If the edge is coherent...
				boolean isCoherent = false;
				if (v.size() > (w * h * coherenceFactor))
				{
					for (int k = 0; k < v.size(); k++)
					{
						final Point2d pp = v.get(k);
						outputImage.setPixel(
								Math.round(pp.getX()),
								Math.round(pp.getY()),
								1f);
					}

					isCoherent = true;
				}

				if (isCoherent)
					coDirHist.coherentHistogram.values[dirBin] += v.size();
				else
					coDirHist.incoherentHistogram.values[dirBin] += v.size();
			}
		}

		image.internalAssign(outputImage);
	}

	/**
	 * Function that given a pixel at x, y with value p, in image bi, it will
	 * find all connected edges that fall within the same bin.
	 * 
	 * @param xx
	 *            The x coordinate of the seed edge pixel
	 * @param yy
	 *            The y coordinate of the seed edge pixel
	 * @param w
	 *            The width of the edge image (required to index directions
	 *            array)
	 * @param h
	 *            The height of the edge image
	 * @param p
	 *            The intensity of the given pixel
	 * @param numberOfBins
	 *            Number of bins in the edge histogram (to work out direction)
	 * @param edgeImage
	 *            The back-projected edge image
	 * @param dirs
	 *            The original edge directions map
	 * @param connectedness
	 *            4 or 8-connected
	 */
	private List<Point2d> getConnectedEdges(int xx, int yy, int w, int h, float p,
			int numberOfBins, FImage edgeImage, float[] dirs,
			ConnectedComponent.ConnectMode connectedness)
	{
		final List<Point2d> v = new ArrayList<Point2d>();

		// The original point is always in the final set
		v.add(new Point2dImpl(xx, yy));

		// Pixels are wiped out as they're traced. So we wipe out the
		// first pixel where we start.
		edgeImage.setPixel(xx, yy, -1f);

		final float dir = dirs[yy * w + xx];
		boolean connected = true;
		int x = xx, y = yy;
		while (connected)
		{
			int nx = x, ny = y;

			switch (connectedness)
			{
			// Check 4-connected neighbourhood
			case CONNECT_4:
				nx = x + 1;
				ny = y;
				if (nx >= 0 && ny >= 0 && nx < w && ny < h &&
						dirs[ny * w + nx] < dir + directionThreshold &&
						dirs[ny * w + nx] > dir - directionThreshold &&
						edgeImage.getPixel(nx, ny) != -1)
					break;
				nx = x;
				ny = y + 1;
				if (nx >= 0 && ny >= 0 && nx < w && ny < h &&
						dirs[ny * w + nx] < dir + directionThreshold &&
						dirs[ny * w + nx] > dir - directionThreshold &&
						edgeImage.getPixel(nx, ny) != -1)
					break;
				nx = x - 1;
				ny = y;
				if (nx >= 0 && ny >= 0 && nx < w && ny < h &&
						dirs[ny * w + nx] < dir + directionThreshold &&
						dirs[ny * w + nx] > dir - directionThreshold &&
						edgeImage.getPixel(nx, ny) != -1)
					break;
				nx = x;
				ny = y - 1;
				if (nx >= 0 && ny >= 0 && nx < w && ny < h &&
						dirs[ny * w + nx] < dir + directionThreshold &&
						dirs[ny * w + nx] > dir - directionThreshold &&
						edgeImage.getPixel(nx, ny) != -1)
					break;
				nx = x;
				ny = y;
				break;

			// Check 8-connected neighbourhood
			case CONNECT_8:
				nx = x + 1;
				ny = y - 1;
				if (nx >= 0 && ny >= 0 && nx < w && ny < h &&
						dirs[ny * w + nx] < dir + directionThreshold &&
						dirs[ny * w + nx] > dir - directionThreshold &&
						edgeImage.getPixel(nx, ny) != -1)
					break;
				nx = x + 1;
				ny = y;
				if (nx >= 0 && ny >= 0 && nx < w && ny < h &&
						dirs[ny * w + nx] < dir + directionThreshold &&
						dirs[ny * w + nx] > dir - directionThreshold &&
						edgeImage.getPixel(nx, ny) != -1)
					break;
				nx = x + 1;
				ny = y + 1;
				if (nx >= 0 && ny >= 0 && nx < w && ny < h &&
						dirs[ny * w + nx] < dir + directionThreshold &&
						dirs[ny * w + nx] > dir - directionThreshold &&
						edgeImage.getPixel(nx, ny) != -1)
					break;
				nx = x;
				ny = y + 1;
				if (nx >= 0 && ny >= 0 && nx < w && ny < h &&
						dirs[ny * w + nx] < dir + directionThreshold &&
						dirs[ny * w + nx] > dir - directionThreshold &&
						edgeImage.getPixel(nx, ny) != -1)
					break;
				nx = x - 1;
				ny = y + 1;
				if (nx >= 0 && ny >= 0 && nx < w && ny < h &&
						dirs[ny * w + nx] < dir + directionThreshold &&
						dirs[ny * w + nx] > dir - directionThreshold &&
						edgeImage.getPixel(nx, ny) != -1)
					break;
				nx = x - 1;
				ny = y;
				if (nx >= 0 && ny >= 0 && nx < w && ny < h &&
						dirs[ny * w + nx] < dir + directionThreshold &&
						dirs[ny * w + nx] > dir - directionThreshold &&
						edgeImage.getPixel(nx, ny) != -1)
					break;
				nx = x - 1;
				ny = y - 1;
				if (nx >= 0 && ny >= 0 && nx < w && ny < h &&
						dirs[ny * w + nx] < dir + directionThreshold &&
						dirs[ny * w + nx] > dir - directionThreshold &&
						edgeImage.getPixel(nx, ny) != -1)
					break;
				nx = x;
				ny = y - 1;
				if (nx >= 0 && ny >= 0 && nx < w && ny < h &&
						dirs[ny * w + nx] < dir + directionThreshold &&
						dirs[ny * w + nx] > dir - directionThreshold &&
						edgeImage.getPixel(nx, ny) != -1)
					break;
				nx = x;
				ny = y;
				break;
			}

			if ((nx >= 0 && nx != x) || (ny >= 0 && ny != y))
			{
				v.add(new Point2dImpl(nx, ny));
				edgeImage.setPixel(nx, ny, -1f);
				x = nx;
				y = ny;
			}
			else
				connected = false;
		}
		return v;
	}

	/**
	 * Returns the edge direction coherence histogram that was calculated.
	 * 
	 * @return the edge direction coherence histogram.
	 */
	public EdgeDirectionCoherenceHistogram getHistogram()
	{
		return coDirHist;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.openimaj.feature.FeatureVectorProvider#getFeatureVector()
	 */
	@Override
	public DoubleFV getFeatureVector()
	{
		return coDirHist.asMultidimensionalDoubleFV();
	}

	/**
	 * Get the edge detector used.
	 * 
	 * @return the canny edge detector
	 */
	public CannyEdgeDetector2 getCannyEdgeDetector()
	{
		return cannyEdgeDetector;
	}

	/**
	 * Get the edge coherence factor. This is the relative size of the edge
	 * compared to the image over which an edge will be considered coherent and
	 * is generally a very small number. The default is 0.00002.
	 * 
	 * @return the coherence factor
	 */
	public double getCoherenceFactor()
	{
		return coherenceFactor;
	}

	/**
	 * Set the edge coherence factor. This is the relative size of the edge
	 * compared to the image over which an edge will be considered coherent and
	 * is generally a very small number. The default is 0.00002.
	 * 
	 * @param coherenceFactor
	 *            the coherence factor value
	 */
	public void setCoherenceFactor(double coherenceFactor)
	{
		this.coherenceFactor = coherenceFactor;
	}
}
