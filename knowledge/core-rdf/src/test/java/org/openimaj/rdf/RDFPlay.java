/**
 * 
 */
package org.openimaj.rdf;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.openimaj.rdf.serialize.Predicate;
import org.openimaj.rdf.serialize.RDFSerializer;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.ntriples.NTriplesWriter;

/**
 * 
 * 
 * @author David Dupplaw (dpd@ecs.soton.ac.uk)
 * @created 16 Apr 2013
 * @version $Author$, $Revision$, $Date$
 */
public class RDFPlay
{
	/**
	 * 
	 * @author David Dupplaw (dpd@ecs.soton.ac.uk)
	 * @created 16 Apr 2013
	 * @version $Author$, $Revision$, $Date$
	 */
	public static class MyClass
	{
		/** */
		@Predicate("http://myclass/hasArrayString")
		public String[] arrayString =
		{
				"item 1", "item 2", "item 3"
		};

		/** */
		@Predicate("http://myclass/hasListString")
		public List<String> listString = new
				ArrayList<String>(Arrays.asList(arrayString));

		@Override
		public String toString()
		{
			return "MC{a:" + Arrays.toString(arrayString) + ",l:" + listString + "}";
		}
	}

	/**
	 * 
	 * @author David Dupplaw (dpd@ecs.soton.ac.uk)
	 * @created 16 Apr 2013
	 */
	public static class MyOtherClass
	{
		/** */
		@Predicate("http://myclass/hasMyClass")
		public MyClass myClass = new MyClass();

		/** */
		@Predicate("http://myclass/hasListOfMyClass")
		public List<MyClass> myClassList = new ArrayList<MyClass>(
				Arrays.asList(new MyClass[] { new MyClass(), new MyClass() }));

		/** */
		@Predicate("http://myclass/hasCollection")
		public MyCollectionClass collection = new MyCollectionClass();

		/** */
		@Predicate("http://myclass/listCollection")
		public List<MyCollectionClass> listCollection =
				new ArrayList<MyCollectionClass>(
						Arrays.asList(
								new MyCollectionClass[] { new MyCollectionClass() }));

		@Override
		public String toString()
		{
			return "OC{\n\tmyClass: " + myClass + ",\n\tmyClassList: " + myClassList + ",\n\tcollection: " + collection
					+ ",\n\tlistCollection: " + listCollection + "\n}";
		}
	}

	/**
	 * 
	 * @author David Dupplaw (dpd@ecs.soton.ac.uk)
	 * @created 16 Apr 2013
	 */
	public static class MyCollectionClass extends ArrayList<String>
	{
		/** */
		public MyCollectionClass()
		{
			add("Collection Item 1");
			add("Collection Item 2");
		}

		/** */
		private static final long serialVersionUID = 1L;

		/** */
		@Predicate("http://mycollection/status")
		public String status = "avoid";

		@Override
		public String toString()
		{
			return "MCC{" + status + " -> " + super.toString() + "}";
		}
	}

	/** */
	@Test
	public void testRDF()
	{
		try
		{
			final MyOtherClass oc = new MyOtherClass();
			System.out.println("The class:\n" + oc);

			final StringWriter sw = new StringWriter();
			final NTriplesWriter tw = new NTriplesWriter(sw);
			tw.startRDF();
			new RDFSerializer(false)
			{
				@Override
				public void addTriple(org.openrdf.model.Statement t)
				{
					try
					{
						tw.handleStatement(t);
					} catch (final RDFHandlerException e)
					{
						e.printStackTrace();
					}
				};
			}.serialize(oc, "http://myclass/");
			tw.endRDF();

			System.out.println(sw);

			// Now unserialize
			final MyOtherClass oc2 = new MyOtherClass();
			oc2.collection = null;
			oc2.listCollection = null;
			oc2.myClass = null;
			oc2.myClassList = null;
			new RDFSerializer(false).unserialize(
					oc2, "http://myclass/", sw.toString(), RDFFormat.TURTLE);

			System.out.println("Unserialized:\n" + oc2);

			// If they're the same, the toString() methods will be the same.
			Assert.assertEquals(oc.toString(), oc2.toString());
		} catch (final RDFHandlerException e)
		{
			e.printStackTrace();
		}

	}
}
