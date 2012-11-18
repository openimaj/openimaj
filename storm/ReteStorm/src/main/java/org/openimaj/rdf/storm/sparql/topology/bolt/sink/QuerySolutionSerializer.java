package org.openimaj.rdf.storm.sparql.topology.bolt.sink;

import java.io.OutputStream;
import java.util.List;

import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFactory;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.sparql.engine.QueryIterator;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 */
public enum QuerySolutionSerializer {
	/**
	 * Output as CSV
	 */
	CSV {
		@Override
		public void output(OutputStream stream, ResultSet rs) {
			ResultSetFormatter.outputAsCSV(stream, rs);
		}

	},
	/**
	 * Output as JSON
	 */
	JSON {
		@Override
		public void output(OutputStream stream, ResultSet rs) {
			ResultSetFormatter.outputAsJSON(stream, rs);
		}

	},
	/**
	 * Output as RDF Turtle
	 */
	RDF_NTRIPLES {
		@Override
		public void output(OutputStream stream, ResultSet rs) {
			ResultSetFormatter.outputAsRDF(stream, "N-TRIPLES", rs);
		}

	};
	/**
	 * @param iter
	 * @param vars
	 * @param stream
	 */
	public void serialize(QueryIterator iter, List<String> vars, OutputStream stream) {
		ResultSet rs = ResultSetFactory.create(iter, vars);
		output(stream, rs);
	}

	protected abstract void output(OutputStream stream, ResultSet rs);
}