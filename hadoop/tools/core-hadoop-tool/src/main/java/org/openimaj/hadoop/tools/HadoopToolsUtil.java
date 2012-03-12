package org.openimaj.hadoop.tools;

import java.io.IOException;
import java.net.URI;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.LocalFileSystem;
import org.apache.hadoop.fs.Path;
import org.kohsuke.args4j.CmdLineException;
import org.openimaj.hadoop.sequencefile.SequenceFileUtility;
import org.openimaj.tools.InOutToolOptions;

/**
 * Tools for dealing with #InOutTool instances that are hdfs files
 * 
 * @author Sina Samangooei <ss@ecs.soton.ac.uk>
 */
public class HadoopToolsUtil {

	/**
	 * 
	 * @param tool options to get data from
	 * @throws CmdLineException
	 */
	public static void validateOutput(InOutToolOptions tool) throws CmdLineException {
		try {
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
			URI outuri = SequenceFileUtility.convertToURI(tool.getInput());
			FileSystem fs = getFileSystem(outuri);
			if(!fs.exists(new Path(outuri.toString())))
				throw new CmdLineException(null, "Couldn't find input file");
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
		return SequenceFileUtility.getFilePaths(options.getInput(), "part");
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

	

}
