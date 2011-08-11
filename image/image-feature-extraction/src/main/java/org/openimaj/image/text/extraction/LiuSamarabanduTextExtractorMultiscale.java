/**
 * 
 */
package org.openimaj.image.text.extraction;

import java.util.Map;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.Image;
import org.openimaj.image.processing.pyramid.PyramidProcessor;
import org.openimaj.image.processing.pyramid.gaussian.GaussianOctave;
import org.openimaj.image.processing.pyramid.gaussian.GaussianPyramid;
import org.openimaj.image.processing.pyramid.gaussian.GaussianPyramidOptions;
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
 *	@author David Dupplaw <dpd@ecs.soton.ac.uk>
 *  @created 28 Jul 2011
 *	@version $Author$, $Revision$, $Date$
 */
public class LiuSamarabanduTextExtractorMultiscale extends TextExtractor<FImage>
{
	private static final boolean DEBUG = true;

	/** The basic text extractor implementation */
	private LiuSamarabanduTextExtractorBasic basicTextExtractor =
		new LiuSamarabanduTextExtractorBasic();

	/** The extracted regions from the processing */
	private Map<Rectangle, FImage> extractedRegions;

	/**
	 *	This is the main processor for this text extractor. For each of the
	 *	multiscale pyramid images, this performs the basic text extraction.
	 *
	 *	@author David Dupplaw <dpd@ecs.soton.ac.uk>
	 *  @created 28 Jul 2011
	 *	@version $Author$, $Revision$, $Date$
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
		 *	@inheritDoc
		 * 	@see org.openimaj.image.processing.pyramid.PyramidProcessor#process(org.openimaj.image.processing.pyramid.gaussian.GaussianPyramid)
		 */
		@Override
		public void process( GaussianPyramid<FImage> pyramid )
		{
			FImage fmap = null;
			
			// Process each of the octaves in the pyramid
			for( GaussianOctave<FImage> octave : pyramid )
			{
				// Extract text regions using the basic text extractor
				FImage octaveFMap = basicTextExtractor.textRegionDetection( 
						octave.getNextOctaveImage() );
				
				if( fmap == null )
					fmap = octaveFMap;
				else
				{
					// Fuse across scales
					octaveFMap = ResizeProcessor.resample( octaveFMap, 
							fmap.getWidth(), fmap.getHeight() ).normalise();
					
					if( DEBUG )
						DisplayUtilities.display( octaveFMap, "Resized feature map" );
					
					fmap.addInline( octaveFMap );
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
		synchronized(this){ try	{ wait( 200000 ); } catch( InterruptedException e1 ) {} }
	}
	
	/**
	 *	@inheritDoc
	 * 	@see org.openimaj.image.processor.ImageProcessor#processImage(org.openimaj.image.Image, org.openimaj.image.Image<?,?>[])
	 */
	@Override
	public void processImage( FImage image, Image<?, ?>... otherimages )
	{
		PyramidTextExtractor ped = new PyramidTextExtractor();
		
		// Unlike Lowe's SIFT DoG pyramid, we just need a basic pyramid
		GaussianPyramidOptions<FImage> gpo = new GaussianPyramidOptions<FImage>();
		gpo.setScales( 1 );
		gpo.setExtraScaleSteps( 1 );
		gpo.setPyramidProcessor( ped );
		gpo.setDoubleInitialImage( false );
		
		// Create and process the pyramid
		GaussianPyramid<FImage> gp = new GaussianPyramid<FImage>( gpo );
		image.process( gp );

		// -------------------------------------------------------------
		// This is not part of the Liu/Samarabandu algorithm:
		// Multiscale feature map
		FImage msFMap = ped.getFeatureMap();
		
		// Single scale feature map
		FImage fmap = this.basicTextExtractor.textRegionDetection( image );
		
		// Combine the two.
		msFMap = fmap.add( msFMap );
		// -------------------------------------------------------------
		
		if( DEBUG )
			DisplayUtilities.display( msFMap.normalise(), "Fused Feature Map" );
		
		// Process the feature map
		this.basicTextExtractor.processFeatureMap( msFMap, image );
		
		// Store the regions
		this.extractedRegions = this.basicTextExtractor.getTextRegions();
		
		if( DEBUG )
			forceWait();
		
		// The output of the processor is the feature map
		image.internalAssign( fmap );
	}

	/**
	 *	@inheritDoc
	 * 	@see org.openimaj.image.text.extraction.TextExtractor#getTextRegions()
	 */
	@Override
	public Map<Rectangle, FImage> getTextRegions()
	{
		return extractedRegions;
	}
}
