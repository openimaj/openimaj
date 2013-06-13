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
public class ImageFomURL<I extends Image<?, I>> implements MultiFunction<URL, I> {
	/**
	 * Static instance of the {@link ImageFomURL} for extracting {@link FImage}s
	 */
	public static ImageFomURL<FImage> FIMAGE_EXTRACTOR = new ImageFomURL<FImage>(ImageUtilities.FIMAGE_READER);

	/**
	 * Static instance of the {@link ImageFomURL} for extracting
	 * {@link MBFImage}s
	 */
	public static ImageFomURL<MBFImage> MBFIMAGE_EXTRACTOR = new ImageFomURL<MBFImage>(ImageUtilities.MBFIMAGE_READER);

	private ObjectReader<I, InputStream> reader;

	/**
	 * Construct with the given image reader.
	 * 
	 * @param reader
	 */
	public ImageFomURL(ObjectReader<I, InputStream> reader) {
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
