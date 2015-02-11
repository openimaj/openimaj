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
package org.openimaj.image.feature.local.affine;

import org.openimaj.citation.annotation.Reference;
import org.openimaj.citation.annotation.ReferenceType;
import org.openimaj.feature.local.list.LocalFeatureList;
import org.openimaj.feature.local.list.MemoryLocalFeatureList;
import org.openimaj.image.FImage;
import org.openimaj.image.Image;
import org.openimaj.image.feature.local.engine.DoGSIFTEngine;
import org.openimaj.image.feature.local.engine.DoGSIFTEngineOptions;
import org.openimaj.image.feature.local.engine.Engine;
import org.openimaj.image.feature.local.keypoints.Keypoint;
import org.openimaj.image.processor.SinglebandImageProcessor;

/**
 * Abstract base implementation of Affine-simulated SIFT (ASIFT).
 * <p>
 * This is implemented as an extension of the {@link AffineSimulationExtractor}
 * which uses a {@link DoGSIFTEngine} to extract SIFT features from each affine
 * simulation.
 *
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 * @param <I>
 *            Type of image
 * @param <P>
 *            Type of pixel
 */
@Reference(
		type = ReferenceType.Article,
		author = { "Morel, Jean-Michel", "Yu, Guoshen" },
		title = "{ASIFT: A New Framework for Fully Affine Invariant Image Comparison}",
		year = "2009",
		journal = "SIAM J. Img. Sci.",
		publisher = "Society for Industrial and Applied Mathematics")
public abstract class ASIFT<I extends Image<P, I> & SinglebandImageProcessor.Processable<Float, FImage, I>, P>
extends
AffineSimulationExtractor<LocalFeatureList<Keypoint>, Keypoint, I, P>
{
	Engine<Keypoint, I> keypointEngine;

	/**
	 * A commonly used option, while all others in {@link DoGSIFTEngineOptions}
	 * are default
	 *
	 * @param hires
	 *            whether the image should be double sized as a first step
	 */
	public ASIFT(boolean hires) {
		super();

		final DoGSIFTEngineOptions<I> opts = new DoGSIFTEngineOptions<I>();
		opts.setDoubleInitialImage(hires);
		keypointEngine = this.constructEngine(opts);
	}

	/**
	 * @param opts
	 *            the options required by {@link DoGSIFTEngine} instances
	 */
	public ASIFT(DoGSIFTEngineOptions<I> opts) {
		super();
		keypointEngine = this.constructEngine(opts);
	}

	/**
	 * An engine which can process images of type <I> and output keypoints
	 *
	 * @param opts
	 * @return various engines
	 */
	public abstract Engine<Keypoint, I> constructEngine(DoGSIFTEngineOptions<I> opts);

	@Override
	protected LocalFeatureList<Keypoint> newList() {
		return new MemoryLocalFeatureList<Keypoint>();
	}

	@Override
	protected LocalFeatureList<Keypoint> detectFeatures(I image) {
		final LocalFeatureList<Keypoint> keys = keypointEngine.findFeatures(image);

		return keys;
	}
}
