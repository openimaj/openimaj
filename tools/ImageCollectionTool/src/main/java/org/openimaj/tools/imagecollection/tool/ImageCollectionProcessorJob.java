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
package org.openimaj.tools.imagecollection.tool;

import java.util.ArrayList;
import java.util.List;

import org.openimaj.image.Image;
import org.openimaj.tools.imagecollection.collection.ImageCollection;
import org.openimaj.tools.imagecollection.collection.ImageCollectionEntry;
import org.openimaj.tools.imagecollection.metamapper.MetaMapper;
import org.openimaj.tools.imagecollection.processor.ImageCollectionProcessor;

public class ImageCollectionProcessorJob <T extends Image<?,T>> implements Runnable{
	
	public static class ProcessorJobEvent{
		public int imagesDone;
		public int imagesTotal;
		public boolean validTotal;
	}
	
	public static interface ProcessorJobListener{
		public void progressUpdate(ProcessorJobEvent event);
	}
	
	private ImageCollection<T> collection;
	private ImageCollectionProcessor<T> processor;
	private List<ProcessorJobListener> listeners;
	private MetaMapper mapper;

	public ImageCollectionProcessorJob(
			ImageCollection<T> collection,
			ImageCollectionProcessor<T> sink,
			MetaMapper mapper
	){
		this.collection = collection;
		this.processor = sink;
		this.listeners = new ArrayList<ProcessorJobListener>();
		this.mapper = mapper;
	}
	
	public void addListener(ProcessorJobListener listener){
		this.listeners.add(listener);
	}

	@Override
	public void run() {
		int done = 0;
		try {
			this.processor.start();
			this.mapper.start();
		} catch (Exception e) {
			return;
		}
		for(ImageCollectionEntry<T> entry: collection){
			try {
				if(entry.accepted)
				{
					String sinkOutput = this.processor.process(entry);
					mapper.mapItem(sinkOutput, entry);
				}
				ProcessorJobEvent event = new ProcessorJobEvent();
				event.imagesDone = ++done;
				event.imagesTotal = collection.countImages();
				if(event.imagesTotal < 0) event.validTotal = false;
				else event.validTotal = true;
				fireProgressUpdate(event);
			} catch (Exception e) {
			}
		}
		try {
			this.mapper.end();
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
