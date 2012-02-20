package org.openimaj.image.processing.convolution;

import org.openimaj.image.FImage;
import org.openimaj.image.processing.algorithm.FourierTransform;
import org.openimaj.image.processor.SinglebandImageProcessor;

import edu.emory.mathcs.jtransforms.fft.FloatFFT_2D;

/**
 * {@link FImage} convolution performed in the fourier domain.
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 *
 */
public class FourierConvolve implements SinglebandImageProcessor<Float, FImage> {
	private float [][] kernel;

	/**
	 * Construct the convolution operator with the given kernel
	 * @param kernel the kernel
	 */
	public FourierConvolve(float [][] kernel) {
		this.kernel = kernel;
	}
	
	/**
	 * Construct the convolution operator with the given kernel
	 * @param kernel the kernel
	 */
	public FourierConvolve(FImage kernel) {
		this.kernel = kernel.pixels;
	}
	
	@Override
	public void processImage(FImage image) {
		convolve(image, kernel, true);
	}

	/**
	 * Convolve an image with a kernel using an FFT.
	 * @param image The image to convolve
	 * @param kernel The kernel
	 * @param inline if true, then output overwrites the input, otherwise a new image is created.
	 * @return convolved image
	 */
	public static FImage convolve(FImage image, float[][] kernel, boolean inline) {
		int cols = image.getCols();
		int rows = image.getRows();

		FloatFFT_2D fft = new FloatFFT_2D(rows,cols);

		float[][] preparedImage = FourierTransform.prepareData(image.pixels, rows, cols, false);
		fft.complexForward(preparedImage);

		float[][] preparedKernel = FourierTransform.prepareData(kernel, rows, cols, false);
		fft.complexForward(preparedKernel);

		for(int y = 0; y < rows; y++){
			for(int x = 0; x < cols; x++){
				float reImage = preparedImage[y][x*2];
				float imImage = preparedImage[y][1 + x*2];

				float reKernel = preparedKernel[y][x*2];
				float imKernel = preparedKernel[y][1 + x*2];

				float re = reImage * reKernel - imImage * imKernel;
				float im = reImage * imKernel + imImage * reKernel;

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
