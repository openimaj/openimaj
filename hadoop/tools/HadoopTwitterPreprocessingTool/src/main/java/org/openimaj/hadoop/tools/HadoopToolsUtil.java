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
