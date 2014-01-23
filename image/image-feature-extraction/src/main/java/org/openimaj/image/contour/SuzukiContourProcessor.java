package org.openimaj.image.contour;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openimaj.citation.annotation.Reference;
import org.openimaj.citation.annotation.ReferenceType;
import org.openimaj.image.FImage;
import org.openimaj.image.analyser.ImageAnalyser;
import org.openimaj.image.pixel.Pixel;
import org.openimaj.math.geometry.shape.Polygon;
import org.openimaj.util.function.Operation;
import org.openimaj.util.pair.IndependentPair;

/**
 * Given a binary image (1-connected and 0-connected regions) detect contours
 * and provide both the contours and a hierarchy of contour membership.
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 */
@Reference(
		type = ReferenceType.Article,
		author = { "Suzuki, S.", "Abe, K." },
		title = "Topological Structural Analysis of Digitized Binary Image by Border Following",
		year = "1985",
		journal = "Computer Vision, Graphics and Image Processing",
		pages = { "32", "46" },
		month = "January",
		number = "1",
		volume = "30")
public class SuzukiContourProcessor implements ImageAnalyser<FImage> {
	/**
	 * The border type
	 * 
	 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
	 * 
	 */
	public static enum BorderType {
		HOLE,
		OUTER
	}

	/**
	 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
	 * 
	 */
	public static class Border extends Polygon {
		/**
		 * 
		 */
		public BorderType type;
		/**
		 * sub borders
		 */
		public List<Border> children = new ArrayList<Border>();
		/**
		 * The parent border, might be null
		 */
		public Border parent;

		/**
		 * where the border starts
		 */
		public Pixel start;

		/**
		 * @param type
		 */
		private Border(BorderType type) {
			this.type = type;
			this.start = new Pixel(0, 0);
		}

		private Border(BorderType type, int x, int y) {
			this.type = type;
			this.start = new Pixel(x, y);
		}

		private Border(BorderType type, Pixel p) {
			this.type = type;
			this.start = p;
		}

		private Border(int x, int y) {
			this.type = null;
			this.start = new Pixel(x, y);
		}

		private void setParent(Border bp) {
			this.parent = bp;
			bp.children.add(this);
		}

		@Override
		public String toString() {
			final StringWriter border = new StringWriter();
			final PrintWriter pw = new PrintWriter(border);
			pw.println(String.format("[%s] %s", this.type, this.points));
			for (final Border child : this.children) {
				pw.print(child);
			}
			pw.flush();
			return border.toString();
		}

	}

	/**
	 * the root border detected
	 */
	public Border root;

	@Override
	public void analyseImage(final FImage image) {
		this.root = findContours(image.clone());
	}

	/**
	 * 
	 * @param image
	 * @return Detect borders hierarcically in this binary image. Note the image
	 *         is changed while borders are found
	 */
	public static Border findContours(final FImage image) {
		final float nbd[] = new float[] { 2 };
		final float lnbd[] = new float[] { 0 };
		final Border root = new Border(BorderType.HOLE);
		final Map<Float, Border> borderMap = new HashMap<Float, Border>();
		borderMap.put(lnbd[0], root);
		final BorderFollowingStrategy borderFollow = new MooreNeighborStrategy();
		for (int i = 0; i < image.height; i++) {
			for (int j = 0; j < image.width; j++) {
				final boolean isOuter = isOuterBorderStart(image, i, j);
				final boolean isHole = isHoleBorderStart(image, i, j);
				if (!isOuter && !isHole)
					continue;
				final Border border = new Border(j, i);
				final Border borderPrime = borderMap.get(lnbd[0]);
				final Pixel from = new Pixel(j, i);
				if (isOuter) {
					from.x -= 1;
					border.type = BorderType.OUTER;
					switch (borderPrime.type) {
					case OUTER:
						border.setParent(borderPrime.parent);
						break;
					case HOLE:
						border.setParent(borderPrime);
						break;
					}
				}
				else { // if(isHole)
					from.x += 1;
					border.type = BorderType.HOLE;
					switch (borderPrime.type) {
					case OUTER:
						border.setParent(borderPrime);
						break;
					case HOLE:
						border.setParent(borderPrime.parent);
						break;
					}
				}
				borderFollow.directedBorder(image, new Pixel(j, i), from,
						new Operation<IndependentPair<Pixel, DIRECTION>>() {

							@Override
							public void perform(IndependentPair<Pixel, DIRECTION> object) {
								final Pixel p = object.firstObject();
								final DIRECTION d = object.secondObject();
								border.points.add(p);
								if (d != DIRECTION.WEST && crossesEastBorder(image, p)) {
									image.setPixel(p.x, p.y, -nbd[0]);
								} else if (image.getPixel(p) == 1f) {
									// only set if the pixel has not been
									// visited before!
									image.setPixel(p.x, p.y, nbd[0]);
								}
							}

						});
				borderMap.put(nbd[0], border);
				lnbd[0] = nbd[0];
				nbd[0] += 1;

			}
		}
		return root;
	}

	private static boolean crossesEastBorder(final FImage image, final Pixel p) {
		return image.getPixel(p) != 0 && (p.x == image.width - 1 || image.getPixel(p.x + 1, p.y) == 0);
	}

	private static boolean isOuterBorderStart(FImage image, int i, int j) {
		return (image.pixels[i][j] == 1 && (j == 0 || image.pixels[i][j - 1] == 0));
	}

	private static boolean isHoleBorderStart(FImage image, int i, int j) {
		return (image.pixels[i][j] >= 1 && (j == image.width - 1 || image.pixels[i][j + 1] == 0));
	}

}
