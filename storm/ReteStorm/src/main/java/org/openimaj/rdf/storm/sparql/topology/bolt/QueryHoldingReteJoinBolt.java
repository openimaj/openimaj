package org.openimaj.rdf.storm.sparql.topology.bolt;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.openimaj.rdf.storm.sparql.topology.builder.datasets.StaticRDFDataset;
import org.openimaj.rdf.storm.topology.bolt.StormReteJoinBolt;

import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.reasoner.rulesys.Rule;

/**
 * @author Jon Hare (jsh2@ecs.soton.ac.uk), Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class QueryHoldingReteJoinBolt extends StormReteJoinBolt {

	/**
	 *
	 */
	private static final long serialVersionUID = -6191193233252490210L;

	/**
	 * @param leftBolt
	 * @param matchLeft
	 * @param templateLeft
	 * @param rightBolt
	 * @param matchRight
	 * @param templateRight
	 * @param rule
	 */
	public QueryHoldingReteJoinBolt(
			String leftBolt, int[] matchLeft, int[] templateLeft,
			String rightBolt, int[] matchRight, int[] templateRight,
			Rule rule) {
		super(leftBolt, matchLeft, templateLeft, rightBolt, matchRight, templateRight, rule);
	}
	
	/**
	 * @param leftBolt
	 * @param matchLeft
	 * @param templateLeft
	 * @param rightBolt
	 * @param matchRight
	 * @param templateRight
	 * @param rule
	 */
	public QueryHoldingReteJoinBolt(
			String leftBolt, int[] matchLeft, int[] templateLeft, int capacityLeft,
			String rightBolt, int[] matchRight, int[] templateRight, int capacityRight,
			Rule rule) {
		super(leftBolt, matchLeft, templateLeft, capacityLeft, rightBolt, matchRight, templateRight, capacityRight, rule);
	}
	
	/**
	 * @param leftBolt
	 * @param matchLeft
	 * @param templateLeft
	 * @param rightBolt
	 * @param matchRight
	 * @param templateRight
	 * @param rule
	 */
	public QueryHoldingReteJoinBolt(
			String leftBolt, int[] matchLeft, int[] templateLeft, long rangeLeft, TimeUnit unitLeft,
			String rightBolt, int[] matchRight, int[] templateRight, long rangeRight, TimeUnit unitRight,
			Rule rule) {
		super(leftBolt, matchLeft, templateLeft, rangeLeft, unitLeft, rightBolt, matchRight, templateRight, rangeRight, unitRight, rule);
	}
	
	/**
	 * @param leftBolt
	 * @param matchLeft
	 * @param templateLeft
	 * @param rightBolt
	 * @param matchRight
	 * @param templateRight
	 * @param rule
	 */
	public QueryHoldingReteJoinBolt(
			String leftBolt, int[] matchLeft, int[] templateLeft,
			String rightBolt, int[] matchRight, int[] templateRight, int capacityRight, long rangeRight, TimeUnit unitRight,
			Rule rule) {
		super(leftBolt, matchLeft, templateLeft, rightBolt, matchRight, templateRight, capacityRight, rangeRight, unitRight, rule);
	}
	
	/**
	 * @param leftBolt
	 * @param matchLeft
	 * @param templateLeft
	 * @param rightBolt
	 * @param matchRight
	 * @param templateRight
	 * @param rule
	 */
	public QueryHoldingReteJoinBolt(
			String leftBolt, int[] matchLeft, int[] templateLeft, long rangeLeft, TimeUnit unitLeft,
			String rightBolt, int[] matchRight, int[] templateRight, int capacityRight, long rangeRight, TimeUnit unitRight,
			Rule rule) {
		super(leftBolt, matchLeft, templateLeft, rangeLeft, unitLeft, rightBolt, matchRight, templateRight, capacityRight, rangeRight, unitRight, rule);
	}
	
	/**
	 * @param leftBolt
	 * @param matchLeft
	 * @param templateLeft
	 * @param rightBolt
	 * @param matchRight
	 * @param templateRight
	 * @param rule
	 */
	public QueryHoldingReteJoinBolt(
			String leftBolt, int[] matchLeft, int[] templateLeft, int capacityLeft,
			String rightBolt, int[] matchRight, int[] templateRight, int capacityRight, long rangeRight, TimeUnit unitRight,
			Rule rule) {
		super(leftBolt, matchLeft, templateLeft, capacityLeft, rightBolt, matchRight, templateRight, capacityRight, rangeRight, unitRight, rule);
	}
	
	/**
	 * @param leftBolt
	 * @param matchLeft
	 * @param templateLeft
	 * @param rightBolt
	 * @param matchRight
	 * @param templateRight
	 * @param rule
	 */
	public QueryHoldingReteJoinBolt(
			String leftBolt, int[] matchLeft, int[] templateLeft, int capacityLeft, long rangeLeft, TimeUnit unitLeft,
			String rightBolt, int[] matchRight, int[] templateRight,
			Rule rule) {
		super(leftBolt, matchLeft, templateLeft, capacityLeft, rangeLeft, unitLeft, rightBolt, matchRight, templateRight, rule);
	}
	
	/**
	 * @param leftBolt
	 * @param matchLeft
	 * @param templateLeft
	 * @param rightBolt
	 * @param matchRight
	 * @param templateRight
	 * @param rule
	 */
	public QueryHoldingReteJoinBolt(
			String leftBolt, int[] matchLeft, int[] templateLeft, int capacityLeft, long rangeLeft, TimeUnit unitLeft,
			String rightBolt, int[] matchRight, int[] templateRight, long rangeRight, TimeUnit unitRight,
			Rule rule) {
		super(leftBolt, matchLeft, templateLeft, capacityLeft, rangeLeft, unitLeft, rightBolt, matchRight, templateRight, rangeRight, unitRight, rule);
	}
	
	/**
	 * @param leftBolt
	 * @param matchLeft
	 * @param templateLeft
	 * @param rightBolt
	 * @param matchRight
	 * @param templateRight
	 * @param rule
	 */
	public QueryHoldingReteJoinBolt(
			String leftBolt, int[] matchLeft, int[] templateLeft, int capacityLeft, long rangeLeft, TimeUnit unitLeft,
			String rightBolt, int[] matchRight, int[] templateRight, int capacityRight,
			Rule rule) {
		super(leftBolt, matchLeft, templateLeft, capacityLeft, rangeLeft, unitLeft, rightBolt, matchRight, templateRight, capacityRight, rule);
	}
	
	/**
	 * @param leftBolt
	 * @param matchLeft
	 * @param templateLeft
	 * @param rightBolt
	 * @param matchRight
	 * @param templateRight
	 * @param rule
	 */
	public QueryHoldingReteJoinBolt(
			String leftBolt, int[] matchLeft, int[] templateLeft, int capacityLeft, long rangeLeft, TimeUnit unitLeft,
			String rightBolt, int[] matchRight, int[] templateRight, int capacityRight, long rangeRight, TimeUnit unitRight,
			Rule rule) {
		super(leftBolt, matchLeft, templateLeft, capacityLeft, rangeLeft, unitLeft, rightBolt, matchRight, templateRight, capacityRight, rangeRight, unitRight, rule);
	}

	/**
	 * @param leftQueryString
	 * @param rightQueryString
	 *
	 */
	public void setQueryString(String leftQueryString, String rightQueryString) {
		this.leftQueryString = leftQueryString;
		this.rightQueryString = rightQueryString;
	}

	private String leftQueryString;
	private String rightQueryString;
	private List<StaticRDFDataset> dataSources;

	@Override
	public void prepare() {
		// init all datasets
		for (StaticRDFDataset ds : this.dataSources) {
			ds.prepare();
		}
		StaticDataRETEStormQueue staticQLeft = new StaticDataRETEStormQueue(this.leftBolt, this.matchLeft, this.templateLeft, this.capacityLeft, this.rangeLeft, this.unitLeft, this.collector, QueryFactory.create(leftQueryString));
		staticQLeft.staticDatasets = this.dataSources;
		this.leftQ = staticQLeft;
		StaticDataRETEStormQueue staticQRight = new StaticDataRETEStormQueue(this.rightBolt, this.matchRight, this.templateRight, this.capacityRight, this.rangeRight, this.unitRight, (StaticDataRETEStormQueue) this.leftQ, this, this.collector, QueryFactory.create(rightQueryString));
		staticQRight.staticDatasets = this.dataSources;
		this.rightQ = staticQRight;
	}

	public void setStaticDataSources(List<StaticRDFDataset> staticDataSources) {
		this.dataSources = staticDataSources;
	}
}
