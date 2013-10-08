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
package org.openimaj.demos.sandbox.geocode;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import org.openimaj.text.nlp.geocode.GeonamesIndex;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk), Jonathan Hare
 *         (jsh2@ecs.soton.ac.uk), David Duplaw (dpd@ecs.soton.ac.uk)
 * 
 */
public class CountryToCC {
	static String[] countries = {
			"Austria", "Belgium", "Bulgaria", "Cyprus", "Denmark",
			"Estonia", "Finland", "France", "Germany", "Greece",
			"Ireland", "Italy", "Luxembourg", "Netherlands",
			"Portugal", "Slovakia", "Slovenia", "Spain", "Sweden", "UK"
	};

	public static void main(String[] args) throws SolrServerException {
		System.setProperty("geonames.solr.home", "/Users/ss/Experiments/geonames/solr");
		final GeonamesIndex geoIndex = GeonamesIndex.instance();
		for (final String country : countries) {
			final String firstpart = "((feature_class:A AND feature_code:PCLI)^10 OR (feature_class:P AND feature_code:PPL)) AND NOT country_code:RU AND NOT country_code:KR AND NOT country_code:HK AND NOT country_code:KP AND ";
			// firstpart = "";
			final QueryResponse resq = geoIndex.query(firstpart + "alternames:" + country, 100);
			final SolrDocumentList res = resq.getResults();
			System.out.println(res.get(0).get("country_code"));
		}
	}
}
