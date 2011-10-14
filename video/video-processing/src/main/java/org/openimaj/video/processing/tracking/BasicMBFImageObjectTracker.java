package org.openimaj.video.processing.tracking;

import java.util.List;

import org.openimaj.image.MBFImage;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.video.tracking.klt.FeatureList;

/**
 * 	A tracker that will track one rectangular region using the KLTTracker from
 * 	MBFImages. It simply uses the {@link BasicObjectTracker} and flattens the
 * 	incoming MBFImages to FImages and then tracks.
 * 
 *  @author David Dupplaw <dpd@ecs.soton.ac.uk>
 *	@version $Author$, $Revision$, $Date$
 *	@created 14 Oct 2011
 */
public class BasicMBFImageObjectTracker implements ObjectTracker<Rectangle,MBFImage>
{
	private BasicObjectTracker objectTracker = new BasicObjectTracker();
	
	/**
	 * 	Returns the list of features that the tracker has been tracking.
	 *	@return the {@link FeatureList}
	 */
	public FeatureList getFeatureList()
	{
		return objectTracker.getFeatureList();
	}

	/**
	 *	@inheritDoc
	 * 	@see org.openimaj.video.processing.tracking.ObjectTracker#initialiseTracking(org.openimaj.math.geometry.shape.Rectangle, java.lang.Object)
	 */
	@Override
	public List<Rectangle> initialiseTracking( Rectangle bounds, MBFImage image )
	{
		return objectTracker.initialiseTracking( bounds, image.flatten() );
	}

	/**
	 *	@inheritDoc
	 * 	@see org.openimaj.video.processing.tracking.ObjectTracker#trackObject(java.lang.Object)
	 */
	@Override
	public List<Rectangle> trackObject( MBFImage image )
	{
		return objectTracker.trackObject( image.flatten() );
	}
}
