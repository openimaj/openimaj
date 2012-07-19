package org.openimaj.ml.annotation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openimaj.experiment.dataset.GroupedDataset;
import org.openimaj.experiment.dataset.ListDataset;

/**
 * Basic implementation of {@link Annotated}. 
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 * @param <OBJECT> Type of object.
 * @param <ANNOTATION> Type of annotations
 */
public class AnnotatedObject<OBJECT, ANNOTATION> implements Annotated<OBJECT, ANNOTATION> {
	/**
	 * The annotated object
	 */
	public OBJECT object;
	
	/**
	 * The annotations
	 */
	public Collection<ANNOTATION> annotations;
	
	/**
	 * Construct with the given object and its annotations.
	 * @param object the object
	 * @param annotations the objects annotations.
	 */
	public AnnotatedObject(OBJECT object, Collection<ANNOTATION> annotations) {
		this.object = object;
		this.annotations = annotations;
	}
	
	/**
	 * Construct with the given object and its annotation.
	 * @param object the object
	 * @param annotation the object's annotation.
	 */
	public AnnotatedObject(OBJECT object, ANNOTATION annotation) {
		this.object = object;
		
		this.annotations = new ArrayList<ANNOTATION>();
		annotations.add(annotation);
	}
	
	@Override
	public OBJECT getObject() {
		return object;
	}

	@Override
	public Collection<ANNOTATION> getAnnotations() {
		return annotations;
	}

	/**
	 * Create an {@link AnnotatedObject} with the given 
	 * object and its annotations.
	 * 
	 * @param <OBJECT> Type of object.
	 * @param <ANNOTATION> Type of annotations
	 * @param object the object
	 * @param annotations the objects annotations.
	 * @return the new {@link AnnotatedObject} 
	 */
	public static <OBJECT, ANNOTATION> AnnotatedObject<OBJECT, ANNOTATION> create(OBJECT object, Collection<ANNOTATION> annotations) {
		return new AnnotatedObject<OBJECT, ANNOTATION>(object, annotations); 
	}
	
	/**
	 * Create an {@link AnnotatedObject} with the given 
	 * object and its annotation.
	 * 
	 * @param <OBJECT> Type of object.
	 * @param <ANNOTATION> Type of annotations
	 * @param object the object
	 * @param annotation the object's annotation.
	 * @return the new {@link AnnotatedObject} 
	 */
	public static <OBJECT, ANNOTATION> AnnotatedObject<OBJECT, ANNOTATION> create(OBJECT object, ANNOTATION annotation) {
		return new AnnotatedObject<OBJECT, ANNOTATION>(object, annotation); 
	}
	
	/**
	 * Convert a grouped dataset to a list of annotated objects. The annotations
	 * correspond to the type of group. If the same object appears in multiple
	 * groups within the dataset then it will have multiple annotations. 
	 * 
	 * @param <OBJECT> Type of object.
	 * @param <ANNOTATION> Type of annotations.
	 * @param dataset the dataset
	 * @return the list of annotated instances
	 */
	public static <OBJECT, ANNOTATION> List<AnnotatedObject<OBJECT, ANNOTATION>> createList(GroupedDataset<ANNOTATION, ListDataset<OBJECT>, OBJECT> dataset) {
		Map<OBJECT, AnnotatedObject<OBJECT, ANNOTATION>> annotated = new HashMap<OBJECT, AnnotatedObject<OBJECT,ANNOTATION>>(dataset.size());
		
		for (ANNOTATION grp : dataset.getGroups()) {
			for (OBJECT inst : dataset.getInstances(grp)) {
				AnnotatedObject<OBJECT, ANNOTATION> ao = annotated.get(inst);
				
				if (ao == null)
					annotated.put(inst, new AnnotatedObject<OBJECT, ANNOTATION>(inst, grp));
				else
					ao.annotations.add(grp);
			}
		}
		
		return new ArrayList<AnnotatedObject<OBJECT,ANNOTATION>>(annotated.values());
	}
}
