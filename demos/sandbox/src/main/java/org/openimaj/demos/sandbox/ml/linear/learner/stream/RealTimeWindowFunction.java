package org.openimaj.demos.sandbox.ml.linear.learner.stream;

import java.util.ArrayList;
import java.util.List;

import org.openimaj.util.function.Function;
import org.openimaj.util.stream.AbstractStream;
import org.openimaj.util.stream.Stream;

/**
 * Given a period of time to wait in milliseconds, this function consumes
 * a stream for that period of time and produces a new stream of lists representing windows of that time period
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 * @param <IN>
 */
public class RealTimeWindowFunction<IN> implements Function<Stream<IN>,Stream<List<IN>>>{
	private long waitTime;

	private long currentWindowStartTime;
	/**
	 * @param waitTime
	 */
	public RealTimeWindowFunction(long waitTime) {
		this.waitTime = waitTime;
		this.currentWindowStartTime = -1;
	}

	@Override
	public Stream<List<IN>> apply(final Stream<IN> inner) {

		return new AbstractStream<List<IN>>() {

			@Override
			public boolean hasNext() {
				return inner.hasNext();
			}
			@Override
			public List<IN> next() {
				currentWindowStartTime = System.currentTimeMillis();
				ArrayList<IN> currentWindow = new ArrayList<IN>();
				while(inner.hasNext()){
					if(System.currentTimeMillis() - currentWindowStartTime >= RealTimeWindowFunction.this.waitTime ){
						break;
					}
					IN next = inner.next();
					currentWindow.add(next);
				}

//				RealTimeWindowFunction.this.currentWindowStartTime = System.currentTimeMillis();
				return currentWindow;
			}
		};
	}

}
