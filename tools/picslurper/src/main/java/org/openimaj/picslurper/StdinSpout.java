package org.openimaj.picslurper;

import java.io.InputStream;

@SuppressWarnings("serial")
public class StdinSpout extends LocalTweetSpout {

	private boolean usedSTDIN;

	@Override
	protected InputStream nextInputStream() throws Exception {
		if(usedSTDIN) return null;
		usedSTDIN = true; 
		return System.in;
	}

}
