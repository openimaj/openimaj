package org.openimaj.demos.sandbox.vlad;

import gnu.trove.list.array.TLongArrayList;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URI;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.UriBuilder;

import org.glassfish.grizzly.http.server.HttpServer;
import org.openimaj.io.IOUtils;
import org.openimaj.knn.pq.IncrementalFloatADCNearestNeighbours;
import org.openimaj.util.array.ArrayUtils;

import scala.actors.threadpool.Arrays;

import com.sun.jersey.api.container.grizzly2.GrizzlyServerFactory;
import com.sun.jersey.api.core.ClassNamesResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;

@Path("/search")
public class FlickrServerTest {
	static IncrementalFloatADCNearestNeighbours nn;
	static long[] indexes;
	static long[] offsets;
	static String imageFormat;
	static RandomAccessFile featureData;
	static RandomAccessFile imageData;

	static {
		try {
			System.out.println("Loading NN");

			nn = IOUtils.read(new File("/Volumes/My Book/flickr46m-vlad-pqadcnn.dat"),
					IncrementalFloatADCNearestNeighbours.class);

			System.out.println("Loading index");
			indexes = ((TLongArrayList) IOUtils.readFromFile(new File(
					"/Volumes/My Book/flickr46m-vlad-pqadcnn-indexes.dat"))).toArray();

			imageFormat = "http://farm%d.staticflickr.com/%d/%d_%s.jpg";

			System.out.println("Creating RAFs");
			featureData = new RandomAccessFile("/Volumes/My Book/flickr46m-vlad.dat", "r");
			imageData = new RandomAccessFile("/Volumes/My Book/flickr46m-id2farm-server-secret.map", "r");

			System.out.println("Building index");
			offsets = buildIndex();
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	private float[] getFeature(int i) throws IOException {
		featureData.seek(i * (Long.SIZE + 128 * Float.SIZE));
		System.out.println(featureData.readLong());

		final float[] data = new float[128];
		for (int j = 0; j < 128; j++)
			data[j] = featureData.readFloat();

		return data;
	}

	private static long[] buildIndex() throws IOException {
		final TLongArrayList aIds = new TLongArrayList(46000000);
		final TLongArrayList aoffsets = new TLongArrayList(46000000);

		final File input = new File("/Volumes/My Book/flickr46m-id2farm-server-secret.map");

		final DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(input)));

		try {
			int offset = 0;
			for (int i = 0;; i++) {
				if (i % 1000000 == 0)
					System.out.println(i);

				final long currentOffset = offset;
				final long id = dis.readLong();
				offset += 8;
				dis.readInt();
				offset += 4;
				dis.readInt();
				offset += 4;

				final int len = dis.readUnsignedShort();
				final byte[] tmp = new byte[len];
				dis.readFully(tmp);
				offset += len + 2;

				aIds.add(id);
				aoffsets.add(currentOffset);
			}
		} catch (final EOFException e) {
			dis.close();
		}

		final long[] ids = aIds.toArray();
		final long[] offsets = aoffsets.toArray();

		ArrayUtils.parallelQuicksortAscending(ids, offsets);

		final long[] indexedOffsets = new long[indexes.length];

		for (int i = 0; i < indexedOffsets.length; i++) {
			final long indexedId = indexes[i];

			if (i % 1000000 == 0)
				System.out.println(i);

			indexedOffsets[i] = offsets[Arrays.binarySearch(ids, indexedId)];
		}

		return indexedOffsets;
	}

	@GET
	@Produces("text/html")
	public String search(@QueryParam("i") int i) throws IOException
	{
		System.out.println("Searching for " + i);

		final int length = 100;
		final int[][] argmins = new int[1][length];
		final float[][] mins = new float[1][length];

		nn.searchKNN(new float[][] { getFeature(i) }, length, argmins, mins);

		final StringBuffer sb = new StringBuffer();
		sb.append("<html><body>");
		for (int j = 0; j < length; j++) {
			sb.append("<a href=\"/search?i=" + argmins[0][j] + "\">");
			sb.append("<img width=\"100\" src=\"" + getImageId(argmins[0][j]) + "\"/>");
			sb.append("</a>");
		}
		sb.append("</body></html>");

		return sb.toString();
	}

	private String getImageId(int i) throws IOException {
		final long seekId = indexes[i];
		final long offset = offsets[i];

		imageData.seek(offset);
		final long id = imageData.readLong();
		final int farm = imageData.readInt();
		final int server = imageData.readInt();
		final String secret = imageData.readUTF();

		return String.format(imageFormat, farm, server, id, secret);
	}

	private static URI getBaseURI() {
		return UriBuilder.fromUri("http://localhost/").port(9998).build();
	}

	public static final URI BASE_URI = getBaseURI();

	protected static HttpServer startServer() throws IOException {
		System.out.println("Starting grizzly...");
		final ResourceConfig rc = new ClassNamesResourceConfig(FlickrServerTest.class);
		return GrizzlyServerFactory.createHttpServer(BASE_URI, rc);
	}

	public static void main(String[] args) throws IOException {
		final HttpServer httpServer = startServer();
		System.in.read();
		httpServer.stop();
	}
}
