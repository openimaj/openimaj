package org.openimaj.image.contour;

import java.util.ArrayList;
import java.util.List;

import org.openimaj.image.contour.ContourAestheticode.Aestheticode;
import org.openimaj.image.contour.SuzukiContourProcessor.Border;
import org.openimaj.util.function.Function;
import org.openimaj.util.function.Predicate;

/**
 *
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 */
public class FindAestheticode implements Function<Border, List<Aestheticode>>, Predicate<Border>{

	private static final int MAX_CHILDLESS_CHILDREN = 0;
	private static final int MAX_CHILDREN = 5;
	private static final int MIN_CHILDREN = 5;
	@Override
	public List<Aestheticode> apply(Border in) {
		List<Aestheticode> found = new ArrayList<Aestheticode>();
		detectCode(in,found);
		return found;
	}
	
	private void detectCode(Border root, List<Aestheticode> found) {
		if(test(root)){
			found.add(new Aestheticode(root));
		}
		else{
			for (Border child : root.children) {
				detectCode(child, found);
			}
		}
	}
	@Override
	public boolean test(Border in) {
		// has at least one child
		if(in.children.size() < MIN_CHILDREN || in.children.size() > MAX_CHILDREN ){
			return false;
		}
		int childlessChild = 0;
		// all grandchildren have no children
		for (Border child : in.children) {
			if(child.children.size() == 0) childlessChild++;
			
			if(childlessChild > MAX_CHILDLESS_CHILDREN) return false;
			for (Border grandchildren : child.children) {
				if(grandchildren.children.size() != 0) return false;
			}
		}
		return true;
	}
	
}