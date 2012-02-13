package org.openimaj.tools.twitter.options;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.ProxyOptionHandler;
import org.openimaj.tools.twitter.modes.TwitterOutputMode;
import org.openimaj.tools.twitter.modes.TwitterOutputModeOption;
import org.openimaj.tools.twitter.modes.TwitterPreprocessingModeOption;
import org.openimaj.tools.twitter.modes.preprocessing.TwitterPreprocessingMode;

public abstract class AbstractTwitterPreprocessingToolOptions {
	@Option(name="--input", aliases="-i", required=true, usage="Input tweets", metaVar="STRING")
	String input = null;
	
	@Option(name="--output", aliases="-o", required=false, usage="Tweet output location", metaVar="STRING")
	String output = null;
	
	@Option(name="--remove-existing-output", aliases="-rm", required=false, usage="If existing output exists, remove it")
	boolean force = false;
	
	@Option(name="--mode", aliases="-m", required=true, usage="How should the tweets be processed.", handler=ProxyOptionHandler.class)
	TwitterPreprocessingModeOption modeOption;
	
	@Option(name="--encoding", aliases="-e", required=false, usage="The outputstreamwriter's text encoding", metaVar="STRING")
	String encoding = "UTF-8";
	
	@Option(name="--output-mode", aliases="-om", required=false, usage="How should the analysis be outputed.", handler=ProxyOptionHandler.class)
	TwitterOutputModeOption outputModeOption = TwitterOutputModeOption.APPEND;
	
	@Option(name="--n-tweets", aliases="-n", required=false, usage="How many tweets from the input should this be applied to.", handler=ProxyOptionHandler.class)
	int nTweets = -1;
	
	@Option(name="--quiet", aliases="-q", required=false, usage="Control the progress messages.")
	boolean quiet = false;
	
	@Option(name="--verbose", aliases="-v", required=false, usage="Be very loud (overrides queit)")
	boolean veryLoud = false;

	private String[] args;
	
	public AbstractTwitterPreprocessingToolOptions(String[] args) {
		this.args = args;
		this.prepare();
	}
	
	public void prepare(){
		CmdLineParser parser = new CmdLineParser(this);
		try {
			if(veryLoud && quiet){
				quiet = false;
				veryLoud = true;
			}
			parser.parseArgument(args);
			this.validate();
		} catch (CmdLineException e) {
			System.err.println(e.getMessage());
			System.err.println("Usage: java -jar JClusterQuantiser.jar [options...] [files...]");
			parser.printUsage(System.err);
			System.err.print(this.getExtractUsageInfo());
			System.exit(1);
		}
		
	}
	private String getExtractUsageInfo() {
		return "Preprocess tweets for bag of words analysis";
	}
	
	public TwitterPreprocessingMode preprocessingMode(){
		return modeOption.createMode();
	}
	
	public TwitterOutputMode ouputMode(){
		return outputModeOption.createMode(modeOption.createMode());
	}
	
	
	public abstract boolean validate() throws CmdLineException;

	public void progress(String string) {
		if(!quiet){
			System.out.print(string);
		}
	}
	
	public boolean veryLoud() {
		return this.veryLoud;
	}
}
