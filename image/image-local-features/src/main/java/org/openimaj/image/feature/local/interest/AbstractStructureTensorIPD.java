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
package org.openimaj.image.feature.local.interest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.openimaj.image.FImage;
import org.openimaj.image.pixel.FValuePixel;
import org.openimaj.image.pixel.Pixel;
import org.openimaj.image.processing.convolution.BasicDerivativeKernels;
import org.openimaj.image.processing.convolution.FGaussianConvolve;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.math.util.FloatArrayStatsUtils;

import Jama.Matrix;

/**
 * Abstract base class for an interest point detector which uses derivatives or
 * the (multiscale) structure tensor.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 */
public abstract class AbstractStructureTensorIPD implements
		InterestPointDetector<InterestPointData>
{

	protected int borderSkip;
	FImage originalImage;
	FImage l, lx, ly, lxmx, lymy, lxmy;
	public FImage lxmxblur, lymyblur, lxmyblur;

	protected float detectionScale;
	protected float integrationScale;
	protected float detIntScaleFactor = 1.4f;

	protected List<Maxima> maxima;

	private boolean blurred;

	/**
	 * Set the scale factor between the integration scale and the detection
	 * scale. When detection scale is set, integration scale = detIntScaleFactor
	 * * detectionScale
	 * 
	 * @param detIntScaleFactor
	 */
	public AbstractStructureTensorIPD(float detIntScaleFactor) {
		this.detIntScaleFactor = detIntScaleFactor;
		this.borderSkip = 2;
	}

	/**
	 * Abstract structure tensor detected at a given scale, the first
	 * derivatives found and a structure tensor combined from these first
	 * derivatives with a gaussian window of sigma = integrationScale
	 * 
	 * @param detectionScale
	 * @param integrationScale
	 */
	public AbstractStructureTensorIPD(float detectionScale,
			float integrationScale)
	{
		this(detectionScale, integrationScale, 2, false);
	}

	/**
	 * Abstract structure tensor detected at a given scale, the first
	 * derivatives found and a structure tensor combined from these first
	 * derivatives with a gaussian window of sigma = integrationScale. Also
	 * state whether the image in from which features are extracted is already
	 * blurred to the detection scale, if not it will be blurred to the correct
	 * level
	 * 
	 * @param detectionScale
	 * @param integrationScale
	 * @param blurred
	 */
	public AbstractStructureTensorIPD(float detectionScale,
			float integrationScale, boolean blurred)
	{
		this(detectionScale, integrationScale, 2, blurred);
	}

	/**
	 * Abstract structure tensor detected at a given scale, the first
	 * derivatives found and a structure tensor combined from these first
	 * derivatives with a gaussian window of sigma = integrationScale. Also
	 * specify how many pixels to skip around the edge of the image. The kernel
	 * used to extract edges results in a black border so some pixels are better
	 * ignored in terms of corner detection.
	 * 
	 * @param detectionScale
	 * @param integrationScale
	 * @param borderSkip
	 */
	public AbstractStructureTensorIPD(float detectionScale,
			float integrationScale, int borderSkip)
	{
		this(detectionScale, integrationScale, borderSkip,
				false);
	}

	/**
	 * Abstract structure tensor detected at a given scale, the first
	 * derivatives found and a structure tensor combined from these first
	 * derivatives with a gaussian window of sigma = integrationScale. Also
	 * specify how many pixels to skip around the edge of the image. The kernel
	 * used to extract edges results in a black border so some pixels are better
	 * ignored in terms of corner detection. Also state whether the image in
	 * from which features are extracted is already blurred to the detection
	 * scale, if not it will be blurred to the correct level
	 * 
	 * @param detectionScale
	 * @param integrationScale
	 * @param borderSkip
	 * @param blurred
	 */
	public AbstractStructureTensorIPD(float detectionScale,
			float integrationScale, int borderSkip, boolean blurred)
	{
		this.blurred = blurred;
		if (borderSkip < 1)
			borderSkip = 1;

		this.detectionScale = detectionScale;
		this.integrationScale = integrationScale;
		this.borderSkip = borderSkip;
	}

	public void prepareInterestPoints(FImage image) {
		originalImage = image;
		// // Add padding around the edges of the image (4 pixels all the way
		// around)
		// image = image.padding(4,4);
		// l = image.clone().processInplace(new
		// FDiscGausConvolve(detectionScale));
		// lx =
		// l.process(BasicDerivativeKernels.DX_KERNEL).extractROI(4,4,this.originalImage.getWidth(),
		// this.originalImage.getHeight()).multiplyInplace((float)Math.sqrt(detectionScale));
		// ly =
		// l.process(BasicDerivativeKernels.DY_KERNEL).extractROI(4,4,this.originalImage.getWidth(),
		// this.originalImage.getHeight()).multiplyInplace((float)Math.sqrt(detectionScale));

		l = image;
		if (!this.blurred)
			l = l.processInplace(new FGaussianConvolve(detectionScale));
		lx = l.process(BasicDerivativeKernels.DX_KERNEL).multiplyInplace(this.detectionScale);
		ly = l.process(BasicDerivativeKernels.DY_KERNEL).multiplyInplace(this.detectionScale);

		lxmx = lx.multiply(lx);
		lymy = ly.multiply(ly);
		lxmy = lx.multiply(ly);
		final FGaussianConvolve intConv = new FGaussianConvolve(integrationScale);
		lxmxblur = lxmx.clone().processInplace(intConv);
		lymyblur = lymy.clone().processInplace(intConv);
		lxmyblur = lxmy.clone().processInplace(intConv);
	}

	public void printStructureTensorStats() {
		System.out.format("Structure tensor stats for sd/si = %4.2f/%4.2f\n",
				detectionScale, integrationScale);
		System.out.format(
				"\tlxmx mean/std = %4.2e/%4.2e max/min = %4.2e/%4.2e\n",
				FloatArrayStatsUtils.mean(lxmxblur.pixels),
				FloatArrayStatsUtils.std(lxmxblur.pixels), lxmx.max(),
				lxmx.min());
		System.out.format(
				"\tlxmy mean/std = %4.2e/%4.2e max/min = %4.2e/%4.2e\n",
				FloatArrayStatsUtils.mean(lxmyblur.pixels),
				FloatArrayStatsUtils.std(lxmyblur.pixels), lxmy.max(),
				lxmy.min());
		System.out.format(
				"\tlymy mean/std = %4.2e/%4.2e max/min = %4.2e/%4.2e\n",
				FloatArrayStatsUtils.mean(lymyblur.pixels),
				FloatArrayStatsUtils.std(lymyblur.pixels), lymy.max(),
				lymy.min());
	}

	@Override
	public void findInterestPoints(FImage image) {

		this.prepareInterestPoints(image);
		final FImage cornerImage = createInterestPointMap();

		detectMaxima(cornerImage, image.getBounds());
	}

	@Override
	public void findInterestPoints(FImage image, Rectangle window) {

		this.prepareInterestPoints(image);
		final FImage cornerImage = createInterestPointMap();
		System.out.format(
				"corner image mean/std = %4.2e/%4.2e max/min = %4.2e/%4.2e\n",
				FloatArrayStatsUtils.mean(cornerImage.pixels),
				FloatArrayStatsUtils.std(cornerImage.pixels),
				cornerImage.max(), cornerImage.min());

		detectMaxima(cornerImage, window);
	}

	public FValuePixel findMaximum(Rectangle window) {
		final FImage cornerImage = createInterestPointMap();
		final FValuePixel c = cornerImage.extractROI(window).maxPixel();
		c.translate(window.x, window.y);
		return c;
	}

	public class Maxima {
		public int x, y;
		public float val;

		public Maxima(int x, int y, float v) {
			this.x = x;
			this.y = y;
			this.val = v;
		}
	}

	protected void detectMaxima(FImage image, Rectangle window) {
		maxima = new ArrayList<Maxima>();

		for (int y = borderSkip; y < image.height - borderSkip; y++) {
			for (int x = borderSkip; x < image.width - borderSkip; x++) {
				if (!window.isInside(new Pixel(x, y)))
					continue;
				final float curr = image.pixels[y][x];
				if (curr > image.pixels[y - 1][x - 1]
						&& curr >= image.pixels[y - 1][x]
						&& curr >= image.pixels[y - 1][x + 1]
						&& curr >= image.pixels[y][x - 1]
						&& curr >= image.pixels[y][x + 1]
						&& curr >= image.pixels[y + 1][x - 1]
						&& curr >= image.pixels[y + 1][x]
						&& curr >= image.pixels[y + 1][x + 1])
				{
					maxima.add(new Maxima(x, y, curr));
				}
			}
		}

		Collections.sort(maxima, new Comparator<Maxima>() {
			@Override
			public int compare(Maxima o1, Maxima o2) {
				if (o1.val == o2.val)
					return 0;
				return o1.val < o2.val ? 1 : -1;
			}
		});
	}

	public abstract FImage createInterestPointMap();

	@Override
	public List<InterestPointData> getInterestPoints(int npoints) {
		if (npoints < 0 || npoints > maxima.size())
			npoints = maxima.size();
		final List<InterestPointData> ipdata = new ArrayList<InterestPointData>();

		for (int i = 0; i < npoints; i++) {
			final InterestPointData ipd = new InterestPointData();

			ipd.x = maxima.get(i).x;
			ipd.y = maxima.get(i).y;
			ipd.scale = integrationScale;
			ipd.score = maxima.get(i).val;

			ipdata.add(ipd);
		}

		return ipdata;
	}

	public float getDetIntScaleFactor() {
		return detIntScaleFactor;
	}

	public void setDetIntScaleFactor(float detIntScaleFactor) {
		this.detIntScaleFactor = detIntScaleFactor;
	}

	public float getDetectionScale() {
		return detectionScale;
	}

	public void setImageBlurred(boolean blurred) {
		this.blurred = blurred;
	}

	@Override
	public void setDetectionScale(float detectionScale) {
		this.detectionScale = detectionScale;
		this.integrationScale = this.detectionScale * this.detIntScaleFactor;
	}

	public float getIntegrationScale() {
		return integrationScale;
	}

	@Override
	public void setIntegrationScale(float integrationScale) {
		this.integrationScale = integrationScale;
		this.detectionScale = integrationScale * (1f / this.detIntScaleFactor);
	}

	@Override
	public List<InterestPointData> getInterestPoints() {
		return getInterestPoints(-1);
	}

	@Override
	public List<InterestPointData> getInterestPoints(float threshold) {
		return getInterestPointsThresh(threshold);
	}

	public List<InterestPointData> getInterestPointsThresh(float thresh) {
		final List<InterestPointData> ipdata = new ArrayList<InterestPointData>();

		for (final Maxima m : maxima) {
			if (m.val < thresh)
				continue;

			final InterestPointData ipd = new InterestPointData();

			ipd.x = m.x;
			ipd.y = m.y;
			ipd.scale = integrationScale;
			ipd.score = m.val;

			ipdata.add(ipd);
		}

		return ipdata;
	}

	public Matrix getSecondMomentsAt(int x, int y) {
		final Matrix secondMoments = new Matrix(2, 2);
		secondMoments.set(0, 0, lxmxblur.pixels[y][x]);
		secondMoments.set(0, 1, lxmyblur.pixels[y][x]);
		secondMoments.set(1, 0, lxmyblur.pixels[y][x]);
		secondMoments.set(1, 1, lymyblur.pixels[y][x]);
		return secondMoments;
	}

	@Override
	public AbstractStructureTensorIPD clone() {
		AbstractStructureTensorIPD a = null;
		try {
			a = (AbstractStructureTensorIPD) super.clone();
		} catch (final CloneNotSupportedException e) {
			return null;
		}
		return a;
	}

	public int pointsFound() {
		return this.maxima.size();
	}
}
