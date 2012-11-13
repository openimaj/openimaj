package org.openimaj.demos.sandbox.flickr.geo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.ByteBuffer;

import krati.core.StoreConfig;
import krati.core.segment.MappedSegmentFactory;
import krati.store.DynamicDataStore;

public class CSVReader {
	final static String CSV_REGEX = ",(?=(?:[^\"]*\"[^\"]*\")*(?![^\"]*\"))";

	static class Record {
		long photoID;
		int farmID;
		int serverID;
		float lat;
		float lon;
		String[] terms;

		public static Record parseLine(String line) {
			final String[] parts = line.split(CSV_REGEX);

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
					r.terms = tags.split(CSV_REGEX);
				}

				return r;
			} catch (final NumberFormatException e) {
				return null;
			}
		}
	}

	DynamicDataStore keywordStore;
	DynamicDataStore recordStore;
	int count = 0;

	public static void buildIndex(File indexLocation, File CSVFile) throws Exception {
		final CSVReader reader = new CSVReader();
		reader.initStores(indexLocation);
		reader.index(CSVFile);
		reader.closeStores();
	}

	private void index(File csvFile) throws Exception {
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(csvFile));

			String line;
			int i = 0;
			while ((line = br.readLine()) != null) {
				final Record r = Record.parseLine(line);
				if (r != null) {

					writeRecord(r);

					i++;
				}

				if (i % 1000 == 0) {
					System.out.println("Read " + i + " records.");
				}
			}
		} finally {
			if (br != null)
				br.close();
		}
	}

	private void writeRecord(Record r) throws Exception {
		final int nTerms = r.terms == null ? 0 : r.terms.length;
		final int[] hashes = new int[nTerms];

		final ByteBuffer keyBuffer = ByteBuffer.allocate(4);
		for (int i = 0; i < nTerms; i++) {
			final String term = r.terms[i];
			hashes[i] = term.hashCode();

			// write keywords:
			keyBuffer.rewind();
			keyBuffer.putInt(hashes[i]);
			keywordStore.put(keyBuffer.array(), term.getBytes("UTF-8"));
		}

		final int recSize = (nTerms * 4) + 28;
		final ByteBuffer valueBuffer = ByteBuffer.allocate(recSize);
		valueBuffer.putLong(r.photoID);
		valueBuffer.putInt(r.farmID);
		valueBuffer.putInt(r.serverID);
		valueBuffer.putFloat(r.lat);
		valueBuffer.putFloat(r.lon);

		valueBuffer.putInt(nTerms);
		for (int i = 0; i < nTerms; i++) {
			valueBuffer.putInt(hashes[i]);
		}

		keyBuffer.rewind();
		keyBuffer.putInt(count);
		recordStore.put(keyBuffer.array(), valueBuffer.array());
		count++;
	}

	private void initStores(File indexLocation) throws Exception {
		final File keywordStoreLocation = new File(indexLocation, "keywords");
		final File recordStoreLocation = new File(indexLocation, "records");

		keywordStoreLocation.mkdirs();
		final StoreConfig keywordStoreConf = new StoreConfig(keywordStoreLocation, 10000000);
		keywordStoreConf.setSegmentFactory(new MappedSegmentFactory());
		keywordStore = new DynamicDataStore(keywordStoreConf);

		recordStoreLocation.mkdirs();
		final StoreConfig recordStoreConf = new StoreConfig(recordStoreLocation, 10000000);
		recordStoreConf.setSegmentFactory(new MappedSegmentFactory());
		recordStore = new DynamicDataStore(recordStoreConf);
	}

	private void closeStores() throws Exception {
		keywordStore.rehash();
		keywordStore.close();

		recordStore.rehash();
		recordStore.close();
	}

	public static void main(String[] args) throws Exception {
		final File index = new File("/Users/jsh2/Desktop/flickrData.idx");
		final File csv = new File("/Volumes/Raid/FlickrCrawls/AllGeo16/images.csv");

		buildIndex(index, csv);
	}
}
