package org.openimaj.text.jaspell;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * A static class that provides utility methods for working with
 * {@link java.util.Collection}s and arrays. No instances of this class can be
 * created, only the static members should be used.
 *
 * @author Bruno Martins
 */
public final class CollUtils {

    /**
     * Default separator used to {@linkplain #flatten(Object[]) flatten} array
     * if no other separator is specified: {@value} (a single space).
     */
    public static final String SEPARATOR = " ";

    /**
     * Convenience method that adds all members of an array to a collection.
     *
     * @param coll the collection to add to
     * @param array the array to value to add
     */
    public static void addAll(final Collection coll, final Object[] array) {
        for (int i = 0; i < array.length; i++) {
            coll.add(array[i]);
        }
    }

    /**
     * Wraps an array into a set. Duplicates will be ignored.
     *
     * @param array the array to wrap
     * @return a set containing the contents of the set (in random order,
     * without duplicates)
     */
    public static Set arrayAsSet(final Object array[]) {
        return new HashSet(Arrays.asList(array));
    }

    /**
     * Combines two array into a target array, inserting all elements of the
     * first array and then all elements of the second array in the target
     * array.
     *
     * @param array1 the first array to copy
     * @param array2 the second array to copy
     * @param targetArray the array to copy the two other array into; the
     * type of this array must be suitable to accept elements from both array;
     * the length of this array must be equal or greater than
     * <code>array1.length + array2.length</code>
     */
    public static void combineArrays(final Object[] array1,
                                    final Object[] array2,
                                    final Object[] targetArray) {
        final int lengthOfFirst = array1.length;
        int i;
        // copy first array into target array
        for (i = 0; i < lengthOfFirst; i++) {
            targetArray[i] = array1[i];
        }
    
        // copy second array into target array
        for (i = 0; i < array2.length; i++) {
            targetArray[i + lengthOfFirst] = array2[i];
        }
    }

    /**
     * Flattens the objects returned by an iterator into a single string,
     * separating elements by a space character.
     *
     * @param iterator the iterator over the elements to join
     * @return the flattened string
     */
    public static String flatten(final Iterator iterator) {
        return flatten(iterator, SEPARATOR);
    }

    /**
     * Flattens the objects returned by an iterator into a single string,
     * separating elements by the provided separator.
     *
     * @param iterator the iterator over the elements to join
     * @param separator the separator string to use
     * @return the flattened string
     */
    public static String flatten(final Iterator iterator, final String separator) {
        final StringBuilder result = new StringBuilder();

        while (iterator.hasNext()) {
            result.append(iterator.next().toString());
            if (iterator.hasNext()) {
                result.append(separator);                
            }
        }
        return result.toString();
    }

    /**
     * Flattens the elements of the provided array into a single string,
     * separating elements by a space character.
     *
     * @param array the array of values to join
     * @return the flattened string
     */
    public static String flatten(final Object[] array) {
        return flatten(array, SEPARATOR);
    }

    /**
     * Flattens the elements of the provided array into a single string,
     * separating elements by the provided separator.
     *
     * @param array the array of values to join
     * @param separator the separator string to use
     * @return the flattened string
     */
    public static String flatten(final Object[] array, final String separator) {
        final StringBuilder result = new StringBuilder();

        for (int i = 0; i < array.length; i++) {
            if (i > 0) {
                result.append(separator);
            }
            result.append(array[i]);
        }
        return result.toString();
    }


    /**
     * Copied the last <em>n</em> elements from a list into a new list (or all
     * elements, if the size of the input list is smaller or equal to
     * <em>n</em>). Modifications of the returned list will not affect the
     * original list and vice versa.
     *
     * <p>If <code>number</code> is 0 or negative or if the original list is
     * <code>null</code> or empty, an empty list is returned.
     *
     * <p>Note that this is somewhat inefficient if the input list is a
     * {@link java.util.LinkedList} because repeated calls to
     * {@link List#get(int)} are necessary (unless the whole list is copied).
     *
     * @param list the input list
     * @param number the number of elements to copy
     * @return an ArrayList containing the last elements from the original list;
     * or an empty ArrayList iff the <code>list</code> is <code>null</code>
     */
    public static ArrayList lastN(final List list, final int number) {
        final ArrayList result;
        if (list != null) {
            final int size = list.size();
            if (number <= size) {
                // copy the whole collection
                result = new ArrayList(list);
            } else {
                // add last N elements
                result = new ArrayList(number);
                for (int i = number; i > 0; i--) {
                    result.add(list.get(size - number));
                }
            }
        } else {
            // return empty list if input list is null
            result = new ArrayList(0);
        }
        return result;
    }


    /**
     * Private constructor prevents creation of instances.
     */
    private CollUtils() {
        super();
    }

}
