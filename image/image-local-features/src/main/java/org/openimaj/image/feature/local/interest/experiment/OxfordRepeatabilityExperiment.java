/**
 * Copyright (c) 2011, The University of Southampton and the individual contributors.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *   * 	Redistributions of source code must retain the above copyright notice,
 * 	this list of conditions and the following disclaimer.
 *
 *   *	Redistributions in binary form must reproduce the above copyright notice,
 * 	this list of conditions and the following disclaimer in the documentation
 * 	and/or other materials provided with the distribution.
 *
 *   *	Neither the name of the University of Southampton nor the names of its
 * 	contributors may be used to endorse or promote products derived from this
 * 	software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.openimaj.image.feature.local.interest.experiment;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.openimaj.feature.local.list.LocalFeatureList;
import org.openimaj.feature.local.list.MemoryLocalFeatureList;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.Transforms;
import org.openimaj.image.feature.local.detector.ipd.collector.CircularInterestPointKeypoint;
import org.openimaj.image.feature.local.engine.ipd.FinderMode;
import org.openimaj.image.feature.local.engine.ipd.IPDSIFTEngine;
import org.openimaj.image.feature.local.interest.AffineAdaption;
import org.openimaj.image.feature.local.interest.HarrisIPD;
import org.openimaj.image.feature.local.interest.IPDSelectionMode;
import org.openimaj.image.feature.local.interest.InterestPointData;
import org.openimaj.image.feature.local.keypoints.InterestPointKeypoint;
import org.openimaj.io.IOUtils;

import Jama.Matrix;



public class OxfordRepeatabilityExperiment {
	private static final String DEFAULT_FEATURE_DUMP_PATH = "/tmp/featurePath";
	static Logger logger = Logger.getLogger(AffineAdaption.class);
	static{
		BasicConfigurator.configure();
		logger.setLevel(Level.DEBUG);
	}
	static class ExperimentException extends Exception{

		public ExperimentException(String string) {
			super(string);
		}

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		
	}
	
	private int nExperiments;
	private String transformName;
	private String imageName;
	private HashMap<String, List<InterestPointData>> features;
	private HashMap<String, Matrix> transforms;
	private HashMap<String, MBFImage> images;
	private ExperimentFeatureExtraction experiemtnFeatureExtraction;
	private String featureDumpPath = DEFAULT_FEATURE_DUMP_PATH;

	OxfordRepeatabilityExperiment(String experimentRoot, String transformName, String imageName, int nImages, ExperimentFeatureExtraction expFE, String featureDumpPath) throws IOException{
		this.nExperiments = nImages - 1;
		this.transformName = transformName;
		this.imageName = imageName;
		this.experiemtnFeatureExtraction = expFE;
		this.featureDumpPath = featureDumpPath;
		transforms = new HashMap<String,Matrix>();
		images = new HashMap<String, MBFImage>();
		
		String imageNameFormated = String.format(imageName, 1);
		String imageLocation = experimentRoot + "/" + imageNameFormated;
		logger.debug("Loading image: " + imageLocation );
		MBFImage im = ImageUtilities.readMBF(this.getClass().getResourceAsStream(imageLocation));
		if(im == null){
			throw new IOException("Can't load image: " + imageLocation);
		}
		images.put(imageNameFormated, im);
		
		for (int i = 1; i < nImages; i++) {
			String transformNameFormated = String.format(transformName, 1, i + 1);
			String transformLocation = experimentRoot + "/" + transformNameFormated;
			logger.debug("Loading trasnform: " + transformLocation);
			transforms.put(transformNameFormated,IPDRepeatability.readHomography(this.getClass().getResourceAsStream(transformLocation)));
			imageNameFormated = String.format(imageName, i+1);
			imageLocation = experimentRoot + "/" + imageNameFormated;
			logger.debug("Loading image: " + imageLocation );
			images.put(imageNameFormated, ImageUtilities.readMBF(this.getClass().getResourceAsStream(imageLocation)));
		}
		
		this.features = new HashMap<String, List<InterestPointData>>();
	}
	
	public OxfordRepeatabilityExperiment(String expBase, String transformName,String imgName, int nExperiments, ExperimentFeatureExtraction experiemtnFeatureExtraction) throws IOException {
		this(expBase, transformName, imgName, nExperiments, experiemtnFeatureExtraction, DEFAULT_FEATURE_DUMP_PATH);
	}

	public IPDRepeatability<InterestPointData> experimentWith(int n) throws ExperimentException{
		if(n > this.nExperiments) 
			return null;
		String image1Name = String.format(imageName, 1);
		String image2Name = String.format(imageName, n+1);
		
		if(!this.images.containsKey(image1Name) || this.images.get(image1Name)  == null){
			throw new ExperimentException("Couldn't load: " + image1Name);
		}
		
		if(!this.images.containsKey(image2Name)){
			throw new ExperimentException("Couldn't load: " + image2Name);
		}
		
		List<InterestPointData> image1Features = getFeatures(image1Name);
		List<InterestPointData> image2Features = getFeatures(image2Name);
		
		if(image1Features == null || image2Features == null){
			throw new ExperimentException("Couldn't load features correctly");
		}
		
		String transformNameFormatted = String.format(this.transformName, 1,n+1);
		
		if(!this.transforms.containsKey(transformNameFormatted)){
			throw new ExperimentException("Couldn't load: " + transformNameFormatted);
		}
		
		
		
		return IPDRepeatability.repeatability(
				this.images.get(image1Name), 
				this.images.get(image2Name), 
				image1Features, 
				image2Features, 
				this.transforms.get(transformNameFormatted), 
				4
		);
	}

	private List<InterestPointData> getFeatures(String imageNameFormatted) {
		
		if(!this.features.containsKey(imageNameFormatted)){
			File featureDump = new File(featureDumpPath ,String.format("%s/%s.%s",experiemtnFeatureExtraction.experimentName(),imageNameFormatted,experiemtnFeatureExtraction.experimentName()));
			featureDump.getParentFile().mkdir();
			LocalFeatureList<? extends InterestPointKeypoint<InterestPointData>> kpts = null;
			if(featureDump.exists()){
				try {
					kpts = MemoryLocalFeatureList.read(featureDump, CircularInterestPointKeypoint.class);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if(kpts==null){
//				HarrisIPD hIPD = new HarrisIPD(1.4f);
//				hIPD.setImageBlurred(true);
//				// AffineAdaption affineIPD = new AffineAdaption(harrisIPD,new
//				// IPDSelectionMode.Threshold(10000f));
//				IPDSIFTEngine engine = new IPDSIFTEngine(hIPD);
//				engine.setAcrossScales(true);
//				engine.setFinderMode(new FinderMode.Characteristic<InterestPointData>());
				IPDSIFTEngine engine = experiemtnFeatureExtraction.engine();
				kpts = engine.findFeatures(Transforms.calculateIntensityNTSC(this.images.get(imageNameFormatted)));
				
				try {
					IOUtils.writeBinary(featureDump, kpts);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			List<InterestPointData> ipts = new ArrayList<InterestPointData>();
			for (InterestPointKeypoint<InterestPointData> interestPointKeypoint : kpts) {
				ipts.add(interestPointKeypoint.location);
			}
			this.features.put(imageNameFormatted, ipts);
		}
		
		
		
		return this.features.get(imageNameFormatted);
	}
	
	static interface ExperimentFeatureExtraction{
		public class AffineHarris implements ExperimentFeatureExtraction {
			@Override
			public String experimentName() {
				return "affineharris";
			}

			@Override
			public IPDSIFTEngine engine() {
				
				return null;
			}

		}

		public String experimentName();
		public IPDSIFTEngine engine();
		
		static class Harris implements ExperimentFeatureExtraction{

			@Override
			public String experimentName() {
				return "harris";
			}

			@Override
			public IPDSIFTEngine engine() {
				HarrisIPD hIPD = new HarrisIPD(1.4f);
				hIPD.setImageBlurred(true);
				// AffineAdaption affineIPD = new AffineAdaption(harrisIPD,new
				// IPDSelectionMode.Threshold(10000f));
				IPDSIFTEngine engine = new IPDSIFTEngine(hIPD);
				engine.setSelectionMode(new IPDSelectionMode.Threshold(10000f));
				engine.setAcrossScales(true);
				engine.setFinderMode(new FinderMode.Characteristic<InterestPointData>());
				return engine;
			}
			
		}
	}
	
	public static void main(String args[]) throws IOException, ExperimentException{
		String expBase = "/org/openimaj/image/feature/validator/graf";
		String imgName = "img%d.ppm";
		OxfordRepeatabilityExperiment exp = new OxfordRepeatabilityExperiment(
				expBase, // The root of all the images and transforms
				"H%dto%dp", // the name format of the transform 
				imgName, // the name format of the image
				6, // the number of experiments to expect
				new ExperimentFeatureExtraction.Harris()
		);
		
		for(int i = 1; i < 6;i++)
		{
			IPDRepeatability<InterestPointData> experiment = exp.experimentWith(i);
			System.out.println(String.format(imgName + ": %f", i+1, experiment.repeatability(0.6f)));
		}
//		IPDRepeatability<InterestPointData> experiment1v2 = exp.experimentWith(1);
//		
//		System.out.println("Experiment 1 v 2");
//		for(float error = 0.1f; error <= .9f; error +=0.1f){
//			float overlap = 1.f - error;
//			System.out.format("Minimum overlap %f (err=%f): %f\n",overlap,error,experiment1v2.repeatability(overlap));
//		}
	}
}
