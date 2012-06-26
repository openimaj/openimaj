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
package org.openimaj.image.renderer;

import java.util.Arrays;

import org.openimaj.image.FImage;
import org.openimaj.image.MBFImage;

/**
 * {@link ImageRenderer} for {@link MBFImage} images.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 */
public class MBFImageRenderer extends MultiBandRenderer<Float, MBFImage, FImage> {
	
	/**
	 * Construct with given target image.
	 * @param targetImage the target image.
	 */
	public MBFImageRenderer(MBFImage targetImage) {
		super(targetImage);
	}
	
	/**
	 * Construct with given target image and rendering hints.
	 * @param targetImage the target image.
	 * @param hints the render hints
	 */
	public MBFImageRenderer(MBFImage targetImage, RenderHints hints) {
		super(targetImage, hints);
	}

	@Override
	public Float[] defaultBackgroundColour() {
		return new Float[this.targetImage.numBands()];
	}
	
	@Override
	public Float[] defaultForegroundColour() {
		Float [] c = new Float[this.targetImage.numBands()];
		Arrays.fill(c, 1f);
		return c;
	}
	
	/**
	 * Draw the provided image at the given coordinates.
	 * Parts of the image outside the bounds of this image
	 * will be ignored
	 * 
	 * @param image Image to draw. 
	 * @param x x-coordinate
	 * @param y y-coordinate
	 */
	@Override
	public void drawImage(MBFImage image, int x, int y) {
		int targetBands = this.targetImage.numBands();
		int imageBands = image.numBands();
		
		if (targetBands == imageBands && targetBands == 3) {
			drawImage3(image, x, y);
			return;
		} else if(targetBands < 3 || targetBands > 4 || imageBands < 3 || imageBands > 4) {
			super.drawImage(image, x, y);
			return;
		}
		
		int stopx = Math.min(targetImage.getWidth(), x + image.getWidth());
		int stopy = Math.min(targetImage.getHeight(), y + image.getHeight());
		int startx = Math.max(0, x);
		int starty = Math.max(0, y);
	
		final float [][][] thisPixels = new float[targetBands][][];
		for (int i=0; i<thisPixels.length; i++) thisPixels[i] = this.targetImage.getBand(i).pixels;
		
		final float [][][] thatPixels = new float[imageBands][][];
		for (int i=0; i<thatPixels.length; i++) thatPixels[i] = image.getBand(i).pixels;
		
		/**
		 * If either image is 4 channel then we deal with the alpha channel correctly.
		 * Basically you add together the pixel values such that the pixel on top dominates (i.e. the image being added)
		 */
		float thisA=1.0f,thatA=1.0f,thisR,thisG,thisB,thatR,thatG,thatB,a,r,g,b;
		for (int yy=starty; yy<stopy; yy++)
		{
			final int thatY = yy - y;
			
			for (int xx=startx; xx<stopx; xx++)
			{
				final int thatX = xx - x;
				
				if(thisPixels.length == 4)
				{
					thisA = thisPixels[3][yy][xx];
					
				}
				thisR = thisPixels[0][yy][xx];
				thisG = thisPixels[1][yy][xx];
				thisB = thisPixels[2][yy][xx];
				if(thatPixels.length == 4)
				{
					thatA = thatPixels[3][thatY][thatX];
				}
				thatR = thatPixels[0][thatY][thatX];
				thatG = thatPixels[1][thatY][thatX];
				thatB = thatPixels[2][thatY][thatX];
				
				a = thatA + thisA * (1 - thatA); a = a > 1.0f ? 1.0f : a;
				r = thatR * thatA + (thisR*thisA)*(1-thatA); r = r > 1.0f ? 1.0f : r;
				g = thatG * thatA + (thisG*thisA)*(1-thatA); g = g > 1.0f ? 1.0f : g;
				b = thatB * thatA + (thisB*thisA)*(1-thatA); b = b > 1.0f ? 1.0f : b;
				
				if(thisPixels.length == 4)
				{
					thisPixels[0][yy][xx] = a;
					thisPixels[1][yy][xx] = r;
					thisPixels[2][yy][xx] = g;
					thisPixels[3][yy][xx] = b;
				}
				else{
					thisPixels[0][yy][xx] = r;
					thisPixels[1][yy][xx] = g;
					thisPixels[2][yy][xx] = b;
				}
			}
		}
	}
	
	protected void drawImage3(MBFImage image, int x, int y) {
		int stopx = Math.min(targetImage.getWidth(), x + image.getWidth());
		int stopy = Math.min(targetImage.getHeight(), y + image.getHeight());
		int startx = Math.max(0, x);
		int starty = Math.max(0, y);
	
		final float [][][] thisPixels = new float[3][][];
		for (int i=0; i<thisPixels.length; i++) thisPixels[i] = this.targetImage.getBand(i).pixels;
		
		final float [][][] thatPixels = new float[3][][];
		for (int i=0; i<thatPixels.length; i++) thatPixels[i] = image.getBand(i).pixels;
		
		for (int yy=starty; yy<stopy; yy++)
		{
			final int thatY = yy - y;
			
			System.arraycopy(thatPixels[0][thatY], startx-x, thisPixels[0][yy], startx, stopx-startx);
			System.arraycopy(thatPixels[1][thatY], startx-x, thisPixels[1][yy], startx, stopx-startx);
			System.arraycopy(thatPixels[2][thatY], startx-x, thisPixels[2][yy], startx, stopx-startx);
			
//			for (int xx=startx; xx<stopx; xx++)
//			{
//				int thatX = xx - x;
//				
//				thisPixels[0][yy][xx] = thatPixels[0][thatY][thatX];
//				thisPixels[1][yy][xx] = thatPixels[1][thatY][thatX];
//				thisPixels[2][yy][xx] = thatPixels[2][thatY][thatX];
//			}
		}
	}

}
