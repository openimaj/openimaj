package org.openimaj.demos.sandbox.tld;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.openimaj.image.MBFImage;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.video.Video;

public class TLDOptions {
	private File output;
	public static class MatlabStruct extends HashMap<String,Object>{
		/**
		 * @param <T>
		 * @param s
		 * @return a value to a type
		 */
		@SuppressWarnings("unchecked")
		public <T> T v(String s){
			return (T) this.get(s);
		}
	}
//	private Video<MBFImage> source;
	public MatlabStruct model;
	public MatlabStruct p_par_init;
	public MatlabStruct p_par_update;
	public MatlabStruct n_par;
	public MatlabStruct tracker;
	public MatlabStruct control;
	public double[][] grid;
	public double[][] scales;
	public int nGrid;
	public TLDFernFeatures features;
	public TLDFernDetector fern;

	/**
	 * Set using the defaults from the TLD matlab implementation
	 */
	public TLDOptions() {
		this.output          = new File("_output"); prepareOutput();

		int min_win = 24; // minimal size of the object"s bounding box in the scanning grid, it may significantly influence speed of TLD, set it to minimal size of the object
		int[] patchsize = new int[]{15,15}; // size of normalized patch in the object detector, larger sizes increase discriminability, must be square
		boolean fliplr = false; // if set to one, the model automatically learns mirrored versions of the object
		boolean maxbbox = true; // fraction of evaluated bounding boxes in every frame, maxbox = 0 means detector is truned off, if you don"t care about speed set it to 1
		boolean update_detector = true; // online learning on/off, of 0 detector is trained only in the first frame and then remains fixed
		// FIXME: Not sure what this does, probably for rendering, but check here for errors
//		this.plot = struct("pex",1,"nex",1,"dt",1,"confidence",1,"target",1,"replace",0,"drawoutput",3,"draw",0,"pts",1,"help", 0,"patch_rescale",1,"save",0); 

		// Do-not-change -----------------------------------------------------------

		this.model           = struct("min_win",min_win,"patchsize",patchsize,"fliplr",fliplr,"ncc_thesame",0.95,"valid",0.5,"num_trees",10,"num_features",13,"thr_fern",0.5,"thr_nn",0.65,"thr_nn_valid",0.7);
		this.p_par_init      = struct("num_closest",10,"num_warps",20,"noise",5,"angle",20,"shift",0.02,"scale",0.02); // synthesis of positive examples during initialization
		this.p_par_update    = struct("num_closest",10,"num_warps",10,"noise",5,"angle",10,"shift",0.02,"scale",0.02); // synthesis of positive examples during update
		this.n_par           = struct("overlap",0.2,"num_patches",100); // negative examples initialization/update
		this.tracker         = struct("occlusion",10,"nfeatures",10,"windowsize",3);
		this.control         = struct("maxbbox",maxbbox,"update_detector",update_detector,"drop_img",1,"repeat",1);
	}

	private MatlabStruct struct(Object ...objects ) {
		MatlabStruct ret = new MatlabStruct();
		for (int i = 0; i < objects.length; i+=2) {
			ret.put((String) objects[i], objects[i+1]);
		}
		return ret ;
	}

	private void prepareOutput() {
		if(this.output.exists()){
			this.output.delete();
		}
		this.output.mkdirs();
	}
}
