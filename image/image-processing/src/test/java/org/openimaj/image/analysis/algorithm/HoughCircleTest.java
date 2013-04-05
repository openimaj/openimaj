package org.openimaj.image.analysis.algorithm;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.junit.Before;
import org.junit.Test;
import org.openimaj.image.FImage;
import org.openimaj.image.analysis.algorithm.HoughCircles.WeightedCircle;
import org.openimaj.image.processing.edges.CannyEdgeDetector;
import org.openimaj.math.geometry.shape.Circle;

public class HoughCircleTest {


	@Before
	public void before() throws IOException {
		ConsoleAppender console = new ConsoleAppender(); //create appender
		//configure the appender
		String PATTERN = "[%C{1}] %m%n";
		console.setLayout(new PatternLayout(PATTERN));
		console.setThreshold(Level.FATAL);
		console.activateOptions();
	  	// add appender to any Logger (here is root)
		Logger.getRootLogger().addAppender(console);
	}
	@Test
	public void testCircle(){

		int imgWidthHeight = 200;
		FImage circleImage = new FImage(imgWidthHeight,imgWidthHeight);
		Circle c = new Circle(imgWidthHeight/2 + 3, imgWidthHeight/2 + 1, imgWidthHeight/4);
		circleImage.drawShapeFilled(c, 1f);
		CannyEdgeDetector det = new CannyEdgeDetector();
		FImage edgeImage = circleImage.process(det);

		HoughCircles circ = new HoughCircles(5, imgWidthHeight,5,360);
		edgeImage.analyseWith(circ);

		List<WeightedCircle> best = circ.getBest(1);
		WeightedCircle b = best.get(0);
		assertTrue(b.equals(c));
	}

	public static void main(String[] args) throws IOException {
		HoughCircleTest t = new HoughCircleTest();
		t.before();
		t.testCircle();
	}
}
