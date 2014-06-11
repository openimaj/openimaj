package org.openimaj.image.calibration;

import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.openimaj.image.FImage;
import org.openimaj.image.MBFImage;
import org.openimaj.image.analyser.ImageAnalyser;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.contour.Contour;
import org.openimaj.image.contour.SuzukiContourProcessor;
import org.openimaj.image.processing.algorithm.EqualisationProcessor;
import org.openimaj.image.processing.algorithm.MeanCenter;
import org.openimaj.image.processing.morphology.Dilate;
import org.openimaj.image.processing.threshold.AdaptiveLocalThresholdMean;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.math.geometry.shape.Circle;
import org.openimaj.math.geometry.shape.PointList;
import org.openimaj.math.geometry.shape.Polygon;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.util.pair.FloatIntPair;
import org.openimaj.video.VideoDisplay;
import org.openimaj.video.VideoDisplayAdapter;
import org.openimaj.video.capture.VideoCapture;

/**
 * This is a port/reworking of the OpenCV chessboard corner finder algorithm to
 * OpenIMAJ. The key image-processing algorithms use the OpenIMAJ equivalents,
 * but the code to extract the board and assess the correctness is purely based
 * on a (slightly sanitised) port of the original OpenCV code.
 * <p>
 * This is improved variant of chessboard corner detection algorithm that uses a
 * graph of connected quads. It is based on the code contributed by Vladimir
 * Vezhnevets and Philip Gruebele. Here is the copyright notice from the
 * original Vladimir's code:
 * <p>
 * The algorithms developed and implemented by Vezhnevets Vldimir aka Dead Moroz
 * (vvp@graphics.cs.msu.ru) See <a
 * href="http://graphics.cs.msu.su/en/research/calibration/opencv.html"
 * >http://graphics.cs.msu.su/en/research/calibration/opencv.html</a> for
 * detailed information.
 * <p>
 * Reliability additions and modifications made by Philip Gruebele.
 * <p>
 * Some further improvements for detection of partially occluded boards at
 * non-ideal lighting conditions have been made by Alex Bovyrin and Kurt
 * Kolonige.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class ChessboardCornerFinder implements ImageAnalyser<FImage> {
	private static final class Corner extends Point2dImpl {
		int row; // Board row index
		int count; // Number of neighbor corners
		Corner[] neighbours = new Corner[4];

		public Corner(Point2d point2d) {
			super(point2d);
		}

		FloatIntPair meanDist()
		{
			float sum = 0;
			int n = 0;
			for (int i = 0; i < 4; i++)
			{
				if (neighbours[i] != null)
				{
					final float dx = neighbours[i].x - x;
					final float dy = neighbours[i].y - y;
					sum += Math.sqrt(dx * dx + dy * dy);
					n++;
				}
			}
			return new FloatIntPair(sum / Math.max(n, 1), n);
		}
	}

	private static final class Quad {
		/**
		 * The group that the quad belongs
		 */
		int group_idx = -1;

		/**
		 * The corners
		 */
		Corner[] corners = new Corner[4];

		/**
		 * Minimum edge length in pixels
		 */
		float minEdgeLength;

		/**
		 * Actual number of neighbours
		 */
		int count;

		/**
		 * The neighbours
		 */
		Quad[] neighbours = new Quad[4];

		/**
		 * has the quad been sorted?
		 */
		boolean ordered;
		/**
		 * The row position
		 */
		int row;
		/**
		 * The column position
		 */
		int col;
	}

	private static final Logger logger = Logger.getLogger(ChessboardCornerFinder.class);

	private static final int MIN_DILATIONS = 0;
	private static final int MAX_DILATIONS = 7;

	/**
	 * Should filtering be enabled
	 */
	boolean filterQuads = true;

	/**
	 * Minimum area of quad if filtering
	 */
	private double minSize = 25;

	/**
	 * Maximum approximation distance
	 */
	private int maxApproxLevel = 7;

	/**
	 * Pre-process with histogram equalisation
	 */
	boolean histogramEqualise = false;

	/**
	 * Should mean adaptive thresholding be used?
	 */
	boolean adaptiveThreshold = false;

	/**
	 * Set if a fast check for the pattern be performed to bail early
	 */
	final FastChessboardDetector fastDetector;

	/**
	 * Number of blocks across the pattern
	 */
	private int patternWidth;

	/**
	 * Number of blocks down the pattern
	 */
	private int patternHeight;

	/**
	 * Was the complete pattern found?
	 */
	boolean found = false;

	/**
	 * The final corners
	 */
	private List<Point2dImpl> out_corners = new ArrayList<Point2dImpl>();

	/**
	 * Options for controlling how the corner finder works
	 * 
	 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
	 * 
	 */
	public static enum Options {
		/**
		 * Apply filtering to remove unlikely quads from detection
		 */
		FILTER_QUADS,
		/**
		 * Pre-process the image by performing histogram equalisation
		 */
		HISTOGRAM_EQUALISE,
		/**
		 * Perform adaptive (mean local) thresholding, rather than global
		 */
		ADAPTIVE_THRESHOLD,
		/**
		 * Perform the fast check to detect a pattern and bail early
		 */
		FAST_CHECK;
	}

	/**
	 * Construct with the given pattern size and options set.
	 * 
	 * @param patternWidth
	 *            the pattern width
	 * @param patternHeight
	 *            the pattern height
	 * @param opts
	 *            the options
	 */
	public ChessboardCornerFinder(int patternWidth, int patternHeight, Options... opts)
	{
		this.patternWidth = patternWidth;
		this.patternHeight = patternHeight;

		final EnumSet<Options> es = EnumSet.noneOf(Options.class);
		for (final Options o : opts)
			es.add(o);

		this.filterQuads = es.contains(Options.FILTER_QUADS);
		this.histogramEqualise = es.contains(Options.HISTOGRAM_EQUALISE);
		this.adaptiveThreshold = es.contains(Options.ADAPTIVE_THRESHOLD);

		if (es.contains(Options.FAST_CHECK))
			fastDetector = new FastChessboardDetector(patternWidth, patternHeight);
		else
			fastDetector = null;
	}

	@Override
	public void analyseImage(FImage image) {
		// reset state:
		this.found = false;
		this.out_corners.clear();

		if (histogramEqualise)
			image = image.process(new EqualisationProcessor());

		int prev_sqr_size = 0;

		if (fastDetector != null) {
			fastDetector.analyseImage(image);

			if (!fastDetector.chessboardDetected()) {
				return;
			}
		}

		for (int k = 0; k < 6; k++) {
			for (int dilations = MIN_DILATIONS; dilations < MAX_DILATIONS; dilations++) {
				if (found)
					break;

				FImage threshImg;
				if (adaptiveThreshold) {
					final int block_size = (int) (Math.round(prev_sqr_size == 0 ?
							Math.min(image.width, image.height) * (k % 2 == 0 ? 0.2 : 0.1) : prev_sqr_size * 2) | 1);

					// adaptive mean thresholding
					threshImg = image.process(new AdaptiveLocalThresholdMean(block_size, (k / 2) * 5));
					if (dilations > 0) {
						Dilate.dilate(threshImg, dilations - 1);
					}
				} else {
					threshImg = image.clone().threshold(MeanCenter.patchMean(image.pixels) - 10f / 255f);
					Dilate.dilate(threshImg, dilations);
				}

				// draw a border to allow us to find quads that go off the edge
				// of the image
				threshImg.drawShape(new Rectangle(1, 1, threshImg.width - 3, threshImg.height - 3), 3, 1f);

				final List<Quad> quads = extractQuads(threshImg);

				// not found...
				if (quads.size() <= 0)
					continue;

				findQuadNeighbours(quads);

				for (int group_idx = 0;; group_idx++)
				{
					final List<Quad> quad_group = findConnectedQuads(quads, group_idx);
					int count = quad_group.size();

					final int icount = count;
					if (count == 0)
						break;

					// order the quad corners globally
					// maybe delete or add some
					logger.trace("Starting ordering of inner quads");
					count = orderFoundConnectedQuads(quad_group, quads);
					logger.trace(String.format("Orig count: %d  After ordering: %d", icount, count));

					if (count == 0)
						continue; // haven't found inner quads

					// If count is more than it should be, this will remove
					// those quads which cause maximum deviation from a nice
					// square pattern.
					count = cleanFoundConnectedQuads(quad_group);
					logger.trace(String.format("Connected group: %d  orig count: %d cleaned: %d", group_idx, icount,
							count));

					final List<Corner> corner_group = new ArrayList<Corner>();
					count = checkQuadGroup(quad_group, corner_group);
					logger.trace(String.format("Connected group: %d  count: %d  cleaned: %d", group_idx, icount, count));

					int n = count > 0 ? patternWidth * patternHeight : -count;
					n = Math.min(n, patternWidth * patternHeight);
					float sum_dist = 0;
					int total = 0;

					for (int i = 0; i < n; i++)
					{
						final FloatIntPair pair = corner_group.get(i).meanDist();
						final float avgi = pair.first;
						final int ni = pair.second;
						sum_dist += avgi * ni;
						total += ni;
					}
					prev_sqr_size = Math.round(sum_dist / Math.max(total, 1));

					if (count > 0 || (out_corners.size() > 0 && -count > out_corners.size()))
					{
						// copy corners to output array
						out_corners.clear();
						for (int i = 0; i < n; i++)
							out_corners.add(new Point2dImpl(corner_group.get(i)));

						if (count == patternWidth * patternHeight && checkBoardMonotony())
						{
							found = true;
							break;
						}
					}
				} // grp
			} // dilations
		} // k

		if (found)
			found = checkBoardMonotony();

		// check that none of the found corners is too close to the image
		// boundary
		if (found)
		{
			final int BORDER = 8;
			int k;
			for (k = 0; k < patternWidth * patternHeight; k++)
			{
				if (out_corners.get(k).x <= BORDER || out_corners.get(k).x > image.width - BORDER ||
						out_corners.get(k).y <= BORDER || out_corners.get(k).y > image.height - BORDER)
					break;
			}

			found = k == patternWidth * patternHeight;
		}

		if (found && patternHeight % 2 == 0 && patternWidth % 2 == 0)
		{
			final int last_row = (patternHeight - 1) * patternWidth;
			final double dy0 = out_corners.get(last_row).y - out_corners.get(0).y;
			if (dy0 < 0)
			{
				int i;
				final int n = patternWidth * patternHeight;
				for (i = 0; i < n / 2; i++)
				{
					Collections.swap(out_corners, i, n - i - 1);
				}
			}
		}

		if (found)
		{
			// cv::Ptr<CvMat> gray;
			// if( CV_MAT_CN(img->type) != 1 )
			// {
			// gray = cvCreateMat(img->rows, img->cols, CV_8UC1);
			// cvCvtColor(img, gray, CV_BGR2GRAY);
			// }
			// else
			// {
			// gray = cvCloneMat(img);
			// }
			// int wsize = 2;
			// cvFindCornerSubPix( gray, out_corners,
			// pattern_size.width*pattern_size.height,
			// cvSize(wsize, wsize), cvSize(-1,-1),
			// cvTermCriteria(CV_TERMCRIT_EPS+CV_TERMCRIT_ITER, 15, 0.1));
		}
	}

	/**
	 * Returns corners in clockwise order corners don't necessarily start at
	 * same position on quad (e.g., top left corner)
	 * 
	 * @param threshImg
	 *            the binary image
	 * @return the extracted Quads
	 */
	private List<Quad> extractQuads(final FImage threshImg) {
		// if filtering is enabled, we try to guess the board contour containing
		// all the relevant quads
		final TObjectIntHashMap<Contour> counters = new TObjectIntHashMap<Contour>();
		// cornerList is the list of valid quads (prior to
		// filtering out those with the wrong parent if applicable)
		final List<Corner[]> cornerList = new ArrayList<Corner[]>();
		final Map<Corner[], Contour> parentMapping = new HashMap<Corner[], Contour>();
		Contour board = null;

		// extract contours
		final Contour contours = SuzukiContourProcessor.findContours(threshImg);

		for (final Contour c : contours.contourIterable()) {
			final Rectangle bounds = c.calculateRegularBoundingBox();

			// skip small regions
			if (!(c.isHole() && bounds.width * bounds.height >= minSize))
				continue;

			// try and make an approximated polygon with 4 vertices
			Polygon p = null;
			for (int approxLevel = 1; approxLevel <= maxApproxLevel; approxLevel++) {
				p = c.reduceVertices(approxLevel);
				if (p.nVertices() == 4)
					break;
			}

			// test polygon for correctness
			if (p != null && p.nVertices() == 4 && p.isConvex() && p.isSimple()) {
				final double perimeter = p.calculatePerimeter();
				final double area = p.calculateArea();

				final Corner[] pt = new Corner[4];
				for (int i = 0; i < 4; i++)
					pt[i] = new Corner(p.points.get(i));

				float dx = pt[0].x - pt[2].x;
				float dy = pt[0].y - pt[2].y;
				final double d1 = Math.sqrt(dx * dx + dy * dy);

				dx = pt[1].x - pt[3].x;
				dy = pt[1].y - pt[3].y;
				final double d2 = Math.sqrt(dx * dx + dy * dy);

				dx = pt[0].x - pt[1].x;
				dy = pt[0].y - pt[1].y;
				final double d3 = Math.sqrt(dx * dx + dy * dy);
				dx = pt[1].x - pt[2].x;
				dy = pt[1].y - pt[2].y;
				final double d4 = Math.sqrt(dx * dx + dy * dy);

				if (!filterQuads
						||
						(d3 * 4 > d4 && d4 * 4 > d3 && d3 * d4 < area * 1.5 && area > minSize && d1 >= 0.15 * perimeter && d2 >= 0.15 * perimeter))
				{
					final Contour parent = c.parent;
					counters.adjustOrPutValue(parent, 1, 1);

					// basic idea is that the board should have more holes in it
					// than anything else in the scene
					if (board == null || counters.get(board) < counters.get(parent))
						board = parent;

					cornerList.add(pt);
					parentMapping.put(pt, c.parent);
				}
			}
		}

		final List<Quad> quads = new ArrayList<Quad>();
		for (int i = 0; i < cornerList.size(); i++) {
			final Corner[] pts = cornerList.get(i);

			// reject regions that don't belong to the predicted board
			if (filterQuads && parentMapping.get(pts) != board)
				continue;

			final Quad q = new Quad();
			q.corners = pts;

			q.minEdgeLength = Float.MAX_VALUE;
			for (int j = 0; j < 4; j++) {
				final float dx = pts[j].x - pts[(j + 1) & 3].x;
				final float dy = pts[j].y - pts[(j + 1) & 3].y;
				final float d = dx * dx + dy * dy;
				if (q.minEdgeLength > d)
					q.minEdgeLength = d;
			}

			quads.add(q);
		}
		return quads;
	}

	/**
	 * Find the neighbouring quads for each quad
	 * 
	 * @param quads
	 *            the quads
	 */
	private void findQuadNeighbours(List<Quad> quads)
	{
		final int quadCount = quads.size();
		final float threshScale = 1.f;

		// find quad neighbours
		for (int idx = 0; idx < quads.size(); idx++)
		{
			final Quad cur_quad = quads.get(idx);

			// choose the points of the current quadrangle that are close to
			// some points of the other quadrangles
			// (it can happen for split corners (due to dilation) of the
			// checker board). Search only in other quadrangles!

			// for each corner of this quadrangle
			for (int i = 0; i < 4; i++)
			{
				float min_dist = Float.MAX_VALUE;
				int closest_corner_idx = -1;
				Quad closest_quad = null;
				Corner closest_corner = null;

				if (cur_quad.neighbours[i] != null)
					continue;

				final Corner pt = cur_quad.corners[i];

				float dx;
				float dy;
				float dist;
				// find the closest corner in all other quadrangles
				for (int k = 0; k < quadCount; k++)
				{
					if (k == idx)
						continue;

					for (int j = 0; j < 4; j++)
					{
						if (quads.get(k).neighbours[j] != null)
							continue;

						dx = pt.x - quads.get(k).corners[j].x;
						dy = pt.y - quads.get(k).corners[j].y;
						dist = dx * dx + dy * dy;

						if (dist < min_dist &&
								dist <= cur_quad.minEdgeLength * threshScale &&
								dist <= quads.get(k).minEdgeLength * threshScale)
						{
							// check edge lengths, make sure they're compatible
							// edges that are different by more than 1:4 are
							// rejected
							final float ediff = cur_quad.minEdgeLength - quads.get(k).minEdgeLength;
							if (ediff > 32 * cur_quad.minEdgeLength ||
									ediff > 32 * quads.get(k).minEdgeLength)
							{
								logger.trace("Incompatible edge lengths");
								continue;
							}
							closest_corner_idx = j;
							closest_quad = quads.get(k);
							min_dist = dist;
						}
					}
				}

				// we found a matching corner point?
				if (closest_corner_idx >= 0 && min_dist < Float.MAX_VALUE)
				{
					// If another point from our current quad is closer to the
					// found corner
					// than the current one, then we don't count this one after
					// all.
					// This is necessary to support small squares where
					// otherwise the wrong
					// corner will get matched to closest_quad;
					closest_corner = closest_quad.corners[closest_corner_idx];

					int j;
					for (j = 0; j < 4; j++)
					{
						if (cur_quad.neighbours[j] == closest_quad)
							break;

						dx = closest_corner.x - cur_quad.corners[j].x;
						dy = closest_corner.y - cur_quad.corners[j].y;

						if (dx * dx + dy * dy < min_dist)
							break;
					}

					if (j < 4 || cur_quad.count >= 4 || closest_quad.count >= 4)
						continue;

					// Check that each corner is a neighbor of different quads
					for (j = 0; j < closest_quad.count; j++)
					{
						if (closest_quad.neighbours[j] == cur_quad)
							break;
					}
					if (j < closest_quad.count)
						continue;

					// check whether the closest corner to closest_corner
					// is different from cur_quad.corners[i].pt
					int k;
					for (k = 0; k < quadCount; k++)
					{
						final Quad q = quads.get(k);
						if (k == idx || q == closest_quad)
							continue;

						for (j = 0; j < 4; j++)
							if (q.neighbours[j] == null)
							{
								dx = closest_corner.x - q.corners[j].x;
								dy = closest_corner.y - q.corners[j].y;
								dist = dx * dx + dy * dy;
								if (dist < min_dist)
									break;
							}
						if (j < 4)
							break;
					}

					if (k < quadCount)
						continue;

					closest_corner.x = (pt.x + closest_corner.x) * 0.5f;
					closest_corner.y = (pt.y + closest_corner.y) * 0.5f;

					// We've found one more corner - remember it
					cur_quad.count++;
					cur_quad.neighbours[i] = closest_quad;
					cur_quad.corners[i] = closest_corner;

					closest_quad.count++;
					closest_quad.neighbours[closest_corner_idx] = cur_quad;
				}
			}
		}
	}

	/**
	 * Find groups of connected quads. This searches for the first un-labelled
	 * quad and then finds the connected ones.
	 * 
	 * @param quads
	 *            the quads
	 * @param group_idx
	 *            the group index
	 * @return the quads belonging to the group
	 */
	private List<Quad> findConnectedQuads(List<Quad> quads, int group_idx)
	{
		final Deque<Quad> stack = new ArrayDeque<Quad>();
		int i;
		final int quad_count = quads.size();
		final List<Quad> out_group = new ArrayList<Quad>();

		// Scan the array for a first unlabeled quad
		for (i = 0; i < quad_count; i++)
		{
			if (quads.get(i).count > 0 && quads.get(i).group_idx < 0)
				break;
		}

		// Recursively find a group of connected quads starting from the seed
		// quad[i]
		if (i < quad_count)
		{
			Quad q = quads.get(i);
			stack.push(q);
			out_group.add(q);
			q.group_idx = group_idx;
			q.ordered = false;

			while (stack.size() > 0)
			{
				q = stack.pop();
				for (i = 0; i < 4; i++)
				{
					final Quad neighbour = q.neighbours[i];
					if (neighbour != null && neighbour.count > 0 && neighbour.group_idx < 0)
					{
						stack.push(neighbour);
						out_group.add(neighbour);
						neighbour.group_idx = group_idx;
						neighbour.ordered = false;
					}
				}
			}
		}

		return out_group;
	}

	/**
	 * order a group of connected quads order of corners: 0 is top left
	 * clockwise from there note: "top left" is nominal, depends on initial
	 * ordering of starting quad but all other quads are ordered consistently
	 * 
	 * can change the number of quads in the group can add quads, so we need to
	 * have quad/corner arrays passed in
	 */
	private int orderFoundConnectedQuads(List<Quad> quads, List<Quad> all_quads)
	{
		final Deque<Quad> stack = new ArrayDeque<Quad>();

		int quad_count = quads.size();

		// first find an interior quad
		Quad start = null;
		for (int i = 0; i < quad_count; i++)
		{
			if (quads.get(i).count == 4)
			{
				start = quads.get(i);
				break;
			}
		}

		if (start == null)
			return 0; // no 4-connected quad

		// start with first one, assign rows/cols
		int row_min = 0, col_min = 0, row_max = 0, col_max = 0;

		final TIntIntHashMap col_hist = new TIntIntHashMap();
		final TIntIntHashMap row_hist = new TIntIntHashMap();

		stack.push(start);
		start.row = 0;
		start.col = 0;
		start.ordered = true;

		// Recursively order the quads so that all position numbers (e.g.,
		// 0,1,2,3) are in the at the same relative corner (e.g., lower right).

		while (stack.size() > 0)
		{
			final Quad q = stack.pop();
			int col = q.col;
			int row = q.row;
			col_hist.adjustOrPutValue(col, 1, 1);
			row_hist.adjustOrPutValue(row, 1, 1);

			// check min/max
			if (row > row_max)
				row_max = row;
			if (row < row_min)
				row_min = row;
			if (col > col_max)
				col_max = col;
			if (col < col_min)
				col_min = col;

			for (int i = 0; i < 4; i++)
			{
				final Quad neighbour = q.neighbours[i];
				switch (i) // adjust col, row for this quad
				{ // start at top left, go clockwise
				case 0:
					row--;
					col--;
					break;
				case 1:
					col += 2;
					break;
				case 2:
					row += 2;
					break;
				case 3:
					col -= 2;
					break;
				}

				// just do inside quads
				if (neighbour != null && neighbour.ordered == false && neighbour.count == 4)
				{
					orderQuad(neighbour, q.corners[i], (i + 2) % 4); // set in
																		// order
					neighbour.ordered = true;
					neighbour.row = row;
					neighbour.col = col;
					stack.push(neighbour);
				}
			}
		}

		// analyze inner quad structure
		int w = patternWidth - 1;
		int h = patternHeight - 1;
		final int drow = row_max - row_min + 1;
		final int dcol = col_max - col_min + 1;

		// normalize pattern and found quad indices
		if ((w > h && dcol < drow) ||
				(w < h && drow < dcol))
		{
			h = patternWidth - 1;
			w = patternHeight - 1;
		}

		logger.trace(String.format("Size: %dx%d  Pattern: %dx%d", dcol, drow, w, h));

		// check if there are enough inner quads
		if (dcol < w || drow < h) // found enough inner quads?
		{
			logger.trace("Too few inner quad rows/cols");
			return 0; // no, return
		}

		// check edges of inner quads
		// if there is an outer quad missing, fill it in
		// first order all inner quads
		int found = 0;
		for (int i = 0; i < quad_count; i++)
		{
			if (quads.get(i).count == 4)
			{ // ok, look at neighbours
				int col = quads.get(i).col;
				int row = quads.get(i).row;
				for (int j = 0; j < 4; j++)
				{
					switch (j) // adjust col, row for this quad
					{ // start at top left, go clockwise
					case 0:
						row--;
						col--;
						break;
					case 1:
						col += 2;
						break;
					case 2:
						row += 2;
						break;
					case 3:
						col -= 2;
						break;
					}
					final Quad neighbour = quads.get(i).neighbours[j];
					if (neighbour != null && !neighbour.ordered && // is it an
																	// inner
																	// quad?
							col <= col_max && col >= col_min &&
							row <= row_max && row >= row_min)
					{
						// if so, set in order
						logger.trace(String.format("Adding inner: col: %d  row: %d", col, row));
						found++;
						orderQuad(neighbour, quads.get(i).corners[j], (j + 2) % 4);
						neighbour.ordered = true;
						neighbour.row = row;
						neighbour.col = col;
					}
				}
			}
		}

		// if we have found inner quads, add corresponding outer quads,
		// which are missing
		if (found > 0)
		{
			logger.trace(String.format("Found %d inner quads not connected to outer quads, repairing", found));
			for (int i = 0; i < quad_count; i++)
			{
				if (quads.get(i).count < 4 && quads.get(i).ordered)
				{
					final int added = addOuterQuad(quads.get(i), quads, all_quads);
					quad_count += added;
				}
			}
		}

		// final trimming of outer quads
		if (dcol == w && drow == h) // found correct inner quads
		{
			logger.trace("Inner bounds ok, check outer quads");
			int rcount = quad_count;
			for (int i = quad_count - 1; i >= 0; i--) // eliminate any quad not
														// connected to
			// an ordered quad
			{
				if (quads.get(i).ordered == false)
				{
					boolean outer = false;
					for (int j = 0; j < 4; j++) // any neighbours that are
												// ordered?
					{
						if (quads.get(i).neighbours[j] != null && quads.get(i).neighbours[j].ordered)
							outer = true;
					}
					if (!outer) // not an outer quad, eliminate
					{
						logger.trace(String.format("Removing quad %d", i));
						removeQuadFromGroup(quads, quads.get(i));
						rcount--;
					}
				}

			}
			return rcount;
		}

		return 0;
	}

	/**
	 * put quad into correct order, where <code>corner</code> has value
	 * <code<common</code>
	 * 
	 * @param quad
	 *            the quad to sort
	 * @param corner
	 *            the corner
	 * @param common
	 *            the common vertex
	 */
	private void orderQuad(Quad quad, Corner corner, int common)
	{
		// find the corner
		int tc;
		for (tc = 0; tc < 4; tc++)
			if (quad.corners[tc].x == corner.x &&
					quad.corners[tc].y == corner.y)
				break;

		// set corner order
		// shift
		while (tc != common)
		{
			// shift by one
			final Corner tempc = quad.corners[3];
			final Quad tempq = quad.neighbours[3];
			for (int i = 3; i > 0; i--)
			{
				quad.corners[i] = quad.corners[i - 1];
				quad.neighbours[i] = quad.neighbours[i - 1];
			}
			quad.corners[0] = tempc;
			quad.neighbours[0] = tempq;
			tc++;
			tc = tc % 4;
		}
	}

	/*
	 * add an outer quad
	 * 
	 * looks for the neighbor of <code>quad</code> that isn't present, tries to
	 * add it in. <code>quad</code> is ordered
	 * 
	 * @param quad the quad
	 * 
	 * @param quads all quad group
	 * 
	 * @param all_quads all the quads
	 * 
	 * @return
	 */
	private int addOuterQuad(final Quad quad, List<Quad> quads, List<Quad> all_quads)
	{
		int added = 0;
		for (int i = 0; i < 4; i++) // find no-neighbor corners
		{
			if (quad.neighbours[i] == null) // ok, create and add neighbor
			{
				final int j = (i + 2) % 4;
				logger.trace("Adding quad as neighbor 2");
				final Quad q = new Quad();
				all_quads.add(q);
				added++;
				quads.add(q);

				// set neighbor and group id
				quad.neighbours[i] = q;
				quad.count += 1;
				q.neighbours[j] = quad;
				q.group_idx = quad.group_idx;
				q.count = 1; // number of neighbours
				q.ordered = false;
				q.minEdgeLength = quad.minEdgeLength;

				// make corners of new quad
				// same as neighbor quad, but offset
				Corner pt = quad.corners[i];
				final float dx = pt.x - quad.corners[j].x;
				final float dy = pt.y - quad.corners[j].y;
				for (int k = 0; k < 4; k++)
				{
					final Corner corner = new Corner(pt);
					pt = quad.corners[k];
					q.corners[k] = corner;
					corner.x += dx;
					corner.y += dy;
				}
				// have to set exact corner
				q.corners[j] = quad.corners[i];

				// now find other neighbor and add it, if possible
				if (quad.neighbours[(i + 3) % 4] != null &&
						quad.neighbours[(i + 3) % 4].ordered &&
						quad.neighbours[(i + 3) % 4].neighbours[i] != null &&
						quad.neighbours[(i + 3) % 4].neighbours[i].ordered)
				{
					final Quad qn = quad.neighbours[(i + 3) % 4].neighbours[i];
					q.count = 2;
					q.neighbours[(j + 1) % 4] = qn;
					qn.neighbours[(i + 1) % 4] = q;
					qn.count += 1;
					// have to set exact corner
					q.corners[(j + 1) % 4] = qn.corners[(i + 1) % 4];
				}
			}
		}
		return added;
	}

	/**
	 * remove quad from quad group
	 */
	private void removeQuadFromGroup(final List<Quad> quads, Quad q0)
	{
		final int count = quads.size();
		// remove any references to this quad as a neighbor
		for (int i = 0; i < count; i++)
		{
			final Quad q = quads.get(i);
			for (int j = 0; j < 4; j++)
			{
				if (q.neighbours[j] == q0)
				{
					q.neighbours[j] = null;
					q.count--;
					for (int k = 0; k < 4; k++)
						if (q0.neighbours[k] == q)
						{
							q0.neighbours[k] = null;
							q0.count--;
							break;
						}
					break;
				}
			}
		}

		quads.remove(q0);
	}

	/**
	 * if we found too many connect quads, remove those which probably do not
	 * belong.
	 * 
	 * @param quad_group
	 *            the group of quads
	 * @return the new group size
	 */
	private int cleanFoundConnectedQuads(List<Quad> quad_group)
	{
		int quad_count = quad_group.size();
		final Point2dImpl center = new Point2dImpl();

		// number of quads this pattern should contain
		final int count = ((patternWidth + 1) * (patternHeight + 1) + 1) / 2;

		// if we have more quadrangles than we should,
		// try to eliminate duplicates or ones which don't belong to the pattern
		// rectangle...
		if (quad_count <= count)
			return quad_count;

		// create an array of quadrangle centers
		final List<Point2dImpl> centers = new ArrayList<Point2dImpl>(quad_count);

		for (int i = 0; i < quad_count; i++)
		{
			final Point2dImpl ci = new Point2dImpl();
			final Quad q = quad_group.get(i);

			for (int j = 0; j < 4; j++)
			{
				final Point2dImpl pt = q.corners[j];
				ci.x += pt.x;
				ci.y += pt.y;
			}

			ci.x *= 0.25f;
			ci.y *= 0.25f;

			centers.add(ci);
			center.x += ci.x;
			center.y += ci.y;
		}
		center.x /= quad_count;
		center.y /= quad_count;

		// If we still have more quadrangles than we should,
		// we try to eliminate bad ones based on minimizing the bounding box.
		// We iteratively remove the point which reduces the size of
		// the bounding box of the blobs the most
		// (since we want the rectangle to be as small as possible)
		// remove the quadrange that causes the biggest reduction
		// in pattern size until we have the correct number
		for (; quad_count > count; quad_count--)
		{
			double min_box_area = Double.MAX_VALUE;
			int min_box_area_index = -1;
			Quad q0, q;

			// For each point, calculate box area without that point
			for (int skip = 0; skip < quad_count; skip++)
			{
				// get bounding rectangle
				final Point2dImpl temp = centers.get(skip); // temporarily make
															// index 'skip' the
															// same as
				centers.set(skip, center); // pattern center (so it is not
											// counted for convex hull)
				final PointList pl = new PointList(centers);
				final Polygon hull = pl.calculateConvexHull();
				centers.set(skip, temp);
				final double hull_area = hull.calculateArea();

				// remember smallest box area
				if (hull_area < min_box_area)
				{
					min_box_area = hull_area;
					min_box_area_index = skip;
				}
			}

			q0 = quad_group.get(min_box_area_index);

			// remove any references to this quad as a neighbor
			for (int i = 0; i < quad_count; i++)
			{
				q = quad_group.get(i);
				for (int j = 0; j < 4; j++)
				{
					if (q.neighbours[j] == q0)
					{
						q.neighbours[j] = null;
						q.count--;
						for (int k = 0; k < 4; k++)
							if (q0.neighbours[k] == q)
							{
								q0.neighbours[k] = null;
								q0.count--;
								break;
							}
						break;
					}
				}
			}

			// remove the quad
			quad_count--;
			quad_group.remove(min_box_area_index);
			centers.remove(min_box_area_index);
		}

		return quad_count;
	}

	/**
	 * 
	 */
	private int checkQuadGroup(List<Quad> quad_group, List<Corner> out_corners)
	{
		final int ROW1 = 1000000;
		final int ROW2 = 2000000;
		final int ROW_ = 3000000;
		int result = 0;
		final int quad_count = quad_group.size();
		int corner_count = 0;
		final Corner[] corners = new Corner[quad_count * 4];

		int width = 0, height = 0;
		final int[] hist = { 0, 0, 0, 0, 0 };
		Corner first = null, first2 = null, right, cur, below, c;

		try {
			// build dual graph, which vertices are internal quad corners
			// and two vertices are connected iff they lie on the same quad edge
			for (int i = 0; i < quad_count; i++)
			{
				final Quad q = quad_group.get(i);

				for (int j = 0; j < 4; j++)
				{
					if (q.neighbours[j] != null)
					{
						final Corner a = q.corners[j], b = q.corners[(j + 1) & 3];
						// mark internal corners that belong to:
						// - a quad with a single neighbor - with ROW1,
						// - a quad with two neighbours - with ROW2
						// make the rest of internal corners with ROW_
						final int row_flag = q.count == 1 ? ROW1 : q.count == 2 ? ROW2 : ROW_;

						if (a.row == 0)
						{
							corners[corner_count++] = a;
							a.row = row_flag;
						}
						else if (a.row > row_flag)
							a.row = row_flag;

						if (q.neighbours[(j + 1) & 3] != null)
						{
							if (a.count >= 4 || b.count >= 4)
								throw new Exception();
							for (int k = 0; k < 4; k++)
							{
								if (a.neighbours[k] == b)
									throw new Exception();
								if (b.neighbours[k] == a)
									throw new Exception();
							}
							a.neighbours[a.count++] = b;
							b.neighbours[b.count++] = a;
						}
					}
				}
			}

			if (corner_count != patternWidth * patternHeight)
				throw new Exception();

			for (int i = 0; i < corner_count; i++)
			{
				final int n = corners[i].count;
				assert (0 <= n && n <= 4);
				hist[n]++;
				if (first == null && n == 2)
				{
					if (corners[i].row == ROW1)
						first = corners[i];
					else if (first2 == null && corners[i].row == ROW2)
						first2 = corners[i];
				}
			}

			// start with a corner that belongs to a quad with a signle
			// neighbor.
			// if we do not have such, start with a corner of a quad with two
			// neighbours.
			if (first == null)
				first = first2;

			if (first == null || hist[0] != 0 || hist[1] != 0 || hist[2] != 4 ||
					hist[3] != (patternWidth + patternHeight) * 2 - 8)
				throw new Exception();

			cur = first;
			right = below = null;
			out_corners.add(cur);

			for (int k = 0; k < 4; k++)
			{
				c = cur.neighbours[k];
				if (c != null)
				{
					if (right == null)
						right = c;
					else if (below == null)
						below = c;
				}
			}

			if (right == null || (right.count != 2 && right.count != 3) ||
					below == null || (below.count != 2 && below.count != 3))
				throw new Exception();

			cur.row = 0;
			first = below; // remember the first corner in the next row
			// find and store the first row (or column)
			while (true)
			{
				right.row = 0;
				out_corners.add(right);

				if (right.count == 2)
					break;
				if (right.count != 3 || out_corners.size() >= Math.max(patternWidth, patternHeight))
					throw new Exception();
				cur = right;

				for (int k = 0; k < 4; k++)
				{
					c = cur.neighbours[k];
					if (c != null && c.row > 0)
					{
						int kk;
						for (kk = 0; kk < 4; kk++)
						{
							if (c.neighbours[kk] == below)
								break;
						}
						if (kk < 4)
							below = c;
						else
							right = c;
					}
				}
			}

			width = out_corners.size();
			if (width == patternWidth)
				height = patternHeight;
			else if (width == patternHeight)
				height = patternWidth;
			else
				throw new Exception();

			// find and store all the other rows
			for (int i = 1;; i++)
			{
				if (first == null)
					break;
				cur = first;
				first = null;
				int j;
				for (j = 0;; j++)
				{
					cur.row = i;
					out_corners.add(cur);
					if (cur.count == 2 + (i < height - 1 ? 1 : 0) && j > 0)
						break;

					right = null;

					// find a neighbor that has not been processed yet
					// and that has a neighbor from the previous row
					for (int k = 0; k < 4; k++)
					{
						c = cur.neighbours[k];
						if (c != null && c.row > i)
						{
							int kk;
							for (kk = 0; kk < 4; kk++)
							{
								if (c.neighbours[kk] != null && c.neighbours[kk].row == i - 1)
									break;
							}
							if (kk < 4)
							{
								right = c;
								if (j > 0)
									break;
							}
							else if (j == 0)
								first = c;
						}
					}
					if (right == null)
						throw new Exception();
					cur = right;
				}

				if (j != width - 1)
					throw new Exception();
			}

			if (out_corners.size() != corner_count)
				throw new Exception();

			// check if we need to transpose the board
			if (width != patternWidth)
			{
				final int t = width;
				width = height;
				height = t;

				// memcpy( &corners[0], out_corners,
				// corner_count*sizeof(corners[0])
				// );
				for (int i = 0; i < corner_count; i++)
					corners[i] = out_corners.get(i);

				for (int i = 0; i < height; i++)
					for (int j = 0; j < width; j++)
						out_corners.set(i * width + j, corners[j * height + i]);
			}

			// check if we need to revert the order in each row
			{
				final Point2dImpl p0 = out_corners.get(0), p1 = out_corners.get(patternWidth - 1), p2 = out_corners
						.get(patternWidth);
				if ((p1.x - p0.x) * (p2.y - p1.y) - (p1.y - p0.y) * (p2.x - p1.x) < 0)
				{
					if (width % 2 == 0)
					{
						for (int i = 0; i < height; i++)
							for (int j = 0; j < width / 2; j++)
								Collections.swap(out_corners, i * width + j, i * width + width - j - 1);
					}
					else
					{
						for (int j = 0; j < width; j++)
							for (int i = 0; i < height / 2; i++)
								Collections.swap(out_corners, i * width + j, (height - i - 1) * width + j);
					}
				}
			}

			result = corner_count;

		} catch (final Exception ex) {
			// ignore
		}

		if (result <= 0)
		{
			corner_count = Math.min(corner_count, patternWidth * patternHeight);
			out_corners.clear();
			for (int i = 0; i < corner_count; i++)
				out_corners.add(corners[i]);
			result = -corner_count;

			if (result == -patternWidth * patternHeight)
				result = -result;
		}

		return result;
	}

	/**
	 * Checks that each board row and column is pretty much monotonous curve:
	 * 
	 * It analyzes each row and each column of the chessboard as following:
	 * 
	 * for each corner c lying between end points in the same row/column it
	 * checks that the point projection to the line segment (a,b) is lying
	 * between projections of the neighbor corners in the same row/column.
	 * 
	 * This function has been created as temporary workaround for the bug in
	 * current implementation of cvFindChessboardCornes that produces absolutely
	 * unordered sets of corners.
	 * 
	 * @return true if the board is good; false otherwise.
	 */
	private boolean checkBoardMonotony()
	{
		int i, j, k;

		for (k = 0; k < 2; k++)
		{
			for (i = 0; i < (k == 0 ? patternHeight : patternWidth); i++)
			{
				final Point2dImpl a = k == 0 ? out_corners.get(i * patternWidth) : out_corners.get(i);
				final Point2dImpl b = k == 0 ? out_corners.get((i + 1) * patternWidth - 1) :
						out_corners.get((patternHeight - 1) * patternWidth + i);
				float prevt = 0;
				final float dx0 = b.x - a.x, dy0 = b.y - a.y;
				if (Math.abs(dx0) + Math.abs(dy0) < Float.MIN_VALUE)
					return false;
				for (j = 1; j < (k == 0 ? patternWidth : patternHeight) - 1; j++)
				{
					final Point2dImpl c = k == 0 ? out_corners.get(i * patternWidth + j) :
							out_corners.get(j * patternWidth + i);
					final float t = ((c.x - a.x) * dx0 + (c.y - a.y) * dy0) / (dx0 * dx0 + dy0 * dy0);
					if (t < prevt || t > 1)
						return false;
					prevt = t;
				}
			}
		}

		return true;
	}

	public void drawChessboardCorners(MBFImage image) {
		cvDrawChessboardCorners(image, patternWidth, patternHeight, out_corners, found);
	}

	public static void cvDrawChessboardCorners(MBFImage image, int patternWidth, int patternHeight,
			List<? extends Point2d> corners, boolean found)
	{
		final int radius = 4;

		if (!found) {
			final Float[] color = RGBColour.RGB(0, 0, 255);

			for (int i = 0; i < corners.size(); i++)
			{
				final Point2d pt = corners.get(i);
				image.drawLine(new Point2dImpl(pt.getX() - radius, pt.getY() - radius),
						new Point2dImpl(pt.getX() + radius, pt.getY() + radius), 1, color);
				image.drawLine(new Point2dImpl(pt.getX() - radius, pt.getY() + radius),
						new Point2dImpl(pt.getX() + radius, pt.getY() - radius), 1, color);
				image.drawShape(new Circle(pt, radius), 1, color);
			}
		} else {
			Point2d prev_pt = new Point2dImpl();
			final Float[][] line_colors =
			{
					RGBColour.RGB(0, 0, 255),
					RGBColour.RGB(0, 128, 255),
					RGBColour.RGB(0, 200, 200),
					RGBColour.RGB(0, 255, 0),
					RGBColour.RGB(200, 200, 0),
					RGBColour.RGB(255, 0, 0),
					RGBColour.RGB(255, 0, 255)
			};

			for (int y = 0, i = 0; y < patternHeight; y++) {
				final Float[] color = line_colors[y % line_colors.length];

				for (int x = 0; x < patternWidth; x++, i++) {
					final Point2d pt = corners.get(i);

					if (i != 0) {
						image.drawLine(prev_pt, pt, 1, color);
					}

					image.drawLine(new Point2dImpl(pt.getX() - radius, pt.getY() - radius),
							new Point2dImpl(pt.getX() + radius, pt.getY() + radius), 1, color);
					image.drawLine(new Point2dImpl(pt.getX() - radius, pt.getY() + radius),
							new Point2dImpl(pt.getX() + radius, pt.getY() - radius), 1, color);
					image.drawShape(new Circle(pt, radius), 1, color);

					prev_pt = pt;
				}
			}
		}
	}

	public static void main(String[] args) throws MalformedURLException, IOException {
		// final FImage chessboard = ImageUtilities.readF(new
		// URL("http://www.ugcs.caltech.edu/~rajan/REPORT/camera.jpg"));
		// final FImage chessboard =
		// ResizeProcessor.halfSize(ImageUtilities.readF(new File(
		// "/Users/jon/Work/opencv/opencv/doc/pattern.png")));
		// final FImage chessboard = ImageUtilities.readF(new
		// URL("http://docs.opencv.org/_images/fileListImageUnDist.jpg"));

		final ChessboardCornerFinder fcc = new ChessboardCornerFinder(9, 6, Options.FILTER_QUADS, Options.FAST_CHECK);
		// chessboard.analyseWith(fcc);
		// final MBFImage cimg = chessboard.toRGB();
		// fcc.drawChessboardCorners(cimg);
		// DisplayUtilities.display(cimg);

		VideoDisplay.createVideoDisplay(new VideoCapture(640, 480)).addVideoListener(new VideoDisplayAdapter<MBFImage>()
		{
			@Override
			public void beforeUpdate(MBFImage frame) {
				fcc.analyseImage(frame.flatten());
				fcc.drawChessboardCorners(frame);
			}
		});

	}
}
