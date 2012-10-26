package org.openimaj.rdf.owl2java;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openrdf.model.URI;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.sail.memory.MemoryStore;

public class Generator {
	protected Map<URI, String> generatePackageMappings(List<ClassDef> classes) {
		final Map<URI, String> packageMapping = new HashMap<URI, String>();

		for (final ClassDef cd : classes) {
			if (!packageMapping.containsKey(cd.uri)) {
				packageMapping.put(cd.uri, getPackageName(cd.uri));
			}
		}
		return packageMapping;
	}

	protected String getPackageName(URI uri) {
		String ns = uri.getNamespace();

		if (ns.contains("//"))
			ns = ns.substring(ns.indexOf("//") + 2);

		if (!ns.contains("/"))
			return ns;

		String last = ns.substring(ns.indexOf("/"));
		if (last.contains("#"))
			last = last.substring(0, last.indexOf("#"));
		if (last.contains("."))
			last = last.substring(0, last.indexOf("."));

		last = last.replace("/", ".");

		String first = ns.substring(0, ns.indexOf("/"));

		final String[] parts = first.split("\\.");
		first = "";
		for (int i = parts.length - 1; i >= 0; i--) {
			first += parts[i];
			if (i != 0)
				first += ".";
		}

		return first + last;
	}

	public static void main(String[] args) throws RepositoryException {
		final File rdfFile = new File("/Users/jsh2/Downloads/arcomem-data-model.owl");
		final File targetDir = new File("/Users/jsh2/Desktop/test");

		targetDir.mkdirs();

		final Repository repository = new SailRepository(new MemoryStore());
		repository.initialize();

		RepositoryConnection conn = null;
		try {
			conn = repository.getConnection();
			conn.add(rdfFile, "", RDFFormat.RDFXML);

			final List<ClassDef> classes = ClassDef.loadClasses(conn);

			final Generator g = new Generator();
			final Map<URI, String> pkgs = g.generatePackageMappings(classes);

			for (final ClassDef cd : classes) {
				cd.generateClass(targetDir, pkgs);
			}

		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

}
