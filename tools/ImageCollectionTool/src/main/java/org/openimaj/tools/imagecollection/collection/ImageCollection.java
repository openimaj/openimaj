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
	
	/**
	 * Get the number of images in this collection (might be an estimate or 0, don't rely on this)
	 * @return
	 */
	public int countImages();
	
	/**
	 * Control how an image collection is to accept or ignore a given entry of the collection
	 * @param selection
	 */
	public void setEntrySelection(ImageCollectionEntrySelection<ImageType> selection);
}
