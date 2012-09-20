package org.openimaj.text.nlp.textpipe.annotators;

import java.util.ArrayList;
import java.util.List;

import org.openimaj.text.nlp.namedentity.NamedEntity;
import org.openimaj.text.nlp.namedentity.YagoEntityExactMatcherFactory;
import org.openimaj.text.nlp.namedentity.YagoEntityExactMatcherFactory.YagoEntityExactMatcher;
import org.openimaj.text.nlp.textpipe.annotations.NamedEntityAnnotation;
import org.openimaj.text.nlp.textpipe.annotations.PhraseAnnotation;
import org.openimaj.text.nlp.textpipe.annotations.RawTextAnnotation;
import org.openimaj.text.nlp.textpipe.annotations.SentenceAnnotation;
import org.openimaj.text.nlp.textpipe.annotations.TokenAnnotation;

public class YagoNEAnnotator extends AbstractNEAnnotator{
	
	private int cw = 1; // On either side of current sentence.
	
	public YagoEntityExactMatcher yagoMatcher;
	
	public YagoNEAnnotator(){
		yagoMatcher=YagoEntityExactMatcherFactory.getMatcher();
	}

	@Override
	void performAnnotation(RawTextAnnotation annotation)
			throws MissingRequiredAnnotationException {
		//Get the sentences
		List<SentenceAnnotation> sentences = annotation.getAnnotationsFor(SentenceAnnotation.class);
		for(int i = 0; i < sentences.size();i++){
			//get context for sentence
			String context = getContextFrom(sentences.subList(Math.max(0, i-cw), Math.min(i+cw+1, sentences.size())));
			annotateSentence(sentences.get(i),context);
		}
	}

	private void annotateSentence(SentenceAnnotation sentence,
			String context) {
		
		List<NamedEntity> ents = yagoMatcher.matchExact(sentence.getAnnotationsFor(TokenAnnotation.class), context);
		
		for(NamedEntity ent: ents){
			NamedEntityAnnotation nea = new NamedEntityAnnotation();
			nea.namedEntity=ent;
			nea.tokensMatched.addAll(sentence.getAnnotationsFor(TokenAnnotation.class).subList(ent.startToken, ent.stopToken));
			sentence.addAnnotation(nea);
		}
		
		/*List<List<TokenAnnotation>> validEntityPhrases = getValidEntityPhrases(sentence);
		for(List<TokenAnnotation> entPhrase: validEntityPhrases){
			List<NamedEntity> ents = yagoMatcher.matchExact(entPhrase, context);
			for(NamedEntity ent: ents){
				NamedEntityAnnotation nea = new NamedEntityAnnotation();
				nea.namedEntity=ent;
				nea.tokensMatched.addAll(sentence.getAnnotationsFor(TokenAnnotation.class).subList(ent.startToken, ent.stopToken));
				sentence.addAnnotation(nea);
			}
		}*/
	}

	private List<List<TokenAnnotation>> getValidEntityPhrases(
			SentenceAnnotation sentence) {
		List<List<TokenAnnotation>> results = new ArrayList<List<TokenAnnotation>>();
		List<TokenAnnotation> current = new ArrayList<TokenAnnotation>();
		for(TokenAnnotation tok:sentence.getAnnotationsFor(TokenAnnotation.class)){
			if(start(tok)){
				current.add(tok);
			}
			else if(cont(tok)&&current.size()>0){
				current.add(tok);
			}
			else if(current.size()>0){
				results.add(current);
				current=new ArrayList<TokenAnnotation>();
			}
		}
		if(current.size()>0)results.add(current);
		return results;
	}

	private boolean cont(TokenAnnotation tok) {
		return (!tok.getAnnotationsFor(PhraseAnnotation.class).get(0).start);
	}

	private boolean start(TokenAnnotation tok) {
		PhraseAnnotation phrase = tok.getAnnotationsFor(PhraseAnnotation.class).get(0);
		if(phrase.phrase.toString().equals("NP"))
		return phrase.start;
		else return false;
	}

	private String getContextFrom(List<SentenceAnnotation> sents) {
		StringBuffer result = new StringBuffer();
		for(SentenceAnnotation sent:sents){
			result.append(sent.text+" ");
		}
		return result.toString();
	}

	@Override
	void checkForRequiredAnnotations(RawTextAnnotation annotation)
			throws MissingRequiredAnnotationException {
		if(!annotation.getAnnotationKeyList().contains(SentenceAnnotation.class))throw new MissingRequiredAnnotationException("No SentenceAnnotations found");
	}

}
