package org.openimaj.tools.reddit;

import java.io.File;
import java.util.List;

import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.ProxyOptionHandler;
import org.openimaj.tools.InOutToolOptions;
import org.openimaj.twitter.collection.StreamJSONStatusList.ReadableWritableJSON;
import org.openimaj.util.list.AbstractStreamBackedList;

/**
 * A split mode is told of the most recent items read and suggests a filename 
 * @author Jon Hare (jsh2@ecs.soton.ac.uk), Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public abstract class SplitMode {
	
	@Option(name="--split-file-root", aliases="-sfr", required=false, usage="the root name of the split file.")
	protected String splitFileRoot = "reddit.";
	
	public InOutToolOptions options;
	/**
	 * @param read
	 */
	public abstract void output(List<ReadableWritableJSON> read);
	
	/**
	 * @param output the output location, the split mode is informed so a sensible decision regarding filenames can be made
	 */
	public void init(InOutToolOptions options){
		this.options = options;
	}
}
