package org.openimaj.lsh.sketch;

public interface Sketcher<IN, OUT> {
	OUT createSketch(IN input);
}
