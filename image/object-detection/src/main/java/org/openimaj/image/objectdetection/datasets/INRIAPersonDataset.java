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
package org.openimaj.image.objectdetection.datasets;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.commons.vfs2.FileSystemException;
import org.openimaj.data.DataUtils;
import org.openimaj.data.dataset.GroupedDataset;
import org.openimaj.data.dataset.ListBackedDataset;
import org.openimaj.data.dataset.ListDataset;
import org.openimaj.data.dataset.MapBackedDataset;
import org.openimaj.data.dataset.VFSListDataset;
import org.openimaj.experiment.annotations.DatasetDescription;
import org.openimaj.image.FImage;
import org.openimaj.image.Image;
import org.openimaj.image.ImageUtilities;
import org.openimaj.io.InputStreamObjectReader;
import org.openimaj.math.geometry.shape.Rectangle;

@DatasetDescription(
		name = "INRIAPerson",
		description = "Images of upright people in images and video. " +
				"The dataset is divided in two formats: (a) original " +
				"images with corresponding annotation files, and " +
				"(b) positive images in normalized 64x128 pixel format " +
				"(as used in the CVPR paper) with original negative images",
		creator = "Navneet Dalal",
		url = "http://pascal.inrialpes.fr/data/human/",
		downloadUrls = {
				"http://datasets.openimaj.org/INRIAPerson.zip",
		})
public class INRIAPersonDataset {
	static class NegEx {
		int id;
		Rectangle r;
	}

	public static <IMAGE extends Image<?, IMAGE>> ListDataset<IMAGE> getNegativeTrainingImages(
			InputStreamObjectReader<IMAGE> reader) throws FileSystemException
	{
		final VFSListDataset<IMAGE> images = new VFSListDataset<IMAGE>(DataUtils.getDataLocation(
				"INRIAPerson/train_64x128_H96/neg")
				.toString(), reader);

		return images;
	}

	public static <IMAGE extends Image<?, IMAGE>> ListDataset<IMAGE> getPositiveTrainingImages(
			InputStreamObjectReader<IMAGE> reader) throws FileSystemException
	{
		final VFSListDataset<IMAGE> images = new VFSListDataset<IMAGE>(DataUtils.getDataLocation(
				"INRIAPerson/train_64x128_H96/pos")
				.toString(), reader);

		return images;
	}

	public static <IMAGE extends Image<?, IMAGE>> ListDataset<IMAGE> generateNegativeExamples(int numSamplesPerImage,
			int width, int height, long seed, InputStreamObjectReader<IMAGE> reader) throws FileSystemException
	{
		final Random rng = new Random(seed);
		final ListDataset<IMAGE> images = getNegativeTrainingImages(reader);

		final List<NegEx> data = new ArrayList<NegEx>();
		for (int i = 0; i < images.size(); i++) {
			final IMAGE image = images.getInstance(i);
			final int imWidth = image.getWidth();
			final int imHeight = image.getHeight();

			for (int j = 0; j < numSamplesPerImage; j++) {
				final NegEx ex = new NegEx();
				ex.id = i;
				ex.r = generateRandomRect(rng, imWidth, imHeight, width, height);
				data.add(ex);
			}
		}

		return new ListBackedDataset<IMAGE>(new AbstractList<IMAGE>() {
			int lastId = -1;
			IMAGE lastImage;

			@Override
			public IMAGE get(int index) {
				final NegEx ex = data.get(index);

				final IMAGE image;
				if (ex.id != lastId) {
					lastImage = images.get(ex.id);
					lastId = ex.id;
				}
				image = lastImage;

				return image.extractROI(ex.r);
			}

			@Override
			public int size() {
				return data.size();
			}
		});
	}

	private static Rectangle generateRandomRect(Random rng, int imWidth, int imHeight, int width, int height) {
		final int maxx = imWidth - width;
		final int maxy = imHeight - height;

		final int x = rng.nextInt(maxx);
		final int y = rng.nextInt(maxy);

		return new Rectangle(x, y, width, height);
	}

	public static GroupedDataset<Boolean, ListDataset<FImage>, FImage> getTrainingData() throws FileSystemException {
		final MapBackedDataset<Boolean, ListDataset<FImage>, FImage> ds = new MapBackedDataset<Boolean, ListDataset<FImage>, FImage>();

		ds.put(true, getPositiveTrainingImages(ImageUtilities.FIMAGE_READER));
		ds.put(false, generateNegativeExamples(10, 64, 128, 0L, ImageUtilities.FIMAGE_READER));

		return ds;
	}
}
