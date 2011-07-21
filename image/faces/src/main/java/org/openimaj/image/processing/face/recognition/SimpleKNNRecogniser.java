/**
 * Copyright (c) 2011, The University of Southampton and the individual contributors.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *   * 	Redistributions of source code must retain the above copyright notice,
 * 	this list of conditions and the following disclaimer.
 *
 *   *	Redistributions in binary form must reproduce the above copyright notice,
 * 	this list of conditions and the following disclaimer in the documentation
 * 	and/or other materials provided with the distribution.
 *
 *   *	Neither the name of the University of Southampton nor the names of its
 * 	contributors may be used to endorse or promote products derived from this
 * 	software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.openimaj.image.processing.face.recognition;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
	
	protected SimpleKNNRecogniser() {}
	
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
		List<FaceMatchResult> results = new ArrayList<FaceMatchResult>();
		results.add(queryBestMatch(face));
		return results;
	}
	
	@Override
	public List<FaceMatchResult> query(Q face, Collection<String> restrict) {
		List<FaceMatchResult> results = new ArrayList<FaceMatchResult>();
		results.add(queryBestMatch(face,restrict));
		return results;
	}

	@Override
	public FaceMatchResult queryBestMatch(Q face) {
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
		Collections.reverse(results);
		
		return results.get(0);
	}
	
	@Override
	public FaceMatchResult queryBestMatch(Q face, Collection<String> restrict) {
		List<FaceDistance> dists = calculateDistances(face, restrict);
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
	
	protected List<FaceDistance> calculateDistances(Q face, Collection<String> restrict) {
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
						T feature = IOUtils.<T>newInstance(featureClass);
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

	@Override
	public List<String> listPeople() {
		return new ArrayList<String>(this.database.keySet());
	}
	
	@Override
	public String toString() {
		return String.format("SimpleKNNRecogniser[count=%d,featurefactory=%s,comparator=%s,k=%d]", database.size(), factory, comparator, K);
	}
}
