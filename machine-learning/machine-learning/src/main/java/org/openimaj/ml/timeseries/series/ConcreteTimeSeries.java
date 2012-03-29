package org.openimaj.ml.timeseries.series;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.openimaj.ml.timeseries.TimeSeries;
import org.openimaj.ml.timeseries.TimeSeriesSetException;
import org.openimaj.util.reflection.ReflectionUtils;

/**
 * A generic though inefficient time series which can be used by any data type. This implementation
 * is backed by a treemap. Array construction is handled using {@link ReflectionUtils#getTypeArguments(Class, Class)}. To
 * use this class simple define a new class 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>, Sina Samangooei <ss@ecs.soton.ac.uk>
 *
 * @param <DATA>
 */
public abstract class ConcreteTimeSeries<DATA> extends TimeSeries<DATA[],ConcreteTimeSeries<DATA>> {
	private TreeMap<Long, DATA> timeSeries;

	/**
	 * Initialise the backing treemap
	 */
	public ConcreteTimeSeries() {
		this.timeSeries = new TreeMap<Long, DATA>();
	}

	@Override
	public ConcreteTimeSeries<DATA> get(long time, int nbefore, int nafter) {
		LinkedList<DATA> dataList = new LinkedList<DATA>();
		LinkedList<Long> timeList = new LinkedList<Long>();
		addBefore(timeList,dataList, time, nbefore);
		addCurrent(timeList,dataList, time);
		addAfter(timeList,dataList, time, nafter);
		return this.newInstance(timeList,dataList);
	}
	
	private ConcreteTimeSeries<DATA> newInstance(LinkedList<Long> timeList,LinkedList<DATA> dataList) {
		ConcreteTimeSeries<DATA> instance = newInstance();
		Iterator<DATA> dataIter = dataList.iterator();
		Iterator<Long> timeIter = timeList.iterator();
		for (; dataIter.hasNext();) {
			instance.timeSeries.put(timeIter.next(), dataIter.next());
		}
		return instance;
	}

	@Override
	public ConcreteTimeSeries<DATA> get(long time, int nbefore, int nafter, DATA[] output) {
		LinkedList<DATA> dataList = new LinkedList<DATA>();
		LinkedList<Long> timeList = new LinkedList<Long>();
		addBefore(timeList,dataList , time, nbefore);
		addCurrent(timeList,dataList , time);
		addAfter(timeList,dataList , time, nafter);
		return newInstance(timeList, dataList);
	}

	@Override
	public ConcreteTimeSeries<DATA> get(long time, long threshbefore, long threshafter) {
		LinkedList<DATA> dataList = new LinkedList<DATA>();
		LinkedList<Long> timeList = new LinkedList<Long>();
		addBefore(timeList,dataList,time,threshbefore);
		addCurrent(timeList,dataList,time);
		addAfter(timeList,dataList,time,threshafter);
		return newInstance(timeList,dataList);
	}

	private void addAfter(LinkedList<Long> timeList,LinkedList<DATA> dataList, long time, long threshafter) {
		Entry<Long, DATA> ceilEntry = null;
		long maxTime = time + threshafter;
		long currentTime = time + 1; // Is this ok? The ceil should be either
										// time + 1
		while ((ceilEntry = this.timeSeries.ceilingEntry(currentTime)) != null) {
			currentTime = ceilEntry.getKey();
			if (currentTime <= maxTime) {
				dataList.addLast(ceilEntry.getValue());
				timeList.addLast(currentTime);
				currentTime++; // we have to nudge it forward, ceil doesn't find
								// the next one otherwise
			} else {
				break;
			}
		}// The entry was either null (didn't exist) or was beyond the threshold
		return;
	}

	private void addCurrent(LinkedList<Long> timeList, LinkedList<DATA> dataList, long time) {
		DATA entry = this.timeSeries.get(time);
		if (entry != null)
		{
			dataList.addLast(entry);
			timeList.addLast(time);
		}
	}

	private void addBefore(LinkedList<Long> timeList,LinkedList<DATA> dataList, long time, long threshbefore) {
		Entry<Long, DATA> floorEntry = null;
		long minTime = time - threshbefore;
		long currentTime = time;
		while ((floorEntry = this.timeSeries.lowerEntry(currentTime)) != null) {
			currentTime = floorEntry.getKey();
			if (currentTime >= minTime) {
				dataList.addFirst(floorEntry.getValue());
				timeList.addFirst(currentTime);
			} else {
				break;
			}
		}// The entry was either null (didn't exist) or was beyond the threshold
		return;
	}

	private void addAfter(LinkedList<Long> timeList, LinkedList<DATA> dataList, long time, int nafter) {
		Entry<Long, DATA> ceilEntry = null;
		int seen = 0;
		long currentTime = time;
		while ((ceilEntry = this.timeSeries.higherEntry(currentTime)) != null) {
			currentTime = ceilEntry.getKey();
			if (seen < nafter) {
				dataList.addLast(ceilEntry.getValue());
				timeList.addLast(currentTime);
				seen++;
			} else {
				break;
			}
		}// The entry was either null (didn't exist) or was beyond the threshold
		return;
	}

	private void addBefore(LinkedList<Long> timeList, LinkedList<DATA> dataList, long time, int nbefore) {
		Entry<Long, DATA> floorEntry = null;
		int seen = 0;
		long currentTime = time;
		while ((floorEntry = this.timeSeries.lowerEntry(currentTime)) != null) {
			currentTime = floorEntry.getKey();
			if (seen < nbefore) {
				dataList.addFirst(floorEntry.getValue());
				timeList.addFirst(currentTime);
				seen++;
			} else {
				break;
			}
		}// The entry was either null (didn't exist) or was beyond the threshold
		return;
	}

	/**
	 * Add an element to this time series
	 * @param time
	 * @param value
	 */
	public void add(long time, DATA value) {
		this.timeSeries.put(time, value);
	}
	
	@Override
	public void set(long[] time, DATA[] data) throws TimeSeriesSetException {
		if(time.length != data.length) throw new TimeSeriesSetException();
		for (int i = 0; i < time.length; i++) {
			this.timeSeries.put(time[i], data[i]);
		}
	};
	
	@Override
	public long[] getTimes() {
		Set<Long> timeSet = this.timeSeries.keySet();
		long[] times = new long[timeSet.size()];
		int i = 0;
		for (long time : timeSet) {
			times[i++] = time;
		}
		return times;
	}
	
	@Override
	public DATA[] getData() {
		Collection<DATA> dataSet = this.timeSeries.values();
		return dataSet.toArray(this.constructData(dataSet.size()));
	}

	@SuppressWarnings("unchecked")
	private DATA[] constructData(int size) {
		Class<?> a = ReflectionUtils.getTypeArguments(ConcreteTimeSeries.class, this.getClass()).get(0);
		return (DATA[]) Array.newInstance(a, size);
	}
}


