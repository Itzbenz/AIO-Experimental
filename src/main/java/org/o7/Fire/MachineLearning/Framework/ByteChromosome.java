package org.o7.Fire.MachineLearning.Framework;

import Atom.Struct.PoolObject;
import io.jenetics.Chromosome;
import io.jenetics.NumericChromosome;
import io.jenetics.util.ISeq;

import java.io.Serializable;

public class ByteChromosome implements NumericChromosome<Byte, ByteGene>, Serializable {
    private transient static final Pool pool = new Pool();
    private ISeq<ByteGene> byteGenes;
    private boolean primitive;
    private ByteGene[] arr;
    
    private ByteChromosome() {}
    
    public static ByteChromosome of(byte... bytes) {
        return of(ByteGene.of(bytes));
    }
    
    public static ByteChromosome of(ISeq<ByteGene> genes) {
        return pool.obtain().reset(genes);
    }
    
    public static ByteChromosome of(ByteGene... genes) {
        return pool.obtain().reset(genes);
    }
    
    protected ByteChromosome reset(ByteGene[] genes) {
        arr = genes;
        byteGenes = null;
        primitive = true;
        return this;
    }
    
    protected ByteChromosome reset(ISeq<ByteGene> genes) {
        byteGenes = genes;
        arr = null;
        primitive = false;
        return this;
    }
    
    @Override
    public Chromosome<ByteGene> newInstance(ISeq<ByteGene> genes) {
        return of(genes);
    }
    
    @Override
    public ByteGene get(int index) {
        if (primitive) return arr[index];
        return byteGenes.get(index);
    }
    
    @Override
    public int length() {
        if (primitive) return arr.length;
        return byteGenes.length();
    }
    
    @Override
    public Chromosome<ByteGene> newInstance() {
        throw new RuntimeException("no");
    }
    
    public static class Pool extends PoolObject<ByteChromosome> {
        
        @Override
        protected ByteChromosome newObject() {
            return new ByteChromosome();
        }
    }
}
