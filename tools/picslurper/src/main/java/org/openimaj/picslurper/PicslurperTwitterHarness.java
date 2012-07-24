package org.openimaj.picslurper;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.openimaj.text.nlp.TweetTokeniserException;

public class PicslurperTwitterHarness {
	private PicslurperTwitterHarness() {
	}
	
	public static void main(String args[]) throws ClientProtocolException, IOException, TweetTokeniserException, InterruptedException{
		System.setIn(TwitterInputStreamFactory.streamFactory().nextInputStream());
		try{
			PicSlurper.main("-o /Users/ss/Development/picslurper/23-2012 -j 1".split(" "));
		}catch(Exception e){
			System.out.println("timeout?");
			e.printStackTrace();
		}
	}
}
