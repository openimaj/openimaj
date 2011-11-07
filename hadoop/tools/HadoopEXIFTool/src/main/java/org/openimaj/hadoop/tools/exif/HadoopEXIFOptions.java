package org.openimaj.hadoop.tools.exif;

import java.io.IOException;
import java.net.URI;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.LocalFileSystem;
import org.apache.hadoop.fs.Path;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.openimaj.hadoop.sequencefile.SequenceFileUtility;

public class HadoopEXIFOptions {
	@Option(name="--input", aliases="-i", required=true, usage="Input image FILE.", metaVar="STRING")
	private String input;
	
	@Option(name="--output", aliases="-o", required=true, usage="Output keypoint FILE.", metaVar="STRING")
	private String output;
	
	@Option(name="--output-mode", aliases="-om", required=true, usage="Output Mode.", metaVar="STRING")
	private EXIFOutputMode outputMode = EXIFOutputMode.TEXT;

	@Option(name="--remove", aliases="-rm", required=false, usage="Remove the existing output location if it exists.", metaVar="BOOLEAN")
	private boolean replace = false;
	
	@Option(name="--exif-path", aliases="-ep", required=false, usage="Path to the exiftools executable", metaVar="STRING")
	private String exifPath = "/usr/local/bin/exiftool";
	
	private String[] args;

	private boolean beforeMap;

	public HadoopEXIFOptions(String[] args, boolean beforeMap) {
		this.args = args;
		this.beforeMap = beforeMap;
	}

	public void prepare() {
		CmdLineParser parser = new CmdLineParser(this);
		try {
			parser.parseArgument(args);
			this.validate();
		} catch (CmdLineException e) {
			System.err.println(e.getMessage());
			System.err.println("Usage: java -jar HadoopEXIFTool.jar [options...] [files...]");
			parser.printUsage(System.err);
			
			System.exit(1);
		}
	}
	
	private void validate() {
		if(replace && beforeMap){
			try {
				URI outuri = SequenceFileUtility.convertToURI(this.getOutputString());
				FileSystem fs = getFileSystem(outuri);
				fs.delete(new Path(outuri.toString()), true);
			} catch (IOException e) {
				
			}
		}
	}
	public static FileSystem getFileSystem(URI uri) throws IOException {
		Configuration config = new Configuration();
		FileSystem fs = FileSystem.get(uri, config);
		if (fs instanceof LocalFileSystem) fs = ((LocalFileSystem)fs).getRaw();
		return fs;
	}

	public Path[] getInputPaths() throws IOException {
		Path[] sequenceFiles = SequenceFileUtility.getFilePaths(this.getInputString(), "part");
		return sequenceFiles;
	}

	public Path getOutputPath() {
		return new Path(SequenceFileUtility.convertToURI(this.getOutputString()).toString());
	}
	
	public String getInputString() {
		return input;
	}

	public String getOutputString() {
		return output;
	}

	public String getExifPath() {
		return this.exifPath;
	}

	public EXIFOutputMode getOutputMode() {
		return outputMode;
	}
}
