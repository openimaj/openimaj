package org.openimaj.rdf.rete;

import org.openimaj.rdf.rete.nodes.AlphaStore;

public class Rete {
	private AlphaStore alphaNodeStore;

	public Rete() {
		this.alphaNodeStore = new AlphaStore();
//		this.betaNodeStore = new BetaStore();
//		this.betaIndex = new BetaIndex();
//		this.alphaIndex = new AlphaIndex();
	}
}
