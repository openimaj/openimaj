package org.openimaj.demos;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.contour.Contour;
import org.openimaj.image.contour.ContourRenderer;
import org.openimaj.image.contour.SuzukiContourProcessor;

public class SuzukiTests {

	/**
	 * @param args
	 * @throws IOException
	 * @throws MalformedURLException
	 */
	public static void main(String[] args) throws MalformedURLException, IOException {
		final FImage img = ImageUtilities.readF(new URL("http://i.stack.imgur.com/IEM15.png"));
		final Contour c = SuzukiContourProcessor.findContours(img);

		System.out.println(c);

		DisplayUtilities.display(ContourRenderer.drawContours(img.toRGB(), c));
	}
}
