package org.openimaj.image.processing.face.tracking.clm;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

import org.openimaj.image.processing.face.tracking.clm.CLM.SimTData;
import org.openimaj.math.matrix.MatrixUtils;

import Jama.Matrix;
import Jama.SingularValueDecomposition;

public class PDM {
	/**< basis of variation                            */
	public Matrix _V;
	
	/**< vector of eigenvalues (row vector)            */
	public Matrix _E;
	
	/**< mean 3D shape vector [x1,..,xn,y1,...yn]      */
	public Matrix _M; 

	private Matrix S_,R_,P_,Px_,Py_,Pz_,R1_,R2_,R3_;

	void AddOrthRow(Matrix R)
	{
		assert((R.getRowDimension() == 3) && (R.getColumnDimension() == 3));

		R.set(2 ,0, R.get(0,1)*R.get(1,2) - R.get(0,2)*R.get(1,1));
		R.set(2, 1, R.get(0,2)*R.get(1,0) - R.get(0,0)*R.get(1,2));
		R.set(2, 2, R.get(0,0)*R.get(1,1) - R.get(0,1)*R.get(1,0));
	}

	//=============================================================================
	void MetricUpgrade(Matrix R)
	{
		assert((R.getRowDimension() == 3) && (R.getColumnDimension() == 3));
		SingularValueDecomposition svd = R.svd();

		Matrix X = svd.getU().times(svd.getV().transpose());
		Matrix W = Matrix.identity(3, 3);
		W.set(2, 2, X.det());

		R.setMatrix(0, 3-1, 0, 3-1, svd.getU().times(W).times(svd.getV().transpose())); 
	}

	//===========================================================================
	Matrix Euler2Rot(final double pitch,final double yaw,final double roll)
	{
		return Euler2Rot(pitch, yaw, roll, true);	
	}

	Matrix Euler2Rot(final double pitch, final double yaw, final double roll, boolean full)
	{
		Matrix R;
		if(full) {
			R = new Matrix(3, 3);
		} else {
			R = new Matrix(2, 3);
		}

		double sina = Math.sin(pitch), sinb = Math.sin(yaw), sinc = Math.sin(roll);
		double cosa = Math.cos(pitch), cosb = Math.cos(yaw), cosc = Math.cos(roll);
		R.set(0, 0, cosb*cosc); R.set(0, 1, -cosb*sinc); R.set(0, 2, sinb);
		R.set(1, 0, cosa*sinc + sina*sinb*cosc);
		R.set(1, 1, cosa*cosc - sina*sinb*sinc);
		R.set(1, 2, -sina*cosb); 

		if(full) AddOrthRow(R); 

		return R;
	}

	//===========================================================================
	Matrix Euler2Rot(Matrix p) 
	{
		return Euler2Rot(p, true);
	}

	Matrix Euler2Rot(Matrix p, boolean full)
	{
		assert((p.getRowDimension() == 6) && (p.getColumnDimension() == 1));
		return Euler2Rot(p.get(1,0),p.get(2,0),p.get(3,0),full); 
	}
	//=============================================================================
	double[] Rot2Euler(Matrix R)
	{
		assert((R.getRowDimension() == 3) && (R.getColumnDimension() == 3));
		double [] q = new double[4];
		q[0] = Math.sqrt(1+R.get(0,0)+R.get(1,1)+R.get(2,2))/2;
		q[1] = (R.get(2,1) - R.get(1,2)) / (4*q[0]) ;
		q[2] = (R.get(0,2) - R.get(2,0)) / (4*q[0]) ;
		q[3] = (R.get(1,0) - R.get(0,1)) / (4*q[0]) ;
		double yaw = Math.asin(2*(q[0]*q[2] + q[1]*q[3]));
		double pitch = Math.atan2(2*(q[0]*q[1]-q[2]*q[3]),
				q[0]*q[0]-q[1]*q[1]-q[2]*q[2]+q[3]*q[3]); 
		double roll = Math.atan2(2*(q[0]*q[3]-q[1]*q[2]),
				q[0]*q[0]+q[1]*q[1]-q[2]*q[2]-q[3]*q[3]);
		return new double[] {pitch, roll, yaw};
	}

	//=============================================================================
	void Rot2Euler(Matrix R, Matrix p)
	{
		assert((p.getRowDimension() == 6) && (p.getColumnDimension() == 1));
		double[] pry = Rot2Euler(R);

		p.set(1, 0, pry[0]);
		p.set(2, 0, pry[2]);
		p.set(3, 0, pry[1]);
	}

	class AlignmentParams {
		double scale;
		double pitch;
		double yaw;
		double roll;
		double x;
		double y;
	}

	//=============================================================================
	void Align3Dto2DShapes(AlignmentParams ap, Matrix s2D, Matrix s3D)
	{
		assert((s2D.getColumnDimension() == 1) && (s3D.getRowDimension() == 3*(s2D.getRowDimension() / 2)) && (s3D.getColumnDimension() == 1));

		final int n = s2D.getRowDimension() / 2;
		double [] t2 = new double[2];
		double [] t3 = new double[3];

		Matrix X = MatrixUtils.reshape(s2D, 2).transpose();
		Matrix S = MatrixUtils.reshape(s3D, 3).transpose();

		for (int i = 0; i < 2; i++) {
			t2[i] = MatrixUtils.sumColumn(X, i) / n;
			MatrixUtils.incrColumn(X, i, -t2[i]);
		}

		for(int i = 0; i < 3; i++){
			t3[i] = MatrixUtils.sumColumn(S, i) / n; 
			MatrixUtils.incrColumn(S, i, -t3[i]);
		}

		Matrix M = ((S.transpose().times(S)).inverse()).times(S.transpose()).times(X);

		Matrix MtM = M.transpose().times(M);

		SingularValueDecomposition svd = MtM.svd();
		Matrix svals = svd.getS();
		svals.set(0,0, 1.0 / Math.sqrt(svals.get(0,0)));
		svals.set(1,1, 1.0 / Math.sqrt(svals.get(1,1)));

		Matrix T = new Matrix(3,3);
		T.setMatrix(0, 2-1, 0, 3-1, svd.getU().times(svals).times(svd.getV().transpose()).times(M.transpose()));

		ap.scale = 0;
		for (int r=0; r<2; r++)
			for (int c=0; c<3; c++) 
				ap.scale += T.get(r, c) * M.get(c, r);
		ap.scale *= 0.5;

		AddOrthRow(T);

		double [] pyr = Rot2Euler(T);
		ap.pitch = pyr[0];
		ap.roll = pyr[1];
		ap.yaw = pyr[2];

		T = T.times(ap.scale);

		ap.x = t2[0] - (T.get(0,0)*t3[0] + T.get(0,1)*t3[1] + T.get(0,2)*t3[2]);
		ap.y = t2[1] - (T.get(1,0)*t3[0] + T.get(1,1)*t3[1] + T.get(1,2)*t3[2]);
	}
	//	//=============================================================================
	//	//=============================================================================
	//	//=============================================================================
	//	//=============================================================================
	//	//=============================================================================
	//	//=============================================================================
	//	//=============================================================================
	//	//=============================================================================
	//	//=============================================================================
	//	//=============================================================================
	//	//=============================================================================
	//	PDM& PDM::operator= (PDM final& rhs)
	//	{   
	//		this->_V  = rhs._V.clone();  this->_E  = rhs._E.clone();
	//		this->_M  = rhs._M.clone();  this->S_  = rhs.S_.clone();
	//		this->R_  = rhs.R_.clone();  this->s_  = rhs.s_.clone();
	//		this->P_  = rhs.P_.clone();  this->Px_ = rhs.Px_.clone();
	//		this->Py_ = rhs.Py_.clone(); this->Pz_ = rhs.Pz_.clone();
	//		this->R1_ = rhs.R1_.clone(); this->R2_ = rhs.R2_.clone(); 
	//		this->R3_ = rhs.R3_.clone(); return *this;
	//	}
	//	//===========================================================================
	//	void PDM::Init(Matrix M, Matrix V, Matrix E)
	//	{
	//		assert((M.type() == CV_64F) && (V.type() == CV_64F) && (E.type() == CV_64F));
	//		assert((V.rows == M.rows) && (V.cols == E.cols));
	//		_M = M.clone(); _V = V.clone(); _E = E.clone();
	//		S_.create(_M.rows,1,CV_64F);  
	//		R_.create(3,3,CV_64F); s_.create(_M.rows,1,CV_64F); P_.create(2,3,CV_64F);
	//		Px_.create(2,3,CV_64F); Py_.create(2,3,CV_64F); Pz_.create(2,3,CV_64F);
	//		R1_.create(3,3,CV_64F); R2_.create(3,3,CV_64F); R3_.create(3,3,CV_64F);  
	//		return;
	//	}
	//	//===========================================================================
	void Clamp(Matrix p, double c)
	{
		assert((p.getRowDimension() == _E.getColumnDimension()) && (p.getColumnDimension() == 1));

		for(int i=0; i<p.getRowDimension(); i++) {
			double v = c*Math.sqrt(_E.get(0, i));
			double p1 = p.get(i, 0);

			if(Math.abs(p1) > v){
				if (p1 > 0.0) { 
					p1 = v; 
				} else {
					p1 = -v;
				}
			}
		}
	}
	//===========================================================================
	Matrix CalcShape3D(Matrix plocal)
	{
		assert((plocal.getRowDimension() == _E.getColumnDimension()) && (plocal.getColumnDimension() == 1));

		Matrix s = _M.plus(_V.times(plocal));

		return s;
	}
	//===========================================================================
	void CalcShape2D(Matrix s, Matrix plocal, Matrix pglobl)
	{
		assert((plocal.getRowDimension() == _E.getColumnDimension()) && (plocal.getColumnDimension() == 1));
		assert((pglobl.getRowDimension() == 6) && (pglobl.getColumnDimension() == 1));

		int n = _M.getRowDimension() / 3; 
		double a=pglobl.get(0, 0);
		double x=pglobl.get(4, 0);
		double y=pglobl.get(5, 0);

		R_ = Euler2Rot(pglobl);

		S_ = _M.plus(_V.times(plocal));

		//		if ((s.getRowDimension() != _M.getRowDimension()) || (s.getColumnDimension() != 1))
		//			s = new Matrix(2*n, 1);

		for(int i = 0; i < n; i++) {
			s.set(i  , 0, a*( R_.get(0, 0)*S_.get(i, 0) + R_.get(0, 1)*S_.get(i+n, 0) + R_.get(0, 2)*S_.get(i+n*2, 0) )+x);
			s.set(i+n, 0, a*( R_.get(1, 0)*S_.get(i, 0) + R_.get(1, 1)*S_.get(i+n, 0) + R_.get(1, 2)*S_.get(i+n*2, 0) )+y);
		}
	}

	//===========================================================================
	void CalcParams(Matrix s, Matrix plocal, Matrix pglobl)
	{  
		assert((s.getRowDimension() == 2*(_M.getRowDimension()/3)) && (s.getColumnDimension() == 1));

		//		if((pglobl.rows != 6) || (pglobl.cols != 1) || (pglobl.type() != CV_64F))
		//			pglobl.create(6,1,CV_64F);

		int n = _M.getRowDimension() / 3; 

		Matrix R = new Matrix(3, 3);
		//		Matrix z = new Matrix(n, 1);
		Matrix t = new Matrix(3, 1);
		Matrix p = new Matrix(_V.getColumnDimension(), 1);

		//		Matrix S = new Matrix(nPoints(), 3);

		//plocal = Matrix::zeros(_V.cols,1,CV_64F);
		MatrixUtils.zero(plocal);

		AlignmentParams ap = new AlignmentParams();

		for(int iter = 0; iter < 100; iter++){
			S_ = CalcShape3D(plocal);

			Align3Dto2DShapes(ap, s, S_);

			R = Euler2Rot(ap.pitch, ap.yaw, ap.roll);

			Matrix r = new Matrix(new double[][] { R.getArray()[2] });

			//S = (S_.reshape(1,3)).t();
			Matrix S = MatrixUtils.reshape(S_, 3).transpose();

			Matrix z = (S.times(r.transpose())).times(ap.scale); 
			double si = 1.0 / ap.scale;

			double Tx = -si*(R.get(0,0)*ap.x + R.get(1,0)*ap.y);
			double Ty = -si*(R.get(0,1)*ap.x + R.get(1,1)*ap.y);
			double Tz = -si*(R.get(0,2)*ap.x + R.get(1,2)*ap.y);

			for (int j = 0; j < n; j++) {
				t.set(0, 0, s.get(j, 0)); 
				t.set(1, 0, s.get(j+n, 0)); 
				t.set(2, 0, z.get(j, 0));

				S_.set(j    , 0, si * dotCol(t, R, 0) + Tx);
				S_.set(j+n  , 0, si * dotCol(t, R, 1) + Ty);
				S_.set(j+n*2, 0, si * dotCol(t, R, 2) + Tz);
			}

			plocal.setMatrix(0, p.getRowDimension()-1, 0, 1-1, _V.transpose().times(S_.minus(_M)));

			if(iter > 0) {
				//broken here//
				double norm = 0;
				for (int i=0; i<plocal.getRowDimension(); i++) {
					double diff = plocal.get(i, 0) - p.get(i, 0);
					norm += Math.abs(diff*diff);
				}
				norm = Math.sqrt(norm);

				if (norm < 1.0e-5)
					break;
			}

			p.setMatrix(0, p.getRowDimension()-1, 0, 1-1, plocal);
		}

		pglobl.set(0, 0, ap.scale);
		pglobl.set(1, 0, ap.pitch);
		pglobl.set(2, 0, ap.yaw);
		pglobl.set(3, 0, ap.roll);
		pglobl.set(4, 0, ap.x);
		pglobl.set(5, 0, ap.y);

		return;
	}

	private double dotCol(Matrix colvec, Matrix m, int col) {
		final int rows = colvec.getRowDimension();

		final double[][] colvec_arr = colvec.getArray();
		final double[][] m_arr = m.getArray();

		double dp = 0;
		for (int i=0; i<rows; i++) 
			dp += colvec_arr[i][0] * m_arr[i][col];

		return dp;
	}

	//===========================================================================
	void Identity(Matrix plocal, Matrix pglobl)
	{
		MatrixUtils.zero(plocal);// = Matrix::zeros(_V.cols,1,CV_64F);

		//pglobl = (Matrix_<double>(6,1) << 1, 0, 0, 0, 0, 0);
		MatrixUtils.zero(pglobl);
		pglobl.set(0, 0, 1);
	}
	//===========================================================================
	void CalcRigidJacob(Matrix plocal, Matrix pglobl, Matrix Jacob)
	{
		final int n = _M.getRowDimension() / 3;
		final int m = _V.getColumnDimension(); 

		assert((plocal.getRowDimension() == m)  && (plocal.getColumnDimension() == 1) && 
				(pglobl.getRowDimension() == 6)  && (pglobl.getColumnDimension() == 1) &&
				(Jacob.getRowDimension() == 2*n) && (Jacob.getColumnDimension() == 6));

		Matrix Rx = new Matrix(new double [][] {{0,0,0},{0,0,-1},{0,1,0}});
		Matrix Ry = new Matrix(new double [][] {{0,0,1},{0,0,0},{-1,0,0}});
		Matrix Rz = new Matrix(new double [][] {{0,-1,0},{1,0,0},{0,0,0}});

		double s = pglobl.get(0,0);

		S_ = CalcShape3D(plocal); 

		R_ = Euler2Rot(pglobl);

		P_ = R_.getMatrix(0, 2-1, 0, 3-1).times(s); 
		Px_ = P_.times(Rx); 
		Py_ = P_.times(Ry); 
		Pz_ = P_.times(Rz);

		final double[][] px = Px_.getArray();
		final double[][] py = Py_.getArray();
		final double[][] pz = Pz_.getArray();
		final double[][] r  =  R_.getArray();
		final double [][] J = Jacob.getArray();

		for(int i = 0; i < n; i++) {
			double X = S_.get(i, 0);
			double Y = S_.get(i+n, 0);
			double Z = S_.get(i+n*2, 0);
			J[i  ][0] = r[0][0]*X  + r[0][1]*Y  + r[0][2]*Z;
			J[i+n][0] = r[1][0]*X  + r[1][1]*Y  + r[1][2]*Z;
			J[i  ][1] = px[0][0]*X + px[0][1]*Y + px[0][2]*Z;
			J[i+n][1] = px[1][0]*X + px[1][1]*Y + px[1][2]*Z;
			J[i  ][2] = py[0][0]*X + py[0][1]*Y + py[0][2]*Z;
			J[i+n][2] = py[1][0]*X + py[1][1]*Y + py[1][2]*Z;
			J[i  ][3] = pz[0][0]*X + pz[0][1]*Y + pz[0][2]*Z;
			J[i+n][3] = pz[1][0]*X + pz[1][1]*Y + pz[1][2]*Z;
			J[i  ][4] = 1.0;
			J[i+n][4] = 0.0;
			J[i  ][5] = 0.0;
			J[i+n][5] = 1.0;
		}
	}
	//===========================================================================
	void CalcJacob(Matrix plocal, Matrix pglobl, Matrix Jacob)
	{ 
		final int n = _M.getRowDimension() / 3;
		final int m = _V.getColumnDimension(); 

		assert((plocal.getRowDimension() == m)  && (plocal.getColumnDimension() == 1) && 
				(pglobl.getRowDimension() == 6)  && (pglobl.getColumnDimension() == 1) &&
				(Jacob.getRowDimension() == 2*n) && (Jacob.getColumnDimension() == 6+m));
		double s = pglobl.get(0,0);

		Matrix Rx = new Matrix(new double [][] {{0,0,0},{0,0,-1},{0,1,0}});
		Matrix Ry = new Matrix(new double [][] {{0,0,1},{0,0,0},{-1,0,0}});
		Matrix Rz = new Matrix(new double [][] {{0,-1,0},{1,0,0},{0,0,0}});

		S_ = CalcShape3D(plocal); 

		R_ = Euler2Rot(pglobl);

		P_ = R_.getMatrix(0, 2-1, 0, 3-1).times(s); 
		Px_ = P_.times(Rx); 
		Py_ = P_.times(Ry); 
		Pz_ = P_.times(Rz);

		final double[][] px = Px_.getArray();
		final double[][] py = Py_.getArray();
		final double[][] pz = Pz_.getArray();
		final double[][] p  =  P_.getArray();
		final double[][] r  =  R_.getArray();

		final double[][] V =  _V.getArray();

		final double[][] J = Jacob.getArray();

		for(int i = 0; i < n; i++) {
			double X = S_.get(i, 0); 
			double Y = S_.get(i+n, 0); 
			double Z = S_.get(i+n*2, 0);

			J[i  ][0] =  r[0][0]*X +  r[0][1]*Y +  r[0][2]*Z;
			J[i+n][0] =  r[1][0]*X +  r[1][1]*Y +  r[1][2]*Z;
			J[i  ][1] = px[0][0]*X + px[0][1]*Y + px[0][2]*Z;
			J[i+n][1] = px[1][0]*X + px[1][1]*Y + px[1][2]*Z;
			J[i  ][2] = py[0][0]*X + py[0][1]*Y + py[0][2]*Z;
			J[i+n][2] = py[1][0]*X + py[1][1]*Y + py[1][2]*Z;
			J[i  ][3] = pz[0][0]*X + pz[0][1]*Y + pz[0][2]*Z;
			J[i+n][3] = pz[1][0]*X + pz[1][1]*Y + pz[1][2]*Z;
			J[i  ][4] = 1.0; 
			J[i+n][4] = 0.0; 
			J[i  ][5] = 0.0; 
			J[i+n][5] = 1.0;

			for(int j = 0; j < m; j++) {
				J[i  ][6+j] = p[0][0]*V[i][j] + p[0][1]*V[i+n][j] + p[0][2]*V[i+2*n][j];
				J[i+n][6+j] = p[1][0]*V[i][j] + p[1][1]*V[i+n][j] + p[1][2]*V[i+2*n][j];
			}
		}
	}
	//===========================================================================
	void CalcReferenceUpdate(Matrix dp, Matrix plocal, Matrix pglobl)
	{
		assert((dp.getRowDimension() == 6+_V.getColumnDimension()) && (dp.getColumnDimension() == 1));

		plocal.setMatrix(0, plocal.getRowDimension()-1, 0, plocal.getColumnDimension()-1, plocal.plus(dp.getMatrix(6, 6+_V.getColumnDimension()-1, 0, 1-1)));

		pglobl.set(0, 0, pglobl.get(0,0) + dp.get(0,0));
		pglobl.set(4, 0, pglobl.get(4,0) + dp.get(4,0));
		pglobl.set(5, 0, pglobl.get(5,0) + dp.get(5,0));

		R1_ = Euler2Rot(pglobl); 

		R2_ = Matrix.identity(3, 3);
		R2_.set(2, 1, dp.get(1, 0));
		R2_.set(1, 2, -R2_.get(2,1));

		R2_.set(0, 2, dp.get(2, 0));
		R2_.set(2, 0, -R2_.get(0,2));

		R2_.set(1, 0, dp.get(3, 0));
		R2_.set(0, 1, -R2_.get(1, 0));

		MetricUpgrade(R2_);
		R3_ = R1_.times(R2_); 
		Rot2Euler(R3_,pglobl);
	}
	//===========================================================================
	void ApplySimT(SimTData data, Matrix pglobl)
	{
		assert((pglobl.getRowDimension() == 6) && (pglobl.getColumnDimension() == 1));

		double angle = Math.atan2(data.b, data.a);
		double scale = data.a / Math.cos(angle);
		double ca = Math.cos(angle);
		double sa = Math.sin(angle);
		double xc = pglobl.get(4, 0);
		double yc = pglobl.get(5, 0);

		MatrixUtils.zero(R1_); 
		R1_.set(2, 2, 1.0);
		R1_.set(0, 0, ca); R1_.set(0, 1, -sa); R1_.set(1, 0, sa); R1_.set(1, 1, ca);

		R2_ = Euler2Rot(pglobl); 
		R3_ = R1_.times(R2_);

		pglobl.set(0, 0, pglobl.get(0, 0) * scale); 
		Rot2Euler(R3_, pglobl);

		pglobl.set(4, 0, data.a*xc - data.b*yc + data.tx); 
		pglobl.set(5, 0, data.b*xc + data.a*yc + data.ty);
	}

	//===========================================================================
	static PDM Read(Scanner s, boolean readType)
	{
		if (readType) {
			int type = s.nextInt(); 
			assert(type == IO.Types.PDM.ordinal());
		}

		PDM pdm = new PDM();

		pdm._V = IO.ReadMat(s); 
		pdm._E = IO.ReadMat(s); 
		pdm._M = IO.ReadMat(s);

		pdm.S_ = new Matrix(pdm._M.getRowDimension(), 1);  
		pdm.R_ = new Matrix(3, 3);  
		pdm.P_ = new Matrix(2, 3);
		pdm.Px_ = new Matrix(2,3); 
		pdm.Py_ = new Matrix(2, 3); 
		pdm.Pz_ = new Matrix(2, 3);
		pdm.R1_ = new Matrix(3, 3); 
		pdm.R2_ = new Matrix(3, 3); 
		pdm.R3_ = new Matrix(3, 3);

		return pdm;
	}

	//	//===========================================================================
	void Write(BufferedWriter s) throws IOException
	{
		s.write(IO.Types.PDM.ordinal() + " ");

		IO.WriteMat(s, _V); 
		IO.WriteMat(s, _E); 
		IO.WriteMat(s, _M);
	}

	//	//===========================================================================
	static PDM Load(final String fname) throws FileNotFoundException
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

	final int nPoints() {
		return _M.getRowDimension() / 3;
	}

	int nModes() {
		return _V.getColumnDimension();
	}

	double Var(int i){
		assert(i<_E.getColumnDimension()); 

		return _E.get(0,i);
	}
}
