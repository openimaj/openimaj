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
