package org.openimaj.tools.twitter.modes.filter;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.arabidopsis.ahocorasick.AhoCorasick;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.kohsuke.args4j.Option;
import org.openimaj.twitter.USMFStatus;

/**
 * The grep functionality. Should only be used as a post filter most of the time
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class DateFilter extends TwitterPreprocessingFilter {
	
	@Option(name="--date-start", aliases="-from", required=false, usage="The start date", metaVar="STRING", multiValued=true)
	String startDateStr;
	DateTime startDate;
	@Option(name="--end-start", aliases="-to", required=false, usage="The start date", metaVar="STRING")
	String endDateStr;
	DateTime endDate;
	@Option(name="--date-range", aliases="-drng", required=false, usage="Comma delimited start,end date range", metaVar="STRING", multiValued=true)
	List<String> dateRanges = new ArrayList<String>();
	List<Interval> intervals = new ArrayList<Interval>();
	
	
	
	
	@Override
	public boolean filter(USMFStatus twitterStatus) {
		DateTime date;
		
		try {
			date = twitterStatus.createdAt();
		} catch (ParseException e) {
			System.out.println("Failed to parse: " + twitterStatus);
			return false;
		}
		
		if(date == null) {
			System.out.println("no date for: " + twitterStatus);
			return false;
		}
		// valid date, is it after the start and before the end?
		
		if(startDate!=null && date.isBefore(startDate)) {
			System.out.println(date + " is before " + startDate);
			return false;
		}
		if(endDate!=null && date.isAfter(endDate)) {
			System.out.println(date + " is after " + endDate);
			return false;
		}
		// We are both after the start and after the end, but are we within one of the intervals?
		boolean match = this.intervals.size() == 0;
		for (Interval  interval : this.intervals) {
			match = interval.contains(date);
			if(match) return match; //it is inside one of the intervals
		}
		
		return match;
	}
	
	@Override
	public void validate() {
		if(startDateStr != null){
			startDate = DateTimeFormat.forPattern("Y/M/d").parseDateTime(startDateStr);
		}
		
		if(endDateStr != null){
			endDate = DateTimeFormat.forPattern("Y/M/d").parseDateTime(endDateStr);
		}
		
		for (String dateRange : this.dateRanges) {
			String[] dRangeSplit = dateRange.split(",");
			if(dRangeSplit.length!=2){
				continue;
			}
			DateTime start = DateTimeFormat.forPattern("Y/M/d").parseDateTime(dRangeSplit[0]);
			DateTime end = DateTimeFormat.forPattern("Y/M/d").parseDateTime(dRangeSplit[1]);
			this.intervals.add(new Interval(start, end));
			
		}
	}

}
