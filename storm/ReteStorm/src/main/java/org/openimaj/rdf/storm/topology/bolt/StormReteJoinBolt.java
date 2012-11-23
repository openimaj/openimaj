/**
 * Copyright (c) ${year}, The University of Southampton and the individual contributors.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *   * 	Redistributions of source code must retain the above copyright notice,
 * 	this list of conditions and the following disclaimer.
 *
 *   *	Redistributions in binary form must reproduce the above copyright notice,
 * 	this list of conditions and the following disclaimer in the documentation
 * 	and/or other materials provided with the distribution.
 *
 *   *	Neither the name of the University of Southampton nor the names of its
 * 	contributors may be used to endorse or promote products derived from this
 * 	software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.openimaj.rdf.storm.topology.bolt;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.openimaj.rdf.storm.bolt.RETEStormNode;
import org.openimaj.rdf.storm.bolt.RETEStormQueue;

import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import com.hp.hpl.jena.reasoner.rulesys.Rule;
import com.hp.hpl.jena.reasoner.rulesys.impl.RETEQueue;
import com.hp.hpl.jena.reasoner.rulesys.impl.RETERuleContext;

/**
 * Given the two parent bolt names, this bolt constructs two {@link RETEQueue}
 * instances. These instances are fed the output from the bolts as they arrive
 * and if a join satisfied their output is passed on.
 *
 * The internally held queues are where windows should be implemented
 *
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 *
 */
public class StormReteJoinBolt extends StormRuleReteBolt {

	protected final static Logger logger = Logger.getLogger(StormReteJoinBolt.class);
	/**
	 *
	 */
	private static final long serialVersionUID = -2927726523603853768L;
	private static final int DEFAULT_CAPACITY = 5000;
	private static final long DEFAULT_RANGE = 15;
	private static final TimeUnit DEFAULT_UNIT = TimeUnit.MINUTES;
	protected String leftBolt;
	protected String rightBolt;
	protected int[] matchLeft;
	protected int[] matchRight;
	protected int[] templateLeft;
	protected int[] templateRight;
	protected int capacityLeft;
	protected int capacityRight;
	protected long rangeLeft;
	protected long rangeRight;
	protected TimeUnit unitLeft;
	protected TimeUnit unitRight;
	protected RETEStormQueue leftQ;
	protected RETEStormQueue rightQ;
	protected Tuple currentInput;

	/**
	 *
	 * @param leftBolt
	 * @param matchLeft
	 * @param templateLeft
	 * @param rightBolt
	 * @param matchRight
	 * @param templateRight
	 * @param rule
	 */
	public StormReteJoinBolt(String leftBolt,
			int[] matchLeft,
			int[] templateLeft,
			String rightBolt,
			int[] matchRight,
			int[] templateRight,
			Rule rule) {
		this(leftBolt,matchLeft,templateLeft,
			 5000,15,TimeUnit.MINUTES,
			 rightBolt,matchRight,templateRight,
			 5000,15,TimeUnit.MINUTES,
			 rule);
	}
	
	/**
	 *
	 * @param leftBolt
	 * @param matchLeft
	 * @param templateLeft
	 * @param capacityLeft
	 * @param rightBolt
	 * @param matchRight
	 * @param templateRight
	 * @param capacityRight
	 * @param rule
	 */
	public StormReteJoinBolt(String leftBolt,
			int[] matchLeft,
			int[] templateLeft,
			int capacityLeft,
			String rightBolt,
			int[] matchRight,
			int[] templateRight,
			int capacityRight,
			Rule rule) {
		this(leftBolt,matchLeft,templateLeft,
			 capacityLeft,DEFAULT_RANGE,DEFAULT_UNIT,
			 rightBolt,matchRight,templateRight,
			 capacityRight,DEFAULT_RANGE,DEFAULT_UNIT,
			 rule);
	}
	
	/**
	 *
	 * @param leftBolt
	 * @param matchLeft
	 * @param templateLeft
	 * @param rangeLeft
	 * @param unitLeft
	 * @param rightBolt
	 * @param matchRight
	 * @param templateRight
	 * @param rangeRight
	 * @param unitRight
	 * @param rule
	 */
	public StormReteJoinBolt(String leftBolt,
			int[] matchLeft,
			int[] templateLeft,
			long rangeLeft,
			TimeUnit unitLeft,
			String rightBolt,
			int[] matchRight,
			int[] templateRight,
			long rangeRight,
			TimeUnit unitRight,
			Rule rule) {
		this(leftBolt,matchLeft,templateLeft,
			 DEFAULT_CAPACITY,rangeLeft,unitLeft,
			 rightBolt,matchRight,templateRight,
			 DEFAULT_CAPACITY,rangeRight,unitRight,
			 rule);
	}
	
	/**
	 *
	 * @param leftBolt
	 * @param matchLeft
	 * @param templateLeft
	 * @param capacityLeft
	 * @param rangeLeft
	 * @param unitLeft
	 * @param rightBolt
	 * @param matchRight
	 * @param templateRight
	 * @param capacityRight
	 * @param rangeRight
	 * @param unitRight
	 * @param rule
	 */
	public StormReteJoinBolt(String leftBolt,
							 int[] matchLeft,
							 int[] templateLeft,
							 int capacityLeft,
							 long rangeLeft,
							 TimeUnit unitLeft,
							 String rightBolt,
							 int[] matchRight,
							 int[] templateRight,
							 int capacityRight,
							 long rangeRight,
							 TimeUnit unitRight,
							 Rule rule){
		super(rule);
		this.leftBolt = leftBolt;
		this.matchLeft = matchLeft;
		this.templateLeft = templateLeft;
		this.capacityLeft = capacityLeft;
		this.rangeLeft = rangeLeft;
		this.unitLeft = unitLeft;
		this.rightBolt = rightBolt;
		this.matchRight = matchRight;
		this.templateRight = templateRight;
		this.capacityRight = capacityRight;
		this.rangeRight = rangeRight;
		this.unitRight = unitRight;
	}

	public StormReteJoinBolt(String leftBolt2, int[] matchLeft2,
			int[] templateLeft2, String rightBolt2, int[] matchRight2,
			int[] templateRight2, int capacityRight2, long rangeRight2,
			TimeUnit unitRight2, Rule rule) {
		this(leftBolt2, matchLeft2, templateLeft2,
			 DEFAULT_CAPACITY, DEFAULT_RANGE, DEFAULT_UNIT,
			 rightBolt2, matchRight2, templateRight2,
			 capacityRight2, rangeRight2, unitRight2,
			 rule);
	}

	public StormReteJoinBolt(String leftBolt2, int[] matchLeft2,
			int[] templateLeft2, long rangeLeft2, TimeUnit unitLeft2,
			String rightBolt2, int[] matchRight2, int[] templateRight2,
			int capacityRight2, long rangeRight2, TimeUnit unitRight2, Rule rule) {
		this(leftBolt2, matchLeft2, templateLeft2,
			 DEFAULT_CAPACITY, rangeLeft2, unitLeft2,
			 rightBolt2, matchRight2, templateRight2,
			 capacityRight2, rangeRight2, unitRight2,
			 rule);
	}

	public StormReteJoinBolt(String leftBolt2, int[] matchLeft2,
			int[] templateLeft2, int capacityLeft2, String rightBolt2,
			int[] matchRight2, int[] templateRight2, int capacityRight2,
			long rangeRight2, TimeUnit unitRight2, Rule rule) {
		this(leftBolt2, matchLeft2, templateLeft2,
			 capacityLeft2, DEFAULT_RANGE, DEFAULT_UNIT,
			 rightBolt2, matchRight2, templateRight2,
			 capacityRight2, rangeRight2, unitRight2,
			 rule);
	}

	public StormReteJoinBolt(String leftBolt2, int[] matchLeft2,
			int[] templateLeft2, int capacityLeft2, long rangeLeft2,
			TimeUnit unitLeft2, String rightBolt2, int[] matchRight2,
			int[] templateRight2, Rule rule) {
		this(leftBolt2, matchLeft2, templateLeft2,
			 capacityLeft2, rangeLeft2, unitLeft2,
			 rightBolt2, matchRight2, templateRight2,
			 DEFAULT_CAPACITY, DEFAULT_RANGE, DEFAULT_UNIT,
			 rule);
	}

	public StormReteJoinBolt(String leftBolt2, int[] matchLeft2,
			int[] templateLeft2, int capacityLeft2, long rangeLeft2,
			TimeUnit unitLeft2, String rightBolt2, int[] matchRight2,
			int[] templateRight2, long rangeRight2, TimeUnit unitRight2,
			Rule rule) {
		this(leftBolt2, matchLeft2, templateLeft2,
			 capacityLeft2, rangeLeft2, unitLeft2,
			 rightBolt2, matchRight2, templateRight2,
			 DEFAULT_CAPACITY, rangeRight2, unitRight2,
			 rule);
	}

	public StormReteJoinBolt(String leftBolt2, int[] matchLeft2,
			int[] templateLeft2, int capacityLeft2, long rangeLeft2,
			TimeUnit unitLeft2, String rightBolt2, int[] matchRight2,
			int[] templateRight2, int capacityRight2, Rule rule) {
		this(leftBolt2, matchLeft2, templateLeft2,
			 capacityLeft2, rangeLeft2, unitLeft2,
			 rightBolt2, matchRight2, templateRight2,
			 capacityRight2, DEFAULT_RANGE, DEFAULT_UNIT,
			 rule);
	}

	/**
	 * @return the Fields output from the left bolt that this bolt joins on.
	 */
	public Fields getLeftJoinFields() {
		return getJoinFieldsByIndex(matchLeft);
	}

	/**
	 * Given a set of match fields, return fields of the non-zero indexes
	 * of the match fields. Has the effect of saying which values in this
	 * bolt are to be used
	 *
	 * @param matchFields
	 * @return the fields in the form of "?index"
	 */
	public static Fields getJoinFieldsByIndex(int[] matchFields) {
		// extract the fields from the left bolt that this bolt joins on from the
		// left match indices (the indices of the array, rather than the values).
		List<String> fields = new ArrayList<String>();
		for (int i = 0; i < matchFields.length; i++)
			if (matchFields[i] >= 0)
				fields.add("?" + i);
		return new Fields(fields);
	}

	/**
	 * Given a set of match fields, return fields of the non-zero values
	 * of the match fields. Has the effect of saying which values in the sibling
	 * join are to be joined against
	 *
	 * @param matchFields
	 * @return the fields in the form of "?matchFields[index]"
	 */
	public static Fields getJoinFieldsByValue(int[] matchFields) {
		// extract the fields from the left bolt that this bolt joins on from the
		// left match indices (the indices of the array, rather than the values).
		List<String> fields = new ArrayList<String>();
		for (int i = 0; i < matchFields.length; i++)
			if (matchFields[i] >= 0)
				fields.add("?" + matchFields[i]);
		return new Fields(fields);
	}

	/**
	 * @return the Fields output from the right bolt that this bolt joins on.
	 */
	public Fields getRightJoinFields() {
		return getJoinFieldsByValue(matchLeft);
	}

	@Override
	public void execute(Tuple input) {
		logger.debug(String.format("\nExecuting join over: {\n\tleft = %s, \n\tright = %s \n} ", this.leftBolt, this.rightBolt));
		boolean isAdd = (Boolean) input.getValueByField(Component.isAdd.toString());
		long timestamp = (Long) input.getValueByField(Component.timestamp.toString());
		this.currentInput = input;
		if (input.getSourceComponent().equals(leftBolt)) {
			logger.debug(String.format("Source: LEFT QUEUE fired"));
			this.leftQ.fire(input, isAdd, timestamp);
		}
		else {
			logger.debug(String.format("Source: RIGHT QUEUE fired"));
			this.rightQ.fire(input, isAdd, timestamp);
		}

		acknowledge(input);
	}

	@Override
	public void prepare() {
		this.leftQ = new RETEStormQueue(this.leftBolt, this.matchLeft, this.templateLeft, this.capacityLeft, this.rangeLeft, this.unitLeft);
		this.rightQ = new RETEStormQueue(this.rightBolt, this.matchRight, this.templateRight, this.capacityRight, this.rangeRight, this.unitRight, this.leftQ, this);
	}

	/**
	 * @return the name of the left bolt of the join
	 */
	public String getLeftBolt() {
		return leftBolt;
	}

	/**
	 * @return the name of the right bolt of the join
	 */
	public String getRightBolt() {
		return rightBolt;
	}

	@Override
	public RETEStormNode clone(Map<RETEStormNode, RETEStormNode> netCopy, RETERuleContext context) {
		return null;
	}
	
	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		super.declareOutputFields(declarer);
		declarer.declareStream(leftBolt, declaredFields(matchLeft.length));
		declarer.declareStream(rightBolt, declaredFields(matchRight.length));
	}
	
}
