package org.openimaj.demos.sandbox.video;

import java.io.File;

import javax.swing.JFrame;

import org.openimaj.video.xuggle.XuggleVideo;
import org.openimaj.vis.video.ShotBoundaryVideoBarVisualisation;

/**
 *	
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *  @created 3 Jul 2012
 *	@version $Author$, $Revision$, $Date$
 */
public class ShotBoundaryVis
{
	/**
	 * 
	 *	@param args
	 */
	public static void main( String[] args )
	{
		XuggleVideo x = new XuggleVideo( new File("/home/dd/video.m4v") );
		ShotBoundaryVideoBarVisualisation sbvbv = new ShotBoundaryVideoBarVisualisation( x );
		JFrame f = new JFrame();
		f.getContentPane().add( sbvbv );
		f.setSize( 1000, 300 );
		f.setVisible( true );
		sbvbv.processVideo();
	}
	
}

