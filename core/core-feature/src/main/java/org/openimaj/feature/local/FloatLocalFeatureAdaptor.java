package org.openimaj.feature.local;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.AbstractList;
import java.util.List;
import java.util.Scanner;

import org.openimaj.feature.FloatFV;
import org.openimaj.feature.local.list.LocalFeatureList;
import org.openimaj.feature.normalisation.Normaliser;
import org.openimaj.util.array.ArrayUtils;

/**
 * This class is designed as a wrapper around any form of local feature and
 * provides a way of exposing that feature as a {@link FloatFV}, without the
 * cost of necessarily storing the underlying feature as a {@link FloatFV}. For
 * example, this can be used to make a Keypoint look like it's backed by a float
 * array rather than a byte array, without incurring the four-times increase in
 * storage this would incur.
 * <p>
 * The implementation also allows a normalisation process to occur during
 * conversion through a {@link Normaliser}.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 * @param <L>
 *            The type of {@link Location}
 */
public class FloatLocalFeatureAdaptor<L extends Location> implements LocalFeature<L, FloatFV> {
	LocalFeature<L, ?> localFeature;
	Normaliser<FloatFV> normaliser;

	/**
	 * Construct a new {@link FloatLocalFeatureAdaptor} with the given
	 * underlying feature
	 * 
	 * @param localFeature
	 *            the underlying feature
	 */
	public FloatLocalFeatureAdaptor(LocalFeature<L, ?> localFeature) {
		this.localFeature = localFeature;
	}

	/**
	 * Construct a new {@link FloatLocalFeatureAdaptor} with the given
	 * underlying feature and normaliser.
	 * 
	 * @param localFeature
	 *            the underlying feature
	 * @param normaliser
	 *            the normaliser
	 */
	public FloatLocalFeatureAdaptor(LocalFeature<L, ?> localFeature, Normaliser<FloatFV> normaliser) {
		this.localFeature = localFeature;
		this.normaliser = normaliser;
	}

	@Override
	public void readASCII(Scanner in) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public String asciiHeader() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void readBinary(DataInput in) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public byte[] binaryHeader() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void writeASCII(PrintWriter out) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void writeBinary(DataOutput out) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public FloatFV getFeatureVector() {
		final FloatFV fv = new FloatFV(ArrayUtils.doubleToFloat(localFeature.getFeatureVector().asDoubleFV().values));

		if (normaliser != null)
			normaliser.normalise(fv);

		return fv;
	}

	@Override
	public L getLocation() {
		return localFeature.getLocation();
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof FloatLocalFeatureAdaptor))
			return false;

		return ((FloatLocalFeatureAdaptor<?>) obj).localFeature.equals(localFeature);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((localFeature == null) ? 0 : localFeature.hashCode());
		return result;
	}

	/**
	 * Produce a {@link LocalFeatureList} of {@link FloatLocalFeatureAdaptor} by
	 * wrapping the input list, and dynamically wrapping with the
	 * {@link FloatLocalFeatureAdaptor}s on demand.
	 * 
	 * @param list
	 *            the input list
	 * @return the wrapped list
	 */
	public static <L extends Location> List<FloatLocalFeatureAdaptor<L>> wrap(
			final List<? extends LocalFeature<L, ?>> list)
	{
		return wrap(list, null);
	}

	/**
	 * Produce a {@link LocalFeatureList} of {@link FloatLocalFeatureAdaptor} by
	 * wrapping the input list, and dynamically wrapping with the
	 * {@link FloatLocalFeatureAdaptor}s on demand.
	 * 
	 * @param list
	 *            the input list
	 * @param normaliser
	 *            the normaliser
	 * @return the wrapped list
	 */
	public static <L extends Location> List<FloatLocalFeatureAdaptor<L>> wrap(
			final List<? extends LocalFeature<L, ?>> list, final Normaliser<FloatFV> normaliser)
	{
		final List<FloatLocalFeatureAdaptor<L>> out = new AbstractList<FloatLocalFeatureAdaptor<L>>() {

			@Override
			public FloatLocalFeatureAdaptor<L> get(int index) {
				return new FloatLocalFeatureAdaptor<L>(list.get(index), normaliser);
			}

			@Override
			public int size() {
				return list.size();
			}
		};

		return out;
	}
}
