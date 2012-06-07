package org.openimaj.demos.sandbox.tld;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.openimaj.data.RandomData;
import org.openimaj.image.FImage;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.math.matrix.MatrixUtils;
import org.openimaj.util.array.ArrayUtils;

import Jama.Matrix;

public class TLDInit {

	private TLDOptions opts;
	private static final int GRID_BORDER = 1;
	private static final int[] ROW_3 = new int[]{2};
	private static final int[] ROW_4 = new int[]{3};
	private Random random;
	private double thrN;
	private int wBBOX;
	private int hBBOX;
	private int nSCALE;
	private int iHEIGHT;
	private int iWIDTH;
	private Rectangle BBOX;
	private FImage integralImage;
	private FImage integralImage2;
	private Rectangle OFF;
	private List<List<Double>> WEIGHT;
	private List<List<Integer>> nP,nN;

	public TLDInit(TLDOptions opts) {
		this.opts = opts;
	}

	public void initWithFirstFrame(FImage frame, Rectangle bb) {
		List<ScaledSpacedCellGrid> grids = precalculateSearchGrids(frame, bb);
		// collapse the producers, just to match the matlab code and make coding this easier
		this.opts.grid = collapseGrids(grids);
		this.opts.scales = collapseScales(grids);
		this.opts.nGrid  = this.opts.grid.length;
		
		// prepare and seed randomly the array which represents the classification forest
		this.opts.features = generateFeatures((Integer)opts.model.get("num_trees"),(Integer)opts.model.get("num_features"));
		
		// init the fern
		this.opts.fern = new TLDFernDetector(opts);
		this.opts.fern.cleanup();
		this.opts.fern.init(frame);
	}
	
	private List<ScaledSpacedCellGrid> precalculateSearchGrids(FImage frame, Rectangle bb) {
		int min_win = (Integer) opts.model.get("min_win");
		Rectangle imsize = frame.getBounds();

		double SHIFT = 0.1;
		Matrix SCALE = MatrixUtils.rangePow(1.2,-10,10); double[][] SCALEdata = SCALE.getArray();
		int MINBB = min_win;

//		% Chack if input bbox is smaller than minimum
		if (Math.min(bb.width,bb.height) < MINBB)
		{
			this.opts.grid = null;
		}

		Matrix bbW = MatrixUtils.round(SCALE.times(bb.width)); double[][] bbwData = bbW.getArray();
		Matrix bbH = MatrixUtils.round(SCALE.times(bb.height)); double[][] bbhData = bbH.getArray();
		Matrix bbSHH = MatrixUtils.min(bbH,bbH).times(SHIFT); double[][] bbSHHData = bbSHH.getArray();
		Matrix bbSHW = MatrixUtils.min(bbH,bbW).times(SHIFT); double[][] bbSHWData = bbSHW.getArray();
		
		List<ScaledSpacedCellGrid> grids = new ArrayList<ScaledSpacedCellGrid>();
		// What this is doing is getting the set of bounding box positions which are above MINBB and within the border of the image with 1 pixel padding
	    // Note also all the rounding! this guy does not deal with subpixels
		// He is then doing this for all the scales calculated above
		for (int i = 0; i < SCALEdata[0].length; i++) { // for i = 1:length(SCALE)
		    if (bbwData[0][i] < MINBB || bbhData[0][i] < MINBB) continue;
		    // equivilant to what he was doing, but a producer model would be much better i think
//		    Rectangle[] grid = createRectangleGrid(imsize,GRID_BORDER,bbwData[0][i],bbhData[0][i],bbSHWData[0][i],bbSHHData[0][i]);
		    // even better! a producer model, knows how to generate each cell and does so in an iterator
		    ScaledSpacedCellGrid scg = new ScaledSpacedCellGrid(imsize,GRID_BORDER,bbwData[0][i],bbhData[0][i],bbSHWData[0][i],bbSHHData[0][i],SCALEdata[0][i]);
		    if(scg.size() == 0) continue;
		    grids.add(scg);
		}
		return grids;
	}

	private TLDFernFeatures generateFeatures(int nTREES,int nFEAT) {
		double SHI = 1d/5d;
		double SCA = 1d;
		
		Matrix x = MatrixUtils.range(0,SHI,1);
		x = MatrixUtils.ntuples(x,x);
		x = MatrixUtils.repmat(x,2,1);
		x = MatrixUtils.hstack(x,MatrixUtils.plus(x.copy(), SHI/2));
		int k = x.getColumnDimension();
		Matrix r = x.copy();
		Matrix rRand = MatrixUtils.plus(Matrix.random(1,k).times(SCA),SHI);
		MatrixUtils.plusEqualsRow(r, rRand, ROW_3); // r.pl(3,:) = r(3,:) + (SCA*rand(1,k)+SHI);
		Matrix l = x.copy();
		Matrix lRand = MatrixUtils.plus(Matrix.random(1,k).times(SCA),SHI);
		MatrixUtils.plusEqualsRow(l, lRand.times(-1), ROW_3); // l(3,:) = l(3,:) - (SCA*rand(1,k)+SHI);
		Matrix t = x.copy();
		Matrix tRand = MatrixUtils.plus(Matrix.random(1,k).times(SCA),SHI);
		MatrixUtils.plusEqualsRow(t, tRand.times(-1), ROW_4);// t(4,:) = t(4,:) - (SCA*rand(1,k)+SHI);
		Matrix b = x.copy();
		Matrix bRand = MatrixUtils.plus(Matrix.random(1,k).times(SCA),SHI);
		MatrixUtils.plusEqualsRow(b, bRand, ROW_4); // b(4,:) = b(4,:) + (SCA*rand(1,k)+SHI);

		x = MatrixUtils.hstack(r,l,t,b); //[r l t b];
		// idx = all(x([1 2],:) < 1 & x([1 2],:) > 0,1); 
		int[] xcols = ArrayUtils.range(0,x.getColumnDimension()-1);
		int[] xrows = ArrayUtils.range(0,x.getRowDimension()-1);
		Matrix xOneTwo = x.getMatrix(new int[]{0,1}, xcols);
		Matrix allGoodCols = MatrixUtils.all(MatrixUtils.and(MatrixUtils.lessThan(xOneTwo,1),MatrixUtils.greaterThan(xOneTwo,0)));
		int[] idx = MatrixUtils.valsToIndex(allGoodCols.getArray()[0]);
		// x = x(:,idx);
		x = x.getMatrix(xrows,idx);
//		x(x > 1) = 1;
//		x(x < 0) = 0;
		MatrixUtils.greaterThanSet(x, 1,1);
		MatrixUtils.lessThanSet(x, 0,0);
		int[] xcolsShuffled = RandomData.getUniqueRandomInts(idx.length, 0, idx.length);
		
		x = x.getMatrix(xrows, xcolsShuffled);
		x = x.getMatrix(xrows, ArrayUtils.range(0, (nFEAT*nTREES)-1)); // (:,1:nFEAT*nTREES);
		x = MatrixUtils.reshape(x,4*nFEAT,true);
		
		TLDFernFeatures f = new TLDFernFeatures();
		f.x = x;
		f.type = "forest";

		return f;
	}

	private double[][] collapseScales(List<ScaledSpacedCellGrid> grids) {
		double[][] scales = new double[grids.size()][2];
		int i = 0;
		for (ScaledSpacedCellGrid d : grids) {
			scales[i][0] = d.cellheight;
			scales[i++][1] = d.cellwidth;
		}
		return scales;
	}

	private double[][] collapseGrids(List<ScaledSpacedCellGrid> grids) {
		int totalsize = 0;
		for (ScaledSpacedCellGrid scaledSpacedCellGrid : grids) {
			totalsize += scaledSpacedCellGrid.size();
		}
		double[][] collapsed = new double[totalsize][6];
		int i = 0;
		int indexOfScaleGrids= 0;
		for (ScaledSpacedCellGrid g : grids) {
			for (double[] ds : g) {
				System.arraycopy(ds, 0, collapsed[i], 0, 4);
				collapsed[i][4] = indexOfScaleGrids;
				collapsed[i][5] = g.nx; // the number of left/right grids is needed for the nearest-neighbour 
				i++;
			}
			indexOfScaleGrids++;
		}
		return collapsed;
	}

	private Rectangle[] createRectangleGrid(Rectangle imsize, int gridBorder,double boxwidth, double boxheight,double dwidth, double dheight) {
		double startx,starty,endx,endy;
		endx = imsize.getWidth()-gridBorder-boxwidth;
		endy = imsize.getHeight()-gridBorder-boxheight;
		startx = gridBorder;
		starty = gridBorder;
		
		int nx = (int) Math.round((endx - startx + 1)/dwidth);
		int ny = (int) Math.round((endy - starty + 1)/dheight);
		Rectangle[] grid = new Rectangle[nx * ny];
		int index = 0;
		for (int j = 0; j < ny; j++) {
			for (int i = 0; i < nx; i++) {
				grid[index++] = new Rectangle(
					(float)Math.round(startx + i * dwidth),
					(float)Math.round(starty + j * dheight),
					(float)(boxwidth),
					(float)(boxheight) 
				);
			}
		}
		return grid;
	}

}
