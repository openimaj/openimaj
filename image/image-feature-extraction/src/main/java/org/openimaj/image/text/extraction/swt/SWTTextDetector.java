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
package org.openimaj.image.text.extraction.swt;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.openimaj.citation.annotation.Reference;
import org.openimaj.citation.annotation.ReferenceType;
import org.openimaj.image.FImage;
import org.openimaj.image.analyser.ImageAnalyser;
import org.openimaj.image.pixel.ConnectedComponent;
import org.openimaj.image.pixel.Pixel;
import org.openimaj.image.processing.edges.CannyEdgeDetector;
import org.openimaj.image.processing.edges.StrokeWidthTransform;
import org.openimaj.image.processing.resize.ResizeProcessor;
import org.openimaj.util.set.DisjointSetForest;

/**
 * Implementation of the Stroke Width Transform text detection algorithm by
 * Epshtein et al.
 * <p>
 * This is a (relatively) high-performance text detection technique that does
 * not require training (except for parameter setting) and is language
 * independent. The algorithm automatically identifies individual characters
 * ("letters"), as well as performing word grouping and line segmentation.
 * <p>
 * There is an implicit assumption in this implementation that the text is
 * *almost* horizontal. This implementation cannot be considered to be rotation
 * invariant. It also has difficulties with curved text.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
@Reference(
		type = ReferenceType.Inproceedings,
		author = { "Epshtein, B.", "Ofek, E.", "Wexler, Y." },
		title = "Detecting text in natural scenes with stroke width transform",
		year = "2010",
		booktitle = "Computer Vision and Pattern Recognition (CVPR), 2010 IEEE Conference on",
		pages = { "2963", "2970" },
		customData = {
				"keywords",
				"image processing;text analysis;image operator;image pixel;natural images;natural scenes;stroke width transform;text detection;Colored noise;Computer vision;Engines;Filter bank;Geometry;Image segmentation;Layout;Optical character recognition software;Pixel;Robustness",
				"doi", "10.1109/CVPR.2010.5540041",
				"ISSN", "1063-6919"
		})
public class SWTTextDetector implements ImageAnalyser<FImage> {
	/**
	 * Text search "directions": Dark text on a lighter background, light text
	 * on a dark background and both.
	 * 
	 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
	 * 
	 */
	public static enum Direction {
		/**
		 * Dark text against a lighter background
		 */
		DarkOnLight {
			@Override
			protected void detect(FImage image, SWTTextDetector detector) {
				final StrokeWidthTransform swt = new StrokeWidthTransform(true, detector.options.canny);
				swt.setMaxStrokeWidth(detector.options.maxStrokeWidth);
				final FImage swtImage = image.process(swt);
				detector.analyseImage(image, swtImage);
			}
		},
		/**
		 * Light text against a lighter background
		 */
		LightOnDark {
			@Override
			protected void detect(FImage image, SWTTextDetector detector) {
				final StrokeWidthTransform swt = new StrokeWidthTransform(false, detector.options.canny);
				swt.setMaxStrokeWidth(detector.options.maxStrokeWidth);
				final FImage swtImage = image.process(swt);
				detector.analyseImage(image, swtImage);
			}
		},
		/**
		 * Search for both light and dark text
		 */
		Both {
			@Override
			protected void detect(FImage image, SWTTextDetector detector) {
				final StrokeWidthTransform swt = new StrokeWidthTransform(true, detector.options.canny);
				swt.setMaxStrokeWidth(detector.options.maxStrokeWidth);
				FImage swtImage = image.process(swt);
				detector.analyseImage(image, swtImage);

				swt.setDirection(false);
				swtImage = image.process(swt);
				detector.analyseImage(image, swtImage);
			}
		};

		protected abstract void detect(FImage image, SWTTextDetector detector);
	}

	/**
	 * Options for controlling the {@link SWTTextDetector}.
	 * 
	 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
	 */
	public static class Options {

		/**
		 * The "direction" to perform the SWT
		 */
		public Direction direction = Direction.DarkOnLight;

		/**
		 * The canny edge detector to use for the SWT.
		 */
		public CannyEdgeDetector canny = new CannyEdgeDetector(1);

		/**
		 * Upscale the image to double size before applying the SWT.
		 */
		public boolean doubleSize = false;

		/**
		 * Maximum allowed ratio of a pair of stroke widths for them to be
		 * considered part of the same connected component.
		 */
		public float strokeWidthRatio = 3.0f;

		/**
		 * Maximum allowed variance of stroke width in a single character as a
		 * percentage of the mean.
		 */
		public double letterVarianceMean = 0.93;

		/**
		 * Maximum allowed aspect ratio for a single letter
		 */
		public double maxAspectRatio = 10;

		/**
		 * Maximum allowed ratio of diameter to stroke width for a single
		 * character.
		 */
		public double maxDiameterStrokeRatio = 10;

		/**
		 * Minimum allowed component size; used to quickly filter out small
		 * components.
		 */
		public int minArea = 38;

		/**
		 * Minimum character height
		 */
		public float minHeight = 10;

		/**
		 * Maximum character height
		 */
		public float maxHeight = 300;

		/**
		 * Maximum allowed number of overlapping characters
		 */
		public int maxNumOverlappingBoxes = 10;

		/**
		 * Maximum allowed stroke width
		 */
		public int maxStrokeWidth = 70;

		/**
		 * Maximum ratio of stroke width for two letters to be considered to be
		 * related
		 */
		public float medianStrokeWidthRatio = 2;

		/**
		 * Maximum ratio of height for two letters to be considered to be
		 * related
		 */
		public float letterHeightRatio = 2;

		/**
		 * Maximum difference in intensity for two letters to be considered to
		 * be related
		 */
		public float intensityThreshold = 0.12f;

		/**
		 * The width multiplier for two letters to be considered to be related.
		 * Distance between centroids must be less than widthMultiplier *
		 * maxLetterWidth.
		 */
		public float widthMultiplier = 3;

		/**
		 * Minimum number of allowed letters on a line
		 */
		public int minLettersPerLine = 3;

		/**
		 * Ratio of vertical intersection for character pairing. This helps
		 * ensure that the characters are horizontal.
		 */
		public float intersectRatio = 1.3f;

		/**
		 * Ratio of the interclass std dev of the letter spacings to the mean to
		 * suggest a word break.
		 */
		public float wordBreakdownRatio = 1f;
	}

	/**
	 * 8-connected locations
	 */
	private final static int[][] connect8 = {
			{ -1, 0 }, { 1, 0 }, { 0, -1 }, { 0, 1 }, { -1, -1 }, { 1, -1 }, { -1, 1 }, { 1, 1 } };

	/**
	 * The parameters of the algorithm
	 */
	protected Options options;

	private List<LetterCandidate> letters = null;
	private List<LineCandidate> lines = null;
	private List<WordCandidate> words = null;

	/**
	 * Construct the {@link SWTTextDetector} with the default parameters.
	 */
	public SWTTextDetector() {
		this(new Options());
	}

	/**
	 * Construct the {@link SWTTextDetector} with the given parameters.
	 * 
	 * @param options
	 *            the parameters
	 */
	public SWTTextDetector(Options options) {
		this.options = options;
	}

	/**
	 * Get the current options.
	 * 
	 * @return the current options.
	 */
	public Options getOptions() {
		return options;
	}

	/**
	 * Modified connected component algorithm - uses a modified predicate to
	 * group SWT pixels based on their stroke width ratio.
	 * 
	 * @param image
	 *            swt image
	 * @return the detected components
	 */
	private List<ConnectedComponent> findComponents(FImage image) {
		final DisjointSetForest<Pixel> forest = new DisjointSetForest<Pixel>();

		Pixel current = new Pixel();
		Pixel next = new Pixel();
		for (int y = 0; y < image.height; y++) {
			for (int x = 0; x < image.width; x++) {
				final float currentValue = image.pixels[y][x];

				if (currentValue > 0 && currentValue != Float.POSITIVE_INFINITY) {
					current.x = x;
					current.y = y;

					if (forest.makeSet(current) != null)
						current = current.clone();

					for (int i = 0; i < connect8.length; i++) {
						final int xx = x + connect8[i][0];
						final int yy = y + connect8[i][1];

						if (xx >= 0 && xx < image.width - 1 && yy >= 0 && yy < image.height - 1) {
							final float value = image.pixels[yy][xx];

							if (value > 0 && value != Float.POSITIVE_INFINITY) {
								next.x = xx;
								next.y = yy;

								if (forest.makeSet(next) != null)
									next = next.clone();

								if ((Math.max(currentValue, value) / Math.min(currentValue, value)) < options.strokeWidthRatio)
									forest.union(current, next);
							}
						}
					}
				}
			}
		}

		final List<ConnectedComponent> components = new ArrayList<ConnectedComponent>();
		for (final Set<Pixel> pixels : forest.getSubsets()) {
			final ConnectedComponent cc = new ConnectedComponent(pixels);
			components.add(cc);
		}

		return components;
	}

	@Override
	public void analyseImage(FImage image) {
		letters = new ArrayList<LetterCandidate>();
		lines = new ArrayList<LineCandidate>();
		words = new ArrayList<WordCandidate>();

		if (options.doubleSize)
			image = ResizeProcessor.doubleSize(image);

		options.direction.detect(image, this);
	}

	protected void analyseImage(FImage image, FImage swt) {
		final List<ConnectedComponent> comps = findComponents(swt);
		final List<LetterCandidate> tmpLetters = LetterCandidate.findLetters(comps, swt, image, options);
		final List<LineCandidate> tmpLines = LineCandidate.extractLines(tmpLetters, this.options);

		this.letters.addAll(tmpLetters);
		this.lines.addAll(tmpLines);
		for (final LineCandidate line : tmpLines) {
			this.words.addAll(line.words);
		}
	}

	/**
	 * Get the detected candidate lines of text
	 * 
	 * @return the lines of text
	 */
	public List<LineCandidate> getLines() {
		return lines;
	}

	/**
	 * Get the unfiltered detected characters. Normally you would want to get
	 * the lines and then get the characters from each line as these will be
	 * filtered.
	 * 
	 * @see #getLines()
	 * 
	 * @return the unfiltered characters
	 */
	public List<LetterCandidate> getLetters() {
		return letters;
	}
}
