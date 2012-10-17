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
package org.openimaj.demos.sandbox.tld;

import java.io.File;
import java.util.HashMap;

public class TLDOptions {
	private File output;
	public static class MatlabStruct extends HashMap<String,Object>{
		private static final long serialVersionUID = 1L;

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
