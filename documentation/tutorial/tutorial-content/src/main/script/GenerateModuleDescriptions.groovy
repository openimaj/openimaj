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

/**
 * This script generates the module descriptions list by recursing through all the 
 * modules from the root. The output file is 
 * ${project.build.outputDirectory}/module-descriptions.xml
 */
import org.apache.maven.model.Model;
import org.apache.maven.project.MavenProject;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;

import java.io.FileReader;

import groovy.xml.MarkupBuilder

MavenProject getRootProject() {
	MavenProject p = project;
	
	while (p.parent != null) {
		p = p.parent;
	}
	
	return p;
}

MavenProject loadProject(File pomFile) {
    Model model = null;
    FileReader reader = null;
    MavenXpp3Reader mavenreader = new MavenXpp3Reader();
    try {
        reader = new FileReader(pomFile);
        model = mavenreader.read(reader);
        
		try {
			model.setPomFile(pomFile);
		} catch (Exception nsm) {
			//ignore. Model 2.x doesn't have this method; 3.x does...
		}
    } catch(Exception ex) {
		ex.printStackTrace();
        fail("Unable to read pom " + pomFile);
    }

    return new MavenProject(model);
}

void process(MavenProject p, File baseDir, MarkupBuilder builder) {
	builder.varlistentry() {
		term() {
			filename( baseDir.name == "." ? "OpenIMAJ" : baseDir.name );
		}
		listitem() {
		    if (p.getDescription() != null) {
				def descr = p.getDescription().replaceAll("\\s+", " ").trim();
				if (!descr.endsWith("."))
					descr+=".";
				para( descr )
			}

		    def modules = p.getModules();
		    if (modules != null) {
				variablelist() {
			        modules.each { module ->
						File baseDir2 = new File(baseDir, module)
						MavenProject mp = loadProject(new File(baseDir2, "/pom.xml"));
			            process(mp, baseDir2, builder);
			        }
				}
		    }
		}
	}
}

void processRoot(MavenProject p, MarkupBuilder builder) {
    def modules = p.getModules();
    if (modules != null) {
		builder.variablelist() {
	        modules.each { module ->
				File baseDir2 = new File(p.basedir, module)
				MavenProject mp = loadProject(new File(baseDir2, "/pom.xml"));
	            process(mp, baseDir2, builder);
	        }
		}
    }
}

MavenProject rootProject = getRootProject();
File outdir = new File(project.build.outputDirectory);
outdir.mkdirs();
def writer = new File(outdir, "module-descriptions.xml").newWriter();
def xml = new MarkupBuilder(writer)

writer << '<?xml version="1.0" encoding="UTF-8"?>\n';
writer <<  '<!DOCTYPE chapter PUBLIC "-//OASIS//DTD DocBook XML V4.5//EN" "http://www.oasis-open.org/docbook/xml/4.5/docbookx.dtd">\n';
processRoot(rootProject, xml);

writer.close();
