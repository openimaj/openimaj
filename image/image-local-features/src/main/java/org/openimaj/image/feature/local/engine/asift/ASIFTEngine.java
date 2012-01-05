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

import org.openimaj.feature.local.list.LocalFeatureList;
import org.openimaj.feature.local.list.MemoryLocalFeatureList;
import org.openimaj.image.FImage;
import org.openimaj.image.feature.local.affine.AffineParams;
import org.openimaj.image.feature.local.affine.AffineSimulation;
import org.openimaj.image.feature.local.affine.AffineSimulationKeypoint;
import org.openimaj.image.feature.local.affine.BasicASIFT;
import org.openimaj.image.feature.local.engine.DoGSIFTEngineOptions;
import org.openimaj.image.feature.local.keypoints.Keypoint;


public class ASIFTEngine {
	protected AffineSimulation<LocalFeatureList<Keypoint>, Keypoint,FImage,Float> asift;
	protected int nTilts = 5;
	
	public ASIFTEngine() {
		this(false);
	}
	
	public ASIFTEngine(boolean hires) {
		asift = new BasicASIFT(hires);
	}
	
	public ASIFTEngine(boolean hires, int nTilts) {
		asift = new BasicASIFT(hires);
		this.nTilts = nTilts;
	}
	
	public ASIFTEngine(DoGSIFTEngineOptions<FImage> opts) {
		asift = new BasicASIFT(opts);
	}
	
	public ASIFTEngine(DoGSIFTEngineOptions<FImage> opts, int nTilts) {
		asift = new BasicASIFT(opts);
		this.nTilts = nTilts;
	}
	
	public LocalFeatureList<Keypoint> findKeypoints(FImage image) {
		asift.process(image, nTilts);
		return asift.getKeypoints();
	}
	
	public LocalFeatureList<Keypoint> findKeypoints(FImage image, AffineParams params) {
		return asift.process(image, params);
	}
	
	public Map<AffineParams, LocalFeatureList<Keypoint>> findKeypointsMapped(FImage image) {
		asift.process(image, nTilts);
		return asift.getKeypointsMap();
	}
	
	public LocalFeatureList<AffineSimulationKeypoint> findSimulationKeypoints(FImage image) {
		asift.process(image, nTilts);
		Map<AffineParams, LocalFeatureList<Keypoint>> keypointMap = asift.getKeypointsMap();
		LocalFeatureList<AffineSimulationKeypoint> affineSimulationList = new MemoryLocalFeatureList<AffineSimulationKeypoint>(); 
		for(AffineParams params : asift.simulationOrder){
			for(Keypoint k : keypointMap.get(params)){
				affineSimulationList .add(new AffineSimulationKeypoint(k,params,asift.simulationOrder.indexOf(params)));
			}
		}
		return affineSimulationList;
	}
}
