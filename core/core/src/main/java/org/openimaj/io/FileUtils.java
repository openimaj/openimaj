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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

/**
 * Utility methods for dealing with files on the filesystem 
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * @author David Dupplaw (dpd@ecs.soton.ac.uk)
 *
 */
public class FileUtils {
	/**
	 * Recursively delete a directory
	 * @param dir
	 * @return true if success; false otherwise
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

	/**
	 * Download the contents of the given URL to the given file
	 * @param url The URL to download from 
	 * @param file The target file
	 * @throws IOException if an error occurs
	 */
	public static void downloadURL(URL url, File file) throws IOException {
		URLConnection conn = url.openConnection();
		InputStream stream = conn.getInputStream();
		FileOutputStream fos = new FileOutputStream(file);
		byte[] buffer = new byte[1024];
		int read = 0;
		while((read = stream.read(buffer)) != -1){
			fos.write(buffer,0,read);
		}
	}

	/**
	 * Utility method for quickly create a {@link BufferedReader} for
	 * a given file. 
	 * @param file The file
	 * @return the corresponding reader
	 * @throws IOException if an error occurs
	 */
	public static BufferedReader read(File file) throws IOException {
		return new BufferedReader(new InputStreamReader(new FileInputStream(file)));
	}
	/**
	 * Utility method for reading a whole file into a single string.
	 * @param stream The stream
	 * @return the corresponding reader
	 * @throws IOException if an error occurs
	 */
	public static String readall(InputStream stream) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(stream));
		String line = null;
		StringBuilder builder = new StringBuilder();
		while((line = br.readLine()) != null){
			builder.append(line);
			builder.append("\n");
		}
		
		return builder.toString(); 
	}
	
	/**
	 * Utility method for reading a whole file into a single string.
	 * @param file The file
	 * @return the corresponding reader
	 * @throws IOException if an error occurs
	 */
	public static String readall(File file) throws IOException {
		return readall(new FileInputStream(file));
	}
		
	/**
	 * Helper function for writing a text stream to a file.
	 * @param stream the stream will be consumed
	 * @param output file
	 * @return a temporary file with the stream context written
	 * @throws IOException
	 */
	public static File copyStreamToFile(InputStream stream,File output) throws IOException{
		BufferedReader r = new BufferedReader(new InputStreamReader(stream));
		String l =null;
		PrintWriter writer = new PrintWriter(new FileWriter(output));
		while((l = r.readLine())!=null){
			writer.println(l);
		}
		writer.close();
		r.close();
		return output;
	}
	
	/**
	 * 	Given a JAR Resource, this method will unpack the file to
	 * 	a temporary file and return the temporary file location.
	 * 	This temporary file will be deleted on the application exit.
	 * 	If the given resource is not a JAR resource, the method
	 * 	will return null.
	 * 
	 * 	@param resource The resource to unpack
	 * 	@return The temporary file location
	 * 	@throws IOException If the temporary file could not be created. 
	 */
	public static File unpackJarFile( URL resource ) throws IOException
	{
		return FileUtils.unpackJarFile( resource, true );
	}

	/**
	 * 	Given a JAR Resource, this method will unpack the file to
	 * 	a temporary file and return the temporary file location.
	 * 	If the given resource is not a JAR resource, the method
	 * 	will return null.
	 * 
	 * 	@param resource The resource to unpack
	 * 	@param deleteOnExit Whether to delete the temporary file on exit
	 * 	@return The temporary file location
	 * 	@throws IOException If the temporary file could not be created. 
	 */
	public static File unpackJarFile( URL resource, boolean deleteOnExit ) 
		throws IOException
	{
		if( !FileUtils.isJarResource( resource ) )
			return null;
	
		String ext = resource.toString().substring( 
				resource.toString().lastIndexOf(".") );
		File f = File.createTempFile( "openimaj",ext );
		FileUtils.unpackJarFile( resource, f, deleteOnExit );
		return f;
	}
	
	/**
	 * 	Given a JAR resource, this method will unpack the file
	 * 	to the given destination. If the given resource is not
	 * 	a JAR resource, this method will do nothing.
	 * 
	 * 	@param resource The resource to unpack.
	 * 	@param destination The destination file
	 * 	@param deleteOnExit Whether to delete the unpacked file on exit.
	 */
	public static void unpackJarFile( URL resource, File destination, 
				boolean deleteOnExit )
	{
		if( deleteOnExit )
			destination.deleteOnExit();
		
		BufferedInputStream urlin = null;
		BufferedOutputStream fout = null;
		try {
			int bufSize = 8 * 1024;
			urlin = new BufferedInputStream(
					resource.openConnection().getInputStream(),
					bufSize);
			fout = new BufferedOutputStream(
					new FileOutputStream( destination ), bufSize);

			int read = -1;
			byte[] buf = new byte[ bufSize ];
			while ((read = urlin.read(buf, 0, bufSize)) >= 0) 
				fout.write(buf, 0, read);

			fout.flush();
		}
		catch (IOException ioex) 
		{
			return;
		}
		catch( SecurityException sx ) 
		{
			return;
		}
		finally 
		{
			if (urlin != null) 
			{
				try 
				{
					urlin.close();
				}
				catch (IOException cioex) 
				{
				}
			}
			if (fout != null) 
			{
				try 
				{
					fout.close();
				}
				catch (IOException cioex) 
				{
				}
			}
		}
	}
	
	/**
	 * 	Returns whether the given resource is a jar resource.
	 * 	@param resource The resource to test.
	 * 	@return TRUE if the resource is a JAR resource
	 */
	public static boolean isJarResource( URL resource )
	{
		return FileUtils.isJarResource( resource.toString() );
	}
	
	/**
	 * 	Returns whether the given resource is a jar resource.
	 * 	@param resourceURL The resource to test.
	 * 	@return TRUE if the resource is in a jar.
	 */
	public static boolean isJarResource( String resourceURL )
	{
		return resourceURL.startsWith( "jar:" );
	}

	/**
	 * Count the number of newlines in the given file
	 * @param filename The file
	 * @return the number of newline characters
	 */
	public static int countLines(File filename)  {
		InputStream is = null;
	    try {
	    	is = new BufferedInputStream(new FileInputStream(filename));
	        byte[] c = new byte[1024];
	        int count = 0;
	        int readChars = 0;
	        while ((readChars = is.read(c)) != -1) {
	            for (int i = 0; i < readChars; ++i) {
	                if (c[i] == '\n')
	                    ++count;
	            }
	        }
	        return count;   
	    }
	    catch(Exception e){
	    	return -1;
	    } finally {
	        try {
				is.close();
			} catch (IOException e) {
				return -1;
			}
	    }
	}

	/**
	 * Using {@link File#listFiles(FilenameFilter)} find a file in the directory recursively (i.e. following directories down).
	 * @param start
	 * @param filenameFilter
	 * @return list of files matching the filter 
	 */
	public static File[] findRecursive(File start, final FilenameFilter filenameFilter) {
		final Stack<File> filesToCheck = new Stack<File>();
		List<File> found = new ArrayList<File>();
		filesToCheck .push(start);
		while(filesToCheck.size() > 0){
			final File toCheck = filesToCheck.pop();
			File[] afiles = toCheck.listFiles(new FilenameFilter() {
				
				@Override
				public boolean accept(File dir, String name) {
					File found = new File(dir,name);
					boolean accept = filenameFilter.accept(found, name);
					if(toCheck != found && found .isDirectory()) 
					{
						System.out.println("Adding: " + found);
						filesToCheck.push(found);
					}
					return accept;
				}
			});
			found.addAll(Arrays.asList(afiles));
		}
		return found.toArray(new File[found.size()]);
	}

	/**
	 * @param file the file to read from
	 * @return the lines in the file
	 * @throws IOException 
	 */
	public static String[] readlines(File file) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
		String line = null;
		List<String> allLines = new ArrayList<String>();
		while((line = br.readLine()) != null){
			allLines.add(line);
		}
		
		return allLines.toArray(new String[allLines.size()]);
	}
	
	/**
	 * @param stream the file to read from
	 * @return the lines in the file
	 * @throws IOException 
	 */
	public static String[] readlines(InputStream stream) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(stream));
		String line = null;
		List<String> allLines = new ArrayList<String>();
		while((line = br.readLine()) != null){
			allLines.add(line);
		}
		
		return allLines.toArray(new String[allLines.size()]);
	}
}
