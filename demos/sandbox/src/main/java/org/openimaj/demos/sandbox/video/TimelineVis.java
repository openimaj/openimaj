/**
 * 
 */
package org.openimaj.demos.sandbox.video;

import java.awt.BorderLayout;
import java.io.File;

import javax.swing.JFrame;

import org.openimaj.video.xuggle.XuggleVideo;
import org.openimaj.vis.timeline.Timeline;
import org.openimaj.vis.timeline.Timeline.TimelineTrack;
import org.openimaj.vis.video.ShotBoundaryVideoBarVisualisation;

/**
 * Visualisation example of a timeline showing video, audio and other stuff.
 * 
 * @author David Dupplaw (dpd@ecs.soton.ac.uk)
 * @created 3 Jul 2012
 * @version $Author$, $Revision$, $Date$
 */
public class TimelineVis
{
	/**
	 * Default main.
	 * 
	 * @param args Command-line arguments
	 */
	public static void main( String[] args )
	{
		// Create a timeline
		Timeline t = new Timeline();

		// Create a frame and display the timeline
		JFrame f = new JFrame();
		f.getContentPane().add( t, BorderLayout.CENTER );
		f.setSize( 1500, 300 );
		f.setVisible( true );

		// Create a track with a video on it.
		TimelineTrack tt = t.addTrack( "Video 1" );
		ShotBoundaryVideoBarVisualisation sb = new ShotBoundaryVideoBarVisualisation(
				new XuggleVideo( new File( "/home/dd/video.m4v" ) ) ); 
		tt.addTimelineObject( sb );
		sb.setStartTimeMilliseconds( 2000 );
		sb.processVideo();
//		
//		TimelineTrack tt1a = t.addTrack( "Video 1 Audio" );
//		AudioWaveformPlotter awp = new AudioWaveformPlotter( 
//				new XuggleAudio( new File( "video.m4v" ) ) );
//		tt1a.addTimelineObject( awp );
//
//		// Create another track with another video on it
//		TimelineTrack tt2 = t.addTrack( "Video 2" );
//		ShotBoundaryVideoBarVisualisation sb2 = new ShotBoundaryVideoBarVisualisation(
//				new XuggleVideo( new File( "videoplayback.mp4" ) ) );
//		tt2.addTimelineObject( sb2 );
//		sb2.processVideo();
	}
}
