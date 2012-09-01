package org.openimaj.picslurper;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.openimaj.text.nlp.TweetTokeniserException;

public class PicslurperTwitterHarness {
	private PicslurperTwitterHarness() {
	}

	public static void main(String args[]) throws ClientProtocolException, IOException, TweetTokeniserException, InterruptedException {
		TwitterInputStreamFactory factory = TwitterInputStreamFactory.streamFactory();
		if (factory == null)
			return;
		System.setIn(factory.nextInputStream());
		try {
			PicSlurper.main("-o /Users/ss/Development/picslurper/01-09-2012 -j 1".split(" "));
		} catch (Exception e) {
			System.out.println("timeout?");
			e.printStackTrace();
		}
	}
}
