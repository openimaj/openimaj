package org.openimaj.ml.timeseries.interpolation.util;

public class TimeSpanUtils {
	/**
	 * Get
	 * @param begin
	 * @param end
	 * @param delta
	 * @return
	 */
	public static long[] getTime(long begin, long end, long delta) {
		long[] times = new long[(int) ((end - begin)/delta) + 1];
		long val = begin;
		for (int i = 0; i < times.length; i++) {
			times[i] = val;
			val += delta;
		}
		return times;
	}
	
	public static long[] getTime(long begin, long end, int splits) {
		long[] times = new long[splits];
		long delta = (end - begin) / (splits-1);
		long val = begin;
		for (int i = 0; i < times.length; i++) {
			times[i] = val;
			val += delta;
		}
		return times;
	}
	
	public static long[] getTime(long begin, int steps, long delta){
		long[] times = new long[steps];
		long val = begin;
		for (int i = 0; i < times.length; i++) {
			times[i] = val;
			val += delta;
		}
		return times;
	}
}
