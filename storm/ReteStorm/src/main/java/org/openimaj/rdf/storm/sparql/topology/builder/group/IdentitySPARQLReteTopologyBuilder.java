package org.openimaj.rdf.storm.sparql.topology.builder.group;

import java.util.List;

import org.openimaj.rdf.storm.sparql.topology.bolt.CompilationStormSPARQLBoltHolder;
import org.openimaj.rdf.storm.sparql.topology.bolt.QueryHoldingReteFilterBolt;
import org.openimaj.rdf.storm.sparql.topology.bolt.QueryHoldingReteFilterIdentityBolt;
import org.openimaj.rdf.storm.sparql.topology.bolt.QueryHoldingReteJoinBolt;
import org.openimaj.rdf.storm.sparql.topology.bolt.QueryHoldingReteJoinIdentityBolt;
import org.openimaj.rdf.storm.sparql.topology.bolt.StormSPARQLFilterBolt;
import org.openimaj.rdf.storm.sparql.topology.bolt.StormSPARQLFilterIdentityBolt;
import org.openimaj.rdf.storm.sparql.topology.bolt.StormSPARQLReteConflictSetBolt;
import org.openimaj.rdf.storm.sparql.topology.bolt.StormSPARQLReteConflictSetIdentityBolt;
import org.openimaj.rdf.storm.sparql.topology.builder.SPARQLReteTopologyBuilderContext;

import scala.actors.threadpool.Arrays;
import backtype.storm.topology.BoltDeclarer;

import com.hp.hpl.jena.reasoner.rulesys.ClauseEntry;
import com.hp.hpl.jena.reasoner.rulesys.Rule;
import com.hp.hpl.jena.sparql.syntax.ElementFilter;

public abstract class IdentitySPARQLReteTopologyBuilder extends
		StaticDataSPARQLReteTopologyBuilder {

	@Override
	public void connectFilterBolt(SPARQLReteTopologyBuilderContext context, String name, QueryHoldingReteFilterBolt bolt){
		super.connectFilterBolt(context, name, bolt);
	}
	@Override
	protected void connectFilterBolt(SPARQLReteTopologyBuilderContext context, String name, StormSPARQLFilterBolt bolt) {
		super.connectFilterBolt(context, name, bolt);
	}
	@Override
	public void connectJoinBolt(SPARQLReteTopologyBuilderContext context, String name, QueryHoldingReteJoinBolt bolt) {
		BoltDeclarer midBuild = context.builder.setBolt(name, bolt,this.getJoinBoltParallelism());
		midBuild.shuffleGrouping(bolt.getLeftBolt());
		midBuild.shuffleGrouping(bolt.getRightBolt());
	}
	@Override
	protected void connectToFinalTerminal(SPARQLReteTopologyBuilderContext context, BoltDeclarer finalTerminalDeclarer,
			String finalTerminalName, StormSPARQLReteConflictSetBolt finalTerm, NamedCompilation namedCompilation)
	{
		CompilationStormSPARQLBoltHolder secondToLastCompilation = namedCompilation.secondObject();
		String secondToLastBolt = namedCompilation.firstObject();
		finalTerminalDeclarer.shuffleGrouping(secondToLastBolt);
		finalTerm.registerSourceVariables(secondToLastBolt, secondToLastCompilation.getVars());
	}

	@Override
	public StormSPARQLReteConflictSetBolt constructConflictSetBolt(
			String finalTermName,
			List<List<NamedCompilation>> secondToLast) {
		return StormSPARQLReteConflictSetIdentityBolt.construct(context.query.simpleQuery, conflictSetSink());
	}

	@Override
	protected StormSPARQLFilterBolt constructFilterCompilation(ElementFilter el){
		return new StormSPARQLFilterIdentityBolt(el);
	}

	@Override
	protected QueryHoldingReteFilterBolt constructReteFilterBolt(Rule rule){
		return new QueryHoldingReteFilterIdentityBolt(rule);
	}

	@Override
	public QueryHoldingReteJoinBolt constructReteJoinBolt(
			NamedCompilation currentNameCompBoltPair,
			NamedCompilation otherNameCompBoltPair, List<ClauseEntry> template) {
		CompilationStormSPARQLBoltHolder currentBolt = currentNameCompBoltPair.secondObject();
		CompilationStormSPARQLBoltHolder otherBolt = otherNameCompBoltPair.secondObject();

		String[] currentVars = currentNameCompBoltPair.secondObject().getVars();
		String[] otherVars = otherNameCompBoltPair.secondObject().getVars();
		String[] newVars = CompilationStormSPARQLBoltHolder.extractFields(template);
		int[] templateLeft = new int[newVars.length];
		int[] templateRight = new int[newVars.length];
		int[] matchLeft = new int[currentVars.length];
		int[] matchRight = new int[otherVars.length];

		for (int l = 0; l < currentVars.length; l++)
			matchLeft[l] = Arrays.asList(otherVars).indexOf(currentVars[l]);
		for (int r = 0; r < otherVars.length; r++)
			matchRight[r] = Arrays.asList(currentVars).indexOf(otherVars[r]);
		for (int n = 0; n < newVars.length; n++) {
			templateLeft[n] = Arrays.asList(currentVars).indexOf(newVars[n]);
			templateRight[n] = Arrays.asList(otherVars).indexOf(newVars[n]);
		}
		QueryHoldingReteJoinBolt queryHoldingReteJoinBolt = new QueryHoldingReteJoinIdentityBolt(
				currentNameCompBoltPair.firstObject(), matchLeft, templateLeft,
				otherNameCompBoltPair.firstObject(), matchRight, templateRight,
				new Rule(template, template)
				);
		VariableIndexRenamingProcessor currentRenamer = new VariableIndexRenamingProcessor(currentVars);
		VariableIndexRenamingProcessor otherRenamer = new VariableIndexRenamingProcessor(otherVars);

		queryHoldingReteJoinBolt.setStaticDataSources(staticDataSources(context));
		queryHoldingReteJoinBolt.setQueryString(
				currentRenamer.constructQueryString(currentBolt.getQueryString()),
				otherRenamer.constructQueryString(otherBolt.getQueryString())
				);
		return queryHoldingReteJoinBolt;
	}

}
