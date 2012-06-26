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
package org.openimaj.hadoop.mapreduce;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.LocalFileSystem;
import org.apache.hadoop.fs.Path;
import org.openimaj.hadoop.mapreduce.stage.Stage;
import org.openimaj.hadoop.sequencefile.SequenceFileUtility;

/**
 * A set of hadoop jobs done in series. The final output (directory) of the nth job is used
 * as the map input path of the (n+1)th job.
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class MultiStagedJob {
	private Path outputRoot;
	private boolean removePreliminary;
	private LinkedList<Stage<?,?,?,?,?,?,?,?>> stages;
	private Path[] initial;
	private String[] toolArgs;
	private Map<String,Path[]> completedJobs;

	/**
	 * Start a multistaged job specification. The root path holds the final
	 * and all preliminary steps
	 * 
	 * @param initialInput the initial input given to the first stage
	 * @param root the final output location
	 * @param args the arguments with which to start the job
	 */
	public MultiStagedJob(Path[] initialInput, Path root, String[] args) {
		this(initialInput, root,false, args);
	}
	
	/**
	 * 
	 * Start a multistaged job specification. The root path holds the final.
	 * and all preliminary steps
	 * 
	 * @param initialInput the initial input given to the first stage
	 * @param removePreliminary whether all intermediate steps should be removed
	 * @param root the final output location
	 * @param args the arguments with which to start the job
	 */
	public MultiStagedJob(Path[] initialInput, Path root, boolean removePreliminary, String args[]) {
		this.outputRoot = root;
		this.initial = initialInput;
		this.removePreliminary = removePreliminary;
		this.stages = new LinkedList<Stage<?,?,?,?,?,?,?,?>>();
		this.toolArgs = args;
		this.completedJobs = new HashMap<String,Path[]>();
	}
	
	/**
	 * Conveniance function. Finds the input paths using #SequenceFileUtility.getFilePaths 
	 * and uses Path(outpath)
	 * @param inpath
	 * @param outpath
	 * @param args the arguments with which to start the job
	 * @throws IOException
	 */
	public MultiStagedJob(String inpath, String outpath, String[] args) throws IOException {
		this(SequenceFileUtility.getFilePaths(inpath, "path"),new Path(outpath),args);
	}

	/**
	 * Add a stage to the end of the queue of stages.
	 * @param s
	 */
	public void queueStage(Stage<?,?,?,?,?,?,?,?> s){
		this.stages.offer(s);
	}
	
	/**
	 * Run all the staged jobs. The input/output of each job is at follows:
	 * initial -> stage1 -> output1
	 * output1 -> stage2 -> output2
	 * ...
	 * output(N-1) -> stageN -> final
	 * 
	 * for each output, the directory created is scanned for part files matching the regex "part.*"
	 * @return The path to the final output for convenience (##base##/final by convention)
	 * @throws Exception 
	 */
	public Path runAll() throws Exception{
		Stage<?,?,?,?,?,?,?,?> s = null;
		Path[] currentInputs = initial;
		Path constructedOutputPath = null;
		List<String> toRemove = new ArrayList<String>();
		// Check if the final output exists, and if so that it is not empty, if so, we're done here! continue!
		constructedOutputPath = constructOutputPath(this.stages.getLast().outname());
		boolean finalOutputExists = fileExists(constructedOutputPath.toString());
		if(
				finalOutputExists && 
				SequenceFileUtility.getFilePaths(constructedOutputPath.toString(), "part").length != 0
		) return constructedOutputPath; // we're done, the output exists and it isn't empty!
		
		while((s = this.stages.pollFirst()) != null){
			constructedOutputPath = constructOutputPath(s.outname());
			boolean fExists = fileExists(constructedOutputPath.toString());
			if(
				!fExists || // if the file doesn't exist
				SequenceFileUtility.getFilePaths(constructedOutputPath.toString(), "part").length == 0 // or the file exists but the partfile does not
			){
				// At this point the file either doesn't exist or if it exists it had no part file, it should be deleted!
				if(fExists){
					System.out.println("File exists but was empty, removing");
					FileSystem fs = getFileSystem(constructedOutputPath.toUri());
					fs.delete(constructedOutputPath, true);
				}
				SingleStagedJob runner = new SingleStagedJob(s, currentInputs, constructedOutputPath );
				runner.runMain(this.toolArgs);
			}
			currentInputs = SequenceFileUtility.getFilePaths(constructedOutputPath.toString(), "part");
			// add the output of this stage to the list of stages to be removed
			if(this.removePreliminary && this.stages.size() > 0){
				toRemove.add(constructedOutputPath.toString());
			}
			completedJobs.put(s.outname(),currentInputs);
		}
		for (String toremove : toRemove) {
			System.out.println("Removing intermediate output: " + toremove);
			Path ptoremove = new Path(toremove);
			FileSystem fs = getFileSystem(ptoremove.toUri());
			fs.delete(ptoremove, true);
		}
		return constructedOutputPath;
	}
	
	private static boolean fileExists(String path) throws IOException{
		URI outuri = SequenceFileUtility.convertToURI(path);
		FileSystem fs = getFileSystem(outuri);
		Path p = new Path(outuri.toString());
		return fs.exists(p);
	}
	
	private static FileSystem getFileSystem(URI uri) throws IOException {
		Configuration config = new Configuration();
		FileSystem fs = FileSystem.get(uri, config);
		if (fs instanceof LocalFileSystem) fs = ((LocalFileSystem)fs).getRaw();
		return fs;
	}
	
	private Path constructOutputPath(String outname) {
		String newOutPath = this.outputRoot.toString();
		if(outname != null) newOutPath +=  "/" + outname;
		
		return new Path(newOutPath);
	}

	/**
	 * @param completedJobId
	 * @return the path to the output of the completed job
	 */
	public Path[] getStagePaths(String completedJobId) {
		return this.completedJobs.get(completedJobId);
	}

	/**
	 * @param b if true all but the final output and input for this multi staged job are true
	 */
	public void removeIntermediate(boolean b) {
		this.removePreliminary = b;
	}
}
