package org.openimaj.image.processing.face.tracking.clm;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

import org.openimaj.image.FImage;

public class MPatch {
	public int _w, _h;           /**< Width and height of patch */
	public Patch[] _p; 			 /**< List of patches           */

	private FImage res_;

	//	MPatch& MPatch::operator= (MPatch final& rhs)
	//	{   
	//	  _w = rhs._p[0]._W.cols; _h = rhs._p[0]._W.rows;
	//	  for(int i = 1; i < (int)rhs._p.size(); i++){
	//	    if((rhs._p[i]._W.cols != _w) || (rhs._p[i]._W.rows != _h)){      
	//	      printf("ERROR(%s,%d): Incompatible patch sizes!\n",
	//		     __FILE__,__LINE__); abort();
	//	    }
	//	  }
	//	  _p = rhs._p; return *this;
	//	}
	//===========================================================================
	MPatch(Patch[] p)
	{
		_w = p[0].matcher.getTemplate().width;
		_h = p[0].matcher.getTemplate().height;

		for (int i = 1; i < p.length; i++) {
			if ((p[i].matcher.getTemplate().width != _w) || (p[i].matcher.getTemplate().height != _h)) {      
				throw new IllegalArgumentException("Patches must all have the same size");
			}
		}
		
		_p = p;
		res_ = new FImage(0,0);
	}
	
	MPatch() {
		// TODO Auto-generated constructor stub
	}

	//===========================================================================
	static MPatch Load(final String fname) throws FileNotFoundException
	{
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(fname));
			Scanner sc = new Scanner(br);
			return Read(sc, true);
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
		s.write(IO.Types.MPATCH.ordinal() + " " + _w + " " + _h + " " + _p.length + " ");
		
		for (int i = 0; i < _p.length; i++)
			_p[i].Write(s);
	}
	
	//===========================================================================
	static MPatch Read(Scanner s, boolean readType)
	{
		if (readType) { 
			int type = s.nextInt(); 
			assert(type == IO.Types.MPATCH.ordinal());
		}
		
		MPatch mpatch = new MPatch();
		
		mpatch._w = s.nextInt();
		mpatch._h = s.nextInt();
		int n = s.nextInt();
		
		mpatch._p = new Patch[n];
		for (int i = 0; i < n; i++)
			mpatch._p[i] = Patch.Read(s, true);
		
		return mpatch;
	}
	
	final void sum2one(FImage M)
	{
		M.divideInplace(M.sum());
	}

	
	//===========================================================================
	void Response(FImage im, FImage resp)
	{
		assert((im.height >= _h) && (im.width >= _w));
		
		int h = im.height - _h + 1, w = im.width - _w + 1;
		
		if(resp.height != h || resp.width != w) resp.internalAssign(new FImage(w,h));
		
		if (res_ == null) res_ = new FImage(w, h);
		if(res_.height != h || res_.width != w) res_.internalAssign(new FImage(w,h));
		
		if(_p.length == 1) {
			_p[0].Response(im,resp); 
			sum2one(resp);
		} else {
			resp.fill(1);
			
			for(int i = 0; i < _p.length; i++) {
				_p[i].Response(im, res_); 
				sum2one(res_); 
				resp.multiplyInplace(res_);
			}
			
			sum2one(resp); 
		}
	}
	//===========================================================================
}
