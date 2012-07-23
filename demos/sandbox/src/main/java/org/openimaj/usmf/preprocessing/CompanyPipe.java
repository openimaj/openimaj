package org.openimaj.usmf.preprocessing;

import java.util.HashMap;
import java.util.List;

import org.openimaj.text.nlp.namedentity.NamedEntityExtractor;
import org.openimaj.twitter.USMFStatus;

/**
 * @author Laurence Willmore <lgw1e10@ecs.soton.ac.uk>
 * 
 *         This is a PipeSection wrapper for the openimaj NLP
 *         AnnieCompanyExtractor. It takes a USMFStatus and returns a USMFStatus
 *         with Company Entity Analysis added.
 * 
 */
public class CompanyPipe extends PipeSection<USMFStatus, USMFStatus> {

	private NamedEntityExtractor ace;

	/**
	 * Constructor
	 */
	public CompanyPipe(NamedEntityExtractor e) {
		super(USMFStatus.class, USMFStatus.class);
		ace = e;
	}

	@Override
	protected USMFStatus doWork(USMFStatus job) {
		// check tokenisation
		if (job.analysis.containsKey("Tokens")) {
			@SuppressWarnings("unchecked")
			List<String> ts = (List<String>) job.analysis.get("Tokens");
			HashMap<Integer,String> found = (HashMap<Integer,String>) ace.getEntities(ts);
			job.addAnalysis("Companies", found);
			return job;
		}
		// do tokenisation if not done;
		else {
			TweetTokeniserPipe t = new TweetTokeniserPipe();
			t.pipe(job);
			return this.doWork(job);
		}
	}

}
