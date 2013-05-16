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
package org.openimaj.image.text.extraction;

import java.util.Map;

import org.openimaj.citation.annotation.Reference;
import org.openimaj.citation.annotation.ReferenceType;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.analysis.pyramid.PyramidProcessor;
import org.openimaj.image.analysis.pyramid.gaussian.GaussianOctave;
import org.openimaj.image.analysis.pyramid.gaussian.GaussianPyramid;
import org.openimaj.image.analysis.pyramid.gaussian.GaussianPyramidOptions;
import org.openimaj.image.processing.resize.ResizeProcessor;
import org.openimaj.math.geometry.shape.Rectangle;

/**
 *	An implementation of the multiscale text extractor from
 *
 *	MULTISCALE EDGE-BASED TEXT EXTRACTION FROM COMPLEX IMAGES;
 *	Xiaoqing Liu and Jagath Samarabandu
 *	The University of Western Ontario
 *
 *	http://ieeexplore.ieee.org/xpl/freeabs_all.jsp?arnumber=4036951.
 *	<p>
 *	This multiscale text extractor uses a Gaussian pyramid to produce the
 *	multiscale feature vector. From this, the basic text extraction algorithm
 *	is used (see the {@link LiuSamarabanduTextExtractorBasic} implementation)
 *	on each image and the results combined using across-scale addition.
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *  @created 28 Jul 2011
 *
 */
@Reference(
		type = ReferenceType.Inproceedings,
		author = { "Liu, X.", "Samarabandu, J." },
		title = "Multiscale Edge-Based Text Extraction from Complex Images",
		year = "2006",
		booktitle = "Multimedia and Expo, 2006 IEEE International Conference on",
		pages = { "1721 ", "1724" },
		month = "july",
		number = "",
		volume = "",
		customData = { "keywords", "multiscale edge-based text extraction;printed document image;scene text;text detection;document image processing;edge detection;feature extraction;text analysis;", "doi", "10.1109/ICME.2006.262882", "ISSN", "" }
	)
public class LiuSamarabanduTextExtractorMultiscale extends TextExtractor<FImage>
{
	private static final boolean DEBUG = true;

	/** The basic text extractor implementation */
	private final LiuSamarabanduTextExtractorBasic basicTextExtractor =
		new LiuSamarabanduTextExtractorBasic();

	/** The extracted regions from the processing */
	private Map<Rectangle, FImage> extractedRegions;

	/** Whether to double the size of the initial image in the pyramid */
	private boolean doubleSizePyramid = true;

	/**
	 *	This is the main processor for this text extractor. For each of the
	 *	multiscale pyramid images, this performs the basic text extraction.
	 *
	 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
	 *  @created 28 Jul 2011
	 *
	 */
	public class PyramidTextExtractor implements PyramidProcessor<FImage>
	{
		/** The resulting feature map */
		private FImage featureMap = null;

		/**
		 * 	Get the feature map for the image.
		 *	@return The feature map for the image.
		 */
		public FImage getFeatureMap()
		{
			return this.featureMap;
		}

		/**
		 *	{@inheritDoc}
		 * 	@see org.openimaj.image.analysis.pyramid.PyramidProcessor#process(org.openimaj.image.analysis.pyramid.gaussian.GaussianPyramid)
		 */
		@Override
		public void process( final GaussianPyramid<FImage> pyramid )
		{
			FImage fmap = null;

			// Process each of the octaves in the pyramid
			for( final GaussianOctave<FImage> octave : pyramid )
			{
				// Extract text regions using the basic text extractor
				FImage octaveFMap = LiuSamarabanduTextExtractorMultiscale.this.basicTextExtractor.textRegionDetection(
						octave.getNextOctaveImage() );

				if( fmap == null )
					fmap = octaveFMap;
				else
				{
					// Fuse across scales
					octaveFMap = ResizeProcessor.resample( octaveFMap,
							fmap.getWidth(), fmap.getHeight() ).normalise();

					if( LiuSamarabanduTextExtractorMultiscale.DEBUG )
						DisplayUtilities.display( octaveFMap, "Resized feature map" );

					fmap.addInplace( octaveFMap );
				}
			}

			this.featureMap = fmap;
		}
	}

	/**
	 * 	Helper method for debugging when viewing images
	 */
	protected void forceWait()
	{
		synchronized(this){ try	{ this.wait( 200000 ); } catch( final InterruptedException e1 ) {} }
	}

	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.image.processor.ImageProcessor#processImage(org.openimaj.image.Image)
	 */
	@Override
	public void processImage( final FImage image )
	{
		final PyramidTextExtractor ped = new PyramidTextExtractor();

		// Unlike Lowe's SIFT DoG pyramid, we just need a basic pyramid
		final GaussianPyramidOptions<FImage> gpo = new GaussianPyramidOptions<FImage>();
		gpo.setScales( 1 );
		gpo.setExtraScaleSteps( 1 );
		gpo.setPyramidProcessor( ped );
		gpo.setDoubleInitialImage( this.doubleSizePyramid );

		// Create and process the pyramid
		final GaussianPyramid<FImage> gp = new GaussianPyramid<FImage>( gpo );
		image.analyseWith( gp );

		// -------------------------------------------------------------
		// This is not part of the Liu/Samarabandu algorithm:
		// Multiscale feature map
		FImage msFMap = ped.getFeatureMap();

		// Single scale feature map
		FImage fmap = this.basicTextExtractor.textRegionDetection( image );

		// Need to make it match the multiscale feature map
		if( this.doubleSizePyramid )
			fmap = ResizeProcessor.doubleSize( fmap );

		// Combine the two.
		msFMap = fmap.add( msFMap );
		// -------------------------------------------------------------

		if( LiuSamarabanduTextExtractorMultiscale.DEBUG )
			DisplayUtilities.display( msFMap.normalise(), "Fused Feature Map" );

		// Process the feature map
		this.basicTextExtractor.processFeatureMap( msFMap, image );

		// Store the regions
		this.extractedRegions = this.basicTextExtractor.getTextRegions();

		// If we doubled the feature map, we'll have to half the size of the bounding boxes.
		if( this.doubleSizePyramid )
			for( final Rectangle r : this.extractedRegions.keySet() )
				r.scale( 0.5f );

		// The output of the processor is the feature map
		image.internalAssign( fmap );
	}

	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.image.text.extraction.TextExtractor#getTextRegions()
	 */
	@Override
	public Map<Rectangle, FImage> getTextRegions()
	{
		return this.extractedRegions;
	}

	/**
	 * 	Whether the initial image in the pyramid is being double sized.
	 *	@return TRUE if the initial image is double sized.
	 */
	public boolean isDoubleSizePyramid()
	{
		return this.doubleSizePyramid;
	}

	/**
	 * 	Set whether to double the size of the pyramid
	 *	@param doubleSizePyramid TRUE to double the size of the initial image.
	 */
	public void setDoubleSizePyramid( final boolean doubleSizePyramid )
	{
		this.doubleSizePyramid = doubleSizePyramid;
	}
}
