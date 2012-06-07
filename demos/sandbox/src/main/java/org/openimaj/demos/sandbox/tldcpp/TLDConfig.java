package org.openimaj.demos.sandbox.tldcpp;

import java.io.File;

import org.openimaj.demos.sandbox.tldcpp.TLDConfig.Settings;
import org.openimaj.math.geometry.shape.Rectangle;

public class TLDConfig {
	
	static class Settings{
		private boolean m_useProportionalShift;
		private boolean m_varianceFilterEnabled;
		private boolean m_ensembleClassifierEnabled;
		private boolean m_nnClassifierEnabled;
		private boolean m_loadModel;
		private boolean m_trackerEnabled;
		private boolean m_selectManually;
		private boolean m_learningEnabled;
		private boolean m_showOutput;
		private boolean m_showNotConfident;
		private boolean m_showColorImage;
		private boolean m_showDetections;
		private boolean m_showForeground;
		private boolean m_saveOutput;
		private boolean m_alternating;
		private boolean m_exportModelAfterRun;
		private int m_trajectory;
		private int m_startFrame;
		private int m_lastFrame;
		private int m_minScale;
		private int m_maxScale;
		private int m_numFeatures;
		private int m_numTrees;
		private float m_thetaP;
		private float m_thetaN;
		private int m_minSize;
		private int m_fps;
		private int m_seed;
		private double m_threshold;
		private float m_proportionalShift;
		private String m_modelExportFile;
		private Rectangle m_initialBoundingBox;

		public Settings(){
			m_useProportionalShift = true;
			m_varianceFilterEnabled = true;
			m_ensembleClassifierEnabled = true;
			m_nnClassifierEnabled = true;
			m_loadModel = false;
			m_trackerEnabled = true;
			m_selectManually = false;
			m_learningEnabled = true;
			m_showOutput = true;
			m_showNotConfident = true;
			m_showColorImage = false;
			m_showDetections = false;
			m_showForeground = false;
			m_saveOutput = false;
			m_alternating = false;
			m_exportModelAfterRun = false;
			m_trajectory = 20;
			m_minScale = -10;
			m_maxScale = 10;
			m_numFeatures = 10;
			m_numTrees = 10;
			m_thetaP = 0.65f;
			m_thetaN = 0.5f;
			m_minSize = 25;
			m_fps = 24;
			m_seed = 0;
			m_threshold = 0.7;
			m_proportionalShift = 0.1f;
			m_modelExportFile = "model";
			m_initialBoundingBox = new Rectangle();
		}
	}
	
	public static void tldConfig(TLDMain main) {
		Settings m_settings = new Settings();
		main.tld.trackerEnabled = m_settings.m_trackerEnabled;
		main.showOutput = m_settings.m_showOutput;
		main.printResults = new File("results.txt");
		main.saveDir = new File("savedir");
		main.threshold = m_settings.m_threshold;
		main.showForeground = m_settings.m_showForeground;
		main.showNotConfident = m_settings.m_showNotConfident;
		main.tld.alternating = m_settings.m_alternating;
		main.tld.learningEnabled = m_settings.m_learningEnabled;
		main.selectManually = m_settings.m_selectManually;
		main.exportModelAfterRun = m_settings.m_exportModelAfterRun;
		main.modelExportFile = new File(m_settings.m_modelExportFile);
		main.loadModel = m_settings.m_loadModel;
		main.seed = m_settings.m_seed;

		DetectorCascade detectorCascade = main.tld.detectorCascade;
		detectorCascade.varianceFilter.enabled = m_settings.m_varianceFilterEnabled;
		detectorCascade.ensembleClassifier.enabled = m_settings.m_ensembleClassifierEnabled;
		detectorCascade.nnClassifier.enabled = m_settings.m_nnClassifierEnabled;

		// classifier
		detectorCascade.useShift = m_settings.m_useProportionalShift;
		detectorCascade.shift = m_settings.m_proportionalShift;
		detectorCascade.minScale = m_settings.m_minScale;
		detectorCascade.maxScale = m_settings.m_maxScale;
		detectorCascade.minSize = m_settings.m_minSize;
		detectorCascade.numTrees = m_settings.m_numTrees;
		detectorCascade.numFeatures = m_settings.m_numFeatures;
		detectorCascade.nnClassifier.thetaTP = m_settings.m_thetaP;
		detectorCascade.nnClassifier.thetaFP = m_settings.m_thetaN;
		
	}

}
