package org.openimaj.image.processing.convolution;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.Image;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.processing.CLImageProcessor;

import com.nativelibs4java.opencl.CLBuffer;
import com.nativelibs4java.opencl.CLContext;
import com.nativelibs4java.opencl.CLMem.Usage;
import com.nativelibs4java.opencl.CLPlatform.DeviceFeature;
import com.nativelibs4java.opencl.JavaCL;

/**
 * Simple 2D convolution
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 * @param <I>
 */
public class CLConvolve2D<I extends Image<?, I>> extends CLImageProcessor<I> {
	private CLBuffer<Float> floatData;

	public CLConvolve2D(CLContext context, float[][] kernel) throws IOException {
		super(context, CLConvolve2D.class.getResource("Convolve.cl"));
		
		ByteBuffer bb = ByteBuffer.allocateDirect(kernel.length * kernel.length * 4);
		bb.order(context.getByteOrder());
		FloatBuffer fb = bb.asFloatBuffer();
		
		for (int y=0; y<kernel.length; y++)
			for (int x=0; x<kernel.length; x++)
				fb.put(kernel[y][x]);
		
		//Note: the buffer needs to be an ivar as it seems to
		//get garbage collected (the setArg line below internally
		//just grabs the native pointer to the buffer; java has no
		//way of knowing that we still need the data...)
		floatData = context.createFloatBuffer(Usage.Input, fb, true);
		
		this.kernel.setArg(2, floatData);
		this.kernel.setArg(3, kernel.length);
	}
	
	public static void main(String[] args) throws Exception {
		MBFImage test = ImageUtilities.readMBF(CLImageProcessor.class.getResourceAsStream("/org/openimaj/image/data/cat.jpg"));
				
		float[][] kernel = {{1f/9f, 1f/9f, 1f/9f},{1f/9f, 1f/9f, 1f/9f},{1f/9f, 1f/9f, 1f/9f}};
		
		CLContext context = JavaCL.createBestContext(DeviceFeature.GPU);
		
		DescriptiveStatistics ds = new DescriptiveStatistics();
		
		CLConvolve2D<MBFImage> convolve = new CLConvolve2D<MBFImage>(context, kernel);
		MBFImage img = null;
		for (int i=0; i<1000; i++) {
			long t1 = System.currentTimeMillis();
			img = test.processInplace(convolve);
			
			long t2 = System.currentTimeMillis();
			
			ds.addValue(t2-t1);
		}
		
		DisplayUtilities.display(img);
		
		System.out.println(ds);
	}
}
