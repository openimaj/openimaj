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
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class HttpUtilsTest {
	protected Logger logger = Logger.getLogger(HttpUtilsTest.class);

	@Test
	public void testRedirect() throws MalformedURLException, IOException{
		String[][] links = new String[][]{
			new String[]{"http://t.co/kp7qdeL6","http://www.openrightsgroup.org/press/releases/bruce-willis-right-to-challenge-apple#copyright"},
			new String[]{"http://t.co/VJn1ISBl","http://ow.ly/1mgxj1", "http://www.electronicsweekly.com/Articles/2012/09/03/54467/raspberry-pi-goes-to-cambridge-to-get-free-os.htm"}
		};
		for (String[] link: links) {
			final String[] expecting = Arrays.copyOfRange(link, 1, link.length);
			String firstLink = link[0];
			HttpUtils.readURLAsByteArrayInputStream(new URL(firstLink),new HttpUtils.MetaRefreshRedirectStrategy(){
				int redirected = 0;

				@Override
				public boolean isRedirected(HttpRequest request, HttpResponse response, HttpContext context)
						throws ProtocolException
				{
					boolean isRedirect = super.isRedirected(request, response, context);
					if(redirected < expecting.length){
						assertTrue(isRedirect);
						HttpUriRequest redirect = this.getRedirect(request, response, context);
						RedirectLocations redirectLocations = (RedirectLocations) context.getAttribute(REDIRECT_LOCATIONS);
						if(redirectLocations!=null)
							redirectLocations.remove(redirect.getURI());
						String uriString = redirect.getURI().toString();
						assertEquals(expecting[redirected++],uriString);
					}
					return isRedirect;
				}
			});
		}
	}
}
