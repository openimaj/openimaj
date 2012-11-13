package org.openimaj.rdf.owl2java;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.WordUtils;
import org.openimaj.rdf.owl2java.PropertyDef.PropertyType;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.URIImpl;
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
 *	Represents the definition of an ontology class.
 *
 *	@author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *  @created 29 Oct 2012
 *	@version $Author$, $Revision$, $Date$
 */
public class ClassDef
{
	/** The description of the class from the RDF comment */
	protected String comment;

	/** The URI of the class */
	protected URI uri;

	/** List of the superclasses to this class */
	protected List<URI> superclasses;

	/** List of the properties in this class */
	protected List<PropertyDef> properties;
	
	/**
	 * 	Outputs the Java class definition for this class def
	 *
	 *	{@inheritDoc}
	 * 	@see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return "class " + this.uri.getLocalName() + " extends " +
				this.superclasses + " {\n" + "\t" + this.properties + "\n}\n";
	}

	/**
	 *	Loads all the class definitions from the given repository
	 *
	 *	@param conn The repository connection from where to get the classes
	 *	@return a Map that maps class URIs to ClassDef objects
	 *	@throws RepositoryException
	 *	@throws MalformedQueryException
	 *	@throws QueryEvaluationException
	 */
	public static Map<URI,ClassDef> loadClasses( final RepositoryConnection conn )
		throws RepositoryException, MalformedQueryException, QueryEvaluationException
	{
		final HashMap<URI,ClassDef> classes = new HashMap<URI, ClassDef>();

		// This is the types we'll look for
		// We'll look for both OWL and RDF classes
		final String[] clzTypes = {
				"<http://www.w3.org/2002/07/owl#Class>",
				"rdfs:Class"
		};

		// Loop over the namespaces
		for( final String clzType : clzTypes )
		{
			// Create a query to get the classes
			final String query = "SELECT Class, Comment "
					+ "FROM {Class} rdf:type {" + clzType + "}; "
					+ " [ rdfs:comment {Comment} ]";

			// Prepare the query...
			final TupleQuery preparedQuery = conn.prepareTupleQuery(
					QueryLanguage.SERQL, query );

			// Run the query...
			final TupleQueryResult res = preparedQuery.evaluate();

			// Loop over the results
			while( res.hasNext() )
			{
				final BindingSet bindingSet = res.next();

				// If we have a class with a URI...
				if( bindingSet.getValue("Class") instanceof URI )
				{
					// Create a new class definition for it
					final ClassDef clz = new ClassDef();

					// Get the comment, if there is one.
					if( bindingSet.getValue("Comment") != null )
					{
						final MemLiteral lit = (MemLiteral)
								bindingSet.getValue("Comment");
						clz.comment = lit.stringValue();
					}

					clz.uri = (URI) bindingSet.getValue("Class");
					clz.superclasses = ClassDef.getSuperclasses( clz.uri, conn );
					clz.properties   = PropertyDef.loadProperties( clz.uri, conn );

					// Check whether there are any other classes
					ClassDef.getEquivalentClasses( clz, conn );
					
					classes.put( clz.uri, clz );
				}
			}
		}
		return classes;
	}

	/**
	 * 	Checks for owl:equivalentClass and updates the class definition based
	 * 	on whats found.
	 *	@param clz the class definition
	 *	@param conn The connection to the repository
	 */
	private static void getEquivalentClasses( final ClassDef clz, final RepositoryConnection conn )
	{
		try
		{
			final String sparql = "prefix owl: <http://www.w3.org/2002/07/owl#> "+
					"SELECT ?clazz WHERE " +
					"{ <"+clz.uri+"> owl:equivalentClass ?clazz . }";

			System.out.println( sparql );
			
			// Prepare the query...
			final TupleQuery preparedQuery = conn.prepareTupleQuery(
					QueryLanguage.SPARQL, sparql );

			// Run the query...
			final TupleQueryResult res = preparedQuery.evaluate();
			
			// Loop through the results (if any)
			while( res.hasNext() )
			{
				final BindingSet bs = res.next();
				
				final Value clazz = bs.getBinding("clazz").getValue();
				
				// If it's an equivalent then we'll simply make this class
				// a subclass of the equivalent class. 
				// TODO: There is a possibility that we could end up with a cycle here
				// and the resulting code would not compile.
				if( clazz instanceof URI )
					clz.superclasses.add( (URI)clazz );
				else
				// If it's a BNode, then the BNode defines the equivalence.
				if( clazz instanceof MemBNode )
				{
					final MemBNode b = (MemBNode)clazz;
					final MemStatementList sl = b.getSubjectStatementList();
					
					for( int i = 0; i < sl.size(); i++ )
					{
						final MemStatement x = sl.get(i);
						System.out.println( "    -> "+x );
					}
				}
			}
			
			res.close();
		}
		catch( final RepositoryException e )
		{
			e.printStackTrace();
		}
		catch( final MalformedQueryException e )
		{
			e.printStackTrace();
		}
		catch( final QueryEvaluationException e )
		{
			e.printStackTrace();
		}
	}

	/**
	 *	Retrieves the superclass list for the given class URI using the given
	 *	repository
	 *
	 *	@param uri The URI of the class to find the superclasses of
	 *	@param conn The respository
	 *	@return A list of URIs of superclasses
	 *
	 *	@throws RepositoryException
	 *	@throws MalformedQueryException
	 *	@throws QueryEvaluationException
	 */
	private static List<URI> getSuperclasses( final URI uri, final RepositoryConnection conn )
				throws RepositoryException, MalformedQueryException, QueryEvaluationException
	{
		// SPARQL query to get the superclasses
		final String query = "SELECT ?superclass WHERE { "+
				"<" + uri.stringValue() + "> "+
				"<http://www.w3.org/2000/01/rdf-schema#subClassOf> "+
				"?superclass. }";

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

	/**
	 * 	Generates a Java file in the target directory
	 *
	 *	@param targetDir The target directory
	 *	@param pkgs A map of package mappings to class URIs
	 * 	@param classes A map of class URIs to ClassDefs
	 * 	@param flattenClassStructure Whether to flatten the class structure
	 * 	@param generateAnnotations Whether to generate OpenIMAJ RDF annotations
	 * 		for the properties
	 * @param separateImplementations
	 * @throws FileNotFoundException
	 */
	public void generateClass( final File targetDir, final Map<URI, String> pkgs,
			final Map<URI, ClassDef> classes, final boolean flattenClassStructure,
			final boolean generateAnnotations, final boolean separateImplementations ) throws FileNotFoundException
	{
		// We don't need to generate an implementation file if there are no
		// properties to get/set
		if( this.properties.size() == 0 )
			return;

		// Generate the filename for the output file
		final File path = new File( targetDir.getAbsolutePath() + File.separator +
				pkgs.get(this.uri).replace( ".", File.separator ) +
				(separateImplementations?File.separator+"impl":"") );
		path.mkdirs();
		final PrintStream ps = new PrintStream( new File( path.getAbsolutePath()
				+ File.separator + Generator.getTypeName( this.uri ) + "Impl.java") );

		// Output the package definition
		ps.println("package " + pkgs.get(this.uri) +
				(separateImplementations?".impl":"")+";");
		ps.println();

		// Output the imports
		if( separateImplementations )
			ps.println( "import "+pkgs.get(this.uri)+".*;" );
		if( generateAnnotations )
			ps.println( "import org.openimaj.rdf.serialize.Predicate;\n");
		this.printImports( ps, pkgs, true, classes );
		ps.println();

		// Output the comment at the top of the class
		this.printClassComment(ps);

		// Output the class
		ps.print("public class " + Generator.getTypeName( this.uri ) + "Impl ");

		// It will implement the interface that defines it
		ps.print( "implements "+Generator.getTypeName( this.uri ) );

		if (this.superclasses.size() > 0)
		{
			// ...and any of the super class interfaces
			for( final URI superclass : this.superclasses )
			{
				ps.print(", ");
				ps.print( Generator.getTypeName( superclass ) );
			}
		}
		ps.println("\n{");

		// Output the definition of the class
		this.printClassPropertyDefinitions( ps, classes,
				flattenClassStructure, generateAnnotations );

		ps.println("}\n");
	}

	/**
	 * 	Generates a Java interface file in the target directory
	 *
	 *	@param targetDir The target directory
	 *	@param pkgs A list of package mappings to class URIs
	 * 	@param classes The URI to class definition map.
	 * 	@throws FileNotFoundException
	 */
	public void generateInterface( final File targetDir, final Map<URI, String> pkgs,
			final Map<URI, ClassDef> classes ) throws FileNotFoundException
	{
		final File path = new File( targetDir.getAbsolutePath() + File.separator +
				pkgs.get(this.uri).replace( ".", File.separator ) );
		path.mkdirs();
		final PrintStream ps = new PrintStream( new File( path.getAbsolutePath()
				+ File.separator + Generator.getTypeName( this.uri ) + ".java") );

		ps.println("package " + pkgs.get(this.uri) + ";");
		ps.println();
		this.printImports( ps, pkgs, false, classes );
		ps.println();

		this.printClassComment(ps);

		ps.print("public interface " + Generator.getTypeName( this.uri ) + " ");
		ps.println("\n{");
		this.printInterfacePropertyDefinitions( ps );
		ps.println("}\n");
	}

	/**
	 * 	Prints the comment at the top of the file for this class.
	 *
	 *	@param ps The stream to print the comment to.
	 */
	private void printClassComment( final PrintStream ps )
	{
		ps.println("/**");
		if (this.comment == null) {
			ps.println(" * " + this.uri);
		} else {
			final String cmt = WordUtils.wrap(" * " + this.comment.replaceAll("\\r?\\n", " "), 80, "\n * ", false);
			ps.println(" " + cmt);
		}
		ps.println(" */");
	}

	/**
	 * 	Outputs the list of imports necessary for this class.
	 *
	 *	@param ps The stream to print the imports to
	 *	@param pkgs The list of package mappings for all the known classes
	 *	@param superclasses Whether to print imports for superclasses
	 */
	private void printImports( final PrintStream ps, final Map<URI, String> pkgs, 
			final boolean superclasses, final Map<URI,ClassDef> classes )
	{
		final Set<String> imports = new HashSet<String>();

		if( superclasses )
		{
			for( final URI sc : this.superclasses )
			{
				for( final PropertyDef p : classes.get(sc).properties )
					if( p.needsImport() != null )
						imports.add( p.needsImport() );
				imports.add( pkgs.get(sc) );
			}
		}
		
		for( final PropertyDef p : this.properties )
			if( p.needsImport() != null )
				imports.add( p.needsImport() );

		imports.remove( pkgs.get(this.uri) );

		final String[] sortedImports = imports.toArray(new String[imports.size()]);
		Arrays.sort(sortedImports);

		for (final String imp : sortedImports) {
			ps.println("import " + imp + ".*;");
		}
	}

	/**
	 *	Outputs all the properties into the class definition.
	 *
	 *	@param ps The stream to print to.
	 */
	private void printInterfacePropertyDefinitions( final PrintStream ps )
	{
		for( final PropertyDef p : this.properties )
			ps.println( p.toSettersAndGetters( "\t", false, null ) );
	}

	/**
	 *	Outputs all the properties into the class definition.
	 *
	 *	@param ps The stream to print to.
	 * 	@param classes A map of class URIs to ClassDefs
	 * 	@param flattenClassStructure Whether to combine all the properties from
	 * 		all the superclasses into this class (TRUE), or whether to use instance
	 * 		pointers to classes of that type (FALSE)
	 * @param generateAnnotations
	 */
	private void printClassPropertyDefinitions( final PrintStream ps,
			final Map<URI, ClassDef> classes, final boolean flattenClassStructure,
			final boolean generateAnnotations )
	{
		if( flattenClassStructure )
		{
			// Work out all the properties to output
			final List<PropertyDef> pd = new ArrayList<PropertyDef>();
			pd.addAll( this.properties );
			for( final URI superclass : this.superclasses )
				pd.addAll( classes.get( superclass ).properties );

			// Output all the property definitions for this class.
			for( final PropertyDef p : pd )
				ps.println( p.toJavaDefinition("\t",generateAnnotations) );
			ps.println();
			// Output all the getters and setters for this class.
			for( final PropertyDef p : pd )
				ps.println( p.toSettersAndGetters( "\t", true, null ) );
		}
		else
		{
			// Output all the property definitions for this class.
			for( final PropertyDef p : this.properties )
				ps.println( p.toJavaDefinition("\t",generateAnnotations) );
			ps.println();

			// Now we need to output the links to other objects from which
			// this class inherits. While we do that, we'll also remember which
			// properties we need to delegate to the other objects.
			final HashMap<String,List<PropertyDef>> pd = new HashMap<String, List<PropertyDef>>();
			for( final URI superclass : this.superclasses )
			{
				// We don't need the instance variable if we're not inheriting
				// any properties from the superclass.
				if( classes.get(superclass).properties.size() == 0 )
					continue;
				
				final String instanceName =
						superclass.getLocalName().substring(0,1).toLowerCase()+
						superclass.getLocalName().substring(1);

				pd.put( instanceName, classes.get(superclass).properties );

				ps.println( "\t/** "+superclass.getLocalName()+" instance */" );
				ps.println( "\tprivate "+Generator.getTypeName( superclass )
						+" "+instanceName+";\n" );
			}

			ps.println( "\n\t// From class "+this.uri.getLocalName()+"\n\n" );
			
			// Output the property getters and setters for this class
			for( final PropertyDef p : this.properties )
				ps.println( p.toSettersAndGetters( "\t", true, null ) );

			// Now output the delegated getters and setters for this class
			for( final String instanceName : pd.keySet() )
			{
				ps.println( "\n\t// From class "+instanceName+"\n\n" );
				for( final PropertyDef p : pd.get(instanceName) )
					ps.println( p.toSettersAndGetters( "\t", true, instanceName ) );
			}
		}

		// We always inject a "instanceURI" field for storing the actual URI
		// of an instance of the given class.
		final PropertyDef iupd = new PropertyDef();
		iupd.type = PropertyType.DATATYPE;
		iupd.uri = new URIImpl("http://onto.arcomem.eu/#URI");
		ps.println( "\n\t// Added to all classes\n\n" );
		ps.println( iupd.toJavaDefinition( "\t", generateAnnotations ) );
		ps.println();
		ps.println( iupd.toSettersAndGetters( "\t", true, null ) );
		ps.println();	
	}
}
