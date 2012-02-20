package org.openimaj.tools.twitter.options;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.ProxyOptionHandler;
import org.openimaj.tools.twitter.modes.TwitterOutputMode;
import org.openimaj.tools.twitter.modes.TwitterOutputModeOption;
import org.openimaj.tools.twitter.modes.TwitterPreprocessingModeOption;
import org.openimaj.tools.twitter.modes.preprocessing.TwitterPreprocessingMode;

/**
 * An abstract kind of twitter processing tool. Contains all the options generic to this kind of tool, not dependant on
 * files or hadoop or whatever.
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>, Sina Samangooei <ss@ecs.soton.ac.uk>
 *
 */
public abstract class AbstractTwitterPreprocessingToolOptions {
	@Option(name="--input", aliases="-i", required=true, usage="Input tweets", metaVar="STRING")
	String input = null;
	
	@Option(name="--output", aliases="-o", required=false, usage="Tweet output location", metaVar="STRING")
	String output = null;
	
	@Option(name="--remove-existing-output", aliases="-rm", required=false, usage="If existing output exists, remove it")
	boolean force = false;
	
	@Option(name="--mode", aliases="-m", required=true, usage="How should the tweets be processed.", handler=ProxyOptionHandler.class, multiValued=true)
	List<TwitterPreprocessingModeOption> modeOptions = new ArrayList<TwitterPreprocessingModeOption>();
	
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
	
	@Option(name="--time-before-skip", aliases="-t", required=false, usage="Time to wait before skipping an entry")
	long timeBeforeSkip = 0;

	private String[] args;
	
	/**
	 * @param args the arguments, prepared using the prepare method
	 */
	public AbstractTwitterPreprocessingToolOptions(String[] args) {
		this.args = args;
		this.prepare();
	}
	
	/**
	 * prepare the tool for running
	 */
	public void prepare(){
		CmdLineParser parser = new CmdLineParser(this);
		try {
			if(veryLoud && quiet){
				quiet = false;
				veryLoud = true;
			}
			
			parser.parseArgument(args);
			// TODO: Fix this hack
			Set<TwitterPreprocessingModeOption> modes = new HashSet<TwitterPreprocessingModeOption>();
			for (TwitterPreprocessingModeOption mode : modeOptions) {
				modes.add(mode);
			}
			modeOptions.clear();
			modeOptions.addAll(modes);
//			System.out.println(Arrays.toString(args));
			System.out.println("Number of mode options: " + modeOptions.size());
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
	
	/**
	 * @return an instance of the selected preprocessing mode
	 * @throws Exception
	 */
	public List<TwitterPreprocessingMode> preprocessingMode() throws Exception{
		if(veryLoud){
			System.out.println("Creating preprocessing modes");
		}
		ArrayList<TwitterPreprocessingMode> modes = new ArrayList<TwitterPreprocessingMode>();
		for (TwitterPreprocessingModeOption modeOpt : this.modeOptions) {
			modes.add(modeOpt.createMode());
		}
		return modes;
	}
	
	/**
	 * @return an instance of the selected output mode
	 * @throws Exception
	 */
	public TwitterOutputMode ouputMode() throws Exception{
		return outputModeOption.createMode(modeOptions);
	}
	
	
	/**
	 * @return whether the options provided make sense
	 * @throws CmdLineException
	 */
	public abstract boolean validate() throws CmdLineException;

	/**
	 * @param string print progress if we are not being quiet
	 */
	public void progress(String string) {
		if(!quiet){
			System.out.print(string);
		}
	}
	
	/**
	 * @return print some extra information
	 */
	public boolean veryLoud() {
		return this.veryLoud;
	}
	
	/**
	 * @return the time to wait while analysing a tweet before it is skipped over
	 */
	public long getTimeBeforeSkip() {
		return this.timeBeforeSkip;
	}
	
	/**
	 * @return the input string option
	 */
	public String getInput(){
		return this.input;
	}
	/**
	 * @return the input string option
	 */
	public String getOutput(){
		return this.output;
	}
	
	/**
	 * @return the force option, whether the output should be overwritten if it exists
	 */
	public boolean overwriteOutput(){
		return this.force;
	}
	
	/**
	 * @return the encoding
	 */
	public String getEncoding() {
		return encoding;
	}
}
