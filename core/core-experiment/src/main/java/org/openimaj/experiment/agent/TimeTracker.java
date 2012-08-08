package org.openimaj.experiment.agent;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.math.stat.descriptive.SummaryStatistics;
import org.openimaj.time.NanoTimer;
import org.openimaj.time.Timer;

import com.bethecoder.ascii_table.ASCIITable;

/**
 * A class for tracking various execution times and generating
 * statistics.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class TimeTracker {
	private static Map<String, SummaryStatistics> times = new HashMap<String, SummaryStatistics>();
	
	public static synchronized void accumulate(String identifier, NanoTimer timer) {
		accumulate(identifier, timer.duration());
	}
	
	public static synchronized void accumulate(String identifier, Timer timer) {
		accumulate(identifier, (long)(timer.duration() * 1e6));
	}
	
	public static synchronized void accumulate(String identifier, long nanoTime) {
		SummaryStatistics t = times.get(identifier);
		
		if (t == null) times.put(identifier, t = new SummaryStatistics());
		
		t.addValue(nanoTime);
	}
	
	public static synchronized Map<String, SummaryStatistics> reset() {
		Map<String, SummaryStatistics> oldTimes = times;
		times = new HashMap<String, SummaryStatistics>();
		return oldTimes;
	}
	
	public static synchronized Map<String, SummaryStatistics> getTimes() {
		HashMap<String, SummaryStatistics> ret = new HashMap<String, SummaryStatistics>();
		
		for (Entry<String, SummaryStatistics> e : times.entrySet()) {
			ret.put(e.getKey(), e.getValue().copy());
		}
		
		return ret;
	}

	public static void addMissing(Map<String, SummaryStatistics> timesToAdd) {
		for (Entry<String, SummaryStatistics> e : timesToAdd.entrySet()) {
			if (!times.containsKey(e.getValue()))
				times.put(e.getKey(), e.getValue());
		}
	}
	
	public static String format(Map<String, SummaryStatistics> times) {
		String [] header = {"Timer Identifier", "Recorded Time"};
		String [][] data = new String[times.size()][];
		
		int i = 0;
		for (Entry<String, SummaryStatistics> e : times.entrySet()) {
			data[i++] = new String [] { e.getKey(), format(e.getValue()) };
		}
		
		return ASCIITable.getInstance().getTable(header, data);
	}
	
	public static String format(SummaryStatistics ss) {
		if (ss.getN() == 1) {
			return formatTime(ss.getMean());
		} 
		return formatTime(ss.getMean(), ss.getStandardDeviation());
	}

	private static String formatTime(double time) {
		long ns = (long) time;
		
		if (ns < 1e3) {
			return time + "ns";
		}
		if (ns < 1e6) {
			return (time / 1e3) + "us";
		}
		if (ns < 1e9) {
			return (time / 1e6) + "ms";
		}
		
		double secs = (time / 1e9);
		if (secs < 60) {
			return String.format("%2.5ss", secs);
		}
		
		long mins = (long)(secs / 60);
		double rsecs = secs - (mins * 60);
		if (mins < 60) {
			return mins + "m" + String.format("%2.5ss", rsecs);
		}
	
		long hrs = (long)(mins / 60);
		long rmins = mins - (hrs * 60);
		if (hrs < 24) { 
			return hrs + "h" + rmins + "m" + String.format("%2.5ss", rsecs);
		}
		
		long days = (long)(hrs / 24);
		long rhrs = days - (hrs * 24);
		if (hrs < 24) { 
			return days + "d" + rhrs + "h" + rmins + "m" + String.format("%2.5ss", rsecs);
		}
	
		return time+"ns";
	}

	private static String formatTime(double mean, double standardDeviation) {
		return formatTime(mean) + " (SD = " +formatTime(standardDeviation) + ")";
	}
}
