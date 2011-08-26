package org.openimaj.image.processing.convolution;

import static org.junit.Assert.*;

import org.junit.Test;
import org.openimaj.image.FImage;

public class FConvolutionTest {
	@Test
	public void testConsistency(){
		FImage kernel = new FImage(3,3);
		kernel.addInline(1f);
		
		FImage kernelRow = new FImage(3,1);
		FImage kernelCol = new FImage(1,3);
		
		kernelRow.addInline(3f);
		kernelCol.addInline(3f);
		
		FImage im = new FImage(10,10);
		im.addInline(1f);
		
		FConvolution conAutoSep = new FConvolution(kernel);
		FConvolution conBrute = new FConvolution(kernel);
		FConvolution conAutoRow = new FConvolution(kernelRow);
		FConvolution conAutoCol = new FConvolution(kernelCol);
		
		conBrute .setBruteForce(true);
		
		assertTrue(im.process(conAutoSep).equalsThresh(im.multiply(9f), 0.001f));
		assertTrue(im.process(conAutoRow).equalsThresh(im.multiply(9f), 0.001f));
		assertTrue(im.process(conAutoCol).equalsThresh(im.multiply(9f), 0.001f));
		assertTrue(im.process(conBrute).equalsThresh(im.multiply(9f), 0.001f));
	}
}
