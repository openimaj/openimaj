package org.openimaj.tools.imagecollection.collection;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.ParseException;

import org.junit.Test;
import org.openimaj.io.IOUtils;
import org.openimaj.tools.imagecollection.collection.ImageCollectionConfig;

public class ImageCollectionConfigTest {
	String jsonConfig = "{\"a\":{\"inner\":2},\"b\":\"cat\"}";
	
	@Test
	public void testImageCollectionConfig() throws IOException, ParseException{
		ImageCollectionConfig config = new ImageCollectionConfig(jsonConfig);
		assertEquals(config.read("$.a.inner"),2);
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		IOUtils.writeASCII(stream, config);
		assertEquals(stream.toString(),jsonConfig);
	}
}
