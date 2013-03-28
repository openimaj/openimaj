package org.openimaj.util.stream;

import java.util.Iterator;

import org.openimaj.util.function.Function;
import org.openimaj.util.function.Operation;
import org.openimaj.util.function.Predicate;

public interface Stream<T> extends Iterator<T>, Iterable<T> {

	public void forEach(Operation<T> op);

	public void forEach(Operation<T> operation, Predicate<T> stopPredicate);

	public Stream<T> filter(Predicate<T> filter);

	public <R> Stream<R> map(Function<T, R> filter);
}
