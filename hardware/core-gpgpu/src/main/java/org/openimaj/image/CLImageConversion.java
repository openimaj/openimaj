package org.openimaj.image;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import org.openimaj.image.colour.ColourSpace;

import com.nativelibs4java.opencl.CLContext;
import com.nativelibs4java.opencl.CLEvent;
import com.nativelibs4java.opencl.CLImage2D;
import com.nativelibs4java.opencl.CLImageFormat;
import com.nativelibs4java.opencl.CLImageFormat.ChannelDataType;
import com.nativelibs4java.opencl.CLImageFormat.ChannelOrder;
import com.nativelibs4java.opencl.CLMem;
import com.nativelibs4java.opencl.CLQueue;

/**
 * Utility methods for converting between OpenIMAJ {@link Image} types
 * and {@link CLImage2D}s for GPGPU acceleration.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class CLImageConversion {
	private static final int FLOAT_SIZE = Float.SIZE / 8;
	
	/**
	 * The optimal channel orders for FImages. Only RGBA is guaranteed to 
	 * be available. 
	 */
	private static final ChannelOrder[] bestFImageChannels = {
		ChannelOrder.INTENSITY, ChannelOrder.LUMINANCE, ChannelOrder.R, ChannelOrder.RGBA
	};
	
	private CLImageConversion() {}
	
	private static FloatBuffer convertToFBDirect(MBFImage image, ByteOrder byteOrder) {
		final int width = image.getWidth();
		final int height = image.getHeight();

		final ByteBuffer bb = ByteBuffer.allocateDirect(width * height * 4 * FLOAT_SIZE);
		bb.order(byteOrder);

		final FloatBuffer fb = bb.asFloatBuffer();

		final FImage[] bands = image.bands.toArray(new FImage[image.numBands()]);

		final int nbands = Math.min(4, image.numBands());
		final int extraBands = 4 - nbands;
		
		for (int y=0; y<height; y++) {
			for (int x=0; x<width; x++) {
				for (int b=0; b<nbands; b++) { 
					fb.put(bands[b].pixels[y][x]);
				}
				for (int b=0; b<extraBands; b++) { 
					fb.put(1);
				}
			}
		}

		return fb;
	}

	private static FloatBuffer convertToFBDirect(FImage image, ByteOrder byteOrder) {
		final int width = image.getWidth();
		final int height = image.getHeight();

		final ByteBuffer bb = ByteBuffer.allocateDirect(width * height * FLOAT_SIZE);
		bb.order(byteOrder);

		final FloatBuffer fb = bb.asFloatBuffer();
		final float[][] pix = image.pixels;
		
		for (int y=0; y<height; y++) {
			for (int x=0; x<width; x++) {
				fb.put( pix[y][x] );
			}
		}

		return fb;
	}
	
	/**
	 * Converts an {@link MBFImage} to a {@link FloatBuffer} containing packed
	 * RGBA pixels. If the byte order is BIG_ENDIAN then the data is
	 * copied to a buffer on the Java heap which is then wrapped
	 * by a {@link FloatBuffer}. If the byte order is LITTLE_ENDIAN
	 * then we allocate a direct buffer in system RAM and copy
	 * the pixel values to that directly.
	 * 
	 * @param image The image to convert
	 * @param byteOrder the required byte order of the data
	 * @return a {@link FloatBuffer} containing packed RGBA pixels
	 */
	public static FloatBuffer convertToFB(MBFImage image, ByteOrder byteOrder) {
		if (byteOrder != ByteOrder.BIG_ENDIAN)
			return convertToFBDirect(image, byteOrder);

		final int width = image.getWidth();
		final int height = image.getHeight();
		final float[] data = new float[width * height * 4];

		final FImage[] bands = image.bands.toArray(new FImage[image.numBands()]);

		final int nbands = Math.min(4, bands.length);
		final int extraBands = 4 - nbands;
		
		for (int y=0, i=0; y<height; y++) {
			for (int x=0; x<width; x++) {
				for (int b=0; b<nbands; b++, i++) { 
					data[i] = bands[b].pixels[y][x];
				}
				for (int b=0; b<extraBands; b++, i++) { 
					data[i] = 1;
				}
			}
		}

		return FloatBuffer.wrap(data);
	}
	
	/**
	 * Converts an FImage to a {@link FloatBuffer} containing packed
	 * intensity pixels. If the byte order is BIG_ENDIAN then the data is
	 * copied to a buffer on the Java heap which is then wrapped
	 * by a {@link FloatBuffer}. If the byte order is LITTLE_ENDIAN
	 * then we allocate a direct buffer in system RAM and copy
	 * the pixel values to that directly.
	 * 
	 * @param image The image to convert
	 * @param byteOrder the required byte order of the data
	 * @return a {@link FloatBuffer} containing packed intensity pixels
	 */
	public static FloatBuffer convertToFB(FImage image, ByteOrder byteOrder) {
		if (byteOrder != ByteOrder.BIG_ENDIAN)
			return convertToFBDirect(image, byteOrder);

		final int width = image.getWidth();
		final int height = image.getHeight();
		
		final float[] data = new float[width * height];
		final float[][] pix = image.pixels;

		for (int y=0, i=0; y<height; y++) {
			for (int x=0; x<width; x++, i++) {
				data[i] = pix[y][x];
			}
		}

		return FloatBuffer.wrap(data);
	}

	/**
	 * Convert an {@link MBFImage} to an RGBA {@link CLImage2D}.
	 * @param context the OpenCL context
	 * @param image the image to convert
	 * @return an OpenCL image
	 */
	public static CLImage2D convert(CLContext context, MBFImage image) {
		CLImageFormat format = new CLImageFormat(ChannelOrder.RGBA, ChannelDataType.Float);

		FloatBuffer cvt = convertToFB(image, context.getByteOrder());
		
		return context.createImage2D(CLMem.Usage.InputOutput, 
				format, image.getWidth(), image.getHeight(), 
				4 * image.getWidth() * FLOAT_SIZE, cvt, false);
	}
	
	private static ChannelOrder getBestFImageChannelOrder(CLContext context) {
		CLImageFormat[] formats = context.getSupportedImageFormats(CLMem.Flags.ReadWrite, CLMem.ObjectType.Image2D);
		List<ChannelOrder> found = new ArrayList<ChannelOrder>();
		
		for (CLImageFormat fmt : formats) {
			if (fmt.getChannelDataType() == ChannelDataType.Float)
				found.add(fmt.getChannelOrder());
		}
		
		for (ChannelOrder co : bestFImageChannels) {
			if (found.contains(co))
				return co;
		}
		return ChannelOrder.RGBA;
	}
	
	/**
	 * Convert an {@link FImage} to {@link CLImage2D}.
	 * @param context the OpenCL context
	 * @param image the image to convert
	 * @return an OpenCL image
	 */
	public static CLImage2D convert(CLContext context, FImage image) {
		ChannelOrder order = getBestFImageChannelOrder(context);
		CLImageFormat format = new CLImageFormat(order, ChannelDataType.Float);

		if (order == ChannelOrder.RGBA) {
			return convert(context, new MBFImage(image, image, image));
		} else {
			FloatBuffer cvt = convertToFB(image, context.getByteOrder());
		
			return context.createImage2D(CLMem.Usage.InputOutput, 
					format, image.getWidth(), image.getHeight(), 
					image.getWidth() * FLOAT_SIZE, cvt, false);
		}
	}

	/**
	 * Convert an {@link Image} to {@link CLImage2D}. {@link MBFImage}s will
	 * be converted to floating-point {@link ChannelOrder#RGBA} {@link CLImage2D}s. 
	 * {@link FImage}s will be converted to either single band or RGBA floating-point
	 * {@link CLImage2D}s depending on the hardware. All other types of
	 * image are converted by first converting to {@link BufferedImage}s, and 
	 * will have 1-byte per band pixels (ARGB). 
	 * 
	 * @param <I> The type of image being converted 
	 * @param context the OpenCL context
	 * @param image the image to convert
	 * @return an OpenCL image
	 */
	public static <I extends Image<?, I>> CLImage2D convert(CLContext context, I image) {
		if (((Object)image) instanceof MBFImage)
			return convert(context, (MBFImage) ((Object)image));
		if (((Object)image) instanceof FImage)
			return convert(context, (FImage) ((Object)image));
		
		BufferedImage bi = ImageUtilities.createBufferedImage(image);
		
		return context.createImage2D(CLMem.Usage.InputOutput, bi, true);
	}
	
	/**
	 * Convert packed RGBA pixels in a {@link FloatBuffer} back into an
	 * {@link MBFImage}. The method takes the {@link MBFImage} as an argument, and
	 * will fill it accordingly. If the image argument is null, a new
	 * {@link MBFImage} with the RGBA {@link ColourSpace} will be created. 
	 * If the given image is the wrong size, it will be resized. If the given
	 * image has less than four bands, then only these bands will be filled. 
	 * Any bands above the fourth will be ignored.  
	 * 
	 * @param fb the {@link FloatBuffer} containing the pixels 
	 * @param width the width
	 * @param height the height
	 * @param image the image to write to or null
	 * @return the image
	 */
	public static MBFImage convertFromFB(FloatBuffer fb, int width, int height, MBFImage image) {
		if (image == null)
			image = new MBFImage(width, height, ColourSpace.RGBA);
		
		if (image.getWidth() != width || image.getHeight() != height)
			image.internalAssign(image.newInstance(width, height));
		
		final FImage[] bands = image.bands.toArray(new FImage[image.numBands()]);

		final int nbands = Math.min(4, image.numBands());
		final int extraBands = 4 - nbands;
		
		for (int y=0; y<height; y++) {
			for (int x=0; x<width; x++) {
				for (int b=0; b<nbands; b++) { 
					bands[b].pixels[y][x] = fb.get();
				}
				for (int b=0; b<extraBands; b++) {
					fb.get();
				}
			}
		}

		return image;
	}
	
	/**
	 * Convert packed intensity pixels in a {@link FloatBuffer} back into an
	 * {@link FImage}. The method takes the {@link FImage} as an argument, and
	 * will fill it accordingly. If the image argument is null, a new
	 * {@link FImage} will be created.  If the given image is the wrong size, 
	 * it will be resized.   
	 * 
	 * @param fb the {@link FloatBuffer} containing the pixels 
	 * @param width the width
	 * @param height the height
	 * @param image the image to write to or null
	 * @return the image
	 */
	public static FImage convertFromFB(FloatBuffer fb, int width, int height, FImage image) {
		if (image == null)
			image = new FImage(width, height);
		
		if (image.getWidth() != width || image.getHeight() != height)
			image.internalAssign(image.newInstance(width, height));
		
		final float[][] pix = image.pixels;
		
		for (int y=0; y<height; y++) {
			for (int x=0; x<width; x++) {
				pix[y][x] = fb.get();
			}
		}

		return image;
	}
	
	/**
	 * Convert a {@link CLImage2D} to an {@link MBFImage}.
	 * 
	 * @param queue the {@link CLQueue}
	 * @param evt event to wait for
	 * @param clImage the image to convert
	 * @param oiImage the output image (or null)
	 * @return the output image
	 */
	public static MBFImage convert(CLQueue queue, CLEvent evt, CLImage2D clImage, MBFImage oiImage) {
		final int width = (int) clImage.getWidth();
		final int height = (int) clImage.getHeight();
		
		final ByteBuffer bb = ByteBuffer.allocateDirect(width * height * 4 * FLOAT_SIZE);
		bb.order(clImage.getContext().getByteOrder());
		
		final FloatBuffer fb = bb.asFloatBuffer();

		clImage.read(queue, 0, 0, width, height, clImage.getRowPitch(), fb, true, evt);

		return convertFromFB(fb, width, height, oiImage);
	}
	
	/**
	 * Convert a {@link CLImage2D} to an {@link FImage}.
	 * 
	 * @param queue the {@link CLQueue}
	 * @param evt event to wait for
	 * @param clImage the image to convert
	 * @param oiImage the output image (or null)
	 * @return the output image
	 */
	public static FImage convert(CLQueue queue, CLEvent evt, CLImage2D clImage, FImage oiImage) {
		if (clImage.getFormat().getChannelOrder() == ChannelOrder.RGBA) {
			return convert(queue, evt, clImage, oiImage == null ? null : new MBFImage(oiImage)).getBand(0);
		}
		
		final int width = (int) clImage.getWidth();
		final int height = (int) clImage.getHeight();
		
		final ByteBuffer bb = ByteBuffer.allocateDirect(width * height * FLOAT_SIZE);
		bb.order(clImage.getContext().getByteOrder());
		
		final FloatBuffer fb = bb.asFloatBuffer();

		clImage.read(queue, 0, 0, width, height, clImage.getRowPitch(), fb, true, evt);

		return convertFromFB(fb, width, height, oiImage);
	}
	
	/**
	 * Convert a {@link CLImage2D} to an {@link Image}.
	 * 
	 * @param <I> Type of image 
	 * @param queue the {@link CLQueue}
	 * @param evt event to wait for
	 * @param clImage the image to convert
	 * @param oiImage the output image (or null)
	 * @return the output image
	 */
	@SuppressWarnings("unchecked")
	public static <I extends Image<?, I>> I convert(CLQueue queue, CLEvent evt, CLImage2D clImage, I oiImage) {
		if (oiImage == null)
			throw new IllegalArgumentException("Output image cannot be null");
		
		if (((Object)oiImage) instanceof MBFImage) {
			MBFImage i = (MBFImage) ((Object)oiImage);
			return (I) (Object)convert(queue, evt, clImage, i);
		}
		
		if (((Object)oiImage) instanceof FImage) {
			FImage i = (FImage) ((Object)oiImage);
			return (I) (Object)convert(queue, evt, clImage, i);
		}
		
		
		BufferedImage bimg = clImage.read(queue, evt);
		
		return ImageUtilities.assignBufferedImage(bimg, oiImage);
	}
}
