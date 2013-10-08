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
package org.openimaj.text.nlp.geocode;

import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;

/**
 * Use a Solr index built against the geonames allcountries.txt dump.
 * Several attempts are made to interrogate the index.
 * @author Sina Samangooei (ss@ecs.soton.ac.uk), Jonathan Hare (jsh2@ecs.soton.ac.uk), David Duplaw (dpd@ecs.soton.ac.uk)
 *
 */
public class ReverseGeoCode {
	private GeonamesIndex geoindex;
	private String validCodes;

	/**
	 * 
	 */
	public ReverseGeoCode() {
		this.geoindex = GeonamesIndex.instance();
		this.validCodes = null;
	}
	
	/**
	 * @param validCountryCodes
	 */
	public ReverseGeoCode(String ... validCountryCodes){
		String[] prefixed = new String[validCountryCodes.length];
		int i = 0;
		for (String vcc : validCountryCodes) {
			prefixed[i++] = "country_code:"+vcc;
		}
		this.validCodes = String.format("(%s)^2", StringUtils.join(prefixed, " OR "));
	}
	
	/**
	 * @param raw
	 * @return The estimated country_code 
	 */
	public String countryCode(String raw){
		try {
			QueryResponse qres = this.geoindex.query(constructQuery(raw));
			SolrDocumentList res = qres.getResults();
			return (String) res.get(0).get("country_code");
		} catch (SolrServerException e) {
			
		}
		return null;
	}

	private String constructQuery(String raw) {
		String query = "{!type=dismax qf=\"country_code basic_name timezone\" mm=1}";
		if(validCodes!=null){
			query +=this.validCodes + " AND ";
		}
		query += raw;
		return query;
	}
}
