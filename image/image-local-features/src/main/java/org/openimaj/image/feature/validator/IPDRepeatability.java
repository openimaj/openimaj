package org.openimaj.image.feature.validator;

import gnu.trove.TDoubleArrayList;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import org.openimaj.image.Image;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.Transforms;
import org.openimaj.image.feature.local.interest.AbstractIPD.InterestPointData;
import org.openimaj.image.feature.local.interest.InterestPointDetector;
import org.openimaj.math.geometry.line.Line2d;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.math.geometry.shape.Ellipse;
import org.openimaj.math.geometry.shape.EllipseUtilities;
import org.openimaj.math.geometry.shape.Shape;
import org.openimaj.math.geometry.transforms.TransformUtilities;
import org.openimaj.util.pair.Pair;

import Jama.Matrix;

/**
 * An interest point repeatability as originally implemented here:
 * 
 * http://www.robots.ox.ac.uk/~vgg/research/affine/evaluation.html
 * 
 * We find some interest points in two images, and the known homography to go from image 1 to image 2
 * 
 * We apply this exhaustively to a pairwise matching of each feature to each other feature and compare the distances
 * of the transformed features from the second image to the features in the first image. If the pair distance is below
 * a give threshold they are placed on top of each other and their overlap measured.
 * 
 * Repeatability is measured at a given overlap threshold, if two feature point ellipses overlap over a certain percentage of 
 * their overall size then those features are counted as repeatable. The repeatability of a given IPD for a 
 * given pair of images is the proportion of repeatable features for a given maximum distance and a given 
 * overlap percentage.
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>, Sina Samangooei <ss@ecs.soton.ac.uk>
 *
 */
public class IPDRepeatability {
	
	public static class ScoredPair<B extends Comparable<B>,T extends Pair<B>> implements Comparable<ScoredPair<B,T>>{
		private T pair;
		private double score;

		public ScoredPair(T pair, double score){
			this.pair = pair;
			this.score = score;
		}

		@Override
		public int compareTo(ScoredPair<B,T> that) { 
			int diff = Double.compare(this.score, that.score);
			if(diff!=0)
				return -diff;
			else{
				diff = this.pair.firstObject().compareTo(that.pair.firstObject());
				if(diff == 0){
					return this.pair.secondObject().compareTo(that.pair.secondObject());
				}
				return diff;
			}
		}
	}
	
	private Matrix homography;
	private List<Ellipse> validImage2Points;
	private List<Ellipse> validImage1Points;
	private List<ScoredPair<Integer, Pair<Integer>>> prunedOverlapping;
	private double maximumDistanceMultiple = 4;

	/**
	 * Check the repeatability against two imags, two sets of points and a homography between the two images.
	 * 
	 * @param image1
	 * @param image2
	 * @param image1Points
	 * @param image2Points
	 * @param homography
	 */
	public IPDRepeatability(Image<?,?> image1, Image<?,?> image2, List<Ellipse> image1Points,List<Ellipse> image2Points,Matrix homography){
		setup(image1,image2,image1Points,image2Points,homography);
	}
	
	/**
	 * Check the repeatability between two images from files, an interest point detector used to find the feature points
	 * in the images and a homography from a file. The homography file has the format:
	 * 
	 * number	number	number
	 * number	number	number
	 * number	number	number
	 * 
	 * @param image1f
	 * @param image2f
	 * @param ipd
	 * @param homographyf
	 * @throws IOException
	 */
	public IPDRepeatability(File image1f, File image2f, InterestPointDetector ipd,File homographyf) throws IOException{
		MBFImage image1 = ImageUtilities.readMBF(image1f);
		MBFImage image2 = ImageUtilities.readMBF(image2f);
		
		ipd.findInterestPoints(Transforms.calculateIntensityNTSC(image1));
		List<InterestPointData> image1Points = ipd.getInterestPoints(20);
		ipd.findInterestPoints(Transforms.calculateIntensityNTSC(image2));
		List<InterestPointData> image2Points = ipd.getInterestPoints(20);
		
		List<Ellipse> image1Ellipse = new ArrayList<Ellipse>();
		List<Ellipse> image2Ellipse = new ArrayList<Ellipse>();
		for(InterestPointData d : image1Points) image1Ellipse.add(d.getEllipse());
		for(InterestPointData d : image2Points) image2Ellipse.add(d.getEllipse());
		
		Matrix homography = readHomography(homographyf);
		setup(image1,image2,image1Ellipse,image2Ellipse,homography);
	}
	
	/**
	 * Two images, features extracted using ipd, homography found in stream. See {@link IPDRepeatability}
	 * 
	 * @param image1
	 * @param image2
	 * @param ipd
	 * @param homographyf
	 * @throws IOException
	 */
	public IPDRepeatability(MBFImage image1, MBFImage image2, InterestPointDetector ipd,InputStream homographyf) throws IOException{
		
		ipd.findInterestPoints(Transforms.calculateIntensityNTSC(image1));
		List<InterestPointData> image1Points = ipd.getInterestPoints(20);
		ipd.findInterestPoints(Transforms.calculateIntensityNTSC(image2));
		List<InterestPointData> image2Points = ipd.getInterestPoints(20);
		
		List<Ellipse> image1Ellipse = new ArrayList<Ellipse>();
		List<Ellipse> image2Ellipse = new ArrayList<Ellipse>();
		for(InterestPointData d : image1Points) image1Ellipse.add(d.getEllipse());
		for(InterestPointData d : image2Points) image2Ellipse.add(d.getEllipse());
		
		Matrix homography = readHomography(homographyf);
		setup(image1,image2,image1Ellipse,image2Ellipse,homography);
	}
	
	/**
	 * Two images, features extracted using ipd, homography matrix between the two images
	 * 
	 * @param image1
	 * @param image2
	 * @param ipd
	 * @param homography
	 * @throws IOException
	 */
	public IPDRepeatability(MBFImage image1, MBFImage image2, InterestPointDetector ipd,Matrix homography) throws IOException{
		
		ipd.findInterestPoints(Transforms.calculateIntensityNTSC(image1));
		List<InterestPointData> image1Points = ipd.getInterestPoints(20);
		ipd.findInterestPoints(Transforms.calculateIntensityNTSC(image2));
		List<InterestPointData> image2Points = ipd.getInterestPoints(20);
		
		List<Ellipse> image1Ellipse = new ArrayList<Ellipse>();
		List<Ellipse> image2Ellipse = new ArrayList<Ellipse>();
		for(InterestPointData d : image1Points) image1Ellipse.add(d.getEllipse());
		for(InterestPointData d : image2Points) image2Ellipse.add(d.getEllipse());
		
		setup(image1,image2,image1Ellipse,image2Ellipse,homography);
	}

	public IPDRepeatability(List<Ellipse> firstImagePoints,List<Ellipse> secondImagePoints, Matrix transform) {
		this.validImage1Points = firstImagePoints;
		this.validImage2Points = secondImagePoints;
		this.homography = transform;
	}

	private void setup(Image<?,?> image1, Image<?,?> image2,List<Ellipse> image1Points,List<Ellipse> image2Points, Matrix homography) {
		this.homography = homography;
		
		this.validImage2Points = IPDRepeatability.validPoints(image2Points, image1, homography);
		this.validImage1Points = IPDRepeatability.validPoints(image1Points, image2, homography.inverse());
	}
	
	/**
	 * The percentage of valid points found to be repeatable. Repeatability of a given ellipse is defined by what percentage overlap there
	 * is between it and a nearby detected ellipse (after an affine transform). The repeatability of all the points in an image is defined by the proprotion
	 * of points which could catch and did match with their ellipses overlapping by more than or equal to the percentageOverlap (1 == complete overlap, 0 == no overlap)
	 * 
	 * @param percentageOverlap the percentage overlap two ellipses must be over to be considered a "repeatable" point
	 * @param maximumDistanceMultiple The distance multiple at which point two interest points are considered to be "close"
	 * @return the percentage of ellipses which are repeatable 
	 */
	public double repeatability(double percentageOverlap){
		prepare();
		double potentialMatches = Math.min(this.validImage2Points.size(), this.validImage1Points.size());
		double countMatches = 0;
		int totalSeen = 0;
		for(ScoredPair<Integer,Pair<Integer>> d : this.prunedOverlapping){
			totalSeen++;
			if(d.score > percentageOverlap)
				countMatches ++ ;
		}
		return countMatches / potentialMatches;
	}
	
	
	/**
	 * Find pairs of interest points whose ellipses overlap sufficiently and calculate how much they overlap. This function must be told
	 * what maximum distance factor is which two interest points are considered to match and therefore their overlap measured.
	 * 
	 * @param maximumDistanceFactor 
	 * 
	 * @return map of an interest point pair to a percentage overlap (0 > overlap =<1.0
	 */
	public List<ScoredPair<Integer,Pair<Integer>>> calculateOverlappingEllipses(){
		int smallerSetSize = Math.min(this.validImage1Points.size(), this.validImage2Points.size());
		PriorityQueue<ScoredPair<Integer,Pair<Integer>>>  overlapping = new PriorityQueue<ScoredPair<Integer,Pair<Integer>>>();
		int oldQueueSize = 0;
		Map<Integer,Matrix> matrixHash1 = new HashMap<Integer,Matrix>();
		Map<Integer,Matrix> matrixHash2 = new HashMap<Integer,Matrix>();
		int i =0; 
		for(Ellipse ellipse1 : this.validImage1Points)
		{
			int j = 0;
			if(!matrixHash1.containsKey(i)) 
				matrixHash1.put(i, EllipseUtilities.ellipseToCovariance(ellipse1));
			for(Ellipse ellipse2: this.validImage2Points)
			{
				
				ellipse2 = ellipse2.transformAffine(this.homography.inverse());
				if(!matrixHash2.containsKey(j))
				{
					matrixHash2.put(j, EllipseUtilities.ellipseToCovariance(ellipse2));
				}
				
				double overlap = calculateOverlapPercentageOxford(matrixHash1.get(i),matrixHash2.get(j),ellipse1, ellipse2,this.maximumDistanceMultiple);
				if(overlap > 0){
//					System.out.println(overlap + " Adding: " + ellipse1.getCOG() + " -> " + ellipse2.getCOG() + " with score: " + overlap);
					overlapping.add(new ScoredPair<Integer,Pair<Integer>>(new Pair<Integer>(i,j), overlap));
					if(oldQueueSize == overlapping.size()){
						System.err.println("The queue didn't change in size!!");
					}
					oldQueueSize = overlapping.size();
				}
				j++;
			}
			i++;
		}
		List<ScoredPair<Integer,Pair<Integer>>> prunedOverlapping = pruneOverlapping(overlapping, smallerSetSize);
		return prunedOverlapping;
	}
	
	private List<ScoredPair<Integer, Pair<Integer>>> pruneOverlapping(PriorityQueue<ScoredPair<Integer,Pair<Integer>>> overlapping, int smallerSetSize) {
		// Use the priority queue to perform a greedy optimisation. 
		// Once you see a pair don't allow any other pair involving either element
		Set<Integer> seenE1 = new HashSet<Integer>();
		Set<Integer> seenE2 = new HashSet<Integer>();
		List<ScoredPair<Integer,Pair<Integer>>> prunedOverlapping = new ArrayList<ScoredPair<Integer,Pair<Integer>>>();
		while(overlapping.size() > 0 && seenE1.size() < smallerSetSize){
			ScoredPair<Integer,Pair<Integer>> scoredPair = overlapping.poll();
			if(!(seenE1.contains(scoredPair.pair.firstObject()) || seenE2.contains(scoredPair.pair.secondObject()))){
				prunedOverlapping.add(scoredPair);
				seenE1.add(scoredPair.pair.firstObject());
				seenE2.add(scoredPair.pair.secondObject());
			}
			else{
			}
		}
		return prunedOverlapping;
	}
	
	
	/**
	 * Generates and initialises a new Repeatability instance. The percentage of valid points found to be repeatable. Repeatability of a given ellipse is defined by what percentage overlap there
	 * is between it and a nearby detected ellipse (after an affine transform). The repeatability of all the points in an image is defined by the proprotion
	 * of points which could catch and did match with their ellipses overlapping by more than or equal to the percentageOverlap (1 == complete overlap, 0 == no overlap)
	 * 
	 * @param percentageOverlap the percentage overlap two ellipses must be over to be considered a "repeatable" point
	 * @param maximumDistanceMultiple The distance multiple at which point two interest points are considered to be "close"
	 * @return the percentage of ellipses which are repeatable 
	 */
	public static IPDRepeatability repeatability(Image<?,?> img1, Image<?,?> img2, List<Ellipse> e1, List<Ellipse> e2, Matrix transform, double maximumDistanceMultiple){
		IPDRepeatability rep = new IPDRepeatability(img1,img2,e1,e2,transform);
		rep.maximumDistanceMultiple = maximumDistanceMultiple;
		rep.prepare();
		return rep;
	}
	
	public static IPDRepeatability repeatability(MBFImage image1,MBFImage image2, List<InterestPointData> interestPoints1,List<InterestPointData> interestPoints2, Matrix transform,int maximumDistanceMultiple2) {
		List<Ellipse> image1Ellipse = new ArrayList<Ellipse>();
		List<Ellipse> image2Ellipse = new ArrayList<Ellipse>();
		for(InterestPointData d : interestPoints1) image1Ellipse.add(d.getEllipse());
		for(InterestPointData d : interestPoints2) image2Ellipse.add(d.getEllipse());
		IPDRepeatability rep = new IPDRepeatability(image1,image2,image1Ellipse,image2Ellipse,transform);
		rep.maximumDistanceMultiple = 4;
		rep.prepare();
		return rep;
	}
	
	private void prepare() {
		if(this.prunedOverlapping==null)
			this.prunedOverlapping = calculateOverlappingEllipses();
	}

	/**
	 * Use the transform to call find the location sourceImage.getBounds() in another image. Drop points from allPoints which are not within the transformed bounds
	 * @param allPoints
	 * @param sourceImage
	 * @param transform
	 * @return
	 */
	public static List<Ellipse> validPoints(List<Ellipse> allPoints,Image<?, ?> sourceImage, Matrix transform) {
		List<Ellipse> valid = new ArrayList<Ellipse>();
		Shape validArea = sourceImage.getBounds();
		for(Ellipse data : allPoints){
			if(data.getCOG().getX() == 294.079f && data.getCOG().getY() == 563.356f){
				System.out.println();
			}
			if(validArea.isInside(data.getCOG().transform(transform.inverse()))){
				valid.add(data);
			}else{
			}
		}
		return valid;
	}
	
	public static List<ScoredPair<Integer, Pair<Integer>>> calculateOverlappingEllipses(List<Ellipse> firstImagePoints, List<Ellipse> secondImagePoints,Matrix transform, double maximumDistanceMultiple) {
		IPDRepeatability rep = new IPDRepeatability(firstImagePoints,secondImagePoints,transform);
		rep.maximumDistanceMultiple = maximumDistanceMultiple;
		return rep.calculateOverlappingEllipses();
	}
	
	public static Matrix readHomography(File homographyf) throws IOException {
		return readHomography(new FileInputStream(homographyf));
	}
	public static Matrix readHomography(InputStream homographyf) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(homographyf));
		List<TDoubleArrayList> doubleListList = new ArrayList<TDoubleArrayList>();
		String line = null;
		int nCols = -1;
		int nRows = 0;
		while((line = reader.readLine())!=null){
			boolean anyAdded = false;
			String[] parts = line.split(" ");
			TDoubleArrayList doubleList = new TDoubleArrayList();
			int currCols= 0;
			for(String part : parts){
				if(part.length() != 0){
					anyAdded = true;
					doubleList.add(Double.parseDouble(part));
					currCols++;
				}
			}
			if(nCols == -1) nCols = currCols;
			if(currCols!=nCols) throw new IOException("Could not read matrix file");
			if(anyAdded){
				doubleListList.add(doubleList);
				nRows ++;
			}
		}
		Matrix ret = new Matrix(nRows,nCols);
		int rowNumber = 0;
		for(TDoubleArrayList doubleList: doubleListList){
			doubleList.toNativeArray(ret.getArray()[rowNumber++], 0, nCols);
		}
		return ret;
	}
	
	/**
	 * The overlap of a pair of ellipses
	 * @param e1
	 * @param e2
	 * @return the percentage overlap with a maximu distance of 4 * scale by ellipse largest ellipse
	 */
	public static double calculateOverlapPercentage(Ellipse e1, Ellipse e2){
		return calculateOverlapPercentageOxford(EllipseUtilities.ellipseToCovariance(e1),EllipseUtilities.ellipseToCovariance(e2),e1,e2,4);
	}
	
	/**
	 * The overlap of a pair of ellipses
	 * @param e1
	 * @param e2
	 * @param maximumDistanceFactor
	 * @return the percentage overlap with a maximum distance scaled by  maximumDistanceFactor * ellipse scale 
	 */
	public static double calculateOverlapPercentageNew(Ellipse e1, Ellipse e2, double maximumDistanceFactor){
		double maxDistance = 1/Math.sqrt((e1.getMajor() * e1.getMinor()));
		// This is a scaling of the two ellipses such that they fit in a normalised space
		double scaleFactor = 30 / maxDistance;
		scaleFactor = 1 / (scaleFactor * scaleFactor);
		maxDistance*=maximumDistanceFactor*2;
		
//		System.out.println("Maximum distance was: " + maxDistance);
		if(new Line2d(e1.getCOG(),e2.getCOG()).calculateLength() >= maxDistance) return 0;
		Matrix scaleMatrix = TransformUtilities.scaleMatrix(scaleFactor, scaleFactor);
		
		Matrix e1Trans = TransformUtilities.translateToPointMatrix(e1.getCOG(),new Point2dImpl(0,0));
		Matrix e2Trans = TransformUtilities.translateToPointMatrix(e2.getCOG(),new Point2dImpl(0,0));
		Ellipse e1Corrected = e1.transformAffine(scaleMatrix.times(e1Trans));
		Ellipse e2Corrected = e2.transformAffine(scaleMatrix.times(e2Trans));
		
		double e1Area = e1Corrected.calculateArea();
		double e2Area = e2Corrected.calculateArea();
		
		double intersection = e1Corrected.intersectionArea(e2Corrected, 200);
		return intersection/( (e1Area - intersection) + (e2Area - intersection) + intersection);
	}
	
	/**
	 * This is how the original matlab found the difference between two ellipses. Basically, if ellipse 1 and 2 were
	 * within a certain distance the ellipses were placed on top of each other (i.e. same centroid) and the difference between
	 * them calculated. This is simulated in a much saner way in calculateOverlapPercentage
	 * @param e1Mat
	 * @param e2Mat
	 * @param e1
	 * @param e2
	 * @return the overlap percentage as calculated the matlab way (uses the covariance matricies of the ellipses)
	 */
	public static double calculateOverlapPercentageOxford(Matrix e1Mat,Matrix e2Mat,Ellipse e1, Ellipse e2, double multiplier){
		double maxDistance = 1/Math.sqrt((e1.getMajor() * e1.getMinor()));
		
		// This is a scaling of the two ellipses such that they fit in a normalised space
		double scaleFactor = 30 / maxDistance;
		scaleFactor = 1 / (scaleFactor * scaleFactor);
		maxDistance*=multiplier;
		if(new Line2d(e1.getCOG(),e2.getCOG()).calculateLength() >= maxDistance) return 0;
//		System.out.println(maxDistance);
		float dx = e2.getCOG().getX() - e1.getCOG().getX();
		float dy = e2.getCOG().getY() - e1.getCOG().getY();
		
		double yy1 = e1Mat.get(1, 1) * scaleFactor;
		double xx1 = e1Mat.get(0, 0) * scaleFactor;
		double xy1 = e1Mat.get(0, 1) * scaleFactor;
		
		double yy2 = e2Mat.get(1, 1) * scaleFactor;
		double xx2 = e2Mat.get(0, 0) * scaleFactor;
		double xy2 = e2Mat.get(0, 1) * scaleFactor;
		
		double e1Width = Math.sqrt(yy1/(xx1*yy1 - xy1*xy1));
		double e1Height = Math.sqrt(xx1/(xx1*yy1 - xy1*xy1));

		double e2Width = Math.sqrt(yy2/(xx2*yy2 - xy2*xy2));
		double e2Height = Math.sqrt(xx2/(xx2*yy2 - xy2*xy2));

		float maxx=(float) Math.ceil((e1Width>(dx+e2Width))?e1Width:(dx+e2Width));
		float minx=(float) Math.floor((-e1Width<(dx-e2Width))?(-e1Width):(dx-e2Width));
		float maxy=(float) Math.ceil((e1Height>(dy+e2Height))?e1Height:(dy+e2Height));
		float miny=(float) Math.floor((-e1Height<(dy-e2Height))?(-e1Height):(dy-e2Height));

		float mina=(maxx-minx)<(maxy-miny)?(maxx-minx):(maxy-miny);
		float dr=(float) (mina/50.0);
		int bua=0;int bna=0;int t1=0;
		//compute the area
		for(float rx=minx;rx<=maxx;rx+=dr){
			float rx2=rx-dx;t1++;
			for(float ry=miny;ry<=maxy;ry+=dr){
				float ry2=ry-dy;
				//compute the distance from the ellipse center
				float a=(float) (xx1*rx*rx+2*xy1*rx*ry+yy1*ry*ry);
				float b=(float) (xx2*rx2*rx2+2*xy2*rx2*ry2+yy2*ry2*ry2);
				//compute the area
				if(a<1 && b<1)bna++;
				if(a<1 || b<1)bua++;
			}
		}
		return (double)bna/(double)bua;
	}
	
	/**
	 * Read an ellipses from the matlab interest point files
	 * @param inputStream
	 * @return list of ellipses
	 * @throws IOException
	 */
	public static List<Ellipse> readMatlabInterestPoints(InputStream inputStream) throws IOException{
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
		reader.readLine(); // 1.0
		reader.readLine(); // nPoints
		
		String line = "";
		List<Ellipse> ret = new ArrayList<Ellipse>();
		while((line = reader.readLine()) !=null){
			String[] parts = line.split(" ");
			
			float x = Float.parseFloat(parts[0]);
			float y = Float.parseFloat(parts[1]);
			float xx = Float.parseFloat(parts[2]);
			float xy = Float.parseFloat(parts[3]);
			float yy = Float.parseFloat(parts[4]);
			
			Ellipse e = EllipseUtilities.ellipseFromCovariance(x, y, new Matrix(new double[][]{{xx,xy},{xy,yy}}), 1.0f);
			ret.add(e);
		}
		return ret;
	}
	
	public static void main(String args[]) throws IOException{
		testSingleEllipseFromMatlab();
	}
	
	/**
	 * Check the overlap of a single ellipse using covariance numbrers loaded from matlab
	 * @throws IOException 
	 */
	public static void testSingleEllipseFromMatlab() throws IOException{
		Matrix HI = IPDRepeatability.readHomography(IPDRepeatability.class.getResourceAsStream("/org/openimaj/image/feature/validator/graf/H1to2p"));
		Matrix H = HI.inverse();
		
		// Directly from matlab
		Matrix covar1 = new Matrix(new double[][]{{0.164293,-0.057837},{-0.057837,0.052221}});
		Ellipse e1 = EllipseUtilities.ellipseFromCovariance(185.130000f,139.150000f,covar1,1.0f);
		Matrix covar2 = new Matrix(new double[][]{{0.0511181,-0.0364547},{-0.0364547,0.0710733}});
		Ellipse e2 = EllipseUtilities.ellipseFromCovariance(161.051f,239.966f,covar2,1.0f);
		
		doTest(covar1,e1,e2,H);
		
		covar1 = new Matrix(new double[][]{{0.0230756,0.00807592},{0.00807592,0.0569272}});
		e1 = EllipseUtilities.ellipseFromCovariance(441.119f, 481.865f,covar1,1.0f);
		covar2 = new Matrix(new double[][]{{0.047822,0.0188057},{0.0188057,0.0590456}});
		e2 = EllipseUtilities.ellipseFromCovariance(462.656f,486.081f,covar2,1.0f);
		doTest(covar1,e1,e2,H);

//        294.079
//        563.356
//      0.0243268
//      0.0227155
//      0.0692199
		covar1 = new Matrix(new double[][]{{0.010261,0.00305527},{0.00305527,0.0741123}});
		e1 = EllipseUtilities.ellipseFromCovariance(214.359f, 498.872f,covar1,1.0f);
		covar2 = new Matrix(new double[][]{{0.0243268,0.0227155},{0.0227155,0.0692199}});
		e2 = EllipseUtilities.ellipseFromCovariance(294.079f,563.356f,covar2,1.0f);
		doTest(covar1,e1,e2,H);
		
	}

	private static void doTest(Matrix covar1, Ellipse e1, Ellipse e2, Matrix h) {
		Ellipse e2Corrected = e2.transformAffine(h);
		Matrix e2CorrectedCovar = EllipseUtilities.ellipseToCovariance(e2Corrected);
		System.out.println(1-IPDRepeatability.calculateOverlapPercentageOxford(covar1,e2CorrectedCovar,e1,e2Corrected ,4));
	}

	
}
