/**
 * Copyright (c) 2012, The University of Southampton and the individual contributors.
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
package org.openimaj.tools.twitter.options;

import java.util.ArrayList;
import java.util.List;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.ProxyOptionHandler;
import org.openimaj.tools.InOutToolOptions;
import org.openimaj.tools.twitter.modes.filter.TwitterPreprocessingFilterOption;
import org.openimaj.tools.twitter.modes.filter.TwitterPreprocessingPredicate;
import org.openimaj.tools.twitter.modes.output.TwitterOutputMode;
import org.openimaj.tools.twitter.modes.output.TwitterOutputModeOption;
import org.openimaj.tools.twitter.modes.preprocessing.TwitterPreprocessingMode;
import org.openimaj.tools.twitter.modes.preprocessing.TwitterPreprocessingModeOption;
import org.openimaj.twitter.GeneralJSON;
import org.openimaj.twitter.GeneralJSONRDF;
import org.openimaj.twitter.USMFStatus;
import org.openimaj.twitter.collection.TwitterStatusListUtils;

/**
 * An abstract kind of twitter processing tool. Contains all the options generic
 * to this kind of tool, not dependant on files or hadoop or whatever.
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 */
public abstract class AbstractTwitterPreprocessingToolOptions extends InOutToolOptions {

	@Option(
			name = "--mode",
			aliases = "-m",
			required = false,
			usage = "How should the tweets be processed.",
			handler = ProxyOptionHandler.class,
			multiValued = true)
	List<TwitterPreprocessingModeOption> modeOptions = new ArrayList<TwitterPreprocessingModeOption>();
	/**
	 * The preprocessing to perform
	 */
	public List<TwitterPreprocessingMode<?>> modeOptionsOp = new ArrayList<TwitterPreprocessingMode<?>>();

	@Option(
			name = "--pre-filter",
			aliases = "-prf",
			required = false,
			usage = "Define filters. Applied before other processing.",
			handler = ProxyOptionHandler.class,
			multiValued = true)
	List<TwitterPreprocessingFilterOption> preFilterOptions = new ArrayList<TwitterPreprocessingFilterOption>();
	/**
	 * The prefiltering to perform
	 */
	public List<TwitterPreprocessingPredicate> preFilterOptionsOp = new ArrayList<TwitterPreprocessingPredicate>();

	@Option(
			name = "--post-filter",
			aliases = "-pof",
			required = false,
			usage = "Define filters. Applied after other processing",
			handler = ProxyOptionHandler.class,
			multiValued = true)
	List<TwitterPreprocessingFilterOption> postFilterOptions = new ArrayList<TwitterPreprocessingFilterOption>();
	/**
	 * the postfiltering to perform
	 */
	public List<TwitterPreprocessingPredicate> postFilterOptionsOp = new ArrayList<TwitterPreprocessingPredicate>();
	//
	@Option(
			name = "--encoding",
			aliases = "-e",
			required = false,
			usage = "The outputstreamwriter's text encoding",
			metaVar = "STRING")
	String encoding = "UTF-8";

	@Option(
			name = "--output-mode",
			aliases = "-om",
			required = false,
			usage = "How should the analysis be outputed.",
			handler = ProxyOptionHandler.class)
	TwitterOutputModeOption outputModeOption = TwitterOutputModeOption.APPEND;
	TwitterOutputMode outputModeOptionOp = TwitterOutputModeOption.APPEND.getOptions();

	@Option(
			name = "--n-tweets",
			aliases = "-n",
			required = false,
			usage = "How many tweets from the input should this be applied to.",
			handler = ProxyOptionHandler.class)
	int nTweets = -1;

	@Option(name = "--quiet", aliases = "-q", required = false, usage = "Control the progress messages.")
	boolean quiet = false;

	@Option(name = "--verbose", aliases = "-v", required = false, usage = "Be very loud (overrides queit)")
	boolean veryLoud = false;

	@Option(
			name = "--time-before-skip",
			aliases = "-t",
			required = false,
			usage = "Time to wait before skipping an entry")
	long timeBeforeSkip = 0;

	/**
	 * the status type to take as input
	 */
	@Option(
			name = "--input-type",
			aliases = "-it",
			required = false,
			usage = "The type of social media message being consumed")
	public StatusType statusType = StatusType.TWITTER;

	/**
	 * the status type to output
	 */
	@Option(name = "--output-type", aliases = "-ot", required = false, usage = "How to output, defaults to USMF")
	public StatusType outputStatusType = StatusType.USMF;

	private String[] args;

	/**
	 * @param args
	 *            the arguments, prepared using the prepare method
	 * @param prepare
	 *            whether prepare should be called now or later
	 */
	public AbstractTwitterPreprocessingToolOptions(String[] args, boolean prepare) throws CmdLineException{
		this.args = args;
		if (prepare)
			this.prepare();
	}

	/**
	 * @param args
	 *            the arguments, prepared using the prepare method
	 */
	public AbstractTwitterPreprocessingToolOptions(String[] args) throws CmdLineException{
		this(args, true);
	}

	/**
	 * prepare the tool for running
	 */
	public void prepare() throws CmdLineException{
		final CmdLineParser parser = new CmdLineParser(this);
		try {
			if (veryLoud && quiet) {
				quiet = false;
				veryLoud = true;
			}
			parser.parseArgument(args);
			InOutToolOptions.prepareMultivaluedArgument(modeOptions);
			validateFilters();
			registerRDFAnalysis();
			this.validate();
		} catch (final CmdLineException e) {
			throw e;
		}

	}

	private void registerRDFAnalysis() {
		if (this.outputStatusType == StatusType.RDF) {
			for (final TwitterPreprocessingMode<?> modes : this.modeOptionsOp) {
				GeneralJSONRDF.registerRDFAnalysisProvider(modes.getAnalysisKey(), modes.rdfAnalysisProvider());
			}
		}
	}

	private void validateFilters() {
		for (final TwitterPreprocessingPredicate filter : this.postFilterOptionsOp) {
			filter.validate();
		}
		for (final TwitterPreprocessingPredicate filter : this.preFilterOptionsOp) {
			filter.validate();
		}
	}

	private String getExtractUsageInfo() {
		return "Preprocess tweets for bag of words analysis";
	}

	/**
	 * @return an instance of the selected preprocessing mode
	 * @throws Exception
	 */
	public List<TwitterPreprocessingMode<?>> preprocessingMode() throws Exception {
		if (veryLoud) {
			System.out.println("Creating preprocessing modes");
		}
		final ArrayList<TwitterPreprocessingMode<?>> modes = new ArrayList<TwitterPreprocessingMode<?>>();
		for (final TwitterPreprocessingModeOption modeOpt : this.modeOptions) {
			modes.add(modeOpt.getOptions());
		}
		return modes;
	}

	/**
	 * @return an instance of the selected output mode
	 */
	public TwitterOutputMode ouputMode() {
		outputModeOptionOp.validate(this);
		return outputModeOptionOp;
	}

	/**
	 * @return whether the options provided make sense
	 * @throws CmdLineException
	 */
	public abstract boolean validate() throws CmdLineException;

	/**
	 * @param string
	 *            print progress if we are not being quiet
	 */
	public void progress(String string) {
		if (!quiet) {
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
	 * @return the time to wait while analysing a tweet before it is skipped
	 *         over
	 */
	public long getTimeBeforeSkip() {
		return this.timeBeforeSkip;
	}

	/**
	 * @return the encoding
	 */
	public String getEncoding() {
		return encoding;
	}

	/**
	 * Check the internal preprocessing filters and say whether a given status
	 * should be skipped
	 * 
	 * @param twitterStatus
	 * @return whether to skip a status
	 */
	public boolean preProcessesSkip(USMFStatus twitterStatus) {
		boolean skip = false;
		for (final TwitterPreprocessingPredicate f : preFilterOptionsOp) {
			skip = !f.test(twitterStatus);
			if (skip)
				break;
		}
		return skip;
	}

	/**
	 * Check the internal postprocessing filters and say whether a given status
	 * should be skipped
	 * 
	 * @param twitterStatus
	 * @return whether to skip a status
	 */
	public boolean postProcessesSkip(USMFStatus twitterStatus) {
		boolean skip = false;
		for (final TwitterPreprocessingPredicate f : postFilterOptionsOp) {
			skip = !f.test(twitterStatus);
			if (skip)
				break;
		}
		return skip;
	}

	/**
	 * Provides the functionality to convert to the required output format as
	 * specified by -ot
	 * 
	 * @param twitterStatus
	 * @return the converted output
	 */
	public GeneralJSON convertToOutputFormat(USMFStatus twitterStatus) {
		final GeneralJSON outInstance = TwitterStatusListUtils.newInstance(this.outputStatusType.type());
		outInstance.fromUSMF(twitterStatus);
		return outInstance;
	}
	
	/**
	 * @return the input status type
	 */
	public StatusType  getInputClass() {
		return this.statusType;
	}
	
	/**
	 * @return the input status type
	 */
	public StatusType getOutputClass() {
		return this.outputStatusType;
	}
}
