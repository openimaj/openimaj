//package org.openimaj.image.feature.local.engine;
//
//import org.openimaj.feature.local.list.LocalFeatureList;
//import org.openimaj.image.FImage;
//import org.openimaj.image.feature.local.keypoints.Keypoint;
//import org.openimaj.image.processing.convolution.FImageGradients;
//
//public class DenseSIFTEngine implements Engine<Keypoint, FImage> {
//	public class DenseSIFT {
//		int stepX = 5;
//		int stepY = 5;
//		int binSize;
//		float scale;
//		int numOrientations;
//		
//		void vl_dsift_process(FImage image)
//		{
//		  
//		  /* Compute gradients, their norm, and their angle */
//		  FImage [] gradmags = FImage.createArray(numOrientations, image.width, image.height); 
//	      FImageGradients.gradientMagnitudesAndQuantisedOrientations(image, gradmags);
//
//		  _vl_dsift_with_gaussian_window();
//
//		  {
//		    VlDsiftKeypoint* frameIter = self->frames ;
//		    float * descrIter = self->descrs ;
//		    int framex, framey, bint ;
//
//		    int frameSizeX = self->geom.binSizeX * (self->geom.numBinX - 1) + 1 ;
//		    int frameSizeY = self->geom.binSizeY * (self->geom.numBinY - 1) + 1 ;
//		    int descrSize = vl_dsift_get_descriptor_size (self) ;
//
//		    float deltaCenterX = 0.5F * self->geom.binSizeX * (self->geom.numBinX - 1) ;
//		    float deltaCenterY = 0.5F * self->geom.binSizeY * (self->geom.numBinY - 1) ;
//
//		    float normConstant = frameSizeX * frameSizeY ;
//
//		    for (framey  = self->boundMinY ;
//		         framey <= self->boundMaxY - frameSizeY + 1 ;
//		         framey += self->stepY) {
//
//		      for (framex  = self->boundMinX ;
//		           framex <= self->boundMaxX - frameSizeX + 1 ;
//		           framex += self->stepX) {
//
//		        frameIter->x    = framex + deltaCenterX ;
//		        frameIter->y    = framey + deltaCenterY ;
//
//		        /* mass */
//		        {
//		          float mass = 0 ;
//		          for (bint = 0 ; bint < descrSize ; ++ bint)
//		            mass += descrIter[bint] ;
//		          mass /= normConstant ;
//		          frameIter->norm = mass ;
//		        }
//
//		        /* L2 normalize */
//		        _vl_dsift_normalize_histogram (descrIter, descrIter + descrSize) ;
//
//		        /* clamp */
//		        for(bint = 0 ; bint < descrSize ; ++ bint)
//		          if (descrIter[bint] > 0.2F) descrIter[bint] = 0.2F ;
//
//		        /* L2 normalize */
//		        _vl_dsift_normalize_histogram (descrIter, descrIter + descrSize) ;
//
//		        frameIter ++ ;
//		        descrIter += descrSize ;
//		      } /* for framex */
//		    } /* for framey */
//		  }
//	}
//	
//	@Override
//	public LocalFeatureList<Keypoint> findFeatures(FImage image) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//	
//	
// }
