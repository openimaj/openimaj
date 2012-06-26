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
import org.openimaj.image.pixel.Pixel;
import org.openimaj.image.renderer.MBFImageRenderer;
import org.openimaj.image.renderer.RenderHints;

/**
 * 	A multiband floating-point image.
 * 
 *  @author Jonathon Hare (jsh2@ecs.soton.ac.uk)  
 */
public class MBFImage extends MultiBandImage<Float, MBFImage, FImage> {
	private static final long serialVersionUID = 1L;
	
	/**
	 * Construct an empty MBFImage with a the default RGB colourspace 
	 */
	public MBFImage() {
		super(ColourSpace.RGB);
	}
	
	/**
	 * Construct an MBFImage from single band images 
	 * @param colourSpace the colourspace
	 * @param images the bands
	 */
	public MBFImage(ColourSpace colourSpace, FImage... images) {
		super(colourSpace, images);
	}
	
	/**
	 * Construct an MBFImage from single band images with the default
	 * RGB colourspace if there are three images, RGBA if there are 4 images, 
	 * or CUSTOM otherwise. 
	 * @param images the bands
	 */
	public MBFImage(FImage... images) {
		super(images.length == 3 ? ColourSpace.RGB : images.length == 4 ? ColourSpace.RGBA : ColourSpace.CUSTOM, images);
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
	 * Create an image from a BufferedImage object.
	 * Resultant image have RGB bands in the 0-1 range. 
	 * @param data array of packed ARGB pixels
	 * @param width the image width
	 * @param height the image height
	 */
	public MBFImage(int [] data, int width, int height) {
		this(data, width, height, false);
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

	/**
	 * Assign planar RGB bytes (R1G1B1R2G2B2...) to this image.
	 * @param bytes the byte array
	 * @param width the width of the byte image
	 * @param height the height of the byte image
	 * @return this
	 */
	public MBFImage internalAssign(byte[] bytes, int width, int height) {
		float [][] br = bands.get(0).pixels;
		float [][] bg = bands.get(1).pixels;
		float [][] bb = bands.get(2).pixels;
		
		for (int i=0, y=0; y<height; y++) {
			for (int x=0; x<width; x++) {
				int blue = bytes[i++] & 0xff;
				int green = ((int)bytes[i++]) & 0xff;
				int red = ((int)bytes[i++]) & 0xff;
				br[y][x] = ImageUtilities.BYTE_TO_FLOAT_LUT[red];
				bg[y][x] = ImageUtilities.BYTE_TO_FLOAT_LUT[green];
				bb[y][x] = ImageUtilities.BYTE_TO_FLOAT_LUT[blue];
			}
		}
		
		return this;
	}

	/* (non-Javadoc)
	 * @see uk.ac.soton.ecs.jsh2.image.Image#internalAssign(java.awt.image.BufferedImage)
	 */
	@Override
	public MBFImage internalAssign(int [] data, int width, int height) {
		float [][] br = bands.get(0).pixels;
		float [][] bg = bands.get(1).pixels;
		float [][] bb = bands.get(2).pixels;
		float [][] ba = null;
		
		if (colourSpace == ColourSpace.RGBA)
			ba = bands.get(3).pixels;
		
		for (int i=0, y=0; y<height; y++) {
			for (int x=0; x<width; x++, i++) {
				int rgb = data[i];
				int alpha = ((rgb >> 24) & 0xff);
				int red = ((rgb >> 16) & 0xff);
				int green = ((rgb >> 8) & 0xff);
				int blue = ((rgb) & 0xff);
				br[y][x] = ImageUtilities.BYTE_TO_FLOAT_LUT[red];
				bg[y][x] = ImageUtilities.BYTE_TO_FLOAT_LUT[green];
				bb[y][x] = ImageUtilities.BYTE_TO_FLOAT_LUT[blue];

				if(ba != null)
					ba[y][x] = ImageUtilities.BYTE_TO_FLOAT_LUT[alpha];				
			}
		}
		
		return this;
	}

	@Override
	protected Float intToT(int n) {
		return (float)n;
	}
	
	@Override
	public FImage newBandInstance(int width, int height) {
		return new FImage(width, height);
	}

	@Override
	public MBFImage newInstance() {
		return new MBFImage();
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

	@Override
	public MBFImageRenderer createRenderer() {
		return new MBFImageRenderer(this);
	}

	@Override
	public MBFImageRenderer createRenderer(RenderHints options) {
		return new MBFImageRenderer(this, options);
	}
	
	/**
	 * Get the value of the pixel at coordinate p
	 * 
	 * @param p The coordinate to get
	 * 
	 * @return The pixel value at (x, y)
	 */
	public float[] getPixelNative(Pixel p) {
		return getPixelNative(p.x, p.y);
	}
	
	/**
	 * Get the value of the pixel at coordinate <code>(x, y)</code>.
	 * 
	 * @param x The x-coordinate to get
	 * @param y The y-coordinate to get
	 * 
	 * @return The pixel value at (x, y)
	 */
	public float[] getPixelNative(int x, int y) {
		float[] pixels = new float[bands.size()];

		for (int i=0; i<bands.size(); i++) {
			pixels[i] = bands.get(i).getPixel(x, y);
		}

		return pixels;
	}
	
	/**
	 * 	Returns the pixels in this image as a vector (an array of the pixel
	 * 	type).
	 *  
	 *  @param f The array into which to place the data
	 *  @return The pixels in the image as a vector (a reference to the given array).
	 */
	public float[][] getPixelVectorNative( float[][] f )
	{
		for( int y = 0; y < getHeight(); y++ )
			for( int x = 0; x < getWidth(); x++ )
				f[x+y*getWidth()] = getPixelNative(x,y);
		
		return f;
	}
	
	/**
	 * Sets the pixel at <code>(x,y)</code> to the given value. Side-affects
	 * this image.
	 * 
	 * @param x The x-coordinate of the pixel to set
	 * @param y The y-coordinate of the pixel to set
	 * @param val The value to set the pixel to.
	 */
	public void setPixelNative(int x, int y, float[] val) {
		int np = bands.size();
		if(np == val.length)
			for (int i = 0; i < np; i++)
				bands.get(i).setPixel(x, y, val[i]);
		else{
			int offset = val.length - np;
			for (int i = 0; i < np; i++)
				if(i + offset >=0)
					bands.get(i).setPixel(x, y, val[i+offset]);
		}
	}
}
