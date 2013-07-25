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
package org.openimaj.image.colour;

import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.util.array.ArrayUtils;

/**
 * A collection of static methods for colour transformations
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 */
public class Transforms {

	/**
	 * Calculate intensity by averaging R, G, B planes. Assumes planes are all
	 * in the same magnitude.
	 * 
	 * @param in
	 *            MBFImage with 3 bands
	 * @return intensity image
	 */
	public static FImage calculateIntensity(final MBFImage in) {
		if (in.colourSpace != ColourSpace.RGB && in.colourSpace != ColourSpace.RGBA)
			throw new UnsupportedOperationException("Can only convert RGB or RGBA images");

		final FImage out = new FImage(in.getWidth(), in.getHeight());

		for (int r = 0; r < in.getHeight(); r++) {
			for (int c = 0; c < in.getWidth(); c++) {
				out.pixels[r][c] = (in.getBand(0).pixels[r][c] +
						in.getBand(1).pixels[r][c] +
						in.getBand(2).pixels[r][c]) / 3.0F;
			}
		}

		return out;
	}

	/**
	 * Calculate Intensity image from an RGB or RGBA MBFImage with given
	 * weightings for R, G and B
	 * 
	 * @param in
	 *            input image
	 * @param wR
	 *            red weight
	 * @param wG
	 *            green weight
	 * @param wB
	 *            blue weight
	 * @return Intensity image
	 */
	public static FImage calculateIntensity(final MBFImage in, final float wR, final float wG, final float wB) {
		if (in.colourSpace != ColourSpace.RGB && in.colourSpace != ColourSpace.RGBA)
			throw new UnsupportedOperationException("Can only convert RGB or RGBA images");

		final FImage out = new FImage(in.getWidth(), in.getHeight());

		final float[][] ra = in.getBand(0).pixels;
		final float[][] ga = in.getBand(1).pixels;
		final float[][] ba = in.getBand(2).pixels;

		for (int rr = 0; rr < in.getHeight(); rr++) {
			for (int c = 0; c < in.getWidth(); c++) {
				final double r = ra[rr][c];
				final double g = ga[rr][c];
				final double b = ba[rr][c];

				out.pixels[rr][c] = (float) (wR * r + wG * g + wB * b);
				if (Float.isNaN(out.pixels[rr][c]))
					out.pixels[rr][c] = 0;
			}
		}

		return out;
	}

	/**
	 * Calculate intensity by a weighted average of the R, G, B planes. Assumes
	 * planes are all in the same magnitude, and NTSC weighting coefficients.
	 * 
	 * @param in
	 *            MBFImage with 3 bands
	 * @return intensity image
	 */
	public static FImage calculateIntensityNTSC(final MBFImage in) {
		if (in.colourSpace != ColourSpace.RGB && in.colourSpace != ColourSpace.RGBA)
			throw new UnsupportedOperationException("Can only convert RGB or RGBA images");

		final FImage out = new FImage(in.getWidth(), in.getHeight());

		for (int r = 0; r < in.getHeight(); r++) {
			for (int c = 0; c < in.getWidth(); c++) {
				out.pixels[r][c] = (0.299f * in.getBand(0).pixels[r][c] +
						0.587f * in.getBand(1).pixels[r][c] +
						0.114f * in.getBand(2).pixels[r][c]);
			}
		}

		return out;
	}

	/**
	 * Calculate intensity by a weighted average of the R, G, B planes. Assumes
	 * planes are all in 0..1, and NTSC weighting coefficients. Assignment to
	 * graylevels is done using a LUT, so greys will have one of 256 discrete
	 * levels. The primary purpose of this is to be compatible with
	 * {@link FImage#FImage(int[], int, int)} and give exactly the same result.
	 * 
	 * @param in
	 *            MBFImage with 3 bands
	 * @return intensity image
	 */
	public static FImage calculateIntensityNTSC_LUT(final MBFImage in) {
		if (in.colourSpace != ColourSpace.RGB && in.colourSpace != ColourSpace.RGBA)
			throw new UnsupportedOperationException("Can only convert RGB or RGBA images");

		final FImage out = new FImage(in.getWidth(), in.getHeight());

		for (int r = 0; r < in.getHeight(); r++) {
			for (int c = 0; c < in.getWidth(); c++) {
				out.pixels[r][c] = ImageUtilities.BYTE_TO_FLOAT_LUT[(int) ((
						0.299f * (255 * in.getBand(0).pixels[r][c]) +
								0.587f * (255 * in.getBand(1).pixels[r][c]) +
						0.114f * (255 * in.getBand(2).pixels[r][c])))];
			}
		}

		return out;
	}

	/**
	 * Calculate Hue in 0..1 range from a 3-band RGB MBFImage
	 * 
	 * @param in
	 *            RGB or RGBA image
	 * @return Hue image
	 */
	public static FImage calculateHue(final MBFImage in) {
		if (in.colourSpace == ColourSpace.HSV)
			return in.getBand(0);

		if (in.colourSpace != ColourSpace.RGB && in.colourSpace != ColourSpace.RGBA)
			throw new IllegalArgumentException("RGB or RGBA colourspace is required");

		final FImage out = new FImage(in.getWidth(), in.getHeight());

		final float[][] ra = in.getBand(0).pixels;
		final float[][] ga = in.getBand(1).pixels;
		final float[][] ba = in.getBand(2).pixels;

		for (int rr = 0; rr < in.getHeight(); rr++) {
			for (int c = 0; c < in.getWidth(); c++) {
				final double r = ra[rr][c];
				final double g = ga[rr][c];
				final double b = ba[rr][c];
				final double i = (r + g + b) / 3.0;

				// from Sonka, Hlavac & Boyle; p.26
				final double num = 0.5 * ((r - g) + (r - b));
				final double den = Math.sqrt(((r - g) * (r - g)) + ((r - b) * (g - b)));

				if (den == 0)
					out.pixels[rr][c] = 0;
				else
					out.pixels[rr][c] = (float) Math.acos(num / den);

				if ((b / i) > (g / i))
					out.pixels[rr][c] = (float) ((2 * Math.PI) - out.pixels[rr][c]);

				// normalise to 0..1
				out.pixels[rr][c] /= 2 * Math.PI;
			}
		}

		return out;
	}

	/**
	 * Calculate Saturation image from a 3-band RGB MBFImage
	 * 
	 * @param in
	 *            RGB or RGBA image
	 * @return Saturation image
	 */
	public static FImage calculateSaturation(final MBFImage in) {
		if (in.colourSpace != ColourSpace.RGB && in.colourSpace != ColourSpace.RGBA)
			throw new IllegalArgumentException("RGB or RGBA colourspace is required");

		final FImage out = new FImage(in.getWidth(), in.getHeight());

		final float[][] ra = in.getBand(0).pixels;
		final float[][] ga = in.getBand(1).pixels;
		final float[][] ba = in.getBand(2).pixels;

		for (int rr = 0; rr < in.getHeight(); rr++) {
			for (int c = 0; c < in.getWidth(); c++) {
				final double r = ra[rr][c];
				final double g = ga[rr][c];
				final double b = ba[rr][c];

				out.pixels[rr][c] = (float) (1.0 - ((3.0 / (r + g + b)) * Math.min(r, Math.min(g, b))));
				if (Float.isNaN(out.pixels[rr][c]))
					out.pixels[rr][c] = 0;
			}
		}

		return out;
	}

	/**
	 * Transform 3 band RGB image to HSV
	 * 
	 * @param in
	 *            RGB or RGBA image
	 * @return HSV image
	 */
	public static MBFImage RGB_TO_HSI(final MBFImage in) {
		if (in.colourSpace != ColourSpace.RGB && in.colourSpace != ColourSpace.RGBA)
			throw new IllegalArgumentException("RGB or RGBA colourspace is required");

		final MBFImage out = new MBFImage(ColourSpace.HSI);

		out.addBand(Transforms.calculateHue(in));
		out.addBand(Transforms.calculateSaturation(in));
		out.addBand(Transforms.calculateIntensity(in));

		return out;
	}

	/**
	 * Transform 3 band RGB image to HSL
	 * 
	 * @param in
	 *            RGB or RGBA image
	 * @return HSL image
	 */
	public static MBFImage RGB_TO_HSL(final MBFImage in) {
		if (in.colourSpace != ColourSpace.RGB && in.colourSpace != ColourSpace.RGBA)
			throw new IllegalArgumentException("RGB or RGBA colourspace is required");

		final MBFImage out = in.clone();
		final float[] pix = new float[in.numBands()];
		for (int y = 0; y < in.getHeight(); y++) {
			for (int x = 0; x < in.getWidth(); x++) {
				for (int b = 0; b < in.numBands(); b++)
					pix[b] = in.getBand(b).pixels[y][x];
				Transforms.RGB_TO_HSL(pix, pix);
				for (int b = 0; b < in.numBands(); b++)
					out.getBand(b).pixels[y][x] = pix[b];
			}
		}

		// final MBFImage out = Transforms.RGB_TO_HSV(in);
		//
		// final FImage R = in.getBand(0);
		// final FImage G = in.getBand(1);
		// final FImage B = in.getBand(2);
		//
		// final FImage L = out.getBand(2);
		// for (int y = 0; y < L.height; y++) {
		// for (int x = 0; x < L.width; x++) {
		// final float max = Math.max(Math.max(R.pixels[y][x], G.pixels[y][x]),
		// B.pixels[y][x]);
		// final float min = Math.min(Math.min(R.pixels[y][x], G.pixels[y][x]),
		// B.pixels[y][x]);
		// L.pixels[y][x] = 0.5f * (max - min);
		// }
		// }

		out.colourSpace = ColourSpace.HSL;

		return out;
	}

	/**
	 * Converts an RGB color value to HSL. Conversion formula adapted from
	 * http://en.wikipedia.org/wiki/HSL_color_space. Assumes r, g, and b are
	 * contained in the set [0, 1] and returns h, s, and l in the set [0, 1].
	 * 
	 * @see "http://stackoverflow.com/questions/2353211/hsl-to-rgb-color-conversion"
	 * @param rgb
	 *            The input RGB pixel
	 * @param hsl
	 *            The output HSL pixel
	 * @return The output HSL pixel
	 */
	public static float[] RGB_TO_HSL(final float[] rgb, final float[] hsl)
	{
		final float r = rgb[0], g = rgb[1], b = rgb[2];
		final float max = Math.max(r, Math.max(g, b)), min = Math.min(r, Math.min(g, b));
		float h = 0, s;
		final float l = (max + min) / 2f;

		if (max == min) {
			h = s = 0f; // achromatic
		} else {
			final float d = max - min;
			s = l > 0.5f ? d / (2f - max - min) : d / (max + min);
			if (max == r)
				h = (g - b) / d + (g < b ? 6f : 0);
			else if (max == g)
				h = (b - r) / d + 2f;
			else if (max == b)
				h = (r - g) / d + 4f;
			h /= 6f;
		}

		hsl[0] = h;
		hsl[1] = s;
		hsl[2] = l;
		return hsl;
	}

	/**
	 * Converts an HSL color value to RGB. Conversion formula adapted from
	 * http://en.wikipedia.org/wiki/HSL_color_space. Assumes h, s, and l are
	 * contained in the set [0, 1] and returns r, g, and b in the set [0, 255].
	 * 
	 * @param hsl
	 *            The HSL input pixel
	 * @param rgb
	 *            The RGB output pixel
	 * @return The RGB output pixel
	 * 
	 * @see "http://stackoverflow.com/questions/2353211/hsl-to-rgb-color-conversion"
	 */
	public static float[] HSL_TO_RGB(final float[] hsl, final float[] rgb)
	{
		float r, g, b;
		final float h = hsl[0], s = hsl[1], l = hsl[2];

		if (s == 0) {
			r = g = b = l; // achromatic
		} else {

			final float q = l < 0.5f ? l * (1f + s) : l + s - l * s;
			final float p = 2f * l - q;
			r = Transforms.hue2rgb(p, q, h + 1f / 3f);
			g = Transforms.hue2rgb(p, q, h);
			b = Transforms.hue2rgb(p, q, h - 1f / 3f);
		}

		rgb[0] = r;
		rgb[1] = g;
		rgb[2] = b;
		return rgb;
	}

	/**
	 * Used for the HSL to RGB conversion.
	 * 
	 * @return The hue
	 */
	private static float hue2rgb(final float p, final float q, float t)
	{
		if (t < 0f)
			t += 1f;
		if (t > 1f)
			t -= 1f;
		if (t < 1f / 6f)
			return p + (q - p) * 6f * t;
		if (t < 1f / 2f)
			return q;
		if (t < 2f / 3f)
			return p + (q - p) * (2f / 3f - t) * 6f;
		return p;
	}

	/**
	 * Transform 3 band RGB image to HSY
	 * 
	 * @param in
	 *            RGB or RGBA image
	 * @return HSV image
	 */
	public static MBFImage RGB_TO_HSY(final MBFImage in) {
		if (in.colourSpace != ColourSpace.RGB && in.colourSpace != ColourSpace.RGBA)
			throw new IllegalArgumentException("RGB or RGBA colourspace is required");

		final MBFImage out = new MBFImage(ColourSpace.HSY);

		out.addBand(Transforms.calculateHue(in));
		out.addBand(Transforms.calculateSaturation(in));
		out.addBand(Transforms.calculateIntensityNTSC(in));

		return out;
	}

	/**
	 * Transform 3 band RGB image to HS
	 * 
	 * @param in
	 *            RGB or RGBA image
	 * @return HS image
	 */
	public static MBFImage RGB_TO_HS(final MBFImage in) {
		if (in.colourSpace != ColourSpace.RGB && in.colourSpace != ColourSpace.RGBA)
			throw new IllegalArgumentException("RGB or RGBA colourspace is required");

		final MBFImage out = new MBFImage();

		out.addBand(Transforms.calculateHue(in));
		out.addBand(Transforms.calculateSaturation(in));

		return out;
	}

	/**
	 * Convert to HS using the formulation from
	 * http://ilab.usc.edu/wiki/index.php/HSV_And_H2SV_Color_Space
	 * 
	 * @param in
	 *            RGB or RGBA image
	 * @return HS image
	 */
	public static MBFImage RGB_TO_HS_2(final MBFImage in) {
		final MBFImage hsv = Transforms.RGB_TO_HSV(in);
		hsv.deleteBand(2);
		hsv.colourSpace = ColourSpace.HS;
		return hsv;
	}

	/**
	 * Transform the Hue and Saturation components of a MBFImage by projecting
	 * them from a radial coordinate system to Cartesian coordinates. Assumes
	 * the Hue is the first band and Saturation is the second band. Any
	 * additional bands will be cloned to the result image.
	 * 
	 * @param in
	 *            input image
	 * @return Multi-band image with coupled first and second bands calculated
	 *         by projecting from radial to Cartesian coordinates.
	 */
	public static MBFImage projectHS(final MBFImage in) {
		if (in.colourSpace != ColourSpace.HS && in.colourSpace != ColourSpace.HSI &&
				in.colourSpace != ColourSpace.HSV && in.colourSpace != ColourSpace.HSY)
			throw new IllegalArgumentException("HS* colourspace is required");

		final MBFImage out = in.clone();

		final float[][] h = in.getBand(0).pixels;
		final float[][] s = in.getBand(1).pixels;
		final float[][] o1 = out.getBand(0).pixels;
		final float[][] o2 = out.getBand(1).pixels;

		for (int r = 0; r < in.getHeight(); r++) {
			for (int c = 0; c < in.getWidth(); c++) {
				o1[r][c] = (float) (s[r][c] * Math.cos(2.0 * Math.PI * h[r][c]));
				o2[r][c] = (float) (s[r][c] * Math.sin(2.0 * Math.PI * h[r][c]));
			}
		}

		out.colourSpace = ColourSpace.CUSTOM;

		return out;
	}

	/**
	 * Convert to HSV using the formulation from:
	 * http://ilab.usc.edu/wiki/index.php/HSV_And_H2SV_Color_Space The
	 * assumption is that RGB are in the range 0..1. H is output in the range
	 * 0..1, SV are output in the range 0..1
	 * 
	 * @param in
	 *            RGB or RGBA image
	 * @return HSV image
	 */
	public static MBFImage RGB_TO_HSV(final MBFImage in) {
		if (in.colourSpace != ColourSpace.RGB && in.colourSpace != ColourSpace.RGBA)
			throw new IllegalArgumentException("RGB or RGBA colourspace is required");

		final int width = in.getWidth();
		final int height = in.getHeight();

		final MBFImage out = new MBFImage(width, height, ColourSpace.HSV);

		final float[][] R = in.getBand(0).pixels;
		final float[][] G = in.getBand(1).pixels;
		final float[][] B = in.getBand(2).pixels;

		final float[][] H = out.getBand(0).pixels;
		final float[][] S = out.getBand(1).pixels;
		final float[][] V = out.getBand(2).pixels;

		final float[] pIn = new float[3];
		final float[] pOut = new float[3];
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {

				pIn[0] = R[y][x];
				pIn[1] = G[y][x];
				pIn[2] = B[y][x];

				Transforms.RGB_TO_HSV(pIn, pOut);

				H[y][x] = pOut[0];
				S[y][x] = pOut[1];
				V[y][x] = pOut[2];
			}
		}
		return out;
	}

	/**
	 * Convert a single RGB pixel to HSV.
	 * 
	 * @param rgb
	 *            The pixel in RGB
	 * @param hsv
	 *            The output pixel in HSV
	 * @return the output pixel, hsv
	 */
	static float[] RGB_TO_HSV(final float[] rgb, final float[] hsv)
	{
		float H, S, V;

		final float R = rgb[0];
		final float G = rgb[1];
		final float B = rgb[2];

		// Blue Is the dominant color
		if ((B > G) && (B > R))
		{
			// Value is set as the dominant color
			V = B;
			if (V != 0)
			{
				float min;
				if (R > G)
					min = G;
				else
					min = R;

				// Delta is the difference between the most dominant
				// color
				// and the least dominant color. This will be used to
				// compute saturation.
				final float delta = V - min;
				if (delta != 0) {
					S = (delta / V);
					H = 4 + (R - G) / delta;
				} else {
					S = 0;
					H = 4 + (R - G);
				}

				// Hue is just the difference between the two least
				// dominant
				// colors offset by the dominant color. That is, here 4
				// puts
				// hue in the blue range. Then red and green just tug it
				// one
				// way or the other. Notice if red and green are equal,
				// hue
				// will stick squarely on blue
				H *= 60;
				if (H < 0)
					H += 360;

				H /= 360;
			}
			else
			{
				S = 0;
				H = 0;
			}
		}
		// Green is the dominant color
		else if (G > R)
		{
			V = G;
			if (V != 0)
			{
				float min;
				if (R > B)
					min = B;
				else
					min = R;

				final float delta = V - min;

				if (delta != 0) {
					S = (delta / V);
					H = 2 + (B - R) / delta;
				} else {
					S = 0;
					H = 2 + (B - R);
				}
				H *= 60;
				if (H < 0)
					H += 360;

				H /= 360;
			} else {
				S = 0;
				H = 0;
			}
		}
		// Red is the dominant color
		else
		{
			V = R;
			if (V != 0)
			{
				float min;
				if (G > B)
					min = B;
				else
					min = G;

				final float delta = V - min;
				if (delta != 0) {
					S = (delta / V);
					H = (G - B) / delta;
				} else {
					S = 0;
					H = (G - B);
				}
				H *= 60;

				if (H < 0)
					H += 360;
				H /= 360;
			}
			else
			{
				S = 0;
				H = 0;
			}
		}

		hsv[0] = H;
		hsv[1] = S;
		hsv[2] = V;
		return hsv;
	}

	/**
	 * Convert from HSV using the formulation from:
	 * http://ilab.usc.edu/wiki/index.php/HSV_And_H2SV_Color_Space Assumption is
	 * that H is in the range 0..1 and SV are in the range 0..1. RGB are output
	 * in the range 0..1
	 * 
	 * @param in
	 *            input image
	 * @return RGB image
	 */
	public static MBFImage HSV_TO_RGB(final MBFImage in)
	{
		if (in.colourSpace != ColourSpace.HSV)
			throw new IllegalArgumentException("HSV colourspace is required");

		final int width = in.getWidth();
		final int height = in.getHeight();

		final MBFImage out = new MBFImage(width, height, ColourSpace.RGB);

		final float[][] H = in.getBand(0).pixels;
		final float[][] S = in.getBand(1).pixels;
		final float[][] V = in.getBand(2).pixels;

		final float[][] R = out.getBand(0).pixels;
		final float[][] G = out.getBand(1).pixels;
		final float[][] B = out.getBand(2).pixels;

		for (int y = 0; y < height; y++)
		{
			for (int x = 0; x < width; x++)
			{
				if (V[y][x] == 0)
				{
					R[y][x] = 0;
					G[y][x] = 0;
					B[y][x] = 0;
				}
				else if (S[y][x] == 0)
				{
					R[y][x] = V[y][x];
					G[y][x] = V[y][x];
					B[y][x] = V[y][x];
				}
				else
				{
					final float hf = H[y][x] * 360f / 60.0f;
					final int i = (int) Math.floor(hf);
					final float f = hf - i;
					final float pv = V[y][x] * (1 - S[y][x]);
					final float qv = V[y][x] * (1 - S[y][x] * f);
					final float tv = V[y][x] * (1 - S[y][x] * (1 - f));
					switch (i)
					{
					// Red is the dominant color
					case 0:
						R[y][x] = V[y][x];
						G[y][x] = tv;
						B[y][x] = pv;
						break;
					// Green is the dominant color
					case 1:
						R[y][x] = qv;
						G[y][x] = V[y][x];
						B[y][x] = pv;
						break;
					case 2:
						R[y][x] = pv;
						G[y][x] = V[y][x];
						B[y][x] = tv;
						break;
					// Blue is the dominant color
					case 3:
						R[y][x] = pv;
						G[y][x] = qv;
						B[y][x] = V[y][x];
						break;
					case 4:
						R[y][x] = tv;
						G[y][x] = pv;
						B[y][x] = V[y][x];
						break;
					// Red is the dominant color
					case 5:
						R[y][x] = V[y][x];
						G[y][x] = pv;
						B[y][x] = qv;
						break;
					// Just in case we overshoot on our math by a little, we put
					// these here. Since its a switch it won't slow us down at
					// all to put these here.
					case 6:
						R[y][x] = V[y][x];
						G[y][x] = tv;
						B[y][x] = pv;
						break;
					case -1:
						R[y][x] = V[y][x];
						G[y][x] = pv;
						B[y][x] = qv;
						break;
					// The color is not defined, we should throw an error.
					default:
						System.out.println(" Unknown colour " + hf);
						break;
					}
				}
			}
		}

		return out;
	}

	/**
	 * Convert to Hue to H2 using the formulation from:
	 * http://ilab.usc.edu/wiki/index.php/HSV_And_H2SV_Color_Space
	 * 
	 * @param in
	 *            input image
	 * @return Two-component hue image
	 */
	public static MBFImage H_TO_H1H2(final FImage in) {
		final int width = in.getWidth();
		final int height = in.getHeight();

		final MBFImage out = new MBFImage(width, height, ColourSpace.H1H2);

		final float[][] H = in.pixels;

		final float[][] H1 = out.getBand(0).pixels;
		final float[][] H2 = out.getBand(1).pixels;

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				if (H[y][x] > 0.5F)
				{
					H2[y][x] = ((H[y][x] - 0.5F) / 0.5F);
					if (H[y][x] > 0.75)
						H1[y][x] = ((H[y][x] - 0.75F) / 0.5F);
					else
						H1[y][x] = (1 - (H[y][x] - 0.25F) / 0.5F);
				}
				else
				{
					H2[y][x] = (1F - H[y][x] / 0.5F);
					if (H[y][x] > 0.25F)
						H1[y][x] = (1 - (H[y][x] - 0.25F) / 0.5F);
					else
						H1[y][x] = (0.5F + H[y][x] / 0.5F);
				}
			}
		}

		return out;
	}

	/**
	 * Convert HSV to H2SV using the formulation from:
	 * http://ilab.usc.edu/wiki/index.php/HSV_And_H2SV_Color_Space
	 * 
	 * @param in
	 *            HSV image
	 * @return H2SV image
	 */
	public static MBFImage HSV_TO_H2SV(final MBFImage in) {
		if (in.colourSpace != ColourSpace.HSV)
			throw new IllegalArgumentException("HSV colourspace is required");

		final MBFImage out = Transforms.H_TO_H1H2(in.getBand(0));
		out.addBand(in.getBand(1)); // S
		out.addBand(in.getBand(2)); // V

		return out;
	}

	/**
	 * Convert RGB to H2SV
	 * 
	 * @param in
	 *            RGB or RGBA image
	 * @return H2SV image
	 */
	public static MBFImage RGB_TO_H2SV(final MBFImage in) {
		if (in.colourSpace != ColourSpace.RGB && in.colourSpace != ColourSpace.RGBA)
			throw new IllegalArgumentException("RGB or RGBA colourspace is required");

		final MBFImage HSV = Transforms.RGB_TO_HSV(in);

		return Transforms.HSV_TO_H2SV(HSV);
	}

	/**
	 * Convert RGB to H2S
	 * 
	 * @param in
	 *            RGB or RGBA image
	 * @return H2S image
	 */
	public static MBFImage RGB_TO_H2S(final MBFImage in) {
		if (in.colourSpace != ColourSpace.RGB && in.colourSpace != ColourSpace.RGBA)
			throw new IllegalArgumentException("RGB or RGBA colourspace is required");

		final MBFImage H2S = Transforms.RGB_TO_H2SV(in);
		H2S.deleteBand(3); // remove V
		H2S.colourSpace = ColourSpace.H2S;

		return H2S;
	}

	/**
	 * Convert to Hue to H2 VARIANT 2 using the formulation from:
	 * http://ilab.usc.edu/wiki/index.php/HSV_And_H2SV_Color_Space
	 * 
	 * @param in
	 *            Hue image
	 * @return H1H2_2 image
	 */
	public static MBFImage H_TO_H1H2_2(final FImage in) {
		final int width = in.getWidth();
		final int height = in.getHeight();

		final MBFImage out = new MBFImage(width, height, ColourSpace.H1H2_2);

		final float[][] H = in.pixels;

		final float[][] H1 = out.getBand(0).pixels;
		final float[][] H2 = out.getBand(1).pixels;

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				if (H[y][x] > 0.3333333333F)
				{
					H2[y][x] = ((H[y][x] - 0.3333333333F) / 0.6666666666F);
					if (H[y][x] > 0.6666666666F)
						H1[y][x] = ((H[y][x] - 0.6666666666F) / 0.5F);
					else
						H1[y][x] = (1 - (H[y][x] - 0.1666666666F) / 0.5F);
				}
				else
				{
					H2[y][x] = (1 - H[y][x] / 0.3333333333F);
					if (H[y][x] > 0.1666666666F)
						H1[y][x] = (1 - (H[y][x] - 0.1666666666F) / 0.5F);
					else
						H1[y][x] = ((2.0F / 3.0F) + H[y][x] / 0.5F);
				}
			}
		}

		return out;
	}

	/**
	 * Convert H2SV to HSV VARIANT 1 using the simple formulation from:
	 * http://ilab.usc.edu/wiki/index.php/HSV_And_H2SV_Color_Space Assumes H1
	 * and H2 are 0..1. Output H is 0..1
	 * 
	 * @param in
	 *            H2SV image
	 * @return HSV image
	 */
	public static MBFImage H2SV_TO_HSV_Simple(final MBFImage in)
	{
		final MBFImage out = new MBFImage(in.getWidth(), in.getHeight(), ColourSpace.HSV);
		final float[][] H = out.getBand(0).pixels;
		final float[][] H1 = in.getBand(0).pixels;
		final float[][] H2 = in.getBand(1).pixels;

		final int width = in.getWidth();
		final int height = in.getHeight();
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				if (H1[y][x] > 0.5)
					if (H2[y][x] > 0.5)
						H[y][x] = 0.5f * H1[y][x] - 0.25f;
					else
						H[y][x] = 0.25f + 0.5f * (1f - H1[y][x]);
				else if (H2[y][x] <= 0.5)
					H[y][x] = 0.25f + 0.5f * (1f - H1[y][x]);
				else
					H[y][x] = 0.75f + 0.5f * H1[y][x];
			}
		}

		out.addBand(in.getBand(2)); // S
		out.addBand(in.getBand(3)); // V
		return out;
	}

	/**
	 * Convert H2SV to HSV VARIANT 2 using the simple formulation from:
	 * http://ilab.usc.edu/wiki/index.php/HSV_And_H2SV_Color_Space Assumes H1
	 * and H2 are 0..1. Output H is 0..1
	 * 
	 * @param in
	 *            H2SV2 image
	 * @return HSV image
	 */
	public static MBFImage H2SV2_TO_HSV_Simple(final MBFImage in)
	{
		final MBFImage out = new MBFImage(in.getWidth(), in.getHeight(), ColourSpace.HSV);
		final float[][] H = out.getBand(0).pixels;
		final float[][] H1 = in.getBand(0).pixels;
		final float[][] H2 = in.getBand(1).pixels;

		final int width = in.getWidth();
		final int height = in.getHeight();
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				if (H1[y][x] > 2.0 / 3.0)
					if (H2[y][x] > 0.5)
						H[y][x] = 0.5f * H1[y][x] - 1 / 3f;
					else
						H[y][x] = 1 / 6f + 0.5f * (1f - H1[y][x]);
				else if (H2[y][x] <= 0.5)
					H[y][x] = 1 / 3f + 0.5f * (1f - H1[y][x]);
				else
					H[y][x] = 2 / 3f + 0.5f * H1[y][x];
			}
		}

		out.addBand(in.getBand(2)); // S
		out.addBand(in.getBand(3)); // V
		return out;
	}

	/**
	 * Convert HSV to H2SV VARIANT 2 using the formulation from:
	 * http://ilab.usc.edu/wiki/index.php/HSV_And_H2SV_Color_Space
	 * 
	 * @param in
	 *            HSV image
	 * @return H2SV_2 image
	 */
	public static MBFImage HSV_TO_H2SV_2(final MBFImage in) {
		if (in.colourSpace != ColourSpace.HSV)
			throw new IllegalArgumentException("HSV colourspace is required");

		final MBFImage out = Transforms.H_TO_H1H2_2(in.getBand(0));
		out.addBand(in.getBand(1)); // S
		out.addBand(in.getBand(2)); // V
		out.colourSpace = ColourSpace.H2SV_2;
		return out;
	}

	/**
	 * Convert RGB to H2SV2 VARIANT 2
	 * 
	 * @param in
	 *            RGB or RGBA image
	 * @return H2SV_2 image
	 */
	public static MBFImage RGB_TO_H2SV_2(final MBFImage in) {
		if (in.colourSpace != ColourSpace.RGB && in.colourSpace != ColourSpace.RGBA)
			throw new IllegalArgumentException("RGB or RGBA colourspace is required");

		final MBFImage HSV = Transforms.RGB_TO_HSV(in);

		return Transforms.HSV_TO_H2SV_2(HSV);
	}

	/**
	 * Convert RGB to H2S VARIANT 2
	 * 
	 * @param in
	 *            RGB or RGBA image
	 * @return H2S image
	 */
	public static MBFImage RGB_TO_H2S_2(final MBFImage in) {
		if (in.colourSpace != ColourSpace.RGB && in.colourSpace != ColourSpace.RGBA)
			throw new IllegalArgumentException("RGB or RGBA colourspace is required");

		final MBFImage H2S = Transforms.RGB_TO_H2SV_2(in);
		H2S.deleteBand(3); // remove V
		H2S.colourSpace = ColourSpace.H2S_2;
		return H2S;
	}

	/**
	 * Intensity normalisation
	 * 
	 * @param in
	 *            RGB image
	 * @return normalised RGB image
	 */
	public static MBFImage RGB_TO_RGB_NORMALISED(final MBFImage in) {
		final int r = in.getHeight();
		final int c = in.getWidth();
		final MBFImage out = new MBFImage(c, r, ColourSpace.RGB_INTENSITY_NORMALISED);
		final float max = (float) Math.sqrt(3);

		final float grey = (1.0f / max);
		for (int j = 0; j < r; j++) {
			for (int i = 0; i < c; i++) {
				final Float[] pixin = in.getPixel(i, j);

				if (pixin[0] == pixin[1] && pixin[1] == pixin[2] && pixin[0] == 0.0) {
					out.setPixel(i, j, new Float[] { grey, grey, grey });
				}
				else if (pixin[0] == pixin[1] && pixin[1] == pixin[2] && pixin[0] == 1.0) {
					out.setPixel(i, j, new Float[] { grey, grey, grey });
				}
				else {
					final float length = (float) Math.sqrt(pixin[0] * pixin[0] + pixin[1] * pixin[1] + pixin[2]
							* pixin[2]);
					out.setPixel(i, j, new Float[] { (pixin[0] / length), (pixin[1] / length), (pixin[2] / length) });
				}

			}
		}

		return out;
	}

	/**
	 * CIE_XYZ color space transform from RGB. Uses inverse sRGB companding for
	 * energy normalisation and assumes a D65 whitepoint.
	 * 
	 * Transform described here:
	 * http://www.brucelindbloom.com/Eqn_RGB_to_XYZ.html
	 * 
	 * @param in
	 *            input RGB image
	 * @return CIEXYZ image
	 */
	public static MBFImage RGB_TO_CIEXYZ(final MBFImage in) {
		final int height = in.getHeight();
		final int width = in.getWidth();
		final MBFImage out = new MBFImage(width, height, ColourSpace.CIE_XYZ);

		final FImage Rb = in.getBand(0);
		final FImage Gb = in.getBand(1);
		final FImage Bb = in.getBand(2);

		final FImage Xb = out.getBand(0);
		final FImage Yb = out.getBand(1);
		final FImage Zb = out.getBand(2);

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				final float R = Rb.pixels[y][x];
				final float G = Gb.pixels[y][x];
				final float B = Bb.pixels[y][x];

				// inverse sRGB companding
				final double r = (R <= 0.04045) ? (R / 12.92) : (Math.pow((R + 0.055) / 1.055, 2.4));
				final double g = (G <= 0.04045) ? (G / 12.92) : (Math.pow((G + 0.055) / 1.055, 2.4));
				final double b = (B <= 0.04045) ? (B / 12.92) : (Math.pow((B + 0.055) / 1.055, 2.4));

				// XYZ linear transform
				Xb.pixels[y][x] = (float) (r * 0.4124564 + g * 0.3575761 + b * 0.1804375);
				Yb.pixels[y][x] = (float) (r * 0.2126729 + g * 0.7151522 + b * 0.0721750);
				Zb.pixels[y][x] = (float) (r * 0.0193339 + g * 0.1191920 + b * 0.9503041);
			}
		}

		return out;
	}

	/**
	 * CIE_XYZ color space transform to RGB. Uses sRGB companding for energy
	 * normalisation and assumes a D65 whitepoint.
	 * 
	 * Transform described here:
	 * http://www.brucelindbloom.com/Eqn_XYZ_to_RGB.html
	 * 
	 * @param in
	 *            input CIEXYZ image
	 * @return RGB image
	 */
	public static MBFImage CIEXYZ_TO_RGB(final MBFImage in) {
		return Transforms.CIEXYZ_TO_RGB(in, false);
	}

	/**
	 * CIE_XYZ color space transform to RGB. Uses sRGB companding for energy
	 * normalisation and assumes a D65 whitepoint.
	 * 
	 * Transform described here:
	 * http://www.brucelindbloom.com/Eqn_XYZ_to_RGB.html
	 * 
	 * @param in
	 *            input CIEXYZ image
	 * @param inPlace
	 *            if true then input image is modified, rather than creating a
	 *            new image
	 * @return RGB image
	 */
	public static MBFImage CIEXYZ_TO_RGB(final MBFImage in, final boolean inPlace) {
		final int height = in.getHeight();
		final int width = in.getWidth();

		MBFImage out;
		if (inPlace) {
			out = in;
			out.colourSpace = ColourSpace.RGB;
		} else {
			out = new MBFImage(width, height, ColourSpace.RGB);
		}

		final FImage Xb = in.getBand(0);
		final FImage Yb = in.getBand(1);
		final FImage Zb = in.getBand(2);

		final FImage Rb = out.getBand(0);
		final FImage Gb = out.getBand(1);
		final FImage Bb = out.getBand(2);

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				final float X = Xb.pixels[y][x];
				final float Y = Yb.pixels[y][x];
				final float Z = Zb.pixels[y][x];

				// XYZ to linear rgb
				final double r = X * 3.2404542 + Y * -1.5371385 + Z * -0.4985314;
				final double g = X * -0.9692660 + Y * 1.8760108 + Z * 0.0415560;
				final double b = X * 0.0556434 + Y * -0.2040259 + Z * 1.0572252;

				// sRGB companding
				Rb.pixels[y][x] = (float) ((r <= 0.0031308) ? (r * 12.92) : (1.055 * Math.pow(r, 1 / 2.4) - 0.055));
				Gb.pixels[y][x] = (float) ((g <= 0.0031308) ? (g * 12.92) : (1.055 * Math.pow(g, 1 / 2.4) - 0.055));
				Bb.pixels[y][x] = (float) ((b <= 0.0031308) ? (b * 12.92) : (1.055 * Math.pow(b, 1 / 2.4) - 0.055));
			}
		}

		return out;
	}

	/**
	 * Convert CIEXYZ to CIELab. See
	 * http://www.brucelindbloom.com/Eqn_XYZ_to_Lab.html
	 * 
	 * @param input
	 *            input image
	 * @return converted image
	 */
	public static MBFImage CIEXYZ_TO_CIELab(final MBFImage input) {
		return Transforms.CIEXYZ_TO_CIELab(input, false);
	}

	/**
	 * Convert CIEXYZ to CIELab. See
	 * http://www.brucelindbloom.com/Eqn_XYZ_to_Lab.html
	 * 
	 * @param input
	 *            input image
	 * @param inPlace
	 *            if true then input image is modified, rather than creating a
	 *            new image
	 * @return converted image
	 */
	public static MBFImage CIEXYZ_TO_CIELab(final MBFImage input, final boolean inPlace) {
		return Transforms.CIEXYZ_TO_CIELab(input, inPlace, false);
	}

	private static MBFImage CIEXYZ_TO_CIELab(final MBFImage input, final boolean inPlace, final boolean norm) {
		final double epsilon = 0.008856; // actual CIE standard
		final double kappa = 903.3; // actual CIE standard

		final double Xr = 0.950456; // reference white
		final double Yr = 1.0; // reference white
		final double Zr = 1.088754; // reference white

		final int height = input.getHeight();
		final int width = input.getWidth();

		MBFImage out;
		if (inPlace) {
			out = input;
			out.colourSpace = ColourSpace.CIE_Lab;
		} else {
			out = new MBFImage(width, height, ColourSpace.CIE_Lab);
		}

		final FImage Xb = input.getBand(0);
		final FImage Yb = input.getBand(1);
		final FImage Zb = input.getBand(2);

		final FImage Lb = out.getBand(0);
		final FImage ab = out.getBand(1);
		final FImage bb = out.getBand(2);

		final float Lscale = norm ? 1f / 100f : 1;
		final float ascale = norm ? 1f / 256f : 1;
		final float bscale = norm ? 1f / 256f : 1;
		final float abdelta = norm ? 127 : 0;

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				final float X = Xb.pixels[y][x];
				final float Y = Yb.pixels[y][x];
				final float Z = Zb.pixels[y][x];

				final double xr = X / Xr;
				final double yr = Y / Yr;
				final double zr = Z / Zr;

				final double fx = (xr > epsilon) ? (Math.pow(xr, 1.0 / 3.0)) : ((kappa * xr + 16.0) / 116.0);
				final double fy = (yr > epsilon) ? (Math.pow(yr, 1.0 / 3.0)) : ((kappa * yr + 16.0) / 116.0);
				final double fz = (zr > epsilon) ? (Math.pow(zr, 1.0 / 3.0)) : ((kappa * zr + 16.0) / 116.0);

				Lb.pixels[y][x] = ((float) (116.0 * fy - 16.0)) * Lscale;
				ab.pixels[y][x] = ((float) (500.0 * (fx - fy)) + abdelta) * ascale;
				bb.pixels[y][x] = ((float) (200.0 * (fy - fz)) + abdelta) * bscale;
			}
		}

		return out;
	}

	/**
	 * Convert RGB to CIE Lab. See <a
	 * href="http://www.brucelindbloom.com/index.html?Math.html">
	 * http://www.brucelindbloom.com/index.html?Math.html</a>
	 * 
	 * Conversion goes from RGB->XYZ->Lab
	 * 
	 * @param input
	 *            input RGB image
	 * @return transformed CIE Lab image
	 */
	public static MBFImage RGB_TO_CIELab(final MBFImage input) {
		return Transforms.CIEXYZ_TO_CIELab(Transforms.RGB_TO_CIEXYZ(input), true);
	}

	/**
	 * Convert CIELab to CIEXYZ. See <a
	 * href="http://www.brucelindbloom.com/Eqn_Lab_to_XYZ.html">
	 * http://www.brucelindbloom.com/Eqn_Lab_to_XYZ.html</a>
	 * 
	 * @param input
	 *            input image
	 * @return converted image
	 */
	public static MBFImage CIELab_TO_CIEXYZ(final MBFImage input) {
		return Transforms.CIELab_TO_CIEXYZ(input, false);
	}

	private static MBFImage CIELab_TO_CIEXYZ(final MBFImage input, final boolean norm) {
		final double epsilon = 0.008856; // actual CIE standard
		final double kappa = 903.3; // actual CIE standard

		final double Xr = 0.950456; // reference white
		final double Yr = 1.0; // reference white
		final double Zr = 1.088754; // reference white

		final int height = input.getHeight();
		final int width = input.getWidth();

		final MBFImage out = new MBFImage(width, height, ColourSpace.CIE_XYZ);

		final FImage Lb = input.getBand(0);
		final FImage ab = input.getBand(1);
		final FImage bb = input.getBand(2);

		final FImage Xb = out.getBand(0);
		final FImage Yb = out.getBand(1);
		final FImage Zb = out.getBand(2);

		final float Lscale = norm ? 100 : 1;
		final float ascale = norm ? 256 : 1;
		final float bscale = norm ? 256 : 1;
		final float abdelta = norm ? -127 : 0;

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				final float L = (Lb.pixels[y][x] * Lscale);
				final float a = (ab.pixels[y][x] * ascale) + abdelta;
				final float b = (bb.pixels[y][x] * bscale) + abdelta;

				final double fy = (L + 16) / 116;
				final double fx = a / 500 + fy;
				final double fz = fy - (b / 200);

				final double fx3 = fx * fx * fx;
				final double fz3 = fz * fz * fz;

				final double xr = (fx3 > epsilon) ? fx3 : (116 * fx - 16) / kappa;
				final double yr = (L > kappa * epsilon) ? Math.pow((L + 16) / 116, 3) : L / kappa;
				final double zr = (fz3 > epsilon) ? fz3 : (116 * fz - 16) / kappa;

				Xb.pixels[y][x] = (float) (Xr * xr);
				Yb.pixels[y][x] = (float) (Yr * yr);
				Zb.pixels[y][x] = (float) (Zr * zr);
			}
		}

		return out;
	}

	/**
	 * Convert CIE Lab to RGB. See <a
	 * href="http://www.brucelindbloom.com/index.html?Math.html">
	 * http://www.brucelindbloom.com/index.html?Math.html</a>
	 * 
	 * Conversion goes from Lab->XYZ->RGB
	 * 
	 * @param input
	 *            input CIE Lab image
	 * @return transformed RGB image
	 */
	public static MBFImage CIELab_TO_RGB(final MBFImage input) {
		return Transforms.CIEXYZ_TO_RGB(Transforms.CIELab_TO_CIEXYZ(input), true);
	}

	/**
	 * Convert CIEXYZ to CIELab and normalise the resultant L, a & b values to
	 * 0..1. See <a href="http://www.brucelindbloom.com/Eqn_XYZ_to_Lab.html">
	 * http://www.brucelindbloom.com/Eqn_XYZ_to_Lab.html</a>.
	 * 
	 * @param input
	 *            input image
	 * @return converted image
	 */
	public static MBFImage RGB_TO_CIELabNormalised(final MBFImage input) {
		return Transforms.CIEXYZ_TO_CIELab(Transforms.RGB_TO_CIEXYZ(input), true, true);
	}

	/**
	 * Convert normalised CIE Lab to RGB. The L, a & b values are in 0..1. See
	 * <a href="http://www.brucelindbloom.com/index.html?Math.html">
	 * http://www.brucelindbloom.com/index.html?Math.html</a>
	 * 
	 * Conversion goes from Lab->XYZ->RGB
	 * 
	 * @param input
	 *            input CIE Lab image
	 * @return transformed RGB image
	 */
	public static MBFImage CIELabNormalised_TO_RGB(final MBFImage input) {
		return Transforms.CIEXYZ_TO_RGB(Transforms.CIELab_TO_CIEXYZ(input, true), true);
	}

	/**
	 * Convert CIEXYZ to CIELUV (CIE 1976) See
	 * http://www.brucelindbloom.com/index.html?Eqn_XYZ_to_Lab.html
	 * 
	 * @param input
	 *            input image
	 * @return converted image
	 */
	public static MBFImage CIEXYZ_TO_CIELUV(final MBFImage input) {
		return Transforms.CIEXYZ_TO_CIELUV(input, false);
	}

	/**
	 * Convert CIEXYZ to CIELUV (CIE 1976) See
	 * http://www.brucelindbloom.com/index.html?Eqn_XYZ_to_Lab.html
	 * 
	 * @param input
	 *            input image
	 * @param inPlace
	 *            if true then input image is modified, rather than creating a
	 *            new image
	 * @return converted image
	 */
	public static MBFImage CIEXYZ_TO_CIELUV(final MBFImage input, final boolean inPlace) {
		final int width = input.getWidth();
		final int height = input.getHeight();

		MBFImage out;
		if (inPlace) {
			out = input;
			out.colourSpace = ColourSpace.CIE_Luv;
		} else {
			out = new MBFImage(width, height, ColourSpace.CIE_Luv);
		}

		final FImage Xb = input.getBand(0);
		final FImage Yb = input.getBand(1);
		final FImage Zb = input.getBand(2);

		final FImage Lb = out.getBand(0);
		final FImage ub = out.getBand(1);
		final FImage vb = out.getBand(2);

		final double Xr = 0.950456; // reference white
		final double Yr = 1.0; // reference white
		final double Zr = 1.088754; // reference white

		final double epsilon = 0.008856;
		final double kappa = 903.3;

		for (int r = 0; r < height; r++) {
			for (int c = 0; c < width; c++) {
				final float X = Xb.pixels[r][c];
				final float Y = Yb.pixels[r][c];
				final float Z = Zb.pixels[r][c];

				final double yr = Y / Yr;

				float L;
				if (yr > epsilon) {
					L = (float) (116 * Math.cbrt(yr) - 16);
				} else {
					L = (float) (kappa * yr);
				}

				final double up = (4 * X) / (X + 15 * Y + 3 * Z);
				final double urp = (float) ((4 * Xr) / (Xr + 15 * Yr + 3 * Zr));
				final float u = (float) (13 * L * (up - urp));

				final double vp = (9 * Y) / (X + 15 * Y + 3 * Z);
				final double vrp = (9 * Yr) / (Xr + 15 * Yr + 3 * Zr);
				final float v = (float) (13 * L * (vp - vrp));

				Lb.pixels[r][c] = L;
				ub.pixels[r][c] = u;
				vb.pixels[r][c] = v;
			}
		}

		return out;
	}

	/**
	 * Convert RGB to CIE LUV. See <a
	 * href="http://www.brucelindbloom.com/index.html?Math.html">
	 * http://www.brucelindbloom.com/index.html?Math.html</a>
	 * 
	 * Conversion goes from RGB->XYZ->LUV
	 * 
	 * @param input
	 *            input RGB image
	 * @return transformed CIE LUV image
	 */
	public static MBFImage RGB_TO_CIELUV(final MBFImage input) {
		return Transforms.CIEXYZ_TO_CIELUV(Transforms.RGB_TO_CIEXYZ(input), true);
	}

	/**
	 * Convert CIELUV to CIEXYZ. See <a
	 * href="http://www.brucelindbloom.com/Eqn_Lab_to_XYZ.html">
	 * http://www.brucelindbloom.com/Eqn_Lab_to_XYZ.html</a>
	 * 
	 * @param input
	 *            input image
	 * @return converted image
	 */
	public static MBFImage CIELUV_TO_CIEXYZ(final MBFImage input) {
		final double epsilon = 0.008856; // actual CIE standard
		final double kappa = 903.3; // actual CIE standard

		final double Xr = 0.950456; // reference white
		final double Yr = 1.0; // reference white
		final double Zr = 1.088754; // reference white

		final int height = input.getHeight();
		final int width = input.getWidth();

		final MBFImage out = new MBFImage(width, height, ColourSpace.CIE_XYZ);

		final FImage Lb = input.getBand(0);
		final FImage ub = input.getBand(1);
		final FImage vb = input.getBand(2);

		final FImage Xb = out.getBand(0);
		final FImage Yb = out.getBand(1);
		final FImage Zb = out.getBand(2);

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				final float L = Lb.pixels[y][x];
				final float u = ub.pixels[y][x];
				final float v = vb.pixels[y][x];

				double Y;
				if (L > kappa * epsilon) {
					Y = Yr * Math.pow(((L + 16) / 116), 3);
				} else {
					Y = Yr * L / kappa;
				}

				final double u0 = (4 * Xr) / (Xr + 15 * Yr + 3 * Zr);
				final double v0 = (9 * Yr) / (Xr + 15 * Yr + 3 * Zr);

				final double a = (1.0 / 3.0) * (((52 * L) / (u + 13 * L * u0)) - 1);
				final double b = -5 * Y;
				final double c = -1.0 / 3.0;
				final double d = Y * (((39 * L) / (v + 13 * L * v0)) - 5);

				final double X = (d - b) / (a - c);
				final double Z = X * a + b;

				Xb.pixels[y][x] = (float) X;
				Yb.pixels[y][x] = (float) Y;
				Zb.pixels[y][x] = (float) Z;
			}
		}

		return out;
	}

	/**
	 * Convert CIE LUV to RGB. See <a
	 * href="http://www.brucelindbloom.com/index.html?Math.html">
	 * http://www.brucelindbloom.com/index.html?Math.html</a>
	 * 
	 * Conversion goes from LUV->XYZ->RGB
	 * 
	 * @param input
	 *            input CIE LUV image
	 * @return transformed RGB image
	 */
	public static MBFImage CIELUV_TO_RGB(final MBFImage input) {
		return Transforms.CIEXYZ_TO_RGB(Transforms.CIELUV_TO_CIEXYZ(input));
	}

	/**
	 * Convert from RGB to YUV. Y is in the range [0, 1]; U is [-0.436, 0.436]
	 * and V is [-0.615, 0.615].
	 * 
	 * @param input
	 *            input RGB image
	 * @return transformed YUV image
	 */
	public static MBFImage RGB_TO_YUV(final MBFImage input) {
		return Transforms.RGB_TO_YUV(input, false, false);
	}

	/**
	 * Convert from RGB to normalised YUV. Y, U and V are all in the range [0,
	 * 1].
	 * 
	 * @param input
	 *            input RGB image
	 * @return transformed normalised YUV image
	 */
	public static MBFImage RGB_TO_YUVNormalised(final MBFImage input) {
		return Transforms.RGB_TO_YUV(input, false, true);
	}

	/**
	 * Convert from RGB to YUV. Conversion can be either in place or in a new
	 * image. The U and V components can optionally be normalised to 0..1 or
	 * alternatively take the standard ranges: U in [-0.436, 0.436] and V in
	 * [-0.615, 0.615].
	 * 
	 * @param input
	 *            input RGB image
	 * @param inPlace
	 *            if true the output will overwrite the input; otherwise a new
	 *            image is created.
	 * @param norm
	 *            true if the U and V values are normalised to [0, 1].
	 * @return transformed YUV image
	 */
	public static MBFImage RGB_TO_YUV(final MBFImage input, final boolean inPlace, final boolean norm) {
		final int width = input.getWidth();
		final int height = input.getHeight();
		MBFImage out = null;

		if (inPlace) {
			out = input;
			out.colourSpace = norm ? ColourSpace.YUV_Norm : ColourSpace.YUV;
		} else {
			out = new MBFImage(width, height, norm ? ColourSpace.YUV_Norm : ColourSpace.YUV);
		}

		final float[][] Rb = input.getBand(0).pixels;
		final float[][] Gb = input.getBand(1).pixels;
		final float[][] Bb = input.getBand(2).pixels;

		final float[][] Yb = out.getBand(0).pixels;
		final float[][] Ub = out.getBand(1).pixels;
		final float[][] Vb = out.getBand(2).pixels;

		final double Wr = 0.299;
		final double Wb = 0.114;
		final double Wg = 0.587;
		final double Umax = 0.436;
		final double Vmax = 0.615;

		final double deltaU = norm ? -Umax : 0;
		final double deltaV = norm ? -Vmax : 0;
		final double Unorm = norm ? 2 * Umax : 1;
		final double Vnorm = norm ? 2 * Vmax : 1;

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				final double R = Rb[y][x];
				final double G = Gb[y][x];
				final double B = Bb[y][x];

				final double Y = Wr * R + Wg * G + Wb * B;
				double U = Umax * ((B - Y) / (1.0 - Wb));
				double V = Vmax * ((R - Y) / (1.0 - Wr));

				U = (U - deltaU) / Unorm;
				V = (V - deltaV) / Vnorm;

				Yb[y][x] = (float) Y;
				Ub[y][x] = (float) U;
				Vb[y][x] = (float) V;
			}
		}

		return out;
	}

	/**
	 * Convert from normalised YUV to RGB.
	 * 
	 * @param input
	 *            input normalised YUV image
	 * @return transformed RGB image
	 */
	public static MBFImage YUVNormalised_TO_RGB(final MBFImage input) {
		return Transforms.YUV_TO_RGB(input, false, true);
	}

	/**
	 * Convert from YUV to RGB.
	 * 
	 * @param input
	 *            input YUV image
	 * @return transformed RGB image
	 */
	public static MBFImage YUV_TO_RGB(final MBFImage input) {
		return Transforms.YUV_TO_RGB(input, false, false);
	}

	/**
	 * Convert from YUV to RGB. Conversion can be either in place or in a new
	 * image. The U and V components are either in [0, 1] (norm = true) or
	 * alternatively take the standard ranges (norm = false): U in [-0.436,
	 * 0.436] and V in [-0.615, 0.615].
	 * 
	 * @param input
	 *            input RGB image
	 * @param inPlace
	 *            if true the output will overwrite the input; otherwise a new
	 *            image is created.
	 * @param norm
	 *            true if the U and V values should be normalised to [0, 1].
	 * @return transformed YUV image
	 */
	public static MBFImage YUV_TO_RGB(final MBFImage input, final boolean inPlace, final boolean norm) {
		final int width = input.getWidth();
		final int height = input.getHeight();
		MBFImage out = null;

		if (inPlace) {
			out = input;
			out.colourSpace = ColourSpace.RGB;
		} else {
			out = new MBFImage(width, height, ColourSpace.RGB);
		}

		final float[][] Yb = input.getBand(0).pixels;
		final float[][] Ub = input.getBand(1).pixels;
		final float[][] Vb = input.getBand(2).pixels;

		final float[][] Rb = out.getBand(0).pixels;
		final float[][] Gb = out.getBand(1).pixels;
		final float[][] Bb = out.getBand(2).pixels;

		final double Wr = 0.299;
		final double Wb = 0.114;
		final double Wg = 0.587;
		final double Umax = 0.436;
		final double Vmax = 0.615;

		final double deltaU = norm ? -Umax : 0;
		final double deltaV = norm ? -Vmax : 0;
		final double Unorm = norm ? 2 * Umax : 1;
		final double Vnorm = norm ? 2 * Vmax : 1;

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				final double Y = Yb[y][x];
				final double U = (Ub[y][x] * Unorm) + deltaU;
				final double V = (Vb[y][x] * Vnorm) + deltaV;

				final double R = Y + V * ((1 - Wr) / Vmax);
				final double G = Y - U * ((Wb * (1 - Wb)) / (Umax * Wg)) - V * ((Wr * (1 - Wr)) / (Vmax * Wg));
				final double B = Y + U * ((1 - Wb) / Umax);

				Rb[y][x] = (float) R;
				Gb[y][x] = (float) G;
				Bb[y][x] = (float) B;
			}
		}

		return out;
	}

	/**
	 * Converts a Kelvin colour temperature the RGB black body equivalent. Note
	 * that the output values are in the range 0..1.
	 * 
	 * @see "http://www.tannerhelland.com/4435/convert-temperature-rgb-algorithm-code/"
	 * 
	 * @param temperature
	 *            the colour temperature.
	 * @return RGB value (0..1)
	 */
	public static double[] kelvinToRGB(double temperature)
	{
		// Start with a temperature, in Kelvin, somewhere between 1000 and
		// 40000.
		// (Other values may work, but I can't make any promises about the
		// quality
		// of the algorithm's estimates above 40000 K.) Note also that the
		// temperature
		// and color variables need to be declared as floating-point.

		temperature = temperature / 100d;

		double r = 0;
		if (temperature <= 66)
			r = 255;
		else
		{
			r = temperature - 60;
			r = 329.698727446 * Math.pow(r, -0.1332047592);
			if (r < 0)
				r = 0;
			if (r > 255)
				r = 255;
		}

		double g = 0;
		if (temperature <= 66)
		{
			g = temperature;
			g = 99.4708025861 * Math.log(g) - 161.1195681661;
			if (g < 0)
				g = 0;
			if (g > 255)
				g = 255;
		}
		else
		{
			g = temperature - 60;
			g = 288.1221695283 * Math.pow(g, -0.0755148492);
			if (g < 0)
				g = 0;
			if (g > 255)
				g = 255;
		}

		double b = 0;
		if (temperature >= 66)
			b = 255;
		else
		{
			if (temperature <= 19)
				b = 0;
			else
			{
				b = temperature - 10;
				b = 138.5177312231 * Math.log(b) - 305.0447927307;
				if (b < 0)
					b = 0;
				if (b > 255)
					b = 255;
			}
		}

		return new double[] { r / 255d, g / 255d, b / 255d };
	}

	/**
	 * Correct colour temperature using the method documented at Tanner Helland.
	 * Calculated the black body colour for the given temperature and alpha
	 * blends that with a constant luminance with the original image at the
	 * given strength. Only works on RGB images. Side affects the incoming
	 * image.
	 * 
	 * @param image
	 * @param colourTemperature
	 *            The colour temperature.
	 * @param strength
	 *            The strength of colour correction
	 * 
	 * @see "http://www.tannerhelland.com/4435/convert-temperature-rgb-algorithm-code/"
	 * @return The affected image.
	 */
	public static MBFImage colourTemperatureCorrection(final MBFImage image,
			final double colourTemperature, final double strength)
	{
		if (image.colourSpace != ColourSpace.RGB)
			throw new IllegalArgumentException("Colour correction only available for RGB images. " +
					"Try using the colour transforms to convert to RGB.");

		// Get the black body colour for the given temperature.
		final double rgb[] = Transforms.kelvinToRGB(colourTemperature);

		final float[] pix = new float[image.numBands()];
		final float[] oPix = new float[image.numBands()];
		for (int y = 0; y < image.getHeight(); y++)
		{
			for (int x = 0; x < image.getWidth(); x++)
			{
				for (int b = 0; b < image.numBands(); b++)
				{
					float f = image.getBand(b).pixels[y][x];
					oPix[b] = f;
					f = (float) (f * strength + rgb[b] * (1f - strength));
					pix[b] = f;
				}

				// pix contains the alpha blended original pixels. Calculate HSL
				Transforms.RGB_TO_HSL(pix, pix);

				// subsititue the luminance of original pixels
				pix[2] = (ArrayUtils.maxValue(oPix) + ArrayUtils.minValue(oPix)) / 2f;

				// Transform back to RGB
				Transforms.HSL_TO_RGB(pix, pix);

				for (int b = 0; b < image.numBands(); b++)
					image.getBand(b).pixels[y][x] = pix[b];
			}
		}

		return image;
	}
}
