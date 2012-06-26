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

import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Iterator;

import javax.imageio.IIOException;
import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageInputStream;
import javax.media.jai.JAI;

import org.w3c.dom.NodeList;

import com.sun.media.jai.codec.SeekableStream;
 
/**
 *	A class that provides extra functionality beyond that of
 *	the standard {@link ImageIO} class. In particular, it tries
 *  to deal jpeg images that the standard ImageIO cannot (i.e.
 *  CMYK jpegs, etc). 
 *
 *	@author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *	
 */
class ExtendedImageIO {
    /**
     * Returns a <code>BufferedImage</code> as the result of decoding
     * a supplied <code>File</code> with an <code>ImageReader</code>
     * chosen automatically from among those currently registered.
     * The <code>File</code> is wrapped in an
     * <code>ImageInputStream</code>.  If no registered
     * <code>ImageReader</code> claims to be able to read the
     * resulting stream, <code>null</code> is returned.
     *
     * <p> The current cache settings from <code>getUseCache</code>and
     * <code>getCacheDirectory</code> will be used to control caching in the
     * <code>ImageInputStream</code> that is created.
     *
     * <p> Note that there is no <code>read</code> method that takes a
     * filename as a <code>String</code>; use this method instead after
     * creating a <code>File</code> from the filename.
     *
     * <p> This method does not attempt to locate
     * <code>ImageReader</code>s that can read directly from a
     * <code>File</code>; that may be accomplished using
     * <code>IIORegistry</code> and <code>ImageReaderSpi</code>.
     *
     * @param input a <code>File</code> to read from.
     *
     * @return a <code>BufferedImage</code> containing the decoded
     * contents of the input, or <code>null</code>.
     *
     * @exception IllegalArgumentException if <code>input</code> is 
     * <code>null</code>.
     * @exception IOException if an error occurs during reading.
     */
    public static BufferedImage read(File input) throws IOException {
        if (input == null) {
            throw new IllegalArgumentException("input == null!");
        }
        if (!input.canRead()) {
            throw new IIOException("Can't read input file!");
        }

        return read(new FileInputStream(input));
    }

    /**
     * Returns a <code>BufferedImage</code> as the result of decoding
     * a supplied <code>InputStream</code> with an <code>ImageReader</code>
     * chosen automatically from among those currently registered.
     * The <code>InputStream</code> is wrapped in an
     * <code>ImageInputStream</code>.  If no registered
     * <code>ImageReader</code> claims to be able to read the
     * resulting stream, <code>null</code> is returned.
     *
     * <p> The current cache settings from <code>getUseCache</code>and
     * <code>getCacheDirectory</code> will be used to control caching in the
     * <code>ImageInputStream</code> that is created.
     *
     * <p> This method does not attempt to locate
     * <code>ImageReader</code>s that can read directly from an
     * <code>InputStream</code>; that may be accomplished using
     * <code>IIORegistry</code> and <code>ImageReaderSpi</code>.
     *
     * <p> This method <em>does not</em> close the provided
     * <code>InputStream</code> after the read operation has completed;
     * it is the responsibility of the caller to close the stream, if desired.
     *
     * @param input an <code>InputStream</code> to read from.
     *
     * @return a <code>BufferedImage</code> containing the decoded
     * contents of the input, or <code>null</code>.
     *
     * @exception IllegalArgumentException if <code>input</code> is 
     * <code>null</code>.
     * @exception IOException if an error occurs during reading.
     */
    public static BufferedImage read(InputStream input) throws IOException {
        if (input == null) {
            throw new IllegalArgumentException("input == null!");
        }
        
        BufferedInputStream buffer = new BufferedInputStream(input);
        buffer.mark(10 * 1024 * 1024); //10mb is big enough?
        
        BufferedImage bi;
        try {
        	ImageInputStream stream = ImageIO.createImageInputStream(buffer);
        	bi = read(stream);
        	if (bi == null) {
        		stream.close();
        	}
        } catch(Exception ex) {
        	buffer.reset();
            bi = JAI.create("stream", SeekableStream.wrapInputStream(buffer, false)).getAsBufferedImage();
        }
        return bi;
    }

    /**
     * Returns a <code>BufferedImage</code> as the result of decoding
     * a supplied <code>URL</code> with an <code>ImageReader</code>
     * chosen automatically from among those currently registered.  An
     * <code>InputStream</code> is obtained from the <code>URL</code>,
     * which is wrapped in an <code>ImageInputStream</code>.  If no
     * registered <code>ImageReader</code> claims to be able to read
     * the resulting stream, <code>null</code> is returned.
     *
     * <p> The current cache settings from <code>getUseCache</code>and
     * <code>getCacheDirectory</code> will be used to control caching in the
     * <code>ImageInputStream</code> that is created.
     *
     * <p> This method does not attempt to locate
     * <code>ImageReader</code>s that can read directly from a
     * <code>URL</code>; that may be accomplished using
     * <code>IIORegistry</code> and <code>ImageReaderSpi</code>.
     *
     * @param input a <code>URL</code> to read from.
     *
     * @return a <code>BufferedImage</code> containing the decoded
     * contents of the input, or <code>null</code>.
     *
     * @exception IllegalArgumentException if <code>input</code> is 
     * <code>null</code>.
     * @exception IOException if an error occurs during reading.
     */
    public static BufferedImage read(URL input) throws IOException {
        if (input == null) {
            throw new IllegalArgumentException("input == null!");
        }

        InputStream istream = null;
        try {
            istream = input.openStream();
        } catch (IOException e) {
            throw new IIOException("Can't get input stream from URL!", e);
        }
        
        return read(istream);
    }

    /**
     * Returns a <code>BufferedImage</code> as the result of decoding
     * a supplied <code>ImageInputStream</code> with an
     * <code>ImageReader</code> chosen automatically from among those
     * currently registered.  If no registered
     * <code>ImageReader</code> claims to be able to read the stream,
     * <code>null</code> is returned.
     *
     * <p> Unlike most other methods in this class, this method <em>does</em>
     * close the provided <code>ImageInputStream</code> after the read
     * operation has completed, unless <code>null</code> is returned,
     * in which case this method <em>does not</em> close the stream.
     *
     * @param input an <code>ImageInputStream</code> to read from.
     *
     * @return a <code>BufferedImage</code> containing the decoded
     * contents of the input, or <code>null</code>.
     *
     * @exception IllegalArgumentException if <code>stream</code> is 
     * <code>null</code>.
     * @exception IOException if an error occurs during reading.
     */
    public static BufferedImage read(ImageInputStream input) throws IOException {
    	if (input == null) {
            throw new IllegalArgumentException("stream == null!");
        }

        Iterator<ImageReader> readers = ImageIO.getImageReaders(input);
        
        if (readers == null || !readers.hasNext()) {
            throw new RuntimeException("No ImageReaders found");
        }

        ImageReader reader = readers.next();
        reader.setInput(input);
        
        String format = reader.getFormatName();
        if ("JPEG".equalsIgnoreCase(format) || "JPG".equalsIgnoreCase(format)) {
        	try{
        		IIOMetadata metadata = reader.getImageMetadata(0);
                String metadataFormat = metadata.getNativeMetadataFormatName();
                IIOMetadataNode iioNode = (IIOMetadataNode) metadata.getAsTree(metadataFormat);
     
                NodeList children = iioNode.getElementsByTagName("app14Adobe");
                if (children.getLength() > 0) {
                    
                	iioNode = (IIOMetadataNode) children.item(0);
                    int transform = Integer.parseInt(iioNode.getAttribute("transform"));
                    
                    if(transform == 0 || transform == 2)
                    {
                    	Raster raster = reader.readRaster(0, reader.getDefaultReadParam());
    	                BufferedImage bi = createJPEG4(raster, transform);
    	                
                    	reader.dispose();
                    	if (input != null) try { input.close(); } catch (IOException ex) {}
                    	return bi;
                    }   
                }
        	}catch(Exception e) {
        		// Continue and assume normal JPEG...
        	}
        }
        
        ImageReadParam param = reader.getDefaultReadParam();
	    BufferedImage bi;
        try {
            bi = reader.read(0, param);
        } finally {
            reader.dispose();
            try {input.close();} catch (IOException e) {}
        }
        return bi;
    }
 
 
    /**
     * Java's ImageIO can't process 4-component images
     * <p/>
     * and Java2D can't apply AffineTransformOp
     * either,
     * <p/>
     * so convert raster data to
     * RGB.
     * <p/>
     * Technique due to Mark Stephens.
     * <p/>
     * Free for any use.
     */
    private static BufferedImage createJPEG4(Raster raster, int xform) {
        int w = raster.getWidth();
        int h = raster.getHeight();
        byte[] rgb = new byte[w * h * 3];
 
        // if (Adobe_APP14 and transform==2) then YCCK else CMYK
        if (xform == 2) {    // YCCK -- Adobe
            float[] Y = raster.getSamples(0, 0, w, h, 0, (float[]) null);
            float[] Cb = raster.getSamples(0, 0, w, h, 1, (float[]) null);
            float[] Cr = raster.getSamples(0, 0, w, h, 2, (float[]) null);
            float[] K = raster.getSamples(0, 0, w, h, 3, (float[]) null);
 
            for (int i = 0, imax = Y.length, base = 0; i < imax; i++, base += 3) {
                float k = 220 - K[i], y = 255 - Y[i], cb = 255 - Cb[i], cr = 255 - Cr[i];
 
                double val = y + 1.402 * (cr - 128) - k;
                val = (val - 128) * .65f + 128;
                rgb[base] = val < 0.0 ? (byte) 0 : val > 255.0 ? (byte) 0xff : (byte) (val + 0.5);
 
                val = y - 0.34414 * (cb - 128) - 0.71414 * (cr - 128) - k;
                val = (val - 128) * .65f + 128;
                rgb[base + 1] = val < 0.0 ? (byte) 0 : val > 255.0 ? (byte) 0xff : (byte) (val + 0.5);
 
                val = y + 1.772 * (cb - 128) - k;
                val = (val - 128) * .65f + 128;
                rgb[base + 2] = val < 0.0 ? (byte) 0 : val > 255.0 ? (byte) 0xff : (byte) (val + 0.5);
            }
 
        } else {
            // assert xform==0: xform;
            // CMYK
            int[] C = raster.getSamples(0, 0, w, h, 0, (int[]) null);
            int[] M = raster.getSamples(0, 0, w, h, 1, (int[]) null);
            int[] Y = raster.getSamples(0, 0, w, h, 2, (int[]) null);
            int[] K = raster.getSamples(0, 0, w, h, 3, (int[]) null);
 
            for (int i = 0, imax = C.length, base = 0; i < imax; i++, base += 3) {
                int c = 255 - C[i];
                int m = 255 - M[i];
                int y = 255 - Y[i];
                int k = 255 - K[i];
                float kk = k / 255f;
 
                rgb[base] = (byte) (255 - Math.min(255f, c * kk + k));
                rgb[base + 1] = (byte) (255 - Math.min(255f, m * kk + k));
                rgb[base + 2] = (byte) (255 - Math.min(255f, y * kk + k));
            }
        }
 
        // from other image types we know InterleavedRaster's can be
        // manipulated by AffineTransformOp, so create one of
        // those.
        raster = Raster.createInterleavedRaster(new DataBufferByte(rgb, rgb.length), w, h, w * 3, 3, new int[]{0, 1, 2}, null);
 
        ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_sRGB);
        ColorModel cm = new ComponentColorModel(cs, false, true, Transparency.OPAQUE, DataBuffer.TYPE_BYTE);
        return new BufferedImage(cm, (WritableRaster) raster, true, null);
    }
}
