package org.openimaj.hadoop.tools.twitter.utils;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.junit.Test;
import org.openimaj.io.IOUtils;

import gnu.trove.TObjectIntHashMap;

public class TestTimeWorldMap {
	@Test
	public void testTimeWorldMapReadWrite() throws IOException{
		long nTweets = 3;
		final TObjectIntHashMap<String> map = new TObjectIntHashMap<String>();
		map .put("word1", 10);
		map .put("word2", 20);
		map .put("word3", 30);
		TweetCountWordMap holder = new TweetCountWordMap(3,map);
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		IOUtils.writeBinary(os, holder);
		TweetCountWordMap read = IOUtils.read(new ByteArrayInputStream(os.toByteArray()), TweetCountWordMap.class);
		assertEquals(read,holder);
	}
}
