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
package org.openimaj.text.nlp.namedentity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openimaj.ml.annotation.ScoredAnnotation;
import org.openimaj.text.nlp.namedentity.YagoEntityContextScorerFactory.YagoEntityContextScorer;

/**
 * {@link EntityAnnotator} wrapper for {@link YagoEntityContextScorer}
 * @author Laurence Willmore (lgw1e10@ecs.soton.ac.uk)
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 */
public class YagoEntityContextAnnotator
		extends
		EntityAnnotator {
	
	private YagoEntityContextScorer ywi;

	/**
	 * Default Constructor
	 * @param ywi
	 */
	public YagoEntityContextAnnotator(YagoEntityContextScorer ywi) {
		super();
		this.ywi = ywi;
	}

	@Override
	public Set<HashMap<String, Object>> getAnnotations() {
		// Intentionally blank
		return null;
	}

	@Override
	public List<ScoredAnnotation<HashMap<String, Object>>> annotate(
			List<String> object) {
		Map<NamedEntity,Float> results = ywi.getScoredEntitiesFromContext(object);
		ArrayList<ScoredAnnotation<HashMap<String,Object>>> ret = new ArrayList<ScoredAnnotation<HashMap<String,Object>>>();
		for(NamedEntity entity : results.keySet()){
			HashMap<String,Object> annotation = new HashMap<String, Object>();
			annotation.put(URI, entity.rootName);
			annotation.put(TYPE, entity.type.toString());
			annotation.put(SCORE, results.get(entity));
			ret.add(new ScoredAnnotation<HashMap<String,Object>>(annotation,1));
		}
		return ret;
	}

}
