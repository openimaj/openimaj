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
package org.openimaj.image.model.patch;

import org.openimaj.feature.DoubleFVComparison;
import org.openimaj.image.MBFImage;
import org.openimaj.image.pixel.statistics.HistogramModel;

public class HistogramPatchModel extends MBFPatchClassificationModel {
	private static final long serialVersionUID = 1L;
	
	public HistogramModel model;
	protected DoubleFVComparison compare = DoubleFVComparison.BHATTACHARYYA;
	
	public HistogramPatchModel(int patchWidth, int patchHeight, int... nbins) {
		super(nbins.length, patchWidth, patchHeight);

		model = new HistogramModel(nbins);
	}

	public HistogramPatchModel(int patchWidth, int patchHeight, DoubleFVComparison compare, int... nbins) {
		this(patchWidth, patchHeight, nbins);
		this.compare = compare;
		model = new HistogramModel(nbins);
	}
	
	public DoubleFVComparison getCompare() {
		return compare;
	}

	public void setCompare(DoubleFVComparison compare) {
		this.compare = compare;
	}

	@Override
	public float classifyPatch(MBFImage patch) {
		HistogramModel h = new HistogramModel(model.histogram.nbins);
		h.estimateModel(patch);
		return (float) model.histogram.compare(h.histogram, compare);
	}

	@Override
	public HistogramPatchModel clone() {
		HistogramPatchModel newmodel = new HistogramPatchModel(patchWidth, patchHeight, model.histogram.nbins);
		newmodel.model = model.clone();
		
		return newmodel;
	}

	@Override
	public void learnModel(MBFImage... images) {
		model.estimateModel(images);
	}	
}
