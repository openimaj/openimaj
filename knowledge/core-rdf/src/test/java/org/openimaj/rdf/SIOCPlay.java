/**
 * Copyright (c) 2011, The University of Southampton and the individual contributors.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *   * 	Redistributions of source code must retain the above copyright notice,
 * 	this list of conditions and the following disclaimer.
 *
 *   *	Redistributions in binary form must reproduce the above copyright notice,
 * 	this list of conditions and the following disclaimer in the documentation
 * 	and/or other materials provided with the distribution.
 *
 *   *	Neither the name of the University of Southampton nor the names of its
 * 	contributors may be used to endorse or promote products derived from this
 * 	software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
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
