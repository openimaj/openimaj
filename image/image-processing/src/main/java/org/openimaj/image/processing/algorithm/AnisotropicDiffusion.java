package org.openimaj.image.processing.algorithm;

import odk.lang.FastMath;

import org.openimaj.citation.annotation.Reference;
import org.openimaj.citation.annotation.ReferenceType;
import org.openimaj.image.FImage;
import org.openimaj.image.pixel.ConnectedComponent;
import org.openimaj.image.pixel.ConnectedComponent.ConnectMode;
import org.openimaj.image.processor.SinglebandImageProcessor;

/**
 * Implementation of Perona & Malik's image filtering by anisotropic diffusion.
 * Enables edge-preserving image smoothing.
 *
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
@Reference(
		type = ReferenceType.Article,
		author = { "Perona, P.", "Malik, J." },
		title = "Scale-Space and Edge Detection Using Anisotropic Diffusion",
		year = "1990",
		journal = "IEEE Trans. Pattern Anal. Mach. Intell.",
		pages = { "629", "", "639" },
		url = "http://dx.doi.org/10.1109/34.56205",
		month = "July",
		number = "7",
		publisher = "IEEE Computer Society",
		volume = "12",
		customData = {
				"issue_date", "July 1990",
				"issn", "0162-8828",
				"numpages", "11",
				"doi", "10.1109/34.56205",
				"acmid", "78304",
				"address", "Washington, DC, USA"
		})
public class AnisotropicDiffusion implements SinglebandImageProcessor<Float, FImage> {
	/**
	 * Interface describing a function that computes the conduction coefficient
	 * as a function of space and gradient magnitude.
	 *
	 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
	 *
	 */
	public static interface ConductionCoefficientFunction {
		/**
		 * Compute the conduction coefficient given gradient and position.
		 *
		 * @param grad
		 *            the gradient
		 * @param x
		 *            the x-position in the image
		 * @param y
		 *            the y-position in the image
		 * @return the conduction coefficient
		 */
		float apply(float grad, int x, int y);
	}

	/**
	 * The first conduction function proposed by Perona & Malik, that privileges
	 * high contrast edges over low constrast ones.
	 *
	 * <pre>
	 * g(∇I) = exp( - (||∇I/κ||²) )
	 * </pre>
	 *
	 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
	 */
	public static class HighConstrastEdgeFunction implements ConductionCoefficientFunction {
		private float kappa;

		/**
		 * Construct with the given kappa value
		 *
		 * @param kappa
		 *            kappa
		 */
		public HighConstrastEdgeFunction(float kappa) {
			this.kappa = kappa;
		}

		@Override
		public float apply(float val, int x, int y) {
			final float t = val / kappa;
			return (float) FastMath.exp(-t * t);
		}
	};

	/**
	 * The second conduction function proposed by Perona & Malik, that
	 * privileges wider regions over smaller ones.
	 *
	 * <pre>
	 * g(∇I) = 1 / ( 1 + (||∇I/κ||²) )
	 * </pre>
	 *
	 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
	 */
	public static class WideRegionFunction implements ConductionCoefficientFunction {
		private float kappa;

		/**
		 * Construct with the given kappa value
		 *
		 * @param kappa
		 *            kappa
		 */
		public WideRegionFunction(float kappa) {
			this.kappa = kappa;
		}

		@Override
		public float apply(float val, int x, int y) {
			final float t = val / kappa;
			return 1 / (1 + t * t);
		}
	};

	protected int numIterations;
	protected float lambda = 1f / 7f;
	protected ConductionCoefficientFunction function;
	protected ConnectedComponent.ConnectMode neighbourMode;

	/**
	 * Construct using the given parameters.
	 *
	 * @param numIterations
	 *            number of iterations of filtering to apply
	 * @param lambda
	 *            the integration constant (less than or equal to 1/4 for
	 *            4-neighbour mode and 1/8 for 8-neighbour)
	 * @param function
	 *            the conduction coefficient function
	 * @param neighbourMode
	 *            the neighbourhood mode
	 */
	public AnisotropicDiffusion(int numIterations, float lambda, ConductionCoefficientFunction function,
			ConnectMode neighbourMode)
	{
		this.numIterations = numIterations;
		this.lambda = lambda;
		this.function = function;
		this.neighbourMode = neighbourMode;
	}

	/**
	 * Construct using the given parameters. 4-neighbour mode is used as per the
	 * original paper.
	 *
	 * @param numIterations
	 *            number of iterations of filtering to apply
	 * @param lambda
	 *            the integration constant (bigger than 0 and less than or equal
	 *            to 1/4)
	 * @param function
	 *            the conduction coefficient function
	 */
	public AnisotropicDiffusion(int numIterations, float lambda, ConductionCoefficientFunction function)
	{
		this.numIterations = numIterations;
		this.lambda = lambda;
		this.function = function;
		this.neighbourMode = ConnectMode.CONNECT_4;
	}

	@Override
	public void processImage(FImage image) {
		for (int i = 0; i < numIterations; i++) {
			processImageOneIteration(image);
		}
	}

	/**
	 * Perform a single iteration of anisotropic diffusion
	 *
	 * @param image
	 *            the image
	 */
	public void processImageOneIteration(FImage image) {
		switch (neighbourMode) {
		case CONNECT_4:
			processImageOneIteration4(image);
		case CONNECT_8:
			processImageOneIteration8(image);
		}
	}

	private void processImageOneIteration4(FImage image) {
		final float[][] tmp = image.clone().pixels;

		for (int y = 0; y < image.height; y++) {
			final int ym = Math.max(y - 1, 0);
			final int yp = Math.min(y + 1, image.height - 1);

			for (int x = 0; x < image.width; x++) {
				final int xm = Math.max(x - 1, 0);
				final int xp = Math.min(x + 1, image.width - 1);

				final float dN = tmp[ym][x] - tmp[y][x];
				final float dS = tmp[yp][x] - tmp[y][x];
				final float dE = tmp[y][xm] - tmp[y][x];
				final float dW = tmp[y][xp] - tmp[y][x];

				final float cN = function.apply(Math.abs(dN), x, y);
				final float cS = function.apply(Math.abs(dS), x, y);
				final float cE = function.apply(Math.abs(dE), x, y);
				final float cW = function.apply(Math.abs(dW), x, y);

				image.pixels[y][x] += lambda * (cN * dN + cS * dS + cE * dE + cW * dW);
			}
		}
	}

	private void processImageOneIteration8(FImage image) {
		final float[][] tmp = image.clone().pixels;
		final float wt = 0.5f;

		for (int y = 0; y < image.height; y++) {
			final int ym = Math.max(y - 1, 0);
			final int yp = Math.min(y + 1, image.height - 1);

			for (int x = 0; x < image.width; x++) {
				final int xm = Math.max(x - 1, 0);
				final int xp = Math.min(x + 1, image.width - 1);

				final float dN = tmp[ym][x] - tmp[y][x];
				final float dS = tmp[yp][x] - tmp[y][x];
				final float dE = tmp[y][xm] - tmp[y][x];
				final float dW = tmp[y][xp] - tmp[y][x];
				final float dNE = tmp[ym][xm] - tmp[y][x];
				final float dSE = tmp[yp][xm] - tmp[y][x];
				final float dSW = tmp[ym][xp] - tmp[y][x];
				final float dNW = tmp[ym][xp] - tmp[y][x];

				final float cN = function.apply(Math.abs(dN), x, y);
				final float cS = function.apply(Math.abs(dS), x, y);
				final float cE = function.apply(Math.abs(dE), x, y);
				final float cW = function.apply(Math.abs(dW), x, y);
				final float cNE = function.apply(Math.abs(dNE), x, y);
				final float cSE = function.apply(Math.abs(dSE), x, y);
				final float cSW = function.apply(Math.abs(dSW), x, y);
				final float cNW = function.apply(Math.abs(dNW), x, y);

				image.pixels[y][x] += lambda
						* (cN * dN + cS * dS + cE * dE + cW * dW + wt * (cNE * dNE + cSE * dSE + cSW * dSW + cNW * dNW));
			}
		}
	}

	// public static void main(String[] args) throws Exception {
	// final MBFImage img = ImageUtilities.readMBF(new
	// File("/Users/jon/veg.jpg"));
	// final AnisotropicDiffusion ad = new AnisotropicDiffusion(20, 0.1f, new
	// HighConstrastEdgeFunction(30f / 255));
	//
	// DisplayUtilities.display(img);
	// DisplayUtilities.display(img.process(ad));
	// }
}
