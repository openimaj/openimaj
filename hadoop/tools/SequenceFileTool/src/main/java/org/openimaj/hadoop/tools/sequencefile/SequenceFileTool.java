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

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.PathFilter;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.Text;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineOptionsProvider;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.ProxyOptionHandler;
import org.openimaj.hadoop.sequencefile.ExtractionPolicy;
import org.openimaj.hadoop.sequencefile.NamingPolicy;
import org.openimaj.hadoop.sequencefile.SequenceFileUtility;
import org.openimaj.hadoop.sequencefile.TextBytesSequenceFileUtility;
import org.openimaj.hadoop.sequencefile.SequenceFileUtility.KeyProvider;


public class SequenceFileTool {
	enum InfoModeOptions {
		GUID,
		METADATA,
		NRECORDS,
		COMPRESSION_CODEC,
		COMPRESSION_TYPE;
	}
	
	enum Mode implements CmdLineOptionsProvider {
		INFO {
			@Option(name="--options", aliases="-opts", required=false, usage="Choose info type. Defaults to all.", multiValued=true)
			private List<InfoModeOptions> options = new ArrayList<InfoModeOptions>(EnumSet.allOf(InfoModeOptions.class));

			@Argument(required=true, usage="Sequence file", metaVar="input-path-or-uri")
			private String inputPathOrUri;

			@Override
			public void execute() throws Exception {
				SequenceFileUtility<Text, BytesWritable> utility = new TextBytesSequenceFileUtility(inputPathOrUri, true);
				
				if (options.contains(InfoModeOptions.GUID) && !options.contains(InfoModeOptions.METADATA)) {
					System.out.println("UUID: " + utility.getUUID());
				}
				
				if (options.contains(InfoModeOptions.METADATA)) {
					Map<Text,Text> metadata = utility.getMetadata();
					
					System.out.println("Metadata:");
					for (Entry<Text, Text> e : metadata.entrySet()) {
						System.out.println(e.getKey() + ": " + e.getValue());
					}
				}
				
				if (options.contains(InfoModeOptions.NRECORDS)) {
					System.out.println("NRecords: " + utility.getNumberRecords());
				}
				
				if (options.contains(InfoModeOptions.COMPRESSION_CODEC)) {
					System.out.println("Compression codec: " + utility.getCompressionCodecClass());
				}
				
				if (options.contains(InfoModeOptions.COMPRESSION_TYPE)) {
					System.out.println("Compression type: " + utility.getCompressionType());
				}
			}
		},
		CREATE {
			@Option(name="--recursive", aliases="-R", required=false, usage="Recurse into directories inside input directories")
			boolean recurse=false;
			
			@Option(name="--no-rename", required=false, usage="Don't rename keys")
			boolean no_rename=false;
			
			@Option(name="--output", aliases="-o", required=false, usage="Output directory (path or uri).")
			String outputPathOrUri = "./";
			
			@Option(name="--output-name", aliases="-name", required=false, usage="Output filename. Defaults to <uuid>.seq.")
			String outputName;
			
			@Option(name="--write-map", aliases="-wm", required=false, usage="Write uuid -> filename map to a file. File is saved in output directory as <name>-map.txt.")
			boolean writeFilename2IDMap = false;
			
			@Option(name="--print-map", aliases="-pm", required=false, usage="Print uuid -> filename map.")
			boolean printFilename2IDMap = false;
			
			@Option(name="--filename-regex", aliases="-fnr", required=false, usage="Regular expressions that file names must match to be added.")
			String filenameRegex = null;
			
			@Argument(usage="input files", multiValued=true, required=true, metaVar="input-paths-or-uris")
			List<String> inputs = new ArrayList<String>();
			
			@Override
			public void execute() throws Exception {
				if (outputName != null) {
					if (!outputPathOrUri.endsWith("/")) outputPathOrUri += "/";
					outputPathOrUri += outputName;
				}
				
				SequenceFileUtility<Text, BytesWritable> utility = new TextBytesSequenceFileUtility(outputPathOrUri, false);
				Map<Path, Text> map = new LinkedHashMap<Path, Text>();
				
				for (String input : inputs) {
					URI uri = SequenceFileUtility.convertToURI(input);
					FileSystem fs = utility.getFileSystem(uri);
					Path path = utility.getPath(uri);
					
					PathFilter pathFilter = null;
					if (filenameRegex != null) {
						pathFilter = new RegexPathFilter(filenameRegex);
					}
					
					KeyProvider<Text> keyProvider;
					if (no_rename)
						keyProvider = new SequenceFileUtility.FilenameKeyProvider();
					else
						keyProvider = new SequenceFileUtility.MD5UUIDKeyProvider();
					
					map.putAll(utility.appendFiles(fs, path, recurse, pathFilter, keyProvider));
				}
				
				if (writeFilename2IDMap) {
					utility.writePathMap(map);
				}
				
				if (printFilename2IDMap) {
					for (Entry<Path, Text> e : map.entrySet()) {
						System.out.println(e.getValue() + " " + e.getKey());
					}
				}
				
				utility.close();
				System.err.println("Created " + utility.getSequenceFilePath());
			} 
		},
		EXTRACT {
			@Option(name="--output", aliases="-o", required=false, usage="Output directory (path or uri).")
			String outputPathOrUri;
			
			@Option(name="--key", aliases="-k", required=false, usage="Key of file to extract. By default if this is not provided, all files are extracted.")
			String queryKey;
			
			@Option(name="--offset", required=false, usage="Offset from which to start extract")
			long offset;
			
			@Option(name="--name-policy",aliases="-n", handler=ProxyOptionHandler.class, required=false, usage="Select the naming policy of outputed files")
			NamingPolicy np = NamingPolicy.KEY;
			
			@Option(name="--random-select",aliases="-r", required=false, usage="Randomly select a subset of input of this size")
			int random = -1;
			
			@Option(name="--extract-max",aliases="-max", required=false, usage="Randomly select a subset of input of this size")
			int max = -1;
			
			@Option(name="--auto-extention",aliases="-ae", required=false, usage="Automatically extract the filetype and append it's appropriate extention")
			boolean autoExtention = false;

			@Argument(required=true, usage="Sequence file", metaVar="input-path-or-uri")
			private String inputPathOrUri;
			
			@Override
			public void execute() throws IOException {
				if (offset < 0)
					throw new IllegalArgumentException("Offset cannot be less than 0.");
				System.out.println("Getting file paths...");
				Path[] sequenceFiles = SequenceFileUtility.getFilePaths(inputPathOrUri, "part");
				ExtractionPolicy nps = new ExtractionPolicy();
				nps.setAutoFileExtention(autoExtention );
				nps.setMaxFileExtract(max);
				if(random>=0){
					System.out.println("Counting records");
					int totalRecords = 0;
					for(Path path : sequenceFiles){
						System.out.println("... Counting from file: " + path);
						SequenceFileUtility<Text, BytesWritable> utility = new TextBytesSequenceFileUtility(path.toUri(), true);
						totalRecords += utility.getNumberRecords();
					}
					System.out.println("Selecting random subset of " + random + " from " + totalRecords);
					nps.setRandomSelection(random,totalRecords);
				}
				for(Path path : sequenceFiles){
					System.out.println("Extracting from " + path.getName());
					SequenceFileUtility<Text, BytesWritable> utility = new TextBytesSequenceFileUtility(path.toUri(), true);
					if (queryKey == null) {
						utility.exportData(outputPathOrUri,np, nps,offset);
					} else {
						if (!utility.findAndExport(new Text(queryKey), outputPathOrUri, offset)) {
							if (offset==0) System.err.format("Key '%s' was not found in the file.\n", queryKey);
							else System.err.format("Key '%s' was not found in the file after offset %d.\n", queryKey, offset);
						}
					}
					if(nps.stop()) break;
				}
			}
		},
		LIST {
			@Option(name="--print-offsets", aliases="-po", required=false, usage="Also print the offset of each record")
			boolean printOffsets = false;
			
			@Option(name="--options", aliases="-opts", required=false, usage="Choose options to include per record in order.", multiValued=true)
			private List<ListModeOptions> options = new ArrayList<ListModeOptions>();
			
			@Option(name="--deliminator", aliases="-delim", required=false, usage="Choose the per record options delimiantor")
			private String delim = " ";
			
			@Argument(required=true, usage="Sequence file", metaVar="input-path-or-uri")
			private String inputPathOrUri;
			
			@Override
			public void execute() throws IOException {
				Path[] sequenceFiles = SequenceFileUtility.getFilePaths(inputPathOrUri, "part");
				
				for(Path path : sequenceFiles){
					System.err.println("Outputting from seqfile: " + path);
					SequenceFileUtility<Text, BytesWritable> utility = new TextBytesSequenceFileUtility(path.toUri(), true);
					
					if(options==null){
						if (printOffsets) {
							for (Entry<Text, Long> e : utility.listKeysAndOffsets().entrySet())
								System.out.format("%10d %s\n", e.getValue(), e.getKey().toString());
						} else {
							for (Text t : utility.listKeys())
								System.out.println(t.toString());
						}
					}
					else{
						utility.streamedListKeysAndOffsets(ListModeOptions.listOptionsToExtractPolicy(options), System.out,delim);
					}
				}
			}			
		};
		
		public abstract void execute() throws Exception;
		
		@Override
		public Object getOptions() {
			return this;
		}
	}
	
	@Option(name="--mode", aliases="-m", required=true, handler=ProxyOptionHandler.class, usage="Operation mode")
	private Mode mode;
	
	public void execute() throws Exception {
		mode.execute();
	}
	
	public static void main(String [] args) throws Exception {
		SequenceFileTool options = new SequenceFileTool();
		CmdLineParser parser = new CmdLineParser(options);

		try {
			parser.parseArgument(args);
		} catch (CmdLineException e) {
			System.err.println(e.getMessage());
			System.err.println("Usage: java -jar SequenceFileTool.jar [options...]");
			parser.printUsage(System.err);
			
			if (options.mode == null) {
				for (Mode m : Mode.values()) {
					System.err.println();
					System.err.println(m + " options: ");
					new CmdLineParser(m).printUsage(System.err);
				}
			}
			return;
		}

		options.execute();
	}
}
