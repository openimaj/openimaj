package org.openimaj.workinprogress.featlearn;

import java.io.IOException;
import java.util.List;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.annotation.evaluation.datasets.CIFAR10Dataset;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.math.matrix.algorithm.whitening.ZCAWhitening;
import org.openimaj.math.statistics.normalisation.PerExampleMeanCenter;
import org.openimaj.ml.clustering.kmeans.SphericalKMeans;
import org.openimaj.ml.clustering.kmeans.SphericalKMeansResult;

public class Test2 {
	public static void main(String[] args) throws IOException {
		System.out.println("start");
		final RandomPatchSampler<MBFImage> sampler = new RandomPatchSampler<MBFImage>(
				CIFAR10Dataset.getTrainingImages(CIFAR10Dataset.MBFIMAGE_READER),
				8, 8, 400000);
		final List<MBFImage> patches = sampler.getPatches();
		System.out.println("stop");

		final double[][] data = new double[patches.size()][];
		for (int i = 0; i < data.length; i++)
			data[i] = patches.get(i).getDoublePixelVector();

		// final PCAWhitening whitening = new PCAWhitening();
		final ZCAWhitening whitening = new ZCAWhitening(0.1, new PerExampleMeanCenter());
		whitening.train(data);
		final double[][] wd = whitening.whiten(data);

		final SphericalKMeans skm = new SphericalKMeans(1600, 10);
		final SphericalKMeansResult res = skm.cluster(wd);
		final MBFImage tmp = new MBFImage(40 * (8 + 1) + 1, 40 * (8 + 1) + 1);
		tmp.fill(RGBColour.WHITE);
		for (int i = 0; i < 40; i++) {
			for (int j = 0; j < 40; j++) {
				final MBFImage patch = new MBFImage(res.centroids[i * 40 + j], 8, 8, 3, false);
				tmp.drawImage(patch, i * (8 + 1) + 1, j * (8 + 1) + 1);
			}
		}
		tmp.subtractInplace(-1.5f);
		tmp.divideInplace(3f);
		DisplayUtilities.display(tmp);
	}
}
