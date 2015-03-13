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
import java.util.Arrays;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.openimaj.web.scraping.SiteSpecificConsumer;

/**
 * Download images from twitter's own image hosting service
 *
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class TwitterPhotoConsumer implements SiteSpecificConsumer {
	@Override
	public boolean canConsume(URL url) {
		// http://twitter.com/HutchSelenator/status/222772697531301890/photo/1
		if (url.getHost().equals("twitter.com") && url.getPath().contains("photo"))
			return true;

		// http://pbs.twimg.com/media/B_7Q6PMWAAAzvH0.jpg
		return url.getHost().endsWith("twimg.com") && url.getPath().contains("media");
	}

	@Override
	public List<URL> consume(URL url) {
		if (url.getHost().endsWith("twimg.com")) {
			return Arrays.asList(new URL[] { url });
		}

		String largeURLStr = url.toString();
		if (!largeURLStr.endsWith("large")) {
			largeURLStr += "/large";
		}
		try {
			final Document doc = Jsoup.connect(largeURLStr).get();
			final Elements largeimage = doc.select(".media-slideshow-image");
			final URL link = new URL(largeimage.get(0).attr("src"));
			return Arrays.asList(link);
		} catch (final Exception e) {
			return null;
		}
	}
}
