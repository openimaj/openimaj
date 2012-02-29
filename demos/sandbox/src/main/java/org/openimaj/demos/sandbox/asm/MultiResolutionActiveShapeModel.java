package org.openimaj.demos.sandbox.asm;

import java.util.ArrayList;
import java.util.List;

import org.openimaj.demos.sandbox.asm.ActiveShapeModel.IterationResult;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.analysis.pyramid.SimplePyramid;
import org.openimaj.math.geometry.line.Line2d;
import org.openimaj.math.geometry.shape.PointDistributionModel;
import org.openimaj.math.geometry.shape.PointDistributionModel.Constraint;
import org.openimaj.math.geometry.shape.PointList;
import org.openimaj.math.geometry.shape.PointListConnections;
import org.openimaj.math.geometry.transforms.TransformUtilities;
import org.openimaj.util.pair.IndependentPair;

import Jama.Matrix;

public class MultiResolutionActiveShapeModel {
	int l; //num resolutions
	ActiveShapeModel [] asms;
	static float sigma = 0.5f;

	public MultiResolutionActiveShapeModel(int l, ActiveShapeModel[] asms) {
		this.l = l;
		this.asms = asms;
	}

	public static MultiResolutionActiveShapeModel trainModel(int l, int k, int m, float scale, int numComponents, PointListConnections connections, List<IndependentPair<PointList, FImage>> data, Constraint constraint) {
		int nPoints = data.get(0).firstObject().size();

		PixelProfileModel[][] ppms = new PixelProfileModel[l][nPoints];
		for (int i=0; i<data.size(); i++) {
			SimplePyramid<FImage> pyr = SimplePyramid.createGaussianPyramid(data.get(i).secondObject(), sigma, l);
			PointList pl = data.get(i).firstObject();
			
			for (int level=0; level<l; level++) {
				Matrix scaling = TransformUtilities.scaleMatrix(1.0/Math.pow(2, level), 1.0/Math.pow(2, level));
				PointList tfpl = pl.transform(scaling);
				FImage image = pyr.pyramid[level];
				
				for (int j=0; j<nPoints; j++) {
					if (ppms[level][j] == null) {
						ppms[level][j] = new PixelProfileModel(2*k + 1);
					}

					float lineScale = (float) (Math.pow(2, level) * scale * tfpl.computeIntrinsicScale());

					Line2d line = connections.calculateNormalLine(j, tfpl, lineScale);
					if (line != null) ppms[level][j].addSample(image, line);
				}
			}
		}

		List<PointList> pls = new ArrayList<PointList>();
		for (IndependentPair<PointList, FImage> i : data)
			pls.add(i.firstObject());

		PointDistributionModel pdm = new PointDistributionModel(constraint, pls);
		pdm.setNumComponents(numComponents);
		
		ActiveShapeModel [] asms = new ActiveShapeModel[l]; 
		for (int level=0; level<l; level++) {
			asms[level] = new ActiveShapeModel(k, m, (float) (Math.pow(2, level) * scale), connections, pdm, ppms[level]);
		}
		
		return new MultiResolutionActiveShapeModel(l, asms);
	}
	
	public IterationResult fit(FImage initialImage, Matrix initialPose, PointList initialShape) {
		SimplePyramid<FImage> pyr = SimplePyramid.createGaussianPyramid(initialImage, sigma, l);
		
		Matrix scaling = TransformUtilities.scaleMatrix(1.0/Math.pow(2, l-1), 1.0/Math.pow(2, l-1));
		
		PointList shape = initialShape.transform(scaling);
		Matrix pose = scaling.times(initialPose);
		
//		DisplayUtilities.displayName(asms[l-1].drawShapeAndNormals(pyr.pyramid[l-1], shape), "shape", true);
		
		double fit = 0;
		for (int level=l-1; level>=0; level--) {
			FImage image = pyr.pyramid[level];
			
			ActiveShapeModel asm = asms[level];
			
			IterationResult newData = asm.fit(image, pose, shape);
			
//			DisplayUtilities.displayName(asm.drawShapeAndNormals(image, newData.shape), "shape", true);
			
			if (level == 0)
				scaling = Matrix.identity(3, 3);
			else
				scaling = TransformUtilities.scaleMatrix(2, 2);
			
			shape = newData.shape.transform(scaling);
			pose = newData.pose.times(scaling);
			fit  = newData.fit;
		}
		
		return new IterationResult(pose, shape, fit);
	}

	public PointDistributionModel getPDM() {
		return asms[0].pdm;
	}
}
