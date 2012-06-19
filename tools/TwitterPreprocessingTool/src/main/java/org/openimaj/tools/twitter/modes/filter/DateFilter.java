package org.openimaj.tools.twitter.modes.filter;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.arabidopsis.ahocorasick.AhoCorasick;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.kohsuke.args4j.Option;
import org.openimaj.twitter.TwitterStatus;

/**
 * The grep functionality. Should only be used as a post filter most of the time
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>, Sina Samangooei <ss@ecs.soton.ac.uk>
 *
 */
public class DateFilter extends TwitterPreprocessingFilter {
	
	@Option(name="--date-start", aliases="-from", required=false, usage="The start date", metaVar="STRING", multiValued=true)
	String startDateStr;
	DateTime startDate;
	@Option(name="--end-start", aliases="-to", required=false, usage="The start date", metaVar="STRING", multiValued=true)
	String endDateStr;
	DateTime endDate;
	
	
	@Override
	public boolean filter(TwitterStatus twitterStatus) {
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
		
		if(startDate!=null && date.isBefore(startDate)) {
			System.out.println(date + " is before " + startDate);
			return false;
		}
		if(endDate!=null && date.isAfter(endDate)) {
			System.out.println(date + " is after " + endDate);
			return false;
		}
		return true;
	}
	
	@Override
	public void validate() {
		if(startDateStr != null){
			startDate = DateTimeFormat.forPattern("Y/M/d").parseDateTime(startDateStr);
		}
		
		if(endDateStr != null){
			endDate = DateTimeFormat.forPattern("Y/M/d").parseDateTime(endDateStr);
		}
	}

}
