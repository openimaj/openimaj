package org.openimaj.image.dataset;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;

import org.openimaj.data.dataset.ReadableListDataset;
import org.openimaj.image.Image;
import org.openimaj.io.HttpUtils;
import org.openimaj.io.ObjectReader;
import org.openimaj.util.api.auth.common.FlickrAPIToken;

import com.aetrion.flickr.Flickr;
import com.aetrion.flickr.REST;
import com.aetrion.flickr.collections.CollectionsInterface;
import com.aetrion.flickr.collections.CollectionsSearchParameters;
import com.aetrion.flickr.photos.Extras;
import com.aetrion.flickr.photos.Photo;
import com.aetrion.flickr.photos.PhotoList;
import com.aetrion.flickr.photosets.PhotosetsInterface;

/**
 * Class to dynamically create image datasets from flickr through various api
 * calls.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 * @param <IMAGE>
 *            The type of {@link Image} instance held by the dataset.
 */
public class FlickrImageDataset<IMAGE extends Image<?, IMAGE>> extends ReadableListDataset<IMAGE> {
	/**
	 * Possible sizes of image from flickr.
	 * 
	 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
	 */
	public enum Size {
		/**
		 * The original uploaded size
		 */
		Original {
			@Override
			protected URL getURL(Photo photo) {
				try {
					return new URL(photo.getOriginalUrl());
				} catch (final Exception e) {
					throw new RuntimeException(e);
				}
			}
		},
		/**
		 * Large size
		 */
		Large {
			@Override
			protected URL getURL(Photo photo) {
				try {
					return new URL(photo.getLargeUrl());
				} catch (final Exception e) {
					throw new RuntimeException(e);
				}
			}
		},
		/**
		 * Medium size
		 */
		Medium {
			@Override
			protected URL getURL(Photo photo) {
				try {
					return new URL(photo.getMediumUrl());
				} catch (final Exception e) {
					throw new RuntimeException(e);
				}
			}
		},
		/**
		 * Small size
		 */
		Small {
			@Override
			protected URL getURL(Photo photo) {
				try {
					return new URL(photo.getSmallUrl());
				} catch (final Exception e) {
					throw new RuntimeException(e);
				}
			}
		},
		/**
		 * Thumbnail size
		 */
		Thumbnail {
			@Override
			protected URL getURL(Photo photo) {
				try {
					return new URL(photo.getThumbnailUrl());
				} catch (final Exception e) {
					throw new RuntimeException(e);
				}
			}
		},
		/**
		 * Square thumbnail size
		 */
		Square {
			@Override
			protected URL getURL(Photo photo) {
				try {
					return new URL(photo.getSmallSquareUrl());
				} catch (final Exception e) {
					throw new RuntimeException(e);
				}
			}
		};

		protected abstract URL getURL(Photo photo);
	}

	private final static Pattern GALLERY_URL_PATTERN = Pattern.compile(".*/photos/.*/galleries/[0-9]*(/|$)");
	private final static Pattern PHOTOSET_URL_PATTERN = Pattern.compile(".*/photos/.*/sets/([0-9]*)(/|$)");
	private final static Pattern COLLECTION_URL_PATTERN = Pattern.compile(".*/photos/(.*)/collections/([0-9]*)(/|$)");

	protected List<Photo> photos;
	protected Size targetSize = Size.Medium;

	protected FlickrImageDataset(ObjectReader<IMAGE> reader, List<Photo> photos) {
		super(reader);

		this.photos = photos;
	}

	/**
	 * Set the size of the images that this dataset produces.
	 * 
	 * @param size
	 *            the size
	 */
	public void setImageSize(Size size) {
		this.targetSize = size;
	}

	/**
	 * Get the size of the images that this dataset produces.
	 * 
	 * @return the size of the returned images
	 */
	public Size getImageSize() {
		return targetSize;
	}

	/**
	 * Get the underlying flickr {@link Photo} objects.
	 * 
	 * @return the underlying list of {@link Photo}s.
	 */
	public List<Photo> getPhotos() {
		return photos;
	}

	/**
	 * Get the a specific underlying flickr {@link Photo} object corresponding
	 * to a particular image instance.
	 * 
	 * @param index
	 *            the index of the instance
	 * 
	 * @return the underlying {@link Photo} corresponding to the given instance
	 *         index.
	 */
	public Photo getPhoto(int index) {
		return photos.get(index);
	}

	@Override
	public IMAGE getInstance(int index) {
		return read(photos.get(index));
	}

	@Override
	public int numInstances() {
		return photos.size();
	}

	@Override
	public String getID(int index) {
		return targetSize.getURL(photos.get(index)).toString();
	}

	private IMAGE read(Photo next) {
		if (next == null)
			return null;

		InputStream stream = null;
		try {
			stream = HttpUtils.readURL(targetSize.getURL(next));

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

	/**
	 * Create an image dataset from the flickr gallery, photoset or collection
	 * at the given url.
	 * 
	 * @param reader
	 *            the reader with which to load the images
	 * @param token
	 *            the flickr api authentication token
	 * @param url
	 *            the url of the collection/gallery/photo-set
	 * @return a {@link FlickrImageDataset} created from the given url
	 * @throws Exception
	 *             if an error occurs
	 */
	public static <IMAGE extends Image<?, IMAGE>> FlickrImageDataset<IMAGE> create(ObjectReader<IMAGE> reader,
			FlickrAPIToken token,
			URL url) throws Exception
	{
		return create(reader, token, url, 0);
	}

	/**
	 * Create an image dataset by searching flickr with the given search terms.
	 * 
	 * @param reader
	 *            the reader with which to load the images
	 * @param token
	 *            the flickr api authentication token
	 * @param searchTerms
	 *            the search terms; space separated. Prepending a term with a
	 *            "-" means that the term should not appear.
	 * @return a {@link FlickrImageDataset} created from the given url
	 * @throws Exception
	 *             if an error occurs
	 */
	public static <IMAGE extends Image<?, IMAGE>> FlickrImageDataset<IMAGE> create(ObjectReader<IMAGE> reader,
			FlickrAPIToken token, String searchTerms) throws Exception
	{
		return create(reader, token, searchTerms, 0);
	}

	/**
	 * Create an image dataset by searching flickr with the given search terms.
	 * The number of images can be limited to a subset.
	 * 
	 * @param reader
	 *            the reader with which to load the images
	 * @param token
	 *            the flickr api authentication token
	 * @param searchTerms
	 *            the search terms; space separated. Prepending a term with a
	 *            "-" means that the term should not appear.
	 * @param number
	 *            the maximum number of images to add to the dataset. Setting to
	 *            0 or less will attempt to use all the images.
	 * @return a {@link FlickrImageDataset} created from the given url
	 * @throws Exception
	 *             if an error occurs
	 */
	public static <IMAGE extends Image<?, IMAGE>> FlickrImageDataset<IMAGE> create(ObjectReader<IMAGE> reader,
			FlickrAPIToken token,
			String searchTerms, int number) throws Exception
	{
		final com.aetrion.flickr.photos.SearchParameters params = new com.aetrion.flickr.photos.SearchParameters();
		params.setText(searchTerms);

		return createFromSearch(reader, token, params, number);
	}

	/**
	 * Create an image dataset from the flickr gallery, photoset or collection
	 * at the given url. The number of images can be limited to a subset.
	 * 
	 * @param reader
	 *            the reader with which to load the images
	 * @param token
	 *            the flickr api authentication token
	 * @param url
	 *            the url of the collection/gallery/photo-set
	 * @param number
	 *            the maximum number of images to add to the dataset. Setting to
	 *            0 or less will attempt to use all the images.
	 * @return a {@link FlickrImageDataset} created from the given url
	 * @throws Exception
	 *             if an error occurs
	 */
	public static <IMAGE extends Image<?, IMAGE>> FlickrImageDataset<IMAGE> create(ObjectReader<IMAGE> reader,
			FlickrAPIToken token,
			URL url, int number) throws Exception
	{
		final String urlString = url.toString();

		if (GALLERY_URL_PATTERN.matcher(urlString).matches()) {
			return fromGallery(reader, token, urlString, number);
		} else if (PHOTOSET_URL_PATTERN.matcher(urlString).matches()) {
			return fromPhotoset(reader, token, urlString, number);
		} else if (COLLECTION_URL_PATTERN.matcher(urlString).matches()) {
			return fromCollection(reader, token, urlString, number);
		}

		throw new IllegalArgumentException("Unknown URL type " + urlString);
	}

	private static <IMAGE extends Image<?, IMAGE>> FlickrImageDataset<IMAGE> fromGallery(ObjectReader<IMAGE> reader,
			FlickrAPIToken token,
			String urlString, int number) throws Exception
	{
		final Flickr flickr = makeFlickr(token);

		final String galleryId = flickr.getUrlsInterface().lookupGallery(urlString);

		final com.aetrion.flickr.galleries.SearchParameters params = new com.aetrion.flickr.galleries.SearchParameters();
		params.setGalleryId(galleryId);

		return createFromGallery(reader, token, params, number);
	}

	private static <IMAGE extends Image<?, IMAGE>> FlickrImageDataset<IMAGE> fromPhotoset(ObjectReader<IMAGE> reader,
			FlickrAPIToken token,
			String urlString, int number) throws Exception
	{
		final Matcher matcher = PHOTOSET_URL_PATTERN.matcher(urlString);
		matcher.find();
		final String setId = matcher.group(1);

		return createFromPhotoset(reader, token, setId, number);
	}

	private static <IMAGE extends Image<?, IMAGE>> FlickrImageDataset<IMAGE> fromCollection(ObjectReader<IMAGE> reader,
			FlickrAPIToken token,
			String urlString, int number) throws Exception
	{
		final Flickr flickr = makeFlickr(token);

		final Matcher matcher = COLLECTION_URL_PATTERN.matcher(urlString);
		matcher.find();
		final String userName = matcher.group(1);
		final String collectionsId = matcher.group(2);

		final CollectionsSearchParameters params = new CollectionsSearchParameters();
		params.setCollectionId(collectionsId);
		params.setUserId(flickr.getPeopleInterface().findByUsername(userName).getId());

		return createFromCollection(reader, token, params, number);
	}

	/**
	 * Create an image dataset from a flickr gallery with the specified
	 * parameters.
	 * 
	 * @param reader
	 *            the reader with which to load the images
	 * @param token
	 *            the flickr api authentication token
	 * @param params
	 *            the parameters describing the gallery and any additional
	 *            constraints.
	 * @return a {@link FlickrImageDataset} created from the gallery described
	 *         by the given parameters
	 * @throws Exception
	 *             if an error occurs
	 */
	public static <IMAGE extends Image<?, IMAGE>> FlickrImageDataset<IMAGE> createFromGallery(ObjectReader<IMAGE> reader,
			FlickrAPIToken token,
			com.aetrion.flickr.galleries.SearchParameters params) throws Exception
	{
		return createFromGallery(reader, token, params, 0);
	}

	/**
	 * Create an image dataset from a flickr gallery with the specified
	 * parameters. The number of images can be limited to a subset.
	 * 
	 * @param reader
	 *            the reader with which to load the images
	 * @param token
	 *            the flickr api authentication token
	 * @param params
	 *            the parameters describing the gallery and any additional
	 *            constraints.
	 * @param number
	 *            the maximum number of images to add to the dataset. Setting to
	 *            0 or less will attempt to use all the images.
	 * @return a {@link FlickrImageDataset} created from the gallery described
	 *         by the given parameters
	 * @throws Exception
	 *             if an error occurs
	 */
	@SuppressWarnings("unchecked")
	public static <IMAGE extends Image<?, IMAGE>> FlickrImageDataset<IMAGE> createFromGallery(ObjectReader<IMAGE> reader,
			FlickrAPIToken token,
			com.aetrion.flickr.galleries.SearchParameters params, int number) throws Exception
	{
		final Flickr flickr = makeFlickr(token);

		params.setExtras(Extras.ALL_EXTRAS);

		final List<Photo> photos = new ArrayList<Photo>();
		final PhotoList first = flickr.getGalleriesInterface().getPhotos(params, 250, 0);
		photos.addAll(first);

		if (number > 0)
			number = Math.min(number, first.getTotal());

		for (int page = 1, n = photos.size(); n < number; page++) {
			final PhotoList result = flickr.getGalleriesInterface().getPhotos(params, 250, page);
			photos.addAll(result);
			n += result.size();
		}

		return new FlickrImageDataset<IMAGE>(reader, photos);
	}

	/**
	 * Create an image dataset from a flickr photoset.
	 * 
	 * @param reader
	 *            the reader with which to load the images
	 * @param token
	 *            the flickr api authentication token
	 * @param setId
	 *            the photoset identifier
	 * @return a {@link FlickrImageDataset} created from the gallery described
	 *         by the given parameters
	 * @throws Exception
	 *             if an error occurs
	 */
	public static <IMAGE extends Image<?, IMAGE>> FlickrImageDataset<IMAGE> createFromPhotoset(
			ObjectReader<IMAGE> reader, FlickrAPIToken token, String setId) throws Exception
	{
		return createFromPhotoset(reader, token, setId, 0);
	}

	/**
	 * Create an image dataset from a flickr photoset. The number of images can
	 * be limited to a subset.
	 * 
	 * @param reader
	 *            the reader with which to load the images
	 * @param token
	 *            the flickr api authentication token
	 * @param setId
	 *            the photoset identifier
	 * @param number
	 *            the maximum number of images to add to the dataset. Setting to
	 *            0 or less will attempt to use all the images.
	 * @return a {@link FlickrImageDataset} created from the gallery described
	 *         by the given parameters
	 * @throws Exception
	 *             if an error occurs
	 */
	@SuppressWarnings("unchecked")
	public static <IMAGE extends Image<?, IMAGE>> FlickrImageDataset<IMAGE> createFromPhotoset(
			ObjectReader<IMAGE> reader,
			FlickrAPIToken token,
			String setId, int number) throws Exception
	{
		final Flickr flickr = makeFlickr(token);

		final PhotosetsInterface setsInterface = flickr.getPhotosetsInterface();

		final List<Photo> photos = new ArrayList<Photo>();
		final PhotoList first = setsInterface.getPhotos(setId, Extras.ALL_EXTRAS, 0, 250, 0);
		photos.addAll(first);

		if (number > 0)
			number = Math.min(number, first.getTotal());

		for (int page = 1, n = photos.size(); n < number; page++) {
			final PhotoList result = setsInterface.getPhotos(setId, Extras.ALL_EXTRAS, 0, 250, page);
			photos.addAll(result);
			n += result.size();
		}

		return new FlickrImageDataset<IMAGE>(reader, photos);
	}

	/**
	 * Create an image dataset from a flickr collection with the specified
	 * parameters.
	 * 
	 * @param reader
	 *            the reader with which to load the images
	 * @param token
	 *            the flickr api authentication token
	 * @param params
	 *            the parameters describing the gallery and any additional
	 *            constraints.
	 * @return a {@link FlickrImageDataset} created from the gallery described
	 *         by the given parameters
	 * @throws Exception
	 *             if an error occurs
	 */
	public static <IMAGE extends Image<?, IMAGE>> FlickrImageDataset<IMAGE> createFromCollection(
			ObjectReader<IMAGE> reader,
			FlickrAPIToken token,
			com.aetrion.flickr.collections.CollectionsSearchParameters params) throws Exception
	{
		return createFromCollection(reader, token, params, 0);
	}

	/**
	 * Create an image dataset from a flickr collection with the specified
	 * parameters. The number of images can be limited to a subset.
	 * 
	 * @param reader
	 *            the reader with which to load the images
	 * @param token
	 *            the flickr api authentication token
	 * @param params
	 *            the parameters describing the gallery and any additional
	 *            constraints.
	 * @param number
	 *            the maximum number of images to add to the dataset. Setting to
	 *            0 or less will attempt to use all the images.
	 * @return a {@link FlickrImageDataset} created from the gallery described
	 *         by the given parameters
	 * @throws Exception
	 *             if an error occurs
	 */
	@SuppressWarnings("unchecked")
	public static <IMAGE extends Image<?, IMAGE>> FlickrImageDataset<IMAGE> createFromCollection(
			ObjectReader<IMAGE> reader,
			FlickrAPIToken token,
			com.aetrion.flickr.collections.CollectionsSearchParameters params, int number) throws Exception
	{
		final Flickr flickr = makeFlickr(token);

		params.setExtras(Extras.ALL_EXTRAS);

		List<Photo> photos = new ArrayList<Photo>();
		final CollectionsInterface collectionsInterface = flickr.getCollectionsInterface();
		final PhotoList photoList = collectionsInterface.getTree(params).getPhotoUrls(flickr.getPhotosetsInterface());
		photos.addAll(photoList);

		if (number > 0 && number < photos.size())
			photos = photos.subList(0, number);

		return new FlickrImageDataset<IMAGE>(reader, photos);
	}

	/**
	 * Create an image dataset from a flickr search with the specified
	 * parameters.
	 * 
	 * @param reader
	 *            the reader with which to load the images
	 * @param token
	 *            the flickr api authentication token
	 * @param params
	 *            the parameters describing the gallery and any additional
	 *            constraints.
	 * @return a {@link FlickrImageDataset} created from the gallery described
	 *         by the given parameters
	 * @throws Exception
	 *             if an error occurs
	 */
	public static <IMAGE extends Image<?, IMAGE>> FlickrImageDataset<IMAGE> createFromSearch(ObjectReader<IMAGE> reader,
			FlickrAPIToken token,
			com.aetrion.flickr.photos.SearchParameters params) throws Exception
	{
		return createFromSearch(reader, token, params, 0);
	}

	/**
	 * Create an image dataset from a flickr search with the specified
	 * parameters. The number of images can be limited to a subset.
	 * 
	 * @param reader
	 *            the reader with which to load the images
	 * @param token
	 *            the flickr api authentication token
	 * @param params
	 *            the parameters describing the gallery and any additional
	 *            constraints.
	 * @param number
	 *            the maximum number of images to add to the dataset. Setting to
	 *            0 or less will attempt to use all the images.
	 * @return a {@link FlickrImageDataset} created from the gallery described
	 *         by the given parameters
	 * @throws Exception
	 *             if an error occurs
	 */
	@SuppressWarnings("unchecked")
	public static <IMAGE extends Image<?, IMAGE>> FlickrImageDataset<IMAGE> createFromSearch(ObjectReader<IMAGE> reader,
			FlickrAPIToken token,
			com.aetrion.flickr.photos.SearchParameters params, int number) throws Exception
	{
		final Flickr flickr = makeFlickr(token);

		params.setExtras(Extras.ALL_EXTRAS);

		final List<Photo> photos = new ArrayList<Photo>();
		final PhotoList first = flickr.getPhotosInterface().search(params, 250, 0);
		photos.addAll(first);

		if (number > 0)
			number = Math.min(number, first.getTotal());

		for (int page = 1, n = photos.size(); n < number; page++) {
			final PhotoList result = flickr.getPhotosInterface().search(params, 250, page);
			photos.addAll(result);
			n += result.size();
		}

		return new FlickrImageDataset<IMAGE>(reader, photos);
	}

	private static Flickr makeFlickr(FlickrAPIToken token) throws ParserConfigurationException {
		if (token.secret == null)
			return new Flickr(token.apikey, new REST(Flickr.DEFAULT_HOST));
		return new Flickr(token.apikey, token.secret, new REST(Flickr.DEFAULT_HOST));
	}
}
