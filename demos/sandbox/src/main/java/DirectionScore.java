import java.util.Arrays;
import java.util.Collection;

import org.openimaj.ml.annotation.Annotated;



public class DirectionScore implements Annotated<Double, Direction> {
	private Direction dir;
	private double score;

	public DirectionScore(double score, Direction d) {
		this.score = score;
		this.dir = d;
	}
	@Override
	public Double getObject() {
		return score;
	}

	@Override
	public Collection<Direction> getAnnotations() {
		return Arrays.asList(dir);
	}

}
