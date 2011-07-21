/**
 * Copyright 2010 The University of Southampton, Yahoo Inc., and the
 * individual contributors. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
