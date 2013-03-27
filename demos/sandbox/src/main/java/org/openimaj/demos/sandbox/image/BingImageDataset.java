package org.openimaj.demos.sandbox.image;

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

	public List<AzureSearchImageResult> getImages() {
		return images;
	}

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

	public static <IMAGE extends Image<?, IMAGE>> BingImageDataset<IMAGE> create(ObjectReader<IMAGE> reader,
			AzureSearchImageQuery query, int number)
	{
		return new BingImageDataset<IMAGE>(reader, performQuery(query, number));
	}

	public static <IMAGE extends Image<?, IMAGE>> BingImageDataset<IMAGE> create(ObjectReader<IMAGE> reader,
			String appid, String query, String imageFilters, int number)
	{
		final AzureSearchImageQuery aq = new AzureSearchImageQuery();
		aq.setAppid(appid);
		aq.setQuery(query);
		aq.setImageFilters(imageFilters);

		return new BingImageDataset<IMAGE>(reader, performQuery(aq, number));
	}

	public static <IMAGE extends Image<?, IMAGE>> BingImageDataset<IMAGE> create(ObjectReader<IMAGE> reader,
			String appid, String query, int number)
	{
		final AzureSearchImageQuery aq = new AzureSearchImageQuery();
		aq.setAppid(appid);
		aq.setQuery(query);

		return new BingImageDataset<IMAGE>(reader, performQuery(aq, number));
	}
}
