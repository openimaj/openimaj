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
package org.openimaj.ml.clustering.spectral;

import java.util.List;

import org.apache.log4j.Logger;
import org.openimaj.feature.DoubleFV;
import org.openimaj.feature.FeatureExtractor;
import org.openimaj.util.function.Function;

import ch.akuhn.matrix.SparseMatrix;

/**
 * Wraps the functionality of a {@link SimilarityClusterer} around a dataset
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * @param <T> 
 *
 */
public abstract class DoubleFVSimilarityFunction<T> implements Function<List<T>,SparseMatrix>{
	Logger logger = Logger.getLogger(DoubleFVSimilarityFunction.class);
	protected double[][] feats = null;
	private FeatureExtractor<DoubleFV, T> extractor;
	private List<T> data;
	/**
	 * @param extractor
	 *
	 */
	public DoubleFVSimilarityFunction(FeatureExtractor<DoubleFV, T> extractor) {
		this.extractor = extractor;
	}
	
	@Override
	public SparseMatrix apply(List<T> in) {
		this.data = in;
		this.prepareFeats();
		return this.similarity();
	};
	
	protected void prepareFeats() {
		if(feats!=null)return;
		int numInstances = data.size();
		feats = new double[numInstances][];
		int index = 0;
		for (T d : this.data) {
			feats[index++] = this.extractor.extractFeature(d).values;
		}
	}
	
	protected abstract SparseMatrix similarity();
}
