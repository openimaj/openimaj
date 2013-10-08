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

import java.io.InputStreamReader;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.openimaj.web.scraping.SiteSpecificConsumer;

import com.google.gson.Gson;

/**
 * Use the instagram api to download images
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 */
public class InstagramConsumer implements SiteSpecificConsumer {
	String apiCallFormat = "http://api.instagram.com/oembed?url=http://instagr.am/p/%s";
	private transient Gson gson = new Gson();

	@Override
	public boolean canConsume(URL url) {
		// http://instagram.com/p/Mbr57UC7L6
		return url.getHost().equals("instagr.am") || url.getHost().equals("instagram.com");
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<URL> consume(URL url) {
		String file = url.getFile();
		if (file.endsWith("/"))
			file = file.substring(0, file.length() - 1);
		final String[] splits = file.split("/");
		final String shortID = splits[splits.length - 1];
		final String apiCall = String.format(apiCallFormat, shortID);
		try {
			final Map<String, Object> res = gson.fromJson(new InputStreamReader(new URL(apiCall).openConnection()
					.getInputStream()), Map.class);
			final String instagramURL = (String) res.get("url");
			final URL u = new URL(instagramURL);
			return Arrays.asList(u);
		} catch (final Exception e) {
			return null;
		}
	}

}
