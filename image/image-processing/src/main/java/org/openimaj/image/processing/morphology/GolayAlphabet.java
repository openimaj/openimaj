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
package org.openimaj.image.processing.morphology;

/**
 * The Golay Alphabet of morphological structuring elements 
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class GolayAlphabet {
	/** The H elements of the Golay Alphabet */
	public final static StructuringElement [] H;
	
	/** The I elements of the Golay Alphabet */
	public final static StructuringElement [] I;
	
	/** The E elements of the Golay Alphabet */
	public final static StructuringElement [] E;
	
	/** The L elements of the Golay Alphabet */
	public final static StructuringElement [] L;
	
	//TODO
//	public final static StructuringElement [] M;
//	public final static StructuringElement [] D;
//	public final static StructuringElement [] F;
//	public final static StructuringElement [] F2;
//	public final static StructuringElement [] K;
//	public final static StructuringElement [] C;
	
	
	static {
		H = new StructuringElement[] {
			StructuringElement.parseElement("***\n***\n***", 1, 1)
		};
		
		I = new StructuringElement[] {
			StructuringElement.parseElement("ooo\nooo\nooo", 1, 1)
		};
		
		E = new StructuringElement[] {
			StructuringElement.parseElement("...\no*o\nooo", 1, 1),
			StructuringElement.parseElement("oo.\no*.\noo.", 1, 1),
			StructuringElement.parseElement("ooo\no*o\n...", 1, 1),
			StructuringElement.parseElement(".oo\n.*o\n.oo", 1, 1)
		};
		
		L = new StructuringElement[] {
			StructuringElement.parseElement("ooo\n.*.\n***", 1, 1),
			StructuringElement.parseElement(".oo\n**o\n.*.", 1, 1),
//			StructuringElement.parseElement("..o..\n...o.\n*.*.o\n.*...\n..*..", 2, 2),
			
			StructuringElement.parseElement("*.o\n**o\n*.o", 1, 1),
			StructuringElement.parseElement(".*.\n**o\n.oo", 1, 1),
//			StructuringElement.parseElement("..*..\n.*...\n*.*.o\n...o.\n..o..", 2, 2),
			
			StructuringElement.parseElement("***\n.*.\nooo", 1, 1),
			StructuringElement.parseElement(".*.\no**\noo.", 1, 1),
//			StructuringElement.parseElement("..*..\n...*.\no.*.*\n.o...\n..o..", 2, 2),
			
			StructuringElement.parseElement("o.*\no**\no.*", 1, 1),
			StructuringElement.parseElement("oo.\no**\n.*.", 1, 1),
//			StructuringElement.parseElement("..o..\n.o...\no.*.*\n...*.\n..*..", 2, 2)
		};
	}
}
