package org.openimaj.rdf.storm.topology.bolt;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.openimaj.io.IOUtils;

import scala.actors.threadpool.Arrays;
import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;

import com.hp.hpl.jena.reasoner.rulesys.ClauseEntry;
import com.hp.hpl.jena.reasoner.rulesys.Rule;

/**
 * A {@link StormReteBolt} which has some specific support for rules
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 */
public abstract class StormRuleReteBolt extends StormReteBolt {

	/**
	 *
	 */
	private static final long serialVersionUID = 3977605874827044044L;
	private int variableCount;
	private byte[] ruleSerialized;

	/**
	 * The rule backing this bolt
	 * 
	 * @param rule
	 */
	@SuppressWarnings("unchecked")
	public StormRuleReteBolt(Rule rule) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(baos);
		try {
			IOUtils.write(rule, dos);
		} catch (IOException e) {
		}
		this.ruleSerialized = baos.toByteArray();
		this.variableCount = countVariables(Arrays.asList(rule.getHead()));
	}

	private int countVariables(List<ClauseEntry> fieldEntry) {

		return CompilationStormRuleReteBoltHolder.extractFields(fieldEntry).length;
	}

	@Override
	public int getVariableCount() {
		return this.variableCount;
	}

	@Override
	public void prepare(@SuppressWarnings("rawtypes") Map stormConf, TopologyContext context, OutputCollector collector) {
		super.prepare(stormConf, context, collector);
	}

	/**
	 * Get the rule on which this FlexibleReteBolt is built.
	 * 
	 * @return Rule
	 */
	public Rule getRule() {
		try {
			return IOUtils.read(new DataInputStream(new ByteArrayInputStream(this.ruleSerialized)));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

}
