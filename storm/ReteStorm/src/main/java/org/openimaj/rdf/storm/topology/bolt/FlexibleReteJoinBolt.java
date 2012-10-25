package org.openimaj.rdf.storm.topology.bolt;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.openimaj.rdf.storm.bolt.RETEStormNode;
import org.openimaj.rdf.storm.bolt.RETEStormQueue;
import scala.actors.threadpool.Arrays;

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
public class FlexibleReteJoinBolt extends FlexibleReteBolt{


	protected final static Logger logger = Logger.getLogger(FlexibleReteJoinBolt.class);
	/**
	 *
	 */
	private static final long serialVersionUID = -2927726523603853768L;
	private String leftBolt;
	private String rightBolt;
	private RETEStormQueue leftQ;
	private RETEStormQueue rightQ;

	/**
	 * @param leftBolt
	 * @param rightBolt
	 * @param rule 
	 */
	public FlexibleReteJoinBolt(String leftBolt, String rightBolt, Rule rule) {
		super(rule);
		this.leftBolt = leftBolt;
		this.rightBolt = rightBolt;
	}
	
	/**
	 * @return the Fields this bolt joins on.
	 */
	public Fields getJoinFields(){
		return new Fields(Arrays.asList(FlexibleReteBolt.extractJoinFields(Arrays.asList(this.getRule().getBody()))));
	}

	@Override
	public void execute(Tuple input) {
		if(input.getSourceComponent().equals(leftBolt)){
			this.leftQ.fire(input, true);
		}
		else{
			this.rightQ.fire(input, true);
		}
		emit(input);
		acknowledge(input);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void prepare() {
		Fields joinFields = new Fields( Arrays.asList( extractJoinFields( Arrays.asList( this.getRule().getBody() ) ) ) );
		Fields outFields = new Fields( Arrays.asList( this.getVars() ) );
		this.leftQ = new RETEStormQueue(joinFields,outFields,5000,15,TimeUnit.MINUTES);
		this.rightQ = new RETEStormQueue(joinFields,outFields,5000,15,TimeUnit.MINUTES,this.leftQ,this);
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
