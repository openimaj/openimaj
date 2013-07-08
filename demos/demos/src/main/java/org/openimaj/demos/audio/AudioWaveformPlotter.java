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
package org.openimaj.demos.audio;

import java.io.File;
import java.io.IOException;

import org.openimaj.demos.Demo;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.video.xuggle.XuggleAudio;

/**
 * 	Utilises an audio processor to plot the audio waveform.
 *
 *  @author David Dupplaw (dpd@ecs.soton.ac.uk)
 *
 *	@created 9 Jun 2011
 */
@Demo(
	author = "David Dupplaw",
	description = "Demonstrates reading an audio file and plotting the waveform" +
			" into an image.",
	keywords = { "audio", "image", "waveform" },
	title = "Audio Waveform Plotter",
	icon = "/org/openimaj/demos/icons/audio/audio-waveform-icon.png"
)
public class AudioWaveformPlotter
{
	/**
	 * 	Default main
	 *  @param args Command-line arguments
	 */
	public static void main( final String[] args )
    {
		// Open the audio stream
	    final XuggleAudio a = new XuggleAudio( AudioWaveformPlotter.class.
	    		getResource( "/org/openimaj/demos/audio/140bpm_formware_psytech.mp3" ) );

	    // This is how wide we're going to draw the display
	    final int w = 1920;

	    // This is how high we'll draw the display
	    final int h = 200;

	    final MBFImage img = org.openimaj.vis.audio.AudioOverviewVisualisation.
	    		getAudioWaveformImage( a, w, h, new Float[]{0f,0f,0f,1f},
	    				new Float[]{1f,1f,1f,1f} );

	    // Write the image to a file.
		try
		{
			ImageUtilities.write( img, "png", new File("audioWaveform.png") );
		}
		catch( final IOException e )
		{
			e.printStackTrace();
		}

		// Display the image
		DisplayUtilities.display( img );
    }
}
