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
