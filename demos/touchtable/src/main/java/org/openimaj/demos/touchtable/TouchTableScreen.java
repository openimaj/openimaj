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
package org.openimaj.demos.touchtable;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JFrame;

import org.openimaj.demos.sandbox.Pong;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.ColourSpace;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.math.geometry.line.Line2d;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.math.geometry.shape.Circle;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.math.geometry.transforms.HomographyModel;
import org.openimaj.util.pair.IndependentPair;

import com.lowagie.text.pdf.codec.Base64;


public class TouchTableScreen extends JFrame implements Runnable {

	/**
	 * A touchtable full screen jframe
	 */
	private static final long serialVersionUID = -966931575089952536L;
	private MBFImage image;
	Mode mode;
	public CameraConfig cameraConfig;
	private Rectangle inputArea;
	private Rectangle visibleArea;
	private boolean renderMode = true;
	private boolean clear = false;;
	
	interface Mode{
		public class PONG extends Pong implements Mode{
			private TouchTableScreen touchScreen;

			public PONG(TouchTableScreen touchScreen) {
				super((int)touchScreen.visibleArea.width, (int)touchScreen.visibleArea.height);
				this.touchScreen = touchScreen;
				reset();
			}
			
			@Override
			public void acceptTouch(List<Touch> filtered) {
				for (Touch tableTouch : filtered) {
					Touch touch = this.touchScreen.cameraConfig.transformTouch(tableTouch);
					if(touch==null)continue;
//					System.out.println(touch);
//					System.out.println(this.getWidth()/2);
//					goToFinger(touch);
					followFinger(touch);
				}
			}

			private void followFinger(Touch touch) {
				if(touch.intersectionArea(this.paddleLeft) > 0){
					this.leftPaddle(touch.getY());
				}
				else if(touch.intersectionArea(this.paddleRight) > 0){
					this.rightPaddle(touch.getY());
				}
			}

			private void goToFinger(Touch touch) {
				if(touch.getX() < this.getWidth()/2) // left paddle
				{
					if(touch.getY() < this.leftPaddleY()){
						this.leftPaddleUp();
					}
					else{
						this.leftPaddleDown();
					}
				}
				else{ // right paddle
					if(touch.getY() < this.rightPaddleY()){
						this.rightPaddleUp();
					}
					else{
						this.rightPaddleDown();
					}
				}
			}

			@Override
			public void drawToImage(MBFImage image) {
				MBFImage gFrame = getNextFrame();
				image.drawImage(gFrame, 0, 0);
			}
			
		}
		public class DRAWING implements Mode {

			protected TouchTableScreen touchScreen;
			protected List<Touch> points;
			

			public DRAWING(TouchTableScreen touchScreen) {
				this.touchScreen = touchScreen;
				points = new ArrayList<Touch>();
			}

			@Override
			public synchronized void acceptTouch(List<Touch> filtered) {
				this.points.addAll(filtered);
			}

			@Override
			public void drawToImage(MBFImage image) {
				List<Touch> toDraw = this.getDrawingPoints();
//				if(this.touchScreen.cameraConfig instanceof TriangleCameraConfig){
//					((TriangleCameraConfig)this.touchScreen.cameraConfig).drawTriangles(image);
//					
//				}
				for (Touch touch : toDraw) {
//					Point2d trans = point2d.transform(this.touchScreen.cameraConfig.homography);
					
					Circle trans = this.touchScreen.cameraConfig.transformTouch(touch);
					if(trans != null)
						image.drawShapeFilled(trans, RGBColour.BLUE);
				}
			}

			protected synchronized List<Touch> getDrawingPoints() {
				List<Touch> toRet = this.points;
				this.points = new ArrayList<Touch>();
				return toRet;
			}
		}
		
		public class DRAWING_TRACKED extends DRAWING {
			Map<Long, Float[]> colours = new HashMap<Long, Float[]>();
			ReallyBasicTouchTracker tracker = new ReallyBasicTouchTracker(75);
			
			public DRAWING_TRACKED(TouchTableScreen touchScreen) {
				super(touchScreen);
			}
			
			@Override
			public synchronized void acceptTouch(List<Touch> filtered) {
				List<Touch> tracked = new ArrayList<Touch>();
				
				for (Touch touch : filtered) {
					Touch trans = this.touchScreen.cameraConfig.transformTouch(touch);
					
					if(trans != null)
						tracked.add(trans);
				}
				
				tracked = tracker.trackPoints(tracked);
				this.points.addAll(tracked);
			}

			
			@Override
			public void drawToImage(MBFImage image) {
				List<Touch> toDraw = this.getDrawingPoints();
				
				
				
				for (Touch touch : toDraw) {
					Float[] col = colours.get(touch.touchID);
					if(touch.getRadius() > 15)
						col = RGBColour.WHITE;
					else if (col == null)
						colours.put(touch.touchID, col = RGBColour.randomColour());
				
					image.drawShapeFilled(touch, col);
					if(touch.motionVector!=null)
						image.drawLine(
							(int)touch.getX(), (int)touch.getY(), 
							(int)(touch.getX()-touch.motionVector.x), 
							(int)(touch.getY()-touch.motionVector.y), 
							(int)(2*touch.getRadius()),col );
				}
			}
		}
		
		public class SERVER extends DRAWING_TRACKED {
			static List<OutputStream> pws = new ArrayList<OutputStream>();
			static ServerSocket serverSocket;
			
			public SERVER(TouchTableScreen touchScreen) {
				super(touchScreen);
				touchScreen.setRenderMode(false);
				
				new Thread(new Runnable() {

					@Override
					public void run() {
						try {
							if(serverSocket!=null) return; 
							serverSocket = new ServerSocket(40000);
						} catch (IOException e) {
							System.out.println("Unable to bind to port");	
							return;
						}
						
						while (true) {
							PrintWriter pw;
							try {
								Socket conn = serverSocket.accept();
								BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
								String line = null;
								String securityBit = "";
								String key = "";
								while((line = br.readLine()) != null){
//									System.out.println(line);
									if(line.startsWith("Sec")){
										securityBit += line + "\r\n";
										if(line.contains("Key")){
											key = line.split(":")[1].trim();
										}
										if(line.contains("Version")){
											break;
										}
									}
								}
								System.out.println("Client connected!");
								OutputStream os = conn.getOutputStream();
								pw = new PrintWriter(os);
								pw.print("HTTP/1.1 101 Web Socket Protocol Handshake\r\n");
								pw.print("Upgrade: WebSocket\r\n");
								pw.print("Connection: Upgrade\r\n");
								pw.print("Sec-WebSocket-Origin: null\r\n" );
								pw.print("Sec-WebSocket-Location: ws://127.0.0.1:40000\r\n");
								System.out.println("Key: \"" + key + "\"");
								String combined = key + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
								byte[] sha1 = MessageDigest.getInstance("SHA-1").digest(combined.getBytes("UTF8"));
								System.out.println("Number of bytes: " + sha1.length);
								String encoded = Base64.encodeBytes(sha1);
								System.out.println("encoded string: " + encoded);
								pw.print("Sec-WebSocket-Accept: " + encoded + "\r\n");
//								pw.print(securityBit);
								pw.print("\r\n");
								pw.flush();
								synchronized(pws) {
									pws.add(os );
								}
							} catch (IOException e) {
								e.printStackTrace();
							} catch (NoSuchAlgorithmException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
					
				}).start();
			}
			
			@Override
			public synchronized void acceptTouch(List<Touch> filtered) {
				super.acceptTouch(filtered);
				
//				String touches = "" + Math.random() + "\n";
				List<Touch> pointsToPrint = this.getDrawingPoints();
//				String touches = createTouchesString(pointsToPrint);
				byte[] touches = createTouchesString(pointsToPrint);
				
				synchronized(pws) {
					//List<PrintWriter> toKill = new ArrayList<PrintWriter>();
					
					for (OutputStream pw : pws) {
						//try {
						try {
							pw.write(touches);
							pw.flush();
						} catch (IOException e) {
						}
						//} catch (IOException e) {
						//	toKill.add(pw);
						//}
					}
					
					//pws.removeAll(toKill);
				}
			}

			private byte[] createTouchesString(List<Touch> pointsToPrint) {
				StringBuilder builder = new StringBuilder();
				String touchFormat = "(%f %f %f %d) ";
				String timeFormat = "[%d] ";
				builder.append(String.format(timeFormat,System.currentTimeMillis()));
				for (Touch touch : pointsToPrint) {
					builder.append(String.format(touchFormat,touch.getX(),touch.getY(),touch.getRadius(),touch.touchID));
				}
//				return builder.toString();
//				String stringout = "{\"wang\" : \"foo\"}";
				String stringout = builder.toString();
				byte[] bytesraw = stringout.getBytes();
				
				byte[] out = null;
				int opcode = 129;
				if(bytesraw.length < 126){
					out = new byte[]{(byte)opcode ,(byte) bytesraw.length};
				}
				else{
					byte first = (byte) (( bytesraw.length >> 8 ) & 255);
					byte second = (byte) (bytesraw.length  & 255);
					
					out = new byte[]{(byte) opcode ,126,first,second};
				}
				try {
					ByteArrayOutputStream bos = new ByteArrayOutputStream();
					bos.write(out);
					bos.write(stringout.getBytes("UTF8"));
					return bos.toByteArray();
				} catch (UnsupportedEncodingException e) {
					
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return new byte[0];
			}
		}
		
		class CALIBRATION_TRIANGLES implements Mode {
			
			private static final int GRIDY = 4;
			private static final int GRIDX = 5;

			private ArrayList<Point2d> touchArray;
			
			int gridxy = (GRIDX+1)* (GRIDY+1); // a 4x4 grid of points
			private TouchTableScreen touchScreen;
			
			public CALIBRATION_TRIANGLES(TouchTableScreen touchTableScreen) {
				this.touchScreen = touchTableScreen;
				this.touchArray = new ArrayList<Point2d>();
			}

			@Override
			public void acceptTouch(List<Touch> filtered) {
				Point2d pixelToAdd = filtered.get(0).getCOG();
				Point2d lastPointAdded = null;
				if(this.touchArray.size() != 0) lastPointAdded = this.touchArray.get(this.touchArray.size() - 1);
				if(
					lastPointAdded == null || 
					Line2d.distance(pixelToAdd, lastPointAdded) > TouchTableDemo.SMALLEST_POINT_DIAMETER
				) {
					this.touchArray.add(pixelToAdd);
				}
				
				if(this.touchArray.size() == this.gridxy){
					calibrate();
				}
			}

			@Override
			public void drawToImage(MBFImage image) {
				image.fill(RGBColour.WHITE);
				int nPoints = touchArray.size();
				float gridX = nPoints % (GRIDX+1);
				float gridY = nPoints / (GRIDX+1);				
				
				Point2dImpl currentpoint = new Point2dImpl(
						(image.getWidth() * (gridX / GRIDX)),
						((image.getHeight()) * (gridY / GRIDY))
				);
				drawTarget(image,currentpoint);
			}
			
			private void drawTarget(MBFImage image, Point2d point){
				image.drawShapeFilled(new Rectangle(point.getX()-5,point.getY()-5,10,10),RGBColour.RED);
			}
			
			private void calibrate() {
				this.touchScreen.cameraConfig = new TriangleCameraConfig(
					this.touchArray,GRIDX,GRIDY,this.touchScreen.visibleArea
				); 
				touchScreen.mode = new Mode.DRAWING(touchScreen);
			}
			
		}
		class CALIBRATION_HOMOGRAPHY implements Mode{
			
			private static Point2d TOP_LEFT = null;
			private static Point2d TOP_RIGHT = null;
			private static Point2d BOTTOM_LEFT = null;
			private static Point2d BOTTOM_RIGHT = null;
			private ArrayList<Point2d> touchArray;
			private TouchTableScreen touchScreen;

			public CALIBRATION_HOMOGRAPHY(TouchTableScreen touchTableScreen){
				this.touchArray = new ArrayList<Point2d>();
				TOP_LEFT = new Point2dImpl(30f,30f);
				TOP_RIGHT = new Point2dImpl(touchTableScreen.image.getWidth()-30f,30f);
				BOTTOM_LEFT = new Point2dImpl(30f,touchTableScreen.image.getHeight()-30f);
				BOTTOM_RIGHT = new Point2dImpl(touchTableScreen.image.getWidth()-30f,touchTableScreen.image.getHeight()-30f);
				this.touchScreen = touchTableScreen;
			}
			
			@Override
			public void drawToImage(MBFImage image) {
				image.fill(RGBColour.WHITE);
				switch (this.touchArray.size()) {
				case 0:
					drawTarget(image,TOP_LEFT);
					break;
				case 1:
					drawTarget(image,TOP_RIGHT);
					break;
				case 2:
					drawTarget(image,BOTTOM_LEFT);
					break;
				case 3:
					drawTarget(image,BOTTOM_RIGHT);
					break;
				default:
					break;
				}
			}

			private void drawTarget(MBFImage image, Point2d point){
				image.drawPoint(point, RGBColour.RED, 10);
			}
			@Override
			public void acceptTouch(List<Touch> filtered) {
				Point2d pixelToAdd = filtered.get(0).getCOG();
				Point2d lastPointAdded = null;
				if(this.touchArray.size() != 0) lastPointAdded = this.touchArray.get(this.touchArray.size() - 1);
				if(
					lastPointAdded == null || 
					Line2d.distance(pixelToAdd, lastPointAdded) > TouchTableDemo.SMALLEST_POINT_DIAMETER
				) {
					this.touchArray.add(pixelToAdd);
				}
				
				if(this.touchArray.size() == 4){
					calibrate();
				}
			}
			private void calibrate() {
				HomographyModel m = new HomographyModel(10f);
				List<IndependentPair<Point2d, Point2d>> matches = new ArrayList<IndependentPair<Point2d, Point2d>>();
			
				matches.add(IndependentPair.pair(TOP_LEFT, this.touchArray.get(0)));
				matches.add(IndependentPair.pair(TOP_RIGHT, this.touchArray.get(1)));
				matches.add(IndependentPair.pair(BOTTOM_LEFT, this.touchArray.get(2)));
				matches.add(IndependentPair.pair(BOTTOM_RIGHT, this.touchArray.get(3)));
				HomographyCameraConfig cameraConfig = new HomographyCameraConfig(
						4.9736307741305950e+002f, 4.9705029823649602e+002f, 
						touchScreen.inputArea.width/2, touchScreen.inputArea.height/2,
						5.8322574816106650e-002f,-1.7482068549377444e-001f,
						-3.1083477039117124e-003f, -4.3781939644044129e-003f
				); 
				m.estimate(matches);
				cameraConfig.homography = m.getTransform().inverse();
				touchScreen.cameraConfig = cameraConfig;
				touchScreen.mode = new Mode.DRAWING(touchScreen);
			}
		};

		public void drawToImage(MBFImage image );

		public void acceptTouch(List<Touch> filtered);
	}
	
	public TouchTableScreen(Rectangle extractionArea, Rectangle visibleArea){
		this.setUndecorated(true);
		this.inputArea = extractionArea;
		this.visibleArea= visibleArea;
	}
	
	public void setRenderMode(boolean renderMode) {
		this.renderMode = renderMode;
		this.setVisible(renderMode);
		
	}

	public void init(){
		int width = this.getWidth();
		int height = this.getHeight();
		
		
		image = new MBFImage(width,height,ColourSpace.RGB);
		this.mode = new Mode.CALIBRATION_TRIANGLES(this);
		
		
		
		Thread t = new Thread(this);
		t.start();
	}
	public void touchEvent(List<Touch> filtered) {
		this.mode.acceptTouch(filtered);
	}
	@Override
	public void run() {
		while(true){
			if(!renderMode)break;
			MBFImage extracted = this.image.extractROI(this.visibleArea);
			if(clear) {
				extracted.fill(RGBColour.WHITE);
				this.clear=false;
			}
			this.mode.drawToImage(extracted);
			
			this.image.drawImage(extracted, 0, 0);
			DisplayUtilities.display(this.image, this);
		}
	}
	public void setCameraConfig(CameraConfig newCC) {
		this.cameraConfig = newCC;
		this.mode = new Mode.DRAWING(this);
	}

	public void clear() {
		this.clear = true;
	}
	
	public static void main(String[] args) throws NoSuchAlgorithmException, UnsupportedEncodingException {
		String key = "x3JJHMbDL1EzLkh9GBhXDw==";
		String combined = key + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
		byte[] sha1 = MessageDigest.getInstance("SHA-1").digest(combined.getBytes("UTF8"));
		System.out.println("Number of bytes: " + sha1.length);
		String encoded = Base64.encodeBytes(sha1);
		System.out.println("encoded string: " + encoded);
	}
}
