package org.openimaj.text.nlp.textpipe.pipes;

import org.openimaj.text.nlp.textpipe.annotations.RawTextAnnotation;
import org.openimaj.text.nlp.textpipe.annotators.MissingRequiredAnnotationException;
import org.openimaj.text.nlp.textpipe.annotators.OpenNLPPOSAnnotator;
import org.openimaj.text.nlp.textpipe.annotators.OpenNLPPhraseChunkAnnotator;
import org.openimaj.text.nlp.textpipe.annotators.OpenNLPSentenceAnnotator;
import org.openimaj.text.nlp.textpipe.annotators.OpenNLPTokenAnnotator;
import org.openimaj.text.nlp.textpipe.annotators.YagoNEAnnotator;

public class YagoEntityPipe {
	
	public OpenNLPTokenAnnotator ta;
	public OpenNLPSentenceAnnotator sa;
	public OpenNLPPOSAnnotator pa;
	public OpenNLPPhraseChunkAnnotator pca;
	public YagoNEAnnotator yna;
	
	public YagoEntityPipe(){
		ta = new OpenNLPTokenAnnotator();
		sa = new OpenNLPSentenceAnnotator();
		pa = new OpenNLPPOSAnnotator();
		pca = new OpenNLPPhraseChunkAnnotator();
		yna = new YagoNEAnnotator();
	}
	
	public RawTextAnnotation annotate(String text){
		RawTextAnnotation rta = new RawTextAnnotation(text);
		try {
			sa.annotate(rta);
			ta.annotate(rta);
			pa.annotate(rta);
			pca.annotate(rta);
			yna.annotate(rta);
		} catch (MissingRequiredAnnotationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return rta;
	}

}
