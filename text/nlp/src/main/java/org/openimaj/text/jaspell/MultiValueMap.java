package org.openimaj.text.jaspell;

import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * A <code>MultiValueMap</code> allows storing multiple values for each key.
 * Putting a value into the map will add the value to a Collection at that key.
 * Getting a value will return a Collection, holding all the values put to that
 * key.
 * <p>
 * This implementation uses an <code>ArrayList</code> as the collection.
 * When there are no values mapped to a key, <code>null</code> is returned.
 * <p>
 * This class does not implement the {@link java.util.Map} interface because
 * of the slightly different semantics: <code>put</code> adds a value of
 * type <code>V</code>, but <code>get</code> returns a Collection of
 * <code>V</code> objects instead of a single <code>V</code> object.
 * <p>
 * For example:
 * <pre>
 * MultiValueMap&lt;String, String&gt; mm =
 *     new MultiValueMap&lt;String, String&gt;();
 * mm.put(key, "A");
 * mm.put(key, "B");
 * mm.put(key, "C");
 * Collection&lt;String&gt; col = mm.get(key);
 * </pre>
 * 
 * <p>
 * <code>col</code> will be a collection containing "A", "B", "C".
 * <p>
 * This class has been adapted from the <code>MultiHashMap</code> in the 
 * <a href="http://jakarta.apache.org/commons/collections/">Jakarta Commons 
 * Collections</a>.
 * 
 * @author Bruno Martins
 */
public class MultiValueMap {


    /**
     * Inner class to view the elements.
     */
    private class Values extends AbstractCollection {

        /**
         * {@inheritDoc}
         */
        public Iterator iterator() {
            return new ValueIterator();
        }

        /**
         * {@inheritDoc}
         */
        public int size() {
            int compt = 0;
            Iterator it = iterator();
            while (it.hasNext()) {
                it.next();
                compt++;
            }
            return compt;
        }

        /**
         * {@inheritDoc}
         */
        public void clear() {
            MultiValueMap.this.clear();
        }

    }

    /**
     * Inner iterator to view the elements.
     */
    private final class ValueIterator implements Iterator {

        /**
         * The backed iterator.
         */
        private Iterator backedIterator;

        /**
         * Iterator used to search.
         */
        private Iterator tempIterator;

        /**
         * Creates a new instance.
         */
        private ValueIterator() {
            backedIterator = store.values().iterator();
        }

        /**
         * Searches for the next available iterator.
         * @return <code>true</code> if there is a new iterator
         */
        private boolean searchNextIterator() {
            while (tempIterator == null || !tempIterator.hasNext()) {
                if (!backedIterator.hasNext()) {
                    return false;
                }
                tempIterator = ((Collection)backedIterator.next()).iterator();
            }
            return true;
        }

        /**
         * {@inheritDoc}
         */
        public boolean hasNext() {
            return searchNextIterator();
        }

        /**
         * {@inheritDoc}
         */
        public Object next() {
            if (!searchNextIterator()) {
                throw new NoSuchElementException();
            }
            return tempIterator.next();
        }

        /**
         * {@inheritDoc}
         */
        public void remove() {
            if (tempIterator == null) {
                throw new IllegalStateException();
            }
            tempIterator.remove();
        }

    }

    /**
     * Wrapped map used as storage.
     */
    private final Map store;

    /**
     * Backed values collection.
     */
    private transient Collection values = null;

    /**
     * Creates a new instance, using a {@link HashMap} as storage.
     */
    public MultiValueMap() {
        this(new HashMap());
    }

    /**
     * Creates a new instance.
     * 
     * @param wrappedMap wrapped map used as storage, e.g. a {@link HashMap} or
     * a {@link java.util.TreeMap}
     */
    public MultiValueMap(final Map wrappedMap) {
        store = wrappedMap;
    }

    /**
     * Creates a new instance of the map value Collection container.
     * <p>
     * This method can be overridden to use your own collection type.
     *
     * @param coll the collection to copy, may be <code>null</code>
     * @return the new collection
     */
    protected Collection createCollection(final Collection coll) {
        if (coll == null) {
            return new ArrayList();
        } else {
            return new ArrayList(coll);
        }
    }

    /**
     * Removes all mappings from this map.
     */
    public void clear() {
        store.clear();
    }

    /**
     * Returns <code>true</code> if this map contains a mapping for the
     * specified key. More formally, returns <code>true</code> if and only if
     * this map contains a mapping for a key <code>k</code> such that
     * <code>(key==null ? k==null : key.equals(k))</code>. (There can be one or
     * several such mappings.)
     * 
     * @param key key whose presence in this map is to be tested
     * @return <code>true</code> if this map contains a mapping for the 
     * pecified key
     */
    public boolean containsKey(Object key) {
        return store.containsKey(key);
    }

    /**
     * Checks whether the map contains the value specified.
     * <p>
     * This checks all collections against all keys for the value, and thus
     * could be slow.
     *
     * @param value the value to search for
     * @return <code>true</code> if the map contains the value
     */
    public boolean containsValue(Object value) {
        final Set pairs = store.entrySet();
        if (pairs == null) {
            return false;
        }

        final Iterator pairsIterator = pairs.iterator();
        Map.Entry keyValuePair;

        while (pairsIterator.hasNext()) {
            keyValuePair = (Map.Entry) pairsIterator.next();
            Collection coll = (Collection) keyValuePair.getValue();
            if (coll.contains(value)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks whether the collection at the specified key contains the value.
     *
     * @param key the key to use
     * @param value the value to search for
     * @return <code>true</code> if the map contains the value at the specified
     * key
     */
    public boolean containsValue(final Object key, final Object value) {
        Collection coll = get(key);
        if (coll == null) {
            return false;
        } else {
            return coll.contains(value);            
        }
    }

    /**
     * Compares the specified object with this map for equality. Returns
     * <code>true</code> if the given object is also a
     * <code>MultiValueMap</code> map or a map of collections and the two Maps
     * represent the same mappings.
     * 
     * @param o object to be compared for equality with this map
     * @return <code>true</code> if the specified object is equal to this map
     */
    public boolean equals(Object o) {
        if (o instanceof MultiValueMap) {
            // compare wrapped maps
            return store.equals(((MultiValueMap) o).store);
        } else if (o instanceof Map) {
            // compare wrapped map with object
            return store.equals(o);
        } else {
            return false;
        }
    }

    /**
     * Returns the collection of values to which this map maps the specified
     * key.
     * 
     * @param key key whose associated value is to be returned
     * @return the collection of values to which this map maps the specified
     * key, or <code>null</code> if the map contains no mapping for this key
     */
    public Collection get(Object key) {
        return (Collection)(store.get(key));
    }

    /**
     * Returns the hash code value for this map.
     * 
     * @return the hash code value for this map.
     */
    public int hashCode() {
        return store.hashCode();
    }

    /**
     * Returns <code>true</code> if this map contains no key-value mappings.
     * 
     * @return <code>true</code> if this map contains no key-value mappings
     */
    public boolean isEmpty() {
        return store.isEmpty();
    }

    /**
     * Returns a set view of the keys contained in this map. The set is backed
     * by the map, so changes to the map are reflected in the set, and
     * vice-versa.
     *
     * @return a set view of the keys contained in this map
     */
    public Set keySet() {
        return store.keySet();
    }

    /**
     * Adds the value to the collection associated with the specified key.
     * <p>
     * Unlike a normal <code>Map</code> the previous value is not replaced.
     * Instead the new value is added to the collection stored against the key.
     *
     * @param key the key to store against
     * @param value the value to add to the collection at the key
     * @return the value added if the map changed and <code>null</code> if the
     * map did not change
     */
    public Object put(Object key, Object value) {
        Collection coll = get(key);
        if (coll == null) {
            coll = createCollection(null);
            store.put(key, coll);
        }
        final boolean result = coll.add(value);
        return (result ? value : null);
    }

    /**
     * Adds a collection of values to the collection associated with the
     * specified key.
     *
     * @param key the key to store against
     * @param valueCol the values to add to the collection at the key,
     * ignored if <code>null</code>
     * @return <code>true</code> if this map changed
     */
    public boolean putAll(final Object key, final Collection valueCol) {
        if (valueCol == null || valueCol.size() == 0) {
            return false;
        }

        Collection coll = get(key);
        if (coll == null) {
            coll = createCollection(valueCol);
            if (coll.size() == 0) {
                return false;
            }
            store.put(key, coll);
            return true;
        } else {
            return coll.addAll(valueCol);
        }
    }

    /**
     * Removes all mappings for this key from this map if any are present.
     * Returns the collection of value to which the map previously associated
     * the key, or <code>null</code> if the map contained no mappings for this
     * key. The map will not contain any mappings for the specified key once
     * the call returns.
     * 
     * @param key key whose mappings are to be removed from the map.
     * @return collection of values previously associated with specified key,
     * or <code>null</code> if there was no mapping for key
     */
    public Collection remove(Object key) {
        return (Collection)(store.remove(key));
    }

    /**
     * Removes a specific value from map.
     * <p>
     * The item is removed from the collection mapped to the specified key.
     * Other values attached to that key are unaffected.
     * <p>
     * If the last value for a key is removed, <code>null</code> will be
     * returned from a subsequant <code>get(key)</code>.
     *
     * @param key the key to remove from
     * @param item the value to remove
     * @return the value removed (which was passed in), <code>null</code> if
     * nothing removed
     */
    public Object remove(final Object key, final Object item) {
        final Collection valuesForKey = get(key);
        if (valuesForKey == null) {
            return null;
        } else {
            valuesForKey.remove(item);

            // remove the list if it is now empty
            // (saves space, and allows equals to work)
            if (valuesForKey.isEmpty()) {
                remove(key);
            }
            return item;
        }
    }

    /**
     * Returns the number of key-value mappings in this map. If the map contains
     * more than <code>Integer.MAX_VALUE</code> elements, returns
     * <code>Integer.MAX_VALUE</code>.
     * 
     * @return the number of key-value mappings in this map
     */
    public int size() {
        return store.size();
    }

    /**
     * Gets the size of the collection mapped to the specified key.
     *
     * @param key the key to get size for
     * @return the size of the collection at the key, zero if key not in map
     */
    public int size(final Object key) {
        final Collection coll = get(key);
        if (coll == null) {
            return 0;
        } else {
            return coll.size();
        }
    }

    /**
     * Gets the total size of the map by counting all the values.
     *
     * @return the total size of the map counting all values
     */
    public int totalSize() {
        int total = 0;
        final Iterator it = store.values().iterator();
        Collection coll;
        while (it.hasNext()) {
            coll = (Collection)(it.next());
            total += coll.size();
        }
        return total;
    }

    /**
     * Gets a collection containing all the values in the map.
     * <p>
     * This returns a collection containing the combination of values from all
     * keys.
     *
     * @return a collection view of the values contained in this map
     */
    public Collection values() {
        if (values == null) {
             values = new Values();
        }
        return values;
    }

}
