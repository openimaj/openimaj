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
package org.openimaj.tools.clusterquantiser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.ProxyOptionHandler;
import org.openimaj.ml.clustering.Cluster;
import org.openimaj.util.array.ByteArrayConverter;


public class ClusterQuantiserOptions extends AbstractClusterQuantiserOptions{
	public ClusterQuantiserOptions(String[] args) {
		super(args);
	}

	public ClusterQuantiserOptions() {
		super(null);
	}

	@Option(name = "--create", aliases = "-c", required = false, usage = "Create a new vocabulary and save as FILE.", metaVar = "String ")
	private String createFile;
	private boolean create_mode = false;

	@Option(name = "--batched-samples", aliases = "-bs", required = false, usage = "Batched sample mode.", metaVar = "BOOLEAN")
	private boolean batchedSampleMode = false;
	
	@Option(name = "--cluster-type", aliases = "-ct", required = false, usage = "Specify the type of file to be read.", handler = ProxyOptionHandler.class)
	protected ClusterType clusterType = ClusterType.HKMEANS;
	
	protected Class<Cluster<?,?>> clusterClass = ClusterType.HKMEANS.getClusterClass();
	protected Class<Cluster<?,?>> otherClusterClass = ClusterType.HKMEANS.getClusterClass();

	@Option(name = "--samples", aliases = "-s", required = false, usage = "Use NUMBER samples from the input.", metaVar = "NUMBER")
	private int samples = -1;

	@Option(name = "--samples-file", aliases = "-sf", required = false, usage = "Save the samples to a file. Load them from this file if it exists", metaVar = "FILE")
	protected File samplesFile = null;
	protected boolean samplesFileMode = false;
	private byte[][] sampleKeypoints = null;
	
	@Option(name = "--input-file", aliases = "-f", required = false, usage = "Read the input from those specified in FILE.", metaVar = "FILE")
	protected File input_file = null;

	@Option(name = "--output-folder", aliases = "-o", required = false, usage = "Where to output all the quantised loc files", metaVar = "FILE")
	private File output_file = null;
	private ClusterType otherClusterType;
	
	
	public boolean isSamplesFileMode() {
		return samplesFileMode;
	}

	public byte[][] getSampleKeypoints() {
		return sampleKeypoints;
	}
	
	public int getSamples() {
		return samples;
	}

	public File getSamplesFile() {
		return this.samplesFile;
	}
	
	@Override
	public ClusterType getClusterType() {
		return this.clusterType;
	}

	public void loadSamplesFile() {
		if (this.sampleKeypoints != null)
			return;
		System.err.println("Loading samples file...");
		ObjectInputStream ois = null;
		try {
			ois = new ObjectInputStream(new FileInputStream(this.getSamplesFile()));
			Object read = ois.readObject();
			if(read instanceof byte[][]){
				this.sampleKeypoints = (byte[][]) read;
			}
			else{
				this.sampleKeypoints = ByteArrayConverter.intToByte((int[][])read);
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				ois.close();
			} catch (IOException e) {
			}
		}
	}
	
	@Override
	public void validate() throws CmdLineException {
		if (createFile != null) {
			create_mode = true;
		}
		if (infoFile != null) {
			if (create_mode)
				throw new CmdLineException(null,
						"--info and --create are mutually exclusive.");
			this.clusterType = ClusterType.sniffClusterType(new File(infoFile));
			this.clusterClass = this.clusterType.getClusterClass();
			if(otherInfoFile != null){
				this.otherClusterType = ClusterType.sniffClusterType(new File(otherInfoFile));
				this.otherClusterClass = this.otherClusterType.getClusterClass();
			}
			info_mode = true;
		}
		File quantFile = null;
		if (quantLocation != null) {
			quantFile = new File(quantLocation);
			if (create_mode)
				throw new CmdLineException(null,
						"--quant and --create are mutually exclusive.");
			if (info_mode)
				throw new CmdLineException(null,
						"--quant and --info are mutually exclusive.");
			quant_mode = true;
			this.clusterType = ClusterType.sniffClusterType(quantFile);
			this.clusterClass = this.clusterType.getClusterClass();
		}
		if (samplesFile != null && samplesFile.exists()) {
			samplesFileMode = true;
			if (!this.batchedSampleMode)
				loadSamplesFile();
		}
		
		if (!create_mode && !info_mode && !quant_mode && samplesFile == null) {
			throw new CmdLineException(null, "");
		}

		if (samplesFile == null && !info_mode && fileType == null) {
			throw new CmdLineException(
					null,
					"--file-type must be specified with --create and --quant arguments. Or you must provied a --samples-file");
		}

		if (input_file != null && inputFiles.size() > 0)
			throw new CmdLineException(
					null,
					"Input files from the commandline arguments not supported with --input-file argument.");
		if(input_file != null && input_file.exists() != true){
			throw new CmdLineException(
					null,
					"--input-file input source does not exist");
		}
		if (this.getCountMode()) {
			if (this.extension.equals(".loc"))
				this.extension = ".counts";
		}
	}
	
	@Override
	public String getTreeFile() throws IOException {
		if (create_mode)
		{
			File createFileParent= new File(createFile).getParentFile();
			if(!createFileParent.exists()){
				if(!createFileParent.mkdirs()){
					throw new IOException("Invalid quant file");
				}
			}
			else{
				if (!createFileParent.isDirectory())
					throw new IOException("Invalid quant file");
			}
			return createFile;
		}
		return super.getTreeFile();
	}

	public boolean isCreateMode() {
		return create_mode;
	}
	
	public boolean isBatchedSampleMode() {
		return this.batchedSampleMode;
	}
	
	
	public List<File> getInputFiles() throws IOException {
		List<File> files = new ArrayList<File>();
		
		if (inputFiles.size() > 0) {
			return inputFiles;
		} else if (input_file != null) {
			BufferedReader br = null;
			try {
				br = new BufferedReader(new FileReader(input_file));

				String line;
				while ((line = br.readLine()) != null) {
					files.add(new File(line.trim()));
				}
			} finally {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		return files;
	}

	public synchronized File getOutputFile() throws IOException {
		if (this.output_file != null) {
			if (!output_file.exists())
				if (!output_file.mkdirs())
					throw new IOException("Invalid output file");
			if (output_file.exists())
				if (!output_file.isDirectory())
					throw new IOException("Invalid output file");
		}
		return this.output_file;
	}

	@Override
	public String getInputFileString() {
		String inputFiles = "";
		try {
			for(File f : this.getInputFiles()){
				inputFiles += f.getAbsolutePath() + " ";
			}
		} catch (IOException e) {
		}
		inputFiles = inputFiles.trim();
		return inputFiles;
	}

	@Override
	public String getOutputFileString() {
		try {
			return this.getOutputFile().getAbsolutePath();
		} catch (IOException e) {
			return null;
		}
	}

	@Override
	public String getOtherInfoFile() {
		return this.otherInfoFile;
	}
	@Override
	public ClusterType getOtherInfoType() {
		return this.otherClusterType;
	}

	@Override
	public Class<Cluster<?,?>> getClusterClass() {
		return this.clusterClass;
	}

	@Override
	public Class<Cluster<?,?>> getOtherInfoClass() {
		return this.otherClusterClass;
	}

	public void setInputFiles(List<File> files) {
		this.inputFiles = files;
	}

	public void setClusterType(ClusterType clusterType) {
		this.clusterType = clusterType;
	}
}
