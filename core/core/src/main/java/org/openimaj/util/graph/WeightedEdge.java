package org.openimaj.util.graph;

import java.util.Comparator;

public class WeightedEdge<VERTEX> extends Edge<VERTEX>{
	public final static Comparator<WeightedEdge<?>> ASCENDING_COMPARATOR = new Comparator<WeightedEdge<?>>() {
		@Override
		public int compare(WeightedEdge<?> o1, WeightedEdge<?> o2) {
			if (o1.weight == o2.weight) return 0;
			return o1.weight < o2.weight ? -1 : 1;
		}
	};
	
	public final static Comparator<WeightedEdge<?>> DESCENDING_COMPARATOR = new Comparator<WeightedEdge<?>>() {
		@Override
		public int compare(WeightedEdge<?> o1, WeightedEdge<?> o2) {
			if (o1.weight == o2.weight) return 0;
			return o1.weight < o2.weight ? 1 : -1;
		}
	};
	
	public float weight;
}
