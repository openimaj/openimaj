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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.Logger;

import com.google.gson.Gson;

/**
 * An imgur client has the various functionality of the imgur.com api
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 */
public class ImgurClient {
	/**
	 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
	 * 
	 */
	public static enum ImgurType {
		/**
		 * /a/hash
		 */
		ALBUM,
		/**
		 * an image
		 */
		IMAGE,
		/**
		 * a call to the raw imgur gallery
		 */
		GALLERY
	}

	/**
	 * The type and hash usually returned from
	 * {@link ImgurClient#imgurURLtoHash(URL)}
	 * 
	 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
	 * 
	 */
	public static class ImgurTypeHash {

		protected ImgurTypeHash(ImgurType type, String hash) {
			this.hash = hash;
			this.type = type;
		}

		/**
		 * the {@link ImgurType}
		 */
		public ImgurType type;
		/**
		 * the hash code, might be null if {@link ImgurType} is GALLERY
		 */
		public String hash;

		@Override
		public String toString() {
			return String.format("Imgur [%s] %s", type, hash);
		}
	}

	/**
	 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
	 * 
	 */
	private static class ImgurResponse {
		AlbumImgurResponse album;
		ImageResponse image;
	}

	/**
	 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
	 * 
	 */
	private static class AlbumImgurResponse {
		List<ImageResponse> images;
	}

	/**
	 * An image response is composed of two Maps, one describing the image and
	 * another describing its links
	 * 
	 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
	 * 
	 */
	public static class ImageResponse {
		/**
		 * Image metadata
		 */
		public Map<String, Object> image;
		/**
		 * various links
		 */
		public Map<String, Object> links;

		/**
		 * @return return links.original from the imgur API response
		 */
		public URL getOriginalLink() {
			final String orig = (String) links.get("original");
			if (orig == null)
				return null;
			try {
				return new URL(orig);
			} catch (final MalformedURLException e) {
			}
			return null;
		}
	}

	private final static Pattern hashPattern = Pattern.compile("(^[a-zA-Z0-9]+)");

	private String apiKey;
	private DefaultHttpClient client;

	private static String ENDPOINT = "http://api.imgur.com/2";
	private transient Gson gson = new Gson();

	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(ImgurClient.class);

	/**
	 * 
	 */
	public ImgurClient() {
		this.apiKey = System.getProperty("imgur.api_key");
		if (apiKey == null) {
			// Anonymous api key for Sina Samangooei
			apiKey = "fecf663ef507f598e8119451e17a6c29";
		}
		client = new DefaultHttpClient();
	}

	/**
	 * Depending on the {@link ImgurTypeHash} instance calls
	 * {@link #getSingleImage(String)}, {@link #getAlbumImages(String)} or
	 * {@link #getGalleryImages()}
	 * 
	 * @param typehash
	 * @return a list of {@link ImageResponse} instances
	 * @throws IOException
	 * @throws ClientProtocolException
	 */
	public List<ImageResponse> getImages(ImgurTypeHash typehash) throws ClientProtocolException, IOException {
		final List<ImageResponse> ret = new ArrayList<ImageResponse>();
		switch (typehash.type) {
		case IMAGE:
			ret.add(getSingleImage(typehash.hash));
			break;
		case ALBUM:
			ret.addAll(getAlbumImages(typehash.hash));
			break;
		case GALLERY:
			ret.addAll(getGalleryImages());
			break;
		}
		return ret;
	}

	/**
	 * Calls http://api.imgur.com/2/image/:HASH
	 * 
	 * @param hash
	 * @return the json response
	 * @throws IOException
	 * @throws ClientProtocolException
	 */
	public ImageResponse getSingleImage(String hash) throws ClientProtocolException, IOException {
		final HttpGet get = new HttpGet(String.format("%s/image/%s.json", ENDPOINT, hash));
		final HttpResponse response = client.execute(get);
		final ImgurResponse resp = gson.fromJson(new InputStreamReader(response.getEntity().getContent()),
				ImgurResponse.class);
		return resp.image;
	}

	/**
	 * Calls http://api.imgur.com/2/album/:ID
	 * 
	 * @param hash
	 * @return the json response
	 * @throws IOException
	 * @throws ClientProtocolException
	 */
	public List<ImageResponse> getAlbumImages(String hash) throws ClientProtocolException, IOException {
		final HttpGet get = new HttpGet(String.format("%s/album/%s.json", ENDPOINT, hash));
		final HttpResponse response = client.execute(get);
		final ImgurResponse resp = gson.fromJson(new InputStreamReader(response.getEntity().getContent()),
				ImgurResponse.class);
		return resp.album.images;
	}

	/**
	 * Calls http://imgur.com/gallery.json
	 * 
	 * @return the json response
	 */
	public List<ImageResponse> getGalleryImages() {
		return null;
	}

	/**
	 * @param url
	 * @return the imgur type and hash, or null if the URL was too tricky
	 */
	public static ImgurTypeHash imgurURLtoHash(URL url) {
		if (!url.getHost().contains("imgur"))
			return null;
		final String path = url.getPath();
		final String[] split = path.split("[/]+");
		if (split.length == 0)
			return null;
		else if (split.length == 2) {
			if (split[1].equals("gallery"))
				return new ImgurTypeHash(ImgurType.GALLERY, null);
			else {
				final Matcher matcher = hashPattern.matcher(split[1]);
				if (matcher.find())
				{
					final String hash = split[1].substring(0, matcher.end());
					return new ImgurTypeHash(ImgurType.IMAGE, hash);
				}
				return null;
			}
		}
		else {
			final String hashPart = split[split.length - 1];
			final String typePart = split[split.length - 2];
			ImgurType type = ImgurType.IMAGE;
			if (typePart.equals("a"))
				type = ImgurType.ALBUM;

			final Matcher matcher = hashPattern.matcher(hashPart);
			matcher.find();
			final String hash = hashPart.substring(0, matcher.end());
			return new ImgurTypeHash(type, hash);
		}

	}
}
