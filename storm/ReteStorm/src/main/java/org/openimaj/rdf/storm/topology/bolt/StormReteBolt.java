package org.openimaj.rdf.storm.topology.bolt;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.openimaj.rdf.storm.bolt.RETEStormSinkNode;
import scala.actors.threadpool.Arrays;

import backtype.storm.spout.ISpout;
import backtype.storm.task.IBolt;
import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.reasoner.TriplePattern;
import com.hp.hpl.jena.reasoner.rulesys.ClauseEntry;
import com.hp.hpl.jena.reasoner.rulesys.Functor;
import com.hp.hpl.jena.reasoner.rulesys.Node_RuleVariable;
import com.hp.hpl.jena.reasoner.rulesys.Rule;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

/**
 * 
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 */
public abstract class StormReteBolt extends BaseRichBolt implements RETEStormSinkNode {

	/**
	 * These are the non-rule-bound fields that all Rete bolts output alongside the rule specific variable bindings.
	 */
	public static final String[] BASE_FIELDS = {"isAdd", "graph", "timestamp"};
	/**
	 * Access value for the isAdd base field.
	 */
	public static final int IS_ADD = 0;
	/**
	 * Access value for the graph base field.
	 */
	public static final int GRAPH = 1;
	/**
	 * Access value for the timestamp base field.
	 */
	public static final int TIMESTAMP = 3;
	
	private static final long serialVersionUID = -748651304134295713L;
	
	protected OutputCollector collector;
	protected TopologyContext context;
	@SuppressWarnings("rawtypes")
	protected Map stormConf;
	
	private final String ruleString;
	private final String[] outputFields;
	
	private List<ClauseEntry> outputTemplate;
	private Values toFire;
	private boolean active;
	
	private int[] usageStatistics = {0,0};
	private double[] costStatistics = {0,0};
	/**
	 * The constant value for accessing potential statistics.
	 */
	public static final int POTENTIAL = 0;
	/**
	 * The constant value for accessing implemented statistics.
	 */
	public static final int ACTUAL = 1;
	
	/**
	 * 
	 * @param rule
	 */
	@SuppressWarnings("unchecked")
	public StormReteBolt(final Rule rule){
		this.ruleString = rule.toString();
		this.outputTemplate = Arrays.asList(Rule.parseRule(this.ruleString).getHead());
		this.outputFields = extractFields(this.outputTemplate);
		this.outputTemplate = null;
	}
	
	// ******** getting/setting ********
	
	/**
	 * @param statIndex
	 * @throws ArrayIndexOutOfBoundsException
	 */
	public void incrementUsage (int statIndex) throws ArrayIndexOutOfBoundsException {
		this.usageStatistics[statIndex]++;
	}
	
	/**
	 * @param statIndex
	 * @throws ArrayIndexOutOfBoundsException
	 */
	public void decrementUsage (int statIndex) throws ArrayIndexOutOfBoundsException {
		this.usageStatistics[statIndex]--;
	}
	
	/**
	 * @param statIndex
	 * @return int
	 * @throws ArrayIndexOutOfBoundsException
	 */
	public int getUsage (int statIndex) throws ArrayIndexOutOfBoundsException {
		return this.usageStatistics[statIndex];
	}
	
	/**
	 * @param statIndex
	 * @return double
	 * @throws ArrayIndexOutOfBoundsException
	 */
	public double getCost (int statIndex) throws ArrayIndexOutOfBoundsException {
		return this.costStatistics[statIndex];
	}
	
	/**
	 * Get the names of the variable fields output from this Bolt.
	 * @return String[]
	 */
	public String[] getVars(){
		return this.outputFields;
	}
	
	/**
	 * Get the rule on which this FlexibleReteBolt is built.
	 * @return Rule
	 */
	public Rule getRule() {
		return Rule.parseRule(this.ruleString);
	}
	
	// ******** Preparation ********
	
	@SuppressWarnings("unchecked")
	@Override
	public void prepare(@SuppressWarnings("rawtypes") Map stormConf, TopologyContext context, OutputCollector collector) {
		this.collector = collector;
		this.context = context;
		this.stormConf = stormConf;
		
		this.outputTemplate = Arrays.asList(Rule.parseRule(this.ruleString).getHead());
		prepare();
		
		this.active = true;
	}
	
	/**
	 * Initialise the Bolt with complex fields only necessary during topology operation.
	 */
	public abstract void prepare();
	
	// ******** Node/Bolt Operation ********
	
	public void fire(Values output, boolean isAdd) {
		this.toFire = output;
	}
	
	/**
	 * Emit the {@link Values} instance that has been prepared for firing,
	 * using the provided {@link Tuple} as the anchor.
	 * @param anchor
	 */
	protected void emit(Tuple anchor){
		if (this.toFire != null){
			this.collector.emit(anchor,toFire);
		}
	}
	
	/**
	 * Acknowledge the input {@link Tuple} as per Storm requirements,
	 * then set the toFire variable to null as per Jena.
	 * @param input
	 */
	protected void acknowledge(Tuple input){
		this.collector.ack(input);
		this.toFire = null;
	}
	
	public boolean isActive() {
		return active;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		List<String> fields = new ArrayList<String>();
		fields.addAll(Arrays.asList(BASE_FIELDS));
		fields.addAll(Arrays.asList(outputFields));
		declarer.declare(new Fields(fields));
	}
	
	// ******** Value <-> Graph Conversion ********
	
	protected Values asValues (Graph graph) {
		return asValues(graph,outputTemplate);
	}
	
	/**
	 * Given a tuple generated from an Storm {@link ISpout} or {@link IBolt} using the same class of RETEStormTranslator, create a Jena
	 * {@link Graph} instance.
	 * 
	 * @param input
	 * @return List of one Jena {@link Triple} instance from the Tuple's fields
	 * @throws ClassCastException 
	 */
	public static Graph asGraph(Tuple input) throws ClassCastException {
		return (Graph) input.getValueByField(BASE_FIELDS[GRAPH]);
	}
	
	/**
	 * Given a Jena {@link Graph} construct a {@link Values} instance which is
	 * the subject, predicate and object of the triple calling
	 * {@link Node#toString()}
	 * 
	 * @param graph
	 * @param fieldsTemplate
	 * @param seen 
	 * @return a {@link Values} instance
	 */
	public static Values asValues(Graph graph, TriplePattern fieldsTemplate, List<Node> seen) {
		Values values = new Values();
		
		boolean varSub=false,varPred=false,varObj=false;
		if (fieldsTemplate.getSubject().isVariable() && !seen.contains(fieldsTemplate.getSubject())){
			varSub = true;
			seen.add(fieldsTemplate.getSubject());
		}
		if (fieldsTemplate.getPredicate().isVariable() && !seen.contains(fieldsTemplate.getPredicate())){
			varPred = true;
			seen.add(fieldsTemplate.getPredicate());
		}
		if (fieldsTemplate.getObject().isVariable() && !seen.contains(fieldsTemplate.getObject())){
			varObj = true;
			seen.add(fieldsTemplate.getObject());
		}
		
		ExtendedIterator<Triple> iter = graph.find(fieldsTemplate.asTripleMatch());
		while (iter.hasNext()){
			Triple t = iter.next();
			
			if (varSub)
				values.add(t.getSubject());
			if (varPred)
				values.add(t.getPredicate());
			if (varObj)
				values.add(t.getObject());
		}
		
		return values;
	}
	
	/**
	 * Given a Jena {@link Graph} construct a {@link Values} instance which is
	 * the subject, predicate and object of the triple calling
	 * {@link Node#toString()}
	 * @param graph
	 * @param fieldsTemplate
	 * @return a {@link Values} instance
	 */
	public static Values asValues(Graph graph, List<ClauseEntry> fieldsTemplate) {
		Values values = new Values();
		List<Node> seen = new ArrayList<Node>();
		for (ClauseEntry tp : fieldsTemplate){
			if (tp instanceof TriplePattern)
				values.addAll(asValues(graph,(TriplePattern)tp,seen));
		}
		values.add(graph);
		return values;
	}
	
	// ******** Field Extraction ********
	// Do not need to extract fields in bare functors or embedded rules,
	// as these lead to system calls rather than graph emission.
	
	protected String[] extractFields () {
		return extractFields(this.outputTemplate);
	}
	
	/**
	 * @param fieldsTemplate
	 * @return String[]
	 */
	public static String[] extractFields (List<ClauseEntry> fieldsTemplate) {
		ArrayList<String> fields = new ArrayList<String>(fieldsTemplate.size()*3);
		String var;
		for (ClauseEntry ce : fieldsTemplate)
			if (ce instanceof TriplePattern){
				TriplePattern tp = (TriplePattern) ce;
				if (tp.getSubject().isVariable() && !fields.contains(var = tp.getSubject().getName()))
					fields.set(((Node_RuleVariable)tp.getSubject()).getIndex(), var);
				if (tp.getPredicate().isVariable() && !fields.contains(var = tp.getPredicate().getName()))
					fields.set(((Node_RuleVariable)tp.getPredicate()).getIndex(), var);
				if (tp.getObject().isVariable() && !fields.contains(var = tp.getObject().getName()))
					fields.set(((Node_RuleVariable)tp.getObject()).getIndex(), var);
				else if (tp.getObject().isLiteral() && tp.getObject().getLiteralValue() instanceof Functor)
					for (Node n : ((Functor) tp.getObject().getLiteralValue()).getArgs())
						if (n.isVariable() && !fields.contains(var = n.getName()))
							fields.set(((Node_RuleVariable)n).getIndex(), var);
			}
		
		fields.trimToSize();
		return fields.toArray(new String[0]);
	}
	
	/**
	 * @param fieldsTemplate
	 * @return String[]
	 */
	public static String[] extractJoinFields (List<ClauseEntry> fieldsTemplate) {
		ArrayList<String> fields = new ArrayList<String>();
		List<String> seen = new ArrayList<String>();
		String var;
		for (ClauseEntry ce : fieldsTemplate)
			if (ce instanceof TriplePattern){
				TriplePattern tp = (TriplePattern) ce;
				if (tp.getSubject().isVariable()){
					if (!seen.contains(var = tp.getSubject().getName()))
						seen.add(var);
					else if (!fields.contains(var = tp.getSubject().getName()))
						fields.add(var);
				}
				if (tp.getPredicate().isVariable()){
					if (!seen.contains(var = tp.getPredicate().getName()))
						seen.add(var);
					else if (!fields.contains(var = tp.getPredicate().getName()))
						fields.add(var);
				}
				if (tp.getObject().isVariable()) {
					if (!seen.contains(var = tp.getObject().getName()))
						seen.add(var);
					else if (!fields.contains(var = tp.getObject().getName()))
						fields.add(var);
				} else if (tp.getObject().isLiteral() && tp.getObject().getLiteralValue() instanceof Functor)
					for (Node n : ((Functor) tp.getObject().getLiteralValue()).getArgs())
						if (n.isVariable()){
							if (!seen.contains(var = n.getName()))
								seen.add(var);
							else if (!fields.contains(var = n.getName()))
								fields.add(var);
						}
			}
		
		fields.trimToSize();
		return fields.toArray(new String[0]);
	}

}
