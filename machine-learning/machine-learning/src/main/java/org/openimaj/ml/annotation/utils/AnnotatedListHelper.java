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
package org.openimaj.ml.annotation.utils;

import gnu.trove.list.array.TIntArrayList;

import java.util.AbstractList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openimaj.feature.FeatureExtractor;
import org.openimaj.ml.annotation.Annotated;


/**
 * Helper class for dealing with lists of annotated objects,
 * and specifically getting objects by class and determining
 * the set of annotations.
 * <p>
 * Because it might not be practical to hold all the items
 * in the list in memory at once, the implementation only stores
 * the index of each item, and performs an indirect lookup
 * as required. This does mean that once you've passed the list
 * to the constructor, you shouldn't modify it as doing so
 * will invalidate the index. The constructor will make one
 * single pass through all the objects in order to build the 
 * index.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 * @param <OBJECT> The type of object
 * @param <ANNOTATION> The type of annotation
 */
public class AnnotatedListHelper<OBJECT, ANNOTATION> {
	Map<ANNOTATION, TIntArrayList> index = new HashMap<ANNOTATION, TIntArrayList>();
	List<? extends Annotated<OBJECT, ANNOTATION>> data;
	
	/**
	 * Construct the {@link AnnotatedListHelper} with the given list.
	 * @param list the list
	 */
	public AnnotatedListHelper(List<? extends Annotated<OBJECT, ANNOTATION>> list) {
		this.data = list;
		
		for (int i=0; i<list.size(); i++) {
			Annotated<OBJECT, ANNOTATION> item = list.get(i);
			
			//only want to add one index/annotation, so make a set
			Set<ANNOTATION> annotations = new HashSet<ANNOTATION>(item.getAnnotations());
			
			for (ANNOTATION annotation : annotations) {
				TIntArrayList indices = index.get(annotation);
				
				if (indices == null) index.put(annotation, indices = new TIntArrayList());
				
				indices.add(i);
			}
		}
	}
		
	/**
	 * Retrieve all the items from the data that have a specific
	 * annotation.
	 * 
	 * @param annotation the annotation to search for.
	 * @return a read-only list of annotated objects with the given annotation.
	 */
	public List<Annotated<OBJECT, ANNOTATION>> get(final ANNOTATION annotation) {
		if (!index.containsKey(annotation))
			return null;
		
		return new AbstractList<Annotated<OBJECT,ANNOTATION>>() {
			TIntArrayList indices = index.get(annotation);
			
			@Override
			public Annotated<OBJECT, ANNOTATION> get(int index) {
				return data.get(indices.get(index));
			}

			@Override
			public int size() {
				return indices.size();
			}			
		};
	}
	
	/**
	 * Get the set of all known annotations
	 * @return the set of known annotations
	 */
	public Set<ANNOTATION> getAnnotations() {
		return index.keySet();
	}
	
	/**
	 * Extract the features corresponding to a specific annotation.
	 * This method doesn't actually perform the extraction, rather
	 * the returned list will perform the extraction when 
	 * {@link List#get(int)} is called. The returned list doesn't perform
	 * any kind of caching, so calling get multiple times on
	 * the same object will result in the features being extracted
	 * multiple times.
	 * <p>
	 * If you need to convert the list to a cached variety, you can write:
	 * <code>
	 * List<F> cached = new ArrayList<F>(alh.extractFeatures(annotation, extractor));
	 * </code>
	 * 
	 * @param <FEATURE> The type of feature
	 * @param annotation the annotation
	 * @param extractor the feature extractor
	 * @return the list of features.
	 */
	public <FEATURE> List<FEATURE> extractFeatures(final ANNOTATION annotation, final FeatureExtractor<FEATURE, OBJECT> extractor) {
		if (!index.containsKey(annotation))
			return null;
		
		return new AbstractList<FEATURE>() {
			TIntArrayList indices = index.get(annotation);
			
			@Override
			public FEATURE get(int index) {
				return extractor.extractFeature( data.get(indices.get(index)).getObject() );
			}

			@Override
			public int size() {
				return indices.size();
			}			
		};
	}
	
	/**
	 * Extract the features corresponding to everything EXCEPT 
	 * the specific given annotation.
	 * <p>
	 * This method doesn't actually perform the extraction, rather
	 * the returned list will perform the extraction when 
	 * {@link List#get(int)} is called. The returned list doesn't perform
	 * any kind of caching, so calling get multiple times on
	 * the same object will result in the features being extracted
	 * multiple times.
	 * <p>
	 * If you need to convert the list to a cached variety, you can write:
	 * <code>
	 * List<F> cached = new ArrayList<F>(alh.extractFeatures(annotation, extractor));
	 * </code>
	 * 
	 * @param <FEATURE> The type of feature
	 * @param annotation the annotation to exclude
	 * @param extractor the feature extractor
	 * @return the list of features.
	 */
	public <FEATURE> List<FEATURE> extractFeaturesExclude(final ANNOTATION annotation, final FeatureExtractor<FEATURE, OBJECT> extractor) {
		final TIntArrayList excludedIndices = index.get(annotation);
		final TIntArrayList selectedIndices = new TIntArrayList(data.size() - excludedIndices.size());

		for (int i=0; i<this.data.size(); i++) {
			if (excludedIndices.binarySearch(i) < 0) 
				selectedIndices.add(i);
		}
		
		return new AbstractList<FEATURE>() {
			@Override
			public FEATURE get(int index) {
				return extractor.extractFeature( data.get(selectedIndices.get(index)).getObject() );
			}

			@Override
			public int size() {
				return selectedIndices.size();
			}			
		};
	}
}
