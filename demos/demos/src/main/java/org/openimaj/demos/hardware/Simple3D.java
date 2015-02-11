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
package org.openimaj.demos.hardware;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.openimaj.image.MBFImage;
import org.openimaj.image.typography.hershey.HersheyFont;
import org.openimaj.math.geometry.point.Point2dImpl;

import Jama.Matrix;

/**
 * Very crude orthographic wireframe renderer
 *
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 */
public class Simple3D {
	/**
	 * Simple interface to describe a primative
	 *
	 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
	 */
	public static interface Primative {
		/**
		 * Render the primative
		 *
		 * @param transform
		 * @param tx
		 * @param ty
		 * @param image
		 */
		public void renderOrtho(Matrix transform, int tx, int ty, MBFImage image);

		/**
		 * Translate the primative
		 *
		 * @param x
		 * @param y
		 * @param z
		 */
		public void translate(int x, int y, int z);
	}

	/**
	 * A 3D point
	 *
	 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
	 *
	 */
	public static class Point3D implements Primative {
		Matrix pt;
		private Float[] colour;
		private int size;

		/**
		 * Construct
		 *
		 * @param x
		 * @param y
		 * @param z
		 * @param colour
		 * @param size
		 */
		public Point3D(double x, double y, double z, Float[] colour, int size) {
			pt = new Matrix(3, 1);
			pt.set(0, 0, x);
			pt.set(1, 0, y);
			pt.set(2, 0, z);
			this.colour = colour;
			this.size = size;
		}

		@Override
		public void renderOrtho(Matrix transform, int tx, int ty, MBFImage image) {
			final Point2dImpl pt1 = projectOrtho(transform.times(pt));
			pt1.x += tx;
			pt1.y += ty;
			pt1.y = image.getHeight() - pt1.y;
			image.drawPoint(pt1, colour, size);
		}

		@Override
		public void translate(int x, int y, int z) {
			pt.set(0, 0, pt.get(0, 0) + x);
			pt.set(1, 0, pt.get(1, 0) + y);
			pt.set(2, 0, pt.get(2, 0) + z);
		}
	}

	/**
	 * 3D Text
	 *
	 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
	 *
	 */
	public static class Text3D implements Primative {
		Matrix pt;
		private Float[] colour;
		private int size;
		private String text;

		/**
		 * Construct
		 *
		 * @param x
		 * @param y
		 * @param z
		 * @param colour
		 * @param size
		 * @param text
		 */
		public Text3D(double x, double y, double z, Float[] colour, int size, String text) {
			pt = new Matrix(3, 1);
			pt.set(0, 0, x);
			pt.set(1, 0, y);
			pt.set(2, 0, z);
			this.colour = colour;
			this.size = size;
			this.text = text;
		}

		@Override
		public void renderOrtho(Matrix transform, int tx, int ty, MBFImage image) {
			final Point2dImpl pt1 = projectOrtho(transform.times(pt));
			pt1.x += tx;
			pt1.y += ty;
			pt1.y = image.getHeight() - pt1.y;
			image.drawText(text, pt1, HersheyFont.ROMAN_DUPLEX, size, colour);
		}

		@Override
		public void translate(int x, int y, int z) {
			pt.set(0, 0, pt.get(0, 0) + x);
			pt.set(1, 0, pt.get(1, 0) + y);
			pt.set(2, 0, pt.get(2, 0) + z);
		}
	}

	/**
	 * 3D line
	 *
	 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
	 *
	 */
	public static class Line3D implements Primative {
		Matrix pt1;
		Matrix pt2;
		private Float[] colour;
		private int thickness;

		/**
		 * Construct
		 *
		 * @param x1
		 * @param y1
		 * @param z1
		 * @param x2
		 * @param y2
		 * @param z2
		 * @param colour
		 * @param size
		 */
		public Line3D(double x1, double y1, double z1, double x2, double y2, double z2, Float[] colour, int size) {
			pt1 = new Matrix(3, 1);
			pt1.set(0, 0, x1);
			pt1.set(1, 0, y1);
			pt1.set(2, 0, z1);
			pt2 = new Matrix(3, 1);
			pt2.set(0, 0, x2);
			pt2.set(1, 0, y2);
			pt2.set(2, 0, z2);
			this.colour = colour;
			this.thickness = size;
		}

		@Override
		public void renderOrtho(Matrix transform, int tx, int ty, MBFImage image) {
			final Point2dImpl p1 = projectOrtho(transform.times(pt1));
			p1.translate(tx, ty);
			p1.y = image.getHeight() - p1.y;

			final Point2dImpl p2 = projectOrtho(transform.times(pt2));
			p2.translate(tx, ty);
			p2.y = image.getHeight() - p2.y;

			image.drawLine(p1, p2, thickness, colour);
		}

		@Override
		public void translate(int x, int y, int z) {
			pt1.set(0, 0, pt1.get(0, 0) + x);
			pt1.set(1, 0, pt1.get(1, 0) + y);
			pt1.set(2, 0, pt1.get(2, 0) + z);
			pt2.set(0, 0, pt2.get(0, 0) + x);
			pt2.set(1, 0, pt2.get(1, 0) + y);
			pt2.set(2, 0, pt2.get(2, 0) + z);
		}
	}

	/**
	 * A scene consisting of primatives
	 *
	 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
	 *
	 */
	public static class Scene {
		List<Primative> primatives = new ArrayList<Primative>();

		/**
		 * Construct
		 */
		public Scene() {
		}

		/**
		 * Construct
		 *
		 * @param primatives
		 */
		public Scene(List<Primative> primatives) {
			this.primatives.addAll(primatives);
		}

		/**
		 * Construct
		 *
		 * @param primatives
		 */
		public Scene(Primative... primatives) {
			this.primatives.addAll(Arrays.asList(primatives));
		}

		/**
		 * Add a primative to the scene
		 *
		 * @param p
		 * @return the scene
		 */
		public Scene addPrimative(Primative p) {
			primatives.add(p);
			return this;
		}

		/**
		 * Render the scene
		 *
		 * @param transform
		 * @param image
		 */
		public void renderOrtho(Matrix transform, MBFImage image) {
			for (final Primative p : primatives)
				p.renderOrtho(transform, image.getWidth() / 2, image.getHeight() / 2, image);
		}

		/**
		 * Translate the scene
		 * 
		 * @param x
		 * @param y
		 * @param z
		 */
		public void translate(int x, int y, int z) {
			for (final Primative p : primatives) {
				p.translate(x, y, z);
			}
		}
	}

	/**
	 * @param pt
	 * @return
	 */
	static Point2dImpl projectOrtho(Matrix pt) {
		final Point2dImpl po = new Point2dImpl();

		po.x = (float) pt.get(0, 0);
		po.y = (float) pt.get(1, 0);

		return po;
	}

	/**
	 * @param pitch
	 * @param yaw
	 * @param roll
	 * @return
	 */
	static Matrix euler2Rot(final double pitch, final double yaw, final double roll)
	{
		Matrix R;
		R = new Matrix(3, 3);

		final double sina = Math.sin(pitch), sinb = Math.sin(yaw), sinc = Math
				.sin(roll);
		final double cosa = Math.cos(pitch), cosb = Math.cos(yaw), cosc = Math
				.cos(roll);
		R.set(0, 0, cosb * cosc);
		R.set(0, 1, -cosb * sinc);
		R.set(0, 2, sinb);
		R.set(1, 0, cosa * sinc + sina * sinb * cosc);
		R.set(1, 1, cosa * cosc - sina * sinb * sinc);
		R.set(1, 2, -sina * cosb);
		R.set(2, 0, R.get(0, 1) * R.get(1, 2) - R.get(0, 2) * R.get(1, 1));
		R.set(2, 1, R.get(0, 2) * R.get(1, 0) - R.get(0, 0) * R.get(1, 2));
		R.set(2, 2, R.get(0, 0) * R.get(1, 1) - R.get(0, 1) * R.get(1, 0));

		return R;
	}
}
