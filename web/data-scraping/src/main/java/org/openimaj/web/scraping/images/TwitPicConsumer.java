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
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openimaj.web.scraping.SiteSpecificConsumer;

/**
 * Use JSoup to load the twitpic page and find the img tag that has a source
 * which contains the string "photos" or "cloudfront"
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 */
public class TwitPicConsumer implements SiteSpecificConsumer {
	@Override
	public boolean canConsume(URL url) {
		// http://twitpic.com/a67733
		return url.getHost().contains("twitpic.com");
	}

	@Override
	public List<URL> consume(URL url) {
		String largeURLStr = url.toString();
		if (!largeURLStr.endsWith("full")) {
			largeURLStr += "/full";
		}
		try {
			final Document doc = Jsoup.connect(largeURLStr).get();
			final Elements largeimage = doc.select("img");
			String imgSrc = "";
			for (final Element e : largeimage) {
				imgSrc = e.attr("src");
				if (imgSrc.contains("photos") || imgSrc.contains("cloudfront")) {
					break;
				}
			}
			final URL link = new URL(imgSrc);
			final List<URL> a = Arrays.asList(link);
			return a;
		} catch (final Exception e) {
			return null;
		}

	}
}
