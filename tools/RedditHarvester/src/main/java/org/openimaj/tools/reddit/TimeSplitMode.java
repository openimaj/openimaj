package org.openimaj.tools.reddit;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.Period;
import org.kohsuke.args4j.Option;
import org.openimaj.twitter.collection.StreamJSONStatusList.ReadableWritableJSON;
import org.openimaj.util.pair.IndependentPair;
import org.openimaj.util.pair.LongLongPair;

/**
 * The time split attempts to read times from the json items provided and splits
 * by WEEK,DAY,HOUR or MINUTE 
 * @author Jon Hare (jsh2@ecs.soton.ac.uk), Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class TimeSplitMode extends SplitMode {
	private static final String outputFormat = "%s.%s.s%s.e%s.json";
	
	enum TimeSplitPeriod{
		WEEK {
			@Override
			public LongLongPair startEnd(long time) {
				DateTime dt = new DateTime(time);
				DateTime start = dt.withDayOfWeek(DateTimeConstants.MONDAY);
				DateTime end = dt.plus(weekPeriod).withDayOfWeek(DateTimeConstants.MONDAY);
				
				return LongLongPair.pair(start.getMillis(), end.getMillis());
			}
		},DAY {
			@Override
			public LongLongPair startEnd(long time) {
				DateTime dt = new DateTime(time);
				DateTime start = dt.withHourOfDay(0);
				DateTime end = dt.plus(dayPeriod).withHourOfDay(0);
				
				return LongLongPair.pair(start.getMillis(), end.getMillis());
			}
		},HOUR {
			@Override
			public LongLongPair startEnd(long time) {
				DateTime dt = new DateTime(time);
				DateTime start = dt.withMinuteOfHour(0);
				DateTime end = dt.plus(hourPeriod).withMinuteOfHour(0);
				
				return LongLongPair.pair(start.getMillis(), end.getMillis());
			}
		},MINUTE {
			@Override
			public LongLongPair startEnd(long time) {
				DateTime dt = new DateTime(time);
				DateTime start = dt.withSecondOfMinute(0);
				DateTime end = dt.plus(minutePeriod).withSecondOfMinute(0);
				
				return LongLongPair.pair(start.getMillis(), end.getMillis());
			}
		};
		Period weekPeriod = new Period(0, 0, 1, 0, 0, 0, 0, 0);
		Period dayPeriod = new Period(0, 0, 0, 1, 0, 0, 0, 0);
		Period hourPeriod = new Period(0, 0, 0, 0, 1, 0, 0, 0);
		Period minutePeriod = new Period(0, 0, 0, 0, 0, 1, 0, 0);
		public abstract LongLongPair startEnd(long time);
	}
	
	@Option(name="--split-period", aliases="-sp", required=false, usage="the file split period.")
	protected TimeSplitPeriod period = TimeSplitPeriod.MINUTE;
	
	@Override
	public void output(List<ReadableWritableJSON> read) {
//		Map<String,List<ReadableWritableJSON>> fileOutputs = new HashMap<String, List<ReadableWritableJSON>>();
//		for (ReadableWritableJSON readableWritableJSON : read) {
//			@SuppressWarnings("unchecked")
//			List<Map<String,Object>> items = (List<Map<String, Object>>) ((Map<String,Object>)readableWritableJSON.get("data")).get("children");
//			
//		}
//		return null;
	}

}
