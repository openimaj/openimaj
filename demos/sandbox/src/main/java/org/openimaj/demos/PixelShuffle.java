package org.openimaj.demos;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;

public class PixelShuffle {

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		final MBFImage image = ImageUtilities.readMBF(new File("/Users/jsh2/Pictures/08-earth_shuttle2.jpg"));

		int[] pixels = image.toPackedARGBPixels();

		Arrays.sort(pixels);
		final MBFImage sorted = new MBFImage(pixels, image.getWidth(), image.getHeight());
		ImageUtilities.write(sorted, new File("/Users/jsh2/Pictures/sorted.jpg"));

		final List<Integer> plist = Arrays.asList(ArrayUtils.toObject(pixels));
		Collections.shuffle(plist);
		pixels = ArrayUtils.toPrimitive(plist.toArray(new Integer[pixels.length]));
		final MBFImage shuffled = new MBFImage(pixels, image.getWidth(), image.getHeight());
		ImageUtilities.write(shuffled, new File("/Users/jsh2/Pictures/shuffled.jpg"));
	}
}
