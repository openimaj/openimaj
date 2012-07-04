package org.kohsuke.args4j.spi;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

/**
 * Factory of {@link Setter}s.
 *
 * @author Kohsuke Kawaguchi
 */
public class Getters {

    /**
     * 
     * @param name
     * @param f
     * @param bean
     * @return estimate the type of field and return the appropriate getter
     */
    public static Getter<?> create(String name, Field f, Object bean) {
        if(List.class.isAssignableFrom(f.getType()))
            return new MultiValueFieldGetter(name,bean,f);
        else if(Map.class.isAssignableFrom(f.getType()))
            return new MapGetter(name,bean,f);
        else
            return new FieldGetter(name,bean,f);
    }
}