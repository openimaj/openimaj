package org.openimaj.image.processing;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.apache.commons.io.IOUtils;
import org.openimaj.image.Image;
import org.openimaj.image.analyser.ImageAnalyser;

import com.nativelibs4java.opencl.CLBuildException;
import com.nativelibs4java.opencl.CLContext;
import com.nativelibs4java.opencl.CLKernel;
import com.nativelibs4java.opencl.CLPlatform.DeviceFeature;
import com.nativelibs4java.opencl.CLProgram;
import com.nativelibs4java.opencl.JavaCL;

/**
 * Base {@link ImageAnalyser} for GPGPU accelerated analysis.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 * @param <I> Type of {@link Image} being processed
 */
public abstract class CLImageAnalyser<I extends Image<?, I>> implements ImageAnalyser<I> {
	protected CLContext context;
	protected CLKernel kernel;

	/**
	 * Construct with the given OpenCL program
	 * @param program the OpenCL program
	 */
	public CLImageAnalyser(CLProgram program) {
		try {
			this.context = JavaCL.createBestContext(DeviceFeature.GPU);
			this.kernel = program.createKernels()[0];
		} catch (CLBuildException e) {
			//fallback to OpenCL on the CPU
			this.context = JavaCL.createBestContext(DeviceFeature.CPU);
			this.kernel = program.createKernels()[0];			
		}
	}

	/**
	 * Construct with the given OpenCL program source, given in
	 * the form of {@link String}s.
	 * 
	 * @param programSrcs the source of the program
	 */
	public CLImageAnalyser(String... programSrcs) {
		CLProgram program;
		try {
			this.context = JavaCL.createBestContext(DeviceFeature.GPU);
			program = context.createProgram(programSrcs);
			this.kernel = program.createKernels()[0];
		} catch (CLBuildException e) {
			//fallback to OpenCL on the CPU
			this.context = JavaCL.createBestContext(DeviceFeature.CPU);
			program = context.createProgram(programSrcs);
			this.kernel = program.createKernels()[0];
		}
	}

	/**
	 * Construct with the program sourcecode at the given URL. 
	 * @param srcUrl the url
	 * @throws IOException
	 */
	public CLImageAnalyser(URL srcUrl) throws IOException {
		this(IOUtils.toString(srcUrl));
	}

	/**
	 * Construct by reading the program source from a stream
	 * @param src the source stream
	 * @throws IOException
	 */
	public CLImageAnalyser(InputStream src) throws IOException {
		this(IOUtils.toString(src));
	}

	/**
	 * Construct with the given OpenCL program
	 * @param context the OpenCL context to use
	 * @param program the OpenCL program
	 */
	public CLImageAnalyser(CLContext context, CLProgram program) {
		this.context = context;
		this.kernel = program.createKernels()[0];
	}

	/**
	 * Construct with the given OpenCL program source, given in
	 * the form of {@link String}s.
	 * @param context the OpenCL context to use
	 * 
	 * @param programSrcs the source of the program
	 */
	public CLImageAnalyser(CLContext context, String... programSrcs) {
		this.context = context;
		CLProgram program = context.createProgram(programSrcs);
		this.kernel = program.createKernels()[0];
	}
	
	/**
	 * Construct with the given OpenCL kernel
	 * @param kernel the OpenCL kernel to use
	 */
	public CLImageAnalyser(CLKernel kernel) {
		this.context = kernel.getProgram().getContext();
		this.kernel = kernel;
	}

	/**
	 * Construct with the program sourcecode at the given URL. 
	 * @param context the OpenCL context to use
	 * @param srcUrl the url
	 * @throws IOException
	 */
	public CLImageAnalyser(CLContext context, URL srcUrl) throws IOException {
		this(context, IOUtils.toString(srcUrl));
	}

	/**
	 * Construct by reading the program source from a stream
	 * @param context the OpenCL context to use
	 * @param src the source stream
	 * @throws IOException
	 */
	public CLImageAnalyser(CLContext context, InputStream src) throws IOException {
		this(context, IOUtils.toString(src));
	}
}
