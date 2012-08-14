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
