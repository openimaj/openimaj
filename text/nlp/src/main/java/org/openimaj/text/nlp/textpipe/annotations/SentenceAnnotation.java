package org.openimaj.text.nlp.textpipe.annotations;

/**
 * {@link SentenceAnnotation} is an extension of {@link RawTextAnnotation} as
 * its function is to encapsulate a substring of raw text.
 * 
 * @author laurence
 * 
 */
public class SentenceAnnotation extends RawTextAnnotation {

	public int start;
	public int stop;

	public SentenceAnnotation(String rawText, int start, int stop) {
		super(rawText);
		this.start = start;
		this.stop = stop;
	}

	public SentenceAnnotation(String rawText) {
		super(rawText);
	}
}
