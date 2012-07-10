package org.openimaj.usmf.preprocessing;

import java.io.IOException;

import org.openimaj.text.nlp.language.LanguageDetector;
import org.openimaj.text.nlp.language.LanguageDetector.WeightedLocale;
import org.openimaj.twitter.USMFStatus;

/**
 * @author Laurence Willmore <lgw1e10@ecs.soton.ac.uk>
 * 
 *         This is a PipeSection wrapper for the openimaj NLP LanguageDetector
 *         It takes a USMFStatus and returns a USMFStatus with language
 *         detection Analysis added.
 * 
 */
public class LanguagePipe extends PipeSection<USMFStatus, USMFStatus> {

	LanguageDetector myDetector;

	/**
	 * Constructor hides the typed constructor of super class.
	 */
	public LanguagePipe() {
		super(USMFStatus.class, USMFStatus.class);
		try {
			myDetector = new LanguageDetector();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	protected USMFStatus doWork(USMFStatus job) {
		WeightedLocale loc = myDetector.classify(job.text);
		job.addAnalysis("Language", loc.language);
		return job;
	}

}
