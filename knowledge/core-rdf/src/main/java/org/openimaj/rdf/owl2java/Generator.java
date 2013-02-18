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
package org.openimaj.rdf.owl2java;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.openrdf.model.URI;
import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;
import org.openrdf.sail.memory.MemoryStore;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;

/**
 * The main class of the OWL 2 Java tool. This class provides a main method that
 * will convert an OWL/RDFS file to a set of Java code files that compile to
 * provide an object representation of the ontology.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * @author David Dupplaw (dpd@ecs.soton.ac.uk)
 * @created 29 Oct 2012
 * @version $Author$, $Revision$, $Date$
 */
public class Generator
{
	/**
	 * Options for the generation of the Java classes.
	 * 
	 * @author David Dupplaw (dpd@ecs.soton.ac.uk)
	 * @created 30 Oct 2012
	 * @version $Author$, $Revision$, $Date$
	 */
	public static class GeneratorOptions implements Cloneable
	{
		/** The RDF file to convert */
		@Argument(metaVar = "RDF-FILE", index = 0, usage = "The RDF file to convert", required = true)
		public String rdfFile;

		/** The target directory for the output */
		@Argument(
				metaVar = "TARGET-DIR",
				index = 1,
				usage = "The output directory for the generated class files",
				required = true)
		public String targetDirectory;

		/** Whether to generate @@Predicate annotations. */
		@Option(name = "-annotate", aliases = "-a", usage = "Generate @Predicate annotations")
		public boolean generateAnnotations = true;

		/** Whether to flatten the properties in the class structure */
		@Option(name = "-flatten", aliases = "-f", usage = "Flatten the properties in the generated classes")
		public boolean flattenClassStructure = false;

		/** Whether to put the implementation files in a separate package */
		@Option(name = "-separate", aliases = "-s", usage = "Put implementations in a separate package")
		public boolean separateImplementations = true;

		/** Whether to create a pom.xml file for the output files */
		@Option(name = "-maven", aliases = "-m", usage = "Create a Maven project with this groupId", metaVar = "GROUP-ID")
		public String mavenProject = null;

		/** The artifact identifier for the maven project, if -maven is used */
		@Option(
				name = "-artifactId",
				usage = "Specify the artifact identifier for the maven project",
				metaVar = "ARTIFACT-ID")
		public String mavenArtifactId = "generated-rdf";

		/** The version number for the maven project, if -maven is used */
		@Option(name = "-version", usage = "Specify the version for the maven project", metaVar = "VERSION-NUMBER")
		public String mavenVersionNumber = "1.0";

		/** If a mavenParent is to be added to the pom.xml, the GAV is put here */
		@Option(
				name = "-mavenParent",
				aliases = "-p",
				usage = "The mavenParent artifact GAV to add to the pom.xml (g:a:v)")
		public String mavenParent = null;

		/** Skip generation of the maven pom */
		@Option(name = "-skipPom", usage = "Skip generation of the maven pom", required = false)
		public boolean skipPom = false;

		@Override
		public Object clone() throws CloneNotSupportedException {
			return super.clone();
		}
	}

	/**
	 * Useful little debug method that will print out all the triples for a
	 * given URI from the given repository.
	 * 
	 * @param uri
	 *            The URI
	 * @param conn
	 *            The connection
	 */
	protected static void debugAllTriples(final URI uri, final RepositoryConnection conn)
	{
		try
		{
			// SPARQL query to get the properties and property comments
			final String query = "SELECT ?uri ?pred ?obj WHERE { ?uri ?pred ?obj. } ";

			// Prepare the query
			final TupleQuery preparedQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, query);

			// Execute the query
			final TupleQueryResult res = preparedQuery.evaluate();

			System.out.println("----------------------------------------------");
			System.out.println("Triples for " + uri);
			System.out.println("----------------------------------------------");

			// Loop through the results
			while (res.hasNext())
			{
				final BindingSet bindingSet = res.next();
				System.out.println("( " +
						bindingSet.getBinding("uri").getValue() + " " +
						bindingSet.getBinding("pred").getValue() + " " +
						bindingSet.getBinding("obj").getValue() + " )"
						);
			}

			res.close();

			System.out.println("----------------------------------------------\n");
		} catch (final RepositoryException e)
		{
			e.printStackTrace();
		} catch (final MalformedQueryException e)
		{
			e.printStackTrace();
		} catch (final QueryEvaluationException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Creates a cache of mappings that map URIs to package names.
	 * 
	 * @param classes
	 *            The list of classes to generate the package names for
	 * @return A map of class URI to package name
	 */
	protected static Map<URI, String> generatePackageMappings(
			final Collection<ClassDef> classes)
	{
		final Map<URI, String> packageMapping = new HashMap<URI, String>();

		for (final ClassDef cd : classes)
		{
			if (!packageMapping.containsKey(cd.uri))
			{
				packageMapping.put(cd.uri, Generator.getPackageName(cd.uri));
			}
		}
		return packageMapping;
	}

	/**
	 * From the given URI will attempt to create a java package name by
	 * reversing all the elements and separating with dots.
	 * 
	 * @param uri
	 *            The URI to get a package name for.
	 * @return The Java package name
	 */
	protected static String getPackageName(final URI uri)
	{
		return Generator.getPackageName(uri, true);
	}

	/**
	 * From the given URI will attempt to create a java package name by
	 * reversing all the elements and separating with dots.
	 * 
	 * @param uri
	 *            The URI to get a package name for.
	 * @param removeWWW
	 *            Whether to remove www. parts of URIs
	 * @return The Java package name
	 */
	protected static String getPackageName(final URI uri, final boolean removeWWW)
	{
		String ns = uri.getNamespace();

		// Remove the protocol
		if (ns.contains("//"))
			ns = ns.substring(ns.indexOf("//") + 2);

		// If there's no path component, return it as it is
		if (!ns.contains("/"))
			return ns;

		// Get the path component
		String last = ns.substring(ns.indexOf("/") + 1);

		// Remove any anchors
		if (last.contains("#"))
			last = last.substring(0, last.indexOf("#"));

		// Remove any file extensions
		if (last.contains("."))
			last = last.substring(0, last.indexOf("."));

		// Replace all the path separators by dots
		last = last.replace("/", ".");

		// Remove invalid characters
		// Replace dashes with underscores
		last = last.replace("-", "_");

		// Get the server part (the bit we're going to reverse)
		String serverName = ns.substring(0, ns.indexOf("/"));

		// Split the server name and reverse it.
		final String[] parts = serverName.split("\\.");
		serverName = "";
		int count = 0;
		for (int i = parts.length - 1; i >= 0; i--)
		{
			// Replace any invalid characters with underscores
			if (parts[i].charAt(0) < 65 || parts[i].charAt(0) > 122)
				serverName += "_";

			if (!removeWWW || !parts[i].equals("www"))
			{
				if (count != 0 && i != 0)
					serverName += ".";
				serverName += parts[i];
				count++;
			}
		}

		// We'll also need to replace any invalid characters (or names) with
		// new names
		String lastBit = "";
		if (last.indexOf(".") == -1)
			lastBit = last;
		else
		{
			for (final String s : last.split("\\."))
			{
				lastBit += ".";
				if (s.charAt(0) < 65 || s.charAt(0) > 122)
					lastBit += "_" + s;
				else
					lastBit += s;
			}
		}

		return serverName + lastBit;
	}

	/**
	 * Creates a pom.xml file in the given directory that contains the necessary
	 * dependencies to make the source files compile. It uses a template pom.xml
	 * that is in the resource directory.
	 * 
	 * @param targetDir
	 *            The target directory
	 * @param groupId
	 *            The groupId of the maven artifact
	 * @param artifactId
	 *            The artifactId of the maven project
	 * @param versionNumber
	 *            The version number of the maven project
	 * @param parentGAV
	 *            The GAV of the mavenParent project
	 */
	private static void createPOM(final File targetDir, final String groupId,
			final String artifactId, final String versionNumber, final String parentGAV)
	{
		try
		{
			String s = Resources.toString(Resources.getResource("pom.xml"), Charsets.UTF_8);
			s = Generator.replaceCodes(groupId, artifactId, versionNumber, s);

			// Determine if we need to add the parent element into the pom
			if (parentGAV != null)
			{
				String ps = Resources.toString(Resources.getResource("pom_xml_parent.xml"), Charsets.UTF_8);
				final String[] splits = parentGAV.split(":");
				ps = Generator.replaceCodes(splits[0], splits[1], splits[2], ps);
				s = s.replaceAll("\\{!p!\\}", ps);
			}
			else {
				s = s.replaceAll("\\{!p!\\}", "");
			}

			FileUtils.writeStringToFile(new File(targetDir, "pom.xml"), s, "UTF-8");
		} catch (final IOException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Replaces GAV codes in the given string.
	 * 
	 * @param groupId
	 *            The group identifier
	 * @param artifactId
	 *            The artifact identifier
	 * @param versionNumber
	 *            The version number
	 * @param s
	 *            The string to replace them in
	 * @return The string with the codes replaced
	 */
	public static String replaceCodes(final String groupId,
			final String artifactId, final String versionNumber, String s)
	{
		s = s.replaceAll("\\{!g!\\}", groupId);
		s = s.replaceAll("\\{!a!\\}", artifactId);
		s = s.replaceAll("\\{!v!\\}", versionNumber);
		return s;
	}

	/**
	 * Returns the Java type name for a given URI
	 * 
	 * @param s
	 *            The URI
	 * @return The Java type name
	 */
	protected static String getTypeName(final URI s)
	{
		return s.getLocalName().substring(0, 1).toUpperCase() + s.getLocalName().substring(1);
	}

	/**
	 * Generate the classes for the RDF information that's read from the given
	 * input stream.
	 * 
	 * @param is
	 *            The input stream to find the RDF description
	 * @param go
	 *            The options for the generator
	 * @throws RepositoryException
	 *             If the repository cannot be created
	 * @throws IOException
	 *             If the InputStream cannot be read
	 * @throws RDFParseException
	 *             If the RDF is malformed
	 * @throws QueryEvaluationException
	 *             If the query for classes fails
	 * @throws MalformedQueryException
	 *             If the query for classes fails
	 */
	public static void generate(final InputStream is, final GeneratorOptions go)
			throws RepositoryException, RDFParseException, IOException,
			MalformedQueryException, QueryEvaluationException
	{
		// If we're going to create a Maven project, we'll put all the source
		// files into a src directory, so we need to alter the target directory.
		if (go.mavenProject != null && !go.skipPom)
		{
			// Create the pom.xml file
			Generator.createPOM(new File(go.targetDirectory), go.mavenProject,
					go.mavenArtifactId, go.mavenVersionNumber, go.mavenParent);

			go.targetDirectory = go.targetDirectory +
					File.separator + "src" + File.separator + "main" +
					File.separator + "java";
			new File(go.targetDirectory).mkdirs();
		}

		// Create a new memory store into which we'll plonk all the RDF
		final Repository repository = new SailRepository(new MemoryStore());
		repository.initialize();

		// Plonk all the RDF into the store
		final RepositoryConnection conn = repository.getConnection();
		conn.add(is, "", RDFFormat.RDFXML);

		// Now we'll get all the classes from the ontology
		final Map<URI, ClassDef> classes = ClassDef.loadClasses(go, conn);

		// Try to generate the package mappings for the classes
		final Map<URI, String> pkgs = Generator.generatePackageMappings(
				classes.values());

		// Now we'll go through each of the class definitions and generate
		// interfaces and classes
		for (final ClassDef cd : classes.values())
		{
			cd.generateInterface(new File(go.targetDirectory), pkgs, classes);
			cd.generateClass(new File(go.targetDirectory), pkgs, classes,
					go.flattenClassStructure, go.generateAnnotations,
					go.separateImplementations);
		}
	}

	/**
	 * 
	 * @param args
	 * @throws RepositoryException
	 */
	public static void main(final String[] args) throws RepositoryException
	{
		final GeneratorOptions go = new GeneratorOptions();
		final CmdLineParser parser = new CmdLineParser(go);

		try
		{
			parser.parseArgument(args);
		} catch (final CmdLineException e)
		{
			System.err.println(e.getMessage());
			System.err.println("java Generator RDF-FILE TARGET-DIR [options]");
			parser.printUsage(System.err);
			System.exit(1);
		}

		final File rdfFile = new File(go.rdfFile);
		final File targetDir = new File(go.targetDirectory);

		if (!rdfFile.exists())
		{
			System.out.println("The RDF file does not exist: " + rdfFile);
			System.exit(1);
		}

		if (!targetDir.exists())
		{
			System.out.println("The target directory does not exist: "
					+ targetDir);
			System.exit(1);
		}

		try
		{
			Generator.generate(new FileInputStream(rdfFile), go);
		} catch (final Exception e)
		{
			e.printStackTrace();
		}
	}
}
