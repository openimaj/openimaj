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

package uk.ac.soton.ecs.dpd.ir.filters;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.MemoryImageSource;
import java.awt.image.PixelGrabber;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;

import uk.ac.soton.ecs.dpd.ir.utils.Options;

/**
 * @author David Dupplaw <dpd@ecs.soton.ac.uk>
 * @param <Q> Image type
 */
public class CannyEdgeDetector<Q extends org.openimaj.image.Image<?,Q>> implements ImageFilter<Q>
{
	private boolean complete;
	private Options o;

	/**
	 * 
	 */
	public CannyEdgeDetector() 
	{
		o = new Options();
		o.addOption( "Threshold", new Integer(128) );
		o.addOption( "Hyst.Threshold 1", new Integer(50) );
		o.addOption( "Hyst.Threshold 2", new Integer(230) );
		o.addOption( "Gaussian Kernel Size", new Integer(15) );

		complete = false;
	}

	@Override
	public BufferedImage filter( BufferedImage i )
	{
		setSourceImage( i );

		try
		{
			process();
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}

		return getEdgeImage();
	}

	@Override
	public Options getOptions()
	{
		return o;
	}

	@Override
	public String getName()
	{
		return "Canny Edge Detector";
	}

	@Override
	public boolean isImageReady()
	{
		return complete;
	}

	/**
	 * @throws Exception
	 */
	public void process() throws Exception 
	{
		complete = false;

		int widGaussianKernel = ((Integer)o.getOption("Gaussian Kernel Size")).intValue();
		int threshold = ((Integer)o.getOption("Threshold")).intValue();
		int threshold1 = ((Integer)o.getOption("Hyst.Threshold 1")).intValue();
		int threshold2 = ((Integer)o.getOption("Hyst.Threshold 2")).intValue();

		if (threshold < 0 || threshold > 255)
			throw new Exception("The value of the threshold is out of its valid range.");
		if (widGaussianKernel < 3 || widGaussianKernel > 40)
			throw new Exception("The value of the widGaussianKernel is out of its valid range.");
		width = sourceImage.getWidth(null);
		height = sourceImage.getHeight(null);
		picsize = width * height;
		data = new int[picsize];
		magnitude = new int[picsize];
		orientation = new int[picsize];
		float f = 1.0F;
		canny_core(f, widGaussianKernel);
		
		thresholding_tracker(threshold1, threshold2);
		for (int i = 0; i < picsize; i++)
			if (data[i] > threshold)
				data[i] = 0xff000000;
			else
				data[i] = -1;
 
		edgeImage = pixels2image(data);
		data = null;

		complete = true;
	}
 
	protected void display(int [] data) {
		FImage tmp = new FImage(width, height);
		for (int r=0; r<height; r++)
			for (int c=0; c<width; c++)
				tmp.pixels[r][c] = data[c + r*width] / 255f;
		DisplayUtilities.display(tmp);
	}
	
	protected void display(float [] data) {
		FImage tmp = new FImage(width, height);
		for (int r=0; r<height; r++)
			for (int c=0; c<width; c++)
				tmp.pixels[r][c] = data[c + r*width] / 255f;
		DisplayUtilities.display(tmp);
	}
	
	private void canny_core(float f, int i) {
//		boolean flag = false;
//		boolean flag1 = false;
		derivative_mag = new int[picsize];
		float af4[] = new float[i];
		float af5[] = new float[i];
		float af6[] = new float[i];
		data = image2pixels(sourceImage);
		int k4 = 0;
		do {
			if (k4 >= i)
				break;
			float f1 = gaussian(k4, f);
			if (f1 <= 0.005F && k4 >= 2)
				break;
			float f2 = gaussian(k4 - 0.5F, f);
			float f3 = gaussian(k4 + 0.5F, f);
			float f4 = gaussian(k4, f * 0.5F);
			af4[k4] = (f1 + f2 + f3) / 3F / (6.283185F * f * f);
			af5[k4] = f3 - f2;
			af6[k4] = 1.6F * f4 - f1;
			k4++;
		} while (true);
		
		int j = k4;
		float af[] = new float[picsize];
		float af1[] = new float[picsize];
		int j1 = width - (j - 1);
		int l = width * (j - 1);
		int i1 = width * (height - (j - 1));
		for (int l4 = j - 1; l4 < j1; l4++) {
			for (int l5 = l; l5 < i1; l5 += width) {
				int k1 = l4 + l5;
				float f8 = data[k1] * af4[0];
				float f10 = f8;
				int l6 = 1;
				int k7 = k1 - width;
				for (int i8 = k1 + width; l6 < j; i8 += width) {
					f8 += af4[l6] * (data[k7] + data[i8]);
					f10 += af4[l6] * (data[k1 - l6] + data[k1 + l6]);
					l6++;
					k7 -= width;
				}
 
				af[k1] = f8;
				af1[k1] = f10;
			}
 
		}

		float af2[] = new float[picsize];
		for (int i5 = j - 1; i5 < j1; i5++) {
			for (int i6 = l; i6 < i1; i6 += width) {
				float f9 = 0.0F;
				int l1 = i5 + i6;
				for (int i7 = 1; i7 < j; i7++)
					f9 += af5[i7] * (af[l1 - i7] - af[l1 + i7]);
 
				af2[l1] = f9;
			}
 
		}
		
		af = null;
		float af3[] = new float[picsize];
		for (int j5 = k4; j5 < width - k4; j5++) {
			for (int j6 = l; j6 < i1; j6 += width) {
				float f11 = 0.0F;
				int i2 = j5 + j6;
				int j7 = 1;
				for (int l7 = width; j7 < j; l7 += width) {
					f11 += af5[j7] * (af1[i2 - l7] - af1[i2 + l7]);
					j7++;
				}
 
				af3[i2] = f11;
			}
 
		}
 
		display(af3);
		
		af1 = null;
		j1 = width - j;
		l = width * j;
		i1 = width * (height - j);
		for (int k5 = j; k5 < j1; k5++) {
			for (int k6 = l; k6 < i1; k6 += width) {
				int j2 = k5 + k6;
				int k2 = j2 - width;
				int l2 = j2 + width;
				int i3 = j2 - 1;
				int j3 = j2 + 1;
				int k3 = k2 - 1;
				int l3 = k2 + 1;
				int i4 = l2 - 1;
				int j4 = l2 + 1;
				float f6 = af2[j2];
				float f7 = af3[j2];
				float f12 = hypotenuse(f6, f7);
				int k = (int) (f12 * 20D);
				derivative_mag[j2] = k >= 256 ? 255 : k;
				float f13 = hypotenuse(af2[k2], af3[k2]);
				float f14 = hypotenuse(af2[l2], af3[l2]);
				float f15 = hypotenuse(af2[i3], af3[i3]);
				float f16 = hypotenuse(af2[j3], af3[j3]);
				float f18 = hypotenuse(af2[l3], af3[l3]);
				float f20 = hypotenuse(af2[j4], af3[j4]);
				float f19 = hypotenuse(af2[i4], af3[i4]);
				float f17 = hypotenuse(af2[k3], af3[k3]);
				float f5;
				if (f6 * f7 <= 0
					? Math.abs(f6) >= Math.abs(f7)
					? (f5 = Math.abs(f6 * f12))
						>= Math.abs(f7 * f18 - (f6 + f7) * f16)
					&& f5
						> Math.abs(f7 * f19 - (f6 + f7) * f15) : (
							f5 = Math.abs(f7 * f12))
						>= Math.abs(f6 * f18 - (f7 + f6) * f13)
					&& f5
						> Math.abs(f6 * f19 - (f7 + f6) * f14) : Math.abs(f6)
						>= Math.abs(f7)
						? (f5 = Math.abs(f6 * f12))
							>= Math.abs(f7 * f20 + (f6 - f7) * f16)
					&& f5
						> Math.abs(f7 * f17 + (f6 - f7) * f15) : (
							f5 = Math.abs(f7 * f12))
						>= Math.abs(f6 * f20 + (f7 - f6) * f14)
					&& f5 > Math.abs(f6 * f17 + (f7 - f6) * f13)) {
					magnitude[j2] = derivative_mag[j2];
					orientation[j2] = (int) (Math.atan2(f7, f6) * 40F);
				}
			}
 
		}

		derivative_mag = null;
		af2 = null;
		af3 = null;
	}
 
	private float hypotenuse(float f, float f1) {
		if (f == 0.0F && f1 == 0.0F)
			return 0.0F;
		else
			return (float) Math.sqrt(f * f + f1 * f1);
	}
 
	private float gaussian(float f, float f1) {
		return (float) Math.exp((-f * f) / (2 * f1 * f1));
	}
 
	private void thresholding_tracker(int i, int j) {
		for (int k = 0; k < picsize; k++)
			data[k] = 0;
 
		for (int l = 0; l < width; l++) {
			for (int i1 = 0; i1 < height; i1++)
				if (magnitude[l + width * i1] >= i)
					follow(l, i1, j);
 
		}
 
	}
 
	private boolean follow(int i, int j, int k) {
		int j1 = i + 1;
		int k1 = i - 1;
		int l1 = j + 1;
		int i2 = j - 1;
		int j2 = i + j * width;
		if (l1 >= height)
			l1 = height - 1;
		if (i2 < 0)
			i2 = 0;
		if (j1 >= width)
			j1 = width - 1;
		if (k1 < 0)
			k1 = 0;
		if (data[j2] == 0) {
			data[j2] = magnitude[j2];
			boolean flag = false;
			int l = k1;
			do {
				if (l > j1)
					break;
				int i1 = i2;
				do {
					if (i1 > l1)
						break;
					int k2 = l + i1 * width;
					if ((i1 != j || l != i)
						&& magnitude[k2] >= k
						&& follow(l, i1, k)) {
						flag = true;
						break;
					}
					i1++;
				} while (true);
				if (!flag)
					break;
				l++;
			}
			while (true);
			return true;
		} else {
			return false;
		}
	}
 
	private BufferedImage pixels2image(int ai[]) {
		MemoryImageSource memoryimagesource =
			new MemoryImageSource(
				width,
				height,
				ColorModel.getRGBdefault(),
				ai,
				0,
				width);
		Image i = Toolkit.getDefaultToolkit().createImage(memoryimagesource);

		// Create a BufferedImage of the same size as the Image
		BufferedImage bi = new BufferedImage( width, height, BufferedImage.TYPE_INT_ARGB );
	
		Graphics2D g = bi.createGraphics();    	// Get a Graphics2D object
		g.drawImage( i, 0, 0, null );      	// Draw the Image data into the BufferedImage

		return bi;
	}
 
	private int[] image2pixels(Image image) {
		int ai[] = new int[picsize];
		PixelGrabber pixelgrabber =
			new PixelGrabber(image, 0, 0, width, height, ai, 0, width);
		try {
			pixelgrabber.grabPixels();
		} catch (InterruptedException interruptedexception) {
			interruptedexception.printStackTrace();
		}
		boolean flag = false;
		int k1 = 0;
		do {
			if (k1 >= 16)
				break;
			int i = (ai[k1] & 0xff0000) >> 16;
			int k = (ai[k1] & 0xff00) >> 8;
			int i1 = ai[k1] & 0xff;
			if (i != k || k != i1) {
				flag = true;
				break;
			}
			k1++;
		} while (true);
		if (flag) {
			for (int l1 = 0; l1 < picsize; l1++) {
				int j = (ai[l1] & 0xff0000) >> 16;
				int l = (ai[l1] & 0xff00) >> 8;
				int j1 = ai[l1] & 0xff;
				ai[l1] =
					(int) (0.29799999999999999D * j
						+ 0.58599999999999997D * l
						+ 0.113D * j1);
			}
 
		} else {
			for (int i2 = 0; i2 < picsize; i2++)
				ai[i2] = ai[i2] & 0xff;
 
		}
		return ai;
	}
 
	/**
	 * @param image
	 */
	public void setSourceImage(Image image) {
		sourceImage = image;
	}
 
	/**
	 * @return edgeImage
	 */
	public BufferedImage getEdgeImage() {
		return edgeImage;
	}
 
//	public void setThreshold(int i) {
//		threshold = i;
//	}
// 
//	public void setWidGaussianKernel(int i) {
//		widGaussianKernel = i;
//	}
 
	final float ORIENT_SCALE = 40F;
	private int height;
	private int width;
	private int picsize;
	private int data[];
	private int derivative_mag[];
	private int magnitude[];
	private int orientation[];
	private Image sourceImage;
	private BufferedImage edgeImage;
//	private int threshold1;
//	private int threshold2;
//	private int threshold;
//	private int widGaussianKernel;

	/**
	 * @return magnitude
	 */
	public int[] getMagnitude()
	{
		return magnitude;
	}

	/**
	 * @return orientation
	 */
	public int[] getOrientation()
	{
		return orientation;
	}
	
	@Override
	public void processImage(Q image, org.openimaj.image.Image<?,?>... otherimages) {
		BufferedImage i = ImageUtilities.createBufferedImage(image);
		BufferedImage o = this.filter(i);
		int [] data = o.getRGB(0, 0, o.getWidth(), o.getHeight(), null, 0, o.getWidth());
		image.internalAssign(data, o.getWidth(), o.getHeight());
	}
}
 
