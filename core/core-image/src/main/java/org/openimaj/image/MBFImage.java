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
package org.openimaj.image;

import java.util.Comparator;

import org.openimaj.image.colour.ColourSpace;

/**
 * 	A multiband floating-point image.
 * 
 *  @author Jonathon Hare <jsh2@ecs.soton.ac.uk>  
 */
public class MBFImage extends MultiBandImage<Float, MBFImage, FImage> {
	/**
	 * Construct an empty MBFImage with a the default RGB colourspace 
	 */
	public MBFImage() {
		super(ColourSpace.RGB);
	}
	
	/**
	 * Construct an MBFImage from single band images with the default
	 * RGB colourspace if there are three images, RGBA if there are 4 images, 
	 * or CUSTOM otherwise. 
	 * @param images
	 */
	public MBFImage(FImage... images) {
		super(images.length == 3 ? ColourSpace.RGB : images.length == 4 ? ColourSpace.RGBA : ColourSpace.CUSTOM, images);
	}
	
	/**
	 * Construct an MBFImage from single band images 
	 * @param colourSpace the colourspace
	 * @param images
	 */
	public MBFImage(ColourSpace colourSpace, FImage... images) {
		super(colourSpace, images);
	}
	
	/**
	 * Construct an empty image
	 * @param width Width of image
	 * @param height Height of image
	 * @param colourSpace the colourspace
	 */
	public MBFImage(int width, int height, ColourSpace colourSpace) {
		this.colourSpace = colourSpace;
		
		for (int i=0; i<colourSpace.getNumBands(); i++) {
			bands.add(new FImage(width, height));
		}
	}

	/**
	 * Construct an empty image. If the number of bands is 3, RGB is assumed,
	 * if the number is 4, then RGBA is assumed, otherwise the colourspace 
	 * is set to CUSTOM. 
	 * @param width Width of image
	 * @param height Height of image
	 * @param nbands number of bands
	 */
	public MBFImage(int width, int height, int nbands) {
		if (nbands == 3)
			this.colourSpace = ColourSpace.RGB;
		else if (nbands == 4)
			this.colourSpace = ColourSpace.RGBA;
		
		for (int i=0; i<nbands; i++) {
			bands.add(new FImage(width, height));
		}
	}
	
	/**
	 * Create an image from a int[] object.
	 * Resultant image will be in the 0-1 range. If alpha is
	 * true, bands will be RGBA, otherwise RGB
	 * @param data array of packed ARGB pixels
	 * @param width the image width
	 * @param height the image height
	 * @param alpha should we load the alpha channel
	 */
	public MBFImage(int [] data, int width, int height, boolean alpha) {
		this(width, height, alpha?4:3);
		internalAssign(data, width, height);
	}
	
	/**
	 * Create an image from a BufferedImage object.
	 * Resultant image have RGB bands in the 0-1 range. 
	 * @param data array of packed ARGB pixels
	 * @param width the image width
	 * @param height the image height
	 */
	public MBFImage(int [] data, int width, int height) {
		this(data, width, height, false);
	}
	
	/* (non-Javadoc)
	 * @see uk.ac.soton.ecs.jsh2.image.Image#internalAssign(java.awt.image.BufferedImage)
	 */
	@Override
	public MBFImage internalAssign(int [] data, int width, int height) {
		for (int i=0, y=0; y<height; y++) {
			for (int x=0; x<width; x++, i++) {
				int rgb = data[i];
				int alpha = ((rgb >> 24) & 0xff);
				int red = ((rgb >> 16) & 0xff);
				int green = ((rgb >> 8) & 0xff);
				int blue = ((rgb) & 0xff);
				(bands.get(0)).pixels[y][x] = red   / 255.0F;
				(bands.get(1)).pixels[y][x] = green / 255.0F;
				(bands.get(2)).pixels[y][x] = blue  / 255.0F;

				if(bands.size() == 4)
					(bands.get(3)).pixels[y][x] = alpha / 255.0F;					
			}
		}
		
		return this;
	}
	
	/* (non-Javadoc)
	 * @see uk.ac.soton.ecs.jsh2.image.MultiBandImage#flattenMax()
	 */
	@Override
	public FImage flattenMax() {
		int width = getWidth();
		int height = getHeight();

		FImage out = new FImage(width, height);
	
		for (int y=0; y<height; y++) {
			for (int x=0; x<width; x++) {
				float max = (bands.get(0)).pixels[y][x];
				
				for (int i=1; i<numBands(); i++)
					if (max > (bands.get(i)).pixels[y][x]) 
						max = (bands.get(i)).pixels[y][x];
				
				out.pixels[y][x] = max;
			}
		}
		
		return out;
	}
	
	/* (non-Javadoc)
	 * @see uk.ac.soton.ecs.jsh2.image.Image#getPixel(int, int)
	 */
	@Override
	public Float[] getPixel(int x, int y) {
		Float[] pixels = new Float[bands.size()];

		for (int i=0; i<bands.size(); i++) {
			pixels[i] = bands.get(i).getPixel(x, y);
		}

		return pixels;
	}
	
	/* (non-Javadoc)
	 * @see uk.ac.soton.ecs.jsh2.image.MultiBandImage#newInstance(int, int)
	 */
	@Override
	public MBFImage newInstance(int width, int height) {
		FImage [] images = new FImage[this.bands.size()];
		
		for (int i=0; i<bands.size(); i++) {
			images[i] = new FImage(width, height);
		}
		
		return new MBFImage(this.colourSpace, images);
	}

	/* (non-Javadoc)
	 * @see uk.ac.soton.ecs.jsh2.image.Image#getPixelInterp(double, double)
	 */
	@Override
	public Float[] getPixelInterp(double x, double y) {
		Float [] result = new Float[bands.size()];
		
		for (int i=0; i<bands.size(); i++) {
			result[i] = bands.get(i).getPixelInterp(x, y);
		}
		
		return result;
	}
	
	/* (non-Javadoc)
	 * @see uk.ac.soton.ecs.jsh2.image.Image#getPixelInterp(double, double,Float[])
	 */
	@Override
	public Float[] getPixelInterp(double x, double y, Float[] b) {
		Float [] result = new Float[bands.size()];
		
		for (int i=0; i<bands.size(); i++) {
			result[i] = bands.get(i).getPixelInterp(x, y,b[i]);
		}
		
		return result;
	}

	@Override
	public MBFImage newInstance() {
		return new MBFImage();
	}

	@Override
	public FImage newBandInstance(int width, int height) {
		return new FImage(width, height);
	}

	@Override
	protected Float intToT(int n) {
		return (float)n;
	}

	@Override
	public Comparator<? super Float[]> getPixelComparator() {
		return new Comparator<Float[]>(){

			@Override
			public int compare(Float[] o1, Float[] o2) {
				int sumDiff=0;
				boolean anyDiff = false;
				for(int i = 0; i < o1.length; i++){
					sumDiff+= o1[i] - o2[i];
					anyDiff = sumDiff != 0 || anyDiff;
				}
				if(anyDiff){
					if(sumDiff > 0)return 1;
					else return -1;
				}
				else return 0;
			}
			
		};
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
		if(this.bands.size() == 3 && this.bands.size() == image.bands.size()) {
			super.drawImage(image, x, y);
			return;
		}
		
		int stopx = Math.min(getWidth(), x + image.getWidth());
		int stopy = Math.min(getHeight(), y + image.getHeight());
		int startx = Math.max(0, x);
		int starty = Math.max(0, y);
	
		/**
		 * If either image is 4 channel then we deal with the alpha channel correctly.
		 * Basically you add together the pixel values such that the pixel on top dominates (i.e. the image being added)
		 */
		float thisA=1.0f,thatA=1.0f,thisR,thisG,thisB,thatR,thatG,thatB,a,r,g,b;
		Float[] toSet = new Float[this.bands.size()];
		for (int yy=starty; yy<stopy; yy++)
		{
			for (int xx=startx; xx<stopx; xx++)
			{
				Float[] thisPixel = this.getPixel(xx, yy);
				Float[] thatPixel = image.getPixel(xx-x,yy-y);
				
				if(thisPixel.length == 4)
				{
					thisA = thisPixel[3];
					
				}
				thisR = thisPixel[0];
				thisG = thisPixel[1];
				thisB = thisPixel[2];
				if(thatPixel.length == 4)
				{
					thatA = thatPixel[3];
				}
				thatR = thatPixel[0];
				thatG = thatPixel[1];
				thatB = thatPixel[2];
				
				
				a = thatA + thisA * (1 - thatA); a = a > 1.0f ? 1.0f : a;
				r = thatR * thatA + (thisR*thisA)*(1-thatA); r = r > 1.0f ? 1.0f : r;
				g = thatG * thatA + (thisG*thisA)*(1-thatA); g = g > 1.0f ? 1.0f : g;
				b = thatB * thatA + (thisB*thisA)*(1-thatA); b = b > 1.0f ? 1.0f : b;
				
				if(toSet.length == 4)
				{
					toSet[0] = a;
					toSet[1] = r;
					toSet[2] = g;
					toSet[3] = b;
				}
				else{
					toSet[0] = r;
					toSet[1] = g;
					toSet[2] = b;
				}
				setPixel(xx, yy, toSet);
			}
		}
	}
}
