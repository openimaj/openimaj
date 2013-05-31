package org.openimaj.demos.sandbox.image.text;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.openimaj.image.FImage;
import org.openimaj.image.MBFImage;
import org.openimaj.image.pixel.ConnectedComponent;
import org.openimaj.image.pixel.ConnectedComponent.ConnectMode;
import org.openimaj.image.pixel.Pixel;
import org.openimaj.image.processing.edges.StrokeWidthTransform;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.video.VideoDisplay;
import org.openimaj.video.VideoDisplayListener;
import org.openimaj.video.capture.VideoCapture;
import org.openimaj.video.capture.VideoCaptureException;

public class SWTTextDetector {
	static List<ConnectedComponent> findComponents(FImage image, ConnectMode mode) {
		final List<ConnectedComponent> components = new ArrayList<ConnectedComponent>();

		// Single pass method inspired by the wikipedia two-pass technique
		// http://en.wikipedia.org/wiki/Connected_component_labeling
		for (int y = 0; y < image.height; y++) {
			for (int x = 0; x < image.width; x++) {
				final float element = image.pixels[y][x];

				if (element > 0 && element != Float.POSITIVE_INFINITY) {
					final List<Pixel> neighbours = getNeighbours(image, x, y, mode);

					ConnectedComponent currentComponent = null;
					for (final Pixel p : neighbours) {
						final ConnectedComponent cc = searchPixel(p, components);
						if (cc != null) {
							if (currentComponent == null) {
								currentComponent = cc;
							} else if (currentComponent != cc) {
								currentComponent.merge(cc);
								components.remove(cc);
							}
						}
					}

					if (currentComponent == null) {
						currentComponent = new ConnectedComponent();
						components.add(currentComponent);
					}

					currentComponent.addPixel(x, y);
				}
			}
		}

		return components;
	}

	private static List<Pixel> getNeighbours(FImage image, int x, int y, ConnectMode mode) {
		final List<Pixel> neighbours = new ArrayList<Pixel>();

		switch (mode) {
		case CONNECT_8:
			if (x - 1 > 0 && y - 1 > 0 && testNeighbour(image.pixels[y][x], image.pixels[y - 1][x - 1]))
				neighbours.add(new Pixel(x - 1, y - 1));
			if (x + 1 < image.getWidth() && y - 1 > 0 && testNeighbour(image.pixels[y][x], image.pixels[y - 1][x + 1]))
				neighbours.add(new Pixel(x + 1, y - 1));
			if (x - 1 > 0 && y + 1 < image.getHeight() && testNeighbour(image.pixels[y][x], image.pixels[y + 1][x - 1]))
				neighbours.add(new Pixel(x - 1, y + 1));
			if (x + 1 < image.getWidth() && y + 1 < image.getHeight()
					&& testNeighbour(image.pixels[y][x], image.pixels[y + 1][x + 1]))
				neighbours.add(new Pixel(x + 1, y + 1));
			// Note : no break, so we fall through...
		case CONNECT_4:
			if (x - 1 > 0 && testNeighbour(image.pixels[y][x], image.pixels[y][x - 1]))
				neighbours.add(new Pixel(x - 1, y));
			if (x + 1 < image.getWidth() && testNeighbour(image.pixels[y][x], image.pixels[y][x + 1]))
				neighbours.add(new Pixel(x + 1, y));
			if (y - 1 > 0 && testNeighbour(image.pixels[y][x], image.pixels[y - 1][x]))
				neighbours.add(new Pixel(x, y - 1));
			if (y + 1 < image.getHeight() && testNeighbour(image.pixels[y][x], image.pixels[y + 1][x]))
				neighbours.add(new Pixel(x, y + 1));
			break;
		}

		return neighbours;
	}

	private static boolean testNeighbour(float centre, float neighbour) {
		return neighbour > 0 && neighbour != Float.POSITIVE_INFINITY
				&& (neighbour / centre <= 3 || centre / neighbour <= 3);
	}

	private static ConnectedComponent searchPixel(Pixel p, List<ConnectedComponent> components) {
		for (final ConnectedComponent c : components) {
			if (c.find(p))
				return c;
		}
		return null;
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

				System.out.println(t2 - t1);

				// final List<ConnectedComponent> comps = findComponents(swt,
				// ConnectMode.CONNECT_4);
				// final List<LetterCandidate> letters = filterComponents(comps,
				// swt, image);
				//
				// final List<LineCandidate> lines =
				// LineCandidate.extractLines(letters);
				// for (final LineCandidate line : lines) {
				// frame.drawShape(line.regularBoundingBox, RGBColour.RED);
				//
				// // final List<WordCandidate> words =
				// // WordCandidate.extractWords(line);
				// // for (final WordCandidate wc : words)
				// // frame.drawShape(wc.regularBoundingBox, RGBColour.BLUE);
				// }
			}

			@Override
			public void afterUpdate(VideoDisplay<MBFImage> display) {

			}
		});
	}
}
