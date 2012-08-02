package org.openimaj.image.processing;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.apache.commons.io.IOUtils;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.Image;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.processor.ImageProcessor;

import com.nativelibs4java.opencl.CLContext;
import com.nativelibs4java.opencl.CLEvent;
import com.nativelibs4java.opencl.CLImage2D;
import com.nativelibs4java.opencl.CLKernel;
import com.nativelibs4java.opencl.CLMem;
import com.nativelibs4java.opencl.CLProgram;
import com.nativelibs4java.opencl.CLQueue;
import com.nativelibs4java.opencl.JavaCL;

public class CLImageProcessor<I extends Image<?, I>> implements ImageProcessor<I> {
	private CLContext context;
	private CLProgram program;
	private CLQueue queue;
	private CLKernel kernel;

	public CLImageProcessor(CLProgram program) {
		this.context = JavaCL.createBestContext();
		this.program = program;
		this.queue = context.createDefaultQueue();
		this.kernel = program.createKernels()[0];
	}
	
	public CLImageProcessor(String... programSrcs) {
		this.context = JavaCL.createBestContext();
		this.program = context.createProgram(programSrcs);
		this.queue = context.createDefaultQueue();
		this.kernel = program.createKernels()[0];
	}
	
	public CLImageProcessor(URL srcUrl) throws IOException {
		this(IOUtils.toString(srcUrl));
	}
	
	public CLImageProcessor(InputStream src) throws IOException {
		this(IOUtils.toString(src));
	}
	
	@Override
	public void processImage(I image) {
		BufferedImage bufferedImage = ImageUtilities.createBufferedImage(image);

		int width = bufferedImage.getWidth();
		int height = bufferedImage.getHeight();
		
		CLImage2D imageIn = context.createImage2D(CLMem.Usage.Input, bufferedImage, false);
		CLImage2D imageOut = context.createImage2D(CLMem.Usage.Output, imageIn.getFormat(), width, height);

		
		
		kernel.setArgs(imageIn, imageOut);
		CLEvent evt = kernel.enqueueNDRange(queue, new int[] { width, height });

		BufferedImage result = imageOut.read(queue, evt);

		ImageUtilities.assignBufferedImage(result, image);
	}
	
	public static void main(String[] args) throws Exception {
		MBFImage test = ImageUtilities.readMBF(new File("/Users/jsh2/Desktop/OpenIMAJPics/screencaps.png"));
		
		MBFImage out = test.process(new CLImageProcessor<MBFImage>(CLImageProcessor.class.getResource("Convolve.cl")));
		
		DisplayUtilities.display(test);
		DisplayUtilities.display(out);
	}
}
