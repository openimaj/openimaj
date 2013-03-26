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
package org.openimaj.experiment.validation;

import org.openimaj.data.dataset.Dataset;

/**
 * Interface describing the contract for classes which can
 * perform a the operations required for a single round
 * of validation.
 * <p>
 * The common use-case of this class is for it to be used
 * as part of the validation of some system as coordinated
 * by a {@link ValidationRunner}. As the {@link ValidationRunner}
 * can perform multiple validations at once using multi-threading,
 * implementors of this interface should be thread-safe. 
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 * @param <DATASET> The type of dataset
 * @param <ANALYSIS_RESULT> The type of the resulting data from the operation.
 */
public interface ValidationOperation<DATASET extends Dataset<?>, ANALYSIS_RESULT> {
	/**
	 * Perform the operations required for one round of validation
	 * using the provided training and validation datasets. The
	 * result of the evaluation round is returned.
	 * 
	 * @param training the training data
	 * @param validation the validation data
	 * @return the result of the validation round
	 */
	public abstract ANALYSIS_RESULT evaluate(DATASET training, DATASET validation);		
}
