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

