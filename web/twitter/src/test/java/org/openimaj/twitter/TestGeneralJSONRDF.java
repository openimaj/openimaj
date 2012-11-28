package org.openimaj.twitter;

import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Test;
import org.openimaj.twitter.collection.FileTwitterStatusList;
import org.openimaj.twitter.utils.TwitterUtilsTest;

public class TestGeneralJSONRDF extends TwitterUtilsTest{
	@Test
	public void testFromUSMF() throws Exception {
		File twitterfile = fileFromeStream(USMFStatus.class.getResourceAsStream("/org/openimaj/twitter/json_tweets.txt"));
		FileTwitterStatusList<USMFStatus> status = FileTwitterStatusList.readUSMF(twitterfile,"UTF-8",GeneralJSONTwitter.class);
		USMFStatus aStatus = status.get(0);

		GeneralJSONRDF rgj = new GeneralJSONRDF();
		rgj.fromUSMF(aStatus);
		USMFStatus n = new USMFStatus();
		rgj.fillUSMF(n);
		assertTrue(n.equals(aStatus));
	}
}
