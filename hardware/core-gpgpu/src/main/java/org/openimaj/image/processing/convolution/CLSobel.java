package org.openimaj.image.processing.convolution;

import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.openimaj.image.CLImageConversion;
import org.openimaj.image.FImage;
import org.openimaj.image.MBFImage;
import org.openimaj.image.processing.CLImageAnalyser;

import com.nativelibs4java.opencl.CLContext;
import com.nativelibs4java.opencl.CLEvent;
import com.nativelibs4java.opencl.CLImage2D;
import com.nativelibs4java.opencl.CLImageFormat;
import com.nativelibs4java.opencl.CLImageFormat.ChannelDataType;
import com.nativelibs4java.opencl.CLImageFormat.ChannelOrder;
import com.nativelibs4java.opencl.CLMem;
import com.nativelibs4java.opencl.CLQueue;

/**
 * OpenCL implementation of a 3x3 Sobel operator. Computes the magnitude, orientation
 * and gradients (in x & y) in parallel. 
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 */
public class CLSobel extends CLImageAnalyser<FImage> {
	/**
	 * The computed Sobel magnitude
	 */
	public FImage magnitude;
	
	/**
	 * The computed Sobel orientations
	 */
	public FImage orientation;
	
	/**
	 * The computed Sobel gradient in the X direction
	 */
	public FImage gradientX;
	
	/**
	 * The computed Sobel gradient in the Y direction
	 */
	public FImage gradientY;

	/**
	 * Default Constructor
	 */
	public CLSobel() {
		super(getKernel());
	}
	
	/**
	 * Construct with context.
	 * @param ctx the context
	 */
	public CLSobel(CLContext ctx) {
		super(ctx, getKernel());
	}
	
	private static String getKernel() {
		try {
			return IOUtils.toString(CLSobel.class.getResource("Sobel3.cl"));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void analyseImage(FImage image) {
		CLQueue queue = context.createDefaultQueue();

		CLImage2D in = CLImageConversion.convert(context, image);
		
		CLImageFormat outFmt = new CLImageFormat(ChannelOrder.RGBA, ChannelDataType.Float);
		CLImage2D out = context.createImage2D(CLMem.Usage.Output, outFmt, in.getWidth(), in.getHeight());
		
		kernel.setArgs(in, out);
		CLEvent evt = kernel.enqueueNDRange(queue, new int[] {(int) in.getWidth(), (int) in.getHeight()});

		MBFImage res = CLImageConversion.convert(queue, evt, out, new MBFImage(image.width, image.height, 4));
		
		this.magnitude = res.bands.get(0);
		this.orientation = res.bands.get(1);
		this.gradientX = res.bands.get(2);
		this.gradientY = res.bands.get(3);
		
		in.release();
		out.release();
		queue.release();
	}

	/**
	 * @return the magnitude
	 */
	public FImage getMagnitude() {
		return magnitude;
	}

	/**
	 * @return the orientation
	 */
	public FImage getOrientation() {
		return orientation;
	}

	/**
	 * @return the gradientX
	 */
	public FImage getGradientX() {
		return gradientX;
	}

	/**
	 * @return the gradientY
	 */
	public FImage getGradientY() {
		return gradientY;
	}
}
