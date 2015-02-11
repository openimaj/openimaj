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
package org.openimaj.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.httpclient.util.HttpURLConnection;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolException;
import org.apache.http.client.RedirectStrategy;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HttpContext;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.openimaj.util.pair.IndependentPair;

/**
 * HTTP(S) download utilities, with support for HTTP redirects and meta refresh
 * redirection.
 *
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class HttpUtils {

	/**
	 * The default user-agent string
	 */
	public static final String DEFAULT_USERAGENT = "Mozilla/5.0 (Windows; U; Windows NT 6.0; ru; rv:1.9.0.11) Gecko/2009060215 Firefox/3.0.11 (.NET CLR 3.5.30729)";

	private HttpUtils() {
	}

	/**
	 * Read the contents of the given {@link URL} as an array of bytes.
	 * Redirects are followed automatically.
	 *
	 * @param u
	 *            the URL to read from
	 * @return the content referenced by the URL
	 * @throws IOException
	 *             if an error occurs
	 * @throws IllegalArgumentException
	 *             if the URL is not an HTTP(s) URL
	 */
	public static byte[] readURLAsBytes(URL u) throws IOException {
		return readURLAsBytes(u, true);
	}

	/**
	 * Read the contents of the given {@link URL} as an array of bytes. If
	 * redirects are not being followed, then the result will be null if the URL
	 * is redirected.
	 *
	 * @param u
	 *            the URL to read from
	 * @param followRedirects
	 *            should redirects be followed?
	 * @return the content referenced by the URL
	 * @throws IOException
	 *             if an error occurs
	 * @throws IllegalArgumentException
	 *             if the URL is not an HTTP(s) URL
	 */
	public static byte[] readURLAsBytes(URL u, boolean followRedirects) throws IOException {
		final InputStream stream = readURLAsStream(u, followRedirects);
		if (stream == null)
			return null;

		try {
			return org.apache.commons.io.IOUtils.toByteArray(stream);
		} finally {
			if (stream != null)
				stream.close();
		}
	}

	/**
	 * A {@link RedirectStrategy} that can deal with meta-refresh style
	 * redirection
	 * 
	 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
	 *
	 */
	public static class MetaRefreshRedirectStrategy extends DefaultRedirectStrategy {
		private static final String METAREFRESH_LOCATION = "METAREFRESH_LOCATION";

		@Override
		public boolean isRedirected(HttpRequest request, HttpResponse response, HttpContext context)
				throws ProtocolException
		{
			final boolean isRedirect = super.isRedirected(request, response, context);
			context.setAttribute(METAREFRESH_LOCATION, null);
			if (!isRedirect) {
				// Consume and buffer the entity, set the entity
				HttpEntity entity = null;
				try {
					entity = response.getEntity();
					if (!entity.isRepeatable())
					{
						entity = new BufferedHttpEntity(response.getEntity());
						response.setEntity(entity); // Set the entity!
					}
					final HttpHost host = (HttpHost) context.getAttribute("http.target_host");
					final URL url = new URL(host.toURI());

					final Header encodingObj = entity.getContentEncoding();
					String encoding = null;
					if (encodingObj == null) {
						encoding = "UTF-8";
					}
					else {
						encoding = encodingObj.getValue();
						if (encoding == null) {
							encoding = "UTF-8";
						}
					}
					final URL u = checkRedirects(url, FileUtils.readall(entity.getContent(), encoding));
					if (u != null) {
						// set the location so it doesn't have to be read again
						context.setAttribute(METAREFRESH_LOCATION, u);
						return true;
					}

				} catch (final IOException e) {
					return false;
				}
			}
			return isRedirect;
		}

		@Override
		public HttpUriRequest getRedirect(HttpRequest request, HttpResponse response, HttpContext context)
				throws ProtocolException
		{
			final URL metarefresh = (URL) context.getAttribute(METAREFRESH_LOCATION);
			if (metarefresh == null) {
				return super.getRedirect(request, response, context);
			}

			final String method = request.getRequestLine().getMethod();
			try {
				if (method.equalsIgnoreCase(HttpHead.METHOD_NAME)) {
					return new HttpHead(metarefresh.toURI());
				} else {
					return new HttpGet(metarefresh.toURI());
				}
			} catch (final URISyntaxException e) {
				return super.getRedirect(request, response, context);
			}
		}
	}

	/**
	 * Read the contents of the given {@link URL} as a
	 * {@link ByteArrayInputStream} (i.e. a byte[] in memory wrapped in an
	 * {@link InputStream}). If redirects are not being followed, then the
	 * result will be null if the URL is redirected.
	 *
	 * @param u
	 *            the URL to read from
	 * @param followRedirects
	 *            should redirects be followed?
	 * @return the content referenced by the URL
	 * @throws IOException
	 *             if an error occurs
	 * @throws IllegalArgumentException
	 *             if the URL is not an HTTP(s) URL
	 */
	public static IndependentPair<HttpEntity, ByteArrayInputStream> readURLAsByteArrayInputStream(URL u,
			boolean followRedirects) throws IOException
			{
		return readURLAsByteArrayInputStream(u, 15000, 15000, followRedirects ? new MetaRefreshRedirectStrategy() : null,
				DEFAULT_USERAGENT);
			}

	/**
	 * Read the contents of the given {@link URL} as a
	 * {@link ByteArrayInputStream} (i.e. a byte[] in memory wrapped in an
	 * {@link InputStream}). If redirects are not being followed, then the
	 * result will be null if the URL is redirected.
	 *
	 * @param u
	 *            the URL to read from
	 * @param strategy
	 *            how redirects should be followed
	 * @return the content referenced by the URL
	 * @throws IOException
	 *             if an error occurs
	 * @throws IllegalArgumentException
	 *             if the URL is not an HTTP(s) URL
	 */
	public static IndependentPair<HttpEntity, ByteArrayInputStream> readURLAsByteArrayInputStream(URL u,
			RedirectStrategy strategy) throws IOException
			{
		return readURLAsByteArrayInputStream(u, 15000, 15000, strategy, DEFAULT_USERAGENT);
			}

	/**
	 * Read the contents of the given {@link URL} as a
	 * {@link ByteArrayInputStream} (i.e. a byte[] in memory wrapped in an
	 * {@link InputStream}). If redirects are not being followed, then the
	 * result will be null if the URL is redirected.
	 *
	 * @param url
	 *            the URL to read from
	 * @param connectionTimeout
	 *            amount of time to wait for connection
	 * @param readTimeout
	 *            amount of time to wait for reading
	 * @param redirectStrategy
	 *            the redirection strategy
	 * @param userAgent
	 *            the useragent string
	 * @return the content referenced by the URL
	 * @throws IOException
	 *             if an error occurs
	 * @throws IllegalArgumentException
	 *             if the URL is not an HTTP(s) URL
	 */
	public static IndependentPair<HttpEntity, ByteArrayInputStream> readURLAsByteArrayInputStream(URL url,
			int connectionTimeout, int readTimeout, RedirectStrategy redirectStrategy, String userAgent)
					throws IOException
					{
		DefaultHttpClient c = null;
		try {
			final HttpParams params = new BasicHttpParams();
			HttpConnectionParams.setConnectionTimeout(params, connectionTimeout);
			HttpConnectionParams.setSoTimeout(params, readTimeout);
			HttpProtocolParams.setUserAgent(params, userAgent);
			HttpClientParams.setRedirecting(params, redirectStrategy != null);
			final boolean followRedirects = redirectStrategy != null;
			c = new DefaultHttpClient(params);
			if (followRedirects)
				c.setRedirectStrategy(redirectStrategy);
			HttpResponse resp = null;
			try {
				resp = c.execute(new HttpGet(url.toURI()));
			} catch (final URISyntaxException e) {
				throw new IOException(e);
			}

			final ByteArrayOutputStream outStream = new ByteArrayOutputStream();
			final InputStream stream = resp.getEntity().getContent();
			final byte[] tempBuffer = new byte[1024];

			// read the rest!
			while (true) {
				final int readThisTime = stream.read(tempBuffer);
				if (readThisTime == -1) {
					break;
				}
				// write to the outStream
				outStream.write(tempBuffer, 0, readThisTime);
			}
			final IndependentPair<HttpEntity, ByteArrayInputStream> toRet = IndependentPair.pair(resp.getEntity(),
					new ByteArrayInputStream(outStream.toByteArray()));
			;
			return toRet;
		} finally {
			if (c != null)
				c.getConnectionManager().shutdown();
		}

					}

	/**
	 * Open an {@link HttpURLConnection} to the {@link URL} as an array of
	 * bytes. Redirects are followed automatically.
	 *
	 * @param url
	 *            the URL to read from
	 * @return the content referenced by the URL
	 * @throws IOException
	 *             if an error occurs
	 * @throws IllegalArgumentException
	 *             if the URL is not an HTTP(s) URL
	 */
	public static InputStream readURL(URL url) throws IOException {
		return readURLAsByteArrayInputStream(url, 15000, 15000, new MetaRefreshRedirectStrategy(), DEFAULT_USERAGENT)
				.getSecondObject();
	}

	/**
	 * Open an {@link HttpURLConnection} to the {@link URL} as an array of
	 * bytes.
	 *
	 * @param url
	 *            the URL to read from
	 * @param followRedirects
	 *            should redirects be followed?
	 * @return the content referenced by the URL
	 * @throws IOException
	 *             if an error occurs
	 * @throws IllegalArgumentException
	 *             if the URL is not an HTTP(s) URL
	 */
	public static InputStream readURL(URL url, boolean followRedirects) throws IOException {
		return readURLAsByteArrayInputStream(url, 15000, 15000,
				followRedirects ? new MetaRefreshRedirectStrategy() : null, DEFAULT_USERAGENT).getSecondObject();
	}

	private static URL searchMetaRefresh(URL base, String html) throws MalformedURLException {
		final Document doc = Jsoup.parse(html);

		final Elements tags = doc.select("meta[http-equiv=refresh]");
		if (tags != null && tags.size() > 0) {
			final String content = tags.first().attr("content");

			final Pattern pattern = Pattern.compile("\\d+\\;url\\=(.*)", Pattern.CASE_INSENSITIVE);
			final Matcher matcher = pattern.matcher(content);
			if (matcher.find()) {
				final String url = matcher.group(1);

				URL toRet = null;
				if (url.contains("://")) {
					toRet = new URL(url);
				}
				{
					toRet = new URL(base, url);
				}
				// A legitimate use of http-refresh was to refresh the current
				// page
				// this would result in a horrible loop
				if (!toRet.equals(base)) {
					return toRet;
				}
			}
		}

		return null;
	}

	private static URL checkRedirects(URL base, String html) throws IOException {
		final URL u = searchMetaRefresh(base, html);

		// potentially add more checks here for things
		// like JS refresh

		return u;
	}

	/**
	 * Open a {@link InputStream} to the contents referenced by the {@link URL}.
	 * Redirects are followed automatically.
	 *
	 * @param url
	 *            the URL to read from
	 * @return the content referenced by the URL
	 * @throws IOException
	 *             if an error occurs
	 * @throws IllegalArgumentException
	 *             if the URL is not an HTTP(s) URL
	 */
	public static InputStream readURLAsStream(URL url) throws IOException {
		return readURL(url);
	}

	/**
	 * Open a {@link InputStream} to the contents referenced by the {@link URL}.
	 * If redirects are not being followed, then the result will be null if the
	 * URL is redirected.
	 *
	 * @param url
	 *            the URL to read from
	 * @param followRedirects
	 *            should redirects be followed.
	 * @return the content referenced by the URL
	 * @throws IOException
	 *             if an error occurs
	 * @throws IllegalArgumentException
	 *             if the URL is not an HTTP(s) URL
	 */
	public static InputStream readURLAsStream(URL url, boolean followRedirects) throws IOException {
		final InputStream conn = readURL(url, followRedirects);

		return conn;
	}

	/**
	 * Read the internal state of an object from the given URL.
	 *
	 * @param <T>
	 *            Type of object being read.
	 *
	 * @param url
	 *            the URL to read from
	 * @param obj
	 *            the object to fill
	 * @return the content referenced by the URL
	 * @throws IOException
	 *             if an error occurs
	 * @throws IllegalArgumentException
	 *             if the URL is not an HTTP(s) URL
	 */
	public static <T extends InternalReadable> T readURL(URL url, T obj) throws IOException {
		final InputStream stream = readURLAsStream(url);

		try {
			return IOUtils.read(stream, obj);
		} finally {
			if (stream != null)
				stream.close();
		}
	}

	/**
	 * Read the an object from the given URL.
	 *
	 * @param <T>
	 *            Type of object being read.
	 *
	 * @param url
	 *            the URL to read from
	 * @param clz
	 *            the class of the object to read
	 * @return the content referenced by the URL
	 * @throws IOException
	 *             if an error occurs
	 * @throws IllegalArgumentException
	 *             if the URL is not an HTTP(s) URL
	 */
	public static <T extends InternalReadable> T readURL(URL url, Class<? extends T> clz) throws IOException {
		final InputStream stream = readURLAsStream(url);

		try {
			return IOUtils.read(stream, clz);
		} finally {
			if (stream != null)
				stream.close();
		}
	}

	/**
	 * Read the an object from the given URL.
	 *
	 * @param <T>
	 *            Type of object being read.
	 * @param <Q>
	 *            Type of the object reader.
	 *
	 * @param url
	 *            the URL to read from
	 * @param reader
	 *            the reader that creates the object.
	 * @return the content referenced by the URL
	 * @throws IOException
	 *             if an error occurs
	 * @throws IllegalArgumentException
	 *             if the URL is not an HTTP(s) URL
	 */
	public static <T, Q extends InputStreamObjectReader<T>> T readURL(URL url, Q reader) throws IOException {
		final InputStream stream = readURLAsStream(url);

		try {
			return reader.read(stream);
		} finally {
			if (stream != null)
				stream.close();
		}
	}
}
