package org.openimaj.text.nlp.textpipe.annotators;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;

import org.apache.commons.lang.StringUtils;
import org.openimaj.text.nlp.textpipe.annotations.RawTextAnnotation;
import org.openimaj.text.nlp.textpipe.annotations.TokenAnnotation;

public class OpenNLPTokenAnnotator extends AbstractTokenAnnotator<OpenNLPTokenAnnotator> {
	
	TokenizerME tokenizer;

	public OpenNLPTokenAnnotator() {
		super();
		InputStream modelIn=null;
		modelIn = OpenNLPTokenAnnotator.class.getClassLoader().getResourceAsStream(
				"org/openimaj/text/opennlp/models/en-token.bin");
		TokenizerModel model = null;
		try {
			model = new TokenizerModel(modelIn);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (modelIn != null) {
				try {
					modelIn.close();
				} catch (IOException e) {
				}
			}
		}
		tokenizer = new TokenizerME(model);
	}
	
	

	@Override
	void checkForRequiredAnnotations(RawTextAnnotation annotation)
			throws MissingRequiredAnnotationException {
		
	}	


	@Override
	public String deTokenise(List<TokenAnnotation<OpenNLPTokenAnnotator>> tokens) {
		List<String> raws = new ArrayList<String>();
		for(TokenAnnotation<OpenNLPTokenAnnotator> token:tokens){
			raws.add(token.getRawString());
		}
		return StringUtils.join(raws,"");
	}



	@Override
	public List<TokenAnnotation<OpenNLPTokenAnnotator>> tokenise(String text) {
		List<TokenAnnotation<OpenNLPTokenAnnotator>> tla = new ArrayList<TokenAnnotation<OpenNLPTokenAnnotator>>();
		int currentOff =0;
		for(String token : tokenizer.tokenize(text)){
			int start = currentOff+(text.substring(currentOff).indexOf(token));
			int stop = start+token.length();
			tla.add(new TokenAnnotation<OpenNLPTokenAnnotator>(token,text.substring(currentOff,stop), start, stop));
			currentOff = stop;		
		}
		return tla;
	}

}
