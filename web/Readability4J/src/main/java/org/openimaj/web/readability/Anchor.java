package org.openimaj.web.readability;

/**
 * Class to represent a simple HTML anchor tag.
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 *
 */
public class Anchor {
	String text;
	String href;
	
	public Anchor(String text, String href) {
		this.text = text;
		this.href = href;
	}
	
	public String getText() {
		return text;
	}
	
	public void setAnchorText(String anchorText) {
		this.text = anchorText;
	}
	
	public String getHref() {
		return href;
	}
	
	public void setHref(String href) {
		this.href = href;
	}
	
	@Override
	public String toString() {
		return "(text: \""+ text+"\", url:\""+ href + "\")";
	}
}
