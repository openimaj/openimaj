package org.openimaj.text.nlp.textpipe.annotators;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import opennlp.tools.chunker.ChunkerME;
import opennlp.tools.chunker.ChunkerModel;

import org.openimaj.text.nlp.textpipe.annotations.AnnotationUtils;
import org.openimaj.text.nlp.textpipe.annotations.PhraseAnnotation;
import org.openimaj.text.nlp.textpipe.annotations.PhraseAnnotation.Phrase;
import org.openimaj.text.nlp.textpipe.annotations.RawTextAnnotation;
import org.openimaj.text.nlp.textpipe.annotations.TokenAnnotation;

/**
 * Phrase chunker instantiating a {@link ChunkerME} backed by a {@link ChunkerModel}
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class OpenNLPPhraseChunkAnnotator extends AbstractPhraseAnnotator {
	/**
	 * The system property
	 */
	public static final String PHRASE_MODEL_PROP = "org.openimaj.text.opennlp.models.chunker";
	ChunkerME chunker;

	/**
	 *
	 */
	public OpenNLPPhraseChunkAnnotator() {
		super();
		InputStream modelIn = null;
		ChunkerModel model = null;
		try {

			modelIn = OpenNLPPhraseChunkAnnotator.class.getResourceAsStream(System.getProperty(PHRASE_MODEL_PROP));
			model = new ChunkerModel(modelIn);
		} catch (IOException e) {
			// Model loading failed, handle the error
			e.printStackTrace();
		} finally {
			if (modelIn != null) {
				try {
					modelIn.close();
				} catch (IOException e) {
				}
			}
		}
		chunker = new ChunkerME(model);
	}

	@Override
	protected void phraseChunk(List<TokenAnnotation> tokens) {
		String[] tags = chunker.chunk(AnnotationUtils
				.ListToArray(AnnotationUtils
						.getStringTokensFromTokenAnnotationList(tokens)),
				AnnotationUtils.ListToArray(AnnotationUtils
						.getStringPOSsFromTokenAnnotationList(tokens)));
		for (int i = 0; i < tags.length; i++) {
			if (tags[i].contains("-")) {
				String[] comps = tags[i].split("-");
				boolean start = comps[0].equals("B");
				tokens.get(i).addAnnotation(
						new PhraseAnnotation(Phrase
								.getPhrasefromString(comps[1]), start));
			}
			else tokens.get(i).addAnnotation(
					new PhraseAnnotation(Phrase
							.getPhrasefromString(tags[i]),true));
		}
	}

	@Override
	void checkForRequiredAnnotations(RawTextAnnotation annotation)
			throws MissingRequiredAnnotationException {
		// TODO Auto-generated method stub

	}

}
