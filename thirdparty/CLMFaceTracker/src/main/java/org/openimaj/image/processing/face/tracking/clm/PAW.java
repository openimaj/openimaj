package org.openimaj.image.processing.face.tracking.clm;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

import org.openimaj.image.FImage;

import Jama.Matrix;

public class PAW {
	int     _nPix;   /**< Number of pixels                  */
	double  _xmin;   /**< Minimum x-coord for src           */
	double  _ymin;   /**< Minimum y-coord for src           */
	Matrix _src;    /**< Source points                      */
	Matrix _dst;    /**< destination points                 */
	int [][] _tri;  /**< Triangulation                      */
	int [][] _tridx;  /**< Triangle for each valid pixel      */
	FImage _mask;   /**< Valid region mask                  */
	Matrix _coeff;  /**< affine coeffs for all triangles    */
	Matrix _alpha;  /**< matrix of (c,x,y) coeffs for alpha */
	Matrix _beta;   /**< matrix of (c,x,y) coeffs for alpha */
	FImage _mapx;   /**< x-destination of warped points     */
	FImage _mapy;   /**< y-destination of warped points     */

	//=============================================================================
	boolean sameSide(double x0, double y0, double x1, double y1, double x2, double y2, double x3, double y3)
	{
		double x = (x3-x2)*(y0-y2) - (x0-x2)*(y3-y2);
		double y = (x3-x2)*(y1-y2) - (x1-x2)*(y3-y2);

		if(x*y >= 0)
			return true; 
		return false;
	}
	
	//=============================================================================
	int isWithinTri(double x, double y, int [][] tri, Matrix shape)
	{
		int n = tri.length;
		int p = shape.getRowDimension() / 2;
		
		for(int t = 0; t < n; t++) {
			int i = tri[t][0]; 
			int j = tri[t][1]; 
			int k = tri[t][2];
			
			double s11 = shape.get(i  , 0); 
			double s21 = shape.get(j  , 0); 
			double s31 = shape.get(k  , 0);
			double s12 = shape.get(i+p, 0);
			double s22 = shape.get(j+p, 0); 
			double s32 = shape.get(k+p, 0);
			
			if(sameSide(x,y,s11,s12,s21,s22,s31,s32) &&
					sameSide(x,y,s21,s22,s11,s12,s31,s32) &&
					sameSide(x,y,s31,s32,s11,s12,s21,s22))
				return t;
		}
		return -1;
	}
	
	//===========================================================================
//	PAW& operator= (PAW const& rhs)
//	{   
//		this->_nPix = rhs._nPix;
//		this->_xmin = rhs._xmin;
//		this->_ymin = rhs._ymin;
//		this->_src  = rhs._src.clone();
//		this->_tri  = rhs._tri.clone();
//		this->_tridx  = rhs._tridx.clone();
//		this->_mask  = rhs._mask.clone();
//		this->_alpha  = rhs._alpha.clone();
//		this->_beta  = rhs._beta.clone();
//		_mapx.create(_mask.rows,_mask.cols,CV_32F);
//		_mapy.create(_mask.rows,_mask.cols,CV_32F);
//		_coeff.create(this->nTri(),6,CV_64F);
//		_dst = _src; return *this;
//	}
	
	//===========================================================================
    static PAW Load(final String fname) throws FileNotFoundException
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
	void Save(final String fname) throws IOException
	{
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
		s.write(IO.Types.PAW.ordinal() + " " + _nPix + " " + _xmin + " " + _ymin + " ");
		
		IO.WriteMat(s,_src); 
		IO.WriteIntArray(s, _tri); 
		IO.WriteIntArray(s,_tridx);
		IO.WriteImg(s,_mask); 
		IO.WriteMat(s,_alpha); 
		IO.WriteMat(s,_beta);
	}
	//===========================================================================
	static PAW Read(Scanner s, boolean readType)
	{
		if(readType) {
			int type = s.nextInt(); 
			assert(type == IO.Types.PAW.ordinal());
		}
		
		PAW paw = new PAW();
		paw._nPix = s.nextInt();
		paw._xmin = s.nextDouble();
		paw._ymin = s.nextDouble();
		
		paw._src = IO.ReadMat(s); 
		paw._tri = IO.ReadIntArray(s); 
		paw._tridx = IO.ReadIntArray(s);
		paw._mask = IO.ReadImgByte(s); 
		paw._alpha = IO.ReadMat(s); 
		paw._beta = IO.ReadMat(s);
		
		paw._mapx = new FImage(paw._mask.width, paw._mask.height);
		paw._mapy = new FImage(paw._mask.width, paw._mask.height);
		
		paw._coeff = new Matrix(paw.nTri(), 6);
		paw._dst = paw._src;
		
		return paw;
	}
	
    int nPoints() { 
    	return _src.getRowDimension() / 2;
    }
    
    int nTri() { 
    	return _tri.length;
    }
    
    int Width() { 
    	return _mask.width;
    }
    
    int Height() {
    	return _mask.height;
    }

	
	//===========================================================================
	PAW(Matrix src, int[][] tri)
	{
		assert(src.getColumnDimension() == 1);
		assert(tri[0].length == 3);
		
		_src = src.copy(); 
		_tri = tri.clone();
		
		int n = nPoints(); 
		 
		_alpha = new Matrix(nTri(), 3); 
		_beta = new Matrix(nTri(), 3);
		
		for(int i = 0; i < nTri(); i++) {
			int j = _tri[i][0];
			int k = _tri[i][1]; 
			int l = _tri[i][2];
			
			double c1 = _src.get(l+n, 0) - _src.get(j+n, 0);
			double c2 = _src.get(l, 0) - _src.get(j, 0);
			double c4 = _src.get(k+n, 0) - _src.get(j+n, 0); 
			double c3 = _src.get(k, 0) - _src.get(j, 0); 
			double c5 = c3*c1 - c2*c4;
			
			_alpha.set(i, 0, (_src.get(j+n,0)*c2 - _src.get(j,0)*c1)/c5);
			_alpha.set(i, 1, c1/c5); 
			_alpha.set(i, 2, -c2/c5); 
			
			_beta.set(i, 0, (_src.get(j,0)*c4 - _src.get(j+n,0)*c3)/c5);
			_beta.set(i, 1, -c4/c5); 
			_beta.set(i, 2, c3/c5);
		}
		
		double xmax,ymax,xmin,ymin;
		xmax = xmin = _src.get(0, 0);
		ymax = ymin = _src.get(n, 0);
		
		for (int i = 0; i < n; i++) {
			double vx = _src.get(i  , 0); 
			double vy = _src.get(i+n, 0);
			
			xmax = Math.max(xmax, vx); 
			ymax = Math.max(ymax, vy);
			xmin = Math.min(xmin, vx); 
			ymin = Math.min(ymin, vy);
		}
		
		int w = (int)(xmax - xmin + 1.0);
		int h = (int)(ymax - ymin + 1.0);
		_mask = new FImage(w, h); 
		_tridx = new int[h][w];
		
		for (int i = 0,_nPix = 0; i < h; i++) {
			for (int j = 0; j < w; j++) {
				if ((_tridx[i][j] = isWithinTri(j+xmin, i+ymin, tri, _src)) == -1) {
					_mask.pixels[i][j] = 0;
				} else {
					_mask.pixels[i][j] = 0;
					_nPix++;
				}
			}
		}
		
		_mapx = new FImage(_mask.width, _mask.height);
		_mapy = new FImage(_mask.width, _mask.height);
		_coeff = new Matrix(nTri(), 6);
		
		_dst = _src;
		_xmin = xmin; 
		_ymin = ymin;
	}
	
	PAW() {
		// TODO Auto-generated constructor stub
	}

	//=============================================================================
	void Crop(FImage src, FImage dst, Matrix s)
	{
		assert((s.getRowDimension() == _src.getRowDimension()) && (s.getColumnDimension() == 1));
		
		_dst = s; 
		
		CalcCoeff();
		
		WarpRegion(_mapx, _mapy);
		
		cvremap(src, dst, _mapx, _mapy);
	}
	
	private void cvremap(FImage src, FImage dst, FImage mx, FImage my) {
		// FIXME move elsewhere
		
		final float[][] dpix = dst.pixels;
		final float[][] mxp = mx.pixels;
		final float[][] myp = my.pixels;
		
		for (int y=0; y<dst.height; y++) {
			for (int x=0; x<dst.width; x++) {
				dpix[y][x] = src.getPixelInterpNative(mxp[y][x], myp[y][x], 0);
			}
		}
	}

	//=============================================================================
	void CalcCoeff()
	{
		int p=nPoints(); 
		
		for(int l = 0; l < nTri(); l++) {
			int i = _tri[l][0]; 
			int j = _tri[l][1]; 
			int k = _tri[l][2];
			
			double c1 = _dst.get(i  , 0); 
			double c2 = _dst.get(j  , 0) - c1;
			double c3 = _dst.get(k  , 0) - c1;
			double c4 = _dst.get(i+p, 0); 
			double c5 = _dst.get(j+p, 0) - c4; 
			double c6 = _dst.get(k+p, 0) - c4;
			
			double[] coeff = _coeff.getArray()[l];
			double[] alpha = _alpha.getArray()[l];
			double[] beta = _beta.getArray()[l];
			
			coeff[0] = c1 + c2*alpha[0] + c3*beta[0];
			coeff[1] =      c2*alpha[1] + c3*beta[1];
			coeff[2] =      c2*alpha[2] + c3*beta[2];
			coeff[3] = c4 + c5*alpha[0] + c6*beta[0];
			coeff[4] =      c5*alpha[1] + c6*beta[1];
			coeff[5] =      c5*alpha[2] + c6*beta[2];
		}
	}
	
	//=============================================================================
	void WarpRegion(FImage mapx, FImage mapy)
	{
		if((mapx.height != _mask.height) || (mapx.width != _mask.width))
			_mapx.internalAssign(new FImage(_mask.width, _mask.height));
		
		if((mapy.height != _mask.height) || (mapy.width != _mask.width))
			_mapy.internalAssign(new FImage(_mask.width, _mask.height));
		
		int k=-1; 
		double [] a=null, ap;
		
		final float [][] xp = mapx.pixels;
		final float [][] yp = mapy.pixels;
		final float [][] mp = _mask.pixels;
		
		for(int y = 0; y < _mask.height; y++) {
			double yi = y + _ymin;
			
			for(int x = 0; x < _mask.width; x++) { 
				double xi = x + _xmin;
		
				if(mp[y][x] == 0) {
					xp[y][x] = -1;
					yp[y][x] = -1;
				} else {
					int j = _tridx[y][x]; 
					
					if(j != k) {
						a = _coeff.getArray()[j]; 
						k = j;
					}  	
					ap = a;
					double xo = ap[0];
					xo += ap[1] * xi;
					xp[y][x] = (float) (xo + ap[2] * yi);
					
					double yo = ap[3];
					yo += ap[4] * xi;
					yp[y][x] = (float) (yo + ap[5] * yi);
				}
			}
		}
	}
}
