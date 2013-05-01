package org.openimaj.picslurper.consumer;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.openimaj.picslurper.SiteSpecificConsumer;
import org.openimaj.picslurper.consumer.ImgurClient.ImageResponse;

/**
 * Downloads images hosted on imgur.com using their API
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 */
public class ImgurConsumer implements SiteSpecificConsumer {

	private ImgurClient client;

	/**
	 * initialise the {@link ImgurClient} instance
	 */
	public ImgurConsumer() {
		this.client = new ImgurClient();
	}

	@Override
	public boolean canConsume(URL url) {

		return url.getHost().contains("imgur");
	}

	@Override
	public List<URL> consume(URL url) {
		try {
			List<ImageResponse> imageJSON = null;
			final List<URL> ret = new ArrayList<URL>();
			imageJSON = client.getImages(ImgurClient.imgurURLtoHash(url));
			for (final ImageResponse imageResponse : imageJSON) {
				final URL link = imageResponse.getOriginalLink();
				ret.add(link);
			}
			return ret;
		} catch (final Exception e) {
			return null;
		}
	}
}
