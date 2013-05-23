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

import org.openimaj.audio.SampleChunk;
import org.openimaj.audio.conversion.MultichannelToMonoProcessor;
import org.openimaj.audio.features.MFCC;
import org.openimaj.video.xuggle.XuggleAudio;
import org.openimaj.vis.general.BarVisualisation;

import scala.actors.threadpool.Arrays;

/**
 *
 *
 * @author David Dupplaw (dpd@ecs.soton.ac.uk)
 * @created 26 Jul 2012
 * @version $Author$, $Revision$, $Date$
 */
public class MFCCVisualiser
{
	/**
	 * @param args
	 * @throws InterruptedException
	 */
	public static void main(final String[] args) throws InterruptedException
	{
		// Setup a thread grabbing audio
//		 final JavaSoundAudioGrabber a = new JavaSoundAudioGrabber();
//		 a.setFormat( new AudioFormat( 16, 96.1, 1 ) );
//		 a.setMaxBufferSize( 1024 );
//		 new Thread( a ).start();
		final XuggleAudio xa = new XuggleAudio(new File("heads1.mpeg"));
		final MultichannelToMonoProcessor a = new MultichannelToMonoProcessor(xa);

		// Setup a visualisation
		final BarVisualisation bv = new BarVisualisation(1500, 500);
		bv.setDrawValues(true);
		bv.fixAxis( 250 );
		bv.showWindow("MFCCs");

		// Setup an MFCC processor
		final MFCC mfcc = new MFCC();

		// Read sample chunks
		SampleChunk sc = null;
		while ((sc = a.nextSampleChunk()) != null)
		{
			bv.getVisualisationImage().zero();
			final double[][] mfccs = mfcc.calculateMFCC(sc.getSampleBuffer());
			bv.setData( Arrays.copyOfRange( mfccs[0], 1, mfccs[0].length ) );

			Thread.sleep(50);
		}
	}
}
