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
package org.openimaj.ml.linear.experiments.sinabill;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.openimaj.io.IOUtils;
import org.openimaj.ml.linear.learner.BilinearLearnerParameters;

public abstract class BilinearExperiment {
	private static final String EXPERIMENT_NAME = "%s/%s/%s_%s";
	private static final String PARAMS_NAME = ".paramsascii";
	private static final String PARAMS_DATA_NAME = ".params";

	private static final String BILL_DATA_ROOT = "%s/TrendMiner/deliverables/year2-18month/Austrian Data/";
	private static final String BILL_DATA = "%s/data.mat";
	
	Logger logger = Logger.getLogger(getClass());
	private static long experimentTime = -1;
	
	protected void prepareExperimentLog(BilinearLearnerParameters params) throws IOException {
		ConsoleAppender console = new ConsoleAppender(); //create appender
		//configure the appender
		String PATTERN = "[%p->%C{1}] %m%n";
		console.setLayout(new PatternLayout(PATTERN)); 
		console.setThreshold(Level.DEBUG);
		console.activateOptions();
	  	// add appender to any Logger (here is root)
		Logger.getRootLogger().addAppender(console);
		File expRoot = prepareExperimentRoot();
		
		IOUtils.write(params, new DataOutputStream(new FileOutputStream(new File(expRoot,PARAMS_DATA_NAME))));
		IOUtils.writeASCII(new File(expRoot,PARAMS_NAME), params);
		
		File logFile = new File(expRoot,"log");
		if(logFile.exists())logFile.delete();
		FileAppender file = new FileAppender(new PatternLayout(PATTERN), logFile.getAbsolutePath()); 
		file.setThreshold(Level.DEBUG);
		file.activateOptions();
		Logger.getRootLogger().addAppender(file );
		
	}
	
	public File prepareExperimentRoot() throws IOException {
		String experimentRoot = String.format(EXPERIMENT_NAME,DATA_ROOT(),getExperimentSetName(),getExperimentName(),""+currentExperimentTime());
		File expRoot = new File(experimentRoot);
		if(expRoot.exists() && expRoot.isDirectory()) return expRoot;
		logger.debug("Experiment root: " + expRoot);
		if(!expRoot.mkdirs()) throw new IOException("Couldn't prepare experiment output");
		return expRoot;
	}

	private long currentExperimentTime() {
		if(experimentTime==-1){
			experimentTime = System.currentTimeMillis();
		}
		return experimentTime;
	}
	protected String FOLD_ROOT(int fold) throws IOException {
		File foldRoot = new File(prepareExperimentRoot(),String.format("fold_%d",fold));
		if(!foldRoot.exists() && !foldRoot.mkdirs()) 
			throw new IOException("Failed creating the fold directory: " + foldRoot);
		return foldRoot.getAbsolutePath();
	}
	
	protected String MATLAB_DATA() {
		
		return String.format(BILL_DATA,DATA_ROOT());
	}
	
	protected String MATLAB_DATA(String data) {
		
		return String.format(data,DATA_ROOT());
	}
	
	protected String DATA_ROOT() {
		
		return String.format(BILL_DATA_ROOT,DROPBOX_HOME());
	}
	
	private String DROPBOX_HOME() {
		String home = System.getProperty("user.home");
		
		return String.format("%s/Dropbox",home);
	}
	
	public abstract void performExperiment() throws Exception;

	public String getExperimentName() {
		return "experiment";
	}
	
	public String getExperimentSetName() {
		return "streamingExperiments";
	}
}
