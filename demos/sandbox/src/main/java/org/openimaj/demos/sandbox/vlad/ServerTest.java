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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriBuilder;

import org.glassfish.grizzly.http.server.HttpServer;
import org.openimaj.image.indexing.vlad.VLADIndexerData;
import org.openimaj.io.IOUtils;
import org.openimaj.knn.pq.FloatADCNearestNeighbours;
import org.openimaj.util.pair.IntObjectPair;

import com.sun.jersey.api.container.grizzly2.GrizzlyServerFactory;
import com.sun.jersey.api.core.ClassNamesResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;

@Path("/search")
public class ServerTest {
	static FloatADCNearestNeighbours nn;
	static List<IntObjectPair<float[]>> index;
	static String imageFormat;
	static int offset;

	static {
		try {
			System.out.println("init");

			// final VLADIndexer indexer = VLADIndexer.read(new
			// File("/Users/jsh2/vlad-indexer-ukbench-2x.dat"));
			// index = IOUtils.readFromFile(new
			// File("/Users/jsh2/Desktop/ukb.idx"));
			// imageFormat = "/Users/jsh2/Data/ukbench/full/ukbench%05d.jpg";
			// offset = 0;
			final VLADIndexerData indexer = VLADIndexerData
					.read(new File("/Users/jsh2/vlad-indexer-mirflickr25k-1x.dat"));
			index = IOUtils.readFromFile(new File("/Users/jsh2/Desktop/mirflickr25k.idx"));
			imageFormat = "/Volumes/Raid/mirflickr/mirflickr/im%d.jpg";
			offset = 1;

			for (final IntObjectPair<float[]> p : index)
				if (p.second == null)
					p.second = new float[128];

			Collections.sort(index, new Comparator<IntObjectPair<float[]>>() {
				@Override
				public int compare(IntObjectPair<float[]> o1, IntObjectPair<float[]> o2)
				{
					return o1.first == o2.first ? 0 : o1.first < o2.first ? -1 : 1;
				}
			});

			final List<float[]> data = IntObjectPair.getSecond(index);
			nn = new FloatADCNearestNeighbours(indexer.getProductQuantiser(), data.toArray(new float[data.size()][]));
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
			sb.append("<div style='float:left; width: 100'>");
			sb.append("<a href=\"/search?i=" + argmins[0][j] + "\">");
			sb.append("<img width=\"100\" src=\"" + String.format("/search/image?i=%d", argmins[0][j]) + "\"/>");
			sb.append("</a>");
			sb.append(mins[0][j]);
			sb.append("</div>");
		}
		sb.append("</body></html>");

		return sb.toString();
	}

	@GET
	@Produces("image/jpeg")
	@Path("/image")
	public StreamingOutput image(@QueryParam("i") final int i) {
		return new StreamingOutput() {
			@Override
			public void write(OutputStream output) throws IOException, WebApplicationException {
				final String imagename = String.format(imageFormat, i + offset);
				final FileInputStream fis = new FileInputStream(new File(imagename));
				org.apache.commons.io.IOUtils.copy(fis, output);
				fis.close();
			}
		};
	}

	private static URI getBaseURI() {
		return UriBuilder.fromUri("http://localhost/").port(9998).build();
	}

	public static final URI BASE_URI = getBaseURI();

	protected static HttpServer startServer() throws IOException {
		System.out.println("Starting grizzly...");
		final ResourceConfig rc = new ClassNamesResourceConfig(ServerTest.class);
		return GrizzlyServerFactory.createHttpServer(BASE_URI, rc);
	}

	public static void main(String[] args) throws IOException {
		final HttpServer httpServer = startServer();
		System.in.read();
		httpServer.stop();
	}
}
