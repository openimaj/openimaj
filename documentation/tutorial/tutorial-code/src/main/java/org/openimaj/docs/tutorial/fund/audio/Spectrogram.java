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

import org.openimaj.audio.AudioFormat;
import org.openimaj.audio.JavaSoundAudioGrabber;
import org.openimaj.audio.SampleChunk;
import org.openimaj.vis.audio.AudioSpectrogram;

/**
 * This is the code for exercise 1 in the basic audio tutorial. When you talk or
 * sing into the computer can you see the pitches in your voice? How does speech
 * compare to other sounds?
 * 
 * @author David Dupplaw (dpd@ecs.soton.ac.uk)
 * @created 19 Jun 2013
 * @version $Author$, $Revision$, $Date$
 */
public class Spectrogram
{
	/**
	 * Main method
	 * 
	 * @param args
	 *            command-line args (not used)
	 * @throws InterruptedException
	 */
	public static void main(final String[] args) throws InterruptedException
	{
		// Construct a new audio waveform visualisation
		final AudioSpectrogram aw = new AudioSpectrogram(440, 600);
		aw.showWindow("Spectrogram");

		// Start a sound grabber that will grab from your default microphone
		final JavaSoundAudioGrabber jsag = new JavaSoundAudioGrabber(new AudioFormat(16, 44.1, 1));
		new Thread(jsag).start();

		// Wait until the grabber has started (sometimes it takes a while)
		while (jsag.isStopped())
			Thread.sleep(50);

		// Then send each of the frames to the visualisation
		SampleChunk sc = null;
		while ((sc = jsag.nextSampleChunk()) != null)
			aw.setData(sc);
	}
}
