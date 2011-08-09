/**
 * 
 */
package org.openimaj.image.text.extraction;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.Image;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.processing.convolution.AbstractFConvolution;
import org.openimaj.image.processing.pyramid.PyramidProcessor;
import org.openimaj.image.processing.pyramid.gaussian.GaussianOctave;
import org.openimaj.image.processing.pyramid.gaussian.GaussianPyramid;
import org.openimaj.image.processing.pyramid.gaussian.GaussianPyramidOptions;
import org.openimaj.image.processing.resize.ResizeProcessor;
import org.openimaj.image.processing.convolution.CompassOperators.Compass0;
import org.openimaj.image.processing.convolution.CompassOperators.Compass135;
import org.openimaj.image.processing.convolution.CompassOperators.Compass45;
import org.openimaj.image.processing.convolution.CompassOperators.Compass90;
import org.openimaj.image.processor.ImageProcessor;
import org.openimaj.math.util.Interpolation;

/**
 *	
 *
 *	@author David Dupplaw <dpd@ecs.soton.ac.uk>
 *  @created 28 Jul 2011
 *	@version $Author$, $Revision$, $Date$
 */
public class TextExtractor implements ImageProcessor<FImage>
{
	/**
	 *
	 *	@author David Dupplaw <dpd@ecs.soton.ac.uk>
	 *  @created 28 Jul 2011
	 *	@version $Author$, $Revision$, $Date$
	 */
	public class PyramidEdgeDetector implements PyramidProcessor<FImage>
	{
		/** Window size */
		private int ws = 25;
		
		/** The image being processed */
		private FImage image = null;
		
		/** The feature map being generated */
		private FImage featureMap = null;
		
		/**
		 *	@param image
		 *	@param windowSize
		 */
		public PyramidEdgeDetector( FImage image, int windowSize )
		{
			this.image = image;
			this.ws = windowSize;
		}
		
		/**
		 * 	Get the value of the pixel in the scale image that would match with
		 * 	the coordinates of the pixel (x,y) in an image wxh in size.
		 * 
		 *	@param x The x coordinate
		 *	@param y The y coordinate
		 *	@param w The width
		 *	@param h The height
		 *	@param scaleImage The actual scale image
		 *	@return Pixel value
		 */
		public float getPixelBiLinear( int x, int y, int w, int h, FImage scaleImage )
		{
			// Get scalars
			double sx = w / scaleImage.getWidth();
			double sy = h / scaleImage.getHeight();
			
			// x and y in the scale image
			int xx = (int)Math.floor( x / sx );
			int yy = (int)Math.floor( y / sy );
			
			// Get fractions
			double dx = (x/sx) - xx;
			double dy = (y/sy) - yy;
			
			// Bi-linear interpolate
			return (float)Interpolation.bilerp( 
					dx, dy, 
					scaleImage.getPixel(xx,yy),
					scaleImage.getPixel(xx+1,yy),
					scaleImage.getPixel(xx,yy+1),
					scaleImage.getPixel(xx+1,yy+1)
			);
		}
		
		private FImage processOctaveImage( FImage img, AbstractFConvolution c )
		{
			FImage i = ResizeProcessor.resample( 
					img.process( c ), 
					image.getWidth(), image.getHeight() ).normalise();
			try
			{
				ImageUtilities.write( i, "png", 
						new File("output"+Math.random()+".png") );
			}
			catch( IOException e )
			{
				e.printStackTrace();
			}
			return i;
		}
	
		@Override
		public void process( GaussianPyramid<FImage> pyramid )
		{
			System.out.println( "Processing pyramid..." );
			
			// We need to temporarily store all the edge images
			List<Map<Integer,FImage>> scaleEdges = 
				new ArrayList<Map<Integer,FImage>>();
			
			// Process all the scales for edges
			for( GaussianOctave<FImage> octave : pyramid )
			{
				System.out.println( "    - processing octave...");
				
				// We only need the first image
				FImage img = octave.images[0];
				
				// Edge detection with the compass operators
				HashMap<Integer,FImage> m = new HashMap<Integer, FImage>();
				m.put(   0, processOctaveImage( img, new Compass0() ) );
				m.put(  45, processOctaveImage( img, new Compass45() ) );
				m.put(  90, processOctaveImage( img, new Compass90() ) ); 
				m.put( 135, processOctaveImage( img, new Compass135() ) );
				scaleEdges.add( m );
			}
			
			// The feature map will be an image the same size of the
			// original image (pixel intensity is probability of text)
			featureMap = new FImage( image.getWidth(), image.getHeight() );

			System.out.println( "Generating feature map..." );

			// Assumption that we normalise by window size 
			double n = 1d/(ws*ws);
			
			// Now we work out the feature map:
			// f(x,y) = S[s=0..n]( sum[T](1/N.sum[i=-c..+c]( sum[j=-c..+c](
			//       E( s, T, x+i, y+j ) x W(i,j) ) ) ) )
			//
			// Although this is just a kernel processor, we don't use
			// the KernelProcessor class because we need to know the x,y coordinate
			// of our kernel which the KernelProcessor doesn't give us.
			for( int j = 0; j < featureMap.getHeight()-ws; j++ )
			{
				System.out.print( "+" );
				for( int i = 0; i < featureMap.getWidth()-ws; i++ )
				{					
					float pp = 0;

					for( int scale = 0; scale < scaleEdges.size(); scale++ )
					{
						Map<Integer,FImage> s = scaleEdges.get(scale);
						
						// Work out the "number of directions" within the
						// window - the weighting for the pixels in the window.
						float w = 0;
						for( FImage im : s.values() )
						{
							FImage f = im.extractROI( i, j, ws, ws );
							Float[] ff = f.getPixelVector( new Float[ws*ws] );
							for( Float fff : ff )
								w += fff;
						}
						w /= s.values().size()*ws*ws;
						
						if( w != 0 )
							System.out.println( "Weight for "+i+","+j+" at scale "+scale+" is "+w );
						
						float p = 0;
						for( Integer t : s.keySet() )
						{
							// Get the scale image
							FImage scaleImage = s.get(t);						
							// DisplayUtilities.display( scaleImage );
							
							// Sum over the window
							for( int x = 0; x < ws; x++ )
								for( int y = 0; y < ws; y++ )
									p += scaleImage.getPixel( x+i, y+j ) * w;
							
							// Normalise
							p *= n;
						}
						
						pp += p;
					}

					// Set the feature map pixel
					featureMap.setPixel( i, j, pp );
				}
			}
		}
		
		public FImage getFeatureMap()
		{
			return featureMap;
		}
	}
	
	@Override
	public void processImage( FImage image, Image<?, ?>... otherimages )
	{
		// Method proposed by X.Liu and J.Samarabandu
		// Uni. of Western Ontario
		// in "Multiscale Edge-based Text Extraction From Complex Images"
		//
		// 1. Find candidate text region
		//    a. Multiscale edge detector
		//    b. Feature map generation
		// 2. Text region localization
		// 3. Character extraction
		//
		
		int ws = 5;
		PyramidEdgeDetector ped = new PyramidEdgeDetector( image, ws );
		
		// Unlike Lowe's SIFT DoG pyramid, we just need a basic pyramid
		GaussianPyramidOptions<FImage> gpo = new GaussianPyramidOptions<FImage>();
		gpo.setScales( 1 );
		gpo.setExtraScaleSteps( 1 );
		gpo.setPyramidProcessor( ped );
		gpo.setDoubleInitialImage( false );
		
		// Create and process the pyramid
		GaussianPyramid<FImage> gp = new GaussianPyramid<FImage>( gpo );
		image.process( gp );
		
		DisplayUtilities.display( ped.getFeatureMap().normalise() );
		try
		{
			ImageUtilities.write( ped.getFeatureMap().normalise(), "png", 
					new File("resources/output.png") );
		}
		catch( IOException e )
		{
			e.printStackTrace();
		}
		
		int a = 2;
		while( 1 != a );
	}
}
