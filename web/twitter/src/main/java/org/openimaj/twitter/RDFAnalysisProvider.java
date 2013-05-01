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
package org.openimaj.twitter;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * The analysis held in {@link GeneralJSON} can be translated to JSON very
 * easily The same analysis cannot be easily translated to RDF so this class
 * must be registered in GeneralJSONRDF's map to do so.
 * <p>
 * What this means that if things add analysis to a {@link GeneralJSONRDF}
 * instance or a {@link USMFStatus} that will eventually feed a
 * {@link GeneralJSONRDF} instance, they should register with
 * {@link GeneralJSONRDF#registerRDFAnalysisProvider(String, RDFAnalysisProvider)}
 * want to
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 */
public interface RDFAnalysisProvider {
	/**
	 * When given this analysis, fill the model with triples representing its
	 * existence. The IRI of the status the analysis is against is also provided
	 * and must be added to the model with the IRI of the analysis also.
	 * 
	 * @param m
	 *            the model to add triples to
	 * @param analysis
	 *            the status to associate the analysis to
	 * @param analysisSource
	 *            the analysis to transform to triples
	 */
	public void addAnalysis(Model m, Resource analysis, GeneralJSON analysisSource);

	/**
	 * Prepare yourself. Called once at the beggining of a write of a batch.
	 */
	public void init();
}
