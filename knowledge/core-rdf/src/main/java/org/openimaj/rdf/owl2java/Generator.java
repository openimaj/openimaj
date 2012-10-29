package org.openimaj.rdf.owl2java;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.openrdf.model.URI;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.sail.memory.MemoryStore;

/**
 *
 *
 *
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * @created 29 Oct 2012
 * @version $Author$, $Revision$, $Date$
 */
public class Generator
{
	/**
	 * Creates a cache of mappings that map URIs to package names.
	 *
	 * @param classes
	 *            The list of classes to generate the package names for
	 * @return A map of class URI to package name
	 */
	protected static Map<URI, String> generatePackageMappings(
	        final Collection<ClassDef> classes )
	{
		final Map<URI, String> packageMapping = new HashMap<URI, String>();

		for( final ClassDef cd : classes )
		{
			if( !packageMapping.containsKey( cd.uri ) )
			{
				packageMapping.put( cd.uri, Generator.getPackageName( cd.uri ) );
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
	protected static String getPackageName( final URI uri )
	{
		String ns = uri.getNamespace();

		if( ns.contains( "//" ) ) ns = ns.substring( ns.indexOf( "//" ) + 2 );

		if( !ns.contains( "/" ) ) return ns;

		String last = ns.substring( ns.indexOf( "/" )+1 );

		if( last.contains( "#" ) )
			last = last.substring( 0, last.indexOf( "#" ) );

		if( last.contains( "." ) )
			last = last.substring( 0, last.indexOf( "." ) );

		last = last.replace( "/", "." );
		last = last.replace( "-", "_" );

		String first = ns.substring( 0, ns.indexOf( "/" ) );

		final String[] parts = first.split( "\\." );
		first = "";
		for( int i = parts.length - 1; i >= 0; i-- )
		{
			if( parts[i].charAt(0) < 65 || parts[i].charAt(0) > 122 )
				first += "_";

			first += parts[i];
			if( i != 0 ) first += ".";
		}

		String lastBit = "";
		if( last.indexOf( "." ) == -1 )
			lastBit = last;
		else
		{
			for( final String s : last.split( "\\." ) )
			{
				lastBit += ".";
				if( s.charAt(0) < 65 || s.charAt(0) > 122 )
						lastBit += "_"+s;
				else	lastBit += s;
			}
		}

		return first + lastBit;
	}

	/**
	 *
	 * @param args
	 * @throws RepositoryException
	 */
	public static void main( final String[] args ) throws RepositoryException
	{
		if( args.length < 2 )
		{
			System.out.println( "Usage: Generator <RDF-File> <Target-Directory>");
			System.exit(1);
		}

		final File rdfFile = new File( args[0] );
		final File targetDir = new File( args[1] );

		if( !rdfFile.exists() )
		{
			System.out.println( "The RDF file does not exist: " + rdfFile );
			System.exit( 1 );
		}

		if( !targetDir.exists() )
		{
			System.out.println( "The target directory does not exist: "
			        + targetDir );
			System.exit( 1 );
		}

		// Create a new memory store into which we'll plonk all the RDF
		final Repository repository = new SailRepository( new MemoryStore() );
		repository.initialize();

		try
		{
			// Plonk all the RDF into the store
			final RepositoryConnection conn = repository.getConnection();
			conn.add( rdfFile, "", RDFFormat.RDFXML );

			// Now we'll get all the classes from the ontology
			final Map<URI, ClassDef> classes = ClassDef.loadClasses( conn );

			// Try to generate the package mappings for the classes
			final Map<URI, String> pkgs = Generator.generatePackageMappings(
					classes.values() );

			// Now we'll go through each of the class definitions and generate
			// interfaces and classes
			for( final ClassDef cd : classes.values() )
			{
				cd.generateInterface( targetDir, pkgs, classes );
				cd.generateClass( targetDir, pkgs, classes, true, true, true );
			}
		}
		catch( final Exception e )
		{
			e.printStackTrace();
		}
	}
}
