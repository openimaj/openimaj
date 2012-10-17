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
package org.openimaj.demos.sandbox.audio;

import java.io.File;
import java.util.Arrays;

import org.openimaj.audio.AudioStream;
import org.openimaj.audio.SampleChunk;
import org.openimaj.audio.analysis.FourierTransform;
import org.openimaj.audio.conversion.MultichannelToMonoProcessor;
import org.openimaj.audio.filters.HanningAudioProcessor;
import org.openimaj.video.xuggle.XuggleAudio;
import org.openimaj.vis.audio.AudioWaveform;
import org.openimaj.vis.general.BarVisualisation;

/**
 *	Simple class that shows a sample, the Hanning function and the FFT
 *	of the hanning-windowed sample using the OpenIMAJ visualisations.
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *  @created 1 Aug 2012
 *	@version $Author$, $Revision$, $Date$
 */
public class AudioFFT
{
	/**
	 *	@param args
	 */
	public static void main( final String[] args )
	{
		String name = "heads1.mpeg";
		if( args.length > 0 )
			name = args[0];
		
		final XuggleAudio xa = new XuggleAudio( new File( name ) );
		final MultichannelToMonoProcessor mm = new MultichannelToMonoProcessor( xa );
		final HanningAudioProcessor hanning = new HanningAudioProcessor( mm, 128 );
		final FourierTransform fft = new FourierTransform();
		
		final AudioStream toUse = hanning;
		
		// Jump past the quiet start of the audio
		toUse.nextSampleChunk();
		toUse.nextSampleChunk();
		toUse.nextSampleChunk();
		
		final SampleChunk sc = toUse.nextSampleChunk();
		final double[] d = sc.getSampleBuffer().asDoubleArray();

		System.out.println( Arrays.toString(d) );
		
		final AudioWaveform hanningw = new AudioWaveform( 1000, 500 );
		hanningw.setData( hanning.getWeights() );
		
		// Show the audio waveform
		final AudioWaveform aw = new AudioWaveform( hanningw );
		aw.setData( d, sc.getFormat() );
		aw.showWindow( "Audio" );
		
//		ArrayUtils.normaliseMax( d, 256d );
//		SampleBuffer sb = new FloatSampleBuffer( d, sc.getFormat() ); 

		fft.process( sc );		
		final float[][] lastFFT = fft.getLastFFT();
		
		System.out.println( Arrays.deepToString( lastFFT ) );

//		float[] f = new float[d.length*2];
//		for( int i = 0; i < d.length; i++ )
//			f[i] = (float)d[i];
//		FloatFFT_1D offt = new FloatFFT_1D( d.length );
//		offt.complexForward( f );
//		
//		System.out.println( Arrays.toString( f ) );
//
		final BarVisualisation bv = new BarVisualisation( 1500, 400 );
		bv.setData( lastFFT[0] );
		bv.showWindow( "FFT" );
	}
}
