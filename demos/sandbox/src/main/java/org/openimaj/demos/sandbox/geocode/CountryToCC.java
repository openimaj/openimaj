package org.openimaj.demos.sandbox.geocode;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.response.JSONResponseWriter;
import org.openimaj.text.nlp.geocode.GeonamesIndex;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk), Jonathan Hare (jsh2@ecs.soton.ac.uk), David Duplaw (dpd@ecs.soton.ac.uk)
 *
 */
public class CountryToCC {
	static String[] countries = {
			"Austria","Belgium","Bulgaria","Cyprus","Denmark",
			"Estonia","Finland","France","Germany","Greece",
			"Ireland","Italy","Luxembourg","Netherlands",
			"Portugal","Slovakia","Slovenia","Spain","Sweden","UK"
	};
	
	public static void main(String[] args) throws SolrServerException {
		System.setProperty("geonames.solr.home", "/Users/ss/Experiments/geonames/solr");
		GeonamesIndex geoIndex = GeonamesIndex.instance();
		for (String country : countries) {
			String firstpart = "((feature_class:A AND feature_code:PCLI)^10 OR (feature_class:P AND feature_code:PPL)) AND NOT country_code:RU AND NOT country_code:KR AND NOT country_code:HK AND NOT country_code:KP AND ";
//			firstpart = "";
			QueryResponse resq = geoIndex.query(firstpart+"alternames:"+country,100);
			SolrDocumentList res = resq.getResults();
			System.out.println(res.get(0).get("country_code"));
		}
	}
}
