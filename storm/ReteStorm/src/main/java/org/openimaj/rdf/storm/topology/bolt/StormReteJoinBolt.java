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
public class StormReteJoinBolt extends StormReteBolt{


	protected final static Logger logger = Logger.getLogger(StormReteJoinBolt.class);
	/**
	 *
	 */
	private static final long serialVersionUID = -2927726523603853768L;
	private String leftBolt;
	private String rightBolt;
	private int[] matchLeft;
	private int[] matchRight;
	private int[] templateLeft;
	private int[] templateRight;
	private RETEStormQueue leftQ;
	private RETEStormQueue rightQ;

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
		super(rule);
		this.leftBolt = leftBolt;
		this.matchLeft = matchLeft;
		this.templateLeft = templateLeft;
		this.rightBolt = rightBolt;
		this.matchRight = matchRight;
		this.templateRight = templateRight;
	}
	
	/**
	 * @return the Fields output from the left bolt that this bolt joins on.
	 */
	public Fields getLeftJoinFields(){
		// extract the fields from the left bolt that this bolt joins on from the
		// left match indices (the indices of the array, rather than the values).
		List<String> fields = new ArrayList<String>();
		for (int i = 0; i < matchLeft.length; i++)
			if (matchLeft[i] >= 0)
				fields.add("?"+i);
		return new Fields(fields);
	}
	
	/**
	 * @return the Fields output from the right bolt that this bolt joins on.
	 */
	public Fields getRightJoinFields(){
		// extract the fields from the right bolt that this bolt joins to from the
		// left match indices (the values of the array, rather than the indices).
		List<String> fields = new ArrayList<String>();
		for (int i = 0; i < matchLeft.length; i++)
			if (matchLeft[i] >= 0)
				fields.add("?"+matchLeft[i]);
		return new Fields(fields);
	}

	@Override
	public void execute(Tuple input) {
		boolean isAdd = (Boolean) input.getValueByField(StormReteBolt.BASE_FIELDS[StormReteBolt.IS_ADD]);
		if(input.getSourceComponent().equals(leftBolt)){
			this.leftQ.fire(input, isAdd);
		}
		else{
			this.rightQ.fire(input, isAdd);
		}
		emit(input);
		acknowledge(input);
	}

	@Override
	public void prepare() {
		this.leftQ = new RETEStormQueue(this.matchLeft,this.templateLeft,5000,15,TimeUnit.MINUTES);
		this.rightQ = new RETEStormQueue(this.matchRight,this.templateRight,5000,15,TimeUnit.MINUTES,this.leftQ,this);
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
	public RETEStormNode clone(Map<RETEStormNode, RETEStormNode> netCopy,
			RETERuleContext context) {
		// TODO Auto-generated method stub
		return null;
	}

}
