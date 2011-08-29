package org.openimaj.tools.imagecollection.processor;

import java.io.IOException;

import org.openimaj.image.Image;
import org.openimaj.tools.imagecollection.collection.ImageCollectionEntry;

public abstract class ImageCollectionProcessor<T extends Image<?,T>> {
	/** 
	 * Start this image sink, called before an image is stored.
	 * @throws IOException 
	 */
	public void start() throws Exception{
		
	}
	public abstract void process(ImageCollectionEntry<T> image) throws Exception;
	/** 
	 * End this image sink, no images will be stored after this call 
	 */
	public void end() throws Exception{
		
	}
}
