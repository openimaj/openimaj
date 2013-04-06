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
package org.openimaj.image;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Iterator;

import javax.imageio.IIOException;
import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.spi.IIORegistry;
import javax.imageio.stream.ImageInputStream;
import javax.media.jai.JAI;

import org.apache.sanselan.ImageFormat;
import org.apache.sanselan.ImageInfo;
import org.apache.sanselan.ImageReadException;
import org.apache.sanselan.Sanselan;

import com.sun.media.jai.codec.SeekableStream;

/**
 * A class that provides extra functionality beyond that of the standard
 * {@link ImageIO} class. In particular, it tries to deal jpeg images that the
 * standard ImageIO cannot (i.e. CMYK jpegs, etc).
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 */
class ExtendedImageIO {
	static class NonClosableInputStream extends BufferedInputStream {
		public NonClosableInputStream(InputStream in) {
			super(in);
		}

		@Override
		public void close() throws IOException {
		}

		/**
		 * @throws IOException
		 */
		public void reallyClose() throws IOException {
			super.close();
		}
	}

	/**
	 * Returns a <code>BufferedImage</code> as the result of decoding a supplied
	 * <code>File</code> with an <code>ImageReader</code> chosen automatically
	 * from among those currently registered. The <code>File</code> is wrapped
	 * in an <code>ImageInputStream</code>. If no registered
	 * <code>ImageReader</code> claims to be able to read the resulting stream,
	 * <code>null</code> is returned.
	 * 
	 * <p>
	 * The current cache settings from <code>getUseCache</code>and
	 * <code>getCacheDirectory</code> will be used to control caching in the
	 * <code>ImageInputStream</code> that is created.
	 * 
	 * <p>
	 * Note that there is no <code>read</code> method that takes a filename as a
	 * <code>String</code>; use this method instead after creating a
	 * <code>File</code> from the filename.
	 * 
	 * <p>
	 * This method does not attempt to locate <code>ImageReader</code>s that can
	 * read directly from a <code>File</code>; that may be accomplished using
	 * <code>IIORegistry</code> and <code>ImageReaderSpi</code>.
	 * 
	 * @param input
	 *            a <code>File</code> to read from.
	 * 
	 * @return a <code>BufferedImage</code> containing the decoded contents of
	 *         the input, or <code>null</code>.
	 * 
	 * @exception IllegalArgumentException
	 *                if <code>input</code> is <code>null</code>.
	 * @exception IOException
	 *                if an error occurs during reading.
	 */
	public static BufferedImage read(File input) throws IOException {
		if (input == null) {
			throw new IllegalArgumentException("input == null!");
		}
		if (!input.canRead()) {
			throw new IIOException("Can't read input file!");
		}
		InputStream stream = null;
		try {
			stream = new FileInputStream(input);
			return read(stream);
		} finally {
			try {
				stream.close();
			} catch (final IOException e) {
			}
		}
	}

	/**
	 * Returns a <code>BufferedImage</code> as the result of decoding a supplied
	 * <code>InputStream</code> with an <code>ImageReader</code> chosen
	 * automatically from among those currently registered. The
	 * <code>InputStream</code> is wrapped in an <code>ImageInputStream</code>.
	 * If no registered <code>ImageReader</code> claims to be able to read the
	 * resulting stream, <code>null</code> is returned.
	 * 
	 * <p>
	 * The current cache settings from <code>getUseCache</code>and
	 * <code>getCacheDirectory</code> will be used to control caching in the
	 * <code>ImageInputStream</code> that is created.
	 * 
	 * <p>
	 * This method does not attempt to locate <code>ImageReader</code>s that can
	 * read directly from an <code>InputStream</code>; that may be accomplished
	 * using <code>IIORegistry</code> and <code>ImageReaderSpi</code>.
	 * 
	 * <p>
	 * This method <em>does not</em> close the provided <code>InputStream</code>
	 * after the read operation has completed; it is the responsibility of the
	 * caller to close the stream, if desired.
	 * 
	 * @param input
	 *            an <code>InputStream</code> to read from.
	 * 
	 * @return a <code>BufferedImage</code> containing the decoded contents of
	 *         the input, or <code>null</code>.
	 * 
	 * @exception IllegalArgumentException
	 *                if <code>input</code> is <code>null</code>.
	 * @exception IOException
	 *                if an error occurs during reading.
	 */
	public static BufferedImage read(InputStream input) throws IOException {
		if (input == null) {
			throw new IllegalArgumentException("input == null!");
		}

		final NonClosableInputStream buffer = new NonClosableInputStream(input);
		buffer.mark(10 * 1024 * 1024); // 10mb is big enough?

		BufferedImage bi;
		try {
			bi = readInternal(buffer);
		} catch (final Exception ex) {
			bi = null;
		}

		if (bi == null) {
			buffer.reset();
			try {
				bi = Sanselan.getBufferedImage(buffer);
			} catch (final Throwable e) {
				throw new IOException(e);
			}
		}

		return bi;
	}

	/**
	 * Returns a <code>BufferedImage</code> as the result of decoding a supplied
	 * <code>URL</code> with an <code>ImageReader</code> chosen automatically
	 * from among those currently registered. An <code>InputStream</code> is
	 * obtained from the <code>URL</code>, which is wrapped in an
	 * <code>ImageInputStream</code>. If no registered <code>ImageReader</code>
	 * claims to be able to read the resulting stream, <code>null</code> is
	 * returned.
	 * 
	 * <p>
	 * The current cache settings from <code>getUseCache</code>and
	 * <code>getCacheDirectory</code> will be used to control caching in the
	 * <code>ImageInputStream</code> that is created.
	 * 
	 * <p>
	 * This method does not attempt to locate <code>ImageReader</code>s that can
	 * read directly from a <code>URL</code>; that may be accomplished using
	 * <code>IIORegistry</code> and <code>ImageReaderSpi</code>.
	 * 
	 * @param input
	 *            a <code>URL</code> to read from.
	 * 
	 * @return a <code>BufferedImage</code> containing the decoded contents of
	 *         the input, or <code>null</code>.
	 * 
	 * @exception IllegalArgumentException
	 *                if <code>input</code> is <code>null</code>.
	 * @exception IOException
	 *                if an error occurs during reading.
	 */
	public static BufferedImage read(URL input) throws IOException {
		if (input == null) {
			throw new IllegalArgumentException("input == null!");
		}

		InputStream istream = null;
		try {
			istream = input.openStream();
		} catch (final IOException e) {
			throw new IIOException("Can't get input stream from URL!", e);
		}

		return read(istream);
	}

	/**
	 * Returns a <code>BufferedImage</code> as the result of decoding a supplied
	 * <code>ImageInputStream</code> with an <code>ImageReader</code> chosen
	 * automatically from among those currently registered. If no registered
	 * <code>ImageReader</code> claims to be able to read the stream,
	 * <code>null</code> is returned.
	 * 
	 * @param input
	 *            an <code>ImageInputStream</code> to read from.
	 * 
	 * @return a <code>BufferedImage</code> containing the decoded contents of
	 *         the input, or <code>null</code>.
	 * 
	 * @exception IllegalArgumentException
	 *                if <code>stream</code> is <code>null</code>.
	 * @exception IOException
	 *                if an error occurs during reading.
	 * @throws ImageReadException
	 */
	private static BufferedImage readInternal(BufferedInputStream binput) throws IOException, ImageReadException {
		if (binput == null) {
			throw new IllegalArgumentException("stream == null!");
		}

		ImageInfo info;
		try {
			info = Sanselan.getImageInfo(binput, null);
		} catch (final ImageReadException ire) {
			info = null;
		} finally {
			binput.reset();
		}

		if (info != null && info.getFormat() == ImageFormat.IMAGE_FORMAT_JPEG) {
			if (info.getColorType() == ImageInfo.COLOR_TYPE_CMYK) {
				final ImageReader reader = getMonkeyReader();

				if (reader == null) {
					// fallback to the ImageIO reader... one day it might be
					// fixed
					return ImageIO.read(binput);
				} else {
					return loadWithReader(reader, binput);
				}
			} else {
				// first try JAI if it's available
				try {
					// OpenJDK7 doesn't work properly with JAI as some of the
					// classes are missing!! This next line will throw in such
					// cases:
					Class.forName("com.sun.image.codec.jpeg.ImageFormatException");

					synchronized (JAI.class) {
						return JAI.create("stream", SeekableStream.wrapInputStream(binput, false)).getAsBufferedImage();
					}
				} catch (final Exception e) {
					// JAI didn't work... we'll fall back to ImageIO, but try
					// the monkey first
					binput.reset();

					// First try the Monkey reader
					final ImageReader reader = getMonkeyReader();

					if (reader == null) {
						// fallback to the ImageIO reader... one day it might be
						// fixed
						return ImageIO.read(binput);
					} else {
						try {
							return loadWithReader(reader, binput);
						} catch (final Exception ee) {
							// fallback to the ImageIO reader... one day it
							// might be
							// fixed
							return ImageIO.read(binput);
						}
					}
				}
			}
		} else {
			return ImageIO.read(binput);
		}
	}

	/**
	 * Load an image with the given reader
	 * 
	 * @param reader
	 * @param binput
	 * @return
	 * @throws IOException
	 */
	private static BufferedImage loadWithReader(ImageReader reader, BufferedInputStream binput) throws IOException {
		final ImageInputStream stream = ImageIO.createImageInputStream(binput);

		final ImageReadParam param = reader.getDefaultReadParam();
		reader.setInput(stream, true, true);

		return reader.read(0, param);
	}

	/**
	 * Get the TwelveMonkeys reader if its present and attempt to load it if
	 * necessary.
	 * 
	 * @return the TwelveMonkeys JPEG Reader or null if it can't be loaded.
	 */
	private static ImageReader getMonkeyReader() {
		Iterator<ImageReader> iter = ImageIO.getImageReadersByFormatName("jpeg");
		while (iter.hasNext()) {
			final ImageReader reader = iter.next();
			if (reader instanceof com.twelvemonkeys.imageio.plugins.jpeg.JPEGImageReader)
				return reader;
		}

		IIORegistry.getDefaultInstance().registerServiceProvider(
				new com.twelvemonkeys.imageio.plugins.jpeg.JPEGImageReaderSpi());

		iter = ImageIO.getImageReadersByFormatName("jpeg");
		while (iter.hasNext()) {
			final ImageReader reader = iter.next();
			if (reader instanceof com.twelvemonkeys.imageio.plugins.jpeg.JPEGImageReader)
				return reader;
		}

		return null;
	}
}
