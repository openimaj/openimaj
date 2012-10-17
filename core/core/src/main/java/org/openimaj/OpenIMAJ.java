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
package org.openimaj;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import org.openimaj.citation.annotation.Reference;
import org.openimaj.citation.annotation.ReferenceType;

/**
 * Useful utility methods to get things such as the current OpenIMAJ version.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
@Reference(
		type = ReferenceType.Inproceedings,
		author = { "Jonathan Hare", "Sina Samangooei", "David Dupplaw" },
		title = "OpenIMAJ and ImageTerrier: Java Libraries and Tools for Scalable Multimedia Analysis and Indexing of Images",
		year = "2011",
		booktitle = "ACM Multimedia 2011",
		pages = { "691", "694" },
		url = "http://eprints.soton.ac.uk/273040/",
		note = " Event Dates: 28/11/2011 until 1/12/2011",
		month = "November",
		publisher = "ACM")
public class OpenIMAJ {
	static {
		String versionString;

		try {
			final InputStream is = OpenIMAJ.class.getResourceAsStream("OpenIMAJ.properties");
			final Properties props = new Properties();

			props.load(is);
			versionString = props.getProperty("version");
		} catch (final IOException e) {
			versionString = "unknown";
		}

		version = versionString;
	}

	private static final String version;

	private OpenIMAJ() {
	}

	/**
	 * Get the OpenIMAJ version currently being used
	 * 
	 * @return the OpenIMAJ version
	 */
	public static String getVersion() {
		return version;
	}

	/**
	 * Get a stream to the OpenIMAJ logo.
	 * 
	 * @return a stream to the OpenIMAJ logo.
	 */
	public static InputStream getLogoAsStream() {
		return OpenIMAJ.class.getResourceAsStream("OpenIMAJ.png");
	}

	/**
	 * Get a URL to the OpenIMAJ logo. This is likely to be a jar:// url so
	 * can't be used outside the Java program.
	 * 
	 * @return the OpenIMAJ logo URL
	 */
	public static URL getLogoAsURL() {
		return OpenIMAJ.class.getResource("OpenIMAJ.png");
	}
}
