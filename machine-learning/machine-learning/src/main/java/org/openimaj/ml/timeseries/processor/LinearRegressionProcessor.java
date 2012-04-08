package org.openimaj.ml.timeseries.processor;

import org.openimaj.ml.regression.LinearRegression;
import org.openimaj.ml.timeseries.series.DoubleTimeSeries;
import org.openimaj.util.array.ArrayUtils;

import Jama.Matrix;

/**
 * Using a {@link LinearRegression} model, a time series is used as input to calculate the coefficients 
 * of a linear regression such that value = b * time + c
 * 
 * This is the simplest kind of model that can be applied to a time series
 * @author ss
 *
 */
public class LinearRegressionProcessor implements TimeSeriesProcessor<double[], Double, DoubleTimeSeries>{

	private LinearRegression reg;
	private boolean regdefined;
	/**
	 * Calculate the regression from the same time series inputed
	 */
	public LinearRegressionProcessor() {
		this.regdefined = false;
	}
	
	/**
	 * Use reg as the linear regression to predict. The {@link #process(DoubleTimeSeries)} function simply calls
	 * {@link LinearRegression#predict(Matrix)} with the times in the series as input
	 * @param reg
	 */
	public LinearRegressionProcessor(LinearRegression reg) {
		this.reg = reg;
		this.regdefined = true;
	}

	@Override
	public void process(DoubleTimeSeries series) {
		Matrix x = new Matrix(new double[][]{ArrayUtils.longToDouble(series.getTimes())}).transpose();
		if(!regdefined)
		{
			this.reg = new LinearRegression();
			Matrix y = new Matrix(new double[][]{series.getData()}).transpose();
			reg.estimate(y, x);
		}
		Matrix predicted = this.reg.predict(x);
		series.set(series.getTimes(), predicted.transpose().getArray()[0]);
	}
	
	/**
	 * @param regdefined if true, process holds its last {@link LinearRegression}
	 */
	public void holdreg(boolean regdefined){
		this.regdefined = regdefined;
	}
	
	/**
	 * @return the {@link LinearRegressionProcessor}'s underlying {@link LinearRegression} model
	 */
	public LinearRegression getRegression() {
		return this.reg;
	}

}
