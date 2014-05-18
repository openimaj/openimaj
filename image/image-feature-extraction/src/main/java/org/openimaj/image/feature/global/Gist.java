package org.openimaj.image.feature.global;

import java.io.File;
import java.io.IOException;

import org.openimaj.feature.FloatFV;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.Image;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.analyser.ImageAnalyser;
import org.openimaj.image.processing.convolution.FourierConvolve;
import org.openimaj.image.processing.convolution.GaborFilters;
import org.openimaj.image.processing.resize.BilinearInterpolation;
import org.openimaj.image.processor.SinglebandImageProcessor;

public abstract class Gist<IMAGE extends Image<?, IMAGE> & SinglebandImageProcessor.Processable<Float, FImage, IMAGE>>
		implements
		ImageAnalyser<IMAGE>
{
	int numberOfBlocks = 4;
	int prefilterFC = 4;
	int boundaryExtension = 32;

	FImage[] gaborFilters;
	private FloatFV response;

	public static class FixedSizeGist<IMAGE extends Image<?, IMAGE> & SinglebandImageProcessor.Processable<Float, FImage, IMAGE>>
			extends
			Gist<IMAGE>
	{
		int imageWidth = 128;
		int imageHeight = 128;

		public FixedSizeGist(int width, int height, int[] orientationsPerScale) {
			this.imageWidth = width;
			this.imageHeight = height;
			this.gaborFilters = GaborFilters.createGaborJets(width, height, orientationsPerScale);
		}

		@Override
		public void analyseImage(IMAGE image) {
			final double sc = Math.max(imageWidth / image.getWidth(), imageHeight / image.getHeight());
			final BilinearInterpolation bil = new BilinearInterpolation(imageWidth, imageHeight, (float) (1f / sc));
			final IMAGE resized = image.process(bil);
			final IMAGE roi = resized.extractCenter(imageWidth, imageHeight);

			extractGist(roi);
		}
	}

	public static class VariableSizeGist<IMAGE extends Image<?, IMAGE> & SinglebandImageProcessor.Processable<Float, FImage, IMAGE>>
			extends
			Gist<IMAGE>
	{
		private int[] orientationsPerScale;

		public VariableSizeGist(int[] orientationsPerScale) {
			this.orientationsPerScale = orientationsPerScale;
		}

		@Override
		public void analyseImage(IMAGE image) {
			if (gaborFilters == null || gaborFilters[0].width != image.getWidth()
					|| gaborFilters[0].height != image.getHeight())
			{
				gaborFilters = GaborFilters.createGaborJets(image.getWidth(), image.getHeight(), orientationsPerScale);
			}

			extractGist(image.clone()); // clone to stop side effects from
										// normalisation further down
		}
	}

	protected void extractGist(IMAGE image) {
		// final int nfeatures = gaborFilters.length * numberOfBlocks *
		// numberOfBlocks;

		if (image instanceof FImage) {
			final FImage o = prefilter(((FImage) image).normalise());
			this.response = gistGabor(o);
		} else if (image instanceof MBFImage) {
			// output = prefilter(((MBFImage) image).normalise());
		} else {
			throw new UnsupportedOperationException("Image type " + image.getClass()
					+ " is not currently supported. Please file a bug report.");
		}

	}

	private IMAGE prefilter(MBFImage multiplyInplace) {
		// TODO Auto-generated method stub
		return null;
	}

	private FloatFV gistGabor(FImage output) {
		// TODO Auto-generated method stub
		return null;
	}

	private FImage prefilter(FImage img) {
		final int w = 5;
		final double s1 = this.prefilterFC / Math.sqrt(Math.log(2));

		for (int y = 0; y < img.height; y++)
			for (int x = 0; x < img.width; x++)
				img.pixels[y][x] = (float) Math.log(1 + img.pixels[y][x] * 255);

		final int sw = img.width + 2 * w;
		final int sh = img.height + 2 * w;
		int n = Math.max(sw, sh);
		n = n + n % 2;
		img = img.paddingSymmetric(w, w, w + n - sw, w + n - sh);

		final FImage filter = new FImage(2 * n, n);
		for (int j = 0; j < n; j++) {
			final int fy = j - n / 2;

			for (int i = 0; i < n * 2; i += 2) {
				final int fx = (i / 2) - n / 2;

				filter.pixels[j][i] = (float) Math.exp(-(fx * fx + fy * fy) / (s1 * s1));
			}
		}
		final FImage output = img.subtractInplace(FourierConvolve.convolvePrepared(img, filter, true));

		final FImage meansq = output.multiply(output);
		final FImage localstd = FourierConvolve.convolvePrepared(meansq, filter, true);

		for (int y = 0; y < localstd.height; y++)
			for (int x = 0; x < localstd.width; x++)
				output.pixels[y][x] = (float) (output.pixels[y][x] / (0.2 + Math.sqrt(Math.abs(localstd.pixels[y][x]))));

		DisplayUtilities.display(output.clone().clip(0F, 1F));
		return output.extractROI(w, w, sw - w - w, sh - w - w);
	}

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		final MBFImage img = ImageUtilities.readMBF(new File("/Users/jsh2/Downloads/gistdescriptor/demo1.jpg"));
		final FImage gimg = img.flatten();

		final FixedSizeGist<FImage> fsg = new FixedSizeGist<FImage>(256, 256, new int[] { 8, 8, 8, 8 });
		fsg.analyseImage(gimg);

	}

}
