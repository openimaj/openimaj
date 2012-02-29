package org.openimaj.demos.sandbox.asm;

import java.util.ArrayList;
import java.util.List;

import org.openimaj.demos.sandbox.asm.ActiveShapeModel.IterationResult;
import org.openimaj.image.FImage;
import org.openimaj.image.analysis.pyramid.SimplePyramid;
import org.openimaj.math.geometry.shape.PointDistributionModel;
import org.openimaj.math.geometry.shape.PointList;
import org.openimaj.math.geometry.shape.PointListConnections;
import org.openimaj.math.geometry.transforms.TransformUtilities;
import org.openimaj.util.pair.IndependentPair;

import Jama.Matrix;

public class MultiResolutionActiveShapeModel {
	int l; //num resolutions
	int k;
	int m;
	float scale;
	PointListConnections connections;
	PointDistributionModel pdm;
	PixelProfileModel[][] ppms;

	public MultiResolutionActiveShapeModel(int l, int k, int m, float scale, PointListConnections connections, PointDistributionModel pdm, PixelProfileModel[][] ppms) {
		this.l = l;
		this.k = k;
		this. m = m;
		this.scale = scale;
		this.connections = connections;
		this.pdm = pdm;
		this.ppms = ppms;
	}

	public static MultiResolutionActiveShapeModel trainModel(int l, int k, int m, float scale, int numComponents, PointListConnections connections, List<IndependentPair<PointList, FImage>> data) {
		int nPoints = data.get(0).firstObject().size();

		PixelProfileModel[][] ppms = new PixelProfileModel[l][nPoints];
		for (int i=0; i<data.size(); i++) {
			SimplePyramid<FImage> pyr = SimplePyramid.create(data.get(i).secondObject(), l);
			PointList pl = data.get(i).firstObject();
			
			for (int level=l-1; level>=0; level--) {
				PointList tfpl = pl.transform(TransformUtilities.scaleMatrix(1.0/(level+1), 1.0/(level+1)));
				FImage image = pyr.pyramid[level];
				
				for (int j=0; j<nPoints; j++) {
					if (ppms[level][j] == null) {
						ppms[level][j] = new PixelProfileModel(2*k + 1);
					}

					float lineScale = scale * tfpl.computeIntrinsicScale();

					ppms[level][j].addSample(image, connections.calculateNormalLine(j, tfpl, lineScale));
				}
			}
		}

		List<PointList> pls = new ArrayList<PointList>();
		for (IndependentPair<PointList, FImage> i : data)
			pls.add(i.firstObject());

		PointDistributionModel pdm = new PointDistributionModel(new PointDistributionModel.EllipsoidConstraint(3.0), pls);
		pdm.setNumComponents(numComponents);

		return new MultiResolutionActiveShapeModel(l, k, m, scale, connections, pdm, ppms);
	}
	
	public IterationResult fit(FImage initialImage, Matrix initialPose, PointList initialShape) {
		SimplePyramid<FImage> pyr = SimplePyramid.create(initialImage, l);
		
		Matrix scaling = TransformUtilities.scaleMatrix(1.0/l, 1.0/l);
		
		PointList shape = initialShape.transform(scaling);
		Matrix pose = initialPose.times(scaling);
		
		double fit = 0;
		for (int level=l-1; level>=0; level--) {
			FImage image = pyr.pyramid[level];
			
			ActiveShapeModel asm = new ActiveShapeModel(k, m, scale, connections, pdm, ppms[level]);
			
			IterationResult newData = asm.fit(image, pose, shape);
			
			scaling = TransformUtilities.scaleMatrix(2, 2);
			shape = newData.shape.transform(scaling);
			pose = newData.pose.times(scaling);
			fit  = newData.fit;
		}
		
		return new IterationResult(pose, shape, fit);
	}

	public PointDistributionModel getPDM() {
		return pdm;
	}
}
