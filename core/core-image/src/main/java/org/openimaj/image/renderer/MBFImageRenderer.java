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
import org.openimaj.image.ImageUtilities;
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
	 *
	 * @param targetImage
	 *            the target image.
	 */
	public MBFImageRenderer(final MBFImage targetImage) {
		super(targetImage);
	}

	/**
	 * Construct with given target image and rendering hints.
	 *
	 * @param targetImage
	 *            the target image.
	 * @param hints
	 *            the render hints
	 */
	public MBFImageRenderer(final MBFImage targetImage, final RenderHints hints) {
		super(targetImage, hints);
	}

	@Override
	public Float[] defaultBackgroundColour() {
		return new Float[this.targetImage.numBands()];
	}

	@Override
	public Float[] defaultForegroundColour() {
		final Float[] c = new Float[this.targetImage.numBands()];
		Arrays.fill(c, 1f);
		return c;
	}

	/**
	 * Draw the provided image at the given coordinates. Parts of the image
	 * outside the bounds of this image will be ignored
	 *
	 * @param image
	 *            Image to draw.
	 * @param x
	 *            x-coordinate
	 * @param y
	 *            y-coordinate
	 */
	@Override
	public void drawImage(final MBFImage image, final int x, final int y) {
		final int targetBands = this.targetImage.numBands();
		final int imageBands = image.numBands();

		if (targetBands == imageBands && targetBands == 3) {
			this.drawImage3(image, x, y);
			return;
		} else if (targetBands < 3 || targetBands > 4 || imageBands < 3 || imageBands > 4) {
			super.drawImage(image, x, y);
			return;
		}

		final int stopx = Math.min(this.targetImage.getWidth(), x + image.getWidth());
		final int stopy = Math.min(this.targetImage.getHeight(), y + image.getHeight());
		final int startx = Math.max(0, x);
		final int starty = Math.max(0, y);

		final float[][][] thisPixels = new float[targetBands][][];
		for (int i = 0; i < thisPixels.length; i++)
			thisPixels[i] = this.targetImage.getBand(i).pixels;

		final float[][][] thatPixels = new float[imageBands][][];
		for (int i = 0; i < thatPixels.length; i++)
			thatPixels[i] = image.getBand(i).pixels;

		/**
		 * If either image is 4 channel then we deal with the alpha channel
		 * correctly. Basically you add together the pixel values such that the
		 * pixel on top dominates (i.e. the image being added)
		 */
//		final float thisA = 1.0f, thatA = 1.0f, thisR, thisG, thisB, thatR, thatG, thatB, a, r, g, b;
		if(thisPixels.length == 4 && thatPixels.length == 4){
			drawBothAlpha(x, y, stopx, stopy, startx, starty, thisPixels, thatPixels);
		} else if (thisPixels.length == 4){
			drawThisAlpha(x, y, stopx, stopy, startx, starty, thisPixels, thatPixels);
		} else{
			drawThatAlpha(x, y, stopx, stopy, startx, starty, thisPixels, thatPixels);
		}
	}

	private void drawBothAlpha(final int x, final int y, final int stopx,
			final int stopy, final int startx, final int starty,
			final float[][][] thisPixels, final float[][][] thatPixels) {
		float[] out = new float[4];
		for (int yy = starty; yy < stopy; yy++) {
			final int thatY = yy - y;

			for (int xx = startx; xx < stopx; xx++) {

				final int thatX = xx - x;
				float thisA = thisPixels[3][yy][xx] ;
				float thatA = thatPixels[3][thatY][thatX] ;
				ImageUtilities.alphaCompositePixel(out,
					thisPixels[0][yy][xx], thisPixels[1][yy][xx], thisPixels[2][yy][xx], thisA,
					thatPixels[0][thatY][thatX], thatPixels[1][thatY][thatX], thatPixels[2][thatY][thatX], thatA
				);

				thisPixels[0][yy][xx] = out[0];
				thisPixels[1][yy][xx] = out[1];
				thisPixels[2][yy][xx] = out[2];
				thisPixels[3][yy][xx] = out[3];
			}
		}
	}
	
	private void drawThisAlpha(final int x, final int y, final int stopx,
			final int stopy, final int startx, final int starty,
			final float[][][] thisPixels, final float[][][] thatPixels) {
		float[] out = new float[4];
		for (int yy = starty; yy < stopy; yy++) {
			final int thatY = yy - y;

			for (int xx = startx; xx < stopx; xx++) {

				final int thatX = xx - x;
				float thisA = thisPixels[3][yy][xx] ;
				float thatA = 1f ;
				ImageUtilities.alphaCompositePixel(out,
					thisPixels[0][yy][xx], thisPixels[1][yy][xx], thisPixels[2][yy][xx], thisA,
					thatPixels[0][thatY][thatX], thatPixels[1][thatY][thatX], thatPixels[2][thatY][thatX], thatA
				);

				thisPixels[0][yy][xx] = out[0];
				thisPixels[1][yy][xx] = out[1];
				thisPixels[2][yy][xx] = out[2];
				thisPixels[3][yy][xx] = out[3];
			}
		}
	}
	
	private void drawThatAlpha(final int x, final int y, final int stopx,
			final int stopy, final int startx, final int starty,
			final float[][][] thisPixels, final float[][][] thatPixels) {
		float[] out = new float[4];
		for (int yy = starty; yy < stopy; yy++) {
			final int thatY = yy - y;

			for (int xx = startx; xx < stopx; xx++) {

				final int thatX = xx - x;
				float thisA = 1f ;
				float thatA = thatPixels[3][thatY][thatX] ;
				ImageUtilities.alphaCompositePixel(out,
					thisPixels[0][yy][xx], thisPixels[1][yy][xx], thisPixels[2][yy][xx], thisA,
					thatPixels[0][thatY][thatX], thatPixels[1][thatY][thatX], thatPixels[2][thatY][thatX], thatA
				);

				thisPixels[0][yy][xx] = out[0];
				thisPixels[1][yy][xx] = out[1];
				thisPixels[2][yy][xx] = out[2];
			}
		}
	}

	protected void drawImage3(final MBFImage image, final int x, final int y) {
		final int stopx = Math.max(0, Math.min(this.targetImage.getWidth(), x + image.getWidth()));
		final int stopy = Math.max(0, Math.min(this.targetImage.getHeight(), y + image.getHeight()));
		final int startx = Math.max(0, x);
		final int starty = Math.max(0, y);

		if (startx >= stopx || starty >= stopy)
			return;

		final float[][][] thisPixels = new float[3][][];
		for (int i = 0; i < thisPixels.length; i++)
			thisPixels[i] = this.targetImage.getBand(i).pixels;

		final float[][][] thatPixels = new float[3][][];
		for (int i = 0; i < thatPixels.length; i++)
			thatPixels[i] = image.getBand(i).pixels;

		for (int yy = starty; yy < stopy; yy++) {
			final int thatY = yy - y;

			System.arraycopy(thatPixels[0][thatY], startx - x, thisPixels[0][yy], startx, stopx - startx);
			System.arraycopy(thatPixels[1][thatY], startx - x, thisPixels[1][yy], startx, stopx - startx);
			System.arraycopy(thatPixels[2][thatY], startx - x, thisPixels[2][yy], startx, stopx - startx);

			// for (int xx=startx; xx<stopx; xx++)
			// {
			// int thatX = xx - x;
			//
			// thisPixels[0][yy][xx] = thatPixels[0][thatY][thatX];
			// thisPixels[1][yy][xx] = thatPixels[1][thatY][thatX];
			// thisPixels[2][yy][xx] = thatPixels[2][thatY][thatX];
			// }
		}
	}

	@Override
	protected void drawHorizLine(final int x1, final int x2, final int y, Float[] col) {
		col = this.sanitise(col);
		if (y < 0 || y > this.targetImage.getHeight() - 1)
			return;

		final int startx = Math.max(0, Math.min(x1, x2));
		final int stopx = Math.min(Math.max(x1, x2), this.targetImage.getWidth() - 1);
		final int nbands = Math.min(col.length, this.targetImage.numBands());

		for (int b = 0; b < nbands; b++) {
			final float[][] img = this.targetImage.getBand(b).pixels;
			final float c = col[b];

			for (int x = startx; x <= stopx; x++) {
				img[y][x] = c;
			}
		}
	}

	@Override
	protected Float[] sanitise(final Float[] colour)
	{
		return this.targetImage.colourSpace.sanitise(colour);
	}
}
