package org.openimaj.demos;

import java.io.File;
import java.io.IOException;

import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.processing.convolution.filterbank.LeungMalikFilterBank;
import org.openimaj.image.processing.resize.ResizeProcessor;

public class FBTest {

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		final FImage img = ResizeProcessor.halfSize(ImageUtilities.readF(new File(
				"/Users/jsh2/Dropbox/Photos/Sample Album/Boston City Flow.jpg")));

		final long t1 = System.currentTimeMillis();
		final LeungMalikFilterBank fb = new LeungMalikFilterBank();
		fb.analyseImage(img);
		final long t2 = System.currentTimeMillis();

		System.out.println(t2 - t1);
	}

}
