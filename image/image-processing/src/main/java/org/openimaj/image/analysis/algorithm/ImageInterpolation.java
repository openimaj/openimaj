package org.openimaj.image.analysis.algorithm;

import org.openimaj.image.FImage;
import org.openimaj.image.analyser.ImageAnalyser;
import org.openimaj.math.util.Interpolation;

/**
 * An {@link ImageAnalyser} that can provide interpolate pixel values using a
 * variety of interpolation approaches.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 */
public class ImageInterpolation implements ImageAnalyser<FImage> {
	/**
	 * Interface defining an object capable of performing pixel interpolation
	 * 
	 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
	 * 
	 */
	public static interface Interpolator {
		/**
		 * Interpolate a pixel value
		 * 
		 * @param x
		 *            the x-ordinate
		 * @param y
		 *            the y-ordinate
		 * @param image
		 *            the image
		 * @param workingSpace
		 *            the working space required
		 * @return the interpolated pixel value
		 */
		public float interpolate(float x, float y, FImage image, Object workingSpace);

		/**
		 * Create the working space required for interpolation
		 * 
		 * @return the working space (can be <code>null</code>)
		 */
		public Object createWorkingSpace();
	}

	/**
	 * Standard interpolation types.
	 * 
	 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
	 * 
	 */
	public static enum InterpolationType implements Interpolator {
		/**
		 * Nearest neighbour interpolation
		 */
		NEAREST_NEIGHBOUR {
			@Override
			public float interpolate(float x, float y, FImage image, Object workingSpace) {
				x = Math.round(x);
				y = Math.round(y);

				if (x < 0 || x >= image.width || y < 0 || y >= image.height)
					return 0;

				return image.pixels[(int) y][(int) x];
			}

			@Override
			public Object createWorkingSpace() {
				return null;
			}
		},
		/**
		 * Bilinear interpolation
		 */
		BILINEAR {
			@Override
			public float interpolate(float x, float y, FImage image, Object workingSpace) {
				return image.getPixelInterpNative(x, y, 0);
			}

			@Override
			public Object createWorkingSpace() {
				return null;
			}
		},
		/**
		 * Bicubic interpolation
		 */
		BICUBIC {
			@Override
			public float interpolate(float x, float y, FImage image, Object workingSpace) {
				final float[][] working = (float[][]) workingSpace;

				final int sx = (int) Math.floor(x) - 1;
				final int sy = (int) Math.floor(y) - 1;
				final int ex = sx + 3;
				final int ey = sy + 3;

				for (int yy = sy, i = 0; yy <= ey; yy++, i++) {
					for (int xx = sx, j = 0; xx <= ex; xx++, j++) {
						final int px = xx < 0 ? 0 : xx >= image.width ? image.width - 1 : xx;
						final int py = yy < 0 ? 0 : yy >= image.height ? image.height - 1 : yy;

						working[i][j] = image.pixels[py][px];
					}
				}

				final float dx = (float) (x - Math.floor(x));
				final float dy = (float) (y - Math.floor(y));
				return Interpolation.bicubicInterp(dx, dy, working);
			}

			@Override
			public Object createWorkingSpace() {
				return new float[4][4];
			}
		};
	}

	protected Interpolator interpolator;
	protected Object workingSpace;
	protected FImage image;

	/**
	 * Default constructor.
	 * 
	 * @param interpolator
	 *            the interpolator to use
	 */
	public ImageInterpolation(Interpolator interpolator) {
		this.interpolator = interpolator;
		this.workingSpace = interpolator.createWorkingSpace();
	}

	@Override
	public void analyseImage(FImage image) {
		this.image = image;
	}

	/**
	 * Get the interpolated pixel value of the previously analysed image
	 * 
	 * @param x
	 *            the x-ordinate
	 * @param y
	 *            the y-ordinate
	 * @return the interpolated pixel value
	 */
	public float getPixelInterpolated(float x, float y) {
		return interpolator.interpolate(x, y, image, workingSpace);
	}
}
