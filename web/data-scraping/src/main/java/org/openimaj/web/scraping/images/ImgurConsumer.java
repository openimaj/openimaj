/**
 * Copyright 2011 The University of Southampton, Yahoo Inc., and the
 * individual contributors. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openimaj.web.scraping.images;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.openimaj.web.scraping.SiteSpecificConsumer;
import org.openimaj.web.scraping.images.ImgurClient.ImageResponse;

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
