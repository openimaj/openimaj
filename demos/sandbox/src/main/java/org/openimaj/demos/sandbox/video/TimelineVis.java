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
package org.openimaj.demos.sandbox.video;

import java.io.File;

import org.openimaj.video.xuggle.XuggleVideo;
import org.openimaj.vis.timeline.Timeline;
import org.openimaj.vis.timeline.Timeline.TimelineTrack;
import org.openimaj.vis.video.ShotBoundaryVideoBarVisualisation;

/**
 * VisualisationImpl example of a timeline showing video, audio and other stuff.
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
	public static void main( final String[] args )
	{
		// Create a timeline
		final Timeline t = new Timeline( 1500, 300 );

		// Create a frame and display the timeline
		t.showWindow( "Timeline" );

		// Create a track with a video on it.
		final TimelineTrack tt = t.addTrack( "Video 1" );
		final ShotBoundaryVideoBarVisualisation sb = new ShotBoundaryVideoBarVisualisation(
				new XuggleVideo( new File( "video.m4v" ) ) );
		t.addTimelineObject( tt, sb );
		sb.setStartTimeMilliseconds( 2000 );
		sb.processVideo();
//
//		TimelineTrack tt1a = t.addTrack( "Video 1 Audio" );
//		AudioOverviewVisualisation awp = new AudioOverviewVisualisation(
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
