package org.openimaj.demos.mediaeval13.placing;

import gnu.trove.list.array.TLongArrayList;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.MMapDirectory;
import org.openimaj.image.FImage;

public class Utils {
	private Utils() {
	}

	/**
	 * Read the lat-lng file into a list of {@link GeoLocation}s
	 * 
	 * @param latlngFile
	 * @param skipIds
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static List<GeoLocation> readLatLng(File latlngFile, TLongArrayList skipIds) throws FileNotFoundException,
			IOException
	{
		final ArrayList<GeoLocation> pts = new ArrayList<GeoLocation>();
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(latlngFile));

			String line;
			while ((line = br.readLine()) != null) {
				try {
					final String[] parts = line.split(" ");

					if (parts.length != 3)
						continue;

					final long id = Long.parseLong(parts[0]);
					final double lat = Double.parseDouble(parts[1]);
					final double lng = Double.parseDouble(parts[2]);

					if (skipIds.binarySearch(id) < 0)
						pts.add(new GeoLocation(lat, lng));
				} catch (final NumberFormatException nfe) {
					// ignore line
				}
			}
		} finally {
			if (br != null)
				br.close();
		}
		return pts;
	}

	public static FImage createPrior(File latlngFile, TLongArrayList skipIds) throws IOException {
		return createPrior(latlngFile, skipIds, true);
	}

	public static FImage createPrior(File latlngFile, TLongArrayList skipIds, boolean norm) throws IOException {
		final FImage img = new FImage(360, 180);
		img.fill(1f / (img.height * img.width));

		if (latlngFile == null)
			return img;

		final BufferedReader br = new BufferedReader(new FileReader(latlngFile));

		String line;
		br.readLine();
		while ((line = br.readLine()) != null) {
			final String[] parts = line.split(" ");

			if (skipIds.contains(Long.parseLong(parts[0])))
				continue;

			final float x = Float.parseFloat(parts[2]) + 180;
			final float y = 90 - Float.parseFloat(parts[1]);

			img.pixels[(int) (y * img.height / 180.001)][(int) (x * img.width / 360.001)]++;
		}
		br.close();

		if (norm)
			logNorm(img);

		return img;
	}

	public static void logNorm(final FImage img) {
		final double norm = img.sum();
		for (int y = 0; y < img.height; y++)
			for (int x = 0; x < img.width; x++)
				img.pixels[y][x] = (float) Math.log(img.pixels[y][x] / norm);
	}

	public static IndexSearcher loadLuceneIndex(File file) throws IOException {
		final Directory directory = new MMapDirectory(file);
		final IndexReader reader = DirectoryReader.open(directory);
		final IndexSearcher luceneIndex = new IndexSearcher(reader);
		return luceneIndex;
	}
}
