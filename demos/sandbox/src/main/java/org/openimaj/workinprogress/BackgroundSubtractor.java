package org.openimaj.workinprogress;

import java.io.File;
import java.io.IOException;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.processing.morphology.Dilate;
import org.openimaj.image.processing.morphology.Erode;
import org.openimaj.image.processing.resize.ResizeProcessor;
import org.openimaj.video.xuggle.XuggleVideo;
import org.openimaj.video.xuggle.XuggleVideoWriter;

public class BackgroundSubtractor {
	public static void main(String[] args) throws IOException {
		final XuggleVideo xv = new XuggleVideo(new File("/Users/jon/Desktop/merlin/tunnel.mp4"));
		final FImage bg = ResizeProcessor.halfSize(
				ImageUtilities.readF(new File("/Users/jon/Desktop/merlin/tunnel-background.png"))
				);

		final XuggleVideoWriter xvw = new XuggleVideoWriter("/Users/jon/Desktop/merlin/tunnel-proc.mp4", bg.width,
				bg.height, xv.getFPS());
		for (final MBFImage frc : xv) {
			final FImage fr = ResizeProcessor.halfSize(frc.flatten());
			final MBFImage diff = diff(bg, fr);

			xvw.addFrame(diff);
			DisplayUtilities.displayName(diff, "");
		}
		xvw.close();
	}

	static MBFImage diff(FImage bg, FImage fg) {
		final FImage df = new FImage(bg.getWidth(), bg.getHeight());
		final float[][] dff = df.pixels;

		final float[][] bgfr = bg.pixels;
		final float[][] fgfr = fg.pixels;

		for (int y = 0; y < df.getHeight(); y++) {
			for (int x = 0; x < df.getWidth(); x++) {
				final float dr = bgfr[y][x] - fgfr[y][x];
				final float ssd = dr * dr;

				if (ssd < 0.03) {
					dff[y][x] = 0;
				} else {
					dff[y][x] = 1;
				}
			}
		}

		Dilate.dilate(df, 1);
		Erode.erode(df, 2);

		return df.toRGB();
	}
}
