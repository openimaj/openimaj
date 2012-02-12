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
package org.openimaj.hadoop.sequencefile.utils;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;

/**
 * <tt>MimeTypeUtils</tt> provides the 
 * {@link #fileExtensionForMIMEType fileExtensionForMIMEType()} method, which
 * converts a MIME type to a file extension. That method uses a traditional
 * <tt>mime.types</tt> files, similar to the file shipped with with web
 * servers such as Apache. It looks for a suitable file in the following
 * locations:
 * <ol>
 * <li> First, it looks for the file <tt>.mime.types</tt> in the user's home
 * directory.
 * <li> Next, it looks for <tt>mime.types</tt> (no leading ".") in all the
 * directories in the CLASSPATH
 * </ol>
 * <p>
 * It loads all the matching files it finds; the first mapping found for a given
 * MIME type is the one that is used. The files are only loaded once within a
 * given running Java VM.
 * </p>
 * 
 * <p>This class is derived from org.clapper.util.misc.MIMETypeUtil, originally 
 * authored by Brian M. Clapper and made available under a BSD-style license.</p>
 * 
 * @author Brian M. Clapper
 * @author Edwin Shin *
 * @version $Id: MimeTypeUtils.java 8082 2009-06-08 20:46:32Z aawoods $
 */
public class MimeTypeUtils {

    /**
     * Default MIME type, when a MIME type cannot be determined from a file's
     * extension.
     */
    public static final String DEFAULT_MIME_TYPE = "application/octet-stream";

    /** Logger for this class. */
    private static final Logger LOG = Logger.getLogger(MimeTypeUtils.class);

    /**
     * Table for converting MIME type strings to file extensions. The table is
     * initialized the first time fileExtensionForMIMEType() is called.
     */
    private static Map<String, String> mimeTypeToExtensionMap = null;

    /**
     * Resource bundle containing MIME type mappings
     */
    private static final String MIME_MAPPINGS_BUNDLE = "org.openimaj.hadoop.sequencefile.MIMETypes";

    private MimeTypeUtils() {
        // Can't be instantiated
    }

    /**
     * Get an appropriate extension for a MIME type.
     * 
     * @param mimeType
     *        the String MIME type
     * @return the appropriate file name extension, or a default extension if
     *         not found. The extension will not have the leading "." character.
     */
    public static String fileExtensionForMIMEType(String mimeType) {
        loadMappings();

        String ext = (String) mimeTypeToExtensionMap.get(mimeType);

        if (ext == null) ext = "dat";

        return ext;
    }

    /**
     * Load the MIME type mappings into memory.
     */
    private static synchronized void loadMappings() {
        if (mimeTypeToExtensionMap != null) return;

        mimeTypeToExtensionMap = new HashMap<String, String>();

        // First, check the user's home directory.

        String fileSep = System.getProperty("file.separator");
        StringBuffer buf = new StringBuffer();

        buf.append(System.getProperty("user.home"));
        buf.append(fileSep);
        buf.append(".mime.types");

        loadMIMETypesFile(buf.toString());

        // Now, check every directory in the classpath.

        String pathSep = System.getProperty("path.separator");
        String[] pathComponents = pathSep.split(" ");
        int i;

        for (i = 0; i < pathComponents.length; i++) {
            buf.setLength(0);
            buf.append(pathComponents[i]);
            buf.append(fileSep);
            buf.append("mime.types");

            loadMIMETypesFile(buf.toString());
        }

        // Finally, load the resource bundle.

        ResourceBundle bundle = ResourceBundle.getBundle(MIME_MAPPINGS_BUNDLE);
        for (Enumeration<?> e = bundle.getKeys(); e.hasMoreElements();) {
            String type = (String) e.nextElement();
            try {
                String[] extensions = bundle.getString(type).split(" ");

                if (mimeTypeToExtensionMap.get(type) == null) {
                    LOG.debug("Internal: " + type + " -> \"" + extensions[0]
                            + "\"");
                    mimeTypeToExtensionMap.put(type, extensions[0]);
                }
            }

            catch (MissingResourceException ex) {
                LOG.error("While reading internal bundle \""
                                  + MIME_MAPPINGS_BUNDLE
                                  + "\", got unexpected error on key \"" + type
                                  + "\"",
                          ex);
            }
        }
    }

    /**
     * Attempt to load a MIME types file. Throws no exceptions.
     * 
     * @param path
     *        path to the file
     * @param map
     *        map to load
     */
    private static void loadMIMETypesFile(String path) {
        try {
            File f = new File(path);

            LOG.debug("Attempting to load MIME types file \"" + path + "\"");
            if (!(f.exists() && f.isFile()))
                LOG.debug("Regular file \"" + path + "\" does not exist.");

            else {
                LineNumberReader r = new LineNumberReader(new FileReader(f));
                String line;

                while ((line = r.readLine()) != null) {
                    line = line.trim();

                    if ((line.length() == 0) || (line.startsWith("#")))
                        continue;

                    String[] fields = line.split(" ");

                    // Skip lines without at least two tokens.

                    if (fields.length < 2) continue;

                    // Special case: Scan the extensions, and make sure we
                    // have at least one valid extension. Some .mime.types
                    // files have entries like this:
                    //
                    // mime/type  desc="xxx" exts="jnlp"
                    //
                    // We don't handle those.

                    List<String> extensions = new ArrayList<String>();

                    for (int i = 1; i < fields.length; i++) {
                        if (fields[i].indexOf('=') != -1) continue;
                        if (fields[i].indexOf('"') != -1) continue;

                        // Treat as valid. Remove any leading "."

                        if (fields[i].startsWith(".")) {
                            if (fields[i].length() == 1) continue;

                            fields[i] = fields[i].substring(1);
                        }

                        extensions.add(fields[i]);
                    }

                    if (extensions.size() == 0) continue;

                    // If the MIME type doesn't have a "/", skip it

                    String mimeType = fields[0];
                    String extension;

                    if (mimeType.indexOf('/') == -1) continue;

                    // The first field is the preferred extension. Keep any
                    // existing mapping for the MIME type.

                    if (mimeTypeToExtensionMap.get(mimeType) == null) {
                        extension = (String) extensions.get(0);
                        LOG.debug("File \"" + path + "\": " + mimeType
                                + " -> \"" + extension + "\"");

                        mimeTypeToExtensionMap.put(mimeType, extension);
                    }
                }

                r.close();
            }
        } catch (IOException ex) {
            LOG.debug("Error reading \"" + path + "\"", ex);
        }
    }

}
