package org.openimaj.rdf.storm.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.openimaj.rdf.storm.topology.bolt.StormReteBolt;

import scala.actors.threadpool.Arrays;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.reasoner.TriplePattern;
import com.hp.hpl.jena.reasoner.rulesys.ClauseEntry;
import com.hp.hpl.jena.reasoner.rulesys.Functor;
import com.hp.hpl.jena.reasoner.rulesys.Rule;

/**
 * A collection of functions to represent Jena's Rete Rule Clauses as strings that are
 * independent of the variable names they contain, while maintaining Join accuracy.
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 */
public class VariableIndependentReteRuleToStringUtils {

	private static List<ClauseEntry> sortClause(List<ClauseEntry> template) {
		Collections.sort(template,new Comparator<ClauseEntry>(){
			@Override
			public int compare(ClauseEntry o1,
					ClauseEntry o2) {
				clauseEntryToString(o1).compareTo(clauseEntryToString(o2));
				return 0;
			}
		});
		return template;
	}

	@SuppressWarnings("unchecked")
	private static String clauseEntryToString(ClauseEntry ce, List<String> varNames, int[] matchIndices, Count count){
		if (ce instanceof TriplePattern) {
			TriplePattern filter = (TriplePattern) ce;
			String subject = filter.getSubject().isVariable()
					? varNames.contains(filter.getSubject().getName())
						? matchIndices[varNames.indexOf(filter.getSubject().getName())] == -1
							? "?"+(matchIndices[varNames.indexOf(filter.getSubject().getName())] = count.inc())
							: "?"+matchIndices[varNames.indexOf(filter.getSubject().getName())]
						: "'VAR'"
					: filter.getSubject().isURI()
						? filter.getSubject().getURI()
						: filter.getSubject().isLiteral()
							? filter.getSubject().getLiteralValue().toString()
							: filter.getSubject().getBlankNodeLabel();
			String predicate = filter.getPredicate().isVariable()
					? varNames.contains(filter.getPredicate().getName())
						? matchIndices[varNames.indexOf(filter.getPredicate().getName())] == -1
							? "?"+(matchIndices[varNames.indexOf(filter.getPredicate().getName())] = count.inc())
							: "?"+matchIndices[varNames.indexOf(filter.getPredicate().getName())]
						: "'VAR'"
					: filter.getPredicate().isURI()
						? filter.getPredicate().getURI()
						: filter.getPredicate().isLiteral()
							? filter.getPredicate().getLiteralValue().toString()
							: filter.getPredicate().getBlankNodeLabel();
			String object;
			if (filter.getObject().isLiteral() && filter.getObject().getLiteralValue() instanceof Functor) {
				object = clauseEntryToString((Functor)filter.getObject().getLiteralValue(),
											 varNames, matchIndices, count);
			} else {
				object = filter.getObject().isVariable()
						? varNames.contains(filter.getObject().getName())
							? matchIndices[varNames.indexOf(filter.getObject().getName())] == -1
								? "?"+(matchIndices[varNames.indexOf(filter.getObject().getName())] = count.inc())
								: "?"+matchIndices[varNames.indexOf(filter.getObject().getName())]
							: "'VAR'"
						: filter.getObject().isURI()
							? filter.getObject().getURI()
							: filter.getObject().isLiteral()
								? filter.getObject().getLiteralValue().toString()
								: filter.getObject().getBlankNodeLabel();
			}
			return String.format("(%s %s %s)", subject, predicate, object);
		} else if (ce instanceof Functor) {
			Functor f = (Functor) ce;
			String functor = f.getName()+"(";
			for (Node n : f.getArgs())
				functor += (n.isVariable()
						? varNames.contains(n.getName())
							? matchIndices[varNames.indexOf(n.getName())] == -1
								? "?"+(matchIndices[varNames.indexOf(n.getName())] = count.inc())
								: "?"+matchIndices[varNames.indexOf(n.getName())]
							: "'VAR'"
						: n.isURI()
							? n.getURI()
							: n.isLiteral()
								? n.getLiteralValue().toString()
								: n.getBlankNodeLabel())
						+ " ";
			return functor.substring(0, functor.length() - 1) + ")";
		} else if (ce instanceof Rule) {
			Rule r = (Rule) ce;
			
			List<String> v = Arrays.asList(StormReteBolt.extractFields(Arrays.asList(r.getBody())));
			int[] m = new int[v.size()];
			for (int i = 0; i < m.length; i++ )
				m[i] = -1;
			Count c = new Count(0);
			
			String rule = "[ ";
			if (r.getName() != null)
				rule += r.getName() + " : ";
			return rule
					+ clauseToStringAllVars(Arrays.asList(r.getBody()),v,m,c) + "-> "
					+ clauseToStringAllVars(Arrays.asList(r.getHead()),v,m,c) + "]";
		}
		throw new ClassCastException("The proffered ClauseEntry is not one of the standard implementations supplied by Jena (TriplePattern, Functor or Rule)");
	}
	
	/**
	 * @param ce
	 * @return Variable Independent Clause Entry String
	 */
	public static String clauseEntryToString(ClauseEntry ce){
		return clauseEntryToString(ce, new ArrayList<String>(), new int[0], new Count(0));
	}
	
	private static String clauseToString(List<ClauseEntry> template, List<String> varNames, int[] matchIndices, Count count) {
		template = sortClause(template);
		
		StringBuilder clause = new StringBuilder();
		for (ClauseEntry ce : template)
			clause.append(clauseEntryToString(ce,varNames,matchIndices,count)+" ");
		
		return clause.toString();
	}
	
	/**
	 * @param template
	 * @return Variable Independent Clause String
	 */
	public static String clauseToString(List<ClauseEntry> template){
		@SuppressWarnings("unchecked")
		List<String> varNames = Arrays.asList(StormReteBolt.extractJoinFields(template));
		int[] matchIndices = new int[varNames.size()];
		for (int i = 0; i < matchIndices.length; i++ )
			matchIndices[i] = -1;
		Count count = new Count(0);
		
		return clauseToString(template, varNames, matchIndices, count);
	}
	
	private static String clauseToStringAllVars(List<ClauseEntry> template, List<String> varNames, int[] matchIndices, Count count) {
		template = sortClause(template);
		
		StringBuilder clause = new StringBuilder();
		for (ClauseEntry ce : template)
			clause.append(clauseEntryToString(ce,varNames,matchIndices,count)+" ");
		
		return clause.toString();
	}
	
	/**
	 * @param template
	 * @return Variable Independent Clause String
	 */
	public static String clauseToStringAllVars(List<ClauseEntry> template){
		@SuppressWarnings("unchecked")
		List<String> varNames = Arrays.asList(StormReteBolt.extractFields(template));
		int[] matchIndices = new int[varNames.size()];
		for (int i = 0; i < matchIndices.length; i++ )
			matchIndices[i] = -1;
		Count count = new Count(0);
		
		return clauseToStringAllVars(template, varNames, matchIndices, count);
	}

}