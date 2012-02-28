package org.openimaj.demos.sandbox.image;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.openimaj.feature.local.list.LocalFeatureList;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.analysis.algorithm.HoughLines;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.feature.local.engine.DoGColourSIFTEngine;
import org.openimaj.image.feature.local.engine.DoGSIFTEngine;
import org.openimaj.image.feature.local.keypoints.Keypoint;
import org.openimaj.image.feature.local.keypoints.KeypointVisualizer;
import org.openimaj.image.processing.edges.CannyEdgeDetector2;
import org.openimaj.math.geometry.line.Line2d;
import org.openimaj.math.geometry.line.Line2d.IntersectionResult;
import org.openimaj.math.geometry.line.Line2d.IntersectionType;
import org.openimaj.math.geometry.point.Point2d;

/**
 * @author ss
 * Find the corners of a chessboard using a hough line detector
 */
public class HoughLineChessboard {
	public static void main(String[] args) throws IOException {
		FImage chessboard = ImageUtilities.readF(new File("/Users/ss/Desktop/chess16-2.gif"));
		HoughLines hlines = new HoughLines(1.f);
		chessboard.process(new CannyEdgeDetector2()).analyseWith(hlines);
		List<Line2d> lines = hlines.getBestLines(14);
		List<Point2d> intersections = new ArrayList<Point2d>();
		for (Line2d inner : lines) {
			for (Line2d outer : lines) {
				if(inner == outer) continue;
				IntersectionResult intersect = inner.getIntersection(outer);
				if(intersect.type == IntersectionType.INTERSECTING){
					intersections.add(intersect.intersectionPoint);
				}
			}
		}
		MBFImage chessboardC = chessboard.toRGB();
		chessboardC.drawLines(lines, 2, RGBColour.RED);
		chessboardC.drawPoints(intersections, RGBColour.BLUE, 3);
		
		DoGSIFTEngine engine = new DoGSIFTEngine();
		LocalFeatureList<Keypoint> kps = engine.findFeatures(chessboard);
		KeypointVisualizer<Float[], MBFImage> vis = new KeypointVisualizer<Float[], MBFImage>(chessboardC,kps);
		vis.drawCenter(RGBColour.GREEN);
		DisplayUtilities.display(vis.drawCenter(RGBColour.GREEN));
	}
}
