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
package org.openimaj.demos.sandbox.vlad;

import gnu.trove.list.array.TLongArrayList;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.util.Map.Entry;

import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.Text;
import org.openimaj.hadoop.sequencefile.TextBytesSequenceFileUtility;
import org.openimaj.image.indexing.vlad.VLADIndexerData;
import org.openimaj.io.IOUtils;
import org.openimaj.knn.pq.IncrementalFloatADCNearestNeighbours;

public class FlickrIndexer {
	public static void convertCSV() throws IOException {
		final String CSV_REGEX = ",(?=(?:[^\"]*\"[^\"]*\")*(?![^\"]*\"))";
		final File csvFile = new File("/Volumes/Raid/FlickrCrawls/AllGeo16/images.csv");
		final File output = new File("/Users/jsh2/Desktop/flickr46m-id2lat-lng.map");

		DataOutputStream dos = null;
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(csvFile));
			dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(output)));

			String line;
			int i = 0;
			while ((line = br.readLine()) != null) {
				try {
					final String[] parts = line.split(CSV_REGEX);

					// final int farmID = Integer.parseInt(parts[0].trim());
					// final int serverID = Integer.parseInt(parts[1].trim());
					final long imageID = Long.parseLong(parts[2].trim());
					// final String secret = parts[3].trim();
					// final String url = parts[5].trim();
					final float lat = Float.parseFloat(parts[15].trim());
					final float lon = Float.parseFloat(parts[16].trim());

					dos.writeLong(imageID);
					dos.writeFloat(lat);
					dos.writeFloat(lon);
					// dos.writeInt(farmID);
					// dos.writeInt(serverID);
					// dos.writeUTF(secret);
				} catch (final Exception e) {
					// skip
				}
				if (i++ % 1000 == 0) {
					System.out.println("Read " + i + " records. " + Runtime.getRuntime().freeMemory());
				}
			}
		} finally {
			if (dos != null)
				dos.close();
			if (br != null)
				br.close();
		}
	}

	public static void extractSequenceFileData() throws IOException {
		final URI[] paths = TextBytesSequenceFileUtility
				.getFiles("hdfs://seurat/data/flickr-all-geo-vlad64-pca128-pq16x8-indexer-mirflickr25k-sift1x.seq",
						"part-m-");

		final File output = new File("/Volumes/My Book/flickr46m-vlad64-pca128-pq16x8-indexer-mirflickr25k-sift1x.dat");

		final DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(output)));

		final byte[] tmp = new byte[512];
		for (final URI p : paths) {
			System.out.println("Starting file " + p);

			final TextBytesSequenceFileUtility sf = new TextBytesSequenceFileUtility(p, true);

			for (final Entry<Text, BytesWritable> rec : sf) {
				final long id = Long.parseLong(rec.getKey().toString().trim());

				System.arraycopy(rec.getValue().getBytes(), 0, tmp, 0, tmp.length);

				dos.writeLong(id);
				dos.write(tmp);
			}
			dos.flush();
		}

		dos.close();
	}

	public static void createPQADCNN() throws IOException {
		final File input = new File("/Volumes/My Book/flickr46m-vlad64-pca128-pq16x8-indexer-mirflickr25k-sift1x.dat");

		final DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(input)));

		final VLADIndexerData indexer = VLADIndexerData.read(new File(
				"/Users/jsh2/vlad64-pca128-pq16x8-indexer-mirflickr25k-sift1x.dat"));
		final IncrementalFloatADCNearestNeighbours nn = new IncrementalFloatADCNearestNeighbours(
				indexer.getProductQuantiser(), 128,
				46000000);
		final TLongArrayList indexes = new TLongArrayList(46000000);
		try {
			final float[] farr = new float[128];

			for (int x = 0;; x++) {
				if (x % 100000 == 0)
					System.out.println(x);

				final long id = dis.readLong();

				for (int i = 0; i < 128; i++) {
					farr[i] = dis.readFloat();
				}

				nn.add(farr);
				indexes.add(id);
			}
		} catch (final EOFException e) {
			dis.close();
		}

		IOUtils.writeBinary(new File(
				"/Volumes/My Book/flickr46m-vlad64-pca128-pq16x8-indexer-mirflickr25k-sift1x-pqadcnn.dat"), nn);
		IOUtils.writeToFile(indexes, new File(
				"/Volumes/My Book/flickr46m-vlad64-pca128-pq16x8-indexer-mirflickr25k-sift1x-pqadcnn-indexes.dat"));
	}

	public static void main(String[] args) throws Exception {
		// convertCSV();
		// extractSequenceFileData();
		createPQADCNN();
	}
}
