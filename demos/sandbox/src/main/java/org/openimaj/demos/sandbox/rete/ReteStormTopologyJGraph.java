package org.openimaj.demos.sandbox.rete;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JTextArea;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.ListenableDirectedGraph;
import org.openimaj.rdf.storm.sparql.topology.StormSPARQLReteTopologyOrchestrator;
import org.openimaj.rdf.storm.sparql.topology.builder.group.StaticDataFileNTriplesSPARQLReteTopologyBuilder;
import org.openimaj.storm.util.graph.StormGraphCreator;
import org.openimaj.storm.util.graph.StormGraphCreator.NamedNode;
import org.openimaj.storm.util.graph.StormGraphCreator.NamingStrategy.AlphabeticNamingStrategy;

import backtype.storm.generated.StormTopology;

import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.model.mxCell;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.view.mxGraph;

import eu.larkc.csparql.streams.formats.TranslationException;

public class ReteStormTopologyJGraph extends JFrame {
	static Logger logger = Logger.getRootLogger();
	static {
		logger.setLevel(Level.ERROR);
	}

	private static final Dimension DEFAULT_SIZE = new Dimension(800, 600);

	public ReteStormTopologyJGraph() {

	}

	public static void main(String[] args) throws TranslationException, IOException {
		String sparqlSource = "/test.userpost.subquery.complex.csparql";
		StormSPARQLReteTopologyOrchestrator orchestrator = StormSPARQLReteTopologyOrchestrator.createTopologyBuilder(
				new StaticDataFileNTriplesSPARQLReteTopologyBuilder(),
				ReteStormTopologyJGraph.class.getResourceAsStream(sparqlSource)
				);
		System.out.println(orchestrator);
		StormTopology rtop = orchestrator.buildTopology();

		final AlphabeticNamingStrategy strat = new AlphabeticNamingStrategy();
		ListenableDirectedGraph<NamedNode, DefaultEdge> graph = StormGraphCreator.asGraph(rtop, strat);
		ReteStormTopologyJGraph frame = new ReteStormTopologyJGraph();
		//		JGraphModelAdapter<NamedNode, DefaultEdge> adp = new JGraphModelAdapter<NamedNode, DefaultEdge>( graph ) ;
		mxGraph mxg = mxGraphUtils.fromJGraphT(graph);
		mxHierarchicalLayout layout = new mxHierarchicalLayout(mxg);
		layout.execute(mxg.getDefaultParent());
		final mxGraphComponent cmp = new mxGraphComponent(mxg);
		final JTextArea outputArea = new JTextArea(10, 40);
		cmp.getGraphControl().addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				Object cell = cmp.getCellAt(e.getX(), e.getY());
				if (cell != null && cell instanceof mxCell) {
					System.out.println("Cell selected");
					mxCell mxc = ((mxCell) cell);
					if (mxc.isVertex()) {

						String n = ((NamedNode) mxc.getValue()).name;
						String compName = strat.lookup.get(n);
						outputArea.setText(n + ": " + compName);
					}
				}
			}
		});
		frame.getContentPane().setLayout(new BorderLayout());
		frame.getContentPane().add(cmp, BorderLayout.CENTER);
		frame.getContentPane().add(outputArea, BorderLayout.SOUTH);
		frame.pack();
		frame.setVisible(true);
	}
}
