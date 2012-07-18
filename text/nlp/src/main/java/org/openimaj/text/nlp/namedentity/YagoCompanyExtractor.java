package org.openimaj.text.nlp.namedentity;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.openimaj.text.nlp.namedentity.FourStoreClientTool.Node;

import uk.co.magus.fourstore.client.Store;

/**
 * This class aims to use the Yago2 knowledge base to determine weather a
 * tokenised string has any references to companies. getEntities() is the
 * callable method for the results.
 * 
 * @author Laurence Willmore <lgw1e10@ecs.soton.ac.uk>
 * 
 */
public class YagoCompanyExtractor implements NamedEntityExtractor {

	StopWordStripper ss;
	YagoCompanyIndexFactory.YagoCompanyIndex ci;

	public YagoCompanyExtractor(YagoCompanyIndexFactory.YagoCompanyIndex index) {
		super();
		ss = new StopWordStripper(StopWordStripper.ENGLISH);
		ci = index;
	}

	private ArrayList<String> getYagoCandidates(String token) {		
		ArrayList<String> rootNames = ci.getCompanyListFromAliasToken(token);
		if (rootNames.size() > 0)return rootNames;
		else return null;
	}	

	@Override
	public Map< Integer, String> getEntities(List<String> tokens) {
		HashMap<Integer, ArrayList<String>> candidates = new HashMap<Integer, ArrayList<String>>();
		//See if any Company Aliases are used
		boolean aliasFound = false;
		for (int i = 0; i < tokens.size(); i++) {
			String token = tokens.get(i);
			if (!ss.isStopWord(token)) {
				ArrayList<String> matches = getYagoCandidates(token);
				if (matches != null) {
					candidates.put(i, matches);
					aliasFound=true;
				}
			}
		}
		//If aliases are found...
		if(aliasFound){
			String context = StringUtils.join(ss.getNonStopWords(tokens), ", ");
			//check if any aliases have more then 1 possible company and disambiguate.
			for (int ind : candidates.keySet()) {
				ArrayList<String> ents = candidates.get(ind);
				//disambiguate
				if(ents.size()>1){
					ArrayList<String> conts = ci.getCompanyListFromContext(context);
					boolean disambiguated=false;
					for (int i = 0; i < conts.size(); i++) {
						if(ents.contains(conts.get(i))){
							disambiguated = true;
							ents.clear();
							ents.add(conts.get(i));
							break;
						}
					}
					if(!disambiguated){
						String top = ents.get(0);
						ents.clear();
						ents.add(top);
					}
				}
			}
			HashMap<Integer, String> result = new HashMap<Integer, String>();
			for (int ind : candidates.keySet()) {
				result.put(ind, candidates.get(ind).get(0));
			}
			return result;
		}
		//If no aliases found, see if the contextual content points at a company.
		else{
			String context = StringUtils.join(ss.getNonStopWords(tokens), ", ");
			ArrayList<String> cc = ci.getCompanyListFromContext(context);
			HashMap<Integer, String> result = new HashMap<Integer,String>();
			if (cc.size()>0) {
				result.put(-1, cc.get(0));
			}
			return result;
		}		
	}
	
	public static void main(String[] args){
		YagoCompanyExtractor ye = null;
		try {
			ye = new YagoCompanyExtractor(YagoCompanyIndexFactory.createFromExistingIndex("src/main/resources/org/openimaj/text/namedentity/yagolucene"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String[] t = "Apple must be an awesome company".split(" ");
		ArrayList<String> tokens = new ArrayList<String>(Arrays.asList(t));
		Map<Integer, String> ents = ye.getEntities(tokens);
		for (int loc : ents.keySet()) { 
			System.out.println(ents.get(loc));
		} 
		
	}
}
