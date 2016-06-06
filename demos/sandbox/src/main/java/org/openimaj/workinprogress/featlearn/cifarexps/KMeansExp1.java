package org.openimaj.workinprogress.featlearn.cifarexps;

import java.io.IOException;
import java.util.List;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.pixel.sampling.RectangleSampler;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.math.matrix.algorithm.whitening.WhiteningTransform;
import org.openimaj.math.matrix.algorithm.whitening.ZCAWhitening;
import org.openimaj.math.statistics.normalisation.Normaliser;
import org.openimaj.math.statistics.normalisation.PerExampleMeanCenterVar;
import org.openimaj.ml.clustering.kmeans.SphericalKMeans;
import org.openimaj.ml.clustering.kmeans.SphericalKMeans.IterationResult;
import org.openimaj.ml.clustering.kmeans.SphericalKMeansResult;
import org.openimaj.util.function.Operation;

public class KMeansExp1 extends CIFARExperimentFramework {
	Normaliser patchNorm = new PerExampleMeanCenterVar(10.0 / 255.0);
	WhiteningTransform whitening = new ZCAWhitening(0.1, patchNorm);
	int numCentroids = 1600;
	int numIters = 10;

	private double[][] dictionary;
	final RectangleSampler rs = new RectangleSampler(new Rectangle(0, 0, 32, 32), 1, 1, patchSize, patchSize);
	final List<Rectangle> rectangles = rs.allRectangles();

	@Override
	protected void learnFeatures(double[][] patches) {
		whitening.train(patches);

		final double[][] whitenedFeaturePatches = whitening.whiten(patches);
		final SphericalKMeans skm = new SphericalKMeans(numCentroids, numIters);
		skm.addIterationListener(new Operation<SphericalKMeans.IterationResult>() {
			@Override
			public void perform(IterationResult object) {
				System.out.println("KMeans iteration " + object.iteration + " / " + numIters);
				DisplayUtilities.display(drawCentroids(object.result.centroids));
			}
		});
		final SphericalKMeansResult res = skm.cluster(whitenedFeaturePatches);
		this.dictionary = res.centroids;

		DisplayUtilities.display(drawCentroids(this.dictionary));
	}

	MBFImage drawCentroids(double[][] centroids) {
		final int wh = (int) Math.sqrt(numCentroids);
		final MBFImage tmp = new MBFImage(wh * (patchSize + 1) + 1, wh * (patchSize + 1) + 1);
		final float mn = -1.0f;
		final float mx = +1.0f;
		tmp.fill(new Float[] { mx, mx, mx });

		for (int i = 0, y = 0; y < wh; y++) {
			for (int x = 0; x < wh; x++, i++) {
				final MBFImage p = new MBFImage(centroids[i], patchSize, patchSize, 3, false);
				tmp.drawImage(p, x * (patchSize + 1) + 1, y * (patchSize + 1) + 1);
			}
		}
		tmp.subtractInplace(mn);
		tmp.divideInplace(mx - mn);
		return tmp;
	}

	@Override
	protected double[] extractFeatures(MBFImage image) {
		double[][] patches = new double[rectangles.size()][];
		final MBFImage tmpImage = new MBFImage(this.patchSize, this.patchSize);

		for (int i = 0; i < patches.length; i++) {
			final Rectangle r = rectangles.get(i);
			patches[i] = image.extractROI((int) r.x, (int) r.y, tmpImage).getDoublePixelVector();
		}
		patches = whitening.whiten(patches);
		patches = activation(patches);

		// sum pooling
		final double[] feature = pool(patches);

		return feature;
	}

	private double[] pool(double[][] patches) {
		final double[] feature = new double[dictionary.length * 4];
		final int sz = (int) Math.sqrt(patches.length);
		final int hsz = sz / 2;
		for (int j = 0; j < sz; j++) {
			final int by = j < hsz ? 0 : 1;
			for (int i = 0; i < sz; i++) {
				final int bx = i < hsz ? 0 : 1;

				final double[] p = patches[j * sz + i];
				for (int k = 0; k < p.length; k++)
					feature[2 * dictionary.length * by + dictionary.length * bx + k] += p[k];
			}
		}
		return feature;
	}

	// private double[][] activation(double[][] p) {
	// final double[][] c = this.dictionary;
	// final double[][] result = new double[p.length][c.length];
	//
	// final double[] z = new double[c.length];
	// for (int i = 0; i < p.length; i++) {
	// final double[] x = p[i];
	// double mu = 0;
	// for (int k = 0; k < z.length; k++) {
	// z[k] = 0;
	// for (int j = 0; j < x.length; j++) {
	// final double d = x[j] - c[k][j];
	// z[k] += d * d;
	// }
	// z[k] = Math.sqrt(z[k]);
	// mu += z[k];
	// }
	//
	// mu /= z.length;
	//
	// for (int k = 0; k < z.length; k++) {
	// result[i][k] = Math.max(0, mu - z[k]);
	// }
	// }
	//
	// return result;
	// }

	private double[][] activation(double[][] p) {
		final double[][] c = this.dictionary;
		final double[][] result = new double[p.length][c.length];

		for (int i = 0; i < p.length; i++) {
			final double[] x = p[i];

			for (int k = 0; k < c.length; k++) {
				double dx = 0;
				for (int j = 0; j < x.length; j++) {
					dx += c[k][j] * x[j];
				}
				result[i][k] = Math.max(0, Math.abs(dx) - 0.5);
			}
		}

		return result;
	}

	public static void main(String[] args) throws IOException {
		new KMeansExp1().run();
	}
}
