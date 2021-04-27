package org.o7.Fire.MachineLearning.Framework;

import java.util.ArrayList;
import java.util.Collection;

public class ArrayListCapped<T> extends ArrayList<T> {
	protected int max = Integer.MAX_VALUE;
	
	
	public ArrayListCapped(int max) {
		this.max = max;
	}
	
	public ArrayListCapped() {
	
	}
	
	public void trim() {
		if (size() > max) {
			subList(max, size() - 1).clear();
		}
	}
	
	@Override
	public boolean addAll(int index, Collection<? extends T> c) {
		boolean b = super.addAll(index, c);
		trim();
		return b;
	}
	
	@Override
	public boolean addAll(Collection<? extends T> c) {
		boolean b = super.addAll(c);
		trim();
		return b;
	}
	
	@Override
	public boolean add(T t) {
		boolean b = super.add(t);
		trim();
		return b;
	}
}
