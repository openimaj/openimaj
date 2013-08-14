package org.openimaj.util.iterator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Iterator;

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
	interface Provider {
		BufferedReader open() throws IOException;
	}

	Provider source;

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
