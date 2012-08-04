package org.openimaj.image.processing;

import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.openimaj.image.CLImageConversion;
import org.openimaj.image.Image;

import com.nativelibs4java.opencl.CLBuildException;
import com.nativelibs4java.opencl.CLContext;
import com.nativelibs4java.opencl.CLEvent;
import com.nativelibs4java.opencl.CLImage2D;
import com.nativelibs4java.opencl.CLKernel;
import com.nativelibs4java.opencl.CLMem;
import com.nativelibs4java.opencl.CLProgram;
import com.nativelibs4java.opencl.CLQueue;
import com.nativelibs4java.opencl.JavaCL;
import com.nativelibs4java.opencl.CLPlatform.DeviceFeature;

/**
 * Simple image arithmetic functions implemented using OpenCL.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class CLImageArithmetic {
	private CLContext context;

	private CLKernel addImage;
	private CLKernel addConstant;
	private CLKernel subtractImage;
	private CLKernel subtractConstant;
	private CLKernel multiplyImage;
	private CLKernel multiplyConstant;
	private CLKernel divideImage;
	private CLKernel divideConstant;

	/**
	 * Default constructor. Automatically selects the context.
	 */
	public CLImageArithmetic() {
		CLProgram program = null;
		try {
			this.context = JavaCL.createBestContext(DeviceFeature.GPU);
			try {
				program = context.createProgram(IOUtils.toString(CLImageArithmetic.class.getResource("ImageArithmetic.cl")));
			} catch (IOException e) { e.printStackTrace(); }
			loadKernels(program);
		} catch (CLBuildException e) {
			//fallback to OpenCL on the CPU
			this.context = JavaCL.createBestContext(DeviceFeature.CPU);
			try {
				program = context.createProgram(IOUtils.toString(CLImageArithmetic.class.getResource("ImageArithmetic.cl")));
			} catch (IOException e1) { e.printStackTrace(); }
			loadKernels(program);
		}
	}

	/**
	 * Construct with the given context.
	 * @param context the context.
	 */
	public CLImageArithmetic(CLContext context) {
		try {
			this.context = context;
			CLProgram program = context.createProgram(IOUtils.toString(CLImageArithmetic.class.getResource("ImageArithmetic.cl")));
			loadKernels(program);
		} catch (IOException e) { e.printStackTrace(); }
	}

	private void loadKernels(CLProgram program) {
		addImage = program.createKernel("addImage");
		addConstant = program.createKernel("addConstant");
		subtractImage = program.createKernel("subtractImage");
		subtractConstant = program.createKernel("subtractConstant");
		multiplyImage = program.createKernel("multiplyImage");
		multiplyConstant = program.createKernel("multiplyConstant");
		divideImage = program.createKernel("divideImage");
		divideConstant = program.createKernel("divideConstant");
	}
	
	private synchronized CLEvent process(CLKernel kernel, CLQueue queue, CLImage2D in1, CLImage2D in2, CLImage2D out) {
		kernel.setArgs(in1, in2, out);
		return kernel.enqueueNDRange(queue, new int[] {(int) in1.getWidth(), (int) in1.getHeight()});
	}
	
	private synchronized CLEvent process(CLKernel kernel, CLQueue queue, CLImage2D in1, float[] amt, CLImage2D out) {
		kernel.setArgs(in1, amt, out);
		return kernel.enqueueNDRange(queue, new int[] {(int) in1.getWidth(), (int) in1.getHeight()});
	}

	private synchronized CLImage2D process(CLKernel kernel, CLImage2D in1, CLImage2D in2) {
		CLQueue queue = context.createDefaultQueue();
		CLImage2D out = context.createImage2D(CLMem.Usage.Output, in1.getFormat(), in1.getWidth(), in1.getHeight());

		process(kernel, queue, in1, in2, out).waitFor();
		queue.release();
		
		return out;
	}

	private synchronized CLImage2D process(CLKernel kernel, CLImage2D in, float[] amt) {
		CLQueue queue = context.createDefaultQueue();
		CLImage2D out = context.createImage2D(CLMem.Usage.Output, in.getFormat(), in.getWidth(), in.getHeight());

		process(kernel, queue, in, amt, out).waitFor();
		queue.release();
		
		return out;
	}

	/**
	 * Add two images, storing the result in another image
	 * 
	 * @param queue the command queue
	 * @param in1 the first image to add
	 * @param in2 the second image to add
	 * @param out the result image
	 * @return the event
	 */
	public CLEvent add(CLQueue queue, CLImage2D in1, CLImage2D in2, CLImage2D out) {
		return process(addImage, queue, in1, in2, out);
	}
	
	/**
	 * Add a constant to an image, storing the result in another image
	 * 
	 * @param queue the command queue
	 * @param in1 the first image to add
	 * @param amt the constant
	 * @param out the result image
	 * @return the event
	 */
	public CLEvent add(CLQueue queue, CLImage2D in1, float[] amt, CLImage2D out) {
		return process(addConstant, queue, in1, amt, out);
	}
	
	/**
	 * Add two images, returning a new image with the result
	 * 
	 * @param in1 the first image to add
	 * @param in2 the second image to add
	 * @return the event
	 */
	public CLImage2D add(CLImage2D in1, CLImage2D in2) {
		return process(addImage, in1, in2);
	}
	
	/**
	 * Add a constant to an image, returning a new image with the result
	 * 
	 * @param in1 the first image to add
	 * @param amt the constant
	 * @return the event
	 */
	public CLImage2D add(CLImage2D in1, float[] amt) {
		return process(addImage, in1, amt);
	}

	/**
	 * Add two images, returning a new image with the result
	 * 
	 * @param <I> The type of image
	 * 
	 * @param in1 the first image to add
	 * @param in2 the second image to add
	 * @return the event
	 */
	public <I extends Image<?, I>> I add(I in1, I in2) {
		CLQueue queue = context.createDefaultQueue();
		
		CLImage2D clin1 = CLImageConversion.convert(context, in1);
		CLImage2D clin2 = CLImageConversion.convert(context, in2);
		CLImage2D clout = context.createImage2D(CLMem.Usage.Output, clin1.getFormat(), clin1.getWidth(), clin1.getHeight());
		
		CLEvent evt = process(addImage, queue, clin1, clin2, clout);
		
		I out = CLImageConversion.convert(queue, evt, clout, in1.newInstance(in1.getWidth(), in1.getHeight()));
		
		clin1.release();
		clin2.release();
		clout.release();
		queue.release();
		
		return out;
	}
	
	/**
	 * Add a constant to an image, returning a new image with the result
	 * 
	 * @param <I> The type of image
	 * 
	 * @param in1 the first image to add
	 * @param amt the constant
	 * @return the event
	 */
	public <I extends Image<?, I>> I add(I in1, float[] amt) {
		CLQueue queue = context.createDefaultQueue();
		
		CLImage2D clin1 = CLImageConversion.convert(context, in1);
		CLImage2D clout = context.createImage2D(CLMem.Usage.Output, clin1.getFormat(), clin1.getWidth(), clin1.getHeight());
		
		CLEvent evt = process(addImage, queue, clin1, amt, clout);
		
		I out = CLImageConversion.convert(queue, evt, clout, in1.newInstance(in1.getWidth(), in1.getHeight()));
		
		clin1.release();
		clout.release();
		queue.release();
		
		return out;
	}
	
	/**
	 * Subtract two images, storing the result in another image
	 * 
	 * @param queue the command queue
	 * @param in1 the first image to subtract
	 * @param in2 the second image to subtract
	 * @param out the result image
	 * @return the event
	 */
	public CLEvent subtract(CLQueue queue, CLImage2D in1, CLImage2D in2, CLImage2D out) {
		return process(subtractImage, queue, in1, in2, out);
	}

	/**
	 * Subtract a constant from an image, storing the result in another image
	 * 
	 * @param queue the command queue
	 * @param in1 the first image to subtract
	 * @param amt the constant
	 * @param out the result image
	 * @return the event
	 */
	public CLEvent subtract(CLQueue queue, CLImage2D in1, float[] amt, CLImage2D out) {
		return process(subtractConstant, queue, in1, amt, out);
	}

	/**
	 * Subtract two images, returning a new image with the result
	 * 
	 * @param in1 the first image to subtract
	 * @param in2 the second image to subtract
	 * @return the event
	 */
	public CLImage2D subtract(CLImage2D in1, CLImage2D in2) {
		return process(subtractImage, in1, in2);
	}

	/**
	 * Subtract a constant from an image, returning a new image with the result
	 * 
	 * @param in1 the first image to subtract
	 * @param amt the constant
	 * @return the event
	 */
	public CLImage2D subtract(CLImage2D in1, float[] amt) {
		return process(subtractImage, in1, amt);
	}

	/**
	 * Subtract two images, returning a new image with the result
	 * 
	 * @param <I> The type of image
	 * 
	 * @param in1 the first image to subtract
	 * @param in2 the second image to subtract
	 * @return the event
	 */
	public <I extends Image<?, I>> I subtract(I in1, I in2) {
		CLQueue queue = context.createDefaultQueue();
		
		CLImage2D clin1 = CLImageConversion.convert(context, in1);
		CLImage2D clin2 = CLImageConversion.convert(context, in2);
		CLImage2D clout = context.createImage2D(CLMem.Usage.Output, clin1.getFormat(), clin1.getWidth(), clin1.getHeight());
		
		CLEvent evt = process(subtractImage, queue, clin1, clin2, clout);
		
		I out = CLImageConversion.convert(queue, evt, clout, in1.newInstance(in1.getWidth(), in1.getHeight()));
		
		clin1.release();
		clin2.release();
		clout.release();
		queue.release();
		
		return out;
	}

	/**
	 * Subtract a constant from an image, returning a new image with the result
	 * 
	 * @param <I> The type of image
	 * 
	 * @param in1 the first image to subtract
	 * @param amt the constant
	 * @return the event
	 */
	public <I extends Image<?, I>> I subtract(I in1, float[] amt) {
		CLQueue queue = context.createDefaultQueue();
		
		CLImage2D clin1 = CLImageConversion.convert(context, in1);
		CLImage2D clout = context.createImage2D(CLMem.Usage.Output, clin1.getFormat(), clin1.getWidth(), clin1.getHeight());
		
		CLEvent evt = process(subtractImage, queue, clin1, amt, clout);
		
		I out = CLImageConversion.convert(queue, evt, clout, in1.newInstance(in1.getWidth(), in1.getHeight()));
		
		clin1.release();
		clout.release();
		queue.release();
		
		return out;
	}
	
	/**
	 * Multiply two images, storing the result in another image
	 * 
	 * @param queue the command queue
	 * @param in1 the first image to multiply
	 * @param in2 the second image to multiply
	 * @param out the result image
	 * @return the event
	 */
	public CLEvent multiply(CLQueue queue, CLImage2D in1, CLImage2D in2, CLImage2D out) {
		return process(multiplyImage, queue, in1, in2, out);
	}

	/**
	 * Multiply an image by a constant, storing the result in another image
	 * 
	 * @param queue the command queue
	 * @param in1 the first image to multiply
	 * @param amt the constant
	 * @param out the result image
	 * @return the event
	 */
	public CLEvent multiply(CLQueue queue, CLImage2D in1, float[] amt, CLImage2D out) {
		return process(multiplyConstant, queue, in1, amt, out);
	}

	/**
	 * Multiply two images, returning a new image with the result
	 * 
	 * @param in1 the first image to multiply
	 * @param in2 the second image to multiply
	 * @return the event
	 */
	public CLImage2D multiply(CLImage2D in1, CLImage2D in2) {
		return process(multiplyImage, in1, in2);
	}

	/**
	 * Multiply an image by a constant, returning a new image with the result
	 * 
	 * @param in1 the first image to multiply
	 * @param amt the constant
	 * @return the event
	 */
	public CLImage2D multiply(CLImage2D in1, float[] amt) {
		return process(multiplyImage, in1, amt);
	}

	/**
	 * Multiply two images, returning a new image with the result
	 * 
	 * @param <I> The type of image
	 * 
	 * @param in1 the first image to multiply
	 * @param in2 the second image to multiply
	 * @return the event
	 */
	public <I extends Image<?, I>> I multiply(I in1, I in2) {
		CLQueue queue = context.createDefaultQueue();
		
		CLImage2D clin1 = CLImageConversion.convert(context, in1);
		CLImage2D clin2 = CLImageConversion.convert(context, in2);
		CLImage2D clout = context.createImage2D(CLMem.Usage.Output, clin1.getFormat(), clin1.getWidth(), clin1.getHeight());
		
		CLEvent evt = process(multiplyImage, queue, clin1, clin2, clout);
		
		I out = CLImageConversion.convert(queue, evt, clout, in1.newInstance(in1.getWidth(), in1.getHeight()));
		
		clin1.release();
		clin2.release();
		clout.release();
		queue.release();
		
		return out;
	}

	/**
	 * Multiply an image by a constant, returning a new image with the result
	 * 
	 * @param <I> The type of image
	 * 
	 * @param in1 the first image to multiply
	 * @param amt the constant
	 * @return the event
	 */
	public <I extends Image<?, I>> I multiply(I in1, float[] amt) {
		CLQueue queue = context.createDefaultQueue();
		
		CLImage2D clin1 = CLImageConversion.convert(context, in1);
		CLImage2D clout = context.createImage2D(CLMem.Usage.Output, clin1.getFormat(), clin1.getWidth(), clin1.getHeight());
		
		CLEvent evt = process(multiplyImage, queue, clin1, amt, clout);
		
		I out = CLImageConversion.convert(queue, evt, clout, in1.newInstance(in1.getWidth(), in1.getHeight()));
		
		clin1.release();
		clout.release();
		queue.release();
		
		return out;
	}
	
	/**
	 * Divide two images, storing the result in another image
	 * 
	 * @param queue the command queue
	 * @param in1 the first image to divide
	 * @param in2 the second image to divide
	 * @param out the result image
	 * @return the event
	 */
	public CLEvent divide(CLQueue queue, CLImage2D in1, CLImage2D in2, CLImage2D out) {
		return process(divideImage, queue, in1, in2, out);
	}

	/**
	 * Divide an image by a constant, storing the result in another image
	 * 
	 * @param queue the command queue
	 * @param in1 the first image to divide
	 * @param amt the constant
	 * @param out the result image
	 * @return the event
	 */
	public CLEvent divide(CLQueue queue, CLImage2D in1, float[] amt, CLImage2D out) {
		return process(divideConstant, queue, in1, amt, out);
	}

	/**
	 * Divide two images, returning a new image with the result
	 * 
	 * @param in1 the first image to divide
	 * @param in2 the second image to divide
	 * @return the event
	 */
	public CLImage2D divide(CLImage2D in1, CLImage2D in2) {
		return process(divideImage, in1, in2);
	}

	/**
	 * Divide an image by a constant, returning a new image with the result
	 * 
	 * @param in1 the first image to divide
	 * @param amt the constant
	 * @return the event
	 */
	public CLImage2D divide(CLImage2D in1, float[] amt) {
		return process(divideImage, in1, amt);
	}

	/**
	 * Divide two images, returning a new image with the result
	 * 
	 * @param <I> The type of image
	 * 
	 * @param in1 the first image to divide
	 * @param in2 the second image to divide
	 * @return the event
	 */
	public <I extends Image<?, I>> I divide(I in1, I in2) {
		CLQueue queue = context.createDefaultQueue();
		
		CLImage2D clin1 = CLImageConversion.convert(context, in1);
		CLImage2D clin2 = CLImageConversion.convert(context, in2);
		CLImage2D clout = context.createImage2D(CLMem.Usage.Output, clin1.getFormat(), clin1.getWidth(), clin1.getHeight());
		
		CLEvent evt = process(divideImage, queue, clin1, clin2, clout);
		
		I out = CLImageConversion.convert(queue, evt, clout, in1.newInstance(in1.getWidth(), in1.getHeight()));
		
		clin1.release();
		clin2.release();
		clout.release();
		queue.release();
		
		return out;
	}

	/**
	 * Divide an image by a constant, returning a new image with the result
	 * 
	 * @param <I> The type of image
	 * 
	 * @param in1 the first image to divide
	 * @param amt the constant
	 * @return the event
	 */
	public <I extends Image<?, I>> I divide(I in1, float[] amt) {
		CLQueue queue = context.createDefaultQueue();
		
		CLImage2D clin1 = CLImageConversion.convert(context, in1);
		CLImage2D clout = context.createImage2D(CLMem.Usage.Output, clin1.getFormat(), clin1.getWidth(), clin1.getHeight());
		
		CLEvent evt = process(divideImage, queue, clin1, amt, clout);
		
		I out = CLImageConversion.convert(queue, evt, clout, in1.newInstance(in1.getWidth(), in1.getHeight()));
		
		clin1.release();
		clout.release();
		queue.release();
		
		return out;
	}
}
