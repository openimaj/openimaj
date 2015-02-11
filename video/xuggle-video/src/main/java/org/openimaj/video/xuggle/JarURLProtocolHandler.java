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
		final String filename = tmp.toURI().toString();
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
