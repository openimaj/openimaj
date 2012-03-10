/**
 * Copyright (c) 2012, The University of Southampton and the individual contributors.
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
package org.openimaj.tools;

import java.io.File;
import java.io.IOException;

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
	public static File validateLocalInput(InOutToolOptions tool) throws IOException{
		File f = new File(tool.input);
		if(!f.exists()) throw new IOException("Couldn't file file");
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
	 * @throws IOException
	 */
	public static File validateLocalOutput(InOutToolOptions tool) throws IOException{
		return validateLocalOutput(tool.output,tool.isForce());
	}
	
	/**
	 * @param out where the file will go
	 * @param overwrite whether to overwrite existing files
	 * @return the output file location, deleted if it is allowed to be deleted
	 * @throws IOException
	 */
	public static File validateLocalOutput(String out, boolean overwrite) throws IOException {
		File output = new File(out);
		if(output.exists()){
			if(overwrite){
				if(!FileUtils.deleteRecursive(output)) throw new IOException("Couldn't delete existing output");
			}
			else{
				throw new IOException("Output already exists, didn't remove");
			}
		}
		return output;
	}
}
