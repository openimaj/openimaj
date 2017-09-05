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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.openimaj.util.pair.ObjectDoublePair;

/**
 * Utility methods for working with {@link ClassificationResult}s
 *
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 */
public class ClassificationResultUtils {

	private ClassificationResultUtils() {
	}

	/**
	 * Get the class with the highest confidence
	 *
	 * @param result
	 *            the {@link ClassificationResult} to work with
	 * @return the class with the highest confidence
	 */
	public static <CLASS> CLASS getHighestConfidenceClass(ClassificationResult<CLASS> result) {
		CLASS bestClass = null;
		double bestConfidence = 0;
		for (final CLASS s : result.getPredictedClasses())
		{
			if (result.getConfidence(s) >= bestConfidence)
			{
				bestClass = s;
				bestConfidence = result.getConfidence(s);
			}
		}

		return bestClass;
	}

	/**
	 * Get the all classes and confidences, sorted by decreasing confidence
	 *
	 * @param result
	 *            the {@link ClassificationResult} to work with
	 * @return the sorted classes and confidences
	 */
	public static <CLASS>
			List<ObjectDoublePair<CLASS>>
			getSortedClassesAndConfidences(ClassificationResult<CLASS> result)
	{
		final List<ObjectDoublePair<CLASS>> list = new ArrayList<>();

		for (final CLASS clz : result.getPredictedClasses()) {
			list.add(new ObjectDoublePair<>(clz, result.getConfidence(clz)));
		}

		Collections.sort(list, new Comparator<ObjectDoublePair<CLASS>>() {
			@Override
			public int compare(ObjectDoublePair<CLASS> o1, ObjectDoublePair<CLASS> o2) {
				return -1 * Double.compare(o1.second, o2.second);
			}
		});

		return list;
	}
}
