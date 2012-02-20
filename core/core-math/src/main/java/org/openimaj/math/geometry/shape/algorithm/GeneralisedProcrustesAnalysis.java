package org.openimaj.math.geometry.shape.algorithm;

import java.util.ArrayList;
import java.util.List;

import org.openimaj.math.geometry.shape.PointList;

public class GeneralisedProcrustesAnalysis {
	
	
	public static void alignPoints(List<PointList> inputShapes, float threshold) {
		PointList reference = inputShapes.get(0); 
		
		List<PointList> workingShapes = new ArrayList<PointList>(inputShapes);	
		workingShapes.remove(reference);
		
		PointList mean = alignPointsAndAverage(workingShapes, reference);
		
		while (ProcrustesAnalysis.computeProcrustesDistance(reference, mean) > threshold) {
			reference = mean;
			mean = alignPointsAndAverage(inputShapes, reference);
		}
	}
	
	protected static PointList alignPointsAndAverage(List<PointList> shapes, PointList reference) {
		ProcrustesAnalysis pa = new ProcrustesAnalysis(reference);
		
		for (PointList shape : shapes) {
			pa.align(shape);
		}
		
		return PointList.computeMean(shapes);
	}
	
}
