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
import org.openimaj.image.MBFImage;

/**
 * A collection of static methods for colour transformations
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 */
public class Transforms {

	/**
	 * Calculate intensity by averaging R, G, B planes. 
	 * Assumes planes are all in the same magnitude.
	 * @param in MBFImage with 3 bands
	 * @return intensity image
	 */
	public static FImage calculateIntensity(MBFImage in) {
		if (in.colourSpace != ColourSpace.RGB && in.colourSpace != ColourSpace.RGBA)
			throw new UnsupportedOperationException("Can only convert RGB or RGBA images");

		FImage out = new FImage(in.getWidth(), in.getHeight());

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
	 * Calculate Intensity image from an RGB or RGBA MBFImage with given weightings for R, G and B
	 * @param in input image
	 * @param wR red weight
	 * @param wG green weight
	 * @param wB blue weight 
	 * @return Intensity image
	 */
	public static FImage calculateIntensity(MBFImage in, float wR, float wG, float wB) {
		if (in.colourSpace != ColourSpace.RGB && in.colourSpace != ColourSpace.RGBA)
			throw new UnsupportedOperationException("Can only convert RGB or RGBA images");

		FImage out = new FImage(in.getWidth(), in.getHeight());

		float [][] ra = in.getBand(0).pixels;
		float [][] ga = in.getBand(1).pixels;
		float [][] ba = in.getBand(2).pixels;

		for (int rr = 0; rr < in.getHeight(); rr++) {
			for (int c = 0; c < in.getWidth(); c++) {
				double r = ra[rr][c];
				double g = ga[rr][c];
				double b = ba[rr][c];

				out.pixels[rr][c] = (float) (wR * r + wG * g + wB * b);
				if (Float.isNaN(out.pixels[rr][c])) out.pixels[rr][c] = 0;
			}
		}

		return out;
	}

	/**
	 * Calculate intensity by a weighted average of the R, G, B planes. 
	 * Assumes planes are all in the same magnitude, and NTSC weighting 
	 * coefficients.
	 * @param in MBFImage with 3 bands
	 * @return intensity image
	 */
	public static FImage calculateIntensityNTSC(MBFImage in) {
		if (in.colourSpace != ColourSpace.RGB && in.colourSpace != ColourSpace.RGBA)
			throw new UnsupportedOperationException("Can only convert RGB or RGBA images");

		FImage out = new FImage(in.getWidth(), in.getHeight());

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
	 * Calculate Hue in 0..1 range from a 3-band RGB MBFImage
	 * @param in RGB or RGBA image
	 * @return Hue image
	 */
	public static FImage calculateHue(MBFImage in) {
		if (in.colourSpace == ColourSpace.HSV)
			return in.getBand(0);
		
		if (in.colourSpace != ColourSpace.RGB && in.colourSpace != ColourSpace.RGBA)
			throw new IllegalArgumentException("RGB or RGBA colourspace is required");

		FImage out = new FImage(in.getWidth(), in.getHeight());

		float [][] ra = in.getBand(0).pixels;
		float [][] ga = in.getBand(1).pixels;
		float [][] ba = in.getBand(2).pixels;

		for (int rr = 0; rr < in.getHeight(); rr++) {
			for (int c = 0; c < in.getWidth(); c++) {
				double r = ra[rr][c];
				double g = ga[rr][c];
				double b = ba[rr][c];
				double i = (r + g + b) / 3.0; 

				//from Sonka, Hlavac & Boyle; p.26
				double num = 0.5 * ((r - g) + (r - b));
				double den = Math.sqrt( ((r-g)*(r-g)) + ((r-b)*(g-b)));

				if (den == 0)
					out.pixels[rr][c] = 0;
				else
					out.pixels[rr][c] = (float) Math.acos(num / den);

				if ((b/i) > (g/i)) out.pixels[rr][c] = (float) ((2*Math.PI) - out.pixels[rr][c]);

				//normalise to 0..1
				out.pixels[rr][c] /= 2*Math.PI;
			}
		}

		return out;
	}

	/**
	 * Calculate Saturation image from a 3-band RGB MBFImage
	 * @param in RGB or RGBA image
	 * @return Saturation image
	 */
	public static FImage calculateSaturation(MBFImage in) {
		if (in.colourSpace != ColourSpace.RGB && in.colourSpace != ColourSpace.RGBA)
			throw new IllegalArgumentException("RGB or RGBA colourspace is required");

		FImage out = new FImage(in.getWidth(), in.getHeight());

		float [][] ra = in.getBand(0).pixels;
		float [][] ga = in.getBand(1).pixels;
		float [][] ba = in.getBand(2).pixels;

		for (int rr = 0; rr < in.getHeight(); rr++) {
			for (int c = 0; c < in.getWidth(); c++) {
				double r = ra[rr][c];
				double g = ga[rr][c];
				double b = ba[rr][c];

				out.pixels[rr][c] = (float) (1.0 - ((3.0 / (r + g + b)) * Math.min(r, Math.min(g, b))));
				if (Float.isNaN(out.pixels[rr][c])) out.pixels[rr][c] = 0;
			}
		}

		return out;
	}

	/**
	 * Transform 3 band RGB image to HSV
	 * @param in RGB or RGBA image
	 * @return HSV image
	 */
	public static MBFImage RGB_TO_HSI(MBFImage in) {
		if (in.colourSpace != ColourSpace.RGB && in.colourSpace != ColourSpace.RGBA)
			throw new IllegalArgumentException("RGB or RGBA colourspace is required");
		
		MBFImage out = new MBFImage(ColourSpace.HSI);

		out.addBand(calculateHue(in));
		out.addBand(calculateSaturation(in));
		out.addBand(calculateIntensity(in));

		return out;
	}

	/**
	 * Transform 3 band RGB image to HSL
	 * @param in RGB or RGBA image
	 * @return HSL image
	 */
	public static MBFImage RGB_TO_HSL(MBFImage in) {
		if (in.colourSpace != ColourSpace.RGB && in.colourSpace != ColourSpace.RGBA)
			throw new IllegalArgumentException("RGB or RGBA colourspace is required");
		
		MBFImage out = RGB_TO_HSV(in);

		FImage R = in.getBand(0);
		FImage G = in.getBand(1);
		FImage B = in.getBand(2);
		
		FImage L = out.getBand(2);
		for (int y=0; y<L.height; y++) {
			for (int x=0; x<L.width; x++) {
				float max = Math.max(Math.max(R.pixels[y][x], G.pixels[y][x]), B.pixels[y][x]);
				float min = Math.min(Math.min(R.pixels[y][x], G.pixels[y][x]), B.pixels[y][x]);
				L.pixels[y][x] = 0.5f * (max-min);
			}
		}
		
		out.colourSpace = ColourSpace.HSL;
		
		return out;
	}

	/**
	 * Transform 3 band RGB image to HSY
	 * @param in RGB or RGBA image
	 * @return HSV image
	 */
	public static MBFImage RGB_TO_HSY(MBFImage in) {
		if (in.colourSpace != ColourSpace.RGB && in.colourSpace != ColourSpace.RGBA)
			throw new IllegalArgumentException("RGB or RGBA colourspace is required");
		
		MBFImage out = new MBFImage(ColourSpace.HSY);

		out.addBand(calculateHue(in));
		out.addBand(calculateSaturation(in));
		out.addBand(calculateIntensityNTSC(in));

		return out;
	}
	
	/**
	 * Transform 3 band RGB image to HS
	 * @param in RGB or RGBA image
	 * @return HS image
	 */
	public static MBFImage RGB_TO_HS(MBFImage in) {
		if (in.colourSpace != ColourSpace.RGB && in.colourSpace != ColourSpace.RGBA)
			throw new IllegalArgumentException("RGB or RGBA colourspace is required");
		
		MBFImage out = new MBFImage();

		out.addBand(calculateHue(in));
		out.addBand(calculateSaturation(in));

		return out;
	}

	/**
	 * Convert to HS using the formulation from
	 * 	http://ilab.usc.edu/wiki/index.php/HSV_And_H2SV_Color_Space
	 * @param in RGB or RGBA image
	 * @return HS image
	 */
	public static MBFImage RGB_TO_HS_2(MBFImage in) {
		MBFImage hsv = RGB_TO_HSV(in);
		hsv.deleteBand(2);
		hsv.colourSpace = ColourSpace.HS;
		return hsv;
	}
	
	/**
	 * Transform the Hue and Saturation components of a MBFImage
	 * by projecting them from a radial coordinate system to
	 * Cartesian coordinates. Assumes the Hue is the first band
	 * and Saturation is the second band. Any additional bands
	 * will be cloned to the result image.
	 * @param in input image
	 * @return Multi-band image with coupled first and second bands
	 * 			calculated by projecting from radial to Cartesian 
	 * 			coordinates. 
	 */
	public static MBFImage projectHS(MBFImage in) {
		if (in.colourSpace != ColourSpace.HS && in.colourSpace != ColourSpace.HSI && 
				in.colourSpace != ColourSpace.HSV && in.colourSpace != ColourSpace.HSY)
			throw new IllegalArgumentException("HS* colourspace is required");

		MBFImage out = in.clone();

		float [][] h = in.getBand(0).pixels;
		float [][] s = in.getBand(1).pixels;
		float [][] o1 = out.getBand(0).pixels;
		float [][] o2 = out.getBand(1).pixels;

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
	 * 	http://ilab.usc.edu/wiki/index.php/HSV_And_H2SV_Color_Space
	 * 	The assumption is that RGB are in the range 0..1. H is output
	 * 	in the range 0..1, SV are output in the range 0..1
	 * @param in RGB or RGBA image
	 * @return HSV image
	 */
	public static MBFImage RGB_TO_HSV(MBFImage in) {
		if (in.colourSpace != ColourSpace.RGB && in.colourSpace != ColourSpace.RGBA)
			throw new IllegalArgumentException("RGB or RGBA colourspace is required");
		
		int width = in.getWidth();
		int height = in.getHeight();

		MBFImage out = new MBFImage(width, height, 3);

		float [][] R = in.getBand(0).pixels;
		float [][] G = in.getBand(1).pixels;
		float [][] B = in.getBand(2).pixels;

		float [][] H = out.getBand(0).pixels;
		float [][] S = out.getBand(1).pixels;
		float [][] V = out.getBand(2).pixels;

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				
				//Blue Is the dominant color
				if((B[y][x] > G[y][x]) && (B[y][x] > R[y][x]))
				{
					//Value is set as the dominant color
					V[y][x] = B[y][x];
					if(V[y][x] != 0)
					{
						float min;
						if(R[y][x] > G[y][x]) 
							min = G[y][x];
						else
							min = R[y][x];
						
						//Delta is the difference between the most dominant color 
						//and the least dominant color. This will be used to compute saturation.
						float delta = V[y][x] - min;
						if(delta != 0) { 
							S[y][x] = (delta/V[y][x]); 
							H[y][x] = 4 + (R[y][x] - G[y][x]) / delta; 
						} else {
							S[y][x] = 0;
							H[y][x] = 4 + (R[y][x] - G[y][x]);
						}
						
						//Hue is just the difference between the two least dominant 
						//colors offset by the dominant color. That is, here 4 puts 
						//hue in the blue range. Then red and green just tug it one 
						//way or the other. Notice if red and green are equal, hue 
						//will stick squarely on blue
						H[y][x] *= 60; 
						if(H[y][x] < 0) 
							H[y][x] += 360;
						
						H[y][x] /= 360;
					}
					else
					{ 
						S[y][x] = 0; 
						H[y][x] = 0;
					}
				}
				//Green is the dominant color
				else if(G[y][x] > R[y][x])
				{
					V[y][x] = G[y][x];
					if(V[y][x] != 0)
					{
						float min;
						if(R[y][x] > B[y][x]) 
							min = B[y][x];
						else
							min = R[y][x];
						
						float delta = V[y][x] - min;
						
						if(delta != 0) {
							S[y][x] = (delta/V[y][x]); 
							H[y][x] = 2 + (B[y][x] - R[y][x]) / delta; 
						} else { 
							S[y][x] = 0;
							H[y][x] = 2 + (B[y][x] - R[y][x]); 
						}
						H[y][x] *= 60; 
						if(H[y][x] < 0) 
							H[y][x] += 360;
						
						H[y][x] /= 360;
					} else {
						S[y][x] = 0;
						H[y][x] = 0;
					}
				}
				//Red is the dominant color
				else
				{
					V[y][x] = R[y][x];
					if(V[y][x] != 0)
					{
						float min;
						if(G[y][x] > B[y][x]) 
							min = B[y][x];
						else
							min = G[y][x];
						
						float delta = V[y][x] - min;
						if(delta != 0) { 
							S[y][x] = (delta/V[y][x]); 
							H[y][x] = (G[y][x] - B[y][x]) / delta; 
						} else { 
							S[y][x] = 0;         
							H[y][x] = (G[y][x] - B[y][x]); 
						}
						H[y][x] *= 60;

						if(H[y][x] < 0) 
							H[y][x] += 360;
						H[y][x] /= 360;
					}
					else
					{ 
						S[y][x] = 0;
						H[y][x] = 0;
					}
				}
			}
		}
		return out;
	}

	/**
	 * Convert from HSV using the formulation from: 
	 * 	http://ilab.usc.edu/wiki/index.php/HSV_And_H2SV_Color_Space
	 * 	Assumption is that H is in the range 0..1 and SV are in the
	 * 	range 0..1. RGB are output in the range 0..1
	 * @param in input image
	 * @return RGB image
	 */
	public static MBFImage HSV_TO_RGB( MBFImage in )
	{
		if (in.colourSpace != ColourSpace.HSV)
			throw new IllegalArgumentException("HSV colourspace is required");
		
		int width = in.getWidth();
		int height = in.getHeight();

		MBFImage out = new MBFImage( width, height, ColourSpace.RGB );

		float[][] H = in.getBand( 0 ).pixels;
		float[][] S = in.getBand( 1 ).pixels;
		float[][] V = in.getBand( 2 ).pixels;

		float[][] R = out.getBand( 0 ).pixels;
		float[][] G = out.getBand( 1 ).pixels;
		float[][] B = out.getBand( 2 ).pixels;

		for( int y = 0; y < height; y++ )
		{
			for( int x = 0; x < width; x++ )
			{
				if( V[y][x] == 0 )
				{
					R[y][x] = 0;
					G[y][x] = 0;
					B[y][x] = 0;
				}
				else if( S[y][x] == 0 )
				{
					R[y][x] = V[y][x];
					G[y][x] = V[y][x];
					B[y][x] = V[y][x];
				}
				else
				{
					float hf = H[y][x] * 360f / 60.0f;
					int i = (int)Math.floor( hf );
					float f = hf - i;
					float pv = V[y][x] * (1 - S[y][x]);
					float qv = V[y][x] * (1 - S[y][x] * f);
					float tv = V[y][x] * (1 - S[y][x] * (1 - f));
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
						System.out.println(" Unknown colour "+hf );
						break;
					}
				}
			}
		}
		
		return out;
	}

	/**
	 * Convert to Hue to H2 using the formulation from: 
	 * 	http://ilab.usc.edu/wiki/index.php/HSV_And_H2SV_Color_Space
	 * @param in input image
	 * @return Two-component hue image
	 */
	public static MBFImage H_TO_H1H2(FImage in) {
		int width = in.getWidth();
		int height = in.getHeight();

		MBFImage out = new MBFImage(width, height, ColourSpace.H1H2);

		float [][] H = in.pixels;
		
		float [][] H1 = out.getBand(0).pixels;
		float [][] H2 = out.getBand(1).pixels;

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				if(H[y][x] > 0.5F)
				{
				  H2[y][x] = ((H[y][x] - 0.5F)/0.5F);
				  if(H[y][x] > 0.75) 
					  H1[y][x] = ((H[y][x] - 0.75F)/0.5F);
				  else        
					  H1[y][x] = (1 - (H[y][x] - 0.25F)/0.5F);
				}
				else
				{
				  H2[y][x] = (1F - H[y][x]/0.5F);
				  if(H[y][x] > 0.25F)
					  H1[y][x] = (1 - (H[y][x] - 0.25F)/0.5F);
				  else
					  H1[y][x] = (0.5F + H[y][x]/0.5F);
				}
			}
		}
		
		return out;
	}

	
	/**
	 * Convert HSV to H2SV using the formulation from: 
	 * 	http://ilab.usc.edu/wiki/index.php/HSV_And_H2SV_Color_Space
	 * @param in HSV image
	 * @return H2SV image
	 */
	public static MBFImage HSV_TO_H2SV(MBFImage in) {
		if (in.colourSpace != ColourSpace.HSV)
			throw new IllegalArgumentException("HSV colourspace is required");
		
		MBFImage out = H_TO_H1H2(in.getBand(0));
		out.addBand(in.getBand(1)); //S
		out.addBand(in.getBand(2)); //V
		
		return out;
	}
	
	/**
	 * Convert RGB to H2SV 
	 * @param in RGB or RGBA image
	 * @return H2SV image
	 */
	public static MBFImage RGB_TO_H2SV(MBFImage in) {
		if (in.colourSpace != ColourSpace.RGB && in.colourSpace != ColourSpace.RGBA)
			throw new IllegalArgumentException("RGB or RGBA colourspace is required");
		
		MBFImage HSV = RGB_TO_HSV(in);
		
		return HSV_TO_H2SV(HSV);
	}
	
	/**
	 * Convert RGB to H2S
	 * @param in RGB or RGBA image
	 * @return H2S image
	 */
	public static MBFImage RGB_TO_H2S(MBFImage in) {
		if (in.colourSpace != ColourSpace.RGB && in.colourSpace != ColourSpace.RGBA)
			throw new IllegalArgumentException("RGB or RGBA colourspace is required");
		
		MBFImage H2S = RGB_TO_H2SV(in);
		H2S.deleteBand(3); //remove V
		H2S.colourSpace = ColourSpace.H2S;
		
		return H2S;
	}
	
	
	/**
	 * Convert to Hue to H2 VARIANT 2 using the formulation from: 
	 * 	http://ilab.usc.edu/wiki/index.php/HSV_And_H2SV_Color_Space
	 * @param in Hue image
	 * @return H1H2_2 image
	 */
	public static MBFImage H_TO_H1H2_2(FImage in) {
		int width = in.getWidth();
		int height = in.getHeight();

		MBFImage out = new MBFImage(width, height, ColourSpace.H1H2_2);

		float [][] H = in.pixels;
		
		float [][] H1 = out.getBand(0).pixels;
		float [][] H2 = out.getBand(1).pixels;

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				if(H[y][x] > 0.3333333333F)
				{
				  H2[y][x] = ((H[y][x] - 0.3333333333F)/0.6666666666F);
				  if(H[y][x] > 0.6666666666F) 
					  H1[y][x] = ((H[y][x] - 0.6666666666F)/0.5F);
				  else
					  H1[y][x] = (1 - (H[y][x] - 0.1666666666F)/0.5F);
				}
				else
				{
				  H2[y][x] = (1 - H[y][x]/0.3333333333F);
				  if(H[y][x] > 0.1666666666F) 
					  H1[y][x] = (1 - (H[y][x] - 0.1666666666F)/0.5F);
				  else       
					  H1[y][x] = ((2.0F/3.0F) + H[y][x]/0.5F);
				}
			}
		}
		
		return out;
	}
	
	/**
	 * Convert H2SV to HSV VARIANT 1 using the simple formulation from: 
	 * 	http://ilab.usc.edu/wiki/index.php/HSV_And_H2SV_Color_Space
	 * 	Assumes H1 and H2 are 0..1. Output H is 0..1
	 * @param in H2SV image
	 * @return HSV image
	 */
	public static MBFImage H2SV_TO_HSV_Simple( MBFImage in )
	{
		MBFImage out = new MBFImage( in.getWidth(), in.getHeight(), ColourSpace.HSV );
		float[][] H  = out.getBand(0).pixels;
		float[][] H1 = in.getBand(0).pixels;
		float[][] H2 = in.getBand(1).pixels;
		
		int width = in.getWidth();
		int height = in.getHeight();
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				if(H1[y][x] > 0.5)
				  if(H2[y][x] > 0.5)  H[y][x] = 0.5f * H1[y][x] - 0.25f;
				  else          	  H[y][x] = 0.25f + 0.5f * (1f - H1[y][x]);
				else
				  if(H2[y][x] <= 0.5) H[y][x] = 0.25f + 0.5f * (1f - H1[y][x]);
				  else          	  H[y][x] = 0.75f + 0.5f * H1[y][x];
			}
		}
		
		out.addBand( in.getBand(2) );	// S
		out.addBand( in.getBand(3) );	// V
		return out;
	}
	
	/**
	 * Convert H2SV to HSV VARIANT 2 using the simple formulation from: 
	 * 	http://ilab.usc.edu/wiki/index.php/HSV_And_H2SV_Color_Space
	 * 	Assumes H1 and H2 are 0..1. Output H is 0..1
	 * @param in H2SV2 image
	 * @return HSV image
	 */
	public static MBFImage H2SV2_TO_HSV_Simple( MBFImage in )
	{
		MBFImage out = new MBFImage( in.getWidth(), in.getHeight(), ColourSpace.HSV );
		float[][] H  = out.getBand(0).pixels;
		float[][] H1 = in.getBand(0).pixels;
		float[][] H2 = in.getBand(1).pixels;
		
		int width = in.getWidth();
		int height = in.getHeight();
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				if(H1[y][x] > 2.0/3.0)
				  if(H2[y][x] > 0.5)  H[y][x] = 0.5f * H1[y][x] - 1/3f;
				  else          	  H[y][x] = 1/6f + 0.5f * (1f - H1[y][x]);
				else
				  if(H2[y][x] <= 0.5) H[y][x] = 1/3f + 0.5f * (1f - H1[y][x]);
				  else          	  H[y][x] = 2/3f + 0.5f * H1[y][x];
			}
		}
		
		out.addBand( in.getBand(2) );	// S
		out.addBand( in.getBand(3) );	// V
		return out;
	}
	
	/**
	 * Convert HSV to H2SV VARIANT 2 using the formulation from: 
	 * 	http://ilab.usc.edu/wiki/index.php/HSV_And_H2SV_Color_Space
	 * @param in HSV image
	 * @return H2SV_2 image
	 */
	public static MBFImage HSV_TO_H2SV_2(MBFImage in) {
		if (in.colourSpace != ColourSpace.HSV)
			throw new IllegalArgumentException("HSV colourspace is required");
		
		MBFImage out = H_TO_H1H2_2(in.getBand(0));
		out.addBand(in.getBand(1)); //S
		out.addBand(in.getBand(2)); //V
		out.colourSpace = ColourSpace.H2SV_2;
		return out;
	}
	
	/**
	 * Convert RGB to H2SV2 VARIANT 2 
	 * @param in RGB or RGBA image
	 * @return H2SV_2 image
	 */
	public static MBFImage RGB_TO_H2SV_2(MBFImage in) {
		if (in.colourSpace != ColourSpace.RGB && in.colourSpace != ColourSpace.RGBA)
			throw new IllegalArgumentException("RGB or RGBA colourspace is required");
		
		MBFImage HSV = RGB_TO_HSV(in);
		
		return HSV_TO_H2SV_2(HSV);
	}
	
	/**
	 * Convert RGB to H2S VARIANT 2 
	 * @param in RGB or RGBA image
	 * @return H2S image
	 */
	public static MBFImage RGB_TO_H2S_2(MBFImage in) {
		if (in.colourSpace != ColourSpace.RGB && in.colourSpace != ColourSpace.RGBA)
			throw new IllegalArgumentException("RGB or RGBA colourspace is required");
		
		MBFImage H2S = RGB_TO_H2SV_2(in);
		H2S.deleteBand(3); //remove V
		H2S.colourSpace = ColourSpace.H2S_2;
		return H2S;
	}
	
	/**
	 * Intensity normalisation
	 * @param in RGB image
	 * @return normalised RGB image 
	 */
	public static MBFImage RGB_TO_RGB_NORMALISED(MBFImage in) {
		int r = in.getHeight();
		int c = in.getWidth();
		MBFImage out = new MBFImage(c, r, ColourSpace.RGB_INTENSITY_NORMALISED);
		float max = (float) Math.sqrt(3);

		float grey = (1.0f / max );
		for (int j=0; j<r; j++) {
			for (int i=0; i<c; i++) {
				Float [] pixin = in.getPixel(i, j);
				
				
				if(pixin[0] == pixin[1] && pixin[1] == pixin[2] && pixin[0] == 0.0){
					out.setPixel(i, j, new Float[] {grey,grey,grey});
				}
				else if(pixin[0] == pixin[1] && pixin[1] == pixin[2] && pixin[0] == 1.0){
					out.setPixel(i, j, new Float[] {grey,grey,grey});
				}
				else{
					float length = (float) Math.sqrt(pixin[0]*pixin[0] + pixin[1]*pixin[1] + pixin[2]*pixin[2]);
					out.setPixel(i, j, new Float[] {(pixin[0]/length), (pixin[1]/length), (pixin[2]/length)});
				}
				
			}
		}
		
		return out;
	}
	
	/**
	 * CIE_XYZ color space transform from RGB.
	 * Uses inverse sRGB companding for energy normalisation
	 * and assumes a D65 whitepoint.
	 * 
	 * Transform described here: http://www.brucelindbloom.com/Eqn_RGB_to_XYZ.html
	 *
	 * @param in input RGB image
	 * @return CIEXYZ image
	 */
	public static MBFImage RGB_TO_CIEXYZ(MBFImage in) {
		int height = in.getHeight();
		int width = in.getWidth();
		MBFImage out = new MBFImage(width, height, ColourSpace.CIE_XYZ);
		
		FImage Rb = in.getBand(0);
		FImage Gb = in.getBand(1);
		FImage Bb = in.getBand(2);
		
		FImage Xb = out.getBand(0);
		FImage Yb = out.getBand(1);
		FImage Zb = out.getBand(2);
		
		for (int y=0; y<height; y++) {
			for (int x=0; x<width; x++) {
				float R = Rb.pixels[y][x];
				float G = Gb.pixels[y][x];
				float B = Bb.pixels[y][x];
				
				//inverse sRGB companding 
				double r = (R <= 0.04045) ? (R / 12.92) : ( Math.pow((R + 0.055) / 1.055, 2.4));
				double g = (G <= 0.04045) ? (G / 12.92) : ( Math.pow((G + 0.055) / 1.055, 2.4));
				double b = (B <= 0.04045) ? (B / 12.92) : ( Math.pow((B + 0.055) / 1.055, 2.4));
				
				//XYZ linear transform
				Xb.pixels[y][x] = (float) (r*0.4124564 + g*0.3575761 + b*0.1804375);
				Yb.pixels[y][x] = (float) (r*0.2126729 + g*0.7151522 + b*0.0721750);
				Zb.pixels[y][x] = (float) (r*0.0193339 + g*0.1191920 + b*0.9503041);
			}
		}
		
		return out;
	}

	/**
	 * CIE_XYZ color space transform to RGB.
	 * Uses sRGB companding for energy normalisation
	 * and assumes a D65 whitepoint.
	 * 
	 * Transform described here: http://www.brucelindbloom.com/Eqn_XYZ_to_RGB.html
	 *
	 * @param in input CIEXYZ image
	 * @return RGB image
	 */
	public static MBFImage CIEXYZ_TO_RGB(MBFImage in) {
		return CIEXYZ_TO_RGB(in, false);
	}
	
	/**
	 * CIE_XYZ color space transform to RGB.
	 * Uses sRGB companding for energy normalisation
	 * and assumes a D65 whitepoint.
	 * 
	 * Transform described here: http://www.brucelindbloom.com/Eqn_XYZ_to_RGB.html
	 *
	 * @param in input CIEXYZ image
	 * @param inPlace if true then input image is modified, rather than creating a new image
	 * @return RGB image
	 */
	public static MBFImage CIEXYZ_TO_RGB(MBFImage in, boolean inPlace) {
		int height = in.getHeight();
		int width = in.getWidth();
		
		MBFImage out;
		if (inPlace) {
			out = in;
			out.colourSpace = ColourSpace.RGB;
		} else {
			out = new MBFImage(width, height, ColourSpace.RGB);
		}
		
		FImage Xb = in.getBand(0);
		FImage Yb = in.getBand(1);
		FImage Zb = in.getBand(2);
		
		FImage Rb = out.getBand(0);
		FImage Gb = out.getBand(1);
		FImage Bb = out.getBand(2);
		
		for (int y=0; y<height; y++) {
			for (int x=0; x<width; x++) {
				float X = Xb.pixels[y][x];
				float Y = Yb.pixels[y][x];
				float Z = Zb.pixels[y][x];
				
				//XYZ to linear rgb
				double r = X*3.2404542 + Y*-1.5371385 + Z*-0.4985314;
				double g = X*-0.9692660 + Y*1.8760108 + Z*0.0415560;
				double b = X*0.0556434 + Y*-0.2040259 + Z*1.0572252;
				
				//sRGB companding
				Rb.pixels[y][x] = (float) ((r <= 0.0031308) ? (r * 12.92) : ( 1.055 * Math.pow(r, 1/2.4) - 0.055));
				Gb.pixels[y][x] = (float) ((g <= 0.0031308) ? (g * 12.92) : ( 1.055 * Math.pow(g, 1/2.4) - 0.055));
				Bb.pixels[y][x] = (float) ((b <= 0.0031308) ? (b * 12.92) : ( 1.055 * Math.pow(b, 1/2.4) - 0.055));
			}
		}
		
		return out;
	}

	
	/**
	 * Convert CIEXYZ to CIELab.
	 * See http://www.brucelindbloom.com/Eqn_XYZ_to_Lab.html
	 * 
	 * @param input input image
	 * @return converted image
	 */
	public static MBFImage CIEXYZ_TO_CIELab(MBFImage input) {
		return CIEXYZ_TO_CIELab(input, false);
	}
	
	/**
	 * Convert CIEXYZ to CIELab.
	 * See http://www.brucelindbloom.com/Eqn_XYZ_to_Lab.html
	 * 
	 * @param input input image
	 * @param inPlace if true then input image is modified, rather than creating a new image
	 * @return converted image
	 */
	public static MBFImage CIEXYZ_TO_CIELab(MBFImage input, boolean inPlace) {
		final double epsilon = 0.008856;	//actual CIE standard
		final double kappa   = 903.3;		//actual CIE standard

		final double Xr = 0.950456;	//reference white
		final double Yr = 1.0;		//reference white
		final double Zr = 1.088754;	//reference white
		
		int height = input.getHeight();
		int width = input.getWidth();
		
		MBFImage out;
		if (inPlace) {
			out = input;
			out.colourSpace = ColourSpace.CIE_Lab;
		} else {
			out = new MBFImage(width, height, ColourSpace.CIE_Lab);
		}
		
		FImage Xb = input.getBand(0);
		FImage Yb = input.getBand(1);
		FImage Zb = input.getBand(2);
		
		FImage Lb = out.getBand(0);
		FImage ab = out.getBand(1);
		FImage bb = out.getBand(2);
		
		for (int y=0; y<height; y++) {
			for (int x=0; x<width; x++) {
				float X = Xb.pixels[y][x];
				float Y = Yb.pixels[y][x];
				float Z = Zb.pixels[y][x];

				double xr = X/Xr;
				double yr = Y/Yr;
				double zr = Z/Zr;

				double fx = (xr > epsilon)	? ( Math.pow(xr, 1.0/3.0) ) : ( (kappa*xr + 16.0)/116.0 );
				double fy = (yr > epsilon)	? ( Math.pow(yr, 1.0/3.0) ) : ( (kappa*yr + 16.0)/116.0 );
				double fz = (zr > epsilon)	? ( Math.pow(zr, 1.0/3.0) ) : ( (kappa*zr + 16.0)/116.0 );

				Lb.pixels[y][x] = (float) (116.0*fy-16.0);
				ab.pixels[y][x] = (float) (500.0*(fx-fy));
				bb.pixels[y][x] = (float) (200.0*(fy-fz));
			}
		}
		
		return out; 
	}
		
	/**
	 * Convert RGB to CIE Lab.
	 * See http://www.brucelindbloom.com/index.html?Math.html
	 * 
	 * Conversion goes from RGB->XYZ->Lab
	 * 
	 * @param input input RGB image
	 * @return transformed CIE Lab image
	 */
	public static MBFImage RGB_TO_CIELab(MBFImage input) {
		return CIEXYZ_TO_CIELab(RGB_TO_CIEXYZ(input), true);
	}
	
	/**
	 * Convert CIELab to CIEXYZ.
	 * See http://www.brucelindbloom.com/Eqn_Lab_to_XYZ.html
	 * 
	 * @param input input image
	 * @return converted image
	 */
	public static MBFImage CIELab_TO_CIEXYZ(MBFImage input) {
		final double epsilon = 0.008856;	//actual CIE standard
		final double kappa   = 903.3;		//actual CIE standard

		final double Xr = 0.950456;	//reference white
		final double Yr = 1.0;		//reference white
		final double Zr = 1.088754;	//reference white
		
		int height = input.getHeight();
		int width = input.getWidth();
		
		MBFImage out = new MBFImage(width, height, ColourSpace.CIE_XYZ);
		
		FImage Lb = input.getBand(0);
		FImage ab = input.getBand(1);
		FImage bb = input.getBand(2);
		
		FImage Xb = out.getBand(0);
		FImage Yb = out.getBand(1);
		FImage Zb = out.getBand(2);
		
		for (int y=0; y<height; y++) {
			for (int x=0; x<width; x++) {
				float L = Lb.pixels[y][x];
				float a = ab.pixels[y][x];
				float b = bb.pixels[y][x];

				double fy = (L + 16) / 116;
				double fx = a / 500 + fy;
				double fz = fy - (b / 200);
				
				double fx3 = fx*fx*fx;
				double fz3 = fz*fz*fz;
				
				double xr = (fx3 > epsilon) ? fx3 : (116*fx - 16) / kappa;
				double yr = (L > kappa*epsilon) ? Math.pow((L+16)/116, 3) : L/kappa;
				double zr = (fz3 > epsilon) ? fz3 : (116*fz - 16) / kappa;
				
				Xb.pixels[y][x] = (float) (Xr * xr);
				Yb.pixels[y][x] = (float) (Yr * yr);
				Zb.pixels[y][x] = (float) (Zr * zr);
			}
		}
		
		return out; 
	}
		
	/**
	 * Convert CIE Lab to RGB.
	 * See http://www.brucelindbloom.com/index.html?Math.html
	 * 
	 * Conversion goes from Lab->XYZ->RGB
	 * 
	 * @param input input CIE Lab image
	 * @return transformed RGB image
	 */
	public static MBFImage CIELab_TO_RGB(MBFImage input) {
		return CIEXYZ_TO_RGB(CIELab_TO_CIEXYZ(input), true);
	}
	
	private static int[] t_gamma = new int[2048];
	private static int[] t_gamma_mm = new int[10000];
	static{
		
		for (int i=0; i<2048; i++) {
			float v = i/2048.0f;
			v = (float) (Math.pow(v, 3)* 6);
			t_gamma[i] = (int) (v*6*256);
		}
		int i;
		for (i=0; i<10000; i++) {
			float v = i/2048.0f;
			v = (float) (Math.pow(v, 3)* 6);
			t_gamma_mm[i] = (int) (v*6*256);
		}
	}
	
	/**
	 * Convert a greyscale image to a pseudo-colour image by applying a colourmap.
	 * @param input image to convert.
	 * @return colourised image.
	 */
	public static MBFImage Grey_To_Colour_MM(FImage input){
		MBFImage image = new MBFImage(input.width,input.height,3);
		
		final float [][] rb = image.getBand(0).pixels;
		final float [][] gb = image.getBand(1).pixels;
		final float [][] bb = image.getBand(2).pixels;
		
		for(int y = 0; y < input.height; y++){
			for(int x = 0; x < input.width; x++){
				int depth = (int) input.pixels[y][x];
				float r,g,b;
				int pval = t_gamma_mm[depth];
				int lb = pval & 0xff;
				switch (pval>>8) {
					case 0:
						r = 255;
						g = 255-lb;
						b = 255-lb;
						break;
					case 1:
						r = 255;
						g = lb;
						b = 0;
						break;
					case 2:
						r = 255-lb;
						g = 255;
						b = 0;
						break;
					case 3:
						r = 0;
						g = 255;
						b = lb;
						break;
					case 4:
						r = 0;
						g = 255-lb;
						b = 255;
						break;
					case 5:
						r = 0;
						g = 0;
						b = 255-lb;
						break;
					default:
						r = 0;
						g = 0;
						b = 0;
						break;
				}
				
				rb[y][x] = r/255.0f;
				gb[y][x] = g/255.0f;
				bb[y][x] = b/255.0f;
			}
		}
		return image;
		
	}
	
	/**
	 * Convert a greyscale image to a pseudo-colour image by applying a colourmap.
	 * @param input image to convert.
	 * @return colourised image.
	 */
	public static MBFImage Grey_To_Colour(FImage input){
		MBFImage image = new MBFImage(input.width,input.height,3);
		
		final float [][] rb = image.getBand(0).pixels;
		final float [][] gb = image.getBand(1).pixels;
		final float [][] bb = image.getBand(2).pixels;
		
		for(int y = 0; y < input.height; y++){
			for(int x = 0; x < input.width; x++){
				int depth = (int) input.pixels[y][x];
				float r,g,b;
				int pval = t_gamma[depth];
				int lb = pval & 0xff;
				switch (pval>>8) {
					case 0:
						r = 255;
						g = 255-lb;
						b = 255-lb;
						break;
					case 1:
						r = 255;
						g = lb;
						b = 0;
						break;
					case 2:
						r = 255-lb;
						g = 255;
						b = 0;
						break;
					case 3:
						r = 0;
						g = 255;
						b = lb;
						break;
					case 4:
						r = 0;
						g = 255-lb;
						b = 255;
						break;
					case 5:
						r = 0;
						g = 0;
						b = 255-lb;
						break;
					default:
						r = 0;
						g = 0;
						b = 0;
						break;
				}
				
				rb[y][x] = r/255.0f;
				gb[y][x] = g/255.0f;
				bb[y][x] = b/255.0f;
			}
		}
		return image;
		
	}

	/**
	 * Convert a greyscale image to a pseudo-colour image by applying a colourmap.
	 * @param input image to convert.
	 * @return colourised image.
	 */
	public static MBFImage Grey_TO_HeatRGB(FImage input){
		MBFImage image = new MBFImage(input.width,input.height,3);
		
		final float [][] rb = image.getBand(0).pixels;
		final float [][] gb = image.getBand(1).pixels;
		final float [][] bb = image.getBand(2).pixels;
		
		for(int y = 0; y < input.height; y++){
			for(int x = 0; x < input.width; x++){
				float prop;
				float r,g,b;
				if(input.pixels[y][x] < 0.5){
					prop = input.pixels[y][x] / 0.5f;
					r = 1f - prop;
					g = prop;
					b = 0f;
				}
				else{
					prop = (input.pixels[y][x]  - 0.5f) / 0.5f;
					r = 0;
					g = 1f - prop;
					b = prop;
				}
				rb[y][x] = r;
				gb[y][x] = g;
				bb[y][x] = b;
			}
		}
		return image;
	}
}
