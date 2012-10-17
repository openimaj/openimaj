/**
 * Copyright (c) 2012, The University of Southampton and the individual contributors.
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
import java.util.Map;

import org.apache.log4j.Logger;

import backtype.storm.tuple.Tuple;

import com.hp.hpl.jena.reasoner.rulesys.impl.BindingVector;
import com.hp.hpl.jena.reasoner.rulesys.impl.RETENode;
import com.hp.hpl.jena.reasoner.rulesys.impl.RETEQueue;
import com.hp.hpl.jena.reasoner.rulesys.impl.RETERuleContext;

/**
 * Given the two parent bolt names, this bolt constructs two {@link RETEQueue}
 * instances. These instances are fed the output from the bolts as they arrive
 * and if a join satisfied their output is passed on.
 *
 * The internally held queues are where windows should be implemented
 *
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class ReteJoinBolt extends ReteBolt{


	protected final static Logger logger = Logger.getLogger(ReteJoinBolt.class);
	/**
	 *
	 */
	private static final long serialVersionUID = -2927726523603853768L;
	private String leftBolt;
	private String rightBolt;
	private ArrayList<Byte> matchIndices;
	private RETEQueue leftQ;
	private RETEQueue rightQ;
	private BindingVector toFire;

	/**
	 * @param leftBolt
	 * @param rightBolt
	 * @param matchIndices the variable indecies to watch out for
	 */
	public ReteJoinBolt(String leftBolt, String rightBolt, ArrayList<Byte> matchIndices) {
		this.leftBolt = leftBolt;
		this.rightBolt = rightBolt;
		this.matchIndices = matchIndices;

	}

	@Override
	public RETENode clone(@SuppressWarnings("rawtypes") Map netCopy, RETERuleContext context) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void fire(BindingVector env, boolean isAdd) {
		this.toFire = env;
	}

	@Override
	public void execute(Tuple input) {
		
		BindingVector env = extractBindings(input);
		this.toFire = null;
		if(input.getSourceComponent().equals(leftBolt)){
			this.leftQ.fire(env, true);
		}
		else{
			this.rightQ.fire(env, true);
		}
		if(this.toFire == null){
			collector.ack(input);
			return;
		}
		this.emitBinding(input, toFire);
		toFire = null;
		collector.ack(input);
	}

	@Override
	public void prepare() {
		this.leftQ = new RETEQueue(matchIndices);
		this.rightQ = new RETEQueue(matchIndices);
        leftQ.setSibling(rightQ);
        rightQ.setSibling(leftQ);
        leftQ.setContinuation(this);
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

}
