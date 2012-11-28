package org.openimaj.twitter;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

import org.openjena.riot.SysRIOT;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.sparql.graph.GraphFactory;

/**
 * Holds an internal Jena Graph of the USMF status. The default language used is
 * NTriples
 * 
 * @author Jon Hare (jsh2@ecs.soton.ac.uk), Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 */
public class GeneralJSONRDF extends GeneralJSON {
	static {
		SysRIOT.wireIntoJena();
	}
	Graph g;

	public GeneralJSONRDF() {

		g = GraphFactory.createGraphMem();

	}

	@Override
	public void readASCII(final Scanner in) throws IOException {
		StringBuffer b = new StringBuffer();
		while (in.hasNext()) {
			b.append(in.next());
		}
		InputStream stream = new ByteArrayInputStream(b.toString().getBytes("UTF-8"));
		Model m = ModelFactory.createModelForGraph(g);
		m.read(stream, "", "NTRIPLES");
		m.close();
	}

	@Override
	public void fillUSMF(USMFStatus status) {
		// Read the 
		// Perform the SPARQL
	}

	@Override
	public void fromUSMF(USMFStatus status) {
		// TODO Auto-generated method stub

	}

}
