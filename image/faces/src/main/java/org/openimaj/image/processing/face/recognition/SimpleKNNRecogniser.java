package org.openimaj.image.processing.face.recognition;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.openimaj.image.processing.face.detection.DetectedFace;
import org.openimaj.image.processing.face.feature.FacialFeature;
import org.openimaj.image.processing.face.feature.FacialFeatureFactory;
import org.openimaj.image.processing.face.feature.comparison.FacialFeatureComparator;
import org.openimaj.io.IOUtils;
import org.openimaj.io.wrappers.ReadWriteableString;
import org.openimaj.io.wrappers.ReadableListBinary;
import org.openimaj.io.wrappers.ReadableMapBinary;
import org.openimaj.io.wrappers.WriteableListBinary;
import org.openimaj.io.wrappers.WriteableMapBinary;

public class SimpleKNNRecogniser<T extends FacialFeature, Q extends DetectedFace> implements FaceRecogniser<Q> {
	protected Map<String, List<T>> database = new HashMap<String, List<T>>();
	protected FacialFeatureFactory<T, Q> factory;
	protected FacialFeatureComparator<T> comparator;
	protected int K;
	
	public SimpleKNNRecogniser(FacialFeatureFactory<T, Q> factory, FacialFeatureComparator<T> comparator, int K) {
		this.factory = factory;
		this.comparator = comparator;
		this.K = K;
	}
	
	@Override
	public void addInstance(String identifier, Q face) {
		List<T> instances = database.get(identifier);
		
		if (instances == null) { 
			database.put(identifier, instances = new ArrayList<T>());
		}
		
		instances.add(factory.createFeature(face, false));
	}

	@Override
	public void train() {

	}

	@Override
	public List<FaceMatchResult> query(Q face) {
		return null;
	}

	@Override
	public FaceMatchResult queryBestMatch(Q face) {
		List<FaceDistance> dists = calculateDistances(face);
		List<FaceMatchResult> results = new ArrayList<FaceMatchResult>();
		
		System.out.println(dists.subList(0, 10));
		
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
		Collections.reverse(results);
		
		return results.get(0);
	}
	
	public FaceMatchResult queryBestMatch(Q face, Set<String> restrict) {
		List<FaceDistance> dists = calculateDistances(face, restrict);
		List<FaceMatchResult> results = new ArrayList<FaceMatchResult>();
		
		System.out.println(dists.subList(0, 10));
		
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
		Collections.reverse(results);
		
		return results.get(0);
	}
	
	class FaceDistance extends FaceMatchResult {
		int instance;
	}
	
	protected List<FaceDistance> calculateDistances(Q face) {
		List<FaceDistance> dists = new ArrayList<FaceDistance>();
		
		T queryfeature = this.factory.createFeature(face, true);
		
		for (Entry<String, List<T>> entry : database.entrySet()) {
			int i=0;
						
			for (T instance : entry.getValue()) {
				FaceDistance d = new FaceDistance();
				d.identifier = entry.getKey();
				d.instance = i++;
				d.score = comparator.compare(queryfeature, instance);
				dists.add(d);
			}
		}
		
		Collections.sort(dists);
		
		return dists;
	}
	
	protected List<FaceDistance> calculateDistances(Q face, Set<String> restrict) {
		List<FaceDistance> dists = new ArrayList<FaceDistance>();
		
		T queryfeature = this.factory.createFeature(face, true);
		
		for (Entry<String, List<T>> entry : database.entrySet()) {
			if (!restrict.contains(entry.getKey()))
				continue;
				
			int i=0;
			
			for (T instance : entry.getValue()) {
				FaceDistance d = new FaceDistance();
				d.identifier = entry.getKey();
				d.instance = i++;
				d.score = comparator.compare(queryfeature, instance);
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

	@Override
	public void readBinary(DataInput in) throws IOException {
		final String featureClass = in.readUTF();
		
		new ReadableMapBinary<String, List<T>>(this.database) {
			@Override
			protected String readKey(DataInput in) throws IOException {
				ReadWriteableString rws = new ReadWriteableString();
				rws.readBinary(in);
				return rws.value;
			}
			
			@Override
			protected List<T> readValue(DataInput in) throws IOException {
				ArrayList<T> list = new ArrayList<T>();
				
				new ReadableListBinary<T>(list) {
					@Override
					protected T readValue(DataInput in) throws IOException {
						T feature = IOUtils.newInstance(featureClass);
						feature.readBinary(in);
						return feature;
					}
				}.readBinary(in);
				
				return list;
			}
		}.readBinary(in);
		
		String factoryClass = in.readUTF();
		factory = IOUtils.newInstance(factoryClass);
		factory.readBinary(in);
		
		String comparatorClass = in.readUTF();
		comparator = IOUtils.newInstance(comparatorClass);
		comparator.readBinary(in);
		
		K = in.readInt();
	}

	@Override
	public byte[] binaryHeader() {
		return "SKNNFR".getBytes();
	}

	@Override
	public void writeBinary(DataOutput out) throws IOException {
		out.writeUTF(factory.getFeatureClass().getName());
		new WriteableMapBinary<String, List<T>>(this.database) {
			@Override
			protected void writeKey(String key, DataOutput out) throws IOException {
				new ReadWriteableString(key).writeBinary(out);
			}
			
			@Override
			protected void writeValue(List<T> value, DataOutput out) throws IOException {
				new WriteableListBinary<T>(value) {
					@Override
					protected void writeValue(T v, DataOutput out) throws IOException {
						v.writeBinary(out);
					}
				}.writeBinary(out);
			}
		}.writeBinary(out);
		
		out.writeUTF(factory.getClass().getName());
		factory.writeBinary(out);
		
		out.writeUTF(comparator.getClass().getName());
		comparator.writeBinary(out);
		
		out.writeInt(K);
	}
}
