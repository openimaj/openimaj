package org.kohsuke.args4j.spi;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.kohsuke.args4j.IllegalAnnotationError;

/**
 * A getter for map objects
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk), Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class MapGetter extends AbstractGetter<Object>{

	/**
	 * @param name
	 * @param bean
	 * @param f
	 */
	public MapGetter(String name, Object bean, Field f) {
		super(name, bean,f);
	}

	@Override
	public List<String> getStringValues()  {
		try {
			return getListStrings();
		} catch (Exception _) {
			// try again
            f.setAccessible(true);
            try {
                return getListStrings();
            } catch (Exception e) {
                throw new IllegalAccessError(e.getMessage());
            }
		}
	}

	private List<String> getListStrings() throws IllegalArgumentException, IllegalAccessException {
		Object o = f.get(bean);
        if(o==null) {
            return new ArrayList<String>();
        }
        if(!(o instanceof Map))
            throw new IllegalAnnotationError(Messages.ILLEGAL_FIELD_SIGNATURE.format(f));
        ArrayList<String> ret = new ArrayList<String>();
        for(Entry<?, ?> obj : ((Map<?,?>) o).entrySet()){
        	ret.add(obj.getKey().toString() + "=" + obj.getValue().toString());
        }
        return ret;
	}
	
	@Override
	public boolean isMultiValued() {
		return true;
	}
}
