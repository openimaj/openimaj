package org.openimaj.image.processing.face.detection;

import java.util.List;

import org.apache.log4j.Logger;
import org.openimaj.experiment.dataset.GroupedDataset;
import org.openimaj.experiment.dataset.ListBackedDataset;
import org.openimaj.experiment.dataset.ListDataset;
import org.openimaj.experiment.dataset.MapBackedDataset;
import org.openimaj.image.Image;

/**
 * Convenience methods for dealing with face detections in datasets of 
 * images.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 */
public class DatasetFaceDetector {
	private static Logger logger = Logger.getLogger(DatasetFaceDetector.class);
	
	private DatasetFaceDetector() {}
	
	public static <KEY, IMAGE extends Image<?, IMAGE>, FACE extends DetectedFace> GroupedDataset<KEY, ListDataset<FACE>, FACE> 
			process(GroupedDataset<KEY, ListDataset<IMAGE>, IMAGE> input, FaceDetector<FACE, IMAGE> detector)
	{
		MapBackedDataset<KEY, ListDataset<FACE>, FACE> output = new MapBackedDataset<KEY, ListDataset<FACE>, FACE>();
		
		for (KEY group : input.getGroups()) {
			ListBackedDataset<FACE> detected = new ListBackedDataset<FACE>();
			ListDataset<IMAGE> instances = input.getInstances(group);
			
			for (int i=0; i<instances.size(); i++) {
				IMAGE img = instances.getInstance(i);
				List<FACE> faces = detector.detectFaces(img);
				
				if (faces == null) {
					logger.warn("There was no face detected in " + group + " instance " + i);
					continue;
				}
				
				if (faces.size() == 1) {
					detected.add(faces.get(0));
					continue;
				}
				
				detected.add(getBiggest(faces));
			}
			
			output.getMap().put(group, detected);
		}
		
		return output;
	}
	
	/**
	 * Get the biggest face (by area) from the list
	 * @param <FACE> Type of {@link DetectedFace}
	 * @param faces the list of faces
	 * @return the biggest face or null if the list is null or empty
	 */
	public static <FACE extends DetectedFace> FACE getBiggest(List<FACE> faces) {
		if (faces == null || faces.size() == 0) return null;
		
		int biggestIndex = 0;
		double biggestSize = faces.get(0).bounds.calculateArea();
		
		for (int i=1; i<faces.size(); i++) {
			double sz = faces.get(i).bounds.calculateArea();
			if (sz > biggestSize) {
				biggestSize = sz;
				biggestIndex = i;
			}
		}
		
		return faces.get(biggestIndex);
	}
}
