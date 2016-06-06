package org.openimaj.workinprogress.optimisation.params;

public interface Parameters<PTYPE extends Parameters<PTYPE>> {
	public abstract void multiplyInplace(PTYPE other);

	public abstract void multiplyInplace(double value);

	public abstract void addInplace(PTYPE other);

	public abstract void addInplace(double value);
}
