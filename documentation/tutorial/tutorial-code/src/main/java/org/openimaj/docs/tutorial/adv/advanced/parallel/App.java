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
package org.openimaj.docs.tutorial.adv.advanced.parallel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.openimaj.data.dataset.GroupedDataset;
import org.openimaj.data.dataset.ListDataset;
import org.openimaj.data.dataset.VFSGroupDataset;
import org.openimaj.experiment.dataset.sampling.GroupSampler;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.annotation.evaluation.datasets.Caltech101;
import org.openimaj.image.colour.ColourSpace;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.processing.resize.ResizeProcessor;
import org.openimaj.time.Timer;
import org.openimaj.util.function.Operation;
import org.openimaj.util.parallel.Parallel;
import org.openimaj.util.parallel.partition.RangePartitioner;

/**
 * OpenIMAJ Hello world!
 * 
 */
public class App {
	/**
	 * Main method
	 * 
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		// demonstrate for loop
		Parallel.forIndex(0, 10, 1, new Operation<Integer>() {
			@Override
			public void perform(Integer i) {
				System.out.println(i);
			}
		});

		// create dataset
		final VFSGroupDataset<MBFImage> allImages = Caltech101.getImages(ImageUtilities.MBFIMAGE_READER);
		final GroupedDataset<String, ListDataset<MBFImage>, MBFImage> images = GroupSampler.sample(
				allImages, 8, false);

		// The non-parallel version
		final List<MBFImage> output = new ArrayList<MBFImage>();
		final ResizeProcessor resize = new ResizeProcessor(200);
		final Timer t1 = Timer.timer();
		for (final ListDataset<MBFImage> clzImages : images.values()) {
			final MBFImage current = new MBFImage(200, 200, ColourSpace.RGB);

			for (final MBFImage i : clzImages) {
				final MBFImage tmp = new MBFImage(200, 200, ColourSpace.RGB);
				tmp.fill(RGBColour.WHITE);

				final MBFImage small = i.process(resize).normalise();
				final int x = (200 - small.getWidth()) / 2;
				final int y = (200 - small.getHeight()) / 2;
				tmp.drawImage(small, x, y);

				current.addInplace(tmp);
			}
			current.divideInplace((float) clzImages.size());
			output.add(current);
		}
		System.out.println("time " + t1.duration() + "ms");

		// first attempt at a parallel version
		output.clear();
		final Timer t2 = Timer.timer();
		for (final ListDataset<MBFImage> clzImages : images.values()) {
			final MBFImage current = new MBFImage(200, 200, ColourSpace.RGB);

			Parallel.forEach(clzImages, new Operation<MBFImage>() {
				@Override
				public void perform(MBFImage i) {
					final MBFImage tmp = new MBFImage(200, 200, ColourSpace.RGB);
					tmp.fill(RGBColour.WHITE);

					final MBFImage small = i.process(resize).normalise();
					final int x = (200 - small.getWidth()) / 2;
					final int y = (200 - small.getHeight()) / 2;
					tmp.drawImage(small, x, y);

					synchronized (current) {
						current.addInplace(tmp);
					}
				}
			});
			current.divideInplace((float) clzImages.size());
			output.add(current);
		}
		System.out.println("time " + t2.duration() + "ms");

		// better parallel version
		output.clear();
		final Timer t3 = Timer.timer();
		for (final ListDataset<MBFImage> clzImages : images.values()) {
			final MBFImage current = new MBFImage(200, 200, ColourSpace.RGB);

			Parallel.forEachPartitioned(new RangePartitioner<MBFImage>(clzImages),
					new Operation<Iterator<MBFImage>>() {
						@Override
						public void perform(Iterator<MBFImage> im) {
							final MBFImage tmpAccum = new MBFImage(200, 200, 3);
							final MBFImage tmp = new MBFImage(200, 200, ColourSpace.RGB);
							while (im.hasNext()) {
								final MBFImage i = im.next();
								tmp.fill(RGBColour.WHITE);

								final MBFImage small = i.process(resize).normalise();
								final int x = (200 - small.getWidth()) / 2;
								final int y = (200 - small.getHeight()) / 2;
								tmp.drawImage(small, x, y);
								tmpAccum.addInplace(tmp);
							}
							synchronized (current) {
								current.addInplace(tmpAccum);
							}
						}
					});
			current.divideInplace((float) clzImages.size());
			output.add(current);
		}
		System.out.println("time " + t3.duration() + "ms");

		DisplayUtilities.display("Images", output);
	}
}
