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
package org.openimaj.demos.sandbox;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.text.AttributedCharacterIterator.Attribute;
import java.text.AttributedString;
import java.util.HashMap;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.ColourSpace;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.renderer.MBFImageRenderer;
import org.openimaj.image.typography.FontStyle;
import org.openimaj.image.typography.hershey.HersheyFont;
import org.openimaj.math.geometry.line.Line2d;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.math.geometry.shape.Circle;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.math.geometry.transforms.TransformUtilities;
import org.openimaj.video.Video;
import org.openimaj.video.VideoDisplay;

import Jama.Matrix;

/**
 * A rather simple implementation of Pong
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 */
public class Pong extends Video<MBFImage> {
	private static final float PADDLE_RADIUS = 0.07f;
	private static final float BALL_SIZE = 0.02f;
	private static final float PADDLE_VELOCITY = 0.01f;

	private static final float BORDER_TOP = 0.1f;
	private static final float BORDER_BOTTOM = 0.05f;

	private static final float SPEED_MULTIPLIER = 1.2f;
	private static final float INITIAL_VELOCITY = 5.0f;

	private final Rectangle borderTop;
	private final Rectangle borderBottom;

	protected Circle paddleLeft;
	protected Circle paddleRight;
	private Point2dImpl paddleLeftPoint;
	private Point2dImpl paddleRightPoint;
	private Circle ball;
	private Point2dImpl ballCentre;

	private final MBFImage lastFrame;
	private final MBFImage frame;
	private final MBFImageRenderer renderer;

	private Point2dImpl ballVelocity;

	private int scoreLeft = 0;
	private int scoreRight = 0;
	private final int frame_height;
	private final int frame_width;
	private final HashMap<Attribute, Object> redText;
	private final HashMap<Attribute, Object> blueText;
	private final HashMap<Attribute, Object> allFont;

	public Pong(final int width, final int height) {

		this.redText = new HashMap<Attribute, Object>();
		this.redText.put(FontStyle.COLOUR, RGBColour.RED);

		this.blueText = new HashMap<Attribute, Object>();
		this.blueText.put(FontStyle.COLOUR, RGBColour.BLUE);

		this.allFont = new HashMap<Attribute, Object>();
		this.allFont.put(FontStyle.FONT, HersheyFont.ROMAN_SIMPLEX);
		this.allFont.put(FontStyle.FONT_SIZE, 40);

		this.frame_height = height;
		this.frame_width = width;
		this.lastFrame = new MBFImage(width, height, ColourSpace.RGB);
		this.frame = new MBFImage(width, height, ColourSpace.RGB);
		this.renderer = this.frame.createRenderer();

		this.borderTop = new Rectangle(0, 0, width, height * Pong.BORDER_TOP);
		this.borderBottom = new Rectangle(0, height * (1 - Pong.BORDER_BOTTOM), width, height * Pong.BORDER_BOTTOM);

		this.initMatch();

		this.getNextFrame();
	}

	private void initMatch() {
		this.scoreLeft = 0;
		this.scoreRight = 0;

		this.initGame();
	}

	private void initGame() {
		this.lastFrame.fill(RGBColour.BLACK);
		this.frame.fill(RGBColour.BLACK);
		final Random r = new Random();
		this.paddleLeftPoint = new Point2dImpl(0, 0.5f * this.frame_height);
		this.paddleLeft = new Circle(this.paddleLeftPoint, Pong.PADDLE_RADIUS * this.frame_height);
		this.paddleRightPoint = new Point2dImpl(this.frame_width, 0.5f * this.frame_height);
		this.paddleRight = new Circle(this.paddleRightPoint, Pong.PADDLE_RADIUS * this.frame_height);

		this.ballCentre = new Point2dImpl(0.5f * this.frame_width, 0.5f * this.frame_height);
		this.ball = new Circle(this.ballCentre, Pong.BALL_SIZE * this.frame_width);

		this.ballVelocity = new Point2dImpl();

		// float vx = (float) (Math.random() - 0.5);
		// float vy = (float) (Math.random() - 0.5);
		//
		// if (0.5 * vy > vx) {
		// float tmp = vx;
		// vx = vy;
		// vy = tmp;
		// }
		//
		// float mult = (float) (INITIAL_VELOCITY / Math.sqrt(vx*vx + vy*vy));
		// vx*=mult;
		// vy*=mult;
		float vx = r.nextBoolean() ? 1f : -1f;
		float vy = r.nextFloat() * 2 + -1f;
		final float mult = (float) (Pong.INITIAL_VELOCITY / Math.sqrt(vx * vx + vy * vy));
		vx *= mult;
		vy *= mult;
		this.ballVelocity = new Point2dImpl(vx, vy);
	}

	@Override
	public MBFImage getNextFrame() {
		// draw scene
		this.updateBall();

		this.frame.fill(RGBColour.BLACK);
		this.renderer.drawShapeFilled(this.borderTop, RGBColour.GRAY);
		this.renderer.drawShapeFilled(this.borderBottom, RGBColour.GRAY);

		this.renderer.drawShapeFilled(this.paddleLeft, RGBColour.RED);
		this.renderer.drawShapeFilled(this.paddleRight, RGBColour.BLUE);

		this.renderer.drawShapeFilled(this.ball, RGBColour.WHITE);
		// if(contactPoint!=null) renderer.drawPoint(contactPoint, 1.0f, 10);
		final int scorLeftLength = new String("" + this.scoreLeft).length();
		final int scorRightLength = new String("" + this.scoreRight).length();
		final int sepLength = new String(" : ").length();
		final String allString = this.scoreLeft + " : " + this.scoreRight;
		final AttributedString str = new AttributedString(allString);
		str.addAttributes(this.allFont, 0, allString.length());
		str.addAttributes(this.redText, 0, scorLeftLength);
		str.addAttributes(this.blueText, scorLeftLength + sepLength, scorLeftLength + sepLength + scorRightLength);
		this.renderer.drawText(str, this.frame_width / 2, 40);

		this.lastFrame.drawImage(this.frame, 0, 0);

		this.currentFrame++;

		return this.frame;
	}

	private void updateBall() {
		float newX = this.ballCentre.x + this.ballVelocity.x;
		float newY = this.ballCentre.y + this.ballVelocity.y;

		if (newX < 0) {
			this.initGame();
			this.scoreRight++;
			return;
		}

		if (newX > this.frame_width) {
			this.initGame();
			this.scoreLeft++;
			return;
		}

		if (newY < this.frame_height * (Pong.BORDER_TOP + Pong.BALL_SIZE)) {
			newY = 2 * this.frame_height * (Pong.BORDER_TOP + Pong.BALL_SIZE) - newY;
			this.ballVelocity.y = -this.ballVelocity.y;
		}

		if (newY > this.frame_height * (1 - Pong.BORDER_BOTTOM - Pong.BALL_SIZE)) {
			newY = 2 * this.frame_height * (1 - Pong.BORDER_BOTTOM - Pong.BALL_SIZE) - newY;
			this.ballVelocity.y = -this.ballVelocity.y;
		}

		final double dLeftPaddle = Line2d.distance(this.paddleLeftPoint, new Point2dImpl(newX, newY));
		final double dRightPaddle = Line2d.distance(this.paddleRightPoint, new Point2dImpl(newX, newY));

		if (dLeftPaddle <= this.paddleLeft.getRadius() + this.ball.getRadius()) {

			// contactPoint = contactPoint(paddleLeft,this.ball);
			final Point2dImpl contactDelta = this.normContactDelta(this.paddleLeft, this.ball);

			newX = this.paddleLeftPoint.x + contactDelta.x * ((Pong.PADDLE_RADIUS + Pong.BALL_SIZE) * this.frame_height);
			newY = this.paddleLeftPoint.y + contactDelta.y * ((Pong.PADDLE_RADIUS + Pong.BALL_SIZE) * this.frame_height);

			final double rotation = -Math.atan2(contactDelta.y, contactDelta.x);
			final Matrix trans = TransformUtilities.rotationMatrix(rotation);
			final Point2dImpl newVel = this.ballVelocity.transform(trans);
			newVel.x *= -1;
			newVel.transform(trans.inverse());
			this.ballVelocity = newVel;

			// newX += ballVelocity.x;

			this.ballVelocity.x *= Pong.SPEED_MULTIPLIER;
			this.ballVelocity.y *= Pong.SPEED_MULTIPLIER;
		}

		if (dRightPaddle <= this.paddleRight.getRadius() + this.ball.getRadius()) {
			// contactPoint = contactPoint(paddleRight,this.ball);
			final Point2dImpl contactDelta = this.normContactDelta(this.paddleRight, this.ball);

			newX = this.paddleRightPoint.x + contactDelta.x * ((Pong.PADDLE_RADIUS + Pong.BALL_SIZE) * this.frame_height);
			newY = this.paddleRightPoint.y + contactDelta.y * ((Pong.PADDLE_RADIUS + Pong.BALL_SIZE) * this.frame_height);
			final double rotation = Math.atan2(contactDelta.y, -contactDelta.x);

			final Matrix trans = TransformUtilities.rotationMatrix(rotation);
			final Point2dImpl newVel = this.ballVelocity.transform(trans);
			newVel.x *= -1;
			newVel.transform(trans.inverse());
			this.ballVelocity = newVel;

			// newX += ballVelocity.x;

			this.ballVelocity.x *= Pong.SPEED_MULTIPLIER;
			this.ballVelocity.y *= Pong.SPEED_MULTIPLIER;
		}

		this.ballCentre.x = newX;
		this.ballCentre.y = newY;
	}

	// private Point2dImpl contactPoint(final Circle paddle, final Circle ball)
	// {
	// final float px = paddle.getX();
	// final float py = paddle.getY();
	// final Point2dImpl contactDelta = this.normContactDelta(paddle,ball);
	// return new Point2dImpl(px+contactDelta.x,py+contactDelta.y);
	// }

	private Point2dImpl normContactDelta(final Circle paddle, final Circle ball) {
		final float px = paddle.getX();
		final float py = paddle.getY();
		final float dx = ball.getX() - px;
		final float dy = ball.getY() - py;
		final float plusX = dx * (paddle.getRadius() / (ball.getRadius() + paddle.getRadius()));
		final float plusY = dy * (paddle.getRadius() / (ball.getRadius() + paddle.getRadius()));

		final float prop = (float) (1f / Math.sqrt(plusX * plusX + plusY * plusY));
		return new Point2dImpl(plusX * prop, plusY * prop);
	}

	@Override
	public MBFImage getCurrentFrame() {
		return this.lastFrame;
	}

	@Override
	public int getWidth() {
		return this.frame_width;
	}

	@Override
	public int getHeight() {
		return this.frame_height;
	}

	@Override
	public long getTimeStamp() {
		return (long) (this.currentFrame * 1000d / this.getFPS());
	}

	@Override
	public double getFPS() {
		return 30;
	}

	@Override
	public boolean hasNextFrame() {
		return true;
	}

	@Override
	public long countFrames() {
		return -1;
	}

	@Override
	public void reset() {
		this.initMatch();
		this.getNextFrame();
	}

	public void leftPaddle(final float paddleY) {
		this.paddleLeftPoint.y = paddleY;

		if (this.paddleLeftPoint.y > this.frame_height * (1 - Pong.BORDER_BOTTOM - Pong.PADDLE_RADIUS))
			this.paddleLeftPoint.y = this.frame_height * (1 - Pong.BORDER_BOTTOM - Pong.PADDLE_RADIUS);
		if (this.paddleLeftPoint.y < (Pong.BORDER_TOP + Pong.PADDLE_RADIUS) * this.frame_height)
			this.paddleLeftPoint.y = (Pong.BORDER_TOP + Pong.PADDLE_RADIUS) * this.frame_height;

	}

	protected void rightPaddle(final float paddleY) {
		this.paddleRightPoint.y = paddleY;

		if (this.paddleRightPoint.y < (Pong.BORDER_TOP + Pong.PADDLE_RADIUS) * this.frame_height)
			this.paddleRightPoint.y = (Pong.BORDER_TOP + Pong.PADDLE_RADIUS) * this.frame_height;
		if (this.paddleRightPoint.y > this.frame_height * (1 - Pong.BORDER_BOTTOM - Pong.PADDLE_RADIUS))
			this.paddleRightPoint.y = this.frame_height * (1 - Pong.BORDER_BOTTOM - Pong.PADDLE_RADIUS);
	}

	public void leftPaddleUp() {
		this.leftPaddle(this.paddleLeftPoint.y - Pong.PADDLE_VELOCITY * this.frame_height);
	}

	public void leftPaddleDown() {
		this.leftPaddle(this.paddleLeftPoint.y + Pong.PADDLE_VELOCITY * this.frame_height);
	}

	protected void rightPaddleUp() {
		this.rightPaddle(this.paddleRightPoint.y - Pong.PADDLE_VELOCITY * this.frame_height);
	}

	protected void rightPaddleDown() {
		this.rightPaddle(this.paddleRightPoint.y + Pong.PADDLE_VELOCITY * this.frame_height);
	}

	public static void main(final String[] args) {
		final Pong p = new Pong(640, 480);
		final VideoDisplay<MBFImage> display = VideoDisplay.createVideoDisplay(p);
		((JFrame) SwingUtilities.getRoot(display.getScreen())).setTitle("OpenIMAJ Pong!");

		SwingUtilities.getRoot(display.getScreen()).addKeyListener(new KeyListener() {

			@Override
			public void keyTyped(final KeyEvent e) {
			}

			@Override
			public void keyPressed(final KeyEvent e) {
				switch (e.getKeyChar()) {
				case 'a':
					p.leftPaddleUp();
					break;
				case 'z':
					p.leftPaddleDown();
					break;
				case '\'':
					p.rightPaddleUp();
					break;
				case '/':
					p.rightPaddleDown();
					break;
				case 'r':
					p.reset();
					break;
				}
			}

			@Override
			public void keyReleased(final KeyEvent e) {
			}

		});
	}

	public float leftPaddleY() {
		return this.paddleLeftPoint.y;
	}

	public float rightPaddleY() {
		return this.paddleRightPoint.y;
	}
}
