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
package org.openimaj.ml.annotation.basic.util;

import gnu.trove.map.hash.TIntIntHashMap;

import java.util.Collection;
import java.util.List;

import org.openimaj.ml.annotation.Annotated;

import cern.jet.random.Empirical;
import cern.jet.random.EmpiricalWalker;
import cern.jet.random.engine.MersenneTwister;

/**
 * Choose the number of annotations based on the numbers of annotations
 * of each training example. Internally this {@link NumAnnotationsChooser}
 * constructs the distribution of annotation lengths from the training
 * data and then picks lengths randomly based on the distribution.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class PriorChooser implements NumAnnotationsChooser {
	private EmpiricalWalker numAnnotations;

	@Override
	public <O, A> void train(List<? extends Annotated<O, A>> data) {
		TIntIntHashMap nAnnotationCounts = new TIntIntHashMap();
		int maxVal = 0;
		
		for (Annotated<O, A> sample : data) {
			Collection<A> annos = sample.getAnnotations();

			nAnnotationCounts.adjustOrPutValue(annos.size(), 1, 1);
			
			if (annos.size()>maxVal) maxVal = annos.size();
		}
		
		//build distribution and rng for the number of annotations
		double [] distr = new double[maxVal+1];
		for (int i=0; i<=maxVal; i++) 
			distr[i] = nAnnotationCounts.get(i);
		numAnnotations = new EmpiricalWalker(distr, Empirical.NO_INTERPOLATION, new MersenneTwister());
	}

	@Override
	public int numAnnotations() {
		return numAnnotations.nextInt();
	}
}
