package org.openimaj.image.indexing;

import org.openimaj.data.identity.Identifiable;
import org.openimaj.experiment.evaluation.retrieval.RetrievalEngine;
import org.openimaj.experiment.evaluation.retrieval.Scored;
import org.openimaj.image.ImageProvider;
import org.openimaj.image.MBFImage;

public interface IncrementalIndexer<DATA extends ImageProvider<MBFImage>, RESULT extends Identifiable & Scored, QUERY extends ImageProvider<MBFImage>>
		extends
		RetrievalEngine<RESULT, QUERY>
{
	public void indexImage(DATA image);
}
