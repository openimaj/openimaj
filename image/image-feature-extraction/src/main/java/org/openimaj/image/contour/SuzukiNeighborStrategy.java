package org.openimaj.image.contour;

import java.util.HashSet;
import java.util.Set;

import org.openimaj.citation.annotation.Reference;
import org.openimaj.citation.annotation.ReferenceType;
import org.openimaj.image.FImage;
import org.openimaj.image.pixel.Pixel;
import org.openimaj.util.function.Operation;
import org.openimaj.util.pair.IndependentPair;

/**
 * The neighbourhood border tracing algorithm described in Appendix 1 of 
 * the Suzuki contour detection algorithm
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 */
@Reference(
		type = ReferenceType.Article,
		author = { "Suzuki, S.", "Abe, K." },
		title = "Topological Structural Analysis of Digitized Binary Image by Border Following",
		year = "1985",
		journal = "Computer Vision, Graphics and Image Processing",
		pages = { "32", "46" },
		month = "January",
		number = "1",
		volume = "30")
public class SuzukiNeighborStrategy extends BorderFollowingStrategy {

	
	public void border(FImage image, Pixel start, Pixel from, final Operation<Pixel> operation) {
		directedBorder(image, start, from, new Operation<IndependentPair<Pixel, Set<Pixel>>>() {

			@Override
			public void perform(IndependentPair<Pixel, Set<Pixel>> object) {
				operation.perform(object.firstObject());
			}
		});
	}
	
	/**
	 * 
	 * @param image
	 * @param ij
	 * @param i2j2
	 * @param operation
	 */
	public void directedBorder(FImage image, Pixel ij, Pixel i2j2, Operation<IndependentPair<Pixel, Set<Pixel>>> operation)
	{
		DIRECTION dir = DIRECTION.fromTo(ij, i2j2);
		DIRECTION trace = dir.clockwise();
		// find i1j1 (3.1)
		Pixel i1j1 = null;
		while(trace!=dir){
			Pixel activePixel = trace.active(image, ij);
			if(activePixel != null){
				i1j1 = activePixel;
				break;
			}
			trace = trace.clockwise();
		}
		if(i1j1 == null) return; //operation never called, signals the starting pixel is alone! (3.1)
		
		i2j2 = i1j1; 
		Pixel i3j3 = ij; // (3.2)
		while(true){
			dir = DIRECTION.fromTo(i3j3, i2j2);
			trace = dir.counterClockwise();
			Pixel i4j4 = null;
			Set<Pixel> checked = new HashSet<Pixel>();
			while(true){
				i4j4 = trace.active(image, i3j3); // 3.3
				if(i4j4 != null) break;
				checked.add(trace.pixel(i3j3));
				trace = trace.counterClockwise();
			}
			operation.perform(IndependentPair.pair(i3j3, checked));
			if(i4j4.equals(ij) && i3j3.equals(i1j1)) break; // 3.5
			i2j2 = i3j3; // 3.5
			i3j3 = i4j4; // 3.5
		}
	}

}
