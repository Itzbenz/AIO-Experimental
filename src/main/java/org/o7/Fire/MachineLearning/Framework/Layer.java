package org.o7.Fire.MachineLearning.Framework;

import java.io.Serializable;

public interface Layer<T extends Number & Comparable<T>, N extends Node<T>> extends Serializable, ProcessVector<T>, Cloneable {

	
}
