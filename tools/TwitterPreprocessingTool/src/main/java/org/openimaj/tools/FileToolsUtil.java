package org.openimaj.tools;

import java.io.File;

import org.kohsuke.args4j.CmdLineException;
import org.openimaj.io.FileUtils;

/**
 * Tools for dealing with #InOutTool instances that are local file
 * 
 * @author ss
 *
 */
public class FileToolsUtil {

	/**
	 * @param tool the tool from which to get settings
	 * @return a none null input file location if it exists
	 * @throws CmdLineException if the file doesn't exist
	 */
	public static File validateLocalInput(InOutToolOptions tool) throws CmdLineException{
		File f = new File(tool.input);
		if(!f.exists()) throw new CmdLineException(null,"Couldn't file file");
		return f;
	}
	/**
	 * @param tool the tool from which to get settings
	 * @return is output the stdout?
	 */
	public static boolean isStdout(InOutToolOptions tool){
		return tool.getOutput().equals("-");
	}
	
	/**
	 * @param tool the tool from which to get settings
	 * @return the output file location, deleted if it is allowed to be deleted
	 * @throws CmdLineException
	 */
	public static File validateLocalOutput(InOutToolOptions tool) throws CmdLineException{
		File output = new File(tool.output);
		if(output.exists()){
			if(tool.overwriteOutput()){
				if(FileUtils.deleteRecursive(output)) throw new CmdLineException(null, "Couldn't delete existing output");
			}
			else{
				throw new CmdLineException(null, "Output already exists, didn't remove");
			}
		}
		return output;
	}
}
