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
package org.openimaj.image.feature.local.engine.asift;

import java.util.Map;

import org.openimaj.citation.annotation.Reference;
import org.openimaj.citation.annotation.ReferenceType;
import org.openimaj.feature.local.list.LocalFeatureList;
import org.openimaj.feature.local.list.MemoryLocalFeatureList;
import org.openimaj.image.FImage;
import org.openimaj.image.feature.local.affine.AffineSimulationExtractor;
import org.openimaj.image.feature.local.affine.AffineSimulationKeypoint;
import org.openimaj.image.feature.local.affine.BasicASIFT;
import org.openimaj.image.feature.local.engine.DoGSIFTEngine;
import org.openimaj.image.feature.local.engine.DoGSIFTEngineOptions;
import org.openimaj.image.feature.local.engine.Engine;
import org.openimaj.image.feature.local.keypoints.Keypoint;
import org.openimaj.image.processing.transform.AffineParams;

/**
 * An {@link Engine} for ASIFT.
 *
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 */
@Reference(
		type = ReferenceType.Article,
		author = { "Morel, Jean-Michel", "Yu, Guoshen" },
		title = "{ASIFT: A New Framework for Fully Affine Invariant Image Comparison}",
		year = "2009",
		journal = "SIAM J. Img. Sci.",
		publisher = "Society for Industrial and Applied Mathematics")
public class ASIFTEngine implements Engine<AffineSimulationKeypoint, FImage> {
	protected AffineSimulationExtractor<LocalFeatureList<Keypoint>, Keypoint, FImage, Float> asift;
	protected int nTilts = 5;

	/**
	 * Construct using 5 tilt levels and no initial double-sizing. The default
	 * parameters for the internal {@link DoGSIFTEngine} are used.
	 */
	public ASIFTEngine() {
		this(false);
	}

	/**
	 * Construct using 5 tilt levels with optional initial double-sizing. The
	 * default parameters for the internal {@link DoGSIFTEngine} are used.
	 *
	 * @param hires
	 *            should the image should be double sized as a first step
	 */
	public ASIFTEngine(boolean hires) {
		asift = new BasicASIFT(hires);
	}

	/**
	 * Construct using given number of tilt levels with optional initial
	 * double-sizing. The default parameters for the internal
	 * {@link DoGSIFTEngine} are used.
	 *
	 * @param hires
	 *            should the image should be double sized as a first step
	 * @param nTilts
	 *            number of tilt levels
	 */
	public ASIFTEngine(boolean hires, int nTilts) {
		asift = new BasicASIFT(hires);
		this.nTilts = nTilts;
	}

	/**
	 * Construct using 5 tilt levels and the given parameters for the internal
	 * {@link DoGSIFTEngine}.
	 *
	 * @param opts
	 *            parameters for the internal {@link DoGSIFTEngine}.
	 */
	public ASIFTEngine(DoGSIFTEngineOptions<FImage> opts) {
		asift = new BasicASIFT(opts);
	}

	/**
	 * Construct using the given numbe of tilt levels and parameters for the
	 * internal {@link DoGSIFTEngine}.
	 *
	 * @param opts
	 *            parameters for the internal {@link DoGSIFTEngine}.
	 * @param nTilts
	 *            number of tilt levels
	 */
	public ASIFTEngine(DoGSIFTEngineOptions<FImage> opts, int nTilts) {
		asift = new BasicASIFT(opts);
		this.nTilts = nTilts;
	}

	/**
	 * Find the features as a list of {@link Keypoint} objects
	 *
	 * @param image
	 *            the image
	 * @return the detected features
	 */
	public LocalFeatureList<Keypoint> findKeypoints(FImage image) {
		asift.detectFeatures(image, nTilts);
		return asift.getFeatures();
	}

	/**
	 * Find the features of a single simulation as a list of {@link Keypoint}
	 * objects
	 *
	 * @param image
	 *            the image
	 * @param params
	 *            the simulation parameters
	 * @return the detected features
	 */
	public LocalFeatureList<Keypoint> findKeypoints(FImage image,
			AffineParams params)
			{
		return asift.detectFeatures(image, params);
			}

	/**
	 * Find the features and return the resultant features in a per-simulation
	 * format.
	 *
	 * @param image
	 *            the image
	 * @return the features
	 */
	public Map<AffineParams, LocalFeatureList<Keypoint>> findKeypointsMapped(FImage image)
	{
		asift.detectFeatures(image, nTilts);
		return asift.getKeypointsMap();
	}

	@Override
	public LocalFeatureList<AffineSimulationKeypoint> findFeatures(FImage image) {
		asift.detectFeatures(image, nTilts);
		final Map<AffineParams, LocalFeatureList<Keypoint>> keypointMap = asift.getKeypointsMap();
		final LocalFeatureList<AffineSimulationKeypoint> affineSimulationList = new MemoryLocalFeatureList<AffineSimulationKeypoint>();
		for (final AffineParams params : asift.simulationOrder) {
			for (final Keypoint k : keypointMap.get(params)) {
				affineSimulationList.add(new AffineSimulationKeypoint(k, params, asift.simulationOrder.indexOf(params)));
			}
		}
		return affineSimulationList;
	}
}
