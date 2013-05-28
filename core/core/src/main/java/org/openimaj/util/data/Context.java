package org.openimaj.util.data;

import java.util.HashMap;
import java.util.HashSet;
import java.util.NoSuchElementException;

/**
 * A Context is a HashMap which can give typed elements and can fail gracefully when elements don't exist
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class Context extends HashMap<String, Object> implements Cloneable{

	/**
	 *
	 */
	private static final long serialVersionUID = 1888665727867672296L;
	private boolean failfast = false;
	/**
	 *
	 */
	public Context() {

	}
	/**
	 * @param failfast forces the getTyped to throw a runtime exception if set and the object does not exist or is not the correct type
	 */
	public Context(boolean failfast){
		this.failfast = failfast;
	}

	/**
	 * @param o
	 * @return the object with key o cast to a specific type
	 */
	public <T> T getTyped(String o){
		Object retUntyped = this.get(o);
		if(retUntyped == null){
			if(failfast) throw new RuntimeException(new NoSuchElementException("Object not found"));
			return null;
		}
		try{
			@SuppressWarnings("unchecked")
			T ret = (T)retUntyped;
			return ret;
		}
		catch(Throwable t){
			if(failfast) throw new RuntimeException(t);
			return null;
		}
	}

	@Override
	public Context clone(){
		Context c = new Context();
		for (java.util.Map.Entry<String, Object> es: this.entrySet()) {
			c.put(es.getKey(), es.getValue());
		}
		return c;
	}
	/**
	 *
	 * @param that
	 * @param thisprefix
	 * @param thatprefix
	 * @return combined context
	 */
	public Context combine(Context that, String thisprefix, String thatprefix) {
		Context combined = new Context();

		HashSet<String> sharedKeys = new HashSet<String>(this.keySet());
		sharedKeys.retainAll(that.keySet());
		HashSet<String> thiskeys = new HashSet<String>(this.keySet());
		thiskeys.removeAll(sharedKeys);
		HashSet<String> thatkeys = new HashSet<String>(that.keySet());
		thatkeys.removeAll(sharedKeys);
		// Add the pref
		for (String key : sharedKeys) {
			combined.put(thisprefix + key, this.get(key));
			combined.put(thatprefix + key, that.get(key));
		}
		for (String key : thatkeys) {
			combined.put(key, that.get(key));
		}
		for (String key : thiskeys) {
			combined.put(key, this.get(key));
		}
		return combined;
	}
}
