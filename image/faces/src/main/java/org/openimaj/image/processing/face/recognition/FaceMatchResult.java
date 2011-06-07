package org.openimaj.image.processing.face.recognition;

public class FaceMatchResult implements Comparable<FaceMatchResult> {
	String identifier;
	double score;
	
	public FaceMatchResult() {}
	
	public FaceMatchResult(String identifier, double score) {
		this.identifier = identifier;
		this.score = score;
	}
	
	public String getIdentifier() {
		return identifier;
	}
	
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}
	
	public double getScore() {
		return score;
	}
	
	public void setScore(double score) {
		this.score = score;
	}

	@Override
	public int compareTo(FaceMatchResult o) {
		if (score < o.score) return -1;
		if (score > o.score) return 1;
		return 0;
	}
	
	@Override
	public String toString() {
		return "FaceMatchResult{id=" + identifier + ", score=" + score + "}";
	}
}
