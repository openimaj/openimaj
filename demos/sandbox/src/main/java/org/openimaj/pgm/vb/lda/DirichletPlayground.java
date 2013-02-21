package org.openimaj.pgm.vb.lda;

import gov.sandia.cognition.math.matrix.mtj.DenseVector;
import gov.sandia.cognition.math.matrix.mtj.DenseVectorFactoryMTJ;
import gov.sandia.cognition.statistics.distribution.DirichletDistribution;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.math.geometry.line.Line2d;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.math.geometry.shape.Triangle;


public class DirichletPlayground {
	private double eta = 0.00001f;
	private DirichletDistribution dir;

	public DirichletPlayground() {

	}

	public void render(double alpha) {
		this.dir = new DirichletDistribution(3);
		DenseVector params = new DenseVectorFactoryMTJ().copyArray(new double[]{alpha,alpha,alpha});
		dir.convertFromVector(params);

		MBFImage out = new MBFImage(400,400,3);
		Point2dImpl p1 = new Point2dImpl(out.getWidth()/2f,0f);
		Point2dImpl p2 = new Point2dImpl(out.getWidth(),out.getHeight());
		Point2dImpl p3 = new Point2dImpl(0,out.getHeight());
		Triangle simplex = new Triangle(
			p1,
			p2,
			p3
		);
		out.fill(RGBColour.WHITE);
		out.drawShape(simplex, 3, RGBColour.BLACK);
//		DenseVector simplexPoint = new DenseVectorFactoryMTJ().copyArray(new double[]{1/3f,1/3f ,1/3f});
		double max = 10;
		for (int y = 0; y < out.getWidth(); y++) {
			for (int x = 0; x < out.getHeight(); x++) {
				if(simplex.isInside(new Point2dImpl(x,y))){
					double p1d = Line2d.distance(x, y, p1.x, p1.y) + eta;
					double p2d = Line2d.distance(x, y, p2.x, p2.y) + eta;
					double p3d = Line2d.distance(x, y, p3.x, p3.y) + eta;
					double sum = p1d + p2d + p3d;
					p1d /= sum;
					p2d /= sum;
					p3d /= sum;
					DenseVector v = new DenseVectorFactoryMTJ().copyArray(new double[]{p1d,p2d,p3d});
					double val = Math.min(max, dir.getProbabilityFunction().evaluate(v)) / max;
					out.setPixel(x, y, blend(RGBColour.BLACK,RGBColour.RED,(float) val));
				}
			}
		}
		DisplayUtilities.display(out);
	}

	private Float[] blend(Float[] black, Float[] red, float val) {
		return new Float[]{
				((black[0] * (1 - val)) + (red[0] * (val))),
				((black[1] * (1 - val)) + (red[1] * (val))),
				((black[2] * (1 - val)) + (red[2] * (val))),
		};
	}

	public static void main(String[] args) {
		DirichletPlayground pg = new DirichletPlayground();
		pg.render(0.3f);
	}
}
