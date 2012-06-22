package org.openimaj.image.processing.face.tracking.clm;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Scanner;

import org.openimaj.image.FImage;
import org.openimaj.image.analysis.algorithm.FourierTemplateMatcher;
import org.openimaj.image.analysis.algorithm.TemplateMatcher;
import org.openimaj.image.analysis.algorithm.TemplateMatcher.Mode;
import org.openimaj.image.processing.resize.ResizeProcessor;
import org.openimaj.math.geometry.shape.Rectangle;

import Jama.Matrix;

public class Tracker {
	private static final double TSCALE=0.3;
	
    /**< Constrained Local Model           */
	public CLM        _clm;
	
	/**< Face Detector                     */
	FDet       _fdet;
	
	/**< Frame number since last detection */
	long      _frame;
	
	/**< Failure checker                   */
	MFCheck    _fcheck;
	
	/**< Current shape                     */
	public Matrix    _shape;
	
	/**< Reference shape                   */
	public Matrix    _rshape;
	
	/**< Detected rectangle                */
	Rectangle   _rect;
	
	/**< Initialization similarity         */
	double[]  _simil;

	FImage gray_, temp_;

	private FImage small_;
	
	Tracker(CLM clm, FDet fdet, MFCheck fcheck, Matrix rshape, double[] simil)
	{
		_clm = clm; 
		_fdet = fdet; 
		_fcheck = fcheck;

		_rshape = rshape.copy(); 
		_simil = simil;

		_shape = new Matrix(2*_clm._pdm.nPoints(), 1);
		_rect.x = 0; 
		_rect.y = 0; 
		_rect.width = 0; 
		_rect.height = 0; 
		_frame = -1; 
		_clm._pdm.Identity(_clm._plocal, _clm._pglobl);
	}

	Tracker() {
	}
	
	/** Reset frame number (will perform detection in next image) */
    public void FrameReset() {
    	_frame = -1;
    }

	//===========================================================================
	static Tracker Load(final String fname) throws FileNotFoundException
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
	
	public static Tracker Load(final InputStream in)
	{
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(in));
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
		s.write(IO.Types.TRACKER.ordinal() + " ");
		
		_clm.Write(s); 
		_fdet.Write(s); 
		_fcheck.Write(s); 
		IO.WriteMat(s, _rshape); 
		
		s.write(_simil[0] + " " + _simil[1] + " " + _simil[2] + " " + _simil[3] + " ");
	}
	//===========================================================================
	static Tracker Read(Scanner s, boolean readType)
	{
		if (readType) { 
			int type = s.nextInt();
			assert(type == IO.Types.TRACKER.ordinal());
		}
		Tracker tracker = new Tracker();
		tracker._clm = CLM.Read(s, true);
		tracker._fdet = FDet.Read(s, true);
		tracker._fcheck = MFCheck.Read(s, true); 
		tracker._rshape = IO.ReadMat(s);
		
		tracker._simil = new double[] {s.nextDouble(), s.nextDouble(), s.nextDouble(), s.nextDouble()}; 
		tracker._shape = new Matrix(2*tracker._clm._pdm.nPoints(), 1);
		
		tracker._rect = new Rectangle();
		tracker._rect.x = 0;
		tracker._rect.y = 0;
		tracker._rect.width = 0;
		tracker._rect.height = 0;
		tracker._frame = -1;
		tracker._clm._pdm.Identity(tracker._clm._plocal, tracker._clm._pglobl);
		
		return tracker;
	}
	//===========================================================================
	public int Track(FImage im, int[] wSize, final int fpd, final int nIter, final double clamp,final double fTol, final boolean fcheck)
	{ 
		gray_ = im;

		boolean gen, rsize=true; 
		Rectangle R;
		
		if ((_frame < 0) || (fpd >= 0 && fpd < _frame)) {
			_frame = 0;
			R = _fdet.Detect(gray_);
			gen = true;
		} else {
			R = ReDetect(gray_);
			gen = false;
		}
		
		if ((R.width == 0) || (R.height == 0)) { 
			_frame = -1;
			return -1;
		}
		
		_frame++;
		
		if(gen) {
			InitShape(R, _shape);
			
			_clm._pdm.CalcParams(_shape, _clm._plocal, _clm._pglobl);
		} else {
			double tx = R.x - _rect.x;
			double ty = R.y - _rect.y;
			
			_clm._pglobl.getArray()[4][0] += tx;
			_clm._pglobl.getArray()[5][0] += ty; 
			
			rsize = false;
		}
		
		_clm.Fit(gray_ ,wSize, nIter, clamp, fTol);
		
		_clm._pdm.CalcShape2D(_shape,_clm._plocal,_clm._pglobl);
		
		if (fcheck) {
			if (!_fcheck.Check(_clm.GetViewIdx(),gray_,_shape))
				return -1;
		}
		
		_rect = UpdateTemplate(gray_, _shape, rsize);
		
		if((_rect.width == 0) || (_rect.height == 0))
			return -1; 

		return 0;
	}
	//===========================================================================
	void InitShape(Rectangle r, Matrix shape)
	{
		assert((shape.getRowDimension() == _rshape.getRowDimension()) && (shape.getColumnDimension() == _rshape.getColumnDimension()));
		
		int n = _rshape.getRowDimension() / 2; 
		
		double a = r.width * Math.cos( _simil[1] ) * _simil[0] + 1;
		double b = r.width * Math.sin( _simil[1] ) * _simil[0];
		
		double tx = r.x + (int)(r.width/2)  + r.width *_simil[2];
		double ty = r.y + (int)(r.height/2) + r.height*_simil[3];
		
		double[][] s = _rshape.getArray();
		double[][] d = shape.getArray();
		
		for (int i = 0; i < n; i++) {
			d[i][0] = a*s[i][0] - b*s[i+n][0] + tx; 
			d[i+n][0] = b*s[i][0] + a*s[i+n][0] + ty;
		}
	}
	//===========================================================================
	Rectangle ReDetect(FImage im)
	{
		final int ww = im.width;
		final int hh = im.height;
		
		int w = (int) (TSCALE * ww - temp_.width + 1);
		int h = (int) (TSCALE * hh - temp_.height + 1);
		
		small_ = ResizeProcessor.resample(im, (int)(TSCALE*ww), (int)(TSCALE*hh));
		
		h = small_.height - temp_.height + 1;
		w = small_.width - temp_.width + 1;
		
		FourierTemplateMatcher matcher = new FourierTemplateMatcher(temp_, FourierTemplateMatcher.Mode.NORM_CORRELATION_COEFFICIENT);
		matcher.analyseImage(small_);
		float[][] ncc_ = matcher.getResponseMap().pixels; 
		
		Rectangle R = temp_.getBounds();
		float v, vb=-2;
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				v = ncc_[y][x];
				
				if (v > vb) {
					vb = v; 
					R.x = x; 
					R.y = y;
				}
			}
		}
		
		R.x *= 1.0/TSCALE; 
		R.y *= 1.0/TSCALE;
		
		R.width *= 1.0/TSCALE; 
		R.height *= 1.0/TSCALE; 
		
		return R;
	}
	//===========================================================================
	Rectangle UpdateTemplate(FImage im, Matrix s, boolean rsize)
	{
		final int n = s.getRowDimension() / 2; 
		
		double[][] sv = s.getArray(); //,y = s.begin<double>()+n;
		double xmax=sv[0][0], ymax=sv[n][0], xmin=sv[0][0], ymin=sv[n][0];
		
		for (int i = 0; i < n; i++) {
			double vx = sv[i  ][0];
			double vy = sv[i+n][0];
			
			xmax = Math.max(xmax, vx); 
			ymax = Math.max(ymax, vy);
			
			xmin = Math.min(xmin, vx); 
			ymin = Math.min(ymin, vy);
		}
		
		if ((xmin < 0) || (ymin < 0) || (xmax >= im.width) || (ymax >= im.height) ||
				Double.isNaN(xmin) || Double.isInfinite(xmin) || Double.isNaN(xmax) || Double.isInfinite(xmax) ||
				Double.isNaN(ymin) || Double.isInfinite(ymin) || Double.isNaN(ymax) || Double.isInfinite(ymax)) {
			return new Rectangle(0, 0, 0, 0);
		} else {
			xmin *= TSCALE; 
			ymin *= TSCALE; 
			xmax *= TSCALE; 
			ymax *= TSCALE;
			
			Rectangle R = new Rectangle(
					(float)Math.floor(xmin), 
					(float)Math.floor(ymin), 
					(float)Math.ceil(xmax-xmin), 
					(float)Math.ceil(ymax-ymin));
			
			final int ww = im.width;
			final int hh = im.height;
			
			if(rsize) {
				small_ = ResizeProcessor.resample(im, (int)(TSCALE*ww), (int)(TSCALE*hh));
			}
			
			temp_ = small_.extractROI(R);
			
			R.x *= 1.0 / TSCALE;
			R.y *= 1.0 / TSCALE;
			R.width *= 1.0 / TSCALE; 
			R.height *= 1.0 / TSCALE; 
			
			return R;
		}
	}
}
