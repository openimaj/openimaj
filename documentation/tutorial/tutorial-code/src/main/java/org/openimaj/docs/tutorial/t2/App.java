package org.openimaj.docs.tutorial.t2;

import java.io.IOException;
import java.net.URL;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.processing.edges.CannyEdgeDetector;
import org.openimaj.image.typography.hershey.HersheyFont;
import org.openimaj.math.geometry.shape.Ellipse;

/**
 * OpenIMAJ Hello world!
 * 
 */
public class App {
	/**
	 * Main method
	 * 
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		// Load the image
		final MBFImage image = ImageUtilities.readMBF(new URL("http://dl.dropbox.com/u/8705593/sinaface.jpg"));

		// Print colour space
		System.out.println(image.colourSpace);

		// Display the image
		DisplayUtilities.display(image);
		DisplayUtilities.display(image.getBand(0), "Red Channel");

		// Set blue and green pixels to black, and draw
		final MBFImage clone = image.clone();
		for (int y = 0; y < image.getHeight(); y++) {
			for (int x = 0; x < image.getWidth(); x++) {
				clone.getBand(1).pixels[y][x] = 0;
				clone.getBand(2).pixels[y][x] = 0;
			}
		}
		DisplayUtilities.display(clone);

		// Find edges
		image.processInplace(new CannyEdgeDetector());
		DisplayUtilities.display(image);

		// Draw some stuff
		image.drawShapeFilled(new Ellipse(700f, 450f, 20f, 10f, 0f), RGBColour.WHITE);
		image.drawShapeFilled(new Ellipse(650f, 425f, 25f, 12f, 0f), RGBColour.WHITE);
		image.drawShapeFilled(new Ellipse(600f, 380f, 30f, 15f, 0f), RGBColour.WHITE);
		image.drawShapeFilled(new Ellipse(500f, 300f, 100f, 70f, 0f), RGBColour.WHITE);
		image.drawText("OpenIMAJ is", 425, 300, HersheyFont.ASTROLOGY, 20, RGBColour.BLACK);
		image.drawText("Awesome", 425, 330, HersheyFont.ASTROLOGY, 20, RGBColour.BLACK);
		DisplayUtilities.display(image);
	}
}
