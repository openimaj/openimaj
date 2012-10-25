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
	 * @return the Fields this bolt joins on.
	 */
	@SuppressWarnings("unchecked")
	public Fields getJoinFields(){
		return new Fields( Arrays.asList(
								StormReteBolt.extractJoinFields(
										Arrays.asList( this.getRule().getBody() )
								)
						));
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
