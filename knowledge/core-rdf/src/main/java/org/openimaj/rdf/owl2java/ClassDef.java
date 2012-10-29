package org.openimaj.rdf.owl2java;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.WordUtils;
import org.openrdf.model.URI;
import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.sail.memory.model.MemLiteral;

public class ClassDef {
	String comment;
	URI uri;
	List<URI> superclasses;
	List<PropertyDef> properties;

	@Override
	public String toString() {
		return "class " + uri.getLocalName() + " extends " + superclasses + " {\n" +
				"\t" + properties + "\n}\n";
	}

	public static List<ClassDef> loadClasses(RepositoryConnection conn) throws RepositoryException,
			MalformedQueryException, QueryEvaluationException
	{
		final List<ClassDef> classes = new ArrayList<ClassDef>();
		final String[] clzTypes = { "owl", "rdfs" };

		for (final String clzType : clzTypes) {
			final String query = "SELECT Class, Comment "
					+ "FROM {Class} rdf:type {" + clzType + ":Class}; "
					+ " [ rdfs:comment {Comment} ]" + " USING NAMESPACE "
					+ "   owl = <http://www.w3.org/2002/07/owl#>";

			final TupleQuery preparedQuery = conn.prepareTupleQuery(QueryLanguage.SERQL, query);
			final TupleQueryResult res = preparedQuery.evaluate();

			while (res.hasNext()) {
				final BindingSet bindingSet = res.next();

				if (bindingSet.getValue("Class") instanceof URI) {
					final ClassDef clz = new ClassDef();

					if (bindingSet.getValue("Comment") != null) {
						final MemLiteral lit = (MemLiteral) bindingSet.getValue("Comment");
						clz.comment = lit.stringValue();
					}

					clz.uri = (URI) bindingSet.getValue("Class");

					clz.superclasses = getSuperclasses(clz.uri, conn);
					clz.properties = PropertyDef.loadProperties(clz.uri, conn);

					classes.add(clz);
				}
			}
		}
		return classes;
	}

	private static List<URI> getSuperclasses(URI uri, RepositoryConnection conn) throws RepositoryException,
			MalformedQueryException, QueryEvaluationException
	{
		final String query = "PREFIX rdfs:    <http://www.w3.org/2000/01/rdf-schema#>\n" +
				"SELECT ?superclass WHERE { <" + uri.stringValue() + "> rdfs:subClassOf ?superclass. }";

		final TupleQuery preparedQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, query);
		final TupleQueryResult res = preparedQuery.evaluate();

		final List<URI> superclasses = new ArrayList<URI>();
		while (res.hasNext()) {
			final BindingSet bindingSet = res.next();

			if (bindingSet.getValue("superclass") instanceof URI) {
				superclasses.add((URI) bindingSet.getValue("superclass"));
			}
		}

		return superclasses;
	}

	public void generateClass(File targetDir, Map<URI, String> pkgs) {
		final PrintStream ps = System.out;

		ps.println("package " + pkgs.get(this.uri) + ";");
		ps.println();
		printImports(ps, pkgs);
		ps.println();
		printClassComment(ps);
		ps.print("public class " + this.uri.getLocalName() + " ");
		if (this.superclasses.size() > 0) {
			if (superclasses.size() > 1)
				throw new UnsupportedOperationException("multiple inheritance not supported yet");
			ps.print("extends " + this.superclasses.get(0).getLocalName() + " ");
		}
		ps.println("{\n");

		ps.println("}\n");
	}

	private void printClassComment(PrintStream ps) {
		ps.println("/**");
		if (this.comment == null) {
			ps.println(" * " + this.uri);
		} else {
			final String cmt = WordUtils.wrap(" * " + comment.replaceAll("\\r?\\n", " "), 80, "\n * ", false);
			ps.println(" " + cmt);
		}
		ps.println(" */");
	}

	private void printImports(PrintStream ps, Map<URI, String> pkgs) {
		final Set<String> imports = new HashSet<String>();

		for (final URI sc : superclasses) {
			imports.add(pkgs.get(sc));
		}

		imports.remove(pkgs.get(this.uri));

		final String[] sortedImports = imports.toArray(new String[imports.size()]);
		Arrays.sort(sortedImports);

		for (final String imp : sortedImports) {
			ps.println("import " + imp + ";");
		}
	}
}
