package org.openimaj.docs.tutorial.adv.faces.eigen;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import org.openimaj.feature.DoubleFV;
import org.openimaj.feature.DoubleFVComparison;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.model.EigenImages;

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
		final List<FImage> basisImages = Arrays.asList(new FImage[] {
				ImageUtilities.readF(new URL("")),
				ImageUtilities.readF(new URL("")),
				ImageUtilities.readF(new URL("")),
				ImageUtilities.readF(new URL("")),
				ImageUtilities.readF(new URL(""))
		});

		final int nEigenvectors = 20;

		final EigenImages eigen = new EigenImages(nEigenvectors);
		eigen.train(basisImages);

		for (int i = 0; i < nEigenvectors; i++) {
			DisplayUtilities.display(eigen.visualisePC(i), "PC " + i);
		}

		final FImage[][] trainingImages = new FImage[][] {
				{ ImageUtilities.readF(new URL("")), ImageUtilities.readF(new URL("")) },
				{ ImageUtilities.readF(new URL("")), ImageUtilities.readF(new URL("")) }
		};

		final DoubleFV[][] features = new DoubleFV[trainingImages.length][];

		for (int person = 0; person < trainingImages.length; person++) {
			features[person] = new DoubleFV[trainingImages[person].length];

			for (int face = 0; face < trainingImages[person].length; face++) {
				features[person][face] = eigen.extractFeature(trainingImages[person][face]);
			}
		}

		final FImage testImage = ImageUtilities.readF(new URL(""));
		final DoubleFV testFeature = eigen.extractFeature(testImage);
		int bestPerson = 0;
		double minDistance = Double.MAX_VALUE;
		for (int person = 0; person < trainingImages.length; person++) {
			for (int face = 0; face < trainingImages[person].length; face++) {
				final double distance = features[person][face].compare(testFeature, DoubleFVComparison.EUCLIDEAN);

				if (distance < minDistance) {
					minDistance = distance;
					bestPerson = person;
				}
			}
		}

		System.out.println("Image is person " + bestPerson);
	}
}
