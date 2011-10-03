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
package org.openimaj.tools.imagecollection.collection.webpage;

import java.io.IOException;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.tools.imagecollection.collection.ImageCollectionEntry;
import org.openimaj.tools.imagecollection.collection.ImageCollectionEntrySelection;
import org.openimaj.util.pair.IndependentPair;

public class URLImageIterator implements Iterator<ImageCollectionEntry<MBFImage>> {

	private Iterator<IndependentPair<URL, Map<String, String>>> imageList;
	private ImageCollectionEntrySelection<MBFImage> selection;

	public URLImageIterator(Set<IndependentPair<URL, Map<String, String>>> imageList2, ImageCollectionEntrySelection<MBFImage> selection) {
		this.imageList = imageList2.iterator();
		this.selection = selection;
	}

	@Override
	public boolean hasNext() {
		return imageList.hasNext();
	}

	@Override
	public ImageCollectionEntry<MBFImage> next() {
		IndependentPair<URL, Map<String, String>> urlMeta = imageList.next();
		URL u = urlMeta.firstObject();
		try {
			MBFImage image = ImageUtilities.readMBF(u.openConnection().getInputStream());
			ImageCollectionEntry<MBFImage> entry = new ImageCollectionEntry<MBFImage>();
			entry.image = image;
			entry.meta = urlMeta.secondObject();
			entry.accepted = true;
			if(this.selection!=null) entry.accepted  = selection.acceptEntry(image);
			return entry;
		} catch (IOException e) {
		}
		return null;
	}

	@Override
	public void remove() {
		this.imageList.remove();
	}

}
