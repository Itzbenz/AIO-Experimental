package org.o7.Fire.MachineLearning.Framework;

import Atom.Struct.PoolObject;
import Atom.Utility.Random;
import io.jenetics.Chromosome;
import io.jenetics.Genotype;
import io.jenetics.NumericGene;

public class ByteGene implements NumericGene<Byte, ByteGene> {
    private final static Pool pool = new Pool();
    byte value;
    
    private ByteGene(byte b) {
        value = b;
    }
    
    public static ByteGene of() {
        return pool.obtain();
    }
    
    public static ByteGene of(byte b) {
        ByteGene byteGene = pool.obtain();
        byteGene.value = b;
        return byteGene;
    }
    
    public static ByteGene[] of(byte... bytes) {
        ByteGene[] byteGenes = new ByteGene[bytes.length];
        for (int i = 0; i < byteGenes.length; i++) {
            byteGenes[i] = pool.obtain();
        }
        return byteGenes;
    }
    
    public static void release(ByteGene[] bytes) {
        for (ByteGene b : bytes)
            release(b);
    }
    
    public static void release(ByteGene byteGene) {
        pool.free(byteGene);
    }
    
    public static Pool getPool() {
        return pool;
    }
    
    @Override
    public ByteGene newInstance(Number number) {
        return newInstance(number.byteValue());
    }
    
    @Override
    public Byte min() {
        return Byte.MIN_VALUE;
    }
    
    @Override
    public Byte max() {
        return Byte.MAX_VALUE;
    }
    
    @Override
    public Byte allele() {
        return value;
    }
    
    @Override
    public ByteGene newInstance() {
        return of();
    }
    
    @Override
    public ByteGene newInstance(Byte value) {
        return of(value);
    }
    
    public static class GenotypeFactory extends PoolObject<Genotype<ByteGene>> implements io.jenetics.util.Factory<Genotype<ByteGene>> {
        public final int maxStateSize;
        
        public GenotypeFactory(int maxStateSize) {
            this.maxStateSize = maxStateSize;
        }
        
        @Override
        protected Genotype<ByteGene> newObject() {
            byte[] bytes = new byte[maxStateSize];
            Random.getNextBytes(bytes);
            return Genotype.of(ByteChromosome.of(bytes));
        }
        
        @Override
        public void free(Genotype<ByteGene> object) {
            for (Chromosome<ByteGene> s : object) {
                for (ByteGene ss : s) {
                    release(ss);
                }
            }
        }
        
        @Override
        protected void reset(Genotype<ByteGene> object) {
            throw new IllegalArgumentException("Must Dispose Object");
        }
        
        @Override
        public Genotype<ByteGene> newInstance() {
            return newObject();
        }
    }
    
    public static class Pool extends PoolObject<ByteGene> implements io.jenetics.util.Factory<ByteGene> {
        @Override
        protected ByteGene newObject() {
            byte[] b = new byte[1];
            Random.getNextBytes(b);
            return new ByteGene(b[0]);
        }
        
        
        @Override
        public ByteGene newInstance() {
            return obtain();
        }
    }
}
