package org.openimaj.demos.sandbox.hand;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.connectedcomponent.ConnectedComponentLabeler;
import org.openimaj.image.pixel.ConnectedComponent;
import org.openimaj.image.pixel.ConnectedComponent.ConnectMode;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.shape.Circle;
import org.openimaj.math.geometry.shape.Polygon;
import org.openimaj.math.geometry.shape.Triangle;
import org.openimaj.video.capture.VideoCapture;
import org.openimaj.video.capture.VideoCaptureException;

public class Fingers {
	static class ConvexityDefect {
		Point2d start;
		Point2d end;
		Point2d deepestPoint;
		float depth;

		public Triangle getTriangle() {
			return new Triangle(start, deepestPoint, end);
		}

		static List<ConvexityDefect> findDefects(Polygon p, Polygon hull) {
			// test orientation of hull w.r.t poly
			final int index1 = p.points.indexOf(hull.points.get(0));
			final int index2 = p.points.indexOf(hull.points.get(1));
			final int index3 = p.points.indexOf(hull.points.get(2));

			int sign = 0;
			sign += (index2 > index1) ? 1 : 0;
			sign += (index3 > index2) ? 1 : 0;
			sign += (index1 > index3) ? 1 : 0;

			final boolean reverseOri = (sign == 2) ? false : true;

			final List<ConvexityDefect> defects = new ArrayList<ConvexityDefect>();

			for (int i = 0; i < hull.points.size(); i++) {
				final ConvexityDefect defect = new ConvexityDefect();
				defect.start = hull.get(i);

				if (i == hull.points.size() - 1) {
					defect.end = hull.get(0);
				} else {
					defect.end = hull.get(i + 1);
				}

				final double dx0 = defect.end.getX() - defect.start.getX();
				final double dy0 = defect.end.getY() - defect.start.getY();
				final double scale = 1f / Math.sqrt(dx0 * dx0 + dy0 * dy0);

				float depth = 0;
				boolean isDefect = false;
				int curi = p.points.indexOf(defect.start);
				while (true) {
					if (reverseOri) {
						curi--;
						if (curi < 0)
							curi = p.points.size() - 1;
					} else {
						curi++;
						if (curi >= p.points.size())
							curi = 0;
					}

					final Point2d cur = p.points.get(curi);
					if (cur == defect.end)
						break;

					final double dx = (double) cur.getX() - (double) defect.start.getX();
					final double dy = (double) cur.getY() - (double) defect.start.getY();

					/* compute depth */
					final double dist = Math.abs(-dy0 * dx + dx0 * dy) * scale;

					if (dist > depth)
					{
						depth = (float) dist;
						defect.deepestPoint = cur;
						defect.depth = depth;
						isDefect = true;
					}
				}

				if (isDefect) {
					defects.add(defect);
				}
			}

			return defects;
		}
	}

	public static void main(String[] args) throws VideoCaptureException {
		final VideoCapture vc = new VideoCapture(320, 240);

		final JFrame frame = DisplayUtilities.displaySimple(vc.getNextFrame(), "capture");
		final ConnectedComponentLabeler ccl = new ConnectedComponentLabeler(ConnectMode.CONNECT_4);

		while (true) {
			final MBFImage cimg = vc.getNextFrame();
			final FImage gimg = cimg.flatten();
			// gimg.processInplace(new OtsuThreshold());
			gimg.threshold(0.4f);

			ccl.analyseImage(gimg);
			final ConnectedComponent hand = findBiggest(ccl.getComponents());

			if (hand != null) {
				Polygon poly = hand.toPolygon();
				poly = poly.reduceVertices(3);

				final Polygon chull = poly.calculateConvexHull();

				final List<ConvexityDefect> defects = ConvexityDefect.findDefects(poly, chull);
				// for (final ConvexityDefect cd : defects) {
				// cimg.drawShapeFilled(cd.getTriangle(), RGBColour.MAGENTA);
				// }

				final List<Point2d> tips = findTips(defects);

				final Point2d centroid = poly.calculateCentroid();
				for (final Point2d pt : tips) {
					cimg.drawLine(centroid, pt, RGBColour.RED);
					cimg.drawShape(new Circle(pt, 5), RGBColour.CYAN);
				}

				cimg.drawPolygon(poly, 1, RGBColour.RED);
				cimg.drawPolygon(chull, 1, RGBColour.BLUE);
			}

			DisplayUtilities.display(cimg, frame);
		}
	}

	private static final int MIN_FINGER_DEPTH = 20;
	private static final int MAX_FINGER_ANGLE = 60; // degrees

	private static List<Point2d> findTips(List<ConvexityDefect> defects) {
		final ArrayList<Point2d> fingerTips = new ArrayList<Point2d>();

		for (int i = 0; i < defects.size(); i++) {
			if (defects.get(i).depth < MIN_FINGER_DEPTH) // defect too shallow
				continue;

			// look at fold points on either side of a tip
			final int prevIdx = (i == 0) ? (defects.size() - 1) : (i - 1);
			final int nextIdx = (i == defects.size() - 1) ? 0 : (i + 1);

			final int angle = angleBetween(defects.get(i).start, defects.get(prevIdx).deepestPoint,
					defects.get(nextIdx).deepestPoint);
			if (angle >= MAX_FINGER_ANGLE)
				continue; // angle between finger and folds too wide

			// this point is probably a fingertip, so add to list
			fingerTips.add(defects.get(i).start);
		}

		return fingerTips;
	}

	// calculate the angle between the tip and its neighboring folds
	// (in integer degrees)
	private static int angleBetween(Point2d tip, Point2d next, Point2d prev)
	{
		return Math.abs((int) Math.round(
				Math.toDegrees(
						Math.atan2(next.getX() - tip.getX(), next.getY() - tip.getY()) -
								Math.atan2(prev.getX() - tip.getX(), prev.getY() - tip.getY()))));
	}

	static ConnectedComponent findBiggest(List<ConnectedComponent> components) {
		ConnectedComponent biggest = null;
		int size = 0;

		for (final ConnectedComponent cc : components) {
			if (cc.pixels.size() > size) {
				size = cc.pixels.size();
				biggest = cc;
			}
		}

		return biggest;
	}
}
