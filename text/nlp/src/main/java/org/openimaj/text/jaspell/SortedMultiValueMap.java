package org.openimaj.text.jaspell;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * A {@link de.fu_berlin.ties.util.MultiValueMap} that sorts the values
 * stored for each key, discarding duplicates. For this purpose,
 * {@link java.util.TreeSet}s are used for inner collections.
 *
 * By default, the keys are sorted as well, by wrapping a
 * {@link java.util.TreeMap}. This can be changed by specifying a different kind
 * of map to wrap in the constructor.
 *
 * @author Bruno Martins
 */
public class SortedMultiValueMap extends MultiValueMap {

    /**
     * Creates a new instance, wrapping a {@link TreeMap}.
     */
    public SortedMultiValueMap() {
        this(new TreeMap());
    }

    /**
     * Creates a new instance.
     *
     * @param wrappedMap wrapped map used as storage, e.g. a {@link HashMap} or
     * a {@link java.util.TreeMap}
     */
    public SortedMultiValueMap(Map wrappedMap) {
        super(wrappedMap);
    }

    /**
     * {@inheritDoc}
     * This implementation returns a {@link TreeSet}.
     */
    protected Collection createCollection(final Collection coll) {
        if (coll == null) {
            return new TreeSet();
        } else {
            return new TreeSet(coll);
        }
    }

}
