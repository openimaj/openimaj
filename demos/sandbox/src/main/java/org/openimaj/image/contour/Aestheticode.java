package org.openimaj.image.contour;

import java.util.Arrays;

import org.openimaj.image.contour.SuzukiContourProcessor.Border;

/**
 * A detected Aestheticode
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 */
public class Aestheticode{
	private int[] code;
	/**
	 * @param code the code
	 */
	public Aestheticode(int[] code) {
		this.code = code;
		Arrays.sort(code);
	}
	
	/**
	 * @param acode copy this {@link Aestheticode}
	 */
	public Aestheticode(Aestheticode acode) {
		this.code = Arrays.copyOf(acode.code, acode.code.length);
	}
	
	/**
	 * @param root the detected borders to construct a code from
	 */
	public Aestheticode(Border root) {
		this.root = root;
		
		this.code = new int[root.children.size()];
		int i = 0;
		for (Border child : root.children) {
			this.code[i++] = child.children.size();
		}
		Arrays.sort(code);
	}
	
	public int hashCode() {
		return Arrays.hashCode(this.code);
	};
	
	public boolean equals(Object obj) {
		if(!(obj instanceof Aestheticode)){
			return false;
		}
		Aestheticode that = (Aestheticode) obj;
		if(that.code.length!= this.code.length) return false;
		for (int i = 0; i < this.code.length; i++) {
			if(this.code[i] != that.code[i])return false;
		}
		return true;
	};
	
	public String toString() {
		String ret = "" + this.code[0];
		for (int i = 1; i < code.length; i++) {
			ret += ":" + code[i];
		}
		return ret;
	};
	Border root;
}