package org.openimaj.util.stream.window;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

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

			List<T> currentList = new ArrayList<T>();
			@Override
			public boolean hasNext() {
				return inner.hasNext() || currentList.size() != 0;
			}
			@Override
			public T next() {
				while(inner.hasNext()){
					T next = inner.next();
					if(currentList.size() == 0 || comp.compare(currentList.get(0), next) == 0){
						currentList.add(next);
					}
					else{
						T toRet = combine(currentList);
						currentList.clear();
						currentList.add(next);
						return toRet;
					}
				}
				// The end of the stream is reached
				if(currentList.size()!=0){
					T toRet = combine(currentList);
					currentList.clear();
					return toRet;
				}
				else{
					throw new UnsupportedOperationException("The sequential combiner failed");
				}
			}
		};
	}

	/**
	 * Called when a window of identical items needs to be combined
	 * @param window
	 *
	 * @return the combination of the current and next values
	 */
	public abstract T combine(List<T> window) ;


}
