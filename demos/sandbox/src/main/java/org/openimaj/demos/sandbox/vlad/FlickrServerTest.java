package org.openimaj.demos.sandbox.vlad;

import gnu.trove.list.array.TLongArrayList;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.UriBuilder;

import org.glassfish.grizzly.http.server.HttpServer;
import org.openimaj.io.IOUtils;
import org.openimaj.knn.pq.IncrementalFloatADCNearestNeighbours;
import org.openimaj.util.pair.IntObjectPair;

import com.sun.jersey.api.container.grizzly2.GrizzlyServerFactory;
import com.sun.jersey.api.core.ClassNamesResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;

@Path("/search")
public class FlickrServerTest {
	static IncrementalFloatADCNearestNeighbours nn;
	static TLongArrayList indexes;
	static List<IntObjectPair<float[]>> index;
	static String imageFormat;

	static {
		try {
			System.out.println("init");

			nn = IOUtils.read(new File("/Volumes/My Book/flickr46m-vlad-pqadcnn.dat"),
					IncrementalFloatADCNearestNeighbours.class);
			indexes = IOUtils.readFromFile(new File("/Volumes/My Book/flickr46m-vlad-pqadcnn-indexes.dat"));

			imageFormat = "http://farm%d.staticflickr.com/%d/%d_%s.jpg";
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	@GET
	@Produces("text/html")
	public String search(@QueryParam("i") int i)
	{
		final int length = 100;
		final int[][] argmins = new int[1][length];
		final float[][] mins = new float[1][length];
		nn.searchKNN(new float[][] { index.get(i).second }, length, argmins, mins);

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

	private String getImageId(int i) {
		return "foo";
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
