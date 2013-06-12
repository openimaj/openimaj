/**
 *
 */
package org.openimaj.vis;

import org.openimaj.image.MBFImage;
import org.openimaj.video.AnimatedVideo;
import org.openimaj.video.VideoDisplay;
import org.openimaj.vis.general.DotPlotVisualisation;
import org.openimaj.vis.general.DotPlotVisualisation.ColouredDot;
import org.openimaj.vis.world.WorldMap;


/**
 *	This class provides a means for generating a video representation of a
 *	visualisation. This has various advantages in that the visualisation
 *	can be rendered easily to a video file or the {@link VideoDisplay}
 *	tools can be used to display the visualisation.
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *  @created 12 Jun 2013
 */
public class VideoVisualisation extends AnimatedVideo<MBFImage>
{
	private final Visualisation<?> visualisation;

	/**
	 *	Construct a video visualisation using the given visualisation.
	 *	@param v The visualisation to make into a video.
	 */
	public VideoVisualisation( final Visualisation<?> v )
	{
		super( v.getVisualisationImage() );
		this.visualisation = v;
	}

	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.video.AnimatedVideo#updateNextFrame(org.openimaj.image.Image)
	 */
	@Override
	protected void updateNextFrame( final MBFImage frame )
	{
		this.visualisation.updateVis();
		frame.internalAssign( this.visualisation.getVisualisationImage() );
	}

	/**
	 * 	Main method
	 *	@param args command-line args (unused)
	 */
	public static void main( final String[] args )
	{
		// Create the visualisation
		final DotPlotVisualisation dpv = new DotPlotVisualisation();
		final WorldMap<ColouredDot> wm = new WorldMap<ColouredDot>( 1280, 720, dpv );

		// Create the video and the video display
		final VideoVisualisation vv = new VideoVisualisation( wm );
		final VideoDisplay<MBFImage> vd = VideoDisplay.createVideoDisplay( vv );

		// Now run the sequence
		final String[] countries = new String[] {"us","cn","gb","za","au","cl","ru"};
		for( int i = 0; i < countries.length; i++ )
		{
			wm.addHighlightCountry( countries[i] );
			try
			{
				Thread.sleep( 1000 );
			}
			catch( final InterruptedException e )
			{
				e.printStackTrace();
			}
			wm.removeHighlightCountry( countries[i] );
		}

		vd.close();
	}
}
