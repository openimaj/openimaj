package org.openimaj.io;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

/**
 * HTTP(S) download utilities, with support for HTTP redirects
 * and meta refresh redirection.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class HttpUtils {
	private static final int READ_LIMIT = 1024*1024;
	
	/**
	 * The default user-agent string 
	 */
	public static final String DEFAULT_USERAGENT = "Mozilla/5.0 (Windows; U; Windows NT 6.0; ru; rv:1.9.0.11) Gecko/2009060215 Firefox/3.0.11 (.NET CLR 3.5.30729)";

	private HttpUtils() {
	}
	
	/**
	 * Read the contents of the given {@link URL} as an
	 * array of bytes. Redirects are followed automatically.
	 * 
	 * @param u the URL to read from
	 * @return the content referenced by the URL
	 * @throws IOException if an error occurs
	 * @throws IllegalArgumentException if the URL is not an HTTP(s) URL
	 */
	public static byte[] readURLAsBytes(URL u) throws IOException {
		return readURLAsBytes(u, true);
	}
	
	/**
	 * Read the contents of the given {@link URL} as an
	 * array of bytes. If redirects are not being followed,
	 * then the result will be null if the URL is redirected.
	 * 
	 * @param u the URL to read from
	 * @param followRedirects should redirects be followed?
	 * @return the content referenced by the URL
	 * @throws IOException if an error occurs
	 * @throws IllegalArgumentException if the URL is not an HTTP(s) URL
	 */
	public static byte[] readURLAsBytes(URL u, boolean followRedirects) throws IOException {
		InputStream stream = readURLAsStream(u, followRedirects);
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
	 * Open an {@link HttpURLConnection} to the {@link URL} as an
	 * array of bytes. Redirects are followed automatically.
	 * 
	 * @param url the URL to read from
	 * @return the content referenced by the URL
	 * @throws IOException if an error occurs
	 * @throws IllegalArgumentException if the URL is not an HTTP(s) URL
	 */
	public static HttpURLConnection readURL(URL url) throws IOException {
		return readURL(url, 15000, 15000, true, DEFAULT_USERAGENT);
	}
	
	/**
	 * Open an {@link HttpURLConnection} to the {@link URL} as an
	 * array of bytes. 
	 * 
	 * @param url the URL to read from
	 * @param followRedirects should redirects be followed?
	 * @return the content referenced by the URL
	 * @throws IOException if an error occurs
	 * @throws IllegalArgumentException if the URL is not an HTTP(s) URL
	 */
	public static HttpURLConnection readURL(URL url, boolean followRedirects) throws IOException {
		return readURL(url, 15000, 15000, followRedirects, DEFAULT_USERAGENT);
	}
	
	/**
	 * Open an {@link HttpURLConnection} to the {@link URL} as an
	 * array of bytes.
	 * 
	 * @param url the URL to read from
	 * @param connectionTimeout amount of time to wait for connection 
	 * @param readTimeout amount of time to wait for reading
	 * @param followRedirects should redirects be resolved and followed
	 * @param userAgent the useragent string
	 * @return the content referenced by the URL
	 * @throws IOException if an error occurs
	 * @throws IllegalArgumentException if the URL is not an HTTP(s) URL
	 */
	public static HttpURLConnection readURL(URL url, int connectionTimeout, int readTimeout, boolean followRedirects, String userAgent) throws IOException {
		URLConnection conn = url.openConnection();
		
		if (!(conn instanceof HttpURLConnection))
			throw new IllegalArgumentException("URL is not an HTTP connection.");
		
		HttpURLConnection httpConn = (HttpURLConnection)conn;
        httpConn.setConnectTimeout(connectionTimeout);
        httpConn.setReadTimeout(readTimeout);
        httpConn.setInstanceFollowRedirects(followRedirects);
        httpConn.setRequestProperty("User-Agent", userAgent);
        httpConn.connect();
        
        String mime = httpConn.getContentType().toLowerCase();
        if (followRedirects && mime.contains("html")) {
        	InputStream stream = httpConn.getInputStream();
        	stream.mark(READ_LIMIT);
        	
        	byte [] buffer = new byte[READ_LIMIT - 1];
        	org.apache.commons.io.IOUtils.read(stream, buffer);
        	
        	URL u = checkRedirects(url, new String(buffer, httpConn.getContentEncoding()));
        	
        	if (u != null) {
        		httpConn.disconnect();
        		
        		return readURL(u, connectionTimeout, readTimeout, followRedirects, userAgent);
        	}
        	
        	stream.reset();
        }
        
        return httpConn;
	}
	
	private static URL searchMetaRefresh(URL base, String html) throws MalformedURLException {
		Document doc = Jsoup.parse(html);
		
		Elements tags = doc.select("meta[http-equiv=refresh]");
		if (tags != null && tags.size() > 0) {
			String content = tags.first().attr("content");
		
			Pattern pattern = Pattern.compile("\\d+\\;url\\=(.*)");
			Matcher matcher = pattern.matcher(content);
			if (matcher.find()) {
				String url = matcher.group(1);
				
				if (url.contains("://")) {
					return new URL(url);					
				}
				return new URL(base, url);
			}
		}
		
		return null;
	}
	
	private static URL checkRedirects(URL base, String html) throws IOException {
		URL u = searchMetaRefresh(base, html);
		
		//potentially add more checks here for things
		//like JS refresh
		
		return u;
	}

	/**
	 * Open a {@link InputStream} to the contents 
	 * referenced by the {@link URL}. Redirects are 
	 * followed automatically.
	 * 
	 * @param url the URL to read from
	 * @return the content referenced by the URL
	 * @throws IOException if an error occurs
	 * @throws IllegalArgumentException if the URL is not an HTTP(s) URL
	 */
	public static InputStream readURLAsStream(URL url) throws IOException {
		return readURL(url).getInputStream();
	}
	
	/**
	 * Open a {@link InputStream} to the contents 
	 * referenced by the {@link URL}. If redirects are not being followed,
	 * then the result will be null if the URL is redirected.
	 * 
	 * @param url the URL to read from
	 * @param followRedirects should redirects be followed.
	 * @return the content referenced by the URL
	 * @throws IOException if an error occurs
	 * @throws IllegalArgumentException if the URL is not an HTTP(s) URL
	 */
	public static InputStream readURLAsStream(URL url, boolean followRedirects) throws IOException {
		HttpURLConnection conn = readURL(url, followRedirects);
		
		if (conn.getResponseCode() > 300 && conn.getResponseCode() < 400)
			return null;
		
		return conn.getInputStream();
	}
	
	/**
	 * Read the internal state of an object from
	 * the given URL.
	 *  
	 * @param <T> Type of object being read.
	 * 
	 * @param url the URL to read from
	 * @param obj the object to fill 
	 * @return the content referenced by the URL
	 * @throws IOException if an error occurs
	 * @throws IllegalArgumentException if the URL is not an HTTP(s) URL
	 */
	public static <T extends InternalReadable> T readURL(URL url, T obj) throws IOException {
		InputStream stream = readURLAsStream(url);
		
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
	 * @param <T> Type of object being read.
	 * 
	 * @param url the URL to read from
	 * @param clz the class of the object to read 
	 * @return the content referenced by the URL
	 * @throws IOException if an error occurs
	 * @throws IllegalArgumentException if the URL is not an HTTP(s) URL
	 */
	public static <T extends InternalReadable> T readURL(URL url, Class<? extends T> clz) throws IOException {
		InputStream stream = readURLAsStream(url);
		
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
	 * @param <T> Type of object being read.
	 * @param <Q> Type of the object reader.
	 * 
	 * @param url the URL to read from
	 * @param reader the reader that creates the object. 
	 * @return the content referenced by the URL
	 * @throws IOException if an error occurs
	 * @throws IllegalArgumentException if the URL is not an HTTP(s) URL
	 */
	public static <T, Q extends ObjectReader<T>> T readURL(URL url, Q reader) throws IOException {
		InputStream stream = readURLAsStream(url);
		
		try {
			return reader.read(stream);
		} finally {
			if (stream != null)
				stream.close();
		}
	}	
}
