package org.kohsuke.args4j.spi;
import java.util.List;

import org.kohsuke.args4j.CmdLineException;
import org.openimaj.util.pair.IndependentPair;

/**
 * Abstraction of the value setter.
 *
 * @author Kohsuke Kawaguchi
 * @param <T> the type returned
 */
public interface Getter<T> {
    /**
     * A {@link Getter} object has an implicit knowledge about the property it's setting,
     * and the instance of the option bean.
     * 
     * @return Get all values to the property of the option bean.
     * @throws CmdLineException 
     */
    List<IndependentPair<String, Class<?>>> getStringValues() throws CmdLineException;

    /**
     * @return Gets the type of the underlying method/field.
     */
    Class<?> getType();
    
    /**
     * @return Whether this setter is instrinsically multi-valued.
     */
    boolean isMultiValued();
    
    /**
     * @return The option name
     */
    public String getOptionName();
}