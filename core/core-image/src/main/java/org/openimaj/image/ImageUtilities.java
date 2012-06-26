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

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.ComponentSampleModel;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.util.Map;
import java.util.StringTokenizer;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;

import org.openimaj.image.colour.ColourSpace;

/**
 * 	A static utility class with methods for dealing with images.
 * 
 *  @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class ImageUtilities 
{
	/** Lookup table for byte->float conversion */
	public final static float[] BYTE_TO_FLOAT_LUT;
	static {
		BYTE_TO_FLOAT_LUT = new float[256];
		for (int i=0; i<BYTE_TO_FLOAT_LUT.length; i++) BYTE_TO_FLOAT_LUT[i] = (float)i/255f;
	}
	
	/**
	 * Calculate normalised RGB planes. Extracts the planes from the given
	 * RGB BufferedImage and returns an array of FImage of length 3. The
	 * images are ordered Red, Green and Blue.
	 * 
	 * @param bimg A {@link BufferedImage} from which the planes are extracted. 
	 * @return An array of {@link FImage}.
	 */
	public static FImage[] getNormalisedColourPlanes(BufferedImage bimg) {
		FImage [] images = new FImage[3];
		BufferedImage workingImage = createWorkingImage(bimg);
		int [] data = workingImage.getRGB(0, 0, workingImage.getWidth(), workingImage.getHeight(), null, 0, workingImage.getWidth());
		
		
		images[0] = new FImage(data, bimg.getWidth(), bimg.getHeight(), ARGBPlane.RED);
		images[1] = new FImage(data, bimg.getWidth(), bimg.getHeight(), ARGBPlane.GREEN);
		images[2] = new FImage(data, bimg.getWidth(), bimg.getHeight(), ARGBPlane.BLUE);

		int r, c;
		for (r = 0; r < images[0].height; r++) {
			for (c = 0; c < images[0].width; c++) {
				float norm = (float)Math.sqrt(
						(images[0].pixels[r][c] * images[0].pixels[r][c]) + 
						(images[1].pixels[r][c] * images[1].pixels[r][c]) + 
						(images[2].pixels[r][c] * images[2].pixels[r][c])
						);
				
				if (norm == 0 && images[0].pixels[r][c] == 0)
					images[0].pixels[r][c] /= (1.0 / Math.sqrt(3.0));
				else
					images[0].pixels[r][c] /= norm;
				
				if (norm == 0 && images[1].pixels[r][c] == 0)
					images[1].pixels[r][c] /= (1.0 / Math.sqrt(3.0));
				else
					images[1].pixels[r][c] /= norm;
				
				if (norm == 0 && images[2].pixels[r][c] == 0)
					images[2].pixels[r][c] /= (1.0 / Math.sqrt(3.0));
				else
					images[2].pixels[r][c] /= norm;
			}
		}
		
		return images;
	}
	
	/**
	 * 	Returns a ARGB BufferedImage, even if the input BufferedImage is not
	 * 	ARGB format.
	 * 
	 *  @param bimg The {@link BufferedImage} to normalise to ARGB
	 *  @return An ARGB {@link BufferedImage}
	 */
	public static BufferedImage createWorkingImage(BufferedImage bimg) 
	{
		// to avoid performance complications in the getRGB method, we pre-calculate the RGB rep of the image
		BufferedImage workingImage;
		if (bimg.getType() == BufferedImage.TYPE_INT_ARGB) {
			workingImage = bimg;
		} else {
			workingImage = new BufferedImage(bimg.getWidth(), bimg.getHeight(), BufferedImage.TYPE_INT_ARGB);
			Graphics2D g2d = workingImage.createGraphics();
			g2d.drawImage(bimg, null, 0, 0);
		}
		return workingImage;
	}
	
	/**
	 * 	Write the given image to the given file with the given format name.
	 * 	Format names are the same as used by 
	 * 	{@link ImageIO#write(java.awt.image.RenderedImage, String, File)}.
	 * 
	 *  @param image The image to write.
	 *  @param formatName a {@link String} containing the informal name of the format.
	 *  @param output The {@link File} to write the image to.
	 *  @throws IOException If the image cannot be written to the file.
	 */
	public static void write(Image<?,?> image, String formatName, File output) throws IOException {
		ImageIO.write(createBufferedImage(image), formatName, output);
	}
	
	/**
	 * 	Write the given image to the given file with the given format name.
	 * 	Format names are the same as used by 
	 * 	{@link ImageIO#write(java.awt.image.RenderedImage, String, OutputStream)}.
	 * 
	 *  @param image The image to write.
	 *  @param formatName a {@link String} containing the informal name of the format.
	 *  @param output The {@link OutputStream} to write the image to.
	 *  @throws IOException If the image cannot be written to the file.
	 */
	public static void write(Image<?,?> image, String formatName, OutputStream output) throws IOException {
		ImageIO.write(createBufferedImage(image), formatName, output);
	}
	
	/**
	 * 	Write the given image to the given file with the given format name.
	 * 	Format names are the same as used by 
	 * 	{@link ImageIO#write(java.awt.image.RenderedImage, String, ImageOutputStream)}.
	 * 
	 *  @param image The image to write.
	 *  @param formatName a {@link String} containing the informal name of the format.
	 *  @param output The {@link ImageOutputStream} to write the image to.
	 *  @throws IOException If the image cannot be written to the file.
	 */
	public static void write(Image<?,?> image, String formatName, ImageOutputStream output) throws IOException {
		ImageIO.write(createBufferedImage(image), formatName, output);
	}
	
	/**
	 * 	Write the given image to the given file, guessing the format name from the extension.
	 * 	Format names are the same as used by 
	 * 	{@link ImageIO#write(java.awt.image.RenderedImage, String, File)}.
	 * 
	 *  @param image The image to write.
	 *  @param output The {@link File} to write the image to.
	 *  @throws IOException If the image cannot be written to the file.
	 */
	public static void write(Image<?,?> image, File output) throws IOException {
		String name = output.getName();
		String format = name.substring(name.lastIndexOf(".") + 1);
		
		format = format.toLowerCase().trim();
		
		ImageIO.write(createBufferedImage(image), format, output);
	}
	
	/**
	 * Create an FImage from a buffered image. 
	 * @param image the image
	 * @return an FImage representation of the input image
	 */
	public static FImage createFImage(BufferedImage image) {
		BufferedImage bimg = createWorkingImage(image);
		int [] data = bimg.getRGB(0, 0, bimg.getWidth(), bimg.getHeight(), null, 0, bimg.getWidth());
		
		return new FImage(data, bimg.getWidth(), bimg.getHeight());		
	}
	
	/**
	 * Create an MBFImage from a buffered image. 
	 * @param image the image
	 * @param alpha should the resultant MBFImage have an alpha channel
	 * @return an MBFImage representation of the input image
	 */
	public static MBFImage createMBFImage(BufferedImage image, boolean alpha) {
		BufferedImage bimg = createWorkingImage(image);
		int [] data = bimg.getRGB(0, 0, bimg.getWidth(), bimg.getHeight(), null, 0, bimg.getWidth());
		
		return new MBFImage(data, bimg.getWidth(), bimg.getHeight(), alpha);		
	}
	
	/**
	 * 	Reads an {@link FImage} from the given file.
	 * 
	 *  @param input The file to read the {@link FImage} from.
	 *  @return An {@link FImage}
	 *  @throws IOException if the file cannot be read
	 */
	public static FImage readF(File input) throws IOException {
		return createFImage(ExtendedImageIO.read(input));
	}
	
	/**
	 * 	Reads an {@link FImage} from the given input stream.
	 * 
	 *  @param input The input stream to read the {@link FImage} from.
	 *  @return An {@link FImage}
	 *  @throws IOException if the stream cannot be read
	 */
	public static FImage readF(InputStream input) throws IOException {
		return createFImage(ExtendedImageIO.read(input));
	}
	
	/**
	 * 	Reads an {@link FImage} from the given URL.
	 * 
	 *  @param input The URL to read the {@link FImage} from.
	 *  @return An {@link FImage}
	 *  @throws IOException if the URL stream cannot be read
	 */
	public static FImage readF(URL input) throws IOException {
		return createFImage(ExtendedImageIO.read(input));
	}
	
	/**
	 * 	Reads an {@link MBFImage} from the given file.
	 * 
	 *  @param input The file to read the {@link MBFImage} from.
	 *  @return An {@link MBFImage}
	 *  @throws IOException if the file cannot be read
	 */
	public static MBFImage readMBF(File input) throws IOException {
		return createMBFImage(ExtendedImageIO.read(input), false);
	}
	
	/**
	 * 	Reads an {@link MBFImage} from the given input stream.
	 * 
	 *  @param input The input stream to read the {@link MBFImage} from.
	 *  @return An {@link MBFImage}
	 *  @throws IOException if the stream cannot be read
	 */
	public static MBFImage readMBF(InputStream input) throws IOException {
		return createMBFImage(ExtendedImageIO.read(input), false);
	}
	
	/**
	 * 	Reads an {@link MBFImage} from the given URL.
	 * 
	 *  @param input The URL to read the {@link MBFImage} from.
	 *  @return An {@link MBFImage}
	 *  @throws IOException if the URL stream cannot be read
	 */
	public static MBFImage readMBF(URL input) throws IOException {
		return createMBFImage(ExtendedImageIO.read(input), false);
	}
	
	/**
	 * 	Reads an {@link MBFImage} from the given file. The resultant
	 * 	MBImage will contain an alpha channel
	 * 
	 *  @param input The file to read the {@link MBFImage} from.
	 *  @return An {@link MBFImage}
	 *  @throws IOException if the file cannot be read
	 */
	public static MBFImage readMBFAlpha(File input) throws IOException {
		return createMBFImage(ExtendedImageIO.read(input), true);
	}
	
	/**
	 * 	Reads an {@link MBFImage} from the given input stream.  The resultant
	 * 	MBImage will contain an alpha channel
	 * 
	 *  @param input The input stream to read the {@link MBFImage} from.
	 *  @return An {@link MBFImage}
	 *  @throws IOException if the stream cannot be read
	 */
	public static MBFImage readMBFAlpha(InputStream input) throws IOException {
		return createMBFImage(ExtendedImageIO.read(input), true);
	}
	
	/**
	 * 	Reads an {@link MBFImage} from the given URL. The resultant
	 * 	MBImage will contain an alpha channel
	 * 
	 *  @param input The URL to read the {@link MBFImage} from.
	 *  @return An {@link MBFImage}
	 *  @throws IOException if the URL stream cannot be read
	 */
	public static MBFImage readMBFAlpha(URL input) throws IOException {
		return createMBFImage(ExtendedImageIO.read(input), true);
	}
	
	
	/**
	 * 	Checks whether the width and height of all the given images
	 * 	match.
	 * 
	 *  @param images The images to compare sizes.
	 *  @return TRUE if all the images are the same size; FALSE otherwise
	 */
	protected static boolean checkSameSize(Image<?,?>... images) {
		if (images == null || images.length == 0) return true;
		
		Image<?,?> image = images[0];
		int w = image.getWidth();
		int h = image.getHeight();
		
		return checkSize(h, w, images);
	}
	
	/**
	 * 	Checks whether the width and height of all the given images
	 * 	match the given width and height.
	 * 
	 * 	@param h The height to match against all the images
	 * 	@param w The width to match against all the images
	 *  @param images The images to compare sizes.
	 *  @return TRUE if all the images are <code>wxh</code> in size; FALSE otherwise
	 */
	protected static boolean checkSize(int h, int w, Image<?,?>... images) {
		for (Image<?,?> image : images)
			if (image.getHeight()!=h || image.getWidth()!=w) return false;
		return true;
	}
	
	/**
	 * 	Checks whether the width and height of all the given images
	 * 	match the given width and height.
	 * 
	 * 	@param h The height to match against all the images
	 * 	@param w The width to match against all the images
	 *  @param images The images to compare sizes.
	 *  @return TRUE if all the images are <code>wxh</code> in size; FALSE otherwise
	 */
	protected static boolean checkSize(int h, int w, Iterable<? extends Image<?,?>> images) {
		for (Image<?,?> image : images)
			if (image.getHeight()!=h || image.getWidth()!=w) return false;
		return true;
	}

	/**
	 * 	Reads a PNM header from the byte array containing the PNM binary data.
	 * 	The <code>headerData</code> variable will be populated with the 
	 * 	header information. Returns the number of bytes read from the array.
	 * 
	 *  @param data The PNM binary data.
	 *  @param headerData A {@link Map} to populate with header information.
	 *  @return The number of bytes read from the array.
	 *  @throws IOException if the byte array does not contain PNM information.
	 */
	protected static int pnmReadHeader(byte [] data, Map<String, Integer>headerData) throws IOException
	{
		ByteArrayInputStream bais = new ByteArrayInputStream(data);
		InputStreamReader isr = new InputStreamReader(bais);
		BufferedReader br = new BufferedReader(isr);
		int count=0, bytesRead=0;
		while (count<4) {
			String line = br.readLine();

			StringTokenizer st = new StringTokenizer(line);

			while (st.hasMoreTokens()) 
			{
				String tok = st.nextToken();

				if (tok.startsWith("#")) break;
				else 
				{
					switch (count) 
					{
					case 0: //magic
					headerData.put("magic", Integer.decode(tok.substring(1)));
					break;
					case 1: //width
						headerData.put("width", Integer.decode(tok));
						break;
					case 2: //height
						headerData.put("height", Integer.decode(tok));
						break;
					case 3: //maxval
						headerData.put("maxval", Integer.decode(tok));
						break;
					}
					count++;
				}
			}
			bytesRead += (line.length() + 1); //+1 for the newlines
		}

		return bytesRead;
	}	

	/**
	 * 	Returns the contents of a file in a byte array.
	 * 
	 *  @param file The file to read
	 *  @return A byte array representation of the file.
	 *  @throws IOException if the file cannot be read fully.
	 */
	protected static byte[] getBytes(File file) throws IOException {
		InputStream is = new FileInputStream(file);

		// Get the size of the file
		long length = file.length();

		// You cannot create an array using a long type.
		// It needs to be an int type.
		// Before converting to an int type, check
		// to ensure that file is not larger than Integer.MAX_VALUE.
		if (length > Integer.MAX_VALUE) {
			// File is too large
		}

		// Create the byte array to hold the data
		byte[] bytes = new byte[(int)length];

		// Read in the bytes
		int offset = 0;
		int numRead = 0;
		while (offset < bytes.length
				&& (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
			offset += numRead;
		}

		// Ensure all the bytes have been read in
		if (offset < bytes.length) {
			throw new IOException("Could not completely read file "+file.getName());
		}

		// Close the input stream and return bytes
		is.close();
		return bytes;
	}


	/**
	 *	Converts the input stream to a byte array. The input stream is fully read.
	 * 	
	 *  @param stream The {@link InputStream} to convert to byte array
	 *  @return A byte array representation of the {@link InputStream} data.
	 *  @throws IOException if the input stream cannot be fully read.
	 */
	protected static byte[] getBytes(InputStream stream) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte [] b = new byte[4096];
		
		while (stream.read(b) > 0)
			baos.write(b);
		
		return baos.toByteArray();	
	}
	
	/**
	 * Convert any image to a {@link BufferedImage}.
	 * @param img image to convert
	 * @return BufferedImage representation
	 */
	public static BufferedImage createBufferedImage(Image<?,?> img) {
		return createBufferedImage(img, null);
	}
	
	/**
	 * Convert any image to a {@link BufferedImage}.
	 * @param img image to convert
	 * @param bimg BufferedImage to draw into if possible. Can be null.
	 * @return BufferedImage representation
	 */
	public static BufferedImage createBufferedImage(Image<?,?> img, BufferedImage bimg) {
		if (bimg == null || bimg.getWidth() != img.getWidth() || bimg.getHeight() != img.getHeight() || bimg.getType() != BufferedImage.TYPE_INT_ARGB)
			bimg = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_ARGB);
		
		bimg.setRGB(0, 0, img.getWidth(), img.getHeight(), img.toPackedARGBPixels(), 0, img.getWidth());
		
		return bimg;
	}
	
	/**
	 * Convert any image to a {@link BufferedImage}.
	 * @param img image to convert
	 * @return BufferedImage representation
	 */
	public static BufferedImage createBufferedImageForDisplay(Image<?,?> img) {
		if (img instanceof MBFImage) 
			return createBufferedImageForDisplay((MBFImage)img);
		else if (img instanceof FImage)
			return createBufferedImage((FImage)img);
		return createBufferedImage(img);
	}
	
	/**
	 * Convert any image to a {@link BufferedImage}.
	 * @param img image to convert
	 * @param bimg BufferedImage to draw into if possible. Can be null.
	 * @return BufferedImage representation
	 */
	public static BufferedImage createBufferedImageForDisplay(Image<?,?> img, BufferedImage bimg) {
		if (img instanceof MBFImage) 
			return createBufferedImageForDisplay((MBFImage)img, bimg);
		else if (img instanceof FImage)
			return createBufferedImage((FImage)img, bimg);
		return createBufferedImage(img, bimg);
	}
	
	/**
	 * Efficiently create a TYPE_3BYTE_BGR for display if possible.
	 * This is typically much faster than to create and display than an
	 * ARGB buffered image. If the input image is not in RGB format, then
	 * the ARGB form will be returned instead.
	 * 
	 * @param img the image to convert
	 * @return the converted image
	 */
	public static BufferedImage createBufferedImageForDisplay(MBFImage img) {
		return createBufferedImageForDisplay(img, null);
	}
	
	/**
	 * Efficiently create a TYPE_3BYTE_BGR for display if possible.
	 * This is typically much faster than to create and display than an
	 * ARGB buffered image. If the input image is not in RGB format, then
	 * the ARGB form will be returned instead.
	 * 
	 * @param img the image to convert
	 * @param ret the image to draw into if possible. Can be null.
	 * @return the converted image. Might not be the same as the ret parameter. 
	 */
	public static BufferedImage createBufferedImageForDisplay(MBFImage img, BufferedImage ret) {
		if (img.colourSpace != ColourSpace.RGB)
			return createBufferedImage(img, ret);
		
		final int width = img.getWidth();
		final int height = img.getHeight();
		
		if (ret == null || ret.getWidth() != width || ret.getHeight() != height || ret.getType() != BufferedImage.TYPE_3BYTE_BGR)
			ret = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
		
		final WritableRaster raster = ret.getRaster();

		final float[][] r = img.getBand(0).pixels;
		final float[][] g = img.getBand(1).pixels;
		final float[][] b = img.getBand(2).pixels;
		
		final ComponentSampleModel sm = (ComponentSampleModel) raster.getSampleModel();
		final DataBufferByte db = (DataBufferByte) raster.getDataBuffer();
		final int scanlineStride = sm.getScanlineStride();
		final int pixelStride = sm.getPixelStride();
		
		final byte[] data = db.getData();
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				data[y*scanlineStride + x*pixelStride + 2] = (byte)(r[y][x] * 255);
				data[y*scanlineStride + x*pixelStride + 1] = (byte)(g[y][x] * 255);
				data[y*scanlineStride + x*pixelStride] = (byte)(b[y][x] * 255);
			}
		}

		return ret;
	}

	/**
	 * Efficiently create a TYPE_BYTE_GRAY for display.
	 * This is typically much faster than to create and display than an
	 * ARGB buffered image. 
	 * 
	 * @param img the image to convert
	 * @return the converted image
	 */
	public static BufferedImage createBufferedImage(FImage img) {
		return createBufferedImage(img, null);
	}
	
	/**
	 * Efficiently create a TYPE_BYTE_GRAY for display.
	 * This is typically much faster than to create and display than an
	 * ARGB buffered image. 
	 * 
	 * @param img the image to convert
	 * @param ret BufferedImage to draw into if possible. Can be null.
	 * @return the converted image
	 */
	public static BufferedImage createBufferedImage(FImage img, BufferedImage ret) {
		final int width = img.getWidth();
		final int height = img.getHeight();
		
		if (ret == null || ret.getWidth() != width || ret.getHeight() != height || ret.getType() != BufferedImage.TYPE_BYTE_GRAY)
			ret = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
		
		final WritableRaster raster = ret.getRaster();

		final float[][] p = img.pixels;
		
		final ComponentSampleModel sm = (ComponentSampleModel) raster.getSampleModel();
		final DataBufferByte db = (DataBufferByte) raster.getDataBuffer();
		final int scanlineStride = sm.getScanlineStride();
		final int pixelStride = sm.getPixelStride();
		
		final byte[] data = db.getData();
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				data[y*scanlineStride + x*pixelStride] = (byte)(p[y][x] * 255);
			}
		}

		return ret;
	}

	/**
	 * Write an image to a {@link DataOutput}.
	 * @param img the image
	 * @param formatName the format
	 * @param out the output
	 * @throws IOException if an error occurs
	 */
	public static void write(Image<?,?> img, String formatName, DataOutput out) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		write(img, formatName, baos);
		out.writeInt(baos.size());
		out.write(baos.toByteArray());
	}

	/**
	 * Read an {@link FImage} from a DataInput
	 * @param in input
	 * @return new FImage
	 * @throws IOException if error occurs
	 */
	public static FImage readF(DataInput in) throws IOException {
		int sz = in.readInt();
		byte[] bytes = new byte[sz];
		in.readFully(bytes);
		
		return readF(new ByteArrayInputStream(bytes));
	}
}
