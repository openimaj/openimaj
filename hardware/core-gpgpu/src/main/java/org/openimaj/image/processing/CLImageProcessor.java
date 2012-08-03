package org.openimaj.image.processing;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.apache.commons.io.IOUtils;
import org.openimaj.image.CLImageConversion;
import org.openimaj.image.Image;
import org.openimaj.image.processor.ImageProcessor;

import com.nativelibs4java.opencl.CLContext;
import com.nativelibs4java.opencl.CLImage2D;
import com.nativelibs4java.opencl.CLKernel;
import com.nativelibs4java.opencl.CLMem;
import com.nativelibs4java.opencl.CLProgram;
import com.nativelibs4java.opencl.CLQueue;
import com.nativelibs4java.opencl.JavaCL;

/**
 * Base {@link ImageProcessor} for GPGPU accelerated processing.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 * @param <I> Type of {@link Image} being processed
 */
public class CLImageProcessor<I extends Image<?, I>> implements ImageProcessor<I> {
	protected CLContext context;
	protected CLProgram program;
	protected CLQueue queue;
	protected CLKernel kernel;

	/**
	 * Construct with the given OpenCL program
	 * @param program the OpenCL program
	 */
	public CLImageProcessor(CLProgram program) {
		this.context = JavaCL.createBestContext();
		this.program = program;
		this.queue = context.createDefaultQueue();
		this.kernel = program.createKernels()[0];
	}
	
	/**
	 * Construct with the given OpenCL program source, given in
	 * the form of {@link String}s.
	 * 
	 * @param programSrcs the source of the program
	 */
	public CLImageProcessor(String... programSrcs) {
		this.context = JavaCL.createBestContext();
		this.program = context.createProgram(programSrcs);
		this.queue = context.createDefaultQueue();
		this.kernel = program.createKernels()[0];
	}
	
	/**
	 * Construct with the program sourcecode at the given URL. 
	 * @param srcUrl the url
	 * @throws IOException
	 */
	public CLImageProcessor(URL srcUrl) throws IOException {
		this(IOUtils.toString(srcUrl));
	}
	
	/**
	 * Construct by reading the program source from a stream
	 * @param src the source stream
	 * @throws IOException
	 */
	public CLImageProcessor(InputStream src) throws IOException {
		this(IOUtils.toString(src));
	}
	
	@Override
	public void processImage(I image) {
		int width = image.getWidth();
		int height = image.getHeight();
		
		CLImage2D imageIn = CLImageConversion.convert(context, image);
		CLImage2D imageOut = context.createImage2D(CLMem.Usage.Output, imageIn.getFormat(), width, height);
		
		kernel.setArgs(imageIn, imageOut);
		kernel.enqueueNDRange(queue, new int[] { width, height });

		CLImageConversion.convert(queue, imageOut, image);
	}
}
