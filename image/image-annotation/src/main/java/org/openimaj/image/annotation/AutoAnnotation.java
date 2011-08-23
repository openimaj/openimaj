package org.openimaj.image.annotation;

public class AutoAnnotation {
	public String annotation;
	public float confidence;

	public AutoAnnotation(String annotation, float confidence) {
		this.annotation = annotation;
		this.confidence = confidence;
	}
	
	public String toString() {
		return annotation;
	}
}
