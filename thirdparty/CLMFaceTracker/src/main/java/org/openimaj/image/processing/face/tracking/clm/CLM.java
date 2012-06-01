package org.openimaj.image.processing.face.tracking.clm;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

import org.openimaj.image.FImage;
import org.openimaj.math.matrix.MatrixUtils;

import Jama.Matrix;

public class CLM {
	class SimTData {
		//data for similarity xform
		double a;
		double b;
		double tx;
		double ty;
	}

	PDM         _pdm;   /**< 3D Shape model           */
	Matrix      _plocal;/**< local parameters         */
	Matrix      _pglobl;/**< global parameters        */
	Matrix      _refs;  /**< Reference shape          */
	Matrix[]    _cent;  /**< Centers/view (Euler)     */
	public Matrix[]    _visi;  /**< Visibility for each view */
	MPatch[][]  _patch; /**< Patches/point/view       */

	private Matrix cshape_,bshape_,oshape_,ms_,u_,g_,J_,H_; 
	private FImage[] prob_;
	private FImage[] pmem_;
	private FImage[] wmem_;

	//=============================================================================
	void CalcSimT(Matrix src, Matrix dst, SimTData data)
	{
		assert((src.getRowDimension() == dst.getRowDimension()) && (src.getColumnDimension() == dst.getColumnDimension()) && (src.getColumnDimension() == 1));

		int n = src.getRowDimension() / 2;

		Matrix H = new Matrix(4,4);
		Matrix g = new Matrix(4,1);

		final double[][] Hv = H.getArray();
		final double[][] gv = g.getArray();

		for (int i=0; i<n; i++) {
			double ptr1x = src.get(i, 0);
			double ptr1y = src.get(i+n, 0);
			double ptr2x = dst.get(i, 0);
			double ptr2y = dst.get(i+n, 0);

			Hv[0][0] += (ptr1x*ptr1x) + (ptr1y*ptr1y);
			Hv[0][2] += ptr1x; 
			Hv[0][3] += ptr1y;

			gv[0][0] += ptr1x*ptr2x + ptr1y*ptr2y;
			gv[1][0] += ptr1x*ptr2y - ptr1y*ptr2x;
			gv[2][0] += ptr2x; 
			gv[3][0] += ptr2y;
		}

		Hv[1][1] = Hv[0][0];
		Hv[3][0] = Hv[0][3];
		Hv[1][2] = Hv[2][1] = -Hv[3][0];
		Hv[1][3] = Hv[3][1] = Hv[2][0] = Hv[0][2];
		Hv[2][2] = Hv[3][3] = n;

		Matrix p = H.solve(g);

		data.a = p.get(0, 0); 
		data.b = p.get(1, 0); 
		data.tx = p.get(2, 0); 
		data.ty = p.get(3, 0);
	}

	//=============================================================================
	void invSimT(SimTData in, SimTData out)
	{
		Matrix M = new Matrix(new double[][]{{in.a, -in.b}, {in.b, in.a}});
		Matrix N = M.inverse(); 
		out.a = N.get(0, 0); 
		out.b = N.get(1, 0);

		out.tx = -1.0 * (N.get(0,0) * in.tx + N.get(0,1) * in.ty);
		out.ty = -1.0 * (N.get(1,0) * in.tx + N.get(1,1) * in.ty);
	}

	//=============================================================================
	void SimT(Matrix s, SimTData data)
	{
		assert(s.getColumnDimension() == 1);

		int n = s.getRowDimension() / 2; 

		for(int i = 0; i < n; i++) {
			double x = s.get(i, 0); 
			double y = s.get(i+n, 0);

			s.set(i, 0, data.a*x - data.b*y + data.tx); 
			s.set(i+n, 0, data.b*x + data.a*y + data.ty);    
		}
	}

	public CLM(PDM s, Matrix r, Matrix[] c, Matrix[] v, MPatch[][] p)
	{
		int n = p.length; 

		assert(((int)c.length == n) && ((int)v.length == n));
		assert((r.getRowDimension() == 2*s.nPoints()) && (r.getColumnDimension() == 1));

		for(int i = 0; i < n; i++) {
			assert((int)p[i].length == s.nPoints());
			assert((c[i].getRowDimension() == 3) && (c[i].getColumnDimension() == 1));
			assert((v[i].getRowDimension() == s.nPoints()) &&  (v[i].getColumnDimension() == 1));
		}

		_pdm = s; 
		_refs = r.copy();
		_cent = new Matrix[n];
		_visi = new Matrix[n];
		_patch = new MPatch[n][];

		for(int i = 0; i < n; i++) {
			_cent[i] = c[i].copy(); 
			_visi[i] = v[i].copy();
			_patch[i] = new MPatch[p[i].length];

			for (int j = 0; j < p[i].length; j++)
				_patch[i][j] = p[i][j];
		}

		_plocal = new Matrix(_pdm.nModes(), 1);
		_pglobl = new Matrix(6,1);
		cshape_ = new Matrix(2*_pdm.nPoints(),1);
		bshape_ = new Matrix(2*_pdm.nPoints(),1);
		oshape_ = new Matrix(2*_pdm.nPoints(),1);
		ms_ = new Matrix(2*_pdm.nPoints(),1);
		u_ = new Matrix(6+_pdm.nModes(),1);
		g_ = new Matrix(6+_pdm.nModes(),1);
		J_ = new Matrix(2*_pdm.nPoints(),6+_pdm.nModes());
		H_ = new Matrix(6+_pdm.nModes(),6+_pdm.nModes());

		prob_ = new FImage[_pdm.nPoints()]; 
		pmem_ = new FImage[_pdm.nPoints()]; 
		wmem_ = new FImage[_pdm.nPoints()];
	}

	public CLM() {
		// TODO Auto-generated constructor stub
	}

	//===========================================================================
	static CLM Load(final String fname) throws FileNotFoundException
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

	//=============================================================================
	void Write(BufferedWriter s) throws IOException
	{
		s.write(IO.Types.CLM.ordinal() + " " + _patch.length + " "); 
		_pdm.Write(s); 
		IO.WriteMat(s, _refs);

		for(int i = 0; i < _cent.length; i++)
			IO.WriteMat(s, _cent[i]);

		for(int i = 0; i < _visi.length; i++)
			IO.WriteMat(s, _visi[i]);

		for(int i = 0; i < _patch.length; i++) {
			for(int j = 0; j < _pdm.nPoints(); j++)
				_patch[i][j].Write(s);
		}
	}

	//=============================================================================
	static CLM Read(Scanner s, boolean readType)
	{
		if (readType) {
			int type = s.nextInt(); 
			assert(type == IO.Types.CLM.ordinal());
		}

		CLM clm = new CLM();
		
		int n = s.nextInt(); 
		clm._pdm = PDM.Read(s, true);
		clm._cent = new Matrix[n];
		clm._visi = new Matrix[n];
		clm._patch = new MPatch[n][]; 
		clm._refs = IO.ReadMat(s);

		for(int i = 0; i < clm._cent.length; i++)
			clm._cent[i] = IO.ReadMat(s);

		for(int i = 0; i < clm._visi.length; i++)
			clm._visi[i] = IO.ReadMat(s);

		for(int i = 0; i < clm._patch.length; i++) {
			clm._patch[i] = new MPatch[clm._pdm.nPoints()];

			for(int j = 0; j < clm._pdm.nPoints(); j++) {
				clm._patch[i][j] = MPatch.Read(s, true);
			}
		}

		clm._plocal = new Matrix(clm._pdm.nModes(), 1);
		clm._pglobl = new Matrix(6, 1);
		clm.cshape_ = new Matrix(2*clm._pdm.nPoints(),1);
		clm.bshape_ = new Matrix(2*clm._pdm.nPoints(),1);
		clm.oshape_ = new Matrix(2*clm._pdm.nPoints(),1);
		clm.ms_ = new Matrix(2*clm._pdm.nPoints(),1);
		clm.u_ = new Matrix(6+clm._pdm.nModes(),1);
		clm.g_ = new Matrix(6+clm._pdm.nModes(),1);
		clm.J_ = new Matrix(2*clm._pdm.nPoints(), 6+clm._pdm.nModes());
		clm.H_ = new Matrix(6+clm._pdm.nModes(), 6+clm._pdm.nModes());
		clm.prob_ = new FImage[clm._pdm.nPoints()]; 
		clm.pmem_ = new FImage[clm._pdm.nPoints()];
		clm.wmem_ = new FImage[clm._pdm.nPoints()];
		
		return clm;
	}

	final int nViews() {
		return _patch.length;
	}

	//=============================================================================
	public int GetViewIdx()
	{
		int idx=0;

		if(this.nViews() == 1) { 
			return 0;
		} else {
			int i; double v1,v2,v3,d,dbest = -1.0;
			for(i = 0; i < this.nViews(); i++) {
				v1 = _pglobl.get(1,0) - _cent[i].get(0,0);
				v2 = _pglobl.get(2,0) - _cent[i].get(1,0);
				v3 = _pglobl.get(3,0) - _cent[i].get(2,0);

				d = v1*v1 + v2*v2 + v3*v3;

				if(dbest < 0 || d < dbest) {
					dbest = d; 
					idx = i;
				}
			}return idx;
		}
	}

	//=============================================================================
	void Fit(FImage im, int[] wSize, int nIter, double clamp, double fTol)
	{
		int i,idx,n = _pdm.nPoints(); 
		
		SimTData d1 = new SimTData();
		SimTData d2 = new SimTData();
		
		for (int witer = 0; witer < wSize.length; witer++) {
			_pdm.CalcShape2D(cshape_,_plocal,_pglobl);
			
			CalcSimT(_refs,cshape_,d1);
			invSimT(d1, d2);
			
			idx = GetViewIdx();

			for(i = 0; i < n; i++) {
				if(_visi[idx].getRowDimension() == n){
					if(_visi[idx].get(i,0) == 0)
						continue;
				}
				
				int w = wSize[witer]+_patch[idx][i]._w - 1; 
				int h = wSize[witer]+_patch[idx][i]._h - 1;
				
				Matrix sim = new Matrix(new double[][]{{d1.a, -d1.b, cshape_.get(i,0)},{d1.b, d1.a, cshape_.get(i+n,0)}});
				
				if (wmem_[i] == null || (w>wmem_[i].width) || (h>wmem_[i].height))
					wmem_[i] = new FImage(w, h);
				
				//gah, we need to get a subimage backed by the original; luckily its from the origin
				FImage wimg = subImage(wmem_[i], w, h);
				
				FImage wimg_o = wimg; //why? is this supposed to clone?
				Matrix sim_o = sim; 
				FImage im_o = im;
				
				cvGetQuadrangleSubPix(im_o, wimg_o, sim_o);
				
				if (pmem_[i] == null || wSize[witer] > pmem_[i].height)
					pmem_[i] = new FImage(wSize[witer], wSize[witer]);
				
				prob_[i] = subImage(pmem_[i], wSize[witer], wSize[witer]);
				
				_patch[idx][i].Response(wimg, prob_[i]);
			}
			
			SimT(cshape_, d2);
			_pdm.ApplySimT(d2, _pglobl);
			bshape_.setMatrix(0, cshape_.getRowDimension()-1, 0, cshape_.getColumnDimension()-1, cshape_);
			
			this.Optimize(idx, wSize[witer], nIter, fTol, clamp, true);
			this.Optimize(idx, wSize[witer], nIter, fTol, clamp, false);
			
			_pdm.ApplySimT(d1, _pglobl);
		}
	}

	/**
	 * Construct a view on an FImage from the origin to
	 * a new height/width (which must be the same or smaller
	 * than in the input image)  
	 * 
	 * @param fImage
	 * @param i
	 * @param j
	 * @return
	 */
	private FImage subImage(FImage fImage, int w, int h) {
		FImage img = new FImage(fImage.pixels);
		img.width = w;
		img.height = h;
		return img;
	}

	private void cvGetQuadrangleSubPix(FImage src, FImage dest, Matrix tx) {
		//FIXME: move this somewhere appropriate
		final float[][] dpix = dest.pixels;
		
		final double A11 = tx.get(0, 0);
		final double A12 = tx.get(0, 1);
		final double A21 = tx.get(1, 0);
		final double A22 = tx.get(1, 1);
		final double b1 = tx.get(0, 2);
		final double b2 = tx.get(1, 2);
		
		for (int y=0; y<dest.width; y++) {
			for (int x=0; x<dest.height; x++) {
				double xp = x - (dest.width - 1) * 0.5;
				double yp = y - (dest.height - 1) * 0.5;
				
				float xpp = (float) (A11*xp + A12*yp + b1);
				float ypp = (float) (A21*xp + A22*yp + b2);
				
				dpix[y][x] = src.getPixelInterpNative(xpp, ypp, 0);
			}
		}
	}

	//	=============================================================================
	void Optimize(int idx, int wSize, int nIter, double fTol, double clamp, boolean rigid)
	{
		int m=_pdm.nModes();
		int n=_pdm.nPoints();

		double sigma=(wSize*wSize)/36.0; 

		Matrix u, g, J, H;
		if(rigid) {
			//FIXME - in the original this probably creates "views" rather than copies
			u = u_.getMatrix(0, 6-1, 0, 1-1);
			g = g_.getMatrix(0, 6-1, 0, 1-1); 
			J = J_.getMatrix(0, 2*n-1, 0, 6-1);
			H = H_.getMatrix(0, 6-1, 0, 6-1);
		} else {
			u = u_; 
			g = g_;
			J = J_; 
			H = H_;
		}

		for(int iter = 0; iter < nIter; iter++){
			_pdm.CalcShape2D(cshape_,_plocal,_pglobl);

			if(iter > 0) {
				if(l2norm(cshape_,oshape_) < fTol)
					break;
			}

			oshape_.setMatrix(0, oshape_.getRowDimension()-1, 0, oshape_.getColumnDimension()-1, cshape_);

			if(rigid) { 
				_pdm.CalcRigidJacob(_plocal, _pglobl, J);
			} else {
				_pdm.CalcJacob(_plocal, _pglobl, J);
			}

			for(int i = 0; i < n; i++) {
				if(_visi[idx].getRowDimension() == n) {
					if(_visi[idx].get(i,0) == 0) {
						MatrixUtils.setRow(J, i, 0);

						MatrixUtils.setRow(J, i+n, 0);

						ms_.set(i,  0, 0);
						ms_.set(i+n,0, 0); 

						continue;
					}
				}

				double dx = cshape_.get(i  , 0) - bshape_.get(i  , 0) + (wSize-1)/2;
				double dy = cshape_.get(i+n, 0) - bshape_.get(i+n, 0) + (wSize-1)/2;

				double mx=0.0,my=0.0,sum=0.0;      
				for(int ii = 0; ii < wSize; ii++) {
					double vx = (dy-ii)*(dy-ii);

					for(int jj = 0; jj < wSize; jj++) {
						double vy = (dx-jj)*(dx-jj);

						double v = prob_[i].pixels[ii][jj];
						v *= Math.exp(-0.5*(vx+vy)/sigma);
						sum += v;
						mx += v*jj;
						my += v*ii; 
					}
				}

				ms_.set(i, 0, mx/sum - dx);
				ms_.set(i+n, 0, my/sum - dy);
			}

			g = J.transpose().times(ms_);
			H = J.transpose().times(J);

			if(!rigid) {
				for(int i = 0; i < m; i++) {
					double var = 0.5 * sigma / _pdm._E.get(0, i);

					H.getArray()[6+i][6+i] += var; 
					g.getArray()[6+i][0] -= var*_plocal.get(i,0);
				}
			}

			MatrixUtils.fill(u_, 0);
			u = H.solve(g);
			
			if(rigid)
				u_.setMatrix(0, 6-1, 0, 1-1, u);
			else
				u_.setMatrix(0, u.getRowDimension()-1, 0, u.getColumnDimension()-1, u);
					

			_pdm.CalcReferenceUpdate(u_, _plocal, _pglobl);

			if(!rigid)
				_pdm.Clamp(_plocal, clamp);
		}
		
		//FIXME do we need to deal with rigid setting underlying _u correctly?
		//this attempts do do so, but might not be the best way!
		if(rigid) {
			u_.setMatrix(0, 6-1, 0, 1-1, u);
			g_.setMatrix(0, 6-1, 0, 1-1, g); 
			J_.setMatrix(0, 2*n-1, 0, 6-1, J);
			H_.setMatrix(0, 6-1, 0, 6-1, H);
		} else {
			u_.setMatrix(0, u.getRowDimension()-1, 0, u.getColumnDimension()-1, u);
			g_.setMatrix(0, g.getRowDimension()-1, 0, g.getColumnDimension()-1, g); 
			J_.setMatrix(0, J.getRowDimension()-1, 0, J.getColumnDimension()-1, J);
			H_.setMatrix(0, H.getRowDimension()-1, 0, H.getColumnDimension()-1, H);
		}
	}

	private double l2norm(Matrix m1, Matrix m2) {
		final double[][] m1v = m1.getArray();
		final double[][] m2v = m2.getArray();
		final int rows = m1.getRowDimension();
		final int cols = m1.getColumnDimension();

		double sum = 0;
		for (int r=0; r<rows; r++) {
			for (int c=0; c<cols; c++) {
				double diff = m1v[r][c] - m2v[r][c];

				sum += diff*diff;
			}
		}

		return Math.sqrt(sum);
	}
	//=============================================================================

}
