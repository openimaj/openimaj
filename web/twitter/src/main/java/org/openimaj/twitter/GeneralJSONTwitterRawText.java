/**
 * Copyright (c) 2012, The University of Southampton and the individual contributors.
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
package org.openimaj.twitter;

import java.io.IOException;
import java.util.Scanner;


/**
 * {@link GeneralJSONTwitterRawText} extends {@link GeneralJSONTwitter} to provide an object that can
 * read raw strings. It can also then be used by USMFFStatus to
 * fill a USMFSStatus with the relevant twitter fields.
 *
 * @author Laurence Willmore (lgw1e10@ecs.soton.ac.uk), Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class GeneralJSONTwitterRawText extends GeneralJSONTwitter {
	@Override
	public void readASCII(Scanner in) throws IOException {
		this.text = in.nextLine();
	}

	@Override
	public GeneralJSON instanceFromString(String line) {
		GeneralJSONTwitterRawText ret = new GeneralJSONTwitterRawText();
		ret.text = line;
		return ret ;
	}
}
