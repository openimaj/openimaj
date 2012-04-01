package org.openimaj.hadoop.tools.twitter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;

import org.apache.commons.lang.ArrayUtils;
import org.apache.tools.tar.TarEntry;
import org.apache.tools.tar.TarInputStream;
import org.junit.Before;
import org.junit.Test;

public class CorrelationModeTest {
	private String hadoopCommand;
	private File dest;
	private File output;
	@Before
	public void setup() throws IOException {
		hadoopCommand = "-i %s -om %s -ro %s -t 1";
		TarInputStream tin = new TarInputStream( new GZIPInputStream( CorrelationModeTest.class.getResourceAsStream("/org/openimaj/hadoop/tools/twitter/dfidf.out.tar.gz") ));
		TarEntry entry = null;
		output = File.createTempFile("results",".out");
		output.delete();
		output.mkdir();
		dest = File.createTempFile("DFIDF", ".out");
		dest.delete();
		dest.mkdir();
		while((entry = tin.getNextEntry()) != null){
			File tdst = new File(dest.toString(),entry.getName());
			if(entry.isDirectory()){
				tdst.mkdir();
			}
			else{
				FileOutputStream fout = new FileOutputStream(tdst);
				tin.copyEntryContents(fout);
				fout.close();
			}
		}
		tin.close();
	}
	@Test
	public void testCorrelation() throws Exception{
		System.out.println("Reading DFIDF from: " + dest.getAbsolutePath());
		String command = String.format(
				hadoopCommand,
				dest.getAbsolutePath(),
				"CORRELATION",
				output
		);
		String[] args = command.split(" ");
		HadoopTwitterTokenTool.main(args);
	}
}
