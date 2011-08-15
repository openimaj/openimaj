/**
 * 
 */
package org.openimaj.image.processing.transform;

import java.util.Collection;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.Image;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.processing.algorithm.HoughLines;
import org.openimaj.image.processing.edges.CannyEdgeDetector2;
import org.openimaj.image.processing.threshold.OtsuThreshold;
import org.openimaj.image.processor.ImageProcessor;
import org.openimaj.image.renderer.MBFImageRenderer;
import org.openimaj.math.geometry.line.Line2d;

import Jama.Matrix;

/**
 * 	Uses the Hough transform (for lines) to attempt to find the skew of the 
 * 	image and unskews it using a basic skew transform.	
 * 
 *	@see http://javaanpr.sourceforge.net/anpr.pdf
 *
 *	@author David Dupplaw <dpd@ecs.soton.ac.uk>
 *  @created 12 Aug 2011
 *	@version $Author$, $Revision$, $Date$
 */
public class SkewCorrector implements ImageProcessor<FImage>
{
	private static final boolean DEBUG = false;
	
	/**
	 *	@inheritDoc
	 * 	@see org.openimaj.image.processor.ImageProcessor#processImage(org.openimaj.image.Image, org.openimaj.image.Image<?,?>[])
	 */
	@Override
	public void processImage( FImage image, Image<?, ?>... otherimages )
	{
		CannyEdgeDetector2 cad = new CannyEdgeDetector2();
		FImage edgeImage = image.process(cad).inverse();
		
		// Detect Lines in the image
		HoughLines hl = new HoughLines( 360 );
		edgeImage.process( hl );

		if( DEBUG )
			debugLines( edgeImage, Matrix.identity(3,3), "Detection of Horizontal Lines",
					hl.getBestLines(2) );

		// ---------------------------------------------------------------
		// First rotate the image such that the prevailing lines
		// are horizontal.
		// ---------------------------------------------------------------
		// Find the prevailing angle
		double rotationAngle = hl.calculatePrevailingAngle();
		
		if( rotationAngle == -1 )
			System.out.println( "WARNING: Detection of rotation angle failed.");
		
		rotationAngle -= 90;
		rotationAngle %= 360;

		if( DEBUG )
			System.out.println( "Rotational angle: "+rotationAngle );
		
		rotationAngle *= 0.0174532925 ;

		// Rotate so that horizontal lines are horizontal
		Matrix rotationMatrix = new Matrix( new double[][] {
				{Math.cos(-rotationAngle), -Math.sin(-rotationAngle), 0},
				{Math.sin(-rotationAngle), Math.cos(-rotationAngle), 0},
				{0,0,1}
		});

		// We use a projection processor as we need our
		// background pixels to be white.
		FImage rotImg = ProjectionProcessor.project( edgeImage, rotationMatrix, 1f ).
			process( new OtsuThreshold() );
		
		// We need to return a proper image (not the edge image), so we
		// process that here too.
		FImage outImg = ProjectionProcessor.project( edgeImage, rotationMatrix, 0f );

		if( DEBUG )
			DisplayUtilities.display( rotImg, "Rotated Image" );

		// ---------------------------------------------------------------
		// Now attempt to make the verticals vertical by shearing
		// ---------------------------------------------------------------
		// Re-process with the Hough lines
		rotImg.process( hl );

		float shearAngleRange = 20;
		
		if( DEBUG )
			debugLines( rotImg, Matrix.identity(3,3), "Detection of Vertical Lines", 
					hl.getBestLines(2,-shearAngleRange,shearAngleRange) );

		// Get the prevailing angle around vertical
		double shearAngle = hl.calculatePrevailingAngle( -shearAngleRange,shearAngleRange );

		if( shearAngle == -1 )
			System.out.println( "WARNING: Detection of shear angle failed.");

		// shearAngle -= 90;
		shearAngle %= 360;

		if( DEBUG )
			System.out.println( "Shear angle = "+shearAngle );
		
		shearAngle *= 0.0174532925 ;
		
		// Create a shear matrix
		Matrix shearMatrix = new Matrix( new double[][] {
				{1, Math.tan( shearAngle ), 0},
				{0,1,0},
				{0,0,1}
		});
		
		// Process the image to unshear it. 
		FImage unshearedImage = rotImg.transform( shearMatrix );
		outImg = outImg.transform( shearMatrix );
		
		if( DEBUG )
			DisplayUtilities.display( unshearedImage, "Final Image" );
		
		image.internalAssign( outImg );
	}	
	
	/**
	 * 	Helper function to display the image with lines
	 *  @param i
	 *  @param hl
	 *  @param tf
	 *  @param title
	 *  @param lines
	 */
	private void debugLines( FImage i, Matrix tf, String title,
			Collection<Line2d> lines )
	{
		// Create an image showing where the lines are
		MBFImage output = new MBFImage( i.getWidth(), 
				i.getHeight(), 3 );
		MBFImageRenderer r = output.createRenderer(); // RenderHints.ANTI_ALIASED );
		r.drawImage( i, 0, 0 );
		
		for( Line2d l : lines )
		{
			Line2d l2 = l.transform(tf).lineWithinSquare( output.getBounds() );
			
			// l2 can be null if it doesn't intersect with the image
			if( l2 != null )
			{
				System.out.println( l2 );
				r.drawLine( l2, 2, RGBColour.RED );				
			}
		}
		
		DisplayUtilities.display( output, title );
	}
}
