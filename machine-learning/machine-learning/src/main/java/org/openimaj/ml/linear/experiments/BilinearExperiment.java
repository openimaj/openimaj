package org.openimaj.ml.linear.experiments;

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
	
	static Logger logger = Logger.getLogger(BillAustrianExperiments.class);
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
		String experimentRoot = String.format(EXPERIMENT_NAME,BILL_DATA_ROOT(),getExperimentSetName(),getExperimentName(),""+currentExperimentTime());
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
	
	protected String BILL_DATA() {
		
		return String.format(BILL_DATA,BILL_DATA_ROOT());
	}
	
	protected String BILL_DATA_ROOT() {
		
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
