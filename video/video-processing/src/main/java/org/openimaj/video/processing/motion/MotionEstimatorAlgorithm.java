/**
 * Copyright (c) 2011, The University of Southampton and the individual contributors.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *   * 	Redistributions of source code must retain the above copyright notice,
 * 	this list of conditions and the following disclaimer.
 *
 *   *	Redistributions in binary form must reproduce the above copyright notice,
 * 	this list of conditions and the following disclaimer in the documentation
 * 	and/or other materials provided with the distribution.
 *
 *   *	Neither the name of the University of Southampton nor the names of its
 * 	contributors may be used to endorse or promote products derived from this
 * 	software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.openimaj.video.processing.motion;

import org.apache.commons.math.complex.Complex;
import org.apache.commons.math.linear.Array2DRowFieldMatrix;
import org.apache.commons.math.linear.FieldLUDecompositionImpl;
import org.openimaj.image.FImage;
import org.openimaj.image.analysis.algorithm.TemplateMatcher;
import org.openimaj.image.analysis.algorithm.TemplateMatcher.Mode;
import org.openimaj.image.pixel.FValuePixel;
import org.openimaj.image.processing.algorithm.FourierTransform;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.video.VideoFrame;
import org.openimaj.video.VideoSubFrame;

import edu.emory.mathcs.jtransforms.fft.FloatFFT_2D;

/**
 * A set of algorithms for the motion estimator.
 *
 * @author David Dupplaw (dpd@ecs.soton.ac.uk)
 * @created 1 Mar 2012
 *
 */
public abstract class MotionEstimatorAlgorithm
{

	/**
	 * Within a search window around the subimages detect most likely match and
	 * thus motion.
	 *
	 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
	 *
	 */
	public static class TEMPLATE_MATCH extends MotionEstimatorAlgorithm {
		private float searchProp;
		private Mode mode;

		/**
		 * Defaults to allowing a maximum of templatesize/2 movement using the
		 * {@link Mode#CORRELATION}
		 */
		public TEMPLATE_MATCH() {
			this.searchProp = .5f;
			this.mode = TemplateMatcher.Mode.NORM_SUM_SQUARED_DIFFERENCE;
		}

		/**
		 * Given the template's size, search around a border of size
		 * template*searchWindowBorderProp
		 *
		 * @param searchWindowBorderProp
		 * @param mode
		 *            the matching mode
		 */
		public TEMPLATE_MATCH(float searchWindowBorderProp, Mode mode) {
			this.searchProp = searchWindowBorderProp;
			this.mode = mode;
		}

		@Override
		Point2d estimateMotion(VideoSubFrame<FImage> img1sub,
				VideoSubFrame<FImage>... imagesSub)
		{

			final VideoFrame<FImage> current = img1sub.extract();
			final VideoFrame<FImage> prev = imagesSub[0];
			final Rectangle prevSearchRect = imagesSub[0].roi;

			final int sw = (int) (prevSearchRect.width * this.searchProp);
			final int sh = (int) (prevSearchRect.height * this.searchProp);
			final int sx = (int) (prevSearchRect.x + ((prevSearchRect.width - sw) / 2.f));
			final int sy = (int) (prevSearchRect.y + ((prevSearchRect.height - sh) / 2.f));

			final Rectangle searchRect = new Rectangle(sx, sy, sw, sh);
			// System.out.println("Search window: " + searchRect);
			// MBFImage searchRectDraw = new
			// MBFImage(img1sub.frame.clone(),img1sub.frame.clone(),img1sub.frame.clone());
			// searchRectDraw.drawShape(searchRect, RGBColour.RED);
			// searchRectDraw.drawPoint(img1sub.roi.getCOG(), RGBColour.GREEN,
			// 3);
			final TemplateMatcher matcher = new TemplateMatcher(current.frame, mode, searchRect);
			matcher.analyseImage(prev.frame);
			final FValuePixel[] responses = matcher.getBestResponses(1);
			final FValuePixel firstBest = responses[0];
			// for (FValuePixel bestRespose : responses) {
			// if(bestRespose == null) continue;
			// if(firstBest == null) firstBest = bestRespose;
			// bestRespose.translate(current.frame.width/2,
			// current.frame.height/2);
			// // searchRectDraw.drawPoint(bestRespose, RGBColour.BLUE, 3);
			// }
			final Point2d centerOfGrid = img1sub.roi.calculateCentroid();
			// System.out.println("First reponse: " + firstBest );
			// System.out.println("Center of template: " + centerOfGrid);

			// DisplayUtilities.displayName(searchRectDraw, "searchWindow");
			if (firstBest == null || Float.isNaN(firstBest.value))
				return new Point2dImpl(0, 0);
			// firstBest.translate(current.frame.width/2,
			// current.frame.height/2);
			// System.out.println("First reponse (corrected): " + firstBest );
			// System.out.println("Diff: " + centerOfGrid.minus(firstBest));
			return centerOfGrid.minus(firstBest);
		}
	}

	/**
	 * Basic phase correlation algorithm that finds peaks in the cross-power
	 * spectrum between two images. This is the basic implementation without
	 * sub-pixel accuracy.
	 */
	public static class PHASE_CORRELATION extends MotionEstimatorAlgorithm
	{
		/**
		 * Calculate the estimated motion vector between <code>images</code>
		 * which [0] is first in the sequence and <code>img2</code> which is
		 * second in the sequence. This method uses phase correlation - the fact
		 * that translations in space can be seen as shifts in phase in the
		 * frequency domain. The returned vector will have a maximum horizontal
		 * displacement of <code>img2.getWidth()/2</code> and a minimum
		 * displacement of <code>-img2.getWidth()/2</code> and similarly for the
		 * vertical displacement and height.
		 *
		 * @param img2sub
		 *            The second image in the sequence
		 * @param imagesSub
		 *            The previous image in the sequence
		 * @return the estimated motion vector as a {@link Point2d} in absolute
		 *         x and y coordinates.
		 */
		@Override
		public Point2d estimateMotion(VideoSubFrame<FImage> img2sub,
				VideoSubFrame<FImage>... imagesSub)
		{
			// The previous image will be the first in the images array
			final FImage img1 = imagesSub[0].extract().frame;
			final VideoFrame<FImage> img2 = img2sub.extract();

			// No previous frame?
			if (img1 == null)
				return new Point2dImpl(0, 0);

			// The images must have comparable shapes and must be square
			if (img1.getRows() != img2.frame.getRows() ||
					img1.getCols() != img2.frame.getCols() ||
					img1.getCols() != img2.frame.getRows())
				return new Point2dImpl(0, 0);

			// Prepare and perform an FFT for each of the incoming images.
			final int h = img1.getRows();
			final int w = img1.getCols();

			try
			{
				final FloatFFT_2D fft1 = new FloatFFT_2D(h, w);
				final FloatFFT_2D fft2 = new FloatFFT_2D(h, w);
				final float[][] data1 = FourierTransform.prepareData(img1, h, w, false);
				final float[][] data2 = FourierTransform.prepareData(img2.frame, h, w, false);
				fft1.complexForward(data1);
				fft2.complexForward(data2);

				// Multiply (element-wise) the fft and the conjugate of the fft.
				Complex[][] cfft = new Complex[h][w];
				for (int y = 0; y < h; y++)
				{
					for (int x = 0; x < w; x++)
					{
						final float re1 = data1[y][x * 2];
						final float im1 = data1[y][1 + x * 2];
						final float re2 = data2[y][x * 2];
						final float im2 = data2[y][1 + x * 2];

						final Complex c1 = new Complex(re1, im1);
						final Complex c2 = new Complex(re2, -im2);
						cfft[y][x] = c1.multiply(c2);
					}
				}

				// ----------------------------------------
				// Normalise by the determinant
				// ----------------------------------------
				// First calculate the determinant
				final Array2DRowFieldMatrix<Complex> cmat =
						new Array2DRowFieldMatrix<Complex>(cfft);
				final FieldLUDecompositionImpl<Complex> luDecomp =
						new FieldLUDecompositionImpl<Complex>(cmat);
				final Complex det = luDecomp.getDeterminant();
				cmat.scalarMultiply(new Complex(1d / det.abs(), 0));

				// Convert back to an array for doing the inverse FFTing
				cfft = cmat.getData();
				for (int y = 0; y < h; y++)
				{
					for (int x = 0; x < w; x++)
					{
						data1[y][x * 2] = (float) cfft[y][x].getReal();
						data1[y][1 + x * 2] = (float) cfft[y][x].getImaginary();
					}
				}

				// Perform the inverse FFT
				fft1.complexInverse(data1, false);

				// Get the data back out
				FourierTransform.unprepareData(data1, img1, false);

				// Get the estimated motion vector from the peak in the space
				final FValuePixel p = img1.maxPixel();
				return new Point2dImpl(
						-(p.x > w / 2 ? p.x - w : p.x),
						-(p.y > w / 2 ? p.y - w : p.y));
			} catch (final Exception e)
			{
				return new Point2dImpl(0, 0);
			}
		}
	};

	/**
	 * Estimate the motion to the given subimage, <code>img1sub</code> from the
	 * previous frames. The previous frames will be given in reverse order so
	 * that imagesSub[0] will be the previous frame, imagesSub[1] the frame
	 * before that, etc. The number of frames given will be at most that given
	 * by {@link #requiredNumberOfFrames()}. It could be less if at the
	 * beginning of the video. If you require more frames, return an empty
	 * motion vector - that is (0,0).
	 *
	 * @param img1sub
	 *            The image to which we want to estimate the motion.
	 * @param imagesSub
	 *            The previous frames in reverse order
	 * @return The estimated motion vector.
	 */
	abstract Point2d estimateMotion(VideoSubFrame<FImage> img1sub,
			VideoSubFrame<FImage>... imagesSub);

	/**
	 * The required number of frames required for the given motion estimation
	 * algorithm to work. The default is 1 which means the algorithm will only
	 * receive the previous frame. If more are required, override this method
	 * and return the required number.
	 *
	 * @return The required number of frames to pass to the algorithm.
	 */
	protected int requiredNumberOfFrames()
	{
		return 1;
	}
}
