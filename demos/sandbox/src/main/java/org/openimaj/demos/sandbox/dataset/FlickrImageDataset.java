package org.openimaj.demos.sandbox.dataset;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import org.openimaj.image.FImage;
import org.openimaj.image.Image;
import org.openimaj.image.ImageUtilities;
import org.openimaj.io.ObjectReader;

import com.aetrion.flickr.Flickr;
import com.aetrion.flickr.REST;
import com.aetrion.flickr.galleries.GalleriesInterface;
import com.aetrion.flickr.photos.Photo;
import com.aetrion.flickr.photos.PhotoList;

public class FlickrImageDataset<IMAGE extends Image<?, IMAGE>> extends ReadableListDataset<IMAGE> {
	private final Pattern GALLERY_URL_PATTERN = Pattern.compile(".*/photos/.*/galleries/[0-9]*(/|$)");

	protected List<Photo> photos;

	protected FlickrImageDataset(ObjectReader<IMAGE> reader, List<Photo> photos) {
		super(reader);

		this.photos = photos;
	}

	/**
	 * Get the underlying flickr {@link Photo} objects.
	 * 
	 * @return the underlying list of {@link Photo}s.
	 */
	public List<Photo> getPhotos() {
		return photos;
	}

	@Override
	public IMAGE getInstance(int index) {
		return read(photos.get(index));
	}

	@Override
	public int size() {
		return photos.size();
	}

	private IMAGE read(Photo next) {
		if (next == null)
			return null;

		InputStream stream = null;
		try {
			stream = new URL(next.getUrl()).openStream();
			return reader.read(stream);
		} catch (final IOException e) {
			throw new RuntimeException(e);
		} finally {
			try {
				if (stream != null)
					stream.close();
			} catch (final IOException e) {
				// ignore
			}
		}
	}

	@Override
	public Iterator<IMAGE> iterator() {
		return new Iterator<IMAGE>() {
			@SuppressWarnings("unchecked")
			Iterator<Photo> internal = photos.iterator();

			@Override
			public boolean hasNext() {
				return internal.hasNext();
			}

			@Override
			public IMAGE next() {
				return read(internal.next());
			}

			@Override
			public void remove() {
				internal.remove();
			}
		};
	}

	@Override
	public String toString() {
		return String.format("%s(%d images)", this.getClass().getName(), this.photos.size());
	}

	public static <IMAGE extends Image<?, IMAGE>> FlickrImageDataset<IMAGE> fromGallery(ObjectReader<IMAGE> reader,
			String apikey, String secret,
			URL galleryURL) throws Exception
	{
		return fromGallery(reader, apikey, secret, galleryURL, 0);
	}

	public static <IMAGE extends Image<?, IMAGE>> FlickrImageDataset<IMAGE> fromGallery(ObjectReader<IMAGE> reader,
			String apikey, String secret,
			URL galleryURL, int number) throws Exception
	{
		final Flickr flickr = new Flickr(apikey, secret, new REST(Flickr.DEFAULT_HOST));

		final GalleriesInterface galleriesInterface = flickr.getGalleriesInterface();
		final String galleryId = flickr.getUrlsInterface().lookupGallery(galleryURL.toString());

		final com.aetrion.flickr.galleries.SearchParameters params = new com.aetrion.flickr.galleries.SearchParameters();
		params.setGalleryId(galleryId);

		return fromGallery(reader, apikey, secret, params, number);
	}

	public static <IMAGE extends Image<?, IMAGE>> FlickrImageDataset<IMAGE> fromGallery(ObjectReader<IMAGE> reader,
			String apikey, String secret,
			com.aetrion.flickr.galleries.SearchParameters params) throws Exception
	{
		return fromGallery(reader, apikey, secret, params, 0);
	}

	@SuppressWarnings("unchecked")
	public static <IMAGE extends Image<?, IMAGE>> FlickrImageDataset<IMAGE> fromGallery(ObjectReader<IMAGE> reader,
			String apikey, String secret,
			com.aetrion.flickr.galleries.SearchParameters params, int number) throws Exception
	{
		final Flickr flickr = new Flickr(apikey, secret, new REST(Flickr.DEFAULT_HOST));

		final List<Photo> photos = new ArrayList<Photo>();
		final PhotoList first = flickr.getGalleriesInterface().getPhotos(params, 250, 0);

		if (number > 0)
			number = Math.min(number, first.getTotal());

		for (int page = 1, n = photos.size(); n < number; page++) {
			final PhotoList result = flickr.getGalleriesInterface().getPhotos(params, 250, page);
			photos.addAll(result);
			n += result.size();
		}

		return new FlickrImageDataset<IMAGE>(reader, photos);
	}

	public static void main(String[] args) throws Exception {
		final String apikey = "14a08800b264a92412b4ee36b0bff8b1";
		final String secret = "814e0bbb677c673e";

		final FlickrImageDataset<FImage> dataset =
				FlickrImageDataset.fromGallery(ImageUtilities.FIMAGE_READER, apikey,
						secret, new URL("http://www.flickr.com/photos/f3lixlovesyou/galleries/72157622280276133/"), 500);

		System.out.println(dataset);
	}
}
