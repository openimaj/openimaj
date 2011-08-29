package org.openimaj.io;

import java.io.File;

public class FileUtils {
	/**
	 * Recursively delete a directory
	 * @param dir
	 * @return
	 */
	public static boolean deleteRecursive(File dir) {
	    if (dir.isDirectory()) {
	        String[] children = dir.list();
	        for (int i=0; i<children.length; i++) {
	            boolean success = deleteRecursive(new File(dir, children[i]));
	            if (!success) {
	                return false;
	            }
	        }
	    }

	    // The directory is now empty so delete it
	    return dir.delete();
	}
}
