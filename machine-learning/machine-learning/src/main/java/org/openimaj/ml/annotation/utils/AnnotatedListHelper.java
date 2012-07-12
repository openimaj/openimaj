package org.openimaj.ml.annotation.utils;

import gnu.trove.list.array.TIntArrayList;

import java.util.AbstractList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
}
