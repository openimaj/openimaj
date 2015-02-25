package org.openimaj.workinprogress;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.math.util.FloatArrayStatsUtils;
import org.openimaj.video.xuggle.XuggleVideo;

public class BackgroundEstimator {
	public static void main(String[] args) throws IOException {
		final XuggleVideo xv = new XuggleVideo(new File("/Users/jon/Desktop/merlin/tunnel.mp4"));

		final List<MBFImage> frameSample = new ArrayList<MBFImage>();
		int count = 0;
		for (final MBFImage fr : xv) {
			if (count % 30 == 0)
				System.out.println(count / 30);

			if (count % 30 == 0)
				frameSample.add(fr.clone());
			count++;
		}

		final MBFImage img = new MBFImage(frameSample.get(0).getWidth(), frameSample.get(0).getHeight());
		final float[] vec = new float[frameSample.size()];
		for (int y = 0; y < img.getHeight(); y++) {
			for (int x = 0; x < img.getWidth(); x++) {
				for (int b = 0; b < 3; b++) {
					for (int i = 0; i < frameSample.size(); i++)
						vec[i] = frameSample.get(i).getBand(b).pixels[y][x];

					img.bands.get(b).pixels[y][x] = FloatArrayStatsUtils.median(vec);
				}
			}
		}

		DisplayUtilities.display(img);
		ImageUtilities.write(img, new File("/Users/jon/Desktop/merlin/tunnel-background.png"));
	}
}
