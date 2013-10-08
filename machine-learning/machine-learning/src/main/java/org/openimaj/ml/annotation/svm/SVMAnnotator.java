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
/**
 *
 */
package org.openimaj.ml.annotation.svm;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import libsvm.svm;
import libsvm.svm_model;
import libsvm.svm_node;
import libsvm.svm_parameter;
import libsvm.svm_problem;

import org.openimaj.feature.FeatureExtractor;
import org.openimaj.feature.FeatureVector;
import org.openimaj.ml.annotation.Annotated;
import org.openimaj.ml.annotation.BatchAnnotator;
import org.openimaj.ml.annotation.ScoredAnnotation;
import org.openimaj.ml.annotation.utils.AnnotatedListHelper;
import org.openimaj.util.array.ArrayUtils;

/**
 *	Wraps the libsvm SVM and provides basic positive/negative
 *	annotation for a single class.
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *  @created 14 May 2013
 *
 * 	@param <OBJECT> The object being annotated
 * 	@param <ANNOTATION> The type of the annotation
 */
public class SVMAnnotator<OBJECT,ANNOTATION> extends BatchAnnotator<OBJECT,ANNOTATION>
{
	/** The input to the SVM model for positive classes */
	public static final int POSITIVE_CLASS = +1;

	/** The input to the SVM model for negative classes */
	public static final int NEGATIVE_CLASS = -1;

	/** Stores the mapping between the positive and negative class and the annotation */
	public HashMap<Integer,ANNOTATION> classMap = new HashMap<Integer,ANNOTATION>();

	/** The libsvm SVM model */
	private svm_model model = null;

	/** The feature extractor being used to extract features from OBJECT */
	private FeatureExtractor<? extends FeatureVector, OBJECT> extractor = null;

	/** The file to save the model to after it is trained (or null for no saving) */
	private File saveModel = null;

	/**
	 * 	Constructor that takes the feature extractor to use.
	 *	@param extractor The feature extractor
	 */
	public SVMAnnotator( final FeatureExtractor<? extends FeatureVector, OBJECT> extractor )
	{
		this.extractor = extractor;
	}

	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.ml.training.BatchTrainer#train(java.util.List)
	 */
	@Override
	public void train( final List<? extends Annotated<OBJECT, ANNOTATION>> data )
	{
		// Check the data has 2 classes and update the class map.
		if( this.checkInputDataOK( data ) )
		{
			// Setup the SVM problem
			final svm_parameter param = SVMAnnotator.getDefaultSVMParameters();
			final svm_problem prob = this.getSVMProblem( data, param, this.extractor );

			// Train the SVM
			this.model = libsvm.svm.svm_train( prob, param );

			// Save the model if we're going to do that.
			if( this.saveModel != null ) try
			{
				svm.svm_save_model( this.saveModel.getAbsolutePath(), this.model );
			}
			catch( final IOException e )
			{
				e.printStackTrace();
			}
		}
	}

	/**
	 * 	Checks that the input data only has 2 classes. The method also assigns
	 * 	those classes to positive or negative values.
	 *
	 *	@param data The data
	 *	@return TRUE if and only if there are 2 classes
	 */
	private boolean checkInputDataOK( final List<? extends Annotated<OBJECT, ANNOTATION>> data )
	{
		// Clear the class map. We don't want any old annotations left in there.
		this.classMap.clear();

		// Loop over the data and check for valid data.
		int i = 0;
		for( final Annotated<OBJECT,ANNOTATION> x : data )
		{
			// Get the annotations for the object.
			final Collection<ANNOTATION> anns = x.getAnnotations();

			// Check there is only one annotation on each object.
			if( anns.size() != 1 )
				throw new IllegalArgumentException( "Data contained an object with more than one annotation" );

			// Get the only annotation.
			final ANNOTATION onlyAnnotation = anns.iterator().next();

			// Check if it's already been seen.
			if( !this.classMap.values().contains( onlyAnnotation ) )
			{
				// Key will be -1, +1, +3...
				final int key = i * 2 -1;
				i++;

				// Put the first annotation into the map at the appropriate place.
				this.classMap.put( key, onlyAnnotation );
			}
		}

		// If the data didn't contain 2 classes (i.e. positive and negative) we cannot go on
		if( this.classMap.keySet().size() != 2 )
		{
			throw new IllegalArgumentException( "Data did not contain exactly 2 classes. It had "+this.classMap.keySet().size()+". They were "+this.classMap );
		}

		return true;
	}

	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.ml.annotation.Annotator#getAnnotations()
	 */
	@Override
	public Set<ANNOTATION> getAnnotations()
	{
		final HashSet<ANNOTATION> hs = new HashSet<ANNOTATION>();
		hs.addAll( this.classMap.values() );
		return hs;
	}

	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.ml.annotation.Annotator#annotate(java.lang.Object)
	 */
	@Override
	public List<ScoredAnnotation<ANNOTATION>> annotate( final OBJECT object )
	{
		// Extract the feature and convert to a svm_node[]
		final svm_node[] nodes = SVMAnnotator.featureToNode( this.extractor.extractFeature( object ) );

		// Use the trained SVM model to predict the new buffer's annotation
		final double x = svm.svm_predict( this.model, nodes );

		// Create a singleton list to contain the classified annotation.
		return Collections.singletonList( new ScoredAnnotation<ANNOTATION>( x > 0 ?
				this.classMap.get(SVMAnnotator.POSITIVE_CLASS) : this.classMap.get(SVMAnnotator.NEGATIVE_CLASS),
				1.0f ) );
	}

	/**
	 * 	Set whether to save the SVM model to disk.
	 *	@param saveModel The file name to save to, or null to disable saving.
	 */
	public void setSaveModel( final File saveModel )
	{
		this.saveModel = saveModel;
	}

	/**
	 * 	Load an existing svm model.
	 *
	 *	@param loadModel The model to load from
	 * 	@throws IOException If the loading does not complete
	 */
	public void loadModel( final File loadModel ) throws IOException
	{
		this.model = svm.svm_load_model( loadModel.getAbsolutePath() );
	}

	/**
	 *	Performs cross-validation on the SVM.
	 *
	 * 	@param data The data
	 * 	@param numFold The number of folds
	 * 	@return The calculated accuracy
	 */
	public double crossValidation( final List<? extends Annotated<OBJECT, ANNOTATION>> data,
			final int numFold )
	{
		// Setup the SVM problem
		final svm_parameter param = SVMAnnotator.getDefaultSVMParameters();
		final svm_problem prob = this.getSVMProblem( data, param, this.extractor );

		return SVMAnnotator.crossValidation( prob, param, numFold );
	}

	/**
	 *	Performs cross-validation on the SVM.
	 *
	 *	@param prob The problem
	 * 	@param param The parameters
	 * 	@param numFold The number of folds
	 * 	@return The accuracy
	 */
	static public double crossValidation( final svm_problem prob,
			final svm_parameter param, final int numFold )
	{
		// The target array in which the final classifications are put
		final double[] target = new double[prob.l];

		// Perform the cross-validation.
		svm.svm_cross_validation( prob, param, numFold, target );

		// Work out how many classifications were correct.
		int totalCorrect = 0;
		for( int i = 0; i < prob.l; i++ )
			if( target[i] == prob.y[i] )
				totalCorrect++;

		// Calculate the accuracy
		final double accuracy = 100.0 * totalCorrect / prob.l;
		System.out.print("Cross Validation Accuracy = "+accuracy+"%\n");

		return accuracy;
	}

	// ========================================================================================= //
	// Static methods below here.
	// ========================================================================================= //

	/**
	 * 	Returns the default set of SVM parameters.
	 *	@return The default set of SVM parameters
	 */
	static private svm_parameter getDefaultSVMParameters()
	{
		// These default values came from:
		// https://github.com/arnaudsj/libsvm/blob/master/java/svm_train.java

		final svm_parameter param = new svm_parameter();
		param.svm_type = svm_parameter.C_SVC;
		param.kernel_type = svm_parameter.RBF;
		param.degree = 3;
		param.gamma = 0;	// 1/num_features
		param.coef0 = 0;
		param.nu = 0.5;
		param.cache_size = 100;
		param.C = 1;
		param.eps = 1e-3;
		param.p = 0.1;
		param.shrinking = 1;
		param.probability = 0;
		param.nr_weight = 0;
		param.weight_label = new int[0];
		param.weight = new double[0];

		return param;
	}

	/**
	 * 	Returns an svm_problem for the given dataset. This function will return
	 * 	a new SVM problem and will also side-affect the gamma member of the
	 * 	param argument.
	 *
	 * 	@param trainingCorpus The corpus
	 * 	@param param The SVM parameters
	 * 	@param featureExtractor The feature extractor to use
	 * 	@param positiveClass The name of the positive class in the dataset
	 * 	@param negativeClasses The names of the negative classes in the dataset
	 *	@return A new SVM problem.
	 */
	private svm_problem getSVMProblem( final List<? extends Annotated<OBJECT, ANNOTATION>> data,
		final svm_parameter param, final FeatureExtractor<? extends FeatureVector, OBJECT> extractor  )
	{
		// Get all the nodes for the features
		final svm_node[][] positiveNodes = this.computeFeature(
				data, this.classMap.get( SVMAnnotator.POSITIVE_CLASS ) );
		final svm_node[][] negativeNodes = this.computeFeature(
				data, this.classMap.get( SVMAnnotator.NEGATIVE_CLASS ) );

		// Work out how long the problem is
		final int nSamples = positiveNodes.length + negativeNodes.length;

		// The array that determines whether a sample is positive or negative.
		final double[] flagArray = new double[nSamples];
		ArrayUtils.fill( flagArray, SVMAnnotator.POSITIVE_CLASS, 0, positiveNodes.length );
		ArrayUtils.fill( flagArray, SVMAnnotator.NEGATIVE_CLASS, positiveNodes.length, negativeNodes.length );

		// Concatenate the samples to a single array
		final svm_node[][] sampleArray = ArrayUtils.concatenate(
				positiveNodes, negativeNodes );

		// Create the svm problem to solve
		final svm_problem prob = new svm_problem();

		// Setup the problem
		prob.l = nSamples;
		prob.x = sampleArray;
		prob.y = flagArray;
		param.gamma = 1.0 / SVMAnnotator.getMaxIndex( sampleArray );

		return prob;
	}

	/**
	 *	Computes all the features for a given annotation in the data set and returns
	 *	a set of SVM nodes that represent those features.
	 *
	 *	@param data The data
	 *	@param annotation The annotation of the objects to pick out
	 *	@return
	 */
	private svm_node[][] computeFeature(
			final List<? extends Annotated<OBJECT, ANNOTATION>> data,
			final ANNOTATION annotation )
	{
		// Extract the features for the given annotation
		final AnnotatedListHelper<OBJECT,ANNOTATION> alh = new AnnotatedListHelper<OBJECT,ANNOTATION>(data);
		final List<? extends FeatureVector> f = alh.extractFeatures( annotation, this.extractor );

		// Create the output value - a 2D array of svm_nodes.
		final svm_node[][] n = new svm_node[ f.size() ][];

		// Loop over each feature and convert it to an svm_node[]
		int i = 0;
		for( final FeatureVector feature : f )
			n[i++] = SVMAnnotator.featureToNode( feature );

		return n;
	}

	/**
	 * 	Returns the maximum index value from all the svm_nodes in the array.
	 *	@param sampleArray The array of training samples
	 *	@return The max feature index
	 */
	static private int getMaxIndex( final svm_node[][] sampleArray )
	{
		int max = 0;
		for( final svm_node[] x : sampleArray )
			for( int j = 0; j < x.length; j++ )
				max = Math.max( max, x[j].index );
		return max;
	}

	/**
	 * 	Takes a {@link FeatureVector} and converts it into an array of {@link svm_node}s
	 * 	for the svm library.
	 *
	 *	@param featureVector The feature vector to convert
	 *	@return The equivalent svm_node[]
	 */
	static private svm_node[] featureToNode( final FeatureVector featureVector )
	{
//		if( featureVector instanceof SparseFeatureVector )
//		{
//
//		}
//		else
		{
			final double[] fv = featureVector.asDoubleVector();
			final svm_node[] nodes = new svm_node[fv.length];

			for( int i = 0; i < fv.length; i++ )
			{
				nodes[i] = new svm_node();
				nodes[i].index = i;
				nodes[i].value = fv[i];
			}

			return nodes;
		}
	}
}
