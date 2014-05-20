/**
 * Copyright (c) 2011, The University of Southampton and the individual contributors.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *   * 	Redistributions of source code must retain the above copyright notice,
 * 	this list of conditions and the following disclaimer.
 *
 *   *	Redistributions in binary form must reproduce the above copyright notice,
 * 	this list of conditions and the following disclaimer in the documentation
 * 	and/or other materials provided with the distribution.
 *
 *   *	Neither the name of the University of Southampton nor the names of its
 * 	contributors may be used to endorse or promote products derived from this
 * 	software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.openimaj.image.feature.global;

import java.io.File;
import java.io.IOException;

import org.openimaj.citation.annotation.Reference;
import org.openimaj.citation.annotation.ReferenceType;
import org.openimaj.feature.FloatFV;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.Image;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.analyser.ImageAnalyser;
import org.openimaj.image.colour.ColourMap;
import org.openimaj.image.colour.ColourSpace;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.processing.algorithm.FourierTransform;
import org.openimaj.image.processing.convolution.FourierConvolve;
import org.openimaj.image.processing.convolution.GaborFilters;
import org.openimaj.image.processing.resize.BilinearInterpolation;
import org.openimaj.image.processing.resize.ResizeProcessor;
import org.openimaj.image.processor.SinglebandImageProcessor;

import edu.emory.mathcs.jtransforms.fft.FloatFFT_2D;

/**
 * Implementation of the "Gist" spatial envolope feature. Based on the original
 * matlab implementation from
 * {@link "http://people.csail.mit.edu/torralba/code/spatialenvelope/"}, and
 * designed to produce comparable features.
 * <p>
 * The Gist or Spatial Envelope is a very low dimensional representation of the
 * scene. Gist encodes a set of perceptual dimensions (naturalness, openness,
 * roughness, expansion, ruggedness) that represents the dominant spatial
 * structure of a scene. These perceptual dimensions are reliably estimated
 * using coarsely localized spectral information from the image.
 * <p>
 * <b>Implementation notes:<b> This class is abstract, and it is intended that
 * the {@link FixedSizeGist} or {@link VariableSizeGist} are actually used in
 * practice (normally {@link FixedSizeGist} will be used).
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 * @param <IMAGE>
 *            The type of image. {@link MBFImage} and {@link FImage} are
 *            supported; other types might not work.
 */
@Reference(
		type = ReferenceType.Article,
		author = { "Oliva, Aude", "Torralba, Antonio" },
		title = "Modeling the Shape of the Scene: A Holistic Representation of the Spatial Envelope",
		year = "2001",
		journal = "Int. J. Comput. Vision",
		pages = { "145", "", "175" },
		url = "http://dx.doi.org/10.1023/A:1011139631724",
		month = "May",
		number = "3",
		publisher = "Kluwer Academic Publishers",
		volume = "42",
		customData = {
				"issn", "0920-5691",
				"numpages", "31",
				"doi", "10.1023/A:1011139631724",
				"acmid", "598462",
				"address", "Hingham, MA, USA",
				"keywords", "energy spectrum, natural images, principal components, scene recognition, spatial layout"
		})
public abstract class Gist<IMAGE extends Image<?, IMAGE> & SinglebandImageProcessor.Processable<Float, FImage, IMAGE>>
		implements
		ImageAnalyser<IMAGE>
{
	/**
	 * The default number of filter orientations per scale
	 */
	public static final int[] DEFAULT_ORIENTATIONS_PER_SCALE = { 8, 8, 8, 8 };

	/**
	 * The default number spatial blocks
	 */
	public static final int DEFAULT_NUMBER_OF_BLOCKS = 4;

	/**
	 * The default number of cycles per image for the pre-filter Gaussian
	 */
	public static final int DEFAULT_PREFILTER_FC = 4;

	/**
	 * The default amount of padding to apply before convolving with the Gabor
	 * filters
	 */
	public static final int DEFAULT_BOUNDARY_EXTENSION = 32;

	/**
	 * The number of blocks in each direction
	 */
	public int numberOfBlocks = DEFAULT_NUMBER_OF_BLOCKS;

	/**
	 * The number of cycles per image for the pre-filter Gaussian
	 */
	public int prefilterFC = DEFAULT_PREFILTER_FC;

	/**
	 * The amount of padding to add before convolving with the Gabor functions
	 */
	public int boundaryExtension = DEFAULT_BOUNDARY_EXTENSION;

	protected FImage[] gaborFilters;
	protected FloatFV response;
	protected int[] orientationsPerScale;

	public static class FixedSizeGist<IMAGE extends Image<?, IMAGE> & SinglebandImageProcessor.Processable<Float, FImage, IMAGE>>
			extends
			Gist<IMAGE>
	{
		/**
		 * Default image size (both height and width)
		 */
		public static final int DEFAULT_SIZE = 128;

		protected int imageWidth;
		protected int imageHeight;

		public FixedSizeGist() {
			this(DEFAULT_SIZE, DEFAULT_SIZE, DEFAULT_ORIENTATIONS_PER_SCALE);
		}

		public FixedSizeGist(int width, int height) {
			this(width, height, DEFAULT_ORIENTATIONS_PER_SCALE);
		}

		public FixedSizeGist(int width, int height, int[] orientationsPerScale) {
			super(orientationsPerScale);
			this.imageWidth = width;
			this.imageHeight = height;
			this.gaborFilters = GaborFilters.createGaborJets(width + 2 * this.boundaryExtension, height + 2
					* this.boundaryExtension, orientationsPerScale);
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
		public VariableSizeGist() {
			super(DEFAULT_ORIENTATIONS_PER_SCALE);
		}

		public VariableSizeGist(int[] orientationsPerScale) {
			super(orientationsPerScale);
		}

		@Override
		public void analyseImage(IMAGE image) {
			if (gaborFilters == null || gaborFilters[0].width != image.getWidth()
					|| gaborFilters[0].height != image.getHeight())
			{
				gaborFilters = GaborFilters.createGaborJets(image.getWidth() + 2 * this.boundaryExtension,
						image.getHeight() + 2 * this.boundaryExtension, orientationsPerScale);
			}

			extractGist(image.clone()); // clone to stop side effects from
										// normalisation further down
		}
	}

	protected Gist(int[] orientationsPerScale) {
		this.orientationsPerScale = orientationsPerScale;
	}

	protected void extractGist(IMAGE image) {
		MBFImage mbfimage;
		if (image instanceof FImage) {
			mbfimage = new MBFImage((FImage) image);
		} else if (image instanceof MBFImage) {
			mbfimage = (MBFImage) image;
		} else {
			throw new UnsupportedOperationException("Image type " + image.getClass()
					+ " is not currently supported. Please file a bug report.");
		}

		final MBFImage o = prefilter(mbfimage.normalise());
		this.response = gistGabor(o);
	}

	private MBFImage prefilter(MBFImage img) {
		final int w = 5;
		final double s1 = this.prefilterFC / Math.sqrt(Math.log(2));

		final int sw = img.getWidth() + 2 * w;
		final int sh = img.getHeight() + 2 * w;
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

		final MBFImage output = new MBFImage();
		for (int b = 0; b < img.numBands(); b++) {
			final FImage band = img.getBand(b);
			for (int y = 0; y < band.height; y++) {
				for (int x = 0; x < band.width; x++) {
					band.pixels[y][x] = (float) Math.log(1 + band.pixels[y][x] * 255);
				}
			}

			output.bands.add(band.subtractInplace(FourierConvolve.convolvePrepared(band, filter, true)));
		}
		final FImage mean = output.flatten();
		final FImage meansq = mean.multiply(mean);
		final FImage localstd = FourierConvolve.convolvePrepared(meansq, filter, true);

		for (int b = 0; b < img.numBands(); b++) {
			final FImage band = output.getBand(b);
			for (int y = 0; y < localstd.height; y++)
				for (int x = 0; x < localstd.width; x++)
					band.pixels[y][x] = (float) (band.pixels[y][x] / (0.2 + Math
							.sqrt(Math.abs(localstd.pixels[y][x]))));
		}

		return output.extractROI(w, w, sw - w - w, sh - w - w);
	}

	private FloatFV gistGabor(MBFImage img) {
		final int blocksPerFilter = computeNumberOfSamplingBlocks();
		final int nFeaturesPerBand = gaborFilters.length * blocksPerFilter;
		final int nFilters = this.gaborFilters.length;

		// pad the image
		img = img.paddingSymmetric(boundaryExtension, boundaryExtension, boundaryExtension, boundaryExtension);

		final int cols = img.getCols();
		final int rows = img.getRows();
		final FloatFFT_2D fft = new FloatFFT_2D(rows, cols);

		final float[][] workingSpace = new float[rows][cols * 2];
		final FloatFV fv = new FloatFV(nFeaturesPerBand * img.numBands());

		for (int b = 0; b < img.numBands(); b++) {
			final FImage band = img.bands.get(b);

			final float[][] preparedImage =
					FourierTransform.prepareData(band.pixels, rows, cols, true);
			fft.complexForward(preparedImage);

			for (int i = 0; i < nFilters; i++) {
				// convolve with the filter
				FImage ig = performConv(fft, preparedImage, workingSpace, this.gaborFilters[i], rows, cols);

				// remove padding
				ig = ig.extractROI(boundaryExtension, boundaryExtension, band.width - 2 * boundaryExtension, band.height
						- 2
						* boundaryExtension);

				sampleResponses(ig, fv.values, b * nFeaturesPerBand + i * blocksPerFilter);
			}
		}

		return fv;
	}

	/**
	 * Compute the number of sampling blocks that are used for every filter. The
	 * default implementation returns {@link #numberOfBlocks}*
	 * {@link #numberOfBlocks}, but can be overridden in combination with
	 * {@link #sampleResponses(FImage, float[], int)} in a subclass to support
	 * different spatial sampling strategies.
	 * 
	 * @return the number of sampling blocks per filter.
	 */
	protected int computeNumberOfSamplingBlocks() {
		return numberOfBlocks * numberOfBlocks;
	}

	/**
	 * Sample the average response from each of the blocks in the image and
	 * insert into the vector. This method could be overridden to support
	 * different spatial aggregation strategies (in which case
	 * {@link #computeNumberOfSamplingBlocks()} should also be overridden).
	 * 
	 * @param image
	 *            the image to sample
	 * @param v
	 *            the vector to write into
	 * @param offset
	 *            the offset from which to sample
	 */
	protected void sampleResponses(FImage image, float[] v, int offset) {
		final int gridWidth = image.width / this.numberOfBlocks;
		final int gridHeight = image.height / this.numberOfBlocks;

		for (int iy = 0; iy < this.numberOfBlocks; iy++) {
			final int starty = gridHeight * iy;
			final int stopy = Math.min(starty + gridHeight, image.height);

			for (int ix = 0; ix < this.numberOfBlocks; ix++) {
				final int startx = gridWidth * ix;
				final int stopx = Math.min(startx + gridWidth, image.width);

				float avg = 0;
				for (int y = starty; y < stopy; y++) {
					for (int x = startx; x < stopx; x++) {
						avg += image.pixels[y][x];
					}
				}
				avg /= ((stopx - startx) * (stopy - starty));

				// note y and x transposed to conform to the matlab
				// implementation
				v[offset + iy + ix * this.numberOfBlocks] = avg;
			}
		}
	}

	/*
	 * Perform convolution in the frequency domain an reconstruct the resultant
	 * image as the magnitudes of the complex components from the ifft.
	 */
	private FImage performConv(FloatFFT_2D fft, float[][] preparedImage, float[][] workingSpace, FImage filterfft,
			int rows, int cols)
	{
		final float[][] preparedKernel = filterfft.pixels;

		for (int y = 0; y < rows; y++) {
			for (int x = 0; x < cols; x++) {
				final float reImage = preparedImage[y][x * 2];
				final float imImage = preparedImage[y][1 + x * 2];

				final float reKernel = preparedKernel[y][x * 2];
				final float imKernel = preparedKernel[y][1 + x * 2];

				final float re = reImage * reKernel - imImage * imKernel;
				final float im = reImage * imKernel + imImage * reKernel;

				workingSpace[y][x * 2] = re;
				workingSpace[y][1 + x * 2] = im;
			}
		}

		fft.complexInverse(workingSpace, true);

		final FImage out = new FImage(cols, rows);
		for (int r = 0; r < rows; r++) {
			for (int c = 0; c < cols; c++) {
				out.pixels[r][c] = (float) Math.sqrt(workingSpace[r][c * 2] * workingSpace[r][c * 2]
						+ workingSpace[r][1 + c * 2]
						* workingSpace[r][1 + c * 2]);
			}
		}
		return out;
	}

	/**
	 * Compute the descriptor visualisation in the same form as the original
	 * matlab code. The resultant image illustrates the dominant filter
	 * responses for each spatial bin. The filter scales are drawn with
	 * differing hues, and brightness is proportional to response value.
	 * 
	 * @param width
	 *            the desired width of the produced image
	 * @return the visualisation TODO: colour descriptors
	 */
	public MBFImage visualiseDescriptor(int width) {
		final Float[][] C = ColourMap.HSV.generateColours(orientationsPerScale.length);

		final FImage[] G = new FImage[this.gaborFilters.length];
		for (int i = 0; i < gaborFilters.length; i++) {
			G[i] = new FImage(this.gaborFilters[i].width / 2, this.gaborFilters[i].height);
			FourierTransform.unprepareData(gaborFilters[i].pixels, G[i], false);
			G[i] = ResizeProcessor.halfSize(G[i]);
			G[i].addInplace(G[i].clone().flipY().flipX());
		}

		float max = 0;
		final MBFImage[] blockImages = new MBFImage[numberOfBlocks * numberOfBlocks];
		for (int y = 0, k = 0; y < numberOfBlocks; y++) {
			for (int x = 0; x < numberOfBlocks; x++, k++) {
				blockImages[k] = new MBFImage(G[0].width, G[0].height, 3);

				for (int s = 0, j = 0; s < orientationsPerScale.length; s++) {
					for (int i = 0; i < orientationsPerScale[s]; i++, j++) {
						final MBFImage col = new MBFImage(G[0].width, G[0].height, 3);
						col.fill(C[s]).multiplyInplace(
								this.response.values[y + x * numberOfBlocks + j * numberOfBlocks * numberOfBlocks]);

						blockImages[k].addInplace(G[j].toRGB().multiply(col));
					}
				}

				for (int i = 0; i < blockImages[k].numBands(); i++)
					max = Math.max(max, blockImages[k].bands.get(i).max());
			}
		}

		final MBFImage output = new MBFImage(width, width, ColourSpace.RGB);
		output.fill(RGBColour.WHITE);
		final int ts = (width / 4);
		final ResizeProcessor rp = new ResizeProcessor(ts, ts, true);
		for (int y = 0, k = 0; y < numberOfBlocks; y++) {
			for (int x = 0; x < numberOfBlocks; x++, k++) {
				blockImages[k].divideInplace(max);
				final MBFImage tmp = blockImages[k].process(rp);
				tmp.drawLine(0, 0, 0, ts - 1, 1, RGBColour.WHITE);
				tmp.drawLine(0, 0, ts - 1, 0, 1, RGBColour.WHITE);
				tmp.drawLine(ts - 1, 0, ts - 1, ts - 1, 1, RGBColour.WHITE);
				tmp.drawLine(0, ts - 1, ts - 1, ts - 1, 1, RGBColour.WHITE);
				output.drawImage(tmp, x * ts, y * ts);
			}
		}

		return output;
	}

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		final MBFImage img = ImageUtilities.readMBF(new File("/Users/jon/Downloads/gistdescriptor/demo1.jpg"));
		final FImage gimg = img.flatten();

		final FixedSizeGist<FImage> fsg = new FixedSizeGist<FImage>(256, 256, new int[] { 8, 8, 8, 8 });
		fsg.analyseImage(gimg);
		System.out.println(fsg.response);
		DisplayUtilities.display(fsg.visualiseDescriptor(500));
	}
}
