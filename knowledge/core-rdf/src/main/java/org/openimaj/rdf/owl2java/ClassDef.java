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
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.WordUtils;
import org.joda.time.DateTime;
import org.openimaj.rdf.owl2java.Generator.GeneratorOptions;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.sail.memory.model.MemBNode;
import org.openrdf.sail.memory.model.MemLiteral;
import org.openrdf.sail.memory.model.MemStatement;
import org.openrdf.sail.memory.model.MemStatementList;

/**
 * Represents the definition of an ontology class.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * @author David Dupplaw (dpd@ecs.soton.ac.uk)
 * @created 29 Oct 2012
 * @version $Author$, $Revision$, $Date$
 */
public class ClassDef
{
	/** The description of the class from the RDF comment */
	protected String comment;

	/** The URI of the class */
	protected URI uri;

	/**
	 * List of the all the ancestral superclasses to each of the direct
	 * superclasses
	 */
	protected Map<URI, Set<URI>> allSuperclasses;

	/** A list of the direct superclasses of this class */
	protected Set<URI> directSuperclasses;

	/** List of the properties in this class */
	protected Set<PropertyDef> properties;

	/**
	 * Outputs the Java class definition for this class def
	 * 
	 * {@inheritDoc}
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return "class " + this.uri.getLocalName() + " extends " +
				this.allSuperclasses + " {\n" + "\t" + this.properties + "\n}\n";
	}

	/**
	 * Loads all the class definitions from the given repository
	 * 
	 * @param conn
	 *            The repository connection from where to get the classes
	 * @param go
	 * @return a Map that maps class URIs to ClassDef objects
	 * @throws RepositoryException
	 * @throws MalformedQueryException
	 * @throws QueryEvaluationException
	 */
	public static Map<URI, ClassDef> loadClasses(final GeneratorOptions go, final RepositoryConnection conn)
			throws RepositoryException, MalformedQueryException, QueryEvaluationException
	{
		final HashMap<URI, ClassDef> classes = new HashMap<URI, ClassDef>();

		// This is the types we'll look for
		// We'll look for both OWL and RDF classes
		final String[] clzTypes = {
				"<http://www.w3.org/2002/07/owl#Class>",
				"rdfs:Class"
		};

		// Loop over the namespaces
		for (final String clzType : clzTypes)
		{
			// Create a query to get the classes
			final String query = "SELECT Class, Comment "
					+ "FROM {Class} rdf:type {" + clzType + "}; "
					+ " [ rdfs:comment {Comment} ]";

			// Prepare the query...
			final TupleQuery preparedQuery = conn.prepareTupleQuery(
					QueryLanguage.SERQL, query);

			// Run the query...
			final TupleQueryResult res = preparedQuery.evaluate();

			// Loop over the results
			while (res.hasNext())
			{
				final BindingSet bindingSet = res.next();

				// If we have a class with a URI...
				if (bindingSet.getValue("Class") instanceof URI)
				{
					// Create a new class definition for it
					final ClassDef clz = new ClassDef();

					// Get the comment, if there is one.
					if (bindingSet.getValue("Comment") != null)
					{
						final MemLiteral lit = (MemLiteral)
								bindingSet.getValue("Comment");
						clz.comment = lit.stringValue();
					}

					clz.uri = (URI) bindingSet.getValue("Class");
					clz.directSuperclasses = ClassDef.getSuperclasses(clz.uri, conn);
					clz.properties = PropertyDef.loadProperties(go, clz.uri, conn);

					// Check whether there are any other classes
					ClassDef.getEquivalentClasses(clz, conn);

					// Get all the superclasses in the tree
					clz.allSuperclasses = clz.getAllSuperclasses(conn);

					classes.put(clz.uri, clz);
				}
			}
		}
		return classes;
	}

	/**
	 * Checks for owl:equivalentClass and updates the class definition based on
	 * whats found.
	 * 
	 * @param clz
	 *            the class definition
	 * @param conn
	 *            The connection to the repository
	 */
	private static void getEquivalentClasses(final ClassDef clz, final RepositoryConnection conn)
	{
		try
		{
			final String sparql = "prefix owl: <http://www.w3.org/2002/07/owl#> " +
					"SELECT ?clazz WHERE " +
					"{ <" + clz.uri + "> owl:equivalentClass ?clazz . }";

			// System.out.println( sparql );

			// Prepare the query...
			final TupleQuery preparedQuery = conn.prepareTupleQuery(
					QueryLanguage.SPARQL, sparql);

			// Run the query...
			final TupleQueryResult res = preparedQuery.evaluate();

			// Loop through the results (if any)
			while (res.hasNext())
			{
				final BindingSet bs = res.next();

				final Value clazz = bs.getBinding("clazz").getValue();

				// If it's an equivalent then we'll simply make this class
				// a subclass of the equivalent class.
				// TODO: There is a possibility that we could end up with a
				// cycle here
				// and the resulting code would not compile.
				if (clazz instanceof URI)
					clz.directSuperclasses.add((URI) clazz);
				else
				// If it's a BNode, then the BNode defines the equivalence.
				if (clazz instanceof MemBNode)
				{
					final MemBNode b = (MemBNode) clazz;
					final MemStatementList sl = b.getSubjectStatementList();

					for (int i = 0; i < sl.size(); i++)
					{
						final MemStatement x = sl.get(i);
						System.out.println("    -> " + x);
					}
				}
			}

			res.close();
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
	 * Retrieves the superclass list for the given class URI using the given
	 * repository
	 * 
	 * @param uri
	 *            The URI of the class to find the allSuperclasses of
	 * @param conn
	 *            The respository
	 * @return A list of URIs of allSuperclasses
	 * 
	 * @throws RepositoryException
	 * @throws MalformedQueryException
	 * @throws QueryEvaluationException
	 */
	private static Set<URI> getSuperclasses(final URI uri, final RepositoryConnection conn)
			throws RepositoryException, MalformedQueryException, QueryEvaluationException
	{
		// SPARQL query to get the allSuperclasses
		final String query = "SELECT ?superclass WHERE { " +
				"<" + uri.stringValue() + "> " +
				"<http://www.w3.org/2000/01/rdf-schema#subClassOf> " +
				"?superclass. }";

		final TupleQuery preparedQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, query);
		final TupleQueryResult res = preparedQuery.evaluate();

		final Set<URI> superclasses = new HashSet<URI>();
		while (res.hasNext()) {
			final BindingSet bindingSet = res.next();

			if (bindingSet.getValue("superclass") instanceof URI) {
				superclasses.add((URI) bindingSet.getValue("superclass"));
			}
		}

		return superclasses;
	}

	/**
	 * Generates a Java file in the target directory
	 * 
	 * @param targetDir
	 *            The target directory
	 * @param pkgs
	 *            A map of package mappings to class URIs
	 * @param classes
	 *            A map of class URIs to ClassDefs
	 * @param flattenClassStructure
	 *            Whether to flatten the class structure
	 * @param generateAnnotations
	 *            Whether to generate OpenIMAJ RDF annotations for the
	 *            properties
	 * @param separateImplementations
	 * @throws FileNotFoundException
	 */
	public void generateClass(final File targetDir, final Map<URI, String> pkgs,
			final Map<URI, ClassDef> classes, final boolean flattenClassStructure,
			final boolean generateAnnotations, final boolean separateImplementations) throws FileNotFoundException
	{
		// We don't need to generate an implementation file if there are no
		// properties to get/set
		// if (this.properties.size() == 0)
		// return;

		// Generate the filename for the output file
		final File path = new File(targetDir.getAbsolutePath() + File.separator +
				pkgs.get(this.uri).replace(".", File.separator) +
				(separateImplementations ? File.separator + "impl" : ""));
		path.mkdirs();
		PrintStream ps;
		try
		{
			ps = new PrintStream(new File(path.getAbsolutePath()
					+ File.separator + Generator.getTypeName(this.uri) + "Impl.java"),
					"UTF-8");
		} catch (final UnsupportedEncodingException e)
		{
			e.printStackTrace();
			return;
		}

		// Output the package definition
		ps.println("package " + pkgs.get(this.uri) +
				(separateImplementations ? ".impl" : "") + ";");
		ps.println();

		// Output the imports
		ps.println("import org.openimaj.rdf.owl2java.Something;");
		if (separateImplementations)
			ps.println("import " + pkgs.get(this.uri) + ".*;");
		if (generateAnnotations)
		{
			ps.println("import org.openimaj.rdf.serialize.Predicate;\n");
			ps.println("import org.openimaj.rdf.serialize.RDFType;\n");
		}
		this.printImports(ps, pkgs, false, classes, true);
		ps.println();

		// Output the comment at the top of the class
		this.printClassComment(ps);

		// Output the class
		ps.print("@RDFType(\"" + this.uri + "\")\n");
		ps.print("public class " + Generator.getTypeName(this.uri) + "Impl ");
		ps.print("extends Something ");

		// It will implement the interface that defines it
		ps.print("implements " + Generator.getTypeName(this.uri));

		// if (this.superclasses.size() > 0)
		// {
		// // ...and any of the super class interfaces
		// for( final URI superclass : this.superclasses )
		// {
		// ps.print(", ");
		// ps.print( Generator.getTypeName( superclass ) );
		// }
		// }
		ps.println("\n{");

		// Output the definition of the class
		this.printClassPropertyDefinitions(ps, classes,
				flattenClassStructure, generateAnnotations);

		ps.println("}\n");
	}

	/**
	 * Generates a Java interface file in the target directory
	 * 
	 * @param targetDir
	 *            The target directory
	 * @param pkgs
	 *            A list of package mappings to class URIs
	 * @param classes
	 *            The URI to class definition map.
	 * @throws FileNotFoundException
	 */
	public void generateInterface(final File targetDir, final Map<URI, String> pkgs,
			final Map<URI, ClassDef> classes) throws FileNotFoundException
	{
		final File path = new File(targetDir.getAbsolutePath() + File.separator +
				pkgs.get(this.uri).replace(".", File.separator));
		path.mkdirs();
		PrintStream ps;
		try
		{
			ps = new PrintStream(new File(path.getAbsolutePath()
					+ File.separator + Generator.getTypeName(this.uri) + ".java"),
					"UTF-8");
		} catch (final UnsupportedEncodingException e)
		{
			e.printStackTrace();
			return;
		}

		ps.println("package " + pkgs.get(this.uri) + ";");
		ps.println();
		this.printImports(ps, pkgs, true, classes, false);
		ps.println();

		this.printClassComment(ps);

		ps.print("public interface " + Generator.getTypeName(this.uri) + " ");
		if (this.allSuperclasses.size() > 0)
		{
			ps.print("\n\textends ");
			boolean first = true;
			for (final URI superClassURI : this.directSuperclasses)
			{
				if (!first)
					ps.print(", ");
				ps.print(Generator.getTypeName(superClassURI));
				first = false;
			}
		}

		ps.println("\n{");
		this.printInterfacePropertyDefinitions(ps);
		ps.println("\tpublic String getURI();\n");
		ps.println("}\n");
	}

	/**
	 * Prints the comment at the top of the file for this class.
	 * 
	 * @param ps
	 *            The stream to print the comment to.
	 */
	private void printClassComment(final PrintStream ps)
	{
		ps.println("/**");
		if (this.comment == null) {
			ps.println(" * " + this.uri);
		} else {
			final String cmt = WordUtils.wrap(" * " +
					this.comment.replaceAll("\\r?\\n", " "), 80, "\n * ", false);
			ps.println(" " + cmt);
		}

		ps.println(" *");
		ps.println(" *\t@created " + new DateTime());
		ps.println(" *\t@generated by owl2java from OpenIMAJ");
		ps.println(" */");
	}

	/**
	 * Outputs the list of imports necessary for this class.
	 * 
	 * @param ps
	 *            The stream to print the imports to
	 * @param pkgs
	 *            The list of package mappings for all the known classes
	 * @param allSuperclasses
	 *            Whether to print imports for allSuperclasses
	 */
	private void printImports(final PrintStream ps, final Map<URI, String> pkgs,
			final boolean superclasses, final Map<URI, ClassDef> classes,
			final boolean implementations)
	{
		final Set<String> imports = new HashSet<String>();

		final Map<PropertyDef, String> pd = new HashMap<PropertyDef, String>();
		final Map<String, String> instanceNameMap = new HashMap<String, String>();
		this.getFullPropertyList(pd, instanceNameMap, classes);

		for (final PropertyDef p : pd.keySet())
			if (implementations || pd.get(p).equals("this"))
				if (p.needsImport(implementations) != null)
					imports.addAll(p.needsImport(implementations));

		if (superclasses)
		{
			for (final URI u : this.directSuperclasses) {
				imports.add(pkgs.get(u) + ".");
				// TODO - impl package should depend on configuration
				imports.add(pkgs.get(u) + ".impl.");
			}
		}

		imports.remove(pkgs.get(this.uri) + ".");

		final String[] sortedImports = imports.toArray(new String[imports.size()]);
		Arrays.sort(sortedImports);

		for (final String imp : sortedImports) {
			if (imp.endsWith("."))
				ps.println("import " + imp + "*;");
			else
				ps.println("import " + imp + ";");
		}
	}

	/**
	 * Outputs all the properties into the class definition.
	 * 
	 * @param ps
	 *            The stream to print to.
	 */
	private void printInterfacePropertyDefinitions(final PrintStream ps)
	{
		for (final PropertyDef p : this.properties)
			ps.println(p.toSettersAndGetters("\t", false, null, false));
	}

	/**
	 * Outputs all the properties into the class definition.
	 * 
	 * @param ps
	 *            The stream to print to.
	 * @param classes
	 *            A map of class URIs to ClassDefs
	 * @param flattenClassStructure
	 *            Whether to combine all the properties from all the
	 *            allSuperclasses into this class (TRUE), or whether to use
	 *            instance pointers to classes of that type (FALSE)
	 * @param generateAnnotations
	 */
	private void printClassPropertyDefinitions(final PrintStream ps,
			final Map<URI, ClassDef> classes, final boolean flattenClassStructure,
			final boolean generateAnnotations)
	{
		// Remember which ones we've output already
		final HashSet<URI> alreadyDone = new HashSet<URI>();

		if (flattenClassStructure)
		{
			// TODO: Check this still works after the change in properties list
			// Work out all the properties to output
			final Set<PropertyDef> pd = new HashSet<PropertyDef>();
			pd.addAll(this.properties);
			for (final Set<URI> superclassList : this.allSuperclasses.values())
			{
				for (final URI superclass : superclassList)
				{
					if (!alreadyDone.contains(superclass))
					{
						pd.addAll(classes.get(superclass).properties);
						alreadyDone.add(superclass);
					}
				}
			}

			// Output all the property definitions for this class.
			for (final PropertyDef p : pd)
				ps.println(p.toJavaDefinition("\t", generateAnnotations));
			ps.println();
			// Output all the getters and setters for this class.
			for (final PropertyDef p : pd)
				ps.println(p.toSettersAndGetters("\t", true, null, false));
		}
		else
		{
			System.out.println("=======================================");
			System.out.println(this.uri);
			System.out.println("=======================================");
			System.out.println("Direct superclasses: " + this.directSuperclasses);

			// Output all the property definitions for this class.
			for (final PropertyDef p : this.properties)
				ps.println(p.toJavaDefinition("\t", generateAnnotations));
			ps.println();

			// Now we need to output the links to other objects from which
			// this class inherits. While we do that, we'll also remember which
			// properties we need to delegate to the other objects.
			final HashMap<PropertyDef, String> pd = new HashMap<PropertyDef, String>();
			final HashMap<String, String> entityNameMap = new HashMap<String, String>();
			this.getFullPropertyList(pd, entityNameMap, classes);

			// Check whether there are any delegation members to output
			// We do this by removing "this" from the map and seeing what's
			// left.
			final Map<PropertyDef, String> xx = new HashMap<PropertyDef, String>();
			xx.putAll(pd);
			final HashSet<String> delegatesToOutput = new HashSet<String>();
			final Iterator<String> i = xx.values().iterator();
			String xxx = null;
			while (i.hasNext())
				if (!(xxx = i.next()).equals("this"))
				{
					delegatesToOutput.add(xxx);
				}
			System.out.println(xx);
			System.out.println(entityNameMap);
			System.out.println(delegatesToOutput);
			if (delegatesToOutput.size() > 0)
			{
				for (final String instanceName : delegatesToOutput)
				{
					final String typeName = entityNameMap.get(instanceName);
					ps.println("\t/** " + typeName + " superclass instance */");
					ps.println("\tprivate " + typeName + " " + instanceName + " = new " + typeName + "Impl();\n");
				}
			}

			// Now output the delegated getters and setters for this class
			for (final PropertyDef pp : pd.keySet())
			{
				final String instanceName = pd.get(pp);
				ps.println("\n\t// From class " + instanceName + "\n\n");
				ps.println(pp.toSettersAndGetters("\t", true, instanceName, false));
			}
		}
	}

	/**
	 * Traverses up the tree from this class to find all ancestor
	 * allSuperclasses.
	 * 
	 * @param conn
	 *            A connection to a triple-store repository
	 * @return A list of URIs representing the superclass ancestors of this
	 *         class
	 */
	public Map<URI, Set<URI>> getAllSuperclasses(final RepositoryConnection conn)
	{
		final HashMap<URI, Set<URI>> map = new HashMap<URI, Set<URI>>();
		for (final URI uri : this.directSuperclasses)
		{
			final HashSet<URI> uris = new HashSet<URI>();
			ClassDef.getAllSuperclasses(uri, conn, uris);
			map.put(uri, uris);
		}

		return map;
	}

	/**
	 * For the given URI, will query the repository and fill the list of URIs
	 * with the URIs of all the ancestor allSuperclasses to the given URI.
	 * 
	 * @param uri
	 *            The URI of the class to find the ancestors of
	 * @param conn
	 *            A connection to a triple-store repository.
	 * @param uris
	 *            The list where classes are to be added
	 */
	private static void getAllSuperclasses(final URI uri,
			final RepositoryConnection conn, final Set<URI> uris)
	{
		try
		{
			final Set<URI> superclassesOf = ClassDef.getSuperclasses(uri, conn);
			uris.addAll(superclassesOf);
			for (final URI u : superclassesOf)
				ClassDef.getAllSuperclasses(u, conn, uris);
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
	 * Returns a full list of all the properties for all the classes in the
	 * tree.
	 * 
	 * @param pd
	 *            The set of property definitions (linked to instance name) to
	 *            fill
	 * @param instanceNameMap
	 *            The list of property to instance name map
	 * @param classes
	 *            The definition of all the classes in the ontology
	 */
	private void getFullPropertyList(final Map<PropertyDef, String> pd,
			final Map<String, String> instanceNameMap, final Map<URI, ClassDef> classes)
	{
		for (final PropertyDef pp : this.properties)
			pd.put(pp, "this");

		for (final URI superclass : this.directSuperclasses)
		{
			final String instanceName =
					superclass.getLocalName().substring(0, 1).toLowerCase() +
							superclass.getLocalName().substring(1);

			for (final PropertyDef pp : classes.get(superclass).properties)
				pd.put(pp, instanceName);

			// map all the properties of the ancestors of this direct subclass
			// to the instance name of the direct superclass. This will make all
			// the getters and setters use the superclass instance to access the
			// ancestor properties.
			for (final URI ancestorURI : this.allSuperclasses.get(superclass))
			{
				for (final PropertyDef pp : classes.get(ancestorURI).properties)
					pd.put(pp, instanceName);
			}

			// We don't need the instance variable if we're not inheriting
			// any properties from the superclasses.
			if (pd.keySet().size() == 0)
				continue;

			// Check we've not already got a delegation for that instance
			if (instanceNameMap.get(instanceName) == null)
			{
				instanceNameMap.put(instanceName,
						/* Generator.getPackageName(superclass) + "." + */
						Generator.getTypeName(superclass));
			}
		}
	}
}
