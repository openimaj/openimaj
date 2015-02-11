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
package org.openimaj.stream.functions;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.openimaj.image.FImage;
import org.openimaj.image.Image;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.io.ObjectReader;
import org.openimaj.util.function.MultiFunction;
import org.openimaj.util.stream.Stream;

/**
 * This class implements a function that can read images from URLs. Use in
 * combination with a {@link Stream} to convert from URLs to {@link Image}s.
 *
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 * @param <I>
 *            The type of {@link Image}
 */
public class ImageFromURL<I extends Image<?, I>> implements MultiFunction<URL, I> {
	/**
	 * Static instance of the {@link ImageFromURL} for extracting {@link FImage}
	 * s
	 */
	public static ImageFromURL<FImage> FIMAGE_EXTRACTOR = new ImageFromURL<FImage>(ImageUtilities.FIMAGE_READER);

	/**
	 * Static instance of the {@link ImageFromURL} for extracting
	 * {@link MBFImage}s
	 */
	public static ImageFromURL<MBFImage> MBFIMAGE_EXTRACTOR = new ImageFromURL<MBFImage>(ImageUtilities.MBFIMAGE_READER);

	private ObjectReader<I, InputStream> reader;

	/**
	 * Construct with the given image reader.
	 *
	 * @param reader
	 */
	public ImageFromURL(ObjectReader<I, InputStream> reader) {
		this.reader = reader;
	}

	@Override
	public List<I> apply(URL in) {
		final List<I> images = new ArrayList<I>(1);
		InputStream stream = null;
		try {
			stream = in.openStream();
			final I im = reader.read(stream);

			images.add(im);
		} catch (final IOException e) {
			// silently ignore
		} finally {
			if (stream != null) {
				try {
					stream.close();
				} catch (final IOException e) {
					// silently ignore
				}
			}
		}

		return images;
	}
}
