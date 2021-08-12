package org.o7.Fire.MachineLearning.PolicyMaker;

import Atom.Struct.PoolObject;

public class PolicyPool extends PoolObject<byte[]> {
    final int size;
    
    public PolicyPool(int size) {
        this.size = size;
    }
    
    @Override
    protected byte[] newObject() {
        return new byte[size];
    }
}
