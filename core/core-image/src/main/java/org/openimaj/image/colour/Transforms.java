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
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
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
}
