package org.openimaj.tools.imagecollection;

import java.util.List;

import org.openimaj.image.Image;

/**
 * An image collection knows how to load itself from a given type of configuration. 
 * Image collections are iterable and they can also give all their stored images at once
 * @author ss
 *
 * @param <ImageType>
 */
public interface ImageCollection<ImageType extends Image<?,ImageType>> extends Iterable<ImageType>{
	/**
	 * Setup this collection using the provided collection config.
	 * @param config
	 * @throws ImageCollectionSetupException 
	 */
	public void setup(ImageCollectionConfig config) throws ImageCollectionSetupException;
	/**
	 * Is the contents of the provided image colleciton config sufficient for this collection to run.
	 * @param config
	 * @return
	 */
	public boolean useable(ImageCollectionConfig config);
	/**
	 * Get all images in this collection
	 * @return
	 */
	public List<ImageType> getAll();
}
