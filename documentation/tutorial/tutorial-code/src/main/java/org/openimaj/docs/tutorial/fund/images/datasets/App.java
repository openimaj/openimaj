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
