package org.openimaj.demos.sandbox.tldcpp.videotld;

import java.util.List;

import org.openimaj.demos.sandbox.tldcpp.detector.NNClassifier;
import org.openimaj.demos.sandbox.tldcpp.detector.NormalizedPatch;
import org.openimaj.demos.sandbox.tldcpp.videotld.TLDMain.Command;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.processing.resize.ResizeProcessor;
import org.openimaj.image.typography.hershey.HersheyFont;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.video.VideoDisplay;
import org.openimaj.video.VideoDisplay.Mode;
import org.openimaj.video.VideoDisplayListener;
import org.openimaj.video.tracking.klt.Feature;

public class TLDVideoListener implements VideoDisplayListener<MBFImage> {

	private TLDMain tldMain;
	private boolean reuseFrameOnce;
	private boolean skipProcessingOnce;
	private int currentFrame;

	public TLDVideoListener(TLDMain tldMain) {
		this.tldMain = tldMain;
		this.reuseFrameOnce = false;
		this.skipProcessingOnce = false;
		currentFrame = 0;
	}

	@Override
	public void beforeUpdate(MBFImage frame) {
		if (this.tldMain.selector.drawRect(frame)) {
			return;
		}
		final long tic = System.currentTimeMillis();
		currentFrame++;
		FImage grey = null;
		if (!reuseFrameOnce) {
			grey = frame.flatten();
		}

		if (!skipProcessingOnce) {
			tldMain.tld.processImage(grey);
		} else {
			skipProcessingOnce = false;
		}

		if (tldMain.printResults != null) {
			if (tldMain.tld.currBB != null) {
				tldMain.resultsFile.printf("%d %.2f %.2f %.2f %.2f %f\n", currentFrame - 1, tldMain.tld.currBB.x,
						tldMain.tld.currBB.y, tldMain.tld.currBB.width, tldMain.tld.currBB.height, tldMain.tld.currConf);
			} else {
				tldMain.resultsFile.printf("%d NaN NaN NaN NaN NaN\n", currentFrame - 1);
			}
		}

		final float fps = 1 / ((System.currentTimeMillis() - tic) / 1000f);

		final boolean confident = (tldMain.tld.currConf >= tldMain.threshold) ? true : false;

		if (tldMain.showOutput || tldMain.saveDir != null) {
			String learningString = "";
			if (tldMain.tld.isLearning()) {
				learningString = "learning";
			}

			final String string = String.format("#%d,Posterior %.2f; fps: %.2f, #numwindows:%d, %s", currentFrame - 1,
					tldMain.tld.currConf, fps, tldMain.tld.detectorCascade.getNumWindows(), learningString);
			final Float[] yellow = RGBColour.YELLOW;
			final Float[] blue = RGBColour.BLUE;
			final Float[] red = RGBColour.RED;

			if (tldMain.tld.currBB != null) {
				final Float[] rectangleColor = confident ? blue : yellow;
				// cvRectangle(img, tld.currBB.tl(), tld.currBB.br(),
				// rectangleColor, 8, 8, 0);
				frame.drawShape(tldMain.tld.currBB, rectangleColor);
				if (tldMain.markerMode && this.tldMain.tld.medianFlowTracker.featuresTrackedToBfromA != null) {
					// Draw the tracked points
					final Feature[] from = this.tldMain.tld.medianFlowTracker.featuresTrackedToBfromA.features;
					final Feature[] to = this.tldMain.tld.medianFlowTracker.featuresTrackedToAviaB.features;
					for (int i = 0; i < from.length; i++) {
						frame.drawLine(from[i], to[i], 3, red);
					}
				}
			}

			drawPositivePatches();

			final HersheyFont font = HersheyFont.ROMAN_SIMPLEX;
			frame.drawText(string, 25, 25, font, 12);

		}

		if (reuseFrameOnce) {
			reuseFrameOnce = false;
		}

	}

	private void drawPositivePatches() {
		final NNClassifier nnClass = tldMain.tld.detectorCascade.getNNClassifier();
		final List<NormalizedPatch> patches = nnClass.getPositivePatches();
		final Rectangle inDim = new Rectangle(0, 0, NormalizedPatch.TLD_PATCH_SIZE, NormalizedPatch.TLD_PATCH_SIZE);
		final int X = 5;
		final int Y = Math.max(6, (patches.size() / X) + 1);
		final FImage out = new FImage(50 * X, 50 * Y);
		out.fill(1f);
		int i = 0;
		final Rectangle otRect = new Rectangle(0, 0, 50, 50);
		for (final NormalizedPatch normalizedPatch : patches) {
			otRect.x = (i % X) * 50;
			otRect.y = (i / X) * 50;
			if ((i / X) >= Y)
				break;
			ResizeProcessor.zoom(normalizedPatch.normalisedPatch, inDim, out, otRect);
			i++;
		}
		DisplayUtilities.displayName(out, "patches", true);
	}

	@Override
	public void afterUpdate(VideoDisplay<MBFImage> display) {
		if (this.tldMain.command == Command.NONE) {
		}
		else if (this.tldMain.command == Command.ALTERNATING) {
		}
		else if (this.tldMain.command == Command.CLEAR) {
			this.tldMain.tld.release();
		}
		else if (this.tldMain.command == Command.SELECT) {
			this.tldMain.disp.setMode(Mode.PAUSE);
			this.tldMain.selector.selectBoundingBox();
		}
		else if (this.tldMain.command == Command.LEARNING) {
			tldMain.tld.learningEnabled = !tldMain.tld.learningEnabled;
		}
		tldMain.command = Command.NONE;
	}

}
