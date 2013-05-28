package org.openimaj.util.stream.window;

import java.util.ArrayList;

import org.openimaj.util.data.Context;
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
public class ContextRealTimeWindowFunction<IN> implements Function<Stream<IN>,Stream<Context>>{

	private long waitTime;

	private long currentWindowStartTime;
	/**
	 * @param waitTime
	 */
	public ContextRealTimeWindowFunction(long waitTime) {
		this.waitTime = waitTime;
		this.currentWindowStartTime = -1;
	}

	@Override
	public Stream<Context> apply(final Stream<IN> inner) {

		return new AbstractStream<Context>() {

			@Override
			public boolean hasNext() {
				return inner.hasNext();
			}
			@Override
			public Context next() {
				currentWindowStartTime = System.currentTimeMillis();
				final ArrayList<IN> currentWindow = new ArrayList<IN>();
				while(inner.hasNext()){
					if(System.currentTimeMillis() - currentWindowStartTime >= ContextRealTimeWindowFunction.this.waitTime ){
						break;
					}
					IN next = inner.next();
					currentWindow.add(next);
				}

//				RealTimeWindowFunction.this.currentWindowStartTime = System.currentTimeMillis();
				Context context = new Context();
				context.put("windowstart", currentWindowStartTime);
				context.put("item",currentWindow);

				return context;
			}
		};
	}

}
