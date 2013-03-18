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
/**
 *
 */
package org.openimaj.video.processing.shotdetector;

import org.openimaj.citation.annotation.Reference;
import org.openimaj.citation.annotation.ReferenceType;
import org.openimaj.feature.DoubleFVComparison;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.pixel.statistics.HistogramModel;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.util.array.ArrayUtils;
import org.openimaj.video.Video;

/**
 *	A shot detector implementing the Steiner et al. local histogram comparison.
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *  @created 28 Jan 2013
 *	@version $Author$, $Revision$, $Date$
 */
@Reference(
		type = ReferenceType.Inproceedings,
		author = { "{S}teiner, {T}homas", "{V}erborgh, {R}uben", "{G}abarr{\'o} {V}all{\'e}s, {J}oaquim", "{T}roncy, {R}apha{\"e}l", "{H}ausenblas, {M}ichael", "{V}an de {W}alle, {R}ik", "{B}rousseau, {A}rnaud" },
		title = "{E}nabling on-the-fly video shot detection on {Y}ou{T}ube",
		year = "2012",
		booktitle = "{WWW} 2012, 21st {I}nternational {W}orld {W}ide {W}eb {C}onference {D}eveloper's {T}rack, {A}pril 16-20, 2012, {L}yon, {F}rance",
		url = "http://www.eurecom.fr/publication/3676",
		month = "04",
		customData = {
			"address", "{L}yon, {FRANCE}"
		}
	)
public class LocalHistogramVideoShotDetector
	extends VideoShotDetector<MBFImage>
{
	/** The histogram model used to estimate the histogram for each frame */
	private final HistogramModel histogramModel = new HistogramModel( 4,4,4 );

	/** The histogram of the previous frame */
	private double[][][] lastHistogram = null;

	/** The number of grid elements in the grid */
	private int nGridElements = 20;

	/** The percentage of tiles to use as the most similar */
	private final double pcMostSimilar = 0.1;

	/** The percentage of tiles to use as the most dissimilar */
	private final double pcMostDissimilar = 0.1;

	/** The boosting factor to use */
	private final double boostFactor = 1.1;

	/** The limiting factor to use */
	private final double limitingFactor = 0.9;

	/**
	 * 	If you use this constructor, your timecodes will be messed up
	 * 	unless you call {@link #setFPS(double)} before you process
	 * 	any frames.
	 * 	@param nGridElements The number of x and y grid elements
	 *		(there will be this number squared in total)
	 */
	public LocalHistogramVideoShotDetector( final int nGridElements )
	{
		this.nGridElements = nGridElements;
		this.lastHistogram = new double[this.nGridElements][this.nGridElements][];
		this.threshold = 0.2;
	}

	/**
	 *
	 *	@param video The video to process
	 * 	@param nGridElements The number of x and y grid elements
	 *		(there will be this number squared in total)
	 */
	public LocalHistogramVideoShotDetector( final Video<MBFImage> video,
			final int nGridElements )
	{
		super( video );
		this.nGridElements = nGridElements;
		this.lastHistogram = new double[nGridElements][nGridElements][];
		this.threshold = 0.2;
	}

	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.video.processing.shotdetector.VideoShotDetector#getInterframeDistance(org.openimaj.image.Image)
	 */
	@Override
	protected double getInterframeDistance( final MBFImage frame )
	{
		// The histogram distance at each element
		final double[][] avgHisto = new double[this.nGridElements][this.nGridElements];

		// Work out the size of each grid element in pixels
		final int gw = frame.getWidth()  / this.nGridElements;
		final int gh = frame.getHeight() / this.nGridElements;

		// Loop through the grid elements
		for( int y = 0; y < this.nGridElements; y++ )
		{
			for( int x = 0; x < this.nGridElements; x++ )
			{
				// Extract the local image
				final MBFImage img = frame.extractROI( x*gw, y*gh, gw, gh );

				// Estimate the histogram
				this.histogramModel.estimateModel( img );
				final double[] histogram = this.histogramModel.
						histogram.asDoubleVector();

				// If we have a grid element histogram to compare against,
				// we will implement the algorithm
				if( this.lastHistogram[y][x] != null )
				{
					final double dist = DoubleFVComparison.EUCLIDEAN.compare(
							histogram, this.lastHistogram[y][x] );
					avgHisto[y][x] = dist;
				}

				// Store the histogram for this grid element for next time
				this.lastHistogram[y][x] = histogram;
			}

		}

		// We'll work now with a 1D array
		double[] flattenedAvgHisto = ArrayUtils.reshape( avgHisto );

		// --- Calculate most similar and dissimilar tiles for boosting ---
		// Sort the avgHisto values retaining their original indices
		final int[] indices = new int[this.nGridElements*this.nGridElements];

		// Sort the histogram distance array. The smallest values will end
		// up at the end of the array - the smallest values are the most similar.
		ArrayUtils.parallelQuicksortDescending(	flattenedAvgHisto,
				ArrayUtils.fill( indices ) );

		// Create an array with the boost/limit factors in it
		final double[][] similarDissimilar =
				new double[this.nGridElements][this.nGridElements];

		// Set the boost/limit factors
		for( int index = 0; index < indices.length; index++ )
		{
			double factor = 1;
			if( index < this.nGridElements * this.nGridElements * this.pcMostDissimilar )
					factor = this.limitingFactor;
			else
			if( index >= this.nGridElements * this.nGridElements * (1-this.pcMostSimilar) )
					factor = this.boostFactor;
			else	factor = 1;

			final int y = indices[index] / this.nGridElements;
			final int x = indices[index] % this.nGridElements;
			similarDissimilar[y][x] = factor;
		}

		// DEBUG
//		this.drawBoxes( frame, similarDissimilar );

		// Boost the avgHisto values based on the distance measures
		for( int y = 0; y < this.nGridElements; y++ )
			for( int x = 0; x < this.nGridElements; x++ )
				avgHisto[y][x] *= similarDissimilar[y][x];

		// Calculate the average histogram distance (over all the grid elements)
		flattenedAvgHisto = ArrayUtils.reshape( avgHisto );
		double avgDist = ArrayUtils.sumValues( flattenedAvgHisto );
		avgDist /= this.nGridElements * this.nGridElements;

		// Calculate the stddev
		ArrayUtils.subtract( flattenedAvgHisto, avgDist );
		final double stdDev = Math.sqrt( ArrayUtils.sumValuesSquared(
				flattenedAvgHisto ) / (this.nGridElements*this.nGridElements) );

		return stdDev;
	}

	/**
	 * 	Draws the boxes to show movements.
	 *	@param img The image to draw to
	 */
	protected void drawBoxes( final MBFImage img, final double[][] sim )
	{
		final int gw = img.getWidth()  / this.nGridElements;
		final int gh = img.getHeight() / this.nGridElements;
		for( int y = 0; y < this.nGridElements; y++ )
		{
			for( int x = 0; x < this.nGridElements; x++ )
			{
				Float[] c = new Float[]{0f,0f,0f,0f};
				if( sim[y][x] == this.boostFactor )
						c = RGBColour.RED;
				else
				if( sim[y][x] == this.limitingFactor )
						c = RGBColour.BLUE;
				else	c = RGBColour.BLACK;
				img.drawShape( new Rectangle(x*gw,y*gh,gw,gh), c );
			}
		}
	}
}
