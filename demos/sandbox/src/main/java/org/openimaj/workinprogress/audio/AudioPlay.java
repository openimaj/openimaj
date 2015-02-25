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
package org.openimaj.workinprogress.audio;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.ByteBuffer;

import org.openimaj.audio.AudioFormat;
import org.openimaj.audio.AudioStream;
import org.openimaj.audio.SampleChunk;
import org.openimaj.audio.conversion.MultichannelToMonoProcessor;
import org.openimaj.audio.conversion.SampleRateConverter;
import org.openimaj.audio.processor.FixedSizeSampleAudioProcessor;
import org.openimaj.util.array.ArrayUtils;
import org.openimaj.video.xuggle.XuggleAudio;

public class AudioPlay {
	public static void main(String[] args) throws IOException {
		final AudioStream audio = new XuggleAudio(new File(
				"/Users/jon/Music/iTunes/iTunes Music/Rufus Wainwright/Shrek - Soundtrack/10 Hallelujah.mp3"));

		final int frameSize = 300;
		final int frameStep = 300;

		final AudioFormat targetFormat = new AudioFormat(16, 11.025, 1);
		final AudioStream audioConv = new SampleRateConverter(new MultichannelToMonoProcessor(audio),
				SampleRateConverter.SampleRateConversionAlgorithm.LINEAR_INTERPOLATION, targetFormat);
		final FixedSizeSampleAudioProcessor framer = new
				FixedSizeSampleAudioProcessor(audioConv, frameSize, frameSize -
						frameStep);

		final PrintWriter pw = new PrintWriter(new FileWriter(new
				File("samples.txt")));
		for (final SampleChunk sc : framer) {
			final ByteBuffer sb = sc.getSamplesAsByteBuffer();

			final short[] vector = new short[frameSize];
			for (int i = 0; i < Math.min(frameSize, sb.limit() / (Short.SIZE /
					Byte.SIZE)); i++)
			{
				final short v = sb.getShort();
				vector[i] = v;// (v / (float) Short.MAX_VALUE);
			}
			pw.println(ArrayUtils.toString(vector, "%6d"));
		}
		pw.close();
	}
}
