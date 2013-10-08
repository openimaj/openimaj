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
package org.openimaj.text.geocode;

import org.apache.solr.client.solrj.SolrServerException;
import org.junit.Test;

public class GeonamesIndexTest {
	// private GeonamesIndex geonames;
	//
	// /**
	// *
	// */
	// @Before
	// public void setup(){
	// System.setProperty("geonames.solr.home",
	// "/Users/ss/Experiments/geonames/solr.short");
	// this.geonames = GeonamesIndex.instance();
	// }
	//
	// /**
	// * @throws SolrServerException
	// *
	// */
	@Test
	public void testIndex() throws SolrServerException {
		//
		// QueryResponse resp = this.geonames.query(
		// "{!type=dismax qf=\"country_code basic_name timezone alternames\" mm=1}"
		// +
		// "visanceny",
		// 100
		// );
		// SolrDocumentList results = resp.getResults();
		// for (SolrDocument solrDocument : results) {
		// // System.out.println(solrDocument.keySet());
		// System.out.println(
		// solrDocument.get("basic_name") + ", " +
		// solrDocument.get("country_code")+ ", " +
		// solrDocument.get("timezone")
		// );
		// }
	}
}
