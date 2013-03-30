package org.openimaj.image.dataset;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import net.billylieurance.azuresearch.AbstractAzureSearchQuery.AZURESEARCH_FORMAT;
import net.billylieurance.azuresearch.AzureSearchImageQuery;
import net.billylieurance.azuresearch.AzureSearchImageResult;

import org.openimaj.data.dataset.ReadableListDataset;
import org.openimaj.image.Image;
import org.openimaj.io.HttpUtils;
import org.openimaj.io.ObjectReader;
import org.openimaj.util.api.auth.common.BingAPIToken;

/**
 * Image datasets dynamically created from the Bing search API.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 * @param <IMAGE>
 *            The type of {@link Image} instance held by the dataset.
 */
public class BingImageDataset<IMAGE extends Image<?, IMAGE>> extends ReadableListDataset<IMAGE> {
	List<AzureSearchImageResult> images;

	protected BingImageDataset(ObjectReader<IMAGE> reader, List<AzureSearchImageResult> results) {
		super(reader);
		this.images = results;
	}

	@Override
	public IMAGE getInstance(int index) {
		return read(getImage(index));
	}

	private IMAGE read(AzureSearchImageResult next) {
		if (next == null)
			return null;

		InputStream stream = null;
		try {
			final String imageURL = next.getMediaUrl();
			stream = HttpUtils.readURL(new URL(imageURL));

			return reader.read(stream);
		} catch (final IOException e) {
			throw new RuntimeException(e);
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
	 * Get the underlying {@link AzureSearchImageResult} objects that back the
	 * dataset.
	 * 
	 * @return the underlying {@link AzureSearchImageResult} objects
	 */
	public List<AzureSearchImageResult> getImages() {
		return images;
	}

	/**
	 * Get the specific underlying {@link AzureSearchImageResult} for the given
	 * index.
	 * 
	 * @param index
	 *            the index
	 * @return the specific {@link AzureSearchImageResult} for the given index.
	 */
	public AzureSearchImageResult getImage(int index) {
		return images.get(index);
	}

	private static List<AzureSearchImageResult> performSinglePageQuery(AzureSearchImageQuery query) {
		query.setFormat(AZURESEARCH_FORMAT.XML);
		query.doQuery();

		return query.getQueryResult().getASRs();
	}

	private static List<AzureSearchImageResult> performQuery(AzureSearchImageQuery query, int number) {
		if (number <= 0)
			number = 1000;

		query.setPage(0);
		query.setPerPage(50);
		query.setFormat(AZURESEARCH_FORMAT.XML);

		final List<AzureSearchImageResult> images = new ArrayList<AzureSearchImageResult>();
		for (int i = 0; i < 20; i++) {
			final List<AzureSearchImageResult> res = performSinglePageQuery(query);

			if (res == null || res.size() == 0)
				break;

			images.addAll(res);

			if (images.size() >= number)
				break;
		}

		if (images.size() <= number)
			return images;
		return images.subList(0, number);
	}

	/**
	 * Perform a search with the given query. The appid must have been set
	 * externally.
	 * 
	 * @see AzureSearchImageQuery#setAppid(String)
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
	public static <IMAGE extends Image<?, IMAGE>> BingImageDataset<IMAGE> create(ObjectReader<IMAGE> reader,
			AzureSearchImageQuery query, int number)
	{
		return new BingImageDataset<IMAGE>(reader, performQuery(query, number));
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
	public static <IMAGE extends Image<?, IMAGE>> BingImageDataset<IMAGE> create(ObjectReader<IMAGE> reader,
			BingAPIToken token, AzureSearchImageQuery query, int number)
	{
		query.setAppid(token.accountKey);
		return new BingImageDataset<IMAGE>(reader, performQuery(query, number));
	}

	/**
	 * Perform a search with the given query string and filters.
	 * 
	 * @param reader
	 *            the reader with which to load the images
	 * @param token
	 *            the api authentication token
	 * @param query
	 *            the query
	 * @param imageFilters
	 *            the image filters
	 * @param number
	 *            the target number of results; the resultant dataset may
	 *            contain fewer images than specified.
	 * @return a new {@link BingImageDataset} created from the query.
	 */
	public static <IMAGE extends Image<?, IMAGE>> BingImageDataset<IMAGE> create(ObjectReader<IMAGE> reader,
			BingAPIToken token, String query, String imageFilters, int number)
	{
		final AzureSearchImageQuery aq = new AzureSearchImageQuery();
		aq.setAppid(token.accountKey);
		aq.setQuery(query);
		if (imageFilters != null)
			aq.setImageFilters(imageFilters);

		return new BingImageDataset<IMAGE>(reader, performQuery(aq, number));
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
	public static <IMAGE extends Image<?, IMAGE>> BingImageDataset<IMAGE> create(ObjectReader<IMAGE> reader,
			BingAPIToken token, String query, int number)
	{
		final AzureSearchImageQuery aq = new AzureSearchImageQuery();
		aq.setAppid(token.accountKey);
		aq.setQuery(query);

		return new BingImageDataset<IMAGE>(reader, performQuery(aq, number));
	}
}
