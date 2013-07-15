package org.openimaj.image.searching;

import org.openimaj.data.identity.Identifiable;
import org.openimaj.experiment.evaluation.retrieval.Scored;

public class ImageSearchResult<METADATA extends Identifiable> implements Identifiable, Scored {
	METADATA metadata;
	double score;

	public ImageSearchResult(METADATA metadata, double score) {
		this.metadata = metadata;
		this.score = score;
	}

	@Override
	public double getScore() {
		return score;
	}

	@Override
	public String getID() {
		return metadata.getID();
	}

	public METADATA getMetadata() {
		return metadata;
	}
}
