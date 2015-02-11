package org.openimaj.demos;

import gnu.trove.list.array.TFloatArrayList;

import java.io.File;

import org.openimaj.audio.AudioFormat;
import org.openimaj.audio.AudioStream;
import org.openimaj.audio.SampleChunk;
import org.openimaj.audio.conversion.MultichannelToMonoProcessor;
import org.openimaj.audio.conversion.SampleRateConverter;
import org.openimaj.util.array.ArrayUtils;
import org.openimaj.video.xuggle.XuggleAudio;

public class AudioPlay {
	public static void main(String[] args) {
		final AudioStream audio = new XuggleAudio(new File(
				"/Users/jon/Music/iTunes/iTunes Music/Rufus Wainwright/Shrek - Soundtrack/10 Hallelujah.mp3"));

		final AudioFormat targetFormat = new AudioFormat(16, 11.025, 1);
		final AudioStream audioConv = new SampleRateConverter(new MultichannelToMonoProcessor(audio),
				SampleRateConverter.SampleRateConversionAlgorithm.LINEAR_INTERPOLATION, targetFormat);

		SampleChunk sc;
		final TFloatArrayList samples = new TFloatArrayList();
		while ((sc = audioConv.nextSampleChunk()) != null) {
			for (int i = 0; i < sc.getNumberOfSamples(); i++) {
				final short v = sc.getSamplesAsByteBuffer().getShort();
				samples.add(v / (float) Short.MAX_VALUE);
			}
		}

		final int frameSize = 300;
		final int frameStep = 100;
		for (int i = 0; i < samples.size() - frameSize; i += frameStep) {
			final float[] vector = samples.subList(i, i + frameSize).toArray();
			System.out.println(ArrayUtils.toString(vector, "%2.2f"));
		}
	}
}
