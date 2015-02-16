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
