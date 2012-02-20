package org.openimaj.image.processing.algorithm;

import org.openimaj.image.FImage;
import org.openimaj.image.processor.SinglebandImageProcessor;

import edu.emory.mathcs.jtransforms.fft.FloatFFT_2D;

/**
 * {@link FImage} correlation performed using an FFT.
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 *
 */
public class FourierCorrelation implements SinglebandImageProcessor<Float, FImage> {
	private FImage template;
	
	/**
	 * Construct the correlation operator with the given template
	 * @param template the template
	 */
	public FourierCorrelation(FImage template) {
		this.template = template;
	}
	
	@Override
	public void processImage(FImage image) {
		correlate(image, template, true);
	}

	/**
	 * correlate an image with a kernel using an FFT.
	 * @param image The image 
	 * @param template The template to correlate with the image
	 * @param inline if true, then output overwrites the input, otherwise a new image is created.
	 * @return correlation map
	 */
	public static FImage correlate(FImage image, FImage template, boolean inline) {
		int cols = image.getCols();
		int rows = image.getRows();

		FloatFFT_2D fft = new FloatFFT_2D(rows,cols);

		float[][] preparedImage = FourierTransform.prepareData(image.pixels, rows, cols, false);
		fft.complexForward(preparedImage);

		float[][] preparedKernel = FourierTransform.prepareData(template.pixels, rows, cols, false);
		fft.complexForward(preparedKernel);

		for(int y = 0; y < rows; y++){
			for(int x = 0; x < cols; x++){
				float reImage = preparedImage[y][x*2];
				float imImage = preparedImage[y][1 + x*2];

				float reKernel = preparedKernel[y][x*2];
				float imKernelConj = -1 * preparedKernel[y][1 + x*2];

				float re = reImage * reKernel - imImage * imKernelConj;
				float im = reImage * imKernelConj + imImage * reKernel;

				preparedImage[y][x*2] = re;
				preparedImage[y][1 + x*2] = im;
			}
		}

		fft.complexInverse(preparedImage, true);

		FImage out = image;
		if (!inline) 
			out = new FImage(cols, rows);

		FourierTransform.unprepareData(preparedImage, out, false);
		
		return out;
	}
}
