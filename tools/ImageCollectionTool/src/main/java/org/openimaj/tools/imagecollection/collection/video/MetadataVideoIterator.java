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
package org.openimaj.tools.imagecollection.collection.video;

import java.util.HashMap;
import java.util.Iterator;

import org.openimaj.image.Image;
import org.openimaj.tools.imagecollection.collection.ImageCollectionEntry;
import org.openimaj.tools.imagecollection.collection.ImageCollectionEntrySelection;
import org.openimaj.video.Video;

public class MetadataVideoIterator<T extends Image<?, T>> implements Iterator<ImageCollectionEntry<T>> {

	private Video<T> video;
	private ImageCollectionEntrySelection<T> selection;
	private int frameCount = 0;

	public MetadataVideoIterator(ImageCollectionEntrySelection<T> selection, Video<T> video) {
		this.video = video;
		this.selection = selection;
	}

	@Override
	public boolean hasNext() {
		return frameCount >= 0 && video.hasNextFrame();
	}

	@Override
	public ImageCollectionEntry<T> next() {
		final T image = video.getNextFrame();
		final ImageCollectionEntry<T> entry = new ImageCollectionEntry<T>();
		entry.meta = new HashMap<String, String>();
		entry.meta.put("timestamp", "" + this.frameCount / this.video.getFPS());
		entry.accepted = selection.acceptEntry(image);
		entry.image = image;
		this.frameCount++;

		// hack to stop the iterator at the end until hasNext works properly
		if (image == null)
			frameCount = -1;

		return entry;
	}

	@Override
	public void remove() {
		// TODO Auto-generated method stub

	}

}
