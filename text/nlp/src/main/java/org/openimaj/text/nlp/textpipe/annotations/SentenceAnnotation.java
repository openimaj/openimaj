package org.openimaj.text.nlp.textpipe.annotations;

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
