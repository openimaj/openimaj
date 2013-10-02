/**
 * 
 */
package org.openimaj.experiment.evaluation.agreement;

import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Test;

/**
 * 
 * 
 * @author David Dupplaw (dpd@ecs.soton.ac.uk)
 * @created 14 Aug 2013
 * @version $Author$, $Revision$, $Date$
 */
public class CohensKappaTest
{
	protected enum Response
	{
		YES,
		NO
	}

	private Response[][] ratings = {
			{ Response.YES, Response.YES },
			{ Response.YES, Response.YES },
			{ Response.YES, Response.YES },
			{ Response.YES, Response.YES },
			{ Response.YES, Response.YES },
			{ Response.YES, Response.YES },
			{ Response.YES, Response.YES },
			{ Response.YES, Response.YES },
			{ Response.YES, Response.YES },
			{ Response.YES, Response.YES },
			{ Response.YES, Response.YES },
			{ Response.YES, Response.YES },
			{ Response.YES, Response.YES },
			{ Response.YES, Response.YES },
			{ Response.YES, Response.YES },
			{ Response.YES, Response.YES },
			{ Response.YES, Response.YES },
			{ Response.YES, Response.YES },
			{ Response.YES, Response.YES },
			{ Response.YES, Response.YES },

			{ Response.NO, Response.NO },
			{ Response.NO, Response.NO },
			{ Response.NO, Response.NO },
			{ Response.NO, Response.NO },
			{ Response.NO, Response.NO },
			{ Response.NO, Response.NO },
			{ Response.NO, Response.NO },
			{ Response.NO, Response.NO },
			{ Response.NO, Response.NO },
			{ Response.NO, Response.NO },
			{ Response.NO, Response.NO },
			{ Response.NO, Response.NO },
			{ Response.NO, Response.NO },
			{ Response.NO, Response.NO },
			{ Response.NO, Response.NO },

			{ Response.YES, Response.NO },
			{ Response.YES, Response.NO },
			{ Response.YES, Response.NO },
			{ Response.YES, Response.NO },
			{ Response.YES, Response.NO },

			{ Response.NO, Response.YES },
			{ Response.NO, Response.YES },
			{ Response.NO, Response.YES },
			{ Response.NO, Response.YES },
			{ Response.NO, Response.YES },
			{ Response.NO, Response.YES },
			{ Response.NO, Response.YES },
			{ Response.NO, Response.YES },
			{ Response.NO, Response.YES },
			{ Response.NO, Response.YES },
	};

	/**
	 * Tests the Cohen's Kappa interrater agreement calculation by using the
	 * example given on the Wikipedia page.
	 * 
	 * @see "http://en.wikipedia.org/wiki/Cohen's_kappa"
	 */
	@Test
	public void test()
	{
		// We set up the same experiment as is the example experiment
		// on the wikipedia page for Cohen's Kappa:
		final Map<Integer, Response> rater1 =
				new HashMap<Integer, Response>();
		final Map<Integer, Response> rater2 =
				new HashMap<Integer, Response>();

		int i = 0;
		for (final Response[] r : ratings)
		{
			rater1.put(i, r[0]);
			rater2.put(i, r[1]);
			i++;
		}

		Assert.assertEquals(0.4d,
				CohensKappaInterraterAgreement.calculate(rater1, rater2), 0.01);
	}
}
