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
