package org.openimaj.hadoop.tools.exif;

import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
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
		HadoopEXIF.main(new String[]{"-D","mapred.child.java.opts=\"-Xmx3000M\"","-ep","/usr/bin/exiftooasdadsl","-i",imageSeqFile.getAbsolutePath(),"-o",featureSeqFile.getAbsolutePath()});
		System.out.println(featureSeqFile);
	}
}
