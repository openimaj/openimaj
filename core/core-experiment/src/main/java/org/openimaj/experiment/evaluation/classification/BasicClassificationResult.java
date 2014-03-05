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
package org.openimaj.experiment.evaluation.classification;

import gnu.trove.map.hash.TObjectDoubleHashMap;
import gnu.trove.procedure.TObjectDoubleProcedure;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.openimaj.util.pair.ObjectDoublePair;

/**
 * A basic implementation of a {@link ClassificationResult} that internally
 * maintains a map of classes to confidences.
 * <p>
 * A threshold is used to determine whether a class has a high-enough confidence
 * to be considered part of the predicted set of classes.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 * @param <CLASS>
 *            type of class predicted by the {@link Classifier}
 */
public class BasicClassificationResult<CLASS> implements ClassificationResult<CLASS> {
	private final TObjectDoubleHashMap<CLASS> data = new TObjectDoubleHashMap<CLASS>();
	private double threshold = 0;

	/**
	 * Construct with a default threshold of 0.
	 */
	public BasicClassificationResult() {

	}

	/**
	 * Construct with the given threshold.
	 * 
	 * @param threshold
	 *            the threshold
	 */
	public BasicClassificationResult(double threshold) {
		this.threshold = threshold;
	}

	/**
	 * Add a class/confidence pair.
	 * 
	 * @param clz
	 *            the class
	 * @param confidence
	 *            the confidence
	 */
	public void put(CLASS clz, double confidence) {
		data.put(clz, confidence);
	}

	@Override
	public double getConfidence(CLASS clazz) {
		return data.get(clazz);
	}

	@Override
	public Set<CLASS> getPredictedClasses() {
		// predicted classes are sorted by decreasing confidence

		final List<ObjectDoublePair<CLASS>> toSort = new ArrayList<ObjectDoublePair<CLASS>>();

		data.forEachEntry(new TObjectDoubleProcedure<CLASS>() {
			@Override
			public boolean execute(CLASS key, double confidence) {
				if (confidence > threshold)
					toSort.add(new ObjectDoublePair<CLASS>(key, confidence));
				return true;
			}
		});

		Collections.sort(toSort, new Comparator<ObjectDoublePair<CLASS>>() {
			@Override
			public int compare(ObjectDoublePair<CLASS> o1, ObjectDoublePair<CLASS> o2) {
				return -1 * Double.compare(o1.second, o2.second);
			}
		});

		final Set<CLASS> keys = new LinkedHashSet<CLASS>(toSort.size());

		for (final ObjectDoublePair<CLASS> p : toSort)
			keys.add(p.first);

		return keys;
	}
}
