package org.openimaj.text.nlp.namedentity;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.lucene.index.CorruptIndexException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.openimaj.io.FileUtils;
import org.openimaj.text.nlp.TweetTokeniser;
import org.openimaj.text.nlp.TweetTokeniserException;
import org.openimaj.text.nlp.namedentity.YagoWikiIndexFactory.YagoWikiIndex;

public class YagoCompanyTester {

	private String[][] UriTweets = new String[][] {
			new String[] {
					"http://yago-knowledge.org/resource/Olam_International",
					"What is that company in singapore that produces rice and cotton?" },
			new String[] {
					"http://yago-knowledge.org/resource/Array_Networks",
					"Proven in over 5000 worldwide customer deployments, Array Networks is a global leader in application, desktop and cloud service delivery." },
			new String[] { "http://yago-knowledge.org/resource/Supermicro",
					"a global leader in high-performance, high-efficiency server technology" },
			new String[] {
					"http://yago-knowledge.org/resource/Scientific_Atlanta",
					"This site allows our customers to download various technical documents from the Transmission Networks Systems Division" },
			new String[] {
					"http://yago-knowledge.org/resource/Government_National_Mortgage_Association",
					"At Ginnie Mae, we help make affordable housing a reality for millions of low- and moderate-income households across America" },
			new String[] {
					"http://yago-knowledge.org/resource/International_Union_for_Conservation_of_Nature",
					"the world’s oldest and largest global environmental organization" },
			/*
			 * new String[] { "http://yago-knowledge.org/resource/Belcan",
			 * "From jet engines to electronics, heavy equipment to pharmaceuticals, and distribution centers to manufacturing,"
			 * },
			 */
			new String[] {
					"http://yago-knowledge.org/resource/Yahoo!",
					"Founded in 1994 by Stanford PhD candidates David Filo and Jerry Yang as a way for them to keep track of their personal interests on the Internet" },

			new String[] { "http://yago-knowledge.org/resource/Fujifilm",
					"I have been looking at cannon and fuji for a new camera, but can't decide" },
			new String[] {
					"http://yago-knowledge.org/resource/HSBC",
					"What do people thing of HSBC? How does it compare to other British Banks? In particular for its Commercial Banking Services" } };
	ArrayList<String> companyUris = new ArrayList<String>();
	ArrayList<String> tweets = new ArrayList<String>();
	@Rule
	public TemporaryFolder index = new TemporaryFolder();

	/**
	 * 
	 */
	@Test
	public void testYagoWikiIndex() {
		YagoWikiIndex yci = getYCI();		
		for (int i = 0; i < tweets.size(); i++) {
			String tweet = tweets.get(i);
			TweetTokeniser t = null;
			try {
				t = new TweetTokeniser(tweet);
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (TweetTokeniserException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			List<String> tokens = t.getStringTokens();
			String company = YagoQueryUtils.yagoResourceToString(companyUris
					.get(i));
			String topresult = null;
			float topscore = 0;
			HashMap<String, Float> res = yci.getCompanyListFromContext(tokens);
			for (String com : res.keySet()) {
				if (res.get(com) > topscore) {
					topresult = com;
					topscore = res.get(com);
				}
			}
			System.out.println(topscore);
			assertEquals(company, topresult);
		}

	}

	private YagoWikiIndex getYCI() {
		try {
			if (indexEmpty())
				return buildYagoTestIndex();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			System.exit(0);
		}
		companyUris = new ArrayList<String>();
		tweets = new ArrayList<String>();
		for (int i = 0; i < UriTweets.length; i++) {
			companyUris.add(UriTweets[i][0]);
			tweets.add(UriTweets[i][1]);
		}
		try {
			return new YagoWikiIndexFactory(false)
					.createFromIndexFile(index.getRoot().getAbsolutePath());
		} catch (CorruptIndexException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	private YagoWikiIndex buildYagoTestIndex() {
		YagoWikiIndex yci = null;
		ArrayList<String> companyUris = new ArrayList<String>();
		for (int i = 0; i < UriTweets.length; i++) {
			companyUris.add(UriTweets[i][0]);
		}
		try {
			yci = new YagoWikiIndexFactory(false).createFromYagoURIList(companyUris,index.getRoot().getAbsolutePath(), YagoQueryUtils.YAGO_SPARQL_ENDPOINT); 
		} catch (CorruptIndexException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return yci;
	}

	private boolean indexEmpty() throws IOException {
		if (index.getRoot().listFiles().length > 0)
			return false;
		else
			return true;
	}

}
