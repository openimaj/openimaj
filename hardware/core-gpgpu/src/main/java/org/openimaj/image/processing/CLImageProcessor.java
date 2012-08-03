package org.openimaj.image.processing;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import org.apache.commons.io.IOUtils;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.Image;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.ColourSpace;
import org.openimaj.image.processor.ImageProcessor;

import com.nativelibs4java.opencl.CLContext;
import com.nativelibs4java.opencl.CLEvent;
import com.nativelibs4java.opencl.CLImage2D;
import com.nativelibs4java.opencl.CLImageFormat;
import com.nativelibs4java.opencl.CLImageFormat.ChannelDataType;
import com.nativelibs4java.opencl.CLImageFormat.ChannelOrder;
import com.nativelibs4java.opencl.CLKernel;
import com.nativelibs4java.opencl.CLMem;
import com.nativelibs4java.opencl.CLMem.MapFlags;
import com.nativelibs4java.opencl.CLProgram;
import com.nativelibs4java.opencl.CLQueue;
import com.nativelibs4java.opencl.JavaCL;

public class CLImageProcessor<I extends Image<?, I>> implements ImageProcessor<I> {
	protected CLContext context;
	protected CLProgram program;
	protected CLQueue queue;
	protected CLKernel kernel;

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

	FloatBuffer convertToRGBA(MBFImage image) {
		int width = image.getWidth();
		int height = image.getHeight();
		
		ByteBuffer bb = ByteBuffer.allocateDirect(width * height * 4 * 4);
		bb.order(ByteOrder.LITTLE_ENDIAN);
		FloatBuffer fb = bb.asFloatBuffer();
		
		if (image.numBands() != 3) throw new RuntimeException();
		
		FImage[] bands = image.bands.toArray(new FImage[3]);
		
		for (int y=0; y<height; y++) {
			for (int x=0; x<width; x++) {
				for (int b=0; b<3; b++) { 
					fb.put(bands[b].pixels[y][x]);
				}
				fb.put(1f);
			}
		}
		
		return fb;
	}
	
	MBFImage convertFromRGBA(FloatBuffer fb, int width, int height, MBFImage image) {
		if (image == null)
			image = new MBFImage(width, height, ColourSpace.RGBA);
		
		FImage[] bands = image.bands.toArray(new FImage[image.numBands()]);
		
		for (int y=0; y<height; y++) {
			for (int x=0; x<width; x++) {
				for (int b=0; b<3; b++) { 
					bands[b].pixels[y][x] = fb.get();
				}
				fb.get();
			}
		}
		
		return image;
	}
	
	CLImage2D convert(MBFImage image) {
		CLImageFormat format = new CLImageFormat(ChannelOrder.RGBA, ChannelDataType.Float);
		
//		CLImage2D im = context.createImage2D(CLMem.Usage.InputOutput, format, image.getWidth(), image.getHeight());
//		
//		FloatBuffer buffer = im.map(queue, MapFlags.Write, null).asFloatBuffer();
//		System.out.println(buffer.array());
//		
//		Buffer bfr = FloatBuffer.allocate(image.getWidth() * image.getHeight() * 4);
//		im.write(queue, 0, 0, image.getWidth(), image.getHeight(), image.getWidth(), bfr , true, null);
		
		return context.createImage2D(CLMem.Usage.InputOutput, format, image.getWidth(), image.getHeight(), 4*image.getWidth()*4, convertToRGBA(image), false);
	}
	
	MBFImage convert(CLImage2D image, MBFImage mbf) {
		int width = (int) image.getWidth();
		int height = (int) image.getHeight();
		ByteBuffer bb = ByteBuffer.allocateDirect(width * height * 4 * 4);
		bb.order(ByteOrder.LITTLE_ENDIAN);
		FloatBuffer fb = bb.asFloatBuffer();
		
		image.read(queue, 0, 0, width, height, image.getRowPitch(), fb, true, (CLEvent)null);
		
		return convertFromRGBA(fb, width, height, mbf);
	}
	
	@Override
	public void processImage(I image) {
		int width = image.getWidth();
		int height = image.getHeight();
		
		CLImage2D imageIn = convert((MBFImage) image);

		CLImage2D imageOut = context.createImage2D(CLMem.Usage.Output, imageIn.getFormat(), width, height);
		kernel.setArgs(imageIn, imageOut);
		CLEvent evt = kernel.enqueueNDRange(queue, new int[] { width, height });

		convert(imageOut, (MBFImage) image);
	}
}
