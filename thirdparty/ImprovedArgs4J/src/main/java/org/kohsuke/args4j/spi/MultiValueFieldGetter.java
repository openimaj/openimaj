package org.kohsuke.args4j.spi;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.kohsuke.args4j.IllegalAnnotationError;

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

	@SuppressWarnings("unchecked")
	private List<String> getListStrings() throws IllegalArgumentException, IllegalAccessException {
		Object o = f.get(bean);
        if(o==null) {
            return new ArrayList<String>();
        }
        if(!(o instanceof List))
            throw new IllegalAnnotationError(Messages.ILLEGAL_FIELD_SIGNATURE.format(f));
        ArrayList<String> ret = new ArrayList<String>();
        for(Object obj : (List<?>) o){
        	ret.add(obj.toString());
        }
        return ret;
	}

	@Override
	public boolean isMultiValued() {
		return true;
	}

}
