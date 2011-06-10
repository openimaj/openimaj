package org.openimaj.image.processing.face.recognition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.openimaj.image.processing.face.features.FacialFeature;
import org.openimaj.image.processing.face.features.FacialFeatureFactory;
import org.openimaj.image.processing.face.parts.DetectedFace;

public class SimpleKNNRecogniser<T extends FacialFeature<T>> implements FaceRecogniser {
	protected Map<String, List<T>> database = new HashMap<String, List<T>>();
	protected FacialFeatureFactory<T> factory;
	protected int K;
	
	public SimpleKNNRecogniser(FacialFeatureFactory<T> factory, int K) {
		this.factory = factory;
		this.K = K;
	}
	
	@Override
	public void addInstance(String identifier, DetectedFace face) {
		List<T> instances = database.get(identifier);
		
		if (instances == null) { 
			database.put(identifier, instances = new ArrayList<T>());
		}
		
		instances.add(factory.createFeature(face, false));
	}

	@Override
	public void train() {
		DescriptiveStatistics intraClass = new DescriptiveStatistics();
		DescriptiveStatistics interClass = new DescriptiveStatistics();
		
		for (Entry<String, List<T>> e1 : database.entrySet()) {			
			for (T i1 : e1.getValue()) {			
				for (Entry<String, List<T>> e2 : database.entrySet()) {
					for (T i2 : e2.getValue()) {
						if (i1 == i2)
							continue;
						
						double distance = i1.compare(i2);
						
						//System.out.println((e1.getKey() == e2.getKey() ? "intra" : "inter") + " " + distance);
						
						if (e1.getKey() == e2.getKey()) {
							intraClass.addValue(distance);
						} else {
							interClass.addValue(distance);
						}
					}
				}
				
			}
		}
		System.out.println();
		System.out.println("Inter-class:");
		System.out.println(interClass);
		System.out.println("Intra-class:");
		System.out.println(intraClass);
	}

	@Override
	public List<FaceMatchResult> query(DetectedFace face) {
		return null;
	}

	@Override
	public FaceMatchResult queryBestMatch(DetectedFace face) {
		List<FaceDistance> dists = calculateDistances(face);
		List<FaceMatchResult> results = new ArrayList<FaceMatchResult>();
		
		for (int i=0; i<K; i++) {
			FaceDistance thisdist = dists.get(i);
			
			FaceMatchResult result = null;
			for (FaceMatchResult r : results) {
				if (r.identifier.equals(thisdist.identifier)) {
					result = r;
					break;
				}
			}
			
			if (result == null) {
				result = new FaceMatchResult();
				result.identifier = thisdist.identifier;
				results.add( result );
			}
			
			result.score += (1.0 / K);
		}
		
		Collections.sort(results);
		
		return results.get(0);
	}
	
	class FaceDistance extends FaceMatchResult {
		int instance;
	}
	
	protected List<FaceDistance> calculateDistances(DetectedFace face) {
		List<FaceDistance> dists = new ArrayList<FaceDistance>();
		
		T feature = this.factory.createFeature(face, true);
		
		for (Entry<String, List<T>> entry : database.entrySet()) {
			int i=0;
			
			for (T instance : entry.getValue()) {
				FaceDistance d = new FaceDistance();
				d.identifier = entry.getKey();
				d.instance = i++;
				d.score = instance.compare(feature);
				dists.add(d);
			}
		}
		
		Collections.sort(dists);
		
		return dists;
	}

	@Override
	public void reset() {
		database.clear();
	}
}
