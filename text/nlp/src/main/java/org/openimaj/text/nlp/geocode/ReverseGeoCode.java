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
