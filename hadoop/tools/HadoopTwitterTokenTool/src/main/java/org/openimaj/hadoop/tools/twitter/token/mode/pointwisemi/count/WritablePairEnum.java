package org.openimaj.hadoop.tools.twitter.token.mode.pointwisemi.count;

import org.apache.hadoop.mapreduce.Counters;
import org.openimaj.hadoop.tools.twitter.token.mode.WritableEnumCounter;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class WritablePairEnum extends WritableEnumCounter<PairEnum>{
	
	public WritablePairEnum() {
		// TODO Auto-generated constructor stub
	}
	/**
	 * @param counters
	 * @param values
	 */
	public WritablePairEnum(Counters counters, PairEnum[] values) {
		super(counters,values);
	}

	@Override
	public PairEnum valueOf(String str) {
		return PairEnum.valueOf(str);
	}
}