package org.openimaj.rdf.storm.sparql.topology.bolt.sink;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import org.apache.log4j.Logger;
import org.mortbay.io.RuntimeIOException;
import org.openimaj.rdf.storm.sparql.topology.bolt.StormSPARQLReteConflictSetBolt;
import org.openimaj.rdf.storm.sparql.topology.bolt.StormSPARQLReteConflictSetBolt.StormSPARQLReteConflictSetBoltSink;
import org.openjena.riot.RiotWriter;

import backtype.storm.task.TopologyContext;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.util.graph.GraphFactory;

//import org.openjena.riot.RiotWriter;

/**
 * A Sink used mainly for debugging and tests
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 */
public class FileSink implements StormSPARQLReteConflictSetBolt.StormSPARQLReteConflictSetBoltSink {
	private FileOutputStream fos;
	private File outDir;
	private List<String> varOrder;
	private QuerySolutionSerializer serializerMode;
	private File outFile;
	private static Logger logger = Logger.getLogger(FileSink.class);

	/**
	 * @param file
	 * @throws FileNotFoundException
	 */
	public FileSink(File file) throws FileNotFoundException {
		this.outDir = file;
		this.serializerMode = QuerySolutionSerializer.JSON;
	}

	/**
	 * @param file
	 * @param serializerMode
	 * @throws FileNotFoundException
	 */
	public FileSink(File file, QuerySolutionSerializer serializerMode)
			throws FileNotFoundException {
		this.outDir = file;
		this.serializerMode = serializerMode;
	}

	@Override
	public void consumeTriple(Triple triple) {
		logger.debug("Writing a triple to: " + this.outFile);
		Graph g = GraphFactory.createGraphMem();
		g.add(triple);
		RiotWriter.writeTriples(fos, g);
		try {
			fos.flush();
		} catch (IOException e) {
			throw new RuntimeIOException(e);
		}
	}

	@Override
	public void instantiate(StormSPARQLReteConflictSetBolt conflictSet) {
		try {
			if (outDir.exists()) {
				if (!outDir.isDirectory()) {
					throw new RuntimeIOException("File exists: " + outDir);
				}
			}
			else
				outDir.mkdirs();
			TopologyContext context = conflictSet.getContext();
			String name = String.format("%s_%d", context.getThisComponentId(), context.getThisTaskId());

			this.outFile = new File(outDir, name);
			if (outFile.exists()) {
				throw new RuntimeIOException("The same output was attempting to be written twice!");
			}
			fos = new FileOutputStream(outFile);
		} catch (FileNotFoundException e) {
			throw new RuntimeException("Couldn't open output file: " + outDir + " becuase " + e.getMessage());
		}
		Query query = conflictSet.getQuery();
		if (query.isSelectType()) {
			this.varOrder = query.getResultVars();
			writeSelectHeader();
		}
	}

	private void writeSelectHeader() {
	}

	@Override
	public void consumeBindings(QueryIterator bindingsIter) {
		this.serializerMode.serialize(bindingsIter, varOrder, fos);
	}

}