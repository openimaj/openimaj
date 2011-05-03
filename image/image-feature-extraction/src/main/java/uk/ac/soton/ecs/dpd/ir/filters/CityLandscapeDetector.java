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

package uk.ac.soton.ecs.dpd.ir.filters;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.Vector;

import org.openimaj.feature.DoubleFV;
import org.openimaj.feature.FeatureVectorProvider;
import org.openimaj.feature.MultidimensionalDoubleFV;
import org.openimaj.image.Image;
import org.openimaj.image.ImageUtilities;

import uk.ac.soton.ecs.dpd.ir.utils.Options;

/**
 * 	Uses the Edge Direction Coherence Histograms to attempt to
 * 	classify an image as city or landscape. This uses the coherent
 * 	edge histogram technique described in "On Image Classification:
 * 	City Images vs. Landscapes" by Vailaya, Jain and Zhang, Michigan
 * 	State University.
 *
 * 	@author David Dupplaw, 7th July 2005
 * @param <Q> Image type
 */
/**
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 *
 * @param <Q>
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class CityLandscapeDetector<Q extends Image<?,Q>> implements ImageFilter<Q>, FeatureVectorProvider
{
	private Options o;
	private boolean complete;

	private double[][] coDirHist = null;
	int numberOfDirBins = 72;

	// Edges are extracted using the Canny
	private CannyEdgeDetector cad = null;

	/**
	 * 
	 */
	public CityLandscapeDetector()
	{
		o = new Options();
		cad = new CannyEdgeDetector();

		o.addOptions( cad.getOptions() );
		o.addOption( "Direction Threshold", new Integer( 5 ) );
		o.addOption( "Plot Image", new Integer( 2 ) );
	}

	@Override
	public Options getOptions()
	{
		return o;
	}

	@Override
	public String getName()
	{
		return "City/Landscape Detector";
	}

	/**
	 * @return numberOfDirBins
	 */
	public int getNumberOfDirBins()
	{
		return numberOfDirBins;
	}

	/**
	 * @return coDirHist
	 */
	public double[][] getLastHistogram()
	{
		return coDirHist;
	}

	@Override
	public BufferedImage filter( BufferedImage i )
	{
		int w = i.getWidth();
		int h = i.getHeight();

		complete = false;

		BufferedImage bi = new BufferedImage( w, h, BufferedImage.TYPE_INT_ARGB );
		
		BufferedImage ei = cad.filter( i );
		
		//DisplayUtilities.display(ei);
		
		int[] mags = cad.getMagnitude();
		int[] dirs = cad.getOrientation();

		if( mags == null || dirs == null )
		{
			System.out.println("Canny Edge Detector did not return magnitude or direction.");
			return i;
		}

		// Histogram definition
		int numberOfBins = numberOfDirBins+1;

		// -- THE HISTOGRAM --
		double[] dirHist = new double[numberOfBins];

		int nonEdgeCount = 0;

		// count the number of non-edge pixels
		for( int y = 0; y < ei.getHeight(); y++ )
		{
			for( int x = 0; x < ei.getWidth(); x++ )
			{
				int p = ei.getRGB( x, y );
				if( p != 0xFF000000 )
					nonEdgeCount++;
			}
		}

		// Bin all the directions
		// We use bin 0 for non-edge pixels
		// and bin i+1 for the direction i
		for( int j = 0; j < w*h; j++ )
		{
			int x = j%w;
			int y = j/w;
			int p = ei.getRGB( x, y );
			if( p == 0xFF000000 )
			{
				int dirBin = ((dirs[j]+128)*numberOfDirBins/255) +1;
				//System.out.println( "dir @ "+j+" is "+dirs[j]+" going to bin "+dirBin );
				dirHist[dirBin]++;
	
				int v = (dirs[j]+128);
				int rgb = 0xff000000 + 
					(v << 16) +
					(v << 8) +
					(v);
				bi.setRGB( x, y, rgb );
			}
			else	bi.setRGB( x, y, 0x00000000 );
		}
		
		dirHist[0] = nonEdgeCount;
		int numberOfEdgePix = ei.getWidth()*ei.getHeight() - nonEdgeCount;

		// -- NORMALISE HISTOGRAM --
		for( int j = 0; j < numberOfDirBins; j++ )
			dirHist[j+1] /= numberOfEdgePix;
		dirHist[0] /= ei.getWidth() * ei.getHeight();

		// --------- HISTOGRAM PLOT ----------- //
//		BufferedImage histPlot = HistogramPlot.plotHistogram( dirHist, 1 );
		// ------------------------------------ //

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
		coDirHist = new double[2][numberOfDirBins];

		// Coherent Edge Image (only coherent edges displayed)
		BufferedImage cei = new BufferedImage( w, h, BufferedImage.TYPE_INT_ARGB );

		//System.out.println("0.002% of image size: "+(w*h*0.00002) );

		// First we find an edge pixel
		for( int j = 0; j < w*h; j++ )
		{
			int x = j%w;
			int y = j/w;
			int p = bi.getRGB( x, y );	// get the back projected edge pixel

			// in bi, non-edge pixels are set to 0x00000000 (transparent black)
			// which allows discretion between non-transparent black edge pixels
			if( p != 0x00000000 )
			{
				Vector v = getConnectedEdges( x, y, w, p, numberOfBins, bi, dirs, 8 );

				int dirBin = ((dirs[j]+128)*numberOfDirBins/255);

				//System.out.println("Connected Edge Vector: Size "+v.size() );

				int c = 0;
				if( v.size() > (w*h*0.00002) )
				{
					for( int k = 0; k < v.size(); k++ )
					{
						Point pp = (Point)v.elementAt(k);
						cei.setRGB( pp.x, pp.y, 0xFFFFFFFF );
					}
					c = 1;
				}

				coDirHist[c][dirBin] += v.size();
			}
		}

		complete = true;

		int plotImage = ((Integer)o.getOption("Plot Image")).intValue();

		switch( plotImage )
		{
			// Original Canny Edge Image
			case 0:
				return ei;
			// Back-Projected Edge Image
			// (should be blank after coherence testing)
			case 1:
				return bi;
			// Coherent Edge Image
			case 2:
				return cei;
		}

		return cei;
	}

	/**
	 * 	Function that given a pixel at x, y with value p, in image bi, it
	 * 	will find all connected edges that fall within the same bin.
	 *
	 * 	@param x The x coordinate of the seed edge pixel
	 * 	@param y The y coordinate of the seed edge pixel
	 * 	@param w The width of the edge image (required to index directions array)
	 * 	@param p The intensity of the given pixel
	 * 	@param numberOfBins Number of bins in the edge histogram (to work out direction)
	 * 	@param bi The back-projected edge image
	 * 	@param directions The original edge directions map
	 */
	private Vector getConnectedEdges( int x, int y, int w, int p, int numberOfBins, 
		BufferedImage bi, int[] dirs, int connectedness )
	{
		Vector v = new Vector();

		// The original point is always in the final set
		v.add( new Point( x, y ) );
		bi.setRGB( x, y, 0x00000000 );

		int dir = dirs[y*w+x];
		int fuzz = ((Integer)o.getOption("Direction Threshold")).intValue();
		//System.out.println("Direction @ "+x+", "+y+" is "+dir );

		boolean connected = true;
		while( connected )
		{
			int nx = x, ny = y;
			switch( connectedness )
			{
				// Check 4-connected neighbourhood
				case 4:
					nx = x+1; ny = y;
					if( nx >= 0 && ny >= 0 && dirs[ny*w+nx] < dir+fuzz && dirs[ny*w+nx] > dir-fuzz 
						&& bi.getRGB( nx, ny ) != 0x00000000 )
						break;
					nx = x; ny = y+1;
					if( nx >= 0 && ny >= 0 && dirs[ny*w+nx] < dir+fuzz && dirs[ny*w+nx] > dir-fuzz 
						&& bi.getRGB( nx, ny ) != 0x00000000 )
						break;
					nx = x-1; ny = y;
					if( nx >= 0 && ny >= 0 && dirs[ny*w+nx] < dir+fuzz && dirs[ny*w+nx] > dir-fuzz 
						&& bi.getRGB( nx, ny ) != 0x00000000 )
						break;
					nx = x; ny = y-1;
					if( nx >= 0 && ny >= 0 && dirs[ny*w+nx] < dir+fuzz && dirs[ny*w+nx] > dir-fuzz 
						&& bi.getRGB( nx, ny ) != 0x00000000 )
						break;
					nx = x; ny = y;
					break;

				// Check 8-connected neighbourhood
				case 8:
					nx = x+1; ny = y-1;
					if( nx >= 0 && ny >= 0 && dirs[ny*w+nx] < dir+fuzz && dirs[ny*w+nx] > dir-fuzz 
						&& bi.getRGB( nx, ny ) != 0x00000000 )
						break;
					nx = x+1; ny = y;
					if( nx >= 0 && ny >= 0 && dirs[ny*w+nx] < dir+fuzz && dirs[ny*w+nx] > dir-fuzz 
						&& bi.getRGB( nx, ny ) != 0x00000000 )
						break;
					nx = x+1; ny = y+1;
					if( nx >= 0 && ny >= 0 && dirs[ny*w+nx] < dir+fuzz && dirs[ny*w+nx] > dir-fuzz 
						&& bi.getRGB( nx, ny ) != 0x00000000 )
						break;
					nx = x; ny = y+1;
					if( nx >= 0 && ny >= 0 && dirs[ny*w+nx] < dir+fuzz && dirs[ny*w+nx] > dir-fuzz 
						&& bi.getRGB( nx, ny ) != 0x00000000 )
						break;
					nx = x-1; ny = y+1;
					if( nx >= 0 && ny >= 0 && dirs[ny*w+nx] < dir+fuzz && dirs[ny*w+nx] > dir-fuzz 
						&& bi.getRGB( nx, ny ) != 0x00000000 )
						break;
					nx = x-1; ny = y;
					if( nx >= 0 && ny >= 0 && dirs[ny*w+nx] < dir+fuzz && dirs[ny*w+nx] > dir-fuzz 
						&& bi.getRGB( nx, ny ) != 0x00000000 )
						break;
					nx = x-1; ny = y-1;
					if( nx >= 0 && ny >= 0 && dirs[ny*w+nx] < dir+fuzz && dirs[ny*w+nx] > dir-fuzz 
						&& bi.getRGB( nx, ny ) != 0x00000000 )
						break;
					nx = x; ny = y-1;
					if( nx >= 0 && ny >= 0 && dirs[ny*w+nx] < dir+fuzz && dirs[ny*w+nx] > dir-fuzz 
						&& bi.getRGB( nx, ny ) != 0x00000000 )
						break;
					nx = x; ny = y;
					break;
			}

			//System.out.println("nx = "+nx+", ny = "+ny+", dir = "+dirs[ny*w+nx] );
			if( (nx >= 0 && nx != x) || (ny >= 0 && ny != y) )
			{
				v.addElement( new Point( nx, ny ) );
				bi.setRGB( x, y, 0x00000000 );
				x = nx;
				y = ny;
			}
			else	connected = false;
		}
		return v;
	}

	@Override
	public boolean isImageReady()
	{
		return complete;
	}

	@Override
	public void processImage(Q image, Image<?,?>... otherimages) {
		BufferedImage i = ImageUtilities.createBufferedImage(image);
		BufferedImage o = this.filter(i);
		int [] data = o.getRGB(0, 0, o.getWidth(), o.getHeight(), null, 0, o.getWidth());
		image.internalAssign(data, o.getWidth(), o.getHeight());
	}
	
	@Override
	public DoubleFV getFeatureVector() {
		return new MultidimensionalDoubleFV(coDirHist);
	}
}
