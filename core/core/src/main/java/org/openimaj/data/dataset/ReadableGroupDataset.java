//package org.openimaj.data.dataset;
//
//import java.util.AbstractList;
//import java.util.AbstractMap;
//
//import org.openimaj.data.identity.Identifiable;
//import org.openimaj.data.identity.IdentifiableObject;
//import org.openimaj.io.ObjectReader;
//
///**
// * Base class for {@link GroupedDataset}s in which each instance is read with an
// * {@link ObjectReader}.
// * 
// * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
// * 
// * @param <KEY>
// *            Type of dataset class key
// * @param <DATASET>
// *            Type of sub-datasets.
// * @param <INSTANCE>
// *            Type of instances in the dataset
// */
//public abstract class ReadableGroupDataset<KEY, DATASET extends Dataset<INSTANCE>, INSTANCE>
//		extends
//		AbstractMap<KEY, DATASET> implements GroupedDataset<KEY, DATASET, INSTANCE>
//{
//	protected ObjectReader<INSTANCE> reader;
//
//	/**
//	 * Construct with the given {@link ObjectReader}.
//	 * 
//	 * @param reader
//	 *            the {@link ObjectReader}.
//	 */
//	public ReadableGroupDataset(ObjectReader<INSTANCE> reader) {
//		this.reader = reader;
//	}
//
//	@Override
//	public INSTANCE getRandomInstance() {
//		return getInstance((int) (Math.random() * size()));
//	}
//
//	@Override
//	public INSTANCE get(int index) {
//		return this.getInstance(index);
//	}
//
//	@Override
//	public int numInstances() {
//		return size();
//	}
//
//	/**
//	 * Get an identifier for the instance at the given index. By default this
//	 * just returns the index converted to a {@link String}, but sub-classes
//	 * should override to to something more sensible if possible.
//	 * 
//	 * @param index
//	 *            the index
//	 * @return the identifier of the instance at the given index
//	 */
//	public String getID(int index) {
//		return index + "";
//	}
//
//	/**
//	 * Get the index of the instance with the given ID, or -1 if it can't be
//	 * found.
//	 * 
//	 * @param id
//	 *            the ID string
//	 * @return the index; or -1 if not found.
//	 */
//	public int indexOfID(String id) {
//		for (int i = 0; i < size(); i++) {
//			if (getID(i).equals(id))
//				return i;
//		}
//		return -1;
//	}
//
//	private class WrappedListDataset extends AbstractList<IdentifiableObject<INSTANCE>>
//			implements
//			ListDataset<IdentifiableObject<INSTANCE>>
//	{
//		private final ReadableGroupDataset<INSTANCE> internal;
//
//		WrappedListDataset(ReadableGroupDataset<INSTANCE> internal) {
//			this.internal = internal;
//		}
//
//		@Override
//		public IdentifiableObject<INSTANCE> getRandomInstance() {
//			final int index = (int) (Math.random() * size());
//
//			return getInstance(index);
//		}
//
//		@Override
//		public IdentifiableObject<INSTANCE> getInstance(int index) {
//			return new IdentifiableObject<INSTANCE>(internal.getID(index), internal.getInstance(index));
//		}
//
//		@Override
//		public IdentifiableObject<INSTANCE> get(int index) {
//			return getInstance(index);
//		}
//
//		@Override
//		public int size() {
//			return internal.size();
//		}
//	}
//
//	/**
//	 * Create a view of this dataset in which the instances are wrapped up in
//	 * {@link IdentifiableObject}s. The {@link #getID(int)} method is used to
//	 * determine the identifier.
//	 * 
//	 * @return a view of this dataset with {@link Identifiable}-wrapped
//	 *         instances
//	 */
//	public ListDataset<IdentifiableObject<INSTANCE>> toIdentifiable() {
//		return new WrappedListDataset(this);
//	}
// }
