package org.openimaj.tools;

import org.kohsuke.args4j.Option;

/**
 * A file tool reads and writes files and knows whether existing outputs should be deleted
 * 
 * @author ss
 *
 */
public abstract class InOutToolOptions {
	
	@Option(name="--input", aliases="-i", required=true, usage="Input tweets", metaVar="STRING")
	String input = null;
	
	@Option(name="--output", aliases="-o", required=false, usage="Tweet output location", metaVar="STRING")
	String output = null;
	
	@Option(name="--remove-existing-output", aliases="-rm", required=false, usage="If existing output exists, remove it")
	boolean force = false;
	
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
}
