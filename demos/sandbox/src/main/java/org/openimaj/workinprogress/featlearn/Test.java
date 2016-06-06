package org.openimaj.workinprogress.featlearn;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.processing.resize.ResizeProcessor;
import org.openimaj.math.matrix.algorithm.whitening.WhiteningTransform;
import org.openimaj.math.matrix.algorithm.whitening.ZCAWhitening;
import org.openimaj.math.statistics.normalisation.PerExampleMeanCenterVar;
import org.openimaj.ml.clustering.kmeans.SphericalKMeans;
import org.openimaj.ml.clustering.kmeans.SphericalKMeansResult;

public class Test {
	public static void main(String[] args) throws IOException {
		final File patchesFile = new File("patches.bin");

		// final RandomPatchSampler sampler =
		// new
		// RandomPatchSampler(Caltech101.getImages(ImageUtilities.FIMAGE_READER),
		// 8, 8, 100000);
		// sampler.save(patchesFile);
		final List<FImage> patches = RandomPatchSampler.loadPatches(patchesFile);

		final double[][] data = new double[patches.size()][];
		for (int i = 0; i < data.length; i++)
			data[i] = patches.get(i).getDoublePixelVector();

		// final PCAWhitening whitening = new PCAWhitening();
		final WhiteningTransform whitening = new ZCAWhitening(0.1, new PerExampleMeanCenterVar(10f / 255f));
		whitening.train(data);
		final double[][] wd = whitening.whiten(data);

		// final double[][] comps =
		// whitening.getTransform().transpose().getArray();
		// for (int i = 0; i < comps.length; i++)
		// DisplayUtilities.di play(ResizeProcessor.resample(new
		// FImage(comps[i], 8, 8).normalise(), 128, 128));

		// final FImage tmp1 = new FImage(100 * 8, 100 * 8);
		// final FImage tmp2 = new FImage(100 * 8, 100 * 8);
		// final FImage tmp3 = new FImage(100 * 8, 100 * 8);
		// for (int i = 0; i < 100; i++) {
		// for (int j = 0; j < 100; j++) {
		// final double[] d = new PerExampleMeanCenterVar(10f /
		// 255f).normalise(patches.get(i * 100 + j)
		// .getDoublePixelVector());
		// FImage patch = new FImage(d, 8, 8);
		// patch.divideInplace(2 * Math.max(patch.min(), patch.max()));
		// patch.addInplace(0.5f);
		// tmp2.drawImage(patch, i * 8, j * 8);
		//
		// tmp3.drawImage(patches.get(i * 100 + j), i * 8, j * 8);
		//
		// patch = new FImage(wd[i * 100 + j], 8, 8);
		// patch.divideInplace(2 * Math.max(patch.min(), patch.max()));
		// patch.addInplace(0.5f);
		// tmp1.drawImage(patch, i * 8, j * 8);
		// }
		// }
		// DisplayUtilities.display(tmp3);
		// DisplayUtilities.display(tmp2);
		// DisplayUtilities.display(tmp1);

		final SphericalKMeans skm = new SphericalKMeans(2500, 10);
		final SphericalKMeansResult res = skm.cluster(wd);
		final FImage tmp = new FImage(50 * (8 + 1) + 1, 50 * (8 + 1) + 1);
		tmp.fill(1f);
		for (int i = 0; i < 50; i++) {
			for (int j = 0; j < 50; j++) {
				final FImage patch = ResizeProcessor
						.resample(
								new FImage(res.centroids[i * 50 + j], 8, 8),
								8, 8);
				patch.divideInplace(2 * Math.max(Math.abs(patch.min()),
						Math.abs(patch.max())));
				patch.addInplace(0.5f);
				tmp.drawImage(patch, i * (8 + 1) + 1, j * (8 + 1) + 1);
			}
		}
		DisplayUtilities.display(tmp);
	}
}
