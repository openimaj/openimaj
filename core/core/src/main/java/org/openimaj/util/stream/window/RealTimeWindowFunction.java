package org.openimaj.util.stream.window;

import java.util.ArrayList;

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
public class RealTimeWindowFunction<IN> implements Function<Stream<IN>,Stream<Window<IN,Long>>>{

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
	public Stream<Window<IN,Long>> apply(final Stream<IN> inner) {

		return new AbstractStream<Window<IN,Long>>() {

			@Override
			public boolean hasNext() {
				return inner.hasNext();
			}
			@Override
			public Window<IN,Long> next() {
				currentWindowStartTime = System.currentTimeMillis();
				final ArrayList<IN> currentWindow = new ArrayList<IN>();
				while(inner.hasNext()){
					if(System.currentTimeMillis() - currentWindowStartTime >= RealTimeWindowFunction.this.waitTime ){
						break;
					}
					IN next = inner.next();
					currentWindow.add(next);
				}

//				RealTimeWindowFunction.this.currentWindowStartTime = System.currentTimeMillis();
				return new Window<IN,Long>(currentWindowStartTime, currentWindow);
			}
		};
	}

}
