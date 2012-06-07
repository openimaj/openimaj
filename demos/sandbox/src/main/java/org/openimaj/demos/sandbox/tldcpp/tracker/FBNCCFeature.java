package org.openimaj.demos.sandbox.tldcpp.tracker;

import org.openimaj.video.tracking.klt.Feature;

public class FBNCCFeature extends Feature {
	public float ncc;
	public float fbDistance;
	
	@Override
	public FBNCCFeature  clone() {
		FBNCCFeature f = new FBNCCFeature ();

		f.x = x;
		f.y = y;
		f.val = val;
		f.ncc = ncc;
		f.fbDistance = fbDistance;
//		f.aff_img = aff_img; 
//		f.aff_img_gradx = aff_img_gradx;
//		f.aff_img_grady = aff_img_grady;
//		f.aff_x = aff_x;
//		f.aff_y = aff_y;
//		f.aff_Axx = aff_Axx;
//		f.aff_Ayx = aff_Ayx;
//		f.aff_Axy = aff_Axy;
//		f.aff_Ayy = aff_Ayy;
		
		return f;
	}
}
