/**
 * 
 */
package org.openimaj.image.processing.face.tracking.clm;

import org.openimaj.image.FImage;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.processing.resize.ResizeProcessor;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.math.geometry.shape.Triangle;

import Jama.Matrix;

/**
 *	
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *  @created 4 Jul 2012
 *	@version $Author$, $Revision$, $Date$
 */
public class CLMFaceTracker
{
	/** The tracker to use */
	private Tracker model = null; 
	
	/** The face mesh */
	private int[][] triangles = null; 
	
	/** The face connections */
	private int[][] connections = null;

	/** The scale at which to process the video */
	private float scale = 1f;

	/** Whether to use the face check (using pixels as a face classifier) */
	private boolean fcheck = false;

	/** Number of frames on which to force a redetection */
	private int fpd = -1;

	/** Search window size while tracking */
	private int[] wSize1 = { 7 };
	
	/** Search window size when initialising after a failed track */
	private int[] wSize2 = { 11, 9, 7 };

	/** Number of iterations to use for model fitting */
	private int nIter = 5;

	/** Number of standard deviations from the mean face to allow in the model */
	private double clamp = 3;

	/** Model fitting optimisation tolerance */
	private double fTol = 0.01;

	/** Whether the last track failed */
	private boolean failed = true;
	
	/** The size of the search area for redetection (template matching) */
	private float searchAreaSize = 1.4f;

	/**
	 * 	Instantiates a tracker for tracking faces based on some default
	 * 	values and models.
	 */
	public CLMFaceTracker()
	{
		model = new Tracker( Tracker.Load(Tracker.class.getResourceAsStream( "face2.tracker" )) );
		triangles = IO.LoadTri( Tracker.class.getResourceAsStream( "face.tri" ) );
		connections = IO.LoadCon( Tracker.class.getResourceAsStream( "face.con" ) );
	}
	
	/**
	 * 	Track the face in the given frame.
	 *	@param frame The frame
	 */
	public void track( MBFImage frame )
	{
		// Make a greyscale image
		FImage im = frame.flatten();

		// If we're to rescale, let's do that first
		if( scale != 1 ) 
			if( scale == 0.5f ) 
					im = ResizeProcessor.halfSize( im );
			else	im = ResizeProcessor.resample( im, (int)(scale * im.width),
						(int)(scale * im.height) );

		int[] wSize;
		if( failed ) 
				wSize = wSize2;
		else 	wSize = wSize1;

		// Track the face
		if( model.Track( im, wSize, fpd, nIter, clamp, fTol, fcheck, searchAreaSize ) == 0 )
		{
			failed = false;
		}
		else
		{
			model.frameReset();
			failed = true;
		}
	}
	
	/**
	 * 	Force a reset on the next frame to be tracked.
	 */
	public void reset()
	{
		model.frameReset();
	}
	
	/**
	 * 	Draw the model onto the image
	 * 
	 *	@param image The image to draw onto
	 *	@param drawTriangles Whether to draw the triangles
	 *	@param drawConnections Whether to draw the connections
	 *	@param drawPoints Whether to draw the points
	 * 	@param drawSearchArea Whether to draw the search area 
	 * 	@param drawBounds Whether to draw the bounds
	 */
	public void drawModel( MBFImage image, 
			boolean drawTriangles, boolean drawConnections,
			boolean drawPoints, boolean drawSearchArea, boolean drawBounds )
	{
		for( int fc = 0; fc < model.trackedFaces.size(); fc++ )
		{
			Tracker.TrackedFace f = model.trackedFaces.get(fc);
			
			final int n = f.shape.getRowDimension() / 2;
			final Matrix visi = f.clm._visi[f.clm.GetViewIdx()];

			if( drawSearchArea )
			{
				// Draw the search area size
				Rectangle r = f.lastMatchBounds.clone();
				r.scaleCOG( searchAreaSize );
				image.createRenderer().drawShape( r, RGBColour.YELLOW );
			}
			
			if( drawBounds )
				image.createRenderer().drawShape( f.lastMatchBounds, RGBColour.RED );		
	
			if( drawTriangles )
			{
				// Draw triangulation
				for( int i = 0; i < triangles.length; i++ )
				{
					if( visi.get( triangles[i][0], 0 ) == 0 || 
						visi.get( triangles[i][1], 0 ) == 0 || 
						visi.get( triangles[i][2], 0 ) == 0 ) 
							continue;
		
					Triangle t = new Triangle(
							new Point2dImpl( (float)f.shape.get( triangles[i][0], 0 ) / scale,
									(float)f.shape.get( triangles[i][0] + n, 0 ) / scale ),
							new Point2dImpl( (float)f.shape.get( triangles[i][1], 0 ) / scale,
									(float)f.shape.get( triangles[i][1] + n, 0 ) / scale ),
							new Point2dImpl( (float)f.shape.get( triangles[i][2], 0 ) / scale,
									(float)f.shape.get( triangles[i][2] + n, 0 ) / scale ) );
					image.drawShape( t, RGBColour.BLACK );
				}
			}
			
			if( drawConnections )
			{
				// draw connections
				for( int i = 0; i < connections[0].length; i++ )
				{
					if( visi.get( connections[0][i], 0 ) == 0 || 
						visi.get( connections[1][i], 0 ) == 0 ) 
							continue;
		
					image.drawLine( 
						new Point2dImpl( (float)f.shape.get( connections[0][i], 0 ) / scale,
					        (float)f.shape.get( connections[0][i] + n, 0 ) / scale ),
						new Point2dImpl( (float)f.shape.get( connections[1][i], 0 ) / scale,
					        (float)f.shape.get( connections[1][i] + n, 0 ) / scale ),
					        RGBColour.WHITE );
				}
			}
			
			if( drawPoints )
			{
				// draw points
				for( int i = 0; i < n; i++ )
				{
					if( visi.get( i, 0 ) == 0 ) continue;
					
					image.drawPoint( new Point2dImpl( (float)f.shape.get( i, 0 ) / scale,
					        (float)f.shape.get( i + n, 0 ) / scale ), RGBColour.RED, 2 );
				}
			}
		}
	}
}
