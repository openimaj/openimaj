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

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.httpclient.util.HttpURLConnection;
import org.openimaj.util.api.auth.DefaultTokenFactory;
import org.openimaj.util.auth.web.TumblrAPIToken;
import org.openimaj.web.scraping.SiteSpecificConsumer;

import com.google.gson.Gson;

/**
 * Using a tumblr API key turn a Tmblr URL to an image id and call the tumblr
 * API's posts function.
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 */
public class TmblrPhotoConsumer implements SiteSpecificConsumer {
	private transient Gson gson = new Gson();
	private TumblrAPIToken token;

	/**
	 * Use the {@link DefaultTokenFactory} to load the default api token
	 */
	public TmblrPhotoConsumer() {
		this(DefaultTokenFactory.get(TumblrAPIToken.class));
	}

	/**
	 * Construct with the given api token
	 * 
	 * @param token
	 *            the api token
	 */
	public TmblrPhotoConsumer(TumblrAPIToken token) {
		this.token = token;
	}

	@Override
	public boolean canConsume(URL url) {
		// http://tmblr.co/ZoH2IyP4lDVD
		return (url.getHost().equals("tmblr.co") || url.getHost().endsWith("tumblr.com"))
				&& !url.getHost().contains("media");
	}

	String tumblrAPICall = "http://api.tumblr.com/v2/blog/derekg.org/posts?id=%s&api_key=%s";

	@SuppressWarnings("unchecked")
	@Override
	public List<URL> consume(URL url) {
		// construct the actual tumblr address
		try {
			final List<URL> images = new ArrayList<URL>();
			final String postID = getPostID(url);
			if (postID == null)
				return images;
			// NOW call the tumblrAPI
			final String tmblrRequest = String.format(tumblrAPICall, postID, token.apikey);
			final Map<String, Object> res = gson.fromJson(new InputStreamReader(new URL(tmblrRequest).openConnection()
					.getInputStream()), Map.class);

			final Map<?, ?> response = (Map<?, ?>) res.get("response");
			final Map<?, ?> posts = (Map<?, ?>) ((List<?>) response.get("posts")).get(0);
			final List<Map<?, ?>> photos = ((List<Map<?, ?>>) posts.get("photos"));
			if (photos == null)
				return null;

			for (final Map<?, ?> photo : photos) {
				final String photoURLStr = (String) ((Map<String, Object>) photo.get("original_size")).get("url");
				final URL photoURL = new URL(photoURLStr);
				images.add(photoURL);
			}

			return images;
		} catch (final Throwable e) {
			return null;
		}
	}

	/**
	 * handles the variety of ways a tumblr addresses can be forwarded to
	 * 
	 * @param url
	 * @return
	 * @throws IOException
	 */
	private String getPostID(URL url) throws IOException {
		final String host = url.getHost();
		URL loc = url;

		if (host.equals("tmblr.co") || host.equals("tumblr.com") || host.equals("www.tumblr.com")) {
			URL forwardURL = null;
			if (url.getHost().equals("tmblr.co")) {
				final String tumblrCode = url.getPath();
				forwardURL = new URL("http://www.tumblr.com" + tumblrCode);
			}
			else {
				forwardURL = url;
			}
			// now get the location header
			final HttpURLConnection con = (HttpURLConnection) forwardURL.openConnection();
			con.setInstanceFollowRedirects(false);
			final String locStr = con.getHeaderField("Location");
			loc = new URL(locStr);
			con.disconnect();
		}

		// Now extract the post ID from the actual tumblr address
		final String[] parts = loc.getPath().split("[/]");
		String postID = null;
		for (int i = 0; i < parts.length; i++) {
			if (parts[i].equals("post")) {
				postID = parts[i + 1];
				break;
			}
		}
		return postID;
	}

}
