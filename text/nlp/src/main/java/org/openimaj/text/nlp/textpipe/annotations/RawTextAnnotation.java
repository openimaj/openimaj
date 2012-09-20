package org.openimaj.text.nlp.textpipe.annotations;

/**
 * Text encapsulated as an {@link TextPipeAnnotation} so that it may be annotated.
 * @author laurence
 *
 */
public class RawTextAnnotation extends TextPipeAnnotation{
	
	/**
	 * The original text.
	 */
	public String text;
	
	@SuppressWarnings("javadoc")
	public RawTextAnnotation (String rawText){
		super();
		text=rawText;
	}

}
