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
/**
 * 
 */
package org.openimaj.image.feature.local.detector.mser.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * 	Some useful image utility functions.
 * 
 * 	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 * 	
 */
class ImageUtils
{
	/**
	 * 	Draws an image scaled to the size of the component.
	 *
	 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
	 *	
	 */
	public static class ImagePanel extends JPanel
	{
		private BufferedImage img = null;
		private static final long serialVersionUID = 1259304458335048851L;
		
		/**
		 * Set the image
		 * @param img the image
		 */
		public void setImage( BufferedImage img )
		{
			this.img = img;
			repaint();
		}
	
		@Override
		public void paint( Graphics g )
		{
			if( img == null ) return;
	        int newW = getWidth();
	        int newH = getHeight();
	        BufferedImage scaledImage = ImageUtils.getScaledInstance( img, newW, newH, RenderingHints.VALUE_INTERPOLATION_BILINEAR, false, true );
	        g.drawImage(scaledImage, 0, 0, null);	
		}
	}
	
	
	/**
	 * 	Returns grey-scale image data (0-255) for the given image.
	 * 	If the image is a colour RGB image, the average of RGB is taken.
	 * 	@param img The image to return the greyscale data for.
	 * 	@return The greyscale data for the image as 2D byte array.
	 */
	static public byte[][] getGrayscaleData( BufferedImage img )
	{
		byte[][] pixels = new byte[img.getWidth()][img.getHeight()];

		switch (img.getType())
		{
			case BufferedImage.TYPE_BYTE_GRAY:
				java.awt.image.DataBuffer data = img.getRaster().getDataBuffer();
				for( int x = 0; x < img.getWidth(); x++ )
				{
					for( int y = 0; y < img.getHeight(); y++ )
					{
						pixels[x][y] = (byte) data.getElem( y * img.getWidth() + x );
					}
				}

				break;

			default:
				for( int x = 0; x < img.getWidth(); x++ )
				{
					for( int y = 0; y < img.getHeight(); y++ )
					{
						int argb = img.getRGB( x, y );

						// int alpha = (argb >> 24) & 0xff;
						int red = (argb >> 16) & 0xff;
						int green = (argb >> 8) & 0xff;
						int blue = argb & 0xff;

						pixels[x][y] = (byte) ((blue + red + green) / 3);
					}
				}
				break;
		}

		return pixels;
	}

	/**
	 * 	Converts the given image to a greyscale buffered image.
	 *	@param img A colour image
	 *	@return A greyscale image.
	 */
	static public BufferedImage convertToGreyscale( BufferedImage img )
	{
		BufferedImage image = new BufferedImage( img.getWidth(), img.getHeight(), BufferedImage.TYPE_BYTE_GRAY);  
		Graphics g = image.getGraphics();  
		g.drawImage( img, 0, 0, null );  
		g.dispose();
		return image;
	}
	
	/**
	 * 	Creates a copy of a BufferedImage
	 *	@param source The image to copy
	 *	@return A copy of the source image
	 */
	static public BufferedImage copyImage( BufferedImage source )
	{
		BufferedImage target = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_4BYTE_ABGR );
		Graphics g = target.getGraphics();
		g.drawImage(source, 0, 0, null);
		return target;
	}
	
	/**
	 * 	Load an image from the given file.
	 *	@param f The file to load the image from
	 *	@return a BufferedImage containing the image loaded,
	 *		or null
	 */
	static public BufferedImage loadImage( File f )
	{
		try
		{
			return ImageUtils.loadImage( new FileInputStream( f ) );
		}
		catch( FileNotFoundException e )
		{
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * 	Load an image from the given input stream
	 * 
	 *	@param in The InputStream to load the image from
	 *	@return A BufferedImage or null
	 */
	static public BufferedImage loadImage( InputStream in )
	{
		BufferedImage image = null;
		try
		{
			try
			{
				image = ImageIO.read( in );
			}
			catch( IOException e )
			{
				// throw new ImagingIOException( e );
			}
			finally
			{
				in.close();
			}
		}
		catch( IOException e )
		{
			// throw new ImagingIOException( e );
		}
		return image;
	}

	/**
	 * 	Writes the given image to the given output stream in the given format.
	 *	@param out The output stream to write to
	 *	@param image The image to write
	 *	@param formatName The format to write the image in
	 */
	static public void saveImage( OutputStream out, BufferedImage image, String formatName )
	{
		try
		{
			try
			{
				ImageIO.write( image, formatName, out );
			}
			catch( IOException e )
			{
				// throw new ImagingIOException( e );
			}
			finally
			{
				out.flush();
				out.close();
			}
		}
		catch( IOException e )
		{
			// throw new ImagingIOException( e );
		}
	}
	
	 /**
     * Convenience method that returns a scaled instance of the
     * provided {@code BufferedImage}.
     * 
     * From http://today.java.net/pub/a/today/2007/04/03/perils-of-image-getscaledinstance.html
     *
     * @param img the original image to be scaled
     * @param targetWidthIn the desired width of the scaled instance,
     *    in pixels
     * @param targetHeightIn the desired height of the scaled instance,
     *    in pixels
     * @param hint one of the rendering hints that corresponds to
     *    {@code RenderingHints.KEY_INTERPOLATION} (e.g.
     *    {@code RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR},
     *    {@code RenderingHints.VALUE_INTERPOLATION_BILINEAR},
     *    {@code RenderingHints.VALUE_INTERPOLATION_BICUBIC})
     * @param higherQuality if true, this method will use a multi-step
     *    scaling technique that provides higher quality than the usual
     *    one-step technique (only useful in downscaling cases, where
     *    {@code targetWidth} or {@code targetHeight} is
     *    smaller than the original dimensions, and generally only when
     *    the {@code BILINEAR} hint is specified)
     * @param keepAspect if TRUE will make image fit within targetWidthIn x targetHeightIn
     * @return a scaled version of the original {@code BufferedImage}
     */
    static public BufferedImage getScaledInstance(BufferedImage img,
                                           int targetWidthIn,
                                           int targetHeightIn,
                                           Object hint,
                                           boolean higherQuality,
                                           boolean keepAspect )
    {
        int type = (img.getTransparency() == Transparency.OPAQUE) ?
            BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB;
        BufferedImage ret = (BufferedImage)img;
        
        int targetWidth  = targetWidthIn;
        int targetHeight = targetHeightIn;
        
        if( keepAspect )
        {
            double fixedRatio = targetWidthIn/(double)targetHeightIn;

            if( img.getWidth()/(double)img.getHeight() >= fixedRatio )
        			targetHeight = (int)((double)targetWidthIn * ((double)img.getHeight()/(double)img.getWidth()));
            else	targetWidth = (int)((double)img.getWidth() * ((double)targetHeightIn/(double)img.getHeight()));
        }
        
        int w, h;
        if (higherQuality) {
            // Use multi-step technique: start with original size, then
            // scale down in multiple passes with drawImage()
            // until the target size is reached
            w = img.getWidth();
            h = img.getHeight();
        } else {
            // Use one-step technique: scale directly from original
            // size to target size with a single drawImage() call
            w = targetWidth;
            h = targetHeight;
        }
        
        do {
            if (higherQuality && w > targetWidth) {
                w /= 2;
                if (w < targetWidth) {
                    w = targetWidth;
                }
            }

            if (higherQuality && h > targetHeight) {
                h /= 2;
                if (h < targetHeight) {
                    h = targetHeight;
                }
            }

            BufferedImage tmp = new BufferedImage(w, h, type);
            Graphics2D g2 = tmp.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, hint);
            g2.drawImage(ret, 0, 0, w, h, null);
            g2.dispose();

            ret = tmp;
        } while (w != targetWidth || h != targetHeight);

        return ret;
    }
    
	/**
	 * 	Inverts the given image.
	 *	@param img The image to invert
	 *	@param newImage Whether to return a new image or invert the given image.
	 *	@return the inverted image  
	 */
	static public BufferedImage invertImage( BufferedImage img, boolean newImage )
	{
		BufferedImage returnImage = img;
		if( newImage )
			returnImage = new BufferedImage( img.getWidth(), img.getHeight(), img.getType() );

		for( int y = 0; y < img.getHeight(); y++ ) {
			for( int x = 0; x < img.getWidth(); x++ ) {
				int in = img.getRGB(x,y);
				returnImage.setRGB( x, y, (in & 0xff000000) | ((~in) & 0x00ffffff) );
			}
		}
		
		return returnImage;
	}
	
	/**
	 * 	Displays the give image in a window.
	 *	@param img The image to display
	 */
	static public void displayImage( final BufferedImage img )
	{
		JFrame f = new JFrame();
		f.getContentPane().add( new JPanel()
		{
			private static final long serialVersionUID = 1259304458335048851L;

			@Override
			public void paint( Graphics g )
			{
		        int newW = getWidth();
		        int newH = getHeight();
		        BufferedImage scaledImage = ImageUtils.getScaledInstance( img, newW, newH, RenderingHints.VALUE_INTERPOLATION_BILINEAR, false, true );
		        g.drawImage(scaledImage, 0, 0, null);
				
			}
		}, BorderLayout.CENTER );
		f.setPreferredSize( new Dimension( 800,600 ) );
		f.pack();
		f.setVisible( true );
	}
}
