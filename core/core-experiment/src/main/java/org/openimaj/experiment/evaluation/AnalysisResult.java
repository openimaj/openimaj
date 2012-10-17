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
package org.openimaj.experiment.evaluation;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperPrint;

/**
 * The result of the analysis of raw data. 
 * <p>
 * {@link AnalysisResult}s are capable of producing summary and
 * detailed information in the form of a {@link String} as well
 * as producing a {@link JasperPrint} objects containing a renderable
 * summary and detailed reports.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public interface AnalysisResult {
	/**
	 * Get a {@link JasperPrint} summarising the result.
	 * @param title a title to add to the report
	 * @param info information to print at the beginning of the report
	 * 
	 * @return a {@link String} summarising the result
	 * @throws JRException 
	 */
	public JasperPrint getSummaryReport(String title, String info) throws JRException;
	
	/**
	 * Get a {@link JasperPrint} detailing the result.
	 * @param title a title to add to the report
	 * @param info information to print at the beginning of the report
	 * 
	 * @return a {@link JasperPrint} detailing the result.
	 * @throws JRException 
	 */
	public JasperPrint getDetailReport(String title, String info) throws JRException;
	
	/**
	 * Get a {@link String} summarising the result.
	 * 
	 * @return a {@link String} summarising the result
	 */
	public String getSummaryReport();
	
	/**
	 * Get a {@link String} detailing the result.
	 * 
	 * @return a {@link String} detailing the result
	 */
	public String getDetailReport();
}
