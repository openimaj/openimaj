package org.openimaj.demos.sandbox;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.openimaj.image.FImage;
import org.openimaj.image.renderer.FImageRenderer;
import org.openimaj.image.typography.FontStyle.HorizontalAlignment;
import org.openimaj.image.typography.hershey.HersheyFont;
import org.openimaj.image.typography.hershey.HersheyFontStyle;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.math.geometry.shape.Circle;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.video.Video;
import org.openimaj.video.VideoDisplay;

public class Pong extends Video<FImage> {
	private static final float PADDLE_OFFSET_X = 0.00f;
	private static final float PADDLE_HEIGHT = 0.15f;
	private static final float PADDLE_WIDTH = 0.01f;
	private static final float BALL_SIZE = 0.02f;
	private static final float PADDLE_VELOCITY = 0.01f;
	
	private static final float BORDER_TOP = 0.1f;
	private static final float BORDER_BOTTOM = 0.05f;
	
	private Rectangle borderTop;
	private Rectangle borderBottom;
	
	private Rectangle paddleLeft;
	private Rectangle paddleRight;
	private Circle ball;
	private Point2dImpl ballCentre;
	
	private FImage lastFrame;
	private FImage frame;
	private FImageRenderer renderer;
	
	private Point2dImpl ballVelocity;
	
	private int scoreLeft = 0;
	private int scoreRight = 0;
	
	public Pong(int width, int height) {
		lastFrame = new FImage(width, height);
		frame = new FImage(width, height);
		renderer = frame.createRenderer();
		
		borderTop = new Rectangle(0, 0, frame.width, frame.height*BORDER_TOP);
		borderBottom = new Rectangle(0, frame.height*(1-BORDER_BOTTOM), frame.width, frame.height*BORDER_BOTTOM);
		
		initMatch();
		
		getNextFrame();
	}
	
	private void initMatch() {
		scoreLeft = 0;
		scoreRight = 0;
		
		initGame();
	}
	
	private void initGame() {
		lastFrame.fill(0f);
		frame.fill(0f);
				
		paddleLeft = new Rectangle(PADDLE_OFFSET_X*frame.width, (0.5f - PADDLE_HEIGHT/2)*frame.height, PADDLE_WIDTH*frame.width, PADDLE_HEIGHT*frame.height);
		paddleRight = new Rectangle((1f-PADDLE_OFFSET_X-PADDLE_WIDTH)*frame.width, (0.5f - PADDLE_HEIGHT/2)*frame.height, PADDLE_WIDTH*frame.width, PADDLE_HEIGHT*frame.height);
		
		ballCentre = new Point2dImpl(0.5f*frame.width, 0.5f*frame.height);
		ball = new Circle(ballCentre, BALL_SIZE*frame.width);
		
		ballVelocity = new Point2dImpl();
		while (Math.abs(ballVelocity.y) < 0.1 && Math.abs(ballVelocity.x) < 0.5) {
			ballVelocity = new Point2dImpl((float)(Math.random()-0.5)*2, (float)(Math.random()-0.5));
		}
	}

	@Override
	public FImage getNextFrame() {
		//draw scene
		updateBall();
		
		frame.fill(0f);
		renderer.drawShapeFilled(borderTop, 0.5f);
		renderer.drawShapeFilled(borderBottom, 0.5f);
		
		renderer.drawShapeFilled(paddleLeft, 1f);
		renderer.drawShapeFilled(paddleRight, 1f);
		
		renderer.drawShapeFilled(ball, 1f);
		
		HersheyFontStyle<Float> style = HersheyFont.ROMAN_SIMPLEX.createStyle(renderer);
		style.setHorizontalAlignment(HorizontalAlignment.HORIZONTAL_CENTER);
		style.setFontSize(40);
		renderer.drawText(scoreLeft + " : " + scoreRight, frame.width/2, 40, style);
		
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
		
		if (newX > frame.width) {
			initGame();
			scoreLeft++;
			return;
		}
		
		if (newY < frame.height*(BORDER_TOP + BALL_SIZE)) {
			newY = 2*frame.height*(BORDER_TOP + BALL_SIZE) - newY;
			ballVelocity.y = -ballVelocity.y;
		}
		
		if (newY > frame.height*(1 - BORDER_BOTTOM - BALL_SIZE)) {
			newY = 2*frame.height*(1 - BORDER_BOTTOM - BALL_SIZE) - newY;
			ballVelocity.y = -ballVelocity.y;
		}
		
		if (newX < (PADDLE_WIDTH + BALL_SIZE)*frame.width &&
				newY > paddleLeft.y && 
				newY < paddleLeft.y + paddleLeft.height ) {
			newX = 2*(PADDLE_WIDTH + BALL_SIZE)*frame.width - newX;
			ballVelocity.x = -ballVelocity.x;
		}
		
		if (newX > (1 - (PADDLE_WIDTH + BALL_SIZE))*frame.width &&
				newY > paddleRight.y && 
				newY < paddleRight.y + paddleRight.height ) {
			newX = 2*(1 - PADDLE_WIDTH - BALL_SIZE)*frame.width - newX;
			ballVelocity.x = -ballVelocity.x;
		}
		
		ballCentre.x = newX;
		ballCentre.y = newY;
	}

	@Override
	public FImage getCurrentFrame() {
		return lastFrame;
	}

	@Override
	public int getWidth() {
		return frame.width;
	}

	@Override
	public int getHeight() {
		return frame.height;
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

	protected void leftPaddleUp() {
		paddleLeft.y -= PADDLE_VELOCITY*frame.height;
		
		if (paddleLeft.y < BORDER_TOP*frame.height) paddleLeft.y = BORDER_TOP*frame.height;
	}
	
	protected void leftPaddleDown() {
		paddleLeft.y += PADDLE_VELOCITY*frame.height;
		
		if (paddleLeft.y > frame.height * (1-BORDER_BOTTOM-PADDLE_HEIGHT)) paddleLeft.y = frame.height * (1-BORDER_BOTTOM-PADDLE_HEIGHT);
	}
	
	protected void rightPaddleUp() {
		paddleRight.y -= PADDLE_VELOCITY*frame.height;
		
		if (paddleRight.y < BORDER_TOP*frame.height) paddleRight.y = BORDER_TOP*frame.height;
	}
	
	protected void rightPaddleDown() {
		paddleRight.y += PADDLE_VELOCITY*frame.height;
		
		if (paddleRight.y > frame.height * (1-BORDER_BOTTOM-PADDLE_HEIGHT)) paddleRight.y = frame.height * (1-BORDER_BOTTOM-PADDLE_HEIGHT);
	}
	
	public static void main(String[] args) {
		final Pong p = new Pong(640, 480);
		VideoDisplay<FImage> display = VideoDisplay.createVideoDisplay(p);
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
}
