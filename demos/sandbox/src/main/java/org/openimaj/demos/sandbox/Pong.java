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
	
	private Rectangle borderTop;
	private Rectangle borderBottom;
	
	protected Circle paddleLeft;
	protected Circle paddleRight;
	private Point2dImpl paddleLeftPoint;
	private Point2dImpl paddleRightPoint;
	private Circle ball;
	private Point2dImpl ballCentre;
	
	private MBFImage lastFrame;
	private MBFImage frame;
	private MBFImageRenderer renderer;
	
	private Point2dImpl ballVelocity;
	
	private int scoreLeft = 0;
	private int scoreRight = 0;
	private int frame_height;
	private int frame_width;
	private HashMap<Attribute, Object> redText;
	private HashMap<Attribute, Object> blueText;
	private HashMap<Attribute, Object> allFont;
	
	
	public Pong(int width, int height) {
		
		redText = new HashMap<Attribute, Object>();
		redText.put(FontStyle.COLOUR, RGBColour.RED);
		
		blueText = new HashMap<Attribute, Object>();
		blueText.put(FontStyle.COLOUR, RGBColour.BLUE);
		
		allFont = new HashMap<Attribute, Object>();
		allFont.put(FontStyle.FONT, HersheyFont.ROMAN_SIMPLEX);
		allFont.put(FontStyle.FONT_SIZE, 40);
		
		this.frame_height = height;
		this.frame_width = width;
		lastFrame = new MBFImage(width, height,ColourSpace.RGB);
		frame = new MBFImage(width, height,ColourSpace.RGB);
		renderer = frame.createRenderer();
		
		borderTop = new Rectangle(0, 0, width, height*BORDER_TOP);
		borderBottom = new Rectangle(0, height*(1-BORDER_BOTTOM), width, height*BORDER_BOTTOM);
		
		initMatch();
		
		getNextFrame();
	}
	
	private void initMatch() {
		scoreLeft = 0;
		scoreRight = 0;
		
		initGame();
	}
	
	private void initGame() {
		lastFrame.fill(RGBColour.BLACK);
		frame.fill(RGBColour.BLACK);
		Random r = new Random();
		paddleLeftPoint = new Point2dImpl(0, 0.5f*frame_height);
		paddleLeft = new Circle(paddleLeftPoint, PADDLE_RADIUS*frame_height);
		paddleRightPoint = new Point2dImpl(frame_width, 0.5f*frame_height);
		paddleRight = new Circle(paddleRightPoint , PADDLE_RADIUS*frame_height);
		
		ballCentre = new Point2dImpl(0.5f*frame_width, 0.5f*frame_height);
		ball = new Circle(ballCentre, BALL_SIZE*frame_width);
		
		ballVelocity = new Point2dImpl();
		
//		float vx = (float) (Math.random() - 0.5);
//		float vy = (float) (Math.random() - 0.5);
//		
//		if (0.5 * vy > vx) {
//			float tmp = vx;
//			vx = vy;
//			vy = tmp;
//		}
//		
//		float mult = (float) (INITIAL_VELOCITY / Math.sqrt(vx*vx + vy*vy));
//		vx*=mult;
//		vy*=mult;
		float vx = r.nextBoolean() ? 1f : -1f;
		float vy = r.nextFloat() * 2 + -1f;
		float mult = (float) (INITIAL_VELOCITY / Math.sqrt(vx*vx + vy*vy));
		vx*=mult;
		vy*=mult;
		ballVelocity = new Point2dImpl(vx, vy);
	}

	@Override
	public MBFImage getNextFrame() {
		//draw scene
		updateBall();
		
		frame.fill(RGBColour.BLACK);
		renderer.drawShapeFilled(borderTop, RGBColour.GRAY);
		renderer.drawShapeFilled(borderBottom, RGBColour.GRAY);
		
		renderer.drawShapeFilled(paddleLeft, RGBColour.RED);
		renderer.drawShapeFilled(paddleRight, RGBColour.BLUE);
		
		renderer.drawShapeFilled(ball, RGBColour.WHITE);
//		if(contactPoint!=null) renderer.drawPoint(contactPoint, 1.0f, 10);
		int scorLeftLength = new String("" + scoreLeft).length();
		int scorRightLength = new String("" + scoreRight).length();
		int sepLength = new String(" : ").length();
		String allString = scoreLeft + " : " + scoreRight;
		AttributedString str = new AttributedString(allString );
		str.addAttributes(this.allFont, 0, allString.length());
		str.addAttributes(this.redText, 0, scorLeftLength);
		str.addAttributes(this.blueText, scorLeftLength + sepLength, scorLeftLength + sepLength + scorRightLength);
		renderer.drawText(str, frame_width/2, 40);
		
		lastFrame.drawImage(frame, 0, 0);	
		return frame;
	}

	private void updateBall() {
		float newX = ballCentre.x + ballVelocity.x;
		float newY = ballCentre.y + ballVelocity.y;
		
		
		
		if (newX < 0) {
			initGame();
			scoreRight++;
			return;
		}
		
		if (newX > frame_width) {
			initGame();
			scoreLeft++;
			return;
		}
		
		if (newY < frame_height*(BORDER_TOP + BALL_SIZE)) {
			newY = 2*frame_height*(BORDER_TOP + BALL_SIZE) - newY;
			ballVelocity.y = -ballVelocity.y;
		}
		
		if (newY > frame_height*(1 - BORDER_BOTTOM - BALL_SIZE)) {
			newY = 2*frame_height*(1 - BORDER_BOTTOM - BALL_SIZE) - newY;
			ballVelocity.y = -ballVelocity.y;
		}
		
		double dLeftPaddle = Line2d.distance(paddleLeftPoint, new Point2dImpl(newX,newY));
		double dRightPaddle = Line2d.distance(paddleRightPoint, new Point2dImpl(newX,newY));
		
		if (dLeftPaddle <= paddleLeft.getRadius() + this.ball.getRadius() ) {
			
//			contactPoint = contactPoint(paddleLeft,this.ball);
			Point2dImpl contactDelta = normContactDelta(paddleLeft,this.ball);
			
			newX = paddleLeftPoint.x + contactDelta.x * ((PADDLE_RADIUS + BALL_SIZE) * this.frame_height);
			newY = paddleLeftPoint.y + contactDelta.y * ((PADDLE_RADIUS + BALL_SIZE) * this.frame_height);
			
			double rotation = -Math.atan2(contactDelta.y, contactDelta.x);
			Matrix trans = TransformUtilities.rotationMatrix(rotation);
			Point2dImpl newVel = ballVelocity.transform(trans);
			newVel.x *= -1;
			newVel.transform(trans.inverse());
			ballVelocity = newVel;
			
//			newX += ballVelocity.x;
			
			ballVelocity.x *= SPEED_MULTIPLIER;
			ballVelocity.y *= SPEED_MULTIPLIER;
		}
		
		if (dRightPaddle <= paddleRight.getRadius() + this.ball.getRadius()) {
//			contactPoint = contactPoint(paddleRight,this.ball);
			Point2dImpl contactDelta = normContactDelta(paddleRight,this.ball);
			
			newX = paddleRightPoint.x + contactDelta.x * ((PADDLE_RADIUS + BALL_SIZE) * this.frame_height);
			newY = paddleRightPoint.y + contactDelta.y * ((PADDLE_RADIUS + BALL_SIZE) * this.frame_height);
			double rotation = Math.atan2(contactDelta.y, -contactDelta.x);
			
			Matrix trans = TransformUtilities.rotationMatrix(rotation);
			Point2dImpl newVel = ballVelocity.transform(trans);
			newVel.x *= -1;
			newVel.transform(trans.inverse());
			ballVelocity = newVel;
			
//			newX += ballVelocity.x;
			
			ballVelocity.x *= SPEED_MULTIPLIER;
			ballVelocity.y *= SPEED_MULTIPLIER;
		}
		
		ballCentre.x = newX;
		ballCentre.y = newY;
	}

	private Point2dImpl contactPoint(Circle paddle, Circle ball) {
		float px = paddle.getX();
		float py = paddle.getY();
		Point2dImpl contactDelta = normContactDelta(paddle,ball);
		return new Point2dImpl(px+contactDelta.x,py+contactDelta.y);
	}

	private Point2dImpl normContactDelta(Circle paddle, Circle ball) {
		float px = paddle.getX();
		float py = paddle.getY();
		float dx = ball.getX() - px;
		float dy = ball.getY() - py;
		float plusX = dx * (paddle.getRadius()/(ball.getRadius() + paddle.getRadius()));
		float plusY = dy * (paddle.getRadius()/(ball.getRadius() + paddle.getRadius()));
		
		float prop = (float) (1f / Math.sqrt(plusX*plusX + plusY*plusY));
		return new Point2dImpl(plusX*prop,plusY*prop);
	}

	@Override
	public MBFImage getCurrentFrame() {
		return lastFrame;
	}

	@Override
	public int getWidth() {
		return frame_width;
	}

	@Override
	public int getHeight() {
		return (int) frame_height;
	}

	@Override
	public long getTimeStamp() {
		return System.currentTimeMillis();
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
		return 1;
	}

	@Override
	public void reset() {
		initMatch();
		getNextFrame();
	}
	
	
	public void leftPaddle(float paddleY) {
		paddleLeftPoint.y = paddleY;
		
		if (paddleLeftPoint.y > frame_height * (1-BORDER_BOTTOM-PADDLE_RADIUS)) paddleLeftPoint.y = frame_height * (1-BORDER_BOTTOM-PADDLE_RADIUS);
		if (paddleLeftPoint.y < (BORDER_TOP+PADDLE_RADIUS)*frame_height) paddleLeftPoint.y = (BORDER_TOP+PADDLE_RADIUS)*frame_height;
		
	}
	
	protected void rightPaddle(float paddleY) {
		paddleRightPoint.y = paddleY;
		
		if (paddleRightPoint.y < (BORDER_TOP+PADDLE_RADIUS)*frame_height) paddleRightPoint.y = (BORDER_TOP+PADDLE_RADIUS)*frame_height;
		if (paddleRightPoint.y > frame_height * (1-BORDER_BOTTOM-PADDLE_RADIUS)) paddleRightPoint.y = frame_height * (1-BORDER_BOTTOM-PADDLE_RADIUS);
	}
	
	public void leftPaddleUp() {
		leftPaddle(paddleLeftPoint.y - PADDLE_VELOCITY*frame_height);
	}
	
	public void leftPaddleDown() {
		leftPaddle(paddleLeftPoint.y + PADDLE_VELOCITY*frame_height);
	}
	
	protected void rightPaddleUp() {
		rightPaddle(paddleRightPoint.y - PADDLE_VELOCITY*frame_height);
	}
	
	protected void rightPaddleDown() {
		rightPaddle(paddleRightPoint.y + PADDLE_VELOCITY*frame_height);
	}
	
	public static void main(String[] args) {
		final Pong p = new Pong(640, 480);
		VideoDisplay<MBFImage> display = VideoDisplay.createVideoDisplay(p);
		((JFrame)SwingUtilities.getRoot(display.getScreen())).setTitle("OpenIMAJ Pong!");
		
		SwingUtilities.getRoot(display.getScreen()).addKeyListener(new KeyListener() {

			@Override
			public void keyTyped(KeyEvent e) {}

			@Override
			public void keyPressed(KeyEvent e) {
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
			public void keyReleased(KeyEvent e) {}
			
		});
	}

	public float leftPaddleY() {
		return paddleLeftPoint.y;
	}
	
	public float rightPaddleY() {
		return paddleRightPoint.y;
	}
}
