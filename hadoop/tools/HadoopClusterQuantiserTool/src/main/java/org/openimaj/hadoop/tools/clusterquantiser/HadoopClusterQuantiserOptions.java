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
package org.openimaj.hadoop.tools.clusterquantiser;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.LocalFileSystem;
import org.apache.hadoop.fs.Path;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.Option;
import org.openimaj.hadoop.sequencefile.SequenceFileUtility;
import org.openimaj.ml.clustering.Cluster;

import org.openimaj.tools.clusterquantiser.AbstractClusterQuantiserOptions;
import org.openimaj.tools.clusterquantiser.ClusterType;

public class HadoopClusterQuantiserOptions extends AbstractClusterQuantiserOptions {
	private boolean  beforeMaps;
	public HadoopClusterQuantiserOptions(String[] args) throws CmdLineException {
		this(args,false);
	}
	
	public HadoopClusterQuantiserOptions(String[] args,boolean beforeMaps) throws CmdLineException {
		super(args);
		this.beforeMaps = beforeMaps;
	}

	/*
	 * IO args
	 */
	@Option(name = "--input", aliases="-i", required=true, usage="set the input sequencefile")
	private String input = null;
	
	@Option(name = "--output", aliases="-o", required=true, usage="set the output directory")
	private String output = null;
	
	@Option(name = "--force-delete", aliases="-rm", required=false, usage="If it exists, remove the output directory before starting")
	private boolean forceRM = false;

	private ClusterType clusterType;

	private Class<Cluster<?,?>> clusterClass;

	@Override
	public String getInputFileString() {
		return input;
	}

	@Override
	public String getOutputFileString() {
		return output;
	}

	@Override
	public void validate() throws CmdLineException {
		
		if (infoFile != null) {
			info_mode = true;
			try {
				this.clusterType = sniffClusterType(infoFile);
				if(this.clusterType == null) throw new CmdLineException(null,"Could not identify the clustertype");
				
				this.clusterClass = this.clusterType.getClusterClass();
			} catch (IOException e) {
				throw new CmdLineException(null, "Could not identify the clustertype. File: " + infoFile, e);
			}
			
		}
		if (quantLocation != null) {
			if (info_mode)
				throw new CmdLineException(null,
						"--quant and --info are mutually exclusive.");
			quant_mode = true;
			try {
				this.clusterType = sniffClusterType(quantLocation);
				if(this.clusterType == null) throw new CmdLineException(null,"Could not identify the clustertype");
				
				this.clusterClass = this.clusterType.getClusterClass();
			} catch (Exception e) {
				e.printStackTrace();
				throw new CmdLineException(null, "Could not identify the clustertype. File: " + quantLocation, e);
			}
		}
		
		if (this.getCountMode()) {
			if (this.extension.equals(".loc"))
				this.extension = ".counts";
		}
		if(forceRM && this.beforeMaps){
			
			try {
				URI outuri = SequenceFileUtility.convertToURI(this.output);
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
	
	public static ClusterType sniffClusterType(String quantFile) throws IOException {
		InputStream fios = null;
		try {
			fios = getClusterInputStream( quantFile );
			return ClusterType.sniffClusterType( new BufferedInputStream(fios) );
		} finally {
			if(fios!=null) try { fios.close(); } catch (IOException e) { /*don't care*/ } 
		}
	}

	@Override
	public ClusterType getClusterType() {
		return this.clusterType;
	}
	
	public static InputStream getClusterInputStream(String uriStr) throws IOException{
		URI uri = SequenceFileUtility.convertToURI(uriStr);
		FileSystem fs = getFileSystem(uri);
		Path p = new Path(uri.toString());
		return fs.open(p);
	}

	public InputStream getClusterInputStream() throws IOException {
		return getClusterInputStream(this.quantLocation);
	}

	public String getClusterInputString() {
		return this.quantLocation;
	}

	public Path[] getInputPaths() throws IOException {
		Path[] sequenceFiles = SequenceFileUtility.getFilePaths(this.getInputFileString(), "part");
		return sequenceFiles;
	}

	@Override
	public ClusterType getOtherInfoType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Class<Cluster<?,?>> getClusterClass() {
		return this.clusterClass;
	}

	@Override
	public Class<Cluster<?,?>> getOtherInfoClass() {
		return null;
	}
}
