package org.openimaj.demos.sandbox.flickr.geo;

import gnu.trove.map.hash.TObjectIntHashMap;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CSVReader {
	final static String CVS_REGEX = ",(?=(?:[^\"]*\"[^\"]*\")*(?![^\"]*\"))";

	static class Record {
		long photoID;
		int farmID;
		int serverID;
		float lat;
		float lon;

		public static Record parseLine(String line, TObjectIntHashMap<String> vocabulary) {
			final String[] parts = line.split(CVS_REGEX);

			try {
				final Record r = new Record();
				r.farmID = Integer.parseInt(parts[0].trim());
				r.serverID = Integer.parseInt(parts[1].trim());
				r.photoID = Long.parseLong(parts[2].trim());
				r.lat = Float.parseFloat(parts[15].trim());
				r.lon = Float.parseFloat(parts[16].trim());

				String tags = parts[17].trim();
				if (tags.startsWith("\"[")) {
					tags = tags.substring(2, parts[17].length() - 3);
				} else {
					tags = tags.substring(1, parts[17].length() - 2);
				}
				if (tags.length() > 2) {
					final String[] tagParts = tags.split(CVS_REGEX);

					for (final String s : tagParts) {
						vocabulary.adjustOrPutValue(s, 1, 1);
					}
				}

				return r;
			} catch (final NumberFormatException e) {
				return null;
			}
		}
	}

	public static void main(String[] args) throws IOException {
		final File f = new File("/Volumes/Raid/FlickrCrawls/AllGeo16/images.csv");
		final List<Record> recs = new ArrayList<Record>();

		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(f));

			String line;
			int i = 0;
			final TObjectIntHashMap<String> vocab = new TObjectIntHashMap<String>(1000000);
			while ((line = br.readLine()) != null) {
				final Record r = Record.parseLine(line, vocab);
				if (r != null) {
					// recs.add(r);
					i++;
				}

				if (i % 1000 == 0) {
					System.out.println("Read " + i + " records. Vocab size: " + vocab.size());
				}
			}
		} finally {
			if (br != null)
				br.close();
		}
	}
}
