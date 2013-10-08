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
package org.openimaj.docs.tutorial.fund.audio;

import java.net.MalformedURLException;
import java.net.URL;

import org.openimaj.audio.analysis.FourierTransform;
import org.openimaj.audio.filters.EQFilter;
import org.openimaj.audio.filters.EQFilter.EQType;
import org.openimaj.video.xuggle.XuggleAudio;
import org.openimaj.vis.general.BarVisualisation;

/**
 * Example that demonstrates chaining of audio processors as well as visualising
 * the output of an FFT operation.
 *
 * @author David Dupplaw (dpd@ecs.soton.ac.uk)
 * @created 18 Jun 2013
 * @version $Author$, $Revision$, $Date$
 */
public class FFTEQ
{
	/**
	 * Main method
	 *
	 * @param args Command-line args (unused)
	 * @throws MalformedURLException
	 */
	public static void main( final String[] args ) throws MalformedURLException
	{
		// Open a URL to the sine wave sweep. If you have downloaded
		// this file you should use a new File(<filename>) here.
		final XuggleAudio xa = new XuggleAudio(
			new URL( "http://www.audiocheck.net/download.php?" +
					"filename=Audio/audiocheck.net_sweep20-20klin.wav" ) );

		// Create an EQ processor to pre-process the audio before doing the FFT
		// It will be a low-pass filter with a cut-off at 5KHz
		final EQFilter eq = new EQFilter( xa, EQType.LPF, 5000 );

		// Create the Fourier transform processor chained to the audio decoder
		final FourierTransform fft = new FourierTransform( eq );

		// Create a visualisation to show our FFT and open the window now
		// To avoid the visualisation scaling up the values once the peak
		// fades out, we need to disable auto scaling.
		final BarVisualisation bv = new BarVisualisation( 400, 200 );
		bv.setMaxValue( 1E12 );
		bv.setAutoScale( false );
		bv.showWindow( "FFTs" );

		// Loop through the sample chunks from the audio capture thread
		// sending each one through the feature extractor and displaying
		// the results in the visualisation.
		while( fft.nextSampleChunk() != null )
		{
			final float[][] ffts = fft.getMagnitudes();
			bv.setData( ffts[0] );
		}
	}
}
