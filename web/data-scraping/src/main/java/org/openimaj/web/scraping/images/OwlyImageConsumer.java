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

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openimaj.io.HttpUtils;
import org.openimaj.web.scraping.SiteSpecificConsumer;

/**
 * ow.ly is a url shortening service that also has an image sharing service
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 */
public class OwlyImageConsumer implements SiteSpecificConsumer {

	@Override
	public boolean canConsume(URL url) {
		return url.getHost().contains("ow.ly") && url.getFile().startsWith("/i/");
	}

	@Override
	public List<URL> consume(URL url) {
		try {
			final byte[] retPage = HttpUtils.readURLAsBytes(url, false);
			final Document soup = Jsoup.parse(new String(retPage, "UTF-8"));
			final Elements imageElement = soup.select(".imageWrapper img");
			final List<URL> ret = new ArrayList<URL>();
			for (final Element element : imageElement) {
				final String imageSource = element.attr("src");
				if (imageSource != null) {
					final URL link = new URL(imageSource);
					ret.add(link);
				}
			}
			return ret;
		} catch (final Throwable e) {
			return null;
		}
	}

}
