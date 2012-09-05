package org.openimaj.text.nlp.textpipe.annotators;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;

import org.openimaj.text.nlp.textpipe.annotations.TokenAnnotation;

public class OpenNLPTokenAnnotator extends AbstractTokenAnnotator {
	
	Tokenizer tokenizer;

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
	protected List<TokenAnnotation> tokenise(String text) {
		ArrayList<TokenAnnotation> tla = new ArrayList<TokenAnnotation>(); 
		for(String token : tokenizer.tokenize(text)){
			tla.add(new TokenAnnotation(token, -1, -1));
		}
		return tla;
	}

}
