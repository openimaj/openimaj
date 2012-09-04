package org.openimaj.text.nlp.textpipe.annotations;

public class RawTextAnnotation extends TextPipeAnnotation{
	
	public String text;
	
	public RawTextAnnotation (String rawText){
		super();
		text=rawText;
	}

}
