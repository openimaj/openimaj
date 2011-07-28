package org.openimaj.image.typography;

import java.text.AttributedCharacterIterator.Attribute;
import java.util.Map;

public class FontStyle<T> {
	public boolean italic;
	public float angle;
	public int lineWidth;
	public T fillColour;
	public T lineColour;
	
	public void parseAttributes(Map<Attribute,Object> attrs) {
		
	}
}
