package org.openimaj.workinprogress.featlearn;

import java.io.File;
import java.io.IOException;
import java.util.Random;

import org.openimaj.feature.DoubleFV;
import org.openimaj.feature.FeatureExtractor;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.processing.resize.ResizeProcessor;
import org.openimaj.math.matrix.algorithm.whitening.ZCAWhitening;
import org.openimaj.math.statistics.normalisation.PerExampleMeanCenter;
import org.openimaj.ml.clustering.kmeans.SphericalKMeans;
import org.openimaj.ml.clustering.kmeans.SphericalKMeansResult;
import org.openimaj.util.array.ArrayUtils;

public class TestImageClass implements FeatureExtractor<DoubleFV, FImage> {
	final Random rng = new Random(0);
	double[][] featurePatches;
	FImage[] urbanPatches;
	FImage[] ruralPatches;
	int patchSize;
	int bigPatchSize;

	ZCAWhitening whitening = new ZCAWhitening(0.1, new PerExampleMeanCenter());
	double[][] dictionary;
	private double[][] whitenedFeaturePatches;

	void extractFeaturePatches(FImage image, int npatches, int sz) {
		patchSize = sz;
		featurePatches = new double[npatches][];
		for (int i = 0; i < npatches; i++) {
			final int x = rng.nextInt(image.width - sz - 1);
			final int y = rng.nextInt(image.height - sz - 1);

			final double[] ip = image.extractROI(x, y, sz, sz).getDoublePixelVector();
			featurePatches[i] = ip;
		}
	}

	void extractClassifierTrainingPatches(FImage image, FImage labels, int npatchesPerClass, int sz) {
		bigPatchSize = sz;
		urbanPatches = new FImage[npatchesPerClass];
		ruralPatches = new FImage[npatchesPerClass];

		int u = 0;
		int r = 0;

		while (u < npatchesPerClass || r < npatchesPerClass) {
			final int x = rng.nextInt(image.width - sz - 1);
			final int y = rng.nextInt(image.height - sz - 1);

			final FImage ip = image.extractROI(x, y, sz, sz);
			final float[] lp = labels.extractROI(x, y, sz, sz).getFloatPixelVector();

			boolean same = true;
			for (int i = 0; i < sz * sz; i++) {
				if (lp[i] != lp[0]) {
					same = false;
					break;
				}
			}

			if (same) {
				if (lp[0] == 0 && r < npatchesPerClass) {
					ruralPatches[r] = ip;
					r++;
				} else if (lp[0] == 1 && u < npatchesPerClass) {
					// DisplayUtilities.display(ResizeProcessor.resample(ip,
					// 128, 128).normalise());
					urbanPatches[u] = ip;
					u++;
				}
			}
		}
	}

	void learnDictionary(int dictSize) {
		whitening.train(featurePatches);
		whitenedFeaturePatches = whitening.whiten(featurePatches);

		final SphericalKMeans skm = new SphericalKMeans(dictSize, 40);
		final SphericalKMeansResult res = skm.cluster(whitenedFeaturePatches);
		this.dictionary = res.centroids;
	}

	double[] representPatch(double[] patch) {
		final double[] wp = whitening.whiten(patch);

		final double[] z = new double[dictionary.length];
		for (int i = 0; i < z.length; i++) {
			double accum = 0;
			for (int j = 0; j < patch.length; j++) {
				accum += wp[j] * dictionary[i][j];
			}

			z[i] = Math.max(0, Math.abs(accum) - 0.5);
		}
		return z;
	}

	@Override
	public DoubleFV extractFeature(FImage bigpatch) {
		final double[][][] pfeatures = new double[3][3][dictionary.length];
		final int[][] pcount = new int[3][3];

		final FImage tmp = new FImage(patchSize, patchSize);
		for (int y = 0; y < bigPatchSize - patchSize; y++) {
			final int yp = (int) ((y / (double) (bigPatchSize - patchSize)) * 3);

			for (int x = 0; x < bigPatchSize - patchSize; x++) {
				final int xp = (int) ((x / (double) (bigPatchSize - patchSize)) * 3);

				final double[] p = bigpatch.extractROI(x, y, tmp).getDoublePixelVector();
				ArrayUtils.sum(pfeatures[yp][xp], representPatch(p));
				pcount[yp][xp]++;

			}
		}

		final double[] vector = new double[3 * 3 * dictionary.length];

		for (int y = 0; y < 3; y++)
			for (int x = 0; x < 3; x++)
				for (int i = 0; i < dictionary.length; i++)
					if (pfeatures[y][x][i] > 0)
						vector[3 * x + y * 3 * 3 + i] = pfeatures[y][x][i] / pcount[y][x];

		return new DoubleFV(vector);
	}

	public static void main(String[] args) throws IOException {
		final TestImageClass tic = new TestImageClass();

		final FImage trainPhoto = ResizeProcessor.halfSize(ResizeProcessor.halfSize(ImageUtilities.readF(new File(
				"/Users/jon/Desktop/images50cm4band/sp7034.jpeg"))));
		final FImage trainClass = ResizeProcessor.halfSize(ResizeProcessor.halfSize(ImageUtilities.readF(new File(
				"/Users/jon/Desktop/images50cm4band/sp7034-classes.PNG"))));

		tic.extractFeaturePatches(trainPhoto, 20000, 8);
		tic.extractClassifierTrainingPatches(trainPhoto, trainClass, 1000, 32);
		tic.learnDictionary(100);

		// Note: should really use sparse version!!
		/*
		 * final LiblinearAnnotator<FImage, Boolean> ann = new
		 * LiblinearAnnotator<FImage, Boolean>(tic, Mode.MULTICLASS,
		 * SolverType.L2R_L2LOSS_SVC, 1, 0.0001);
		 * 
		 * final MapBackedDataset<Boolean, ListBackedDataset<FImage>, FImage>
		 * data = new MapBackedDataset<Boolean, ListBackedDataset<FImage>,
		 * FImage>(); data.add(true, new
		 * ListBackedDataset<FImage>(Arrays.asList(tic.ruralPatches)));
		 * data.add(false, new
		 * ListBackedDataset<FImage>(Arrays.asList(tic.urbanPatches)));
		 * ann.train(data);
		 */
		final FImage test = ResizeProcessor.halfSize(ResizeProcessor.halfSize(ImageUtilities.readF(new File(
				"/Users/jon/Desktop/images50cm4band/test.jpeg")))).normalise();

		/*
		 * final FImage result = test.extractCenter(test.width - 32, test.height
		 * - 32); final FImage tmp = new FImage(32, 32); for (int y = 0; y <
		 * test.height - 32; y++) { for (int x = 0; x < test.width - 32; x++) {
		 * test.extractROI(x, y, tmp);
		 * 
		 * final ClassificationResult<Boolean> r = ann.classify(tmp); final
		 * Boolean clz = r.getPredictedClasses().iterator().next();
		 * 
		 * if (clz) result.pixels[y][x] = 1;
		 * 
		 * DisplayUtilities.displayName(result, "result"); } }
		 */

		final FImage tmp = new FImage(8 * 10, 8 * 10);
		for (int i = 0, y = 0; y < 10; y++) {
			for (int x = 0; x < 10; x++, i++) {
				final FImage p = new FImage(tic.dictionary[i], 8, 8);
				p.divideInplace(2 * Math.max(p.min(), p.max()));
				p.addInplace(0.5f);
				tmp.drawImage(p, x * 8, y * 8);
			}
		}
		DisplayUtilities.display(tmp);
	}
}
