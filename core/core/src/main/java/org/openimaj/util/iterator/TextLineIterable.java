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
package org.openimaj.util.iterator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Iterator;
import java.util.zip.GZIPInputStream;

/**
 * An {@link Iterable} that can provide access to lines of a text file
 * referenced by a {@link File} or {@link URL}. It is safe to re-use this
 * {@link Iterable} instance; a new {@link BufferedReader} will be created when
 * {@link #iterator()} is called. Any {@link IOException}s are wrapped as
 * {@link RuntimeException}s.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 */
public class TextLineIterable implements Iterable<String> {
	/**
	 * Interface describing things that can provide input for a
	 * {@link TextLineIterable}
	 * 
	 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
	 * 
	 */
	public static interface Provider {
		/**
		 * Open a stream to the data
		 * 
		 * @return the stream to the data
		 * @throws IOException
		 */
		BufferedReader open() throws IOException;
	}

	/**
	 * A {@link Provider} for gzipped text files
	 * 
	 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
	 * 
	 */
	public static class GZIPFileProvider implements Provider {
		File f;

		/**
		 * Construct with the given file
		 * 
		 * @param f
		 *            the file
		 */
		public GZIPFileProvider(File f) {
			this.f = f;
		}

		@Override
		public BufferedReader open() throws IOException {
			return new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(f))));
		}
	}

	private Provider source;

	/**
	 * Construct with the given provider
	 * 
	 * @param source
	 *            the provider
	 */
	public TextLineIterable(Provider source) {
		this.source = source;
	}

	/**
	 * Construct with the given file
	 * 
	 * @param f
	 *            the file
	 */
	public TextLineIterable(final File f) {
		source = new Provider() {
			@Override
			public BufferedReader open() throws IOException {
				return new BufferedReader(new FileReader(f));
			}
		};
	}

	/**
	 * Construct with the given file and charset
	 * 
	 * @param f
	 *            the file
	 * @param charset
	 *            the character set
	 */
	public TextLineIterable(final File f, final String charset) {
		source = new Provider() {
			@Override
			public BufferedReader open() throws IOException {
				return new BufferedReader(new InputStreamReader(new FileInputStream(f), charset));
			}
		};
	}

	/**
	 * Construct with the given url and charset
	 * 
	 * @param f
	 *            the url
	 * @param charset
	 *            the character set
	 */
	public TextLineIterable(final URL f, final String charset) {
		source = new Provider() {
			@Override
			public BufferedReader open() throws IOException {
				return new BufferedReader(new InputStreamReader(f.openStream(), charset));
			}
		};
	}

	/**
	 * Construct with the given url
	 * 
	 * @param f
	 *            the url
	 */
	public TextLineIterable(final URL f) {
		source = new Provider() {
			@Override
			public BufferedReader open() throws IOException {
				return new BufferedReader(new InputStreamReader(f.openStream()));
			}
		};
	}

	@Override
	public Iterator<String> iterator() {
		return new Iterator<String>() {
			BufferedReader br = open();
			String nextLine = readLine();

			@Override
			public boolean hasNext() {
				return nextLine != null;
			}

			@Override
			public String next() {
				final String result = nextLine;

				if (nextLine != null) {
					nextLine = readLine();
					if (nextLine == null)
						closeQuietly();
				}

				return result;
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException("not supported");
			}

			private String readLine() {
				try {
					return br.readLine();
				} catch (final IOException e) {
					throw new RuntimeException(e);
				}
			}

			private BufferedReader open() {
				try {
					return source.open();
				} catch (final IOException e) {
					throw new RuntimeException(e);
				}
			}

			private void closeQuietly() {
				try {
					br.close();
				} catch (final IOException e) {
					// ignore
				}
			}
		};
	}
}
