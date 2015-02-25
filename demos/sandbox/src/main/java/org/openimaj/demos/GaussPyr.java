package org.openimaj.demos;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.feature.local.engine.DoGSIFTEngine;
import org.openimaj.image.feature.local.engine.DoGSIFTEngineOptions;
import org.openimaj.image.processing.convolution.FFastGaussianConvolve;
import org.openimaj.image.processor.SinglebandImageProcessor;

public class GaussPyr {

	/**
	 * @param args
	 * @throws IOException
	 * @throws MalformedURLException
	 */
	public static void main(String[] args) throws MalformedURLException, IOException {
		final MBFImage im = ImageUtilities.readMBF(new URL(
				"http://fc01.deviantart.net/fs71/i/2013/102/4/a/erica_the_rhino__update_03_by_c_clancy-d61fznp.jpg"));

		// final int[] ts = { 0, 1, 2, 4, 16, 32 };
		// for (final int t : ts) {
		// final long t1 = System.currentTimeMillis();
		// final MBFImage proc = im.process(new FGaussianConvolve((float)
		// Math.sqrt(t)));
		// final long t2 = System.currentTimeMillis();
		// final MBFImage proc2 = im.process(new FFastGaussianConvolve((float)
		// Math.sqrt(t), 5));
		// final long t3 = System.currentTimeMillis();
		//
		// System.out.println(t + " slow " + (t2 - t1));
		// System.out.println(t + " fast " + (t3 - t2));
		//
		// DisplayUtilities.display(proc.subtract(proc2).abs().threshold(1e-4f));
		//
		// // ImageUtilities.write(proc, new File("/Users/jsh2/Desktop/level" +
		// // t + ".png"));
		// }

		final DoGSIFTEngineOptions<FImage> opts = new
				DoGSIFTEngineOptions<FImage>();
		final DoGSIFTEngineOptions<FImage> fastopts = new
				DoGSIFTEngineOptions<FImage>()
				{
					@Override
					public SinglebandImageProcessor<Float, FImage>
							createGaussianBlur(float sigma)
					{
						return new FFastGaussianConvolve(sigma, 8);
					}
				};

		final DoGSIFTEngine sift = new DoGSIFTEngine(opts);
		final DoGSIFTEngine fastsift = new DoGSIFTEngine(fastopts);
		final FImage fimg = im.flatten();
		for (int i = 0; i < 10; i++) {
			final long t1 = System.currentTimeMillis();
			System.out.println(sift.findFeatures(fimg).size());
			final long t2 = System.currentTimeMillis();
			System.out.println(fastsift.findFeatures(fimg).size());
			final long t3 = System.currentTimeMillis();

			System.out.println((t2 - t1) + " " + (t3 - t2));
		}
	}
}
