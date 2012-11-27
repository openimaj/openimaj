package org.openimaj.rdf;

import java.io.File;
import java.net.URL;

import org.openimaj.rdf.owl2java.Generator;
import org.openimaj.rdf.owl2java.Generator.GeneratorOptions;

/**
 * Generates the SIOC ontology java classes
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class SIOCPlay {
	private static final String OPENIMAJ_HOME = "/Users/ss/Development/java/openimaj/trunk";

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		GeneratorOptions opts = new GeneratorOptions();
		opts.mavenArtifactId = "sioc";
		opts.mavenProject = "org.openimaj";
		opts.targetDirectory = new File(OPENIMAJ_HOME,"knowledge/ontologies/sioc").toString();
		opts.mavenParent = "org.openimaj:openimaj-ontologies:1.0.6-SNAPSHOT";
		opts.mavenVersionNumber = "1.0.6-SNAPSHOT";
		opts.separateImplementations = false;
		Generator.generate(new URL("http://rdfs.org/sioc/ns#").openStream(), opts);
	}
}
