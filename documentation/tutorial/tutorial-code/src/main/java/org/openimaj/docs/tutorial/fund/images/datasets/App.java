package org.openimaj.docs.tutorial.fund.images.datasets;

import java.util.Map.Entry;

import org.openimaj.data.dataset.VFSGroupDataset;
import org.openimaj.data.dataset.VFSListDataset;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.dataset.BingImageDataset;
import org.openimaj.image.dataset.FlickrImageDataset;
import org.openimaj.util.api.auth.DefaultTokenFactory;
import org.openimaj.util.api.auth.common.BingAPIToken;
import org.openimaj.util.api.auth.common.FlickrAPIToken;

/**
 * OpenIMAJ Hello world!
 * 
 */
public class App {
	/**
	 * Main method
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		/**
		 * Build a dataset from images on disk
		 */
		final VFSListDataset<FImage> images = new VFSListDataset<FImage>("/images",
				ImageUtilities.FIMAGE_READER);
		System.out.println(images.size());
		DisplayUtilities.display(images.getRandomInstance(), "A random image from the dataset");
		DisplayUtilities.display("My images", images);

		/**
		 * Build a dataset from images in a zip file
		 */
		final VFSListDataset<FImage> faces = new VFSListDataset<FImage>("zip:http://datasets.openimaj.org/att_faces.zip",
				ImageUtilities.FIMAGE_READER);
		System.out.println(faces.size());
		DisplayUtilities.display("ATT faces", faces);

		/**
		 * Build a grouped dataset from images in directories in a zip file
		 */
		final VFSGroupDataset<FImage> groupedFaces = new VFSGroupDataset<FImage>(
				"zip:http://datasets.openimaj.org/att_faces.zip", ImageUtilities.FIMAGE_READER);
		for (final Entry<String, VFSListDataset<FImage>> entry : groupedFaces.entrySet()) {
			DisplayUtilities.display(entry.getKey(), entry.getValue());
		}

		/**
		 * Search for cats on Flickr
		 */
		final FlickrAPIToken flickrToken = DefaultTokenFactory.get(FlickrAPIToken.class);
		final FlickrImageDataset<FImage> cats = FlickrImageDataset.create(ImageUtilities.FIMAGE_READER, flickrToken,
				"cat", 10);
		DisplayUtilities.display("Cats", cats);

		/**
		 * Search for pics of Arnie on Bing
		 */
		final BingAPIToken bingToken = DefaultTokenFactory.get(BingAPIToken.class);
		final BingImageDataset<MBFImage> arnie = BingImageDataset.create(ImageUtilities.MBFIMAGE_READER, bingToken,
				"Arnold Schwarzenegger", 10);
		DisplayUtilities.display("Arnie", arnie);
	}
}
