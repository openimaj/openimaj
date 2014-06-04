package org.openimaj.video.xuggle;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;

import com.xuggle.xuggler.io.FileProtocolHandler;
import com.xuggle.xuggler.io.IURLProtocolHandler;

/**
 * An {@link IURLProtocolHandler} for jar resources. This implementation copies
 * the resource to a temporary file before opening it - it could potentially
 * consume a stream directly, but this would restrict the video codecs to ones
 * that directly support streaming.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class JarURLProtocolHandler extends FileProtocolHandler {
	private static final Map<URL, String> extractedVideos = new HashMap<URL, String>();

	/**
	 * Construct with no video
	 */
	public JarURLProtocolHandler() {

	}

	/**
	 * Construct with the given url. The video will be extracted immediately.
	 * 
	 * @param url
	 *            the url to the video
	 * @throws IOException
	 *             if an error occurs
	 */
	public JarURLProtocolHandler(String url) throws IOException {
		super(extract(url));
	}

	@Override
	public int open(String url, int flags) {
		if (flags != URL_RDONLY_MODE) {
			// log.debug("Cannot write to a video in a jar file");
			return -1;
		}

		try {
			return super.open(extract(url), flags);
		} catch (final IOException e) {
			// log.error("Unexpected IOException: " + e);
			e.printStackTrace();
			return -1;
		}
	}

	private static String extract(String urlString) throws IOException {
		final URL url = new URL(urlString);

		if (extractedVideos.containsKey(url))
			return extractedVideos.get(url);

		final File tmp = File.createTempFile("movie", ".tmp");
		final String filename = tmp.getAbsolutePath();
		// log.debug("Mapping url " + urlString + " to  file " + tmp);
		tmp.deleteOnExit();
		FileUtils.copyURLToFile(url, tmp);
		extractedVideos.put(url, filename);

		return filename;
	}

	@Override
	public int write(byte[] paramArrayOfByte, int paramInt) {
		// writing not supported
		return -1;
	}
}
