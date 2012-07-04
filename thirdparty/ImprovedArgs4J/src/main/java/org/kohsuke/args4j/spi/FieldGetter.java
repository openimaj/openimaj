package org.kohsuke.args4j.spi;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.kohsuke.args4j.CmdLineException;

/**
 * A field getter calls .toString() on the underlying object
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk), Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class FieldGetter extends AbstractGetter<String> {

	/**
	 * @param name 
	 * @param bean
	 * @param f
	 */
	public FieldGetter(String name,Object bean, Field f) {
		super(name,bean, f);
	}

	@Override
	public List<String> getStringValues() throws CmdLineException {
		List<String> ret = new ArrayList<String>();
		try {
			if(f.get(bean) == null) return ret;
			ret.add(f.get(bean).toString());
		} catch (Exception _) {
			// try again
            f.setAccessible(true);
            try {
            	if(f.get(bean) == null) return ret;
            	ret.add(f.get(bean).toString());
            } catch (Exception e) {
                throw new IllegalAccessError(e.getMessage());
            }
		}
		return ret ;
	}

}
