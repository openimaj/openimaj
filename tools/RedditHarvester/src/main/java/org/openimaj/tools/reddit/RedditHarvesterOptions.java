package org.openimaj.tools.reddit;

import java.io.File;
import java.io.IOException;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.ProxyOptionHandler;
import org.openimaj.tools.FileToolsUtil;
import org.openimaj.tools.InOutToolOptions;

/**
 * 
 * @author Jon Hare (jsh2@ecs.soton.ac.uk), Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class RedditHarvesterOptions extends InOutToolOptions{
	
	/**
	 * the root reddit url  
	 */
	public static final String REDDIT_BASE_URL = "http://www.reddit.com";
	
	@Option(name="--split-mode", aliases="-sm", required=false, usage="How to split the reddit inputs.", handler=ProxyOptionHandler.class)
	SplitModeOption mode = SplitModeOption.TIME;
	SplitMode modeOp = mode.getOptions();
	
	private String[] args;
	/**
	 * @param args the arguments, prepared using the prepare method
	 * @param prepare whether prepare should be called now or later
	 */
	public RedditHarvesterOptions(String[] args, boolean prepare) {
		this.args = args;
		if(prepare) this.prepare();
	}
	
	/**
	 * @param args the arguments, prepared using the prepare method
	 */
	public RedditHarvesterOptions(String[] args) {
		this(args,true);
	}
	
	/**
	 * prepare the tool for running
	 */
	public void prepare(){
		CmdLineParser parser = new CmdLineParser(this);
		try {
			parser.parseArgument(args);
			this.validate();
		} catch (CmdLineException e) {
			System.err.println(e.getMessage());
			System.err.println("Usage: java -jar RedditHarvester.jar [options...] [files...]");
			parser.printUsage(System.err);
			System.err.println(this.getExtractUsageInfo());
			System.exit(1);
		}
		
	}

	private String getExtractUsageInfo() {
		return "";
	}

	private void validate() throws CmdLineException {
		if(this.getInput() == null || this.getInput() == ""){
			this.setInput("/all.json");
		}
		else if(!this.getInput().endsWith(".json")){
			this.setInput(this.getInput()+".json");
		}
		this.setOutput(this.getOutput() + File.separator + this.getInput());
		try {
			File fileOut = FileToolsUtil.validateLocalOutput(this);
			fileOut.mkdirs();
			
		} catch (IOException e) {
			throw new CmdLineException(null,e.getMessage());
		}
		
	}
}
