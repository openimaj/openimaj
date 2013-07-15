package org.openimaj.demos.sandbox.image.text;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.pixel.ConnectedComponent;
import org.openimaj.image.pixel.Pixel;
import org.openimaj.image.processing.edges.StrokeWidthTransform;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.util.set.DisjointSetForest;
import org.openimaj.video.VideoDisplay;
import org.openimaj.video.VideoDisplayListener;
import org.openimaj.video.capture.VideoCapture;
import org.openimaj.video.capture.VideoCaptureException;

public class SWTTextDetector {
	private final static int[][] connect8 = {
			{ -1, 0 }, { 1, 0 }, { 0, -1 }, { 0, 1 }, { -1, -1 }, { 1, -1 }, { -1, 1 }, { 1, 1 } };

	static List<ConnectedComponent> findComponents(FImage image) {
		final DisjointSetForest<Pixel> forest = new DisjointSetForest<Pixel>();

		Pixel current = new Pixel();
		Pixel next = new Pixel();
		for (int y = 0; y < image.height; y++) {
			for (int x = 0; x < image.width; x++) {
				final float currentValue = image.pixels[y][x];

				if (currentValue > 0 && currentValue != Float.POSITIVE_INFINITY) {
					current.x = x;
					current.y = y;

					if (forest.makeSet(current) != null)
						current = current.clone();

					for (int i = 0; i < connect8.length; i++) {
						final int xx = x + connect8[i][0];
						final int yy = y + connect8[i][1];

						if (xx >= 0 && xx < image.width - 1 && yy >= 0 && yy < image.height - 1) {
							final float value = image.pixels[yy][xx];

							if (value > 0 && value != Float.POSITIVE_INFINITY) {
								next.x = xx;
								next.y = yy;

								if (forest.makeSet(next) != null)
									next = next.clone();

								// if (Math.max(currentValue, value) /
								// Math.min(currentValue, value) < 3)
								forest.union(current, next);
							}
						}
					}
				}
			}
		}

		final List<ConnectedComponent> components = new ArrayList<ConnectedComponent>();
		for (final Set<Pixel> pixels : forest.getSubsets()) {
			components.add(new ConnectedComponent(pixels));
		}

		return components;
	}

	static List<LetterCandidate> filterComponents(List<ConnectedComponent> components, FImage swt, FImage image) {
		final List<LetterCandidate> output = new ArrayList<LetterCandidate>();

		final DescriptiveStatistics stats = new DescriptiveStatistics();
		for (final ConnectedComponent cc : components) {
			computeStats(stats, cc, swt);

			final double mean = stats.getMean();
			final double variance = stats.getVariance();
			final double median = stats.getPercentile(50);

			// test variance of stroke width
			if (variance > 0.5 * mean)
				continue;

			// test aspect ratio
			final double aspect = cc.calculateOrientatedBoundingBoxAspectRatio();
			if (aspect < 0.1 || aspect > 10)
				continue;

			// test diameter
			final Rectangle bb = cc.calculateRegularBoundingBox();
			final float diameter = Math.max(bb.width, bb.height);
			if (diameter / median > 10)
				continue;

			// check occlusion

			// check height

			// FIXME
			if (cc.pixels.size() < 150)
				continue;

			output.add(new LetterCandidate(cc, (float) median, image));
		}

		return output;
	}

	private static void computeStats(DescriptiveStatistics stats, ConnectedComponent cc, FImage swt) {
		stats.clear();
		for (final Pixel p : cc.pixels) {
			stats.addValue(swt.pixels[p.y][p.x]);
		}
	}

	// public static void main(String[] args) throws MalformedURLException,
	// IOException {
	// // final URL url = new URL(
	// //
	// "https://9b409aec-a-62cb3a1a-s-sites.googlegroups.com/site/roboticssaurav/strokewidthnokia/Story5060.990161001243807486.jpg?attachauth=ANoY7crJITCIECLK_UI3fxtf6oNy2bSChLIiSCorObAvxmY-lopWfzxU4S-6trWLiCZXJbZrbW0C7qSBmGJ5Ga2lLoQ4MvycWlDKiNCsfgS1EX9KlP0AYPTpdg5KGVbOp1IeHjlvRN9WXM2JfxksXXwk9cEETfocogxjQhFhXbjDP-uUJ7UCj767L4D0piLI6kj-sR-qz867C93988vW_haS9GPW2C4LIV3Th1ql3DseoBY8q0DBuRetNhdwamRyyVYtxSkXN3WbwbdaPFYS9dcFimSrsmqvdQ%3D%3D&attredirects=0");
	// final URL url = new
	// URL("http://kevinlambwrites.files.wordpress.com/2012/01/this-is-a-good-sign-official.jpg");
	// final MBFImage colimage =
	// ResizeProcessor.halfSize(ImageUtilities.readMBF(url));
	// final FImage image = Transforms.calculateIntensityNTSC(colimage);
	//
	// final FImage swt = image.process(new StrokeWidthTransform(true, 1));
	// DisplayUtilities.display(StrokeWidthTransform.normaliseImage(swt));
	//
	// final List<ConnectedComponent> comps = findComponents(swt,
	// ConnectMode.CONNECT_4);
	// final MBFImage colImg = swt.toRGB();
	// for (final ConnectedComponent c : comps) {
	// final BlobRenderer<Float[]> r = new BlobRenderer<Float[]>(colImg,
	// RGBColour.randomColour());
	// r.process(c);
	// }
	// DisplayUtilities.display(colImg);
	//
	// colImg.fill(RGBColour.BLACK);
	// final List<LetterCandidate> letters = filterComponents(comps, swt,
	// image);
	// for (final LetterCandidate c : letters) {
	// final BlobRenderer<Float[]> r = new BlobRenderer<Float[]>(colImg,
	// RGBColour.randomColour());
	// r.process(c.cc);
	// }
	// DisplayUtilities.display(colImg);
	//
	// final MBFImage out = colImg.clone();
	// final List<LineCandidate> lines = LineCandidate.extractLines(letters);
	// for (final LineCandidate line : lines) {
	// out.drawShape(line.regularBoundingBox, RGBColour.RED);
	// // for (int i = 0; i < line.letters.size() - 1; i++) {
	// // out.drawLine(line.letters.get(i).centroid, line.letters.get(i +
	// // 1).centroid, RGBColour.randomColour());
	// // }
	//
	// // final List<WordCandidate> words =
	// // WordCandidate.extractWords(line);
	// // for (final WordCandidate wc : words)
	// // out.drawShape(wc.regularBoundingBox, RGBColour.BLUE);
	// }
	// DisplayUtilities.display(out);
	// }

	public static void main(String[] args) throws VideoCaptureException {
		VideoDisplay.createVideoDisplay(new VideoCapture(640, 480)).addVideoListener(new VideoDisplayListener<MBFImage>()
		{
			@Override
			public void beforeUpdate(MBFImage frame) {
				if (frame == null)
					return;

				final FImage image = frame.flatten().normalise();
				final long t1 = System.currentTimeMillis();
				final FImage swt = image.process(new StrokeWidthTransform(true, 2));
				final long t2 = System.currentTimeMillis();
				final List<ConnectedComponent> comps = findComponents(swt);
				final long t3 = System.currentTimeMillis();
				System.out.println((t2 - t1) + "\t" + (t3 - t2));

				DisplayUtilities.displayName(StrokeWidthTransform.normaliseImage(swt), "SWT");

				final List<LetterCandidate> letters = filterComponents(comps, swt, image);

				final List<LineCandidate> lines = LineCandidate.extractLines(letters);
				for (final LineCandidate line : lines) {
					frame.drawShape(line.regularBoundingBox, RGBColour.RED);

					final List<WordCandidate> words = WordCandidate.extractWords(line);
					for (final WordCandidate wc : words)
						frame.drawShape(wc.regularBoundingBox, RGBColour.BLUE);
				}
			}

			@Override
			public void afterUpdate(VideoDisplay<MBFImage> display) {

			}
		});
	}
}
