package org.openimaj.twitter.collection;

import org.openimaj.io.VariableLength;

public class TwitterStatusListUtils {

	public static<T> T newInstance(Class<T> cls) {
		try {
			if(VariableLength.class.isAssignableFrom(cls)) {
				return cls.getConstructor().newInstance();
			}

			return cls.newInstance();
		} catch (Exception e) {
			throw new RuntimeException(e);
		} 
	}

}
