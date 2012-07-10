package org.openimaj.usmf.preprocessing;

import java.io.UnsupportedEncodingException;

import org.openimaj.text.nlp.TweetTokeniser;
import org.openimaj.text.nlp.TweetTokeniserException;
import org.openimaj.twitter.USMFStatus;

/**
 * @author Laurence Willmore <lgw1e10@ecs.soton.ac.uk>
 * 
 *         This is a PipeSection wrapper for the openimaj TweetTokeniser. It
 *         takes a USMFStatus and returns a USMFStatus with Token
 *         Analysis added.
 * 
 */
public class TweetTokeniserPipe extends PipeSection<USMFStatus, USMFStatus> {

	/**
	 * Constructor
	 */
	public TweetTokeniserPipe() {
		super(USMFStatus.class, USMFStatus.class);
	}

	@Override
	protected USMFStatus doWork(USMFStatus job) {
		TweetTokeniser tokeniser = null;
		try {
			tokeniser = new TweetTokeniser(job.text);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TweetTokeniserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		job.addAnalysis("Tokens", tokeniser.getStringTokens());
		return job;
	}

}
