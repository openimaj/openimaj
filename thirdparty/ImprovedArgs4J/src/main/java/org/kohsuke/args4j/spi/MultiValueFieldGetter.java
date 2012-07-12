package org.kohsuke.args4j.spi;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.kohsuke.args4j.IllegalAnnotationError;
import org.openimaj.util.pair.IndependentPair;

/**
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk), Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class MultiValueFieldGetter extends AbstractGetter<Object> {
	
	/**
	 * @param bean
	 * @param name
	 * @param f
	 */
	public MultiValueFieldGetter(String name, Object bean, Field f) {
		super(name,bean,f);
	}

	@Override
	public List<IndependentPair<String, Class<?>>> getStringValues()  {
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

	private List<IndependentPair<String, Class<?>>> getListStrings() throws IllegalArgumentException, IllegalAccessException {
		Object o = f.get(bean);
        if(o==null) {
            return new ArrayList<IndependentPair<String, Class<?>>>();
        }
        if(!(o instanceof List))
            throw new IllegalAnnotationError(Messages.ILLEGAL_FIELD_SIGNATURE.format(f));
        ArrayList<IndependentPair<String, Class<?>>> ret = new ArrayList<IndependentPair<String, Class<?>>>();
        for(Object obj : (List<?>) o){
        	Class<?> objClass = obj.getClass();
        	ret.add(new IndependentPair<String, Class<?>>(obj.toString(), objClass));
        }
        return ret;
	}

	@Override
	public boolean isMultiValued() {
		return true;
	}

}
