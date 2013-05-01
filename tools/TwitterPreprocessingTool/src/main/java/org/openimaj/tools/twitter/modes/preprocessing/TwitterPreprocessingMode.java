/**
 * Copyright (c) 2012, The University of Southampton and the individual contributors.
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
package org.openimaj.tools.twitter.modes.preprocessing;

import org.openimaj.twitter.GeneralJSON;
import org.openimaj.twitter.RDFAnalysisProvider;
import org.openimaj.twitter.USMFStatus;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * A processing mode that is able to process a tweet and also typed on the data
 * which it analyses from the tweet (so it can return this data if required)
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * @param <T>
 *            The type of the analysis result
 * 
 */
public abstract class TwitterPreprocessingMode<T> {

	/**
	 * Alters the twitter status in place with the analysis that is required to
	 * be performed
	 * 
	 * @param twitterStatus
	 * @return for conveniance also returns the analysis
	 */
	public abstract T process(USMFStatus twitterStatus);

	/**
	 * @return by default this adds no analysis and does nothing whatsoever
	 */
	public RDFAnalysisProvider rdfAnalysisProvider() {
		return new RDFAnalysisProvider() {

			@Override
			public void init() {
				// TODO Auto-generated method stub

			}

			@Override
			public void addAnalysis(Model m, Resource analysis, GeneralJSON analysisSource) {
				// TODO Auto-generated method stub

			}
		};
	}

	/**
	 * Given a twitter status, attempts to extract the analysis for this mode.
	 * If the analysis does not exist, the provided mode instance is used to
	 * create the analysis. If the provided mode is null a new mode is created.
	 * This mode creation might be slow, be careful about using this in this
	 * way.
	 * 
	 * @param <Q>
	 * @param status
	 *            the twitter status to be analysed
	 * @param mode
	 *            the mode to use if the analysis does no exist in the tweet
	 * @return the analysis results. These results are also injected into the
	 *         tweet's analysis
	 * @throws Exception
	 */
	public static <Q> Q results(USMFStatus status, TwitterPreprocessingMode<Q> mode) throws Exception {
		Q result = status.getAnalysis(mode.getAnalysisKey());
		if (result == null) {
			result = mode.process(status);
		}
		return result;
	}

	/**
	 * @return the keys this mode adds to the twitter analysis map
	 */
	public abstract String getAnalysisKey();
}
