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
 * @author ss
 *
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
					throw new CmdLineException(null, "Output exists, couldn't delete"); 
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

}
