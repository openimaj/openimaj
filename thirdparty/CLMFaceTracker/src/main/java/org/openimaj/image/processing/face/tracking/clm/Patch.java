package org.openimaj.image.processing.face.tracking.clm;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.analysis.algorithm.TemplateMatcher;
import org.openimaj.image.analysis.algorithm.TemplateMatcher.Mode;
import org.openimaj.image.processing.algorithm.MeanCenter;

public class Patch {
	public int     _t; /**< Type of patch (0=raw,1=grad,2=lbp) */
	public double  _a; /**< scaling                            */
	public double  _b; /**< bias                               */
	public FImage  _W; /**< Gain                               */

	protected FImage im_ = new FImage(0, 0);
	protected TemplateMatcher matcher;
	
	//===========================================================================
	FImage Grad(FImage im)
	{
		FImage grad = new FImage(im.width, im.height);
		
		for(int y = 1; y < im.height-1; y++) { 
			for(int x = 1; x < im.width-1; x++) {
				float vx = im.pixels[y][x+1] - im.pixels[y][x-1]; 
				float vy = im.pixels[y+1][x] - im.pixels[y-1][x];
				grad.pixels[y][x] = vx*vx + vy*vy;
			}
		}
		return grad;
	}
	
	//===========================================================================
	final float SGN(float x) {return (x<0) ? 0:1;}
	
	FImage LBP(FImage im)
	{
		FImage lp = new FImage(im.width, im.height);
		
//		float [] v = new float[9];
//		for(int y = 1; y < im.height-1; y++) {
//			for(int x = 1; x < im.width-1; x++) {
//				v[4] = im.pixels[y][x-1]; 
//				v[0] = im.pixels[y][x];
//				v[5] = im.pixels[y][x+1];
//				v[1] = im.pixels[y-1][x-1];
//				v[2] = im.pixels[y-1][x];
//				v[3] = im.pixels[y-1][x+1];
//				v[6] = im.pixels[y+1][x-1];
//				v[7] = im.pixels[y+1][x];
//				v[8] = im.pixels[y+1][x+1];
//				
//				lp.pixels[y][x] = 
//					SGN(v[0]-v[1])*2   + SGN(v[0]-v[2])*4   + 
//					SGN(v[0]-v[3])*8   + SGN(v[0]-v[4])*16  + 
//					SGN(v[0]-v[5])*32  + SGN(v[0]-v[6])*64  + 
//					SGN(v[0]-v[7])*128 + SGN(v[0]-v[8])*256 ;
//			}
//		}
		
		return lp;
	}
	
	//=============================================================================
//	Patch& Patch::operator= (Patch final& rhs)
//	{   
//		this->_t = rhs._t; this->_a = rhs._a; this->_b = rhs._b; 
//		this->_W = rhs._W.clone(); this->im_ = rhs.im_.clone(); 
//		this->res_ = rhs.res_.clone(); return *this;
//	}
	
	//===========================================================================
	void Load(final String fname) throws FileNotFoundException
	{
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(fname));
			Scanner sc = new Scanner(br);
			Read(sc, true);
		} finally {
			try { br.close(); } catch (IOException e) {}
		}
	}
	
	//===========================================================================
	void Save(final String fname) throws IOException{
		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new FileWriter(fname));
			
			Write(bw);
		} finally {
			try {
				if (bw != null) bw.close();
			} catch (IOException e) {}
		}
	}
	
	//===========================================================================
	void Write(BufferedWriter s) throws IOException
	{
		s.write(IO.Types.PATCH.ordinal() + " " + _t + " " + _a + " " + _b + " ");
		IO.WriteImg(s, _W);
	}
	
	//===========================================================================
	static Patch Read(Scanner s, boolean readType)
	{
		if(readType){
			int type = s.nextInt(); 
			assert(type == IO.Types.PATCH.ordinal());
		}
		
		Patch p = new Patch();
		
		p._t = s.nextInt();
		p._a = s.nextDouble();
		p._b = s.nextDouble();
		p._W = IO.ReadImg(s);
		p.matcher = new TemplateMatcher(p._W.clone(), Mode.NORM_CORRELATION_COEFFICIENT);
		
		return p;
	}
	
	//===========================================================================
	void Patch(int t, double a, double b, FImage W)
	{
		_t=t; 
		_a=a; 
		_b=b;
		matcher = new TemplateMatcher(_W.clone(), Mode.NORM_CORRELATION_COEFFICIENT);
	}
	
	//===========================================================================
	void Response(FImage im, FImage resp)
	{
		assert((im.height>=_W.height) && (im.width>=_W.width));
		
		int h = im.height - _W.height + 1;
		int w = im.width - _W.width + 1; 
		
		if(resp.height != h || resp.width != w) resp.internalAssign(new FImage(w, h));
		
		FImage I;
		if(_t == 0) {
			I = im;
		} else{
			if(im_.height == im.height && im_.width == im.width) {
				I = im_;
			} else if(im_.height >= im.height && im_.width >= im.width) {
				I = im_.extractROI(0, 0, im.width, im.height);
			} else {
				im_ = new FImage(im.width, im.height);
				I = im_;
			}
			
			if (_t == 1) {
				I = Grad(im);
			} else if (_t == 2) {
				I = LBP(im);
			} else {
				throw new RuntimeException("ERROR: Unsupported patch type!\n");
			}
		}
		
		matcher.analyseImage(I);
		FImage res = matcher.getResponseMap();
		
		for (int y=0; y<resp.height; y++)
			for (int x=0; x<resp.width; x++)
				resp.pixels[y][x] = (float) (1.0/(1.0 + Math.exp( res.pixels[y][x] * _a + _b )));
	}
}
