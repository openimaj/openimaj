package org.openimaj.demos.classification;

import java.io.IOException;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.ColourSpace;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.processing.resize.ResizeProcessor;

public class CT101Tests {
	public static void main(String[] args) throws IOException {
		final Caltech101<MBFImage> data = new Caltech101<MBFImage>(ImageUtilities.MBFIMAGE_READER);

		final ResizeProcessor rp = new ResizeProcessor(200, 200, true);

		for (final String key : data.keySet()) {
			System.out.println(key);
			System.out.println(data.get(key).size());

			final MBFImage avg = new MBFImage(200, 200, ColourSpace.RGB);
			final MBFImage tmp = new MBFImage(200, 200, ColourSpace.RGB);

			for (final MBFImage img : data.get(key)) {

				final MBFImage small = img.process(rp);

				final int x = (200 - small.getWidth()) / 2;
				final int y = (200 - small.getHeight()) / 2;

				tmp.fill(RGBColour.WHITE);
				tmp.drawImage(small, x, y);

				avg.addInplace(tmp);
			}

			avg.divideInplace((float) data.get(key).size() + 1);

			DisplayUtilities.display(avg);
		}

		// final GroupedRandomSplits<String, FImage> splits = new
		// GroupedRandomSplits<String, FImage>(data, 1, 1);

		// for (final String key : data.keySet()) {
		// DisplayUtilities.display(key,
		// splits.getTestDataset().get(key).get(0),
		// splits.getTrainingDataset().get(key).get(0));
		// }

		// for (final ValidationData<GroupedDataset<String, ListDataset<FImage>,
		// FImage>> vd : splits.createIterable(5)) {
		// DisplayUtilities.display(vd.getTrainingDataset().get("accordion").get(0));
		// }
	}
}
