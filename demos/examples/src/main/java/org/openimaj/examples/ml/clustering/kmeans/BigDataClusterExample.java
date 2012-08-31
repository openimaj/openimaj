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
		kmeans.cluster(ds);

		// Now the cluster is created you can do things with it...
		// See HierarchicalKMeansExample for some examples.
		System.out.println("Done");
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
			raf = new RandomAccessFile(file, "r");
			numItems = raf.readInt();
			dimensionality = raf.readInt();
		}

		@Override
		public synchronized void getData(int startRow, int stopRow, byte[][] data) {
			System.out.println("Getting data for rows " + startRow + " to " + stopRow);
			try {
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
				final byte[] data = new byte[dimensionality];
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
		public int numRows() {
			return numItems;
		}

		@Override
		public void close() throws IOException {
			raf.close();
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
