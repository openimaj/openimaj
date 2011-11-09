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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.UUID;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.IOUtils;
import org.apache.hadoop.io.Text;
import org.junit.Before;
import org.junit.Test;
import org.openimaj.hadoop.sequencefile.TextBytesSequenceFileUtility;

public class HadoopEXIFTest {
	private File imageSeqFile;
	private ArrayList<Text> keys;

	@Before
	public void setUp() throws Exception {
		imageSeqFile = File.createTempFile("seq", "images");
		TextBytesSequenceFileUtility tbsfu = new TextBytesSequenceFileUtility(imageSeqFile.getAbsolutePath(),false);
		InputStream[] inputs = new InputStream[]{
			this.getClass().getResourceAsStream("ukbench00000.jpg"),
			this.getClass().getResourceAsStream("ukbench00001.jpg"),
			this.getClass().getResourceAsStream("broken.txt"),
		};
		
		Text key;
		keys = new ArrayList<Text>();
		for(InputStream input : inputs){
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			IOUtils.copyBytes(input, baos, new Configuration(), false);
			BytesWritable bytesWriteable = new BytesWritable(baos.toByteArray());
			key = new Text(UUID.randomUUID().toString());
			
			keys.add(key);
			tbsfu.appendData(key,bytesWriteable);
		}
		tbsfu.close();
	}
	
	@Test
	public void testExifGeneration() throws Exception{
		File featureSeqFile = File.createTempFile("seq", "features");
		featureSeqFile.delete();
		HadoopEXIF.main(new String[]{"-D","mapred.child.java.opts=\"-Xmx3000M\"","-ep","/usr/bin/exiftool","-i",imageSeqFile.getAbsolutePath(),"-o",featureSeqFile.getAbsolutePath(),"-om","RDF"});
		System.out.println(featureSeqFile);
	}
}
