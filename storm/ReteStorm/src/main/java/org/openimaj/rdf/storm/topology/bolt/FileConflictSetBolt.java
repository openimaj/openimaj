package org.openimaj.rdf.storm.topology.bolt;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.openjena.riot.RiotWriter;

import backtype.storm.tuple.Tuple;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.util.graph.GraphFactory;

/**
 * Output emitted triples back through the network and write them (in NTriples
 * format) to a specified file
 *
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class FileConflictSetBolt extends ReteConflictSetBolt {

	/**
	 *
	 */
	private static final long serialVersionUID = 3452916757795427436L;
	private String output;
	private File outFile;
	private FileOutputStream outStream;

	/**
	 * @param output the output file location
	 */
	public FileConflictSetBolt(String output) {
		this.output = output;
	}

	@Override
	protected void prepare() {
		this.outFile = new File(this.output);
		try {
			this.outStream = new FileOutputStream(outFile);
		} catch (FileNotFoundException e) {
		}
	}

	@Override
	protected void emitTriple(Tuple input, Triple t) {
		// write to the file
		Graph g = GraphFactory.createGraphMem();
		g.add(t);
		RiotWriter.writeTriples(outStream, g);
		super.emitTriple(input, t);

	}

	@Override
	public void cleanup() {
		super.cleanup();
		try {
			this.outStream.flush();
			this.outStream.close();
		} catch (IOException e) {
		}
	}
}
