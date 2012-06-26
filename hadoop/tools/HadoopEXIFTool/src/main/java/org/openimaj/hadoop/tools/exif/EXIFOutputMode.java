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
package org.openimaj.hadoop.tools.exif;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.Map.Entry;

import com.thebuzzmedia.exiftool.ExifTool;
import com.thebuzzmedia.exiftool.ExifTool.Tag;
import com.thebuzzmedia.exiftool.RDFExifTool;

/**
 * Modes for writing EXIF.
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public enum EXIFOutputMode {
	/**
	 * Textual output
	 * 
	 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
	 *
	 */
	TEXT {
		@Override
		public void output(PrintWriter pw, File tmp, RDFExifTool tool) throws IllegalArgumentException, SecurityException, IOException {
			Map<Tag, String> allExif = tool.getImageMeta(tmp.getAbsoluteFile(), ExifTool.Tag.values());
			
			for(Entry<Tag,String> entry: allExif.entrySet()){
				pw.print(entry.getKey());
				pw.print(' ');
				pw.print('"');
				pw.print(entry.getValue());
				pw.print('"');
				pw.println();
			}
			pw.close();
		}
	},
	/**
	 * RDF Output 
	 * 
	 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
	 */
	RDF {
		@Override
		public void output(PrintWriter pw, File tmp, RDFExifTool tool) throws IllegalArgumentException, SecurityException, IOException {
			String allExif = tool.getImageRDF(tmp.getAbsoluteFile(), ExifTool.Tag.values());
			pw.print(allExif);
			pw.close();
		}
		
		@Override
		public void output(PrintWriter pw, File tmp, String realName, RDFExifTool tool) throws IllegalArgumentException, SecurityException, IOException
		{
			String allExif = tool.getImageRDF(tmp.getAbsoluteFile(), realName, ExifTool.Tag.values());
			pw.print(allExif);
			pw.close();
		}
	};

	public abstract void output(PrintWriter output, File tmp, RDFExifTool tool) throws IllegalArgumentException, SecurityException, IOException;
	
	public void output(PrintWriter output, File tmp, String realName, RDFExifTool tool) throws IllegalArgumentException, SecurityException, IOException
	{
		output(output, tmp, tool);
	}	
}
