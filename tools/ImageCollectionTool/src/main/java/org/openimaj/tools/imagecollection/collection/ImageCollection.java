package org.openimaj.tools.imagecollection.collection;

import java.util.List;

import org.openimaj.image.Image;

/**
 * An image collection knows how to load itself from a given type of configuration. 
 * Image collections are iterable and they can also give all their stored images at once
 * @author ss
 *
 * @param <ImageType>
 */
public interface ImageCollection<ImageType extends Image<?,ImageType>> extends Iterable<ImageCollectionEntry<ImageType>>{
	/**
	 * Setup this collection using the provided collection config.
	 * @param config
	 * @throws ImageCollectionSetupException 
	 */
	public void setup(ImageCollectionConfig config) throws ImageCollectionSetupException;
	/**
	 * Is the contents of the provided image colleciton config sufficient for this collection to run. 
	 * 
	 * @param config
	 * @return < 0 if this collection is not useable with this configuration. >= 0 otherwise, higher numbers 
	 * give a clue as to ability to deal with configuration when compared to other collections
	 */
	public int useable(ImageCollectionConfig config);
	/**
	 * Get all images in this collection
	 * @return
	 */
	public List<ImageCollectionEntry<ImageType>> getAll();
	public int countImages();
}
