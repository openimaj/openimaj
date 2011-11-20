package org.openimaj.demos.acmmm11.presentation.slides.tutorial;

import java.util.List;

import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.processing.face.detection.DetectedFace;
import org.openimaj.image.processing.face.detection.HaarCascadeDetector;
import org.openimaj.image.renderer.MBFImageRenderer;
import org.openimaj.image.typography.hershey.HersheyFont;
import org.openimaj.math.geometry.shape.Ellipse;
import org.openimaj.video.Video;

public class ShapeRenderingTutorial extends TutorialPanel {
	private static final long serialVersionUID = 4894581289602770940L;
	
	private HaarCascadeDetector detector;

	public ShapeRenderingTutorial(Video<MBFImage> capture, int width, int height) {
		super("Drawing", capture, width, height);
		this.detector = new HaarCascadeDetector(height/3);
	}

	@Override
	public void doTutorial(MBFImage toDraw) {
		MBFImageRenderer image = toDraw.createRenderer();
		
		List<DetectedFace> faces = this.detector.detectFaces(toDraw.flatten());
		for (DetectedFace detectedFace : faces) {
			float x = detectedFace.getBounds().x;
			float y = detectedFace.getBounds().y;
			float w = detectedFace.getBounds().width;
			float h = detectedFace.getBounds().height;
			renderBubbles(image,x-w/2,y,w,h);
		}
		
	}

	private void renderBubbles(MBFImageRenderer image, float x, float y, float width, float height) {
		float biggestW = width/3;
		float biggestH = height/4;
		image.drawShapeFilled(new Ellipse(x+biggestW*2, y+biggestH*3, biggestW/3, biggestW/3, 0f), RGBColour.WHITE);
		image.drawShapeFilled(new Ellipse(x+biggestW*1.5, y+biggestH*2.5, biggestW/2.5, biggestH/2.5, 0f), RGBColour.WHITE);
		image.drawShapeFilled(new Ellipse(x+biggestW, y+biggestH*1.75, biggestW/2, biggestH/2, 0f), RGBColour.WHITE);
		image.drawShapeFilled(new Ellipse(x, y, biggestW*1.5, biggestH*1.5 , 0f), RGBColour.WHITE);
		image.drawText("OpenIMAJ", (int)(x-biggestW), (int)y, HersheyFont. ASTROLOGY , (int)(biggestW/2.2), RGBColour.BLACK);
		image.drawText("is", (int)(x-biggestW/2), (int)(y+biggestH/2), HersheyFont. ASTROLOGY , (int)(biggestW/2.2), RGBColour.BLACK);
		image.drawText("Awesome", (int)(x-biggestW), (int)(y+biggestH), HersheyFont. ASTROLOGY , (int)(biggestW/2.2), RGBColour.BLACK);
	}
}
