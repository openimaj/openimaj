package org.openimaj.tools.imagecollection;

import java.util.ArrayList;
import java.util.List;

import org.openimaj.image.Image;
import org.openimaj.tools.imagecollection.collection.ImageCollection;
import org.openimaj.tools.imagecollection.collection.ImageCollectionEntry;
import org.openimaj.tools.imagecollection.processor.ImageCollectionProcessor;

public class ImageCollectionProcessorJob <T extends Image<?,T>> implements Runnable{
	
	public static class ProcessorJobEvent{
		public int imagesDone;
		public int imagesTotal;
	}
	
	public static interface ProcessorJobListener{
		public void progressUpdate(ProcessorJobEvent event);
	}
	
	private ImageCollection<T> collection;
	private ImageCollectionProcessor<T> processor;
	private List<ProcessorJobListener> listeners;

	public ImageCollectionProcessorJob(ImageCollection<T> collection,ImageCollectionProcessor<T> sink){
		this.collection = collection;
		this.processor = sink;
		this.listeners = new ArrayList<ProcessorJobListener>();
	}
	
	public void addListener(ProcessorJobListener listener){
		this.listeners.add(listener);
	}

	@Override
	public void run() {
		int done = 0;
		try {
			this.processor.start();
		} catch (Exception e) {
			return;
		}
		for(ImageCollectionEntry<T> entry: collection){
			try {
				this.processor.process(entry);
				ProcessorJobEvent event = new ProcessorJobEvent();
				event.imagesDone = ++done;
				event.imagesTotal = collection.countImages();
				fireProgressUpdate(event);
			} catch (Exception e) {
			}
		}
		try {
			this.processor.end();
		} catch (Exception e) {
		}
	}
	
	
	
	private void fireProgressUpdate(ProcessorJobEvent event) {
		for (ProcessorJobListener listener : this.listeners) {
			listener.progressUpdate(event);
		}
	}
	
	
}
