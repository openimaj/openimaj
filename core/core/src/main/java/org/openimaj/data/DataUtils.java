package org.openimaj.data;

import java.io.File;

/**
 * Utility functions for dealing with data
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 */
public class DataUtils {
	private static final String DEFAULT_DATA_DIRECTORY_NAME = "Data";
	private static final String USER_HOME = "user.home";
	private static final String OPENIMAJ_DATA_DIR = "openimaj.data.dir";

	private DataUtils() {
	}

	/**
	 * Get the openimaj data directory. This is where things like datasets are
	 * stored. This can be controlled by the <code>openimaj.data.dir</code>
	 * system property. If the property is unset, then this defaults to
	 * <code>$HOME/Data</code> where <code>$HOME</code> is the home directory of
	 * the user who owns the current JVM process.
	 * 
	 * @return The data directory location
	 */
	public static File getDataDirectory() {
		File dataDir = null;

		if (System.getProperty(OPENIMAJ_DATA_DIR) != null) {
			dataDir = new File(System.getProperty(OPENIMAJ_DATA_DIR));
		} else {
			final String userHome = System.getProperty(USER_HOME);
			dataDir = new File(userHome, DEFAULT_DATA_DIRECTORY_NAME);
		}

		return dataDir;
	}

	/**
	 * Get the location of the given data file/directory. This will be relative
	 * to the openimaj data directory given by {@link #getDataDirectory()}.
	 * 
	 * @param relativePath
	 *            the relative path the to data
	 * 
	 * @see DataUtils#getDataDirectory()
	 * 
	 * @return The data location
	 */
	public static File getDataLocation(File relativePath) {
		return getDataLocation(relativePath.getPath());
	}

	/**
	 * Get the location of the given data file/directory. This will be relative
	 * to the openimaj data directory given by {@link #getDataDirectory()}.
	 * 
	 * @param relativePath
	 *            the relative path the to data
	 * 
	 * @see DataUtils#getDataDirectory()
	 * 
	 * @return The data location
	 */
	public static File getDataLocation(String relativePath) {
		return new File(getDataDirectory(), relativePath);
	}
}
