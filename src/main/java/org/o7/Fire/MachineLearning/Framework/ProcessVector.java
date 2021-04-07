package org.o7.Fire.MachineLearning.Framework;

import java.io.Serializable;

public interface ProcessVector<T extends Number & Comparable<T>> extends Serializable, Cloneable {
	int getInputSize();
	
	int getOutputSize();
	
	T[] process(T[] array);
}
