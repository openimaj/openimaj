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
package org.openimaj.hadoop.tools.sequencefile;

import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.openimaj.hadoop.sequencefile.utils.MimeTypeUtils;
import org.openimaj.hadoop.tools.sequencefile.SequenceFileTool;



public class SequenceFileToolTest {
	
	private File tmpImageSEQ;
	private Map<String,byte[]> imageByteArray;

	@Before
	public void setup() throws Exception{
		File tmpImageDir = File.createTempFile("image", "dir");
		tmpImageSEQ = File.createTempFile("image", "seq");
		tmpImageDir.delete();
		tmpImageDir.mkdir();
		tmpImageSEQ.delete();
		InputStream[] inputs = new InputStream[]{
			this.getClass().getResourceAsStream("/org/openimaj/image/data/cat.jpg"),
			this.getClass().getResourceAsStream("/org/openimaj/image/data/sinaface.jpg"),
		};
		int i = 0;
		List<File> lists = new ArrayList<File>();
		imageByteArray = new HashMap<String,byte[]>();
		for(InputStream input : inputs){
			File f = new File(tmpImageDir,i++ + ".jpg");
			FileOutputStream fos = new FileOutputStream(f);
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			byte[] buffer = new byte[1024];
			int len = 0;
			while((len = input.read(buffer)) != -1){
				fos.write(buffer, 0, len);
				bos.write(buffer,0,len);
			}
			fos.flush();
			fos.close();
			bos.flush();
			imageByteArray.put(f.getName(),bos.toByteArray());
			lists.add(f);
			
		}
		
		String[] args = new String[]{"-m", "CREATE", "--no-rename","-o", tmpImageSEQ.getAbsolutePath(), lists.get(0).getAbsolutePath(),lists.get(1).getAbsolutePath()};
		SequenceFileTool.main(args);
		
	}
	@Test
	public void testMimeTypeUtil() throws Exception{
		System.out.println(MimeTypeUtils.fileExtensionForMIMEType("image/jpeg"));
	}
	
	@Test
	public void testSequenceFileExtraction() throws Exception{
		File out = File.createTempFile("random", "10");
		out .delete();
		// java -jar ../bin/SequenceFileTool.jar -m EXTRACT hdfs://degas/data/features/ukbench-mlsift -o features/mlsift
		String[] args = new String[]{"-m", "EXTRACT", tmpImageSEQ.getAbsolutePath(), "-o", out.getAbsolutePath(),"-n","KEY"};
		SequenceFileTool.main(args);
		System.out.println("Gone to: " + out );
		for(File f: out.listFiles()){
			FileInputStream fis = new FileInputStream(f);
			byte[] buffer = new byte[1024];
			int len = 0;
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			while((len = fis.read(buffer)) != -1){
				bos.write(buffer,0,len);
			}
			bos.flush();
			assertTrue(Arrays.equals(imageByteArray.get(f.getName()), bos.toByteArray()));
		}
	}
	
	public static void main(String args[]) throws Exception {
		new SequenceFileToolTest().testSequenceFileExtraction();
	}
}