package org.openimaj.image.ocr;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.analysis.algorithm.TemplateMatcher;
import org.openimaj.image.pixel.FValuePixel;

/**
 * Really simple (arabic numerical) OCR engine, specifically designed to extract
 * the date and time from the GlacsWeb timelapse images <a
 * href="http://data.glacsweb.info/iceland/webcam/river/">here</a>.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 */
public class SimpleOCR {
	class NumberOccurance implements Comparable<NumberOccurance> {
		int offset;
		int value;

		NumberOccurance(int offset, int value) {
			this.offset = offset;
			this.value = value;
		}

		@Override
		public int compareTo(NumberOccurance o) {
			return ((Integer) offset).compareTo(o.offset);
		}
	}

	private TemplateMatcher[] templates = new TemplateMatcher[10];

	/**
	 * Construct the engine and load the templates.
	 * 
	 * @throws IOException
	 */
	public SimpleOCR() throws IOException {
		for (int i = 0; i < 10; i++) {
			final FImage img = ImageUtilities.readF(SimpleOCR.class.getResourceAsStream(i + ".png"));
			templates[i] = new TemplateMatcher(img, TemplateMatcher.Mode.NORM_CORRELATION_COEFFICIENT);
		}
	}

	String processInternal(FImage img, String separator, int... pattern) throws Exception {
		final List<NumberOccurance> occurances = new ArrayList<NumberOccurance>();

		int num = 0;
		for (final int i : pattern)
			num += i;

		for (int i = 0; i < 10; i++) {
			templates[i].analyseImage(img);

			final FValuePixel[] resp = templates[i].getBestResponses(num);

			for (final FValuePixel pt : resp) {
				if (pt.value > 0.95) {
					occurances.add(new NumberOccurance(pt.x, i));
				}
			}
		}

		if (occurances.size() != num)
			throw new Exception();

		Collections.sort(occurances);

		String result = "";
		int i = 0, j = 0;
		for (final NumberOccurance no : occurances) {
			if (pattern[i] == j) {
				j = 0;
				i++;
				result += separator;
			}

			result += no.value;

			j++;
		}

		return result;
	}

	/**
	 * Extract the date-time from the given image
	 * 
	 * @param image
	 * @return the date-time string
	 * @throws Exception
	 *             if there was an error
	 */
	public String process(FImage image) throws Exception {
		final String date = processInternal(extractDateArea(image), "/", 4, 2, 2);
		final String time = processInternal(extractTimeArea(image), ":", 2, 2, 2);

		return date + " " + time;
	}

	private FImage extractDateArea(FImage image) {
		return image.extractROI(664, 1024, 176, 16);
	}

	private FImage extractTimeArea(FImage image) {
		return image.extractROI(840, 1024, 144, 16);
	}

	/**
	 * Process the given images (filenames or urls)
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		final SimpleOCR gocr = new SimpleOCR();

		for (final String f : args) {
			try {
				FImage image;

				if (f.contains("://"))
					image = ImageUtilities.readF(new URL(f));
				else
					image = ImageUtilities.readF(new File(f));

				System.out.println(f + " " + gocr.process(image));
			} catch (final Exception e) {
				System.out.println(f + " error occurred performing ocr");
			}
		}
	}
}
