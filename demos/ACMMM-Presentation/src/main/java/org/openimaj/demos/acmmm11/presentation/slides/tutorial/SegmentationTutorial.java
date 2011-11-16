package org.openimaj.demos.acmmm11.presentation.slides.tutorial;

import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.ColourSpace;
import org.openimaj.ml.clustering.kmeans.fast.FastFloatKMeansCluster;
import org.openimaj.video.Video;

public class SegmentationTutorial extends TutorialPanel {

	private FastFloatKMeansCluster cluster;

	public SegmentationTutorial( Video<MBFImage> capture,int width, int height) {
		super("Segmentation", capture, width, height);
	}

	@Override
	public void doTutorial(MBFImage toDraw) {
		MBFImage space = ColourSpace.convert(toDraw, ColourSpace.CIE_Lab);
		if(cluster == null) cluster = clusterPixels(space);
		if(cluster == null) return;
		float[][] centroids = cluster.getClusters();
		for(int y = 0; y < space.getHeight(); y++){
			for(int x = 0; x < space.getWidth(); x++){
				float[] pixel = space.getPixelNative(x, y);
				int centroid = cluster.push_one(pixel);
				space.setPixelNative(x, y, centroids[centroid]);
			}
		}
		toDraw.internalAssign(ColourSpace.convert(space, ColourSpace.RGB));
		
	}

	private FastFloatKMeansCluster clusterPixels(MBFImage toDraw) {
		float sum = 0;
		float[][] testP = toDraw.getBand(0).pixels;
		for(int i = 0; i < testP.length; i++) for(int j = 0; j < testP[i].length; j++) sum+=testP[i][j];
		if(sum == 0) return null;
		FastFloatKMeansCluster k = new FastFloatKMeansCluster(3,2,true);
		float[][] imageData = toDraw.getPixelVectorNative(new float[toDraw.getWidth() * toDraw.getHeight() * 3][3]);
		k.train(imageData);
		return k;
	}

}
