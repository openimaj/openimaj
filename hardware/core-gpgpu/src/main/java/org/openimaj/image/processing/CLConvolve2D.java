package org.openimaj.image.processing;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.Image;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;

import com.nativelibs4java.opencl.CLBuffer;
import com.nativelibs4java.opencl.CLMem.Usage;

public class CLConvolve2D<I extends Image<?, I>> extends CLImageProcessor<I> {
	public CLConvolve2D(float[][] kernel) throws IOException {
		super(CLConvolve2D.class.getResource("Convolve.cl"));
		
		ByteBuffer bb = ByteBuffer.allocateDirect(kernel.length * kernel.length * 4 * 4);
		bb.order(ByteOrder.LITTLE_ENDIAN);
		FloatBuffer fb = bb.asFloatBuffer();
		
		for (int y=0; y<kernel.length; y++)
			for (int x=0; x<kernel.length; x++)
				fb.put(kernel[y][x]);
		
		CLBuffer<Float> floatData = context.createFloatBuffer(Usage.Input, fb, false);
		
		this.kernel.setArg(2, floatData);
		this.kernel.setArg(3, kernel.length);
	}
	
	public static void main(String[] args) throws Exception {
		MBFImage test = ImageUtilities.readMBF(new File("/Users/jsh2/Desktop/test.jpg"));
		
		float[][] kernel = {
				{ 0,-1, 0},
				{-1, 4,-1},
				{ 0,-1, 0}};
		
		MBFImage out = test.process(new CLConvolve2D<MBFImage>(kernel));
		
		DisplayUtilities.display(test);
		DisplayUtilities.display(out);
	}
}
