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
		StaticDataRETEStormQueue staticQLeft = new StaticDataRETEStormQueue(this.matchLeft, this.templateLeft, 5000, 15, TimeUnit.MINUTES, QueryFactory.create(leftQueryString));
		staticQLeft.staticDatasets = this.dataSources;
		this.leftQ = staticQLeft;
		StaticDataRETEStormQueue staticQRight = new StaticDataRETEStormQueue(this.matchRight, this.templateRight, 5000, 15, TimeUnit.MINUTES, (StaticDataRETEStormQueue) this.leftQ, this, QueryFactory.create(rightQueryString));
		staticQRight.staticDatasets = this.dataSources;
		this.rightQ = staticQRight;
	}

	public void setStaticDataSources(List<StaticRDFDataset> staticDataSources) {
		this.dataSources = staticDataSources;
	}
}
