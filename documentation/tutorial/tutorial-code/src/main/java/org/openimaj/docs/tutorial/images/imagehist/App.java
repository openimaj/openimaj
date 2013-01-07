package org.openimaj.docs.tutorial.images.imagehist;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.openimaj.feature.DoubleFVComparison;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.pixel.statistics.HistogramModel;
import org.openimaj.math.statistics.distribution.MultidimensionalHistogram;

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
		final URL[] imageURLs = new URL[] {
				new URL("http://users.ecs.soton.ac.uk/dpd/projects/openimaj/tutorial/hist1.jpg"),
				new URL("http://users.ecs.soton.ac.uk/dpd/projects/openimaj/tutorial/hist2.jpg"),
				new URL("http://users.ecs.soton.ac.uk/dpd/projects/openimaj/tutorial/hist3.jpg")
		};

		final List<MultidimensionalHistogram> histograms = new ArrayList<MultidimensionalHistogram>();
		final HistogramModel model = new HistogramModel(4, 4, 4);

		for (final URL u : imageURLs) {
			model.estimateModel(ImageUtilities.readMBF(u));
			histograms.add(model.histogram);
		}

		for (int i = 0; i < histograms.size(); i++) {
			for (int j = i; j < histograms.size(); j++) {
				final double distance = histograms.get(i).compare(histograms.get(j), DoubleFVComparison.EUCLIDEAN);
				System.out.println(distance);
			}
		}
	}
}
