package org.openimaj.tools.imagecollection;

import org.openimaj.image.Image;
import org.openimaj.tools.imagecollection.collection.ImageCollection;
import org.openimaj.tools.imagecollection.collection.ImageCollectionEntry;
import org.openimaj.tools.imagecollection.processor.ImageCollectionProcessor;

public class ImageCollectionProcessorJob <T extends Image<?,T>> implements Runnable{
	private ImageCollection<T> collection;
	private ImageCollectionProcessor<T> processor;

	public ImageCollectionProcessorJob(ImageCollection<T> collection,ImageCollectionProcessor<T> sink){
		this.collection = collection;
		this.processor = sink;
	}

	@Override
	public void run() {
		for(ImageCollectionEntry<T> entry: collection){
			try {
				this.processor.process(entry);
			} catch (Exception e) {
			}
		}
	}
	
	
}
