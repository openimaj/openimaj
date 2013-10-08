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
