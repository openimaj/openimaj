package org.openimaj.text.nlp.textpipe.annotations;

//
//import org.openimaj.text.nlp.textpipe.annotations.POSAnnotation.PartOfSpeech;
//import org.openimaj.text.nlp.textpipe.annotations.PhraseAnnotation.Phrase;
//import org.openimaj.text.nlp.textpipe.annotators.DefaultTokenAnnotator;
//import org.openimaj.text.nlp.textpipe.annotators.MissingRequiredAnnotationException;
//import org.openimaj.text.nlp.textpipe.annotators.OpenNLPPOSAnnotator;
//import org.openimaj.text.nlp.textpipe.annotators.OpenNLPPhraseChunkAnnotator;
//import org.openimaj.text.nlp.textpipe.annotators.OpenNLPSentenceAnnotator;
//import org.openimaj.text.nlp.textpipe.annotators.OpenNLPTokenAnnotator;
//
//public class PipePlayground {
//
//	public static void main(String[] args) {
//
//		RawTextAnnotation rta = new RawTextAnnotation(
//				"Hello there, this is the first sentence. And this is the second sentence which mentions Samsung in a nice way");
//		OpenNLPTokenAnnotator ta = new OpenNLPTokenAnnotator();
//		OpenNLPSentenceAnnotator sa = new OpenNLPSentenceAnnotator();
//		OpenNLPPOSAnnotator pa = new OpenNLPPOSAnnotator();
//		OpenNLPPhraseChunkAnnotator pca = new OpenNLPPhraseChunkAnnotator();
//		sa.annotate(rta);
//		ta.annotate(rta);
//		try {
//			pa.annotate(rta);
//		} catch (MissingRequiredAnnotationException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		try {
//			pca.annotate(rta);
//		} catch (MissingRequiredAnnotationException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		for (SentenceAnnotation sentence : rta
//				.getAnnotationsFor(SentenceAnnotation.class)) {
//			System.out.println(sentence.text);
//			for(TokenAnnotation token : sentence.getAnnotationsFor(TokenAnnotation.class)){
//				PartOfSpeech pos = token.getAnnotationsFor(POSAnnotation.class).get(0).pos;
//				Phrase ph = token.getAnnotationsFor(PhraseAnnotation.class).get(0).phrase;
//				System.out.println(token.stringToken+"  "+pos.toString()+"  "+pos.DESCRIPTION+"    "+ph.toString());
//			}
//		}
//	}
//
// }
