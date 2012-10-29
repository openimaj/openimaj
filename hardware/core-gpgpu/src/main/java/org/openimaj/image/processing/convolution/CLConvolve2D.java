package org.openimaj.image.processing.convolution;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import org.openimaj.image.Image;
import org.openimaj.image.processing.CLImageProcessor;

import com.nativelibs4java.opencl.CLBuffer;
import com.nativelibs4java.opencl.CLContext;
import com.nativelibs4java.opencl.CLMem.Usage;

/**
 * Simple 2D convolution. Note that this implementation is just a proof of
 * concept and behaves in a really dumb/slow fashion.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 * @param <I>
 *            Type of image
 */
public class CLConvolve2D<I extends Image<?, I>> extends CLImageProcessor<I> {
	private CLBuffer<Float> floatData;

	/**
	 * Construct the convolution operator with the given kernel and OpenCL
	 * context
	 * 
	 * @param context
	 *            the OpenCL context
	 * @param kernel
	 *            the convolution kernel
	 * @throws IOException
	 *             if an error occurs
	 */
	public CLConvolve2D(CLContext context, float[][] kernel) throws IOException {
		super(context, CLConvolve2D.class.getResource("Convolve.cl"));

		final ByteBuffer bb = ByteBuffer.allocateDirect(kernel.length * kernel.length * 4);
		bb.order(context.getByteOrder());
		final FloatBuffer fb = bb.asFloatBuffer();

		for (int y = 0; y < kernel.length; y++)
			for (int x = 0; x < kernel.length; x++)
				fb.put(kernel[y][x]);

		// Note: the buffer needs to be an ivar as it seems to
		// get garbage collected (the setArg line below internally
		// just grabs the native pointer to the buffer; java has no
		// way of knowing that we still need the data...)
		floatData = context.createFloatBuffer(Usage.Input, fb, true);

		this.kernel.setArg(2, floatData);
		this.kernel.setArg(3, kernel.length);
	}
}
