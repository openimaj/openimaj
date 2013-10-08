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
