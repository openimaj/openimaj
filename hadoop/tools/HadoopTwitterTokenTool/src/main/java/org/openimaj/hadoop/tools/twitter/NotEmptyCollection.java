package org.openimaj.hadoop.tools.twitter;

import java.util.Collection;

import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;

import com.jayway.jsonassert.impl.matcher.CollectionMatcher;

/**
 * asserts that a collection is not empty
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 * @param <T>
 */
public class NotEmptyCollection<T> extends CollectionMatcher<Collection<T>> {

	@Override
    public boolean matchesSafely(Collection<T> item) {
        return !item.isEmpty();
    }

    @Override
	public void describeTo(Description description) {
        description.appendText("an non-empty collection");
    }

    /**
     * Matches an empty collection.
     * @return an instance
     */
    @Factory
    public static <T> Matcher<?> notempty() {
        return new NotEmptyCollection<T>();
    }
}
