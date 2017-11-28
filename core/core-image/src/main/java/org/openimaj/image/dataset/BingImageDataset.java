/**
 * Copyright (c) 2011, The University of Southampton and the individual contributors.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *   * 	Redistributions of source code must retain the above copyright notice,
 * 	this list of conditions and the following disclaimer.
 *
 *   *	Redistributions in binary form must reproduce the above copyright notice,
 * 	this list of conditions and the following disclaimer in the documentation
 * 	and/or other materials provided with the distribution.
 *
 *   *	Neither the name of the University of Southampton nor the names of its
 * 	contributors may be used to endorse or promote products derived from this
 * 	software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.openimaj.image.dataset;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.BackingStoreException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.openimaj.data.dataset.ReadableListDataset;
import org.openimaj.data.identity.Identifiable;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.Image;
import org.openimaj.image.ImageUtilities;
import org.openimaj.io.HttpUtils;
import org.openimaj.io.InputStreamObjectReader;
import org.openimaj.util.api.auth.DefaultTokenFactory;
import org.openimaj.util.api.auth.common.BingAPIToken;

/**
 * Image datasets dynamically created from the Bing search API.
 * 
 * <h5> WARNING </h5>
 * Some of the images inside this dataset may be set to {@code null}if they could not be loaded.
 *
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 * @param <IMAGE>
 *            The type of {@link Image} instance held by the dataset.
 */
public class BingImageDataset<IMAGE extends Image<?, IMAGE>> extends ReadableListDataset<IMAGE, InputStream>
		implements
		Identifiable
{
	public static class ImageDataSourceQuery {
		public static enum SafeSearch {
			Off, Moderate, Strict;
		}

		public static enum Aspect {
			Square, Wide, Tall, All;
		}

		public static enum Color {
			/**
			 * Return color images
			 */
			ColorOnly,
			/**
			 * Return black and white images
			 */
			Monochrome,
			Black,
			Blue,
			Brown,
			Gray,
			Green,
			Orange,
			Pink,
			Purple,
			Red,
			Teal,
			White,
			Yellow
		}

		public static enum Freshness {
			/**
			 * Return images discovered within the last 24 hours
			 */
			Day,
			/**
			 * Return images discovered within the last 7 days
			 */
			Week,
			/**
			 * Return images discovered within the last 30 days
			 */
			Month
		}

		/**
		 * Filter images by content
		 */
		public static enum ImageContent {
			/**
			 * Return images that show only a person's face
			 */
			Face,
			/**
			 * Return images that show only a person's head and shoulders
			 */
			Portrait
		}

		/**
		 * Filter images by image type.
		 */
		public static enum ImageType {
			/**
			 * Return only animated GIFs
			 */
			AnimatedGif,
			/**
			 * Return only clip art images
			 */
			Clipart,
			/**
			 * Return only line drawings
			 */
			Line,
			/**
			 * Return only photographs (excluding line drawings, animated Gifs,
			 * and clip art)
			 */
			Photo,
			/**
			 * Return only images that contain items where Bing knows of a
			 * merchant that is selling the items.
			 */
			Shopping,
		}

		public static enum License {
			/**
			 * Return images where the creator has waived their exclusive
			 * rights, to the fullest extent allowed by law.
			 */
			Public,
			/**
			 * Return images that may be shared with others. Changing or editing
			 * the image might not be allowed. Also, modifying, sharing, and
			 * using the image for commercial purposes might not be allowed.
			 * Typically, this option returns the most images.
			 */
			Share,
			/**
			 * Return images that may be shared with others for personal or
			 * commercial purposes. Changing or editing the image might not be
			 * allowed.
			 */
			ShareCommercially,
			/**
			 * Return images that may be modified, shared, and used. Changing or
			 * editing the image might not be allowed. Modifying, sharing, and
			 * using the image for commercial purposes might not be allowed.
			 */
			Modify,
			/**
			 * Return images that may be modified, shared, and used for personal
			 * or commercial purposes. Typically, this option returns the fewest
			 * images.
			 */
			ModifyCommercially,
			/**
			 * Do not filter by license type. Specifying this value is the same
			 * as not specifying the license parameter.
			 */
			All
		}

		public static enum Size {
			/**
			 * Return images that are less than 200x200 pixels
			 */
			Small,
			/**
			 * Return images that are greater than or equal to 200x200 pixels
			 * but less than 500x500 pixels
			 */
			Medium,
			/**
			 * Return images that are 500x500 pixels or larger
			 */
			Large,
			/**
			 * Return wallpaper images.
			 */
			Wallpaper,
			/**
			 * Do not filter by size. Specifying this value is the same as not
			 * specifying the size parameter.
			 */
			All
		}

		SafeSearch safeSearch;
		Aspect aspect;
		Color color;
		Freshness freshness;
		int height;
		ImageContent imageContent;
		ImageType imageType;
		License license;
		Size size;
		int width;
		int offset;
		int count;
		String query;
		private String accountKey;

		/**
		 * @return the safeSearch
		 */
		public SafeSearch getSafeSearch() {
			return safeSearch;
		}

		/**
		 * @param safeSearch
		 *            the safeSearch to set
		 */
		public void setSafeSearch(SafeSearch safeSearch) {
			this.safeSearch = safeSearch;
		}

		/**
		 * @return the aspect
		 */
		public Aspect getAspect() {
			return aspect;
		}

		/**
		 * @param aspect
		 *            the aspect to set
		 */
		public void setAspect(Aspect aspect) {
			this.aspect = aspect;
		}

		/**
		 * @return the color
		 */
		public Color getColor() {
			return color;
		}

		/**
		 * @param color
		 *            the color to set
		 */
		public void setColor(Color color) {
			this.color = color;
		}

		/**
		 * @return the freshness
		 */
		public Freshness getFreshness() {
			return freshness;
		}

		/**
		 * @param freshness
		 *            the freshness to set
		 */
		public void setFreshness(Freshness freshness) {
			this.freshness = freshness;
		}

		/**
		 * @return the height
		 */
		public int getHeight() {
			return height;
		}

		/**
		 * @param height
		 *            the height to set
		 */
		public void setHeight(int height) {
			this.height = height;
		}

		/**
		 * @return the imageContent
		 */
		public ImageContent getImageContent() {
			return imageContent;
		}

		/**
		 * @param imageContent
		 *            the imageContent to set
		 */
		public void setImageContent(ImageContent imageContent) {
			this.imageContent = imageContent;
		}

		/**
		 * @return the imageType
		 */
		public ImageType getImageType() {
			return imageType;
		}

		/**
		 * @param imageType
		 *            the imageType to set
		 */
		public void setImageType(ImageType imageType) {
			this.imageType = imageType;
		}

		/**
		 * @return the license
		 */
		public License getLicense() {
			return license;
		}

		/**
		 * @param license
		 *            the license to set
		 */
		public void setLicense(License license) {
			this.license = license;
		}

		/**
		 * @return the size
		 */
		public Size getSize() {
			return size;
		}

		/**
		 * @param size
		 *            the size to set
		 */
		public void setSize(Size size) {
			this.size = size;
		}

		/**
		 * @return the width
		 */
		public int getWidth() {
			return width;
		}

		/**
		 * @param width
		 *            the width to set
		 */
		public void setWidth(int width) {
			this.width = width;
		}

		/**
		 * @return the offset
		 */
		public int getOffset() {
			return offset;
		}

		/**
		 * @param offset
		 *            the offset to set
		 */
		public void setOffset(int offset) {
			this.offset = offset;
		}

		/**
		 * @return the count
		 */
		public int getCount() {
			return count;
		}

		/**
		 * @param count
		 *            the count to set
		 */
		public void setCount(int count) {
			this.count = count;
		}

		/**
		 * @return the query
		 */
		public String getQuery() {
			return query;
		}

		/**
		 * @param query
		 *            the query to set
		 */
		public void setQuery(String query) {
			this.query = query;
		}

		public void setSubscriptionKey(String accountKey) {
			this.accountKey = accountKey;
		}

		public URI buildURI() throws URISyntaxException {
			final URIBuilder builder = new URIBuilder("https://api.cognitive.microsoft.com/bing/v7.0/images/search");

			builder.setParameter("q", query);
			builder.setParameter("count", count + "");
			builder.setParameter("offset", offset + "");

			return builder.build();
		}

	}

	public static class ImageDataSourceResponse {
		String contentUrl;

		public ImageDataSourceResponse(JSONObject jro) {
			contentUrl = (String) jro.get("contentUrl");
		}

		public String getContentUrl() {
			return contentUrl;
		}
	}

	List<ImageDataSourceResponse> images;
	ImageDataSourceQuery query;

	protected BingImageDataset(InputStreamObjectReader<IMAGE> reader, List<ImageDataSourceResponse> results,
			ImageDataSourceQuery query)
	{
		super(reader);
		this.images = results;
		this.query = query;
	}

	@Override
	public IMAGE getInstance(int index) {
		return read(getImage(index));
	}

	/**
	 * Loads the image in {@code next} and converts it to the type {@code <IMAGE>}
	 * @param next the image source to load the image from
	 * @return the loaded and converted image if loading the image worked,
	 *         {@code null} otherwise
	 */
	private IMAGE read(ImageDataSourceResponse next) {
		if (next == null)
			return null;

		final String imageURL = next.getContentUrl();
		
		InputStream stream = null;
		try {
			stream = HttpUtils.readURL(new URL(imageURL));

			return reader.read(stream);
		} catch (final MalformedURLException e) {
			//if the URL is malformed, something went wrong with programming
			throw new RuntimeException(e);
		} catch (final IOException e) {
			if (e.getCause() instanceof org.apache.sanselan.ImageReadException) {
				// image urls that redirect to html pages will have this error (eg tinypic.com)
				System.out.println("The following URL didn't redirect to an image: " + imageURL);
			} else {
				// there was some issue with loading data from the URL
				e.printStackTrace();
			}
			return null;
		} finally {
			try {
				if (stream != null)
					stream.close();
			} catch (final IOException e) {
				// ignore
			}
		}
	}

	@Override
	public int numInstances() {
		return images.size();
	}

	/**
	 * Get the underlying {@link ImageDataSourceResponse} objects that back the
	 * dataset.
	 *
	 * @return the underlying {@link ImageDataSourceResponse} objects
	 */
	public List<ImageDataSourceResponse> getImages() {
		return images;
	}

	/**
	 * Get the specific underlying {@link ImageDataSourceResponse} for the given
	 * index.
	 *
	 * @param index
	 *            the index
	 * @return the specific {@link ImageDataSourceResponse} for the given index.
	 */
	public ImageDataSourceResponse getImage(int index) {
		return images.get(index);
	}

	private static List<ImageDataSourceResponse> performSinglePageQuery(ImageDataSourceQuery query)
	{
		final HttpClient httpclient = HttpClients.createDefault();

		try
		{
			final URI uri = query.buildURI();
			final HttpGet request = new HttpGet(uri);
			request.setHeader("Ocp-Apim-Subscription-Key", query.accountKey);

			final HttpResponse response = httpclient.execute(request);
			
			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
				throw new IOException("HTTP ERROR 401: Unauthorized Recieved. "
						+ "You probably have the incorrect API Key");
			}
			final HttpEntity entity = response.getEntity();

			if (entity != null)
			{
				try {
					final JSONParser parser = new JSONParser();
					final JSONObject o = (JSONObject) parser.parse(EntityUtils.toString(entity));

					final JSONArray jresults = ((JSONArray) o.get("value"));
					final List<ImageDataSourceResponse> results = new ArrayList<>(jresults.size());

					for (final Object jro : jresults) {
						results.add(new ImageDataSourceResponse((JSONObject) jro));
					}

					return results;
				} catch (final Exception e) {
					e.printStackTrace();
				}
			}
		} catch (final IOException e) {
			e.printStackTrace();
		} catch (final URISyntaxException e) {
			e.printStackTrace();
		}

		return null;
	}

	private static List<ImageDataSourceResponse> performQuery(ImageDataSourceQuery query, int number) {
		if (number <= 0)
			number = 1000;

		query.setOffset(0);
		query.setCount(50);

		final List<ImageDataSourceResponse> images = new ArrayList<ImageDataSourceResponse>();
		for (int i = 0; i < 20; i++) {
			final List<ImageDataSourceResponse> res = performSinglePageQuery(query);

			if (res == null || res.size() == 0)
				break;

			images.addAll(res);

			if (images.size() >= number)
				break;

			query.setOffset(query.getOffset() + 50);
		}

		if (images.size() <= number)
			return images;
		return images.subList(0, number);
	}

	/**
	 * Perform a search with the given query. The appid must have been set
	 * externally.
	 *
	 *
	 * @param reader
	 *            the reader with which to load the images
	 * @param query
	 *            the query
	 * @param number
	 *            the target number of results; the resultant dataset may
	 *            contain fewer images than specified.
	 * @return a new {@link BingImageDataset} created from the query.
	 */
	public static <IMAGE extends Image<?, IMAGE>> BingImageDataset<IMAGE> create(InputStreamObjectReader<IMAGE> reader,
			ImageDataSourceQuery query, int number)
	{
		return new BingImageDataset<IMAGE>(reader, performQuery(query, number), query);
	}

	/**
	 * Perform a search with the given query. The given api token will be used
	 * to set the appid in the query object.
	 *
	 * @param reader
	 *            the reader with which to load the images
	 * @param token
	 *            the api authentication token
	 * @param query
	 *            the query
	 * @param number
	 *            the target number of results; the resultant dataset may
	 *            contain fewer images than specified.
	 * @return a new {@link BingImageDataset} created from the query.
	 */
	public static <IMAGE extends Image<?, IMAGE>> BingImageDataset<IMAGE> create(InputStreamObjectReader<IMAGE> reader,
			BingAPIToken token, ImageDataSourceQuery query, int number)
	{
		query.setSubscriptionKey(token.accountKey);
		return new BingImageDataset<IMAGE>(reader, performQuery(query, number), query);
	}

	/**
	 * Perform a search with the given query string.
	 *
	 * @param reader
	 *            the reader with which to load the images
	 * @param token
	 *            the api authentication token
	 * @param query
	 *            the query
	 * @param number
	 *            the target number of results; the resultant dataset may
	 *            contain fewer images than specified.
	 * @return a new {@link BingImageDataset} created from the query.
	 */
	public static <IMAGE extends Image<?, IMAGE>> BingImageDataset<IMAGE> create(InputStreamObjectReader<IMAGE> reader,
			BingAPIToken token, String query, int number)
	{
		final ImageDataSourceQuery aq = new ImageDataSourceQuery();
		aq.setSubscriptionKey(token.accountKey);
		aq.setQuery(query);

		return new BingImageDataset<IMAGE>(reader, performQuery(aq, number), aq);
	}

	@Override
	public String getID() {
		return query.getQuery();
	}

	public static void main(String[] args) throws BackingStoreException {
		final BingAPIToken apiToken = DefaultTokenFactory.get(BingAPIToken.class);
		final BingImageDataset<FImage> ds = BingImageDataset
				.create(ImageUtilities.FIMAGE_READER, apiToken, "foo", 10);

		DisplayUtilities.display(ds.getRandomInstance());
	}
}
