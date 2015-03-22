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
package org.openimaj.examples.ml.clustering.kmeans;

import java.io.Closeable;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

import org.openimaj.data.AbstractDataSource;
import org.openimaj.data.DataSource;
import org.openimaj.data.RandomData;
import org.openimaj.ml.clustering.kmeans.HierarchicalByteKMeans;
import org.openimaj.ml.clustering.kmeans.HierarchicalByteKMeansResult;

/**
 * Example showing how to use OpenIMAJ to cluster data that won't fit in memory
 * using a {@link DataSource} that reads data from disk. Hierarchical KMeans
 * clustering is demonstrated, but exact and approximate K-Means can also be
 * used.
 *
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class BigDataClusterExample {
	/**
	 * Main method for the example.
	 *
	 * @param args
	 *            Ignored.
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		// Set up the variables needed to define the clustering operation
		final int dimensionality = 100;
		final int numItems = 10000;
		final int clustersPerNode = 4;
		final int depth = 2;

		// Create the clusterer; there are specific types for all kinds of data
		// (we're using byte data here).
		final HierarchicalByteKMeans kmeans = new HierarchicalByteKMeans(dimensionality, clustersPerNode, depth);

		// Generate a file with some random data
		System.out.println("Generating Data");
		final File dataFile = createDataFile(dimensionality, numItems);

		// Create a datasource for the data
		System.out.println("Creating DataSource");
		final ExampleDatasource ds = new ExampleDatasource(dataFile);

		// Perform the clustering
		System.out.println("Clustering");
		final HierarchicalByteKMeansResult cluster = kmeans.cluster(ds);

		// As we're done with the datasource, we should close it
		ds.close();

		// Now the cluster is created you can do things with it...
		// See HierarchicalKMeansExample for some examples.
		System.out.println("Done");
		System.out.println(cluster);
	}

	/**
	 * An example datasource backed by a file of the format created by
	 * {@link #createDataFile}. Note that the {@link #getData(int)} and
	 * {@link #getData(int, int, byte[][])} are synchronized to ensure that
	 * multiple threads don't interfere with the underlying
	 * {@link RandomAccessFile}.
	 *
	 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
	 *
	 */
	static class ExampleDatasource extends AbstractDataSource<byte[]> implements Closeable {
		/**
		 * The header of the file is 2 4-bytes integers
		 */
		private static final int HEADER_BYTES = 8;

		private RandomAccessFile raf;
		private final int numItems;
		private final int dimensionality;

		public ExampleDatasource(File file) throws IOException {
			// open the file and read the header
			raf = new RandomAccessFile(file, "r");
			numItems = raf.readInt();
			dimensionality = raf.readInt();
		}

		@Override
		public synchronized void getData(int startRow, int stopRow, byte[][] data) {
			try {
				// seek to the location of the start-row and read the data
				raf.seek(HEADER_BYTES + startRow * dimensionality);
				for (int i = 0; i < stopRow - startRow; i++)
					raf.read(data[i], 0, dimensionality);

			} catch (final IOException e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public synchronized byte[] getData(int row) {
			try {
				// allocate data
				final byte[] data = new byte[dimensionality];

				// seek to the row and read
				raf.seek(HEADER_BYTES + row * dimensionality);
				raf.read(data);
				return data;
			} catch (final IOException e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public int numDimensions() {
			return dimensionality;
		}

		@Override
		public int size() {
			return numItems;
		}

		@Override
		public void close() throws IOException {
			raf.close();
		}

		@Override
		public byte[][] createTemporaryArray(int size) {
			return new byte[size][dimensionality];
		}
	}

	/**
	 * Write some randomly generated vectors to a temporary file.
	 * <p>
	 * The file format is simple: there is a two integer header representing the
	 * number of vectors and dimensionality. The remainder of the file is the
	 * vector data, one vector at a time, with each vector encoded as
	 * <code>dimensionality</code> bytes.
	 *
	 * @param dimensionality
	 *            length of the vectors
	 * @param numItems
	 *            number of vectors
	 * @return the file that was created
	 * @throws IOException
	 *             if an error occurs
	 */
	static File createDataFile(int dimensionality, int numItems) throws IOException {
		final File file = File.createTempFile("clusteringExampleData", ".txt");
		file.deleteOnExit();

		FileOutputStream fos = null;
		DataOutputStream dos = null;
		try {
			fos = new FileOutputStream(file);
			dos = new DataOutputStream(fos);

			dos.writeInt(numItems);
			dos.writeInt(dimensionality);

			for (int i = 0; i < numItems; i++) {
				final byte[] vector = RandomData.getRandomByteArray(dimensionality, Byte.MIN_VALUE, Byte.MAX_VALUE);

				for (int j = 0; j < dimensionality; j++) {
					dos.writeByte(vector[j]);
				}
			}
		} finally {
			if (dos != null)
				dos.close();
			if (fos != null)
				fos.close();
		}

		return file;
	}
}
