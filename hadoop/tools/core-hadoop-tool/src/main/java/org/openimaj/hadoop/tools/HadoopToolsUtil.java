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
package org.openimaj.hadoop.tools;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.LocalFileSystem;
import org.apache.hadoop.fs.Path;
import org.kohsuke.args4j.CmdLineException;
import org.openimaj.hadoop.sequencefile.SequenceFileUtility;
import org.openimaj.io.FileUtils;
import org.openimaj.tools.InOutToolOptions;

/**
 * Tools for dealing with #InOutTool instances that are hdfs files
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 */
public class HadoopToolsUtil {

	/**
	 * 
	 * @param tool options to get data from
	 * @throws CmdLineException
	 */
	public static void validateOutput(InOutToolOptions tool) throws CmdLineException {
		try {
			if(tool.getOutput() == null) throw new CmdLineException(null,"No Output Specified");
			URI outuri = SequenceFileUtility.convertToURI(tool.getOutput());
			FileSystem fs = getFileSystem(outuri);
			Path p = new Path(outuri.toString());
			if(fs.exists(p))
			{
				if(tool.overwriteOutput())
				{
					fs.delete(p, true);
				}
				else{
//					throw new CmdLineException(null, "Output exists, couldn't delete"); 
					System.out.println("Output exists, trying to use what is there...");
				}
			}
		} catch (IOException e) {
			throw new CmdLineException(null, "Couldn't delete existing output");
		}
		
	}
	/**
	 * 
	 * @param outpath The desired output
	 * @param replace whether the existing outputs should be removed
	 * @throws CmdLineException
	 */
	public static void validateOutput(String outpath, boolean replace) throws CmdLineException {
		try {
			URI outuri = SequenceFileUtility.convertToURI(outpath);
			FileSystem fs = getFileSystem(outuri);
			Path p = new Path(outuri.toString());
			if(fs.exists(p))
			{
				if(replace)
				{
					fs.delete(p, true);
				}
				else{
//					throw new CmdLineException(null, "Output exists, couldn't delete"); 
					System.out.println("Output exists, trying to use what is there...");
				}
			}
		} catch (IOException e) {
			throw new CmdLineException(null, "Couldn't delete existing output");
		}
		
	}
	
	/**
	 * @param uri
	 * @return the file system of a given path (HDFS or Local usually)
	 * @throws IOException
	 */
	public static FileSystem getFileSystem(URI uri) throws IOException {
		Configuration config = new Configuration();
		FileSystem fs = FileSystem.get(uri, config);
		if (fs instanceof LocalFileSystem) fs = ((LocalFileSystem)fs).getRaw();
		return fs;
	}
	
	/**
	 * Get the {@link FileSystem} corresponding to a {@link Path}.
	 * @param p the path.
	 * @return the filesystem
	 * @throws IOException
	 */
	public static FileSystem getFileSystem(Path p) throws IOException {
		return getFileSystem(p.toUri());
	}

	/**
	 * @param tool
	 * @throws CmdLineException
	 */
	public static void validateInput(InOutToolOptions tool) throws CmdLineException {
		
		try {
			FileSystem fs = null ;
			if(tool.getAllInputs() == null) throw new IOException();
			for (String input : tool.getAllInputs()) {
				URI outuri = SequenceFileUtility.convertToURI(input);
				if(fs == null) fs = getFileSystem(outuri);
				if(!fs.exists(new Path(outuri.toString())))
					throw new CmdLineException(null, "Couldn't find input file");
			}
			
		} catch (IOException e) {
			throw new CmdLineException(null, "Couldn't find input file filesystem");
		}
	}

	/**
	 * Delete a file
	 * @param f the file to delete
	 * @throws IOException
	 */
	public static void removeFile(String f) throws IOException {
		URI outuri = SequenceFileUtility.convertToURI(f);
		FileSystem fs = getFileSystem(outuri);
		Path p = new Path(outuri.toString());
		fs.delete(p, true);
	}
	
	/**
	 * Get the output path from an {@link InOutToolOptions}.
	 * @param options the {@link InOutToolOptions}.
	 * @return the output path.
	 */
	public static Path getOutputPath(InOutToolOptions options) {
		return new Path(options.getOutput());
	}
	
	/**
	 * Get the output path from a String.
	 * @param path the path string
	 * @return the path
	 */
	public static Path getOutputPath(String path) {
		return new Path(path);
	}

	/**
	 * Get the input paths from an {@link InOutToolOptions}. This will resolve the input path
	 * and return either a {@link Path} object representing the string
	 * or, if the path string is a directory, a list of {@link Path}s 
	 * representing all the "part" files.
	 * @param options the {@link InOutToolOptions}.
	 * @return the input path
	 * @throws IOException
	 */
	public static Path[] getInputPaths(InOutToolOptions options) throws IOException {
		return SequenceFileUtility.getFilePaths(options.getAllInputs(), "part");
	}
	
	/**
	 * Get the input paths from a String. This will resolve the path string
	 * and return either a {@link Path} object representing the string
	 * or, if the path string is a directory, a list of {@link Path}s 
	 * representing all the "part" files.
	 * 
	 * @param path the path string
	 * @return the paths
	 * @throws IOException 
	 */
	public static Path[] getInputPaths(String path) throws IOException {
		return SequenceFileUtility.getFilePaths(path, "part");
	}
	
	/**
	 * @param paths
	 * @return all the file starting with "part" in the paths requested
	 * @throws IOException
	 */
	public static Path[] getInputPaths(String[] paths) throws IOException {
		return SequenceFileUtility.getFilePaths(paths, "part");
	}
	
	/**
	 * All the files starting with "part" in the paths which look like: "paths[i]/subdir
	 * @param paths
	 * @param subdir
	 * @return the paths to the part files
	 * @throws IOException
	 */
	public static Path[] getInputPaths(String[] paths, String subdir) throws IOException {
		return SequenceFileUtility.getFilePaths(paths, subdir, "part");
	}
	
	/**
	 * Use hadoop filesystem to check if the given path exists
	 * @param path the path to the file
	 * @return true if file exists; false otherwise
	 * @throws IOException
	 */
	public static boolean fileExists(String path) throws IOException{
		URI outuri = SequenceFileUtility.convertToURI(path);
		FileSystem fs = getFileSystem(outuri);
		Path p = new Path(outuri.toString());
		return fs.exists(p);
	}
	/**
	 * Read a whole hadoop file into a string. This is obviously a ridiculous thing to do for all but the SMALLEST hadoop files
	 * so be very careful
	 * @param p a path
	 * @return the content of the path p as a string 
	 * @throws IOException 
	 */
	public static String[] readlines(String p) throws IOException {
		Path[] allIn = getInputPaths(p);
		if(allIn.length == 0)return new String[0];
		List<String> out = new ArrayList<String>();
		FileSystem fs = getFileSystem(allIn[0]);
		for (Path path : allIn) {
			FSDataInputStream is = fs.open(path);
			out.addAll(Arrays.asList(FileUtils.readlines(is)));
		}
		return out.toArray(new String[out.size()]);
	}
	
	private static String COMMA_REPLACE = "#COMMA#";
	
	/**
	 * A horrible hack to deal with hadoop's horrible hack when setting arrays of strings as configs
	 * @param args
	 * @return horribly replace each "," with #COMMA#
	 */
	public static String[] encodeArgs(String[] args) {
		String[] ret = new String[args.length];
		int i = 0;
		for (String arg : args) {
			ret[i] = arg.replaceAll(",", COMMA_REPLACE);
			i++;
		}
		return ret;
	}
	
	/**
	 * A horrible hack to deal with hadoop's horrible hack when setting arrays of strings as configs
	 * @param args
	 * @return horribly replace each #COMMA# with ","
	 */
	public static String[] decodeArgs(String[] args) {
		String[] ret = new String[args.length];
		int i = 0;
		for (String arg : args) {
			ret[i] = arg.replaceAll(COMMA_REPLACE,",");
			i++;
		}
		return ret;
	}
	

	

}
