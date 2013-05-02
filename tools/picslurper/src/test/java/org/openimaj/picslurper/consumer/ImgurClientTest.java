package org.openimaj.picslurper.consumer;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.apache.http.client.ClientProtocolException;
import org.junit.Test;
import org.openimaj.web.scraping.images.ImgurClient;
import org.openimaj.web.scraping.images.ImgurClient.ImageResponse;
import org.openimaj.web.scraping.images.ImgurClient.ImgurTypeHash;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 */
public class ImgurClientTest {
	/**
	 * Test the translation of Imgur raw URLs to types and hashes
	 * 
	 * @throws MalformedURLException
	 */
	@Test
	public void testURLTranslate() throws MalformedURLException {
		final String[][] tests = new String[][] {
				new String[] { "http://imgur.com/a/Qlh7Y", "ALBUM", "Qlh7Y" },
				new String[] { "http://i.imgur.com/kwWo0.jpg", "IMAGE", "kwWo0" },
				new String[] { "http://i.imgur.com/kwWo0", "IMAGE", "kwWo0" },
				new String[] { "http://i.imgur.com/p/kwWo0", null, "kwWo0" },
				new String[] { "http://imgur.com/", null, null },
				new String[] { "http://imgur.com/gallery", "GALLERY", null },
				new String[] { "http://imgur.com/gallery/kwWo0", "IMAGE", "kwWo0" },
		};
		for (final String[] test : tests) {
			System.out.println("Testing: " + test[0]);
			final ImgurTypeHash res = ImgurClient.imgurURLtoHash(new URL(test[0]));
			System.out.println(res);
		}
	}

	/**
	 * @throws IOException
	 * @throws ClientProtocolException
	 * 
	 */
	@Test
	public void testImgurImageLists() throws ClientProtocolException, IOException {
		final ImgurClient client = new ImgurClient();
		final String album = "http://imgur.com/a/Qlh7Y";
		ImgurTypeHash typehash = ImgurClient.imgurURLtoHash(new URL(album));

		List<ImageResponse> images = client.getImages(typehash);
		assertTrue(images.size() > 0);

		final String image = "http://i.imgur.com/kwWo0.jpg";
		typehash = ImgurClient.imgurURLtoHash(new URL(image));
		images = client.getImages(typehash);
		assertTrue(images.size() == 1);
	}
}
