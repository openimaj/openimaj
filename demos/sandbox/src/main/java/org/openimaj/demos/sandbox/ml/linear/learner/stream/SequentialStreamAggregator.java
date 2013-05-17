package org.openimaj.demos.sandbox.ml.linear.learner.stream;

import java.util.Comparator;

import org.openimaj.util.function.Function;
import org.openimaj.util.stream.AbstractStream;
import org.openimaj.util.stream.Stream;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * @param <T>
 *
 */
public abstract class SequentialStreamAggregator<T>
		implements
			Function<Stream<T>, Stream<T>>
{

	private Comparator<T> comp;

	/**
	 * @param comp the comparator for decided whether sequential items are the same
	 */
	public SequentialStreamAggregator(Comparator<T> comp) {
		this.comp = comp;
	}

	@Override
	public Stream<T> apply(final Stream<T> inner) {
		return new AbstractStream<T>() {

			T currentAgg = null;
			@Override
			public boolean hasNext() {
				return inner.hasNext();
			}
			@Override
			public T next() {
				while(inner.hasNext()){
					T next = inner.next();
					if(currentAgg == null)
					{
						currentAgg = next;
					}
					else{
						if(comp.compare(currentAgg, next) == 0){
							currentAgg = combine(currentAgg,next);
						}
						else{
							T toRet = currentAgg;
							currentAgg = next;
							return toRet;
						}
					}
				}
				// The end of the stream is reached
				if(currentAgg!=null){
					return currentAgg;
				}
				else{
					throw new UnsupportedOperationException("The sequential combiner failed");
				}
			}
		};
	}

	/**
	 * Called when two sequential items are identical and must therefore be combined
	 *
	 * @param current
	 * @param next
	 * @return the combination of the current and next values
	 */
	public abstract T combine(T current, T next) ;


}
