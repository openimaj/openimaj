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
package org.openimaj.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolException;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.RedirectLocations;
import org.apache.http.protocol.HttpContext;
import org.apache.log4j.Logger;
import org.junit.Test;

/**
 * Tests for the {@link HttpUtils}
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 */
public class HttpUtilsTest {
	protected Logger logger = Logger.getLogger(HttpUtilsTest.class);

	/**
	 * Test that the redirection handler is working
	 * 
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	@Test
	public void testRedirect() throws MalformedURLException, IOException {
		final String[][] links = new String[][] {
				new String[] { "http://t.co/kp7qdeL6", "http://www.openrightsgroup.org/press/releases/bruce-willis-right-to-challenge-apple#copyright" },
				new String[] { "http://t.co/VJn1ISBl", "http://ow.ly/1mgxj1", "http://www.electronicsweekly.com/Articles/2012/09/03/54467/raspberry-pi-goes-to-cambridge-to-get-free-os.htm" }
		};
		for (final String[] link : links) {
			final String[] expecting = Arrays.copyOfRange(link, 1, link.length);
			final String firstLink = link[0];
			HttpUtils.readURLAsByteArrayInputStream(new URL(firstLink), new HttpUtils.MetaRefreshRedirectStrategy() {
				int redirected = 0;

				@Override
				public boolean isRedirected(HttpRequest request, HttpResponse response, HttpContext context)
						throws ProtocolException
				{
					final boolean isRedirect = super.isRedirected(request, response, context);
					if (redirected < expecting.length) {
						assertTrue(isRedirect);
						final HttpUriRequest redirect = this.getRedirect(request, response, context);
						final RedirectLocations redirectLocations = (RedirectLocations) context
								.getAttribute(REDIRECT_LOCATIONS);
						if (redirectLocations != null)
							redirectLocations.remove(redirect.getURI());
						final String uriString = redirect.getURI().toString();
						assertEquals(expecting[redirected++], uriString);
					}
					return isRedirect;
				}
			});
		}
	}
}
