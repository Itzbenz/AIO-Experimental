package org.o7.Fire.MachineLearning.Java;

import org.o7.Fire.MachineLearning.Framework.ActivationFunction;
import org.o7.Fire.MachineLearning.Framework.Layer;

import java.io.Serializable;
import java.util.Arrays;

import static Atom.Utility.Random.getBool;
import static Atom.Utility.Random.getFloat;

public class VectorLayer implements Layer<Float, NodeLayer>, Serializable {
    protected Float[] bias;
    protected Float[][] weight;
    protected int output, input;
    protected ActivationFunction activation;
    
    public VectorLayer(int input, int output) {
        this(input, output, ActivationFunction.Identity);
    }
    
    public VectorLayer(int input, int output, ActivationFunction activation) {
        if (input < 1 || output < 1) throw new IllegalArgumentException("not positive integer");
        this.input = input;
        this.output = output;
        this.activation = activation;
        bias = new Float[output];
        weight = new Float[output][input];
        for (int i = 0; i < bias.length; i++) {
            bias[i] = getFloat();
        }
        for (Float[] f : weight) {
            for (int j = 0; j < f.length; j++) {
                f[j] = getFloat();
            }
        }
        
    }

    public void updateBias() {
        for (int i = 0; i < bias.length; i++) {
            float r = getFloat();
            bias[i] = bias[i] + (getBool() ? r : -r);
        }
    }
    
    public void updateWeight() {
        for (Float[] f : weight) {
            for (int j = 0; j < f.length; j++) {
                float r = getFloat();
                f[j] = f[j] + (getBool() ? r : -r);
            }
        }
    }
    
    
    protected float getBias(int i) {
        return bias[i];
    }
    
    @Override
    public int getInputSize() {
        return input;
    }
    
    @Override
    public int getOutputSize() {
        return output;
    }
    
    public ActivationFunction getActivation() {
        return activation;
    }
    
    public VectorLayer setActivation(ActivationFunction activation) {
        this.activation = activation;
        return this;
    }
    
    @Override
    public Float[] process(Float[] array) {
        if (array.length != input)
            throw new IllegalArgumentException("Input promised: " + input + " but get " + array.length);
        Float[] output = new Float[getOutputSize()];
        //vector operation would be great
        for (int i = 0; i < getOutputSize(); i++) {
            float node = 0;
            for (int j = 0; j < array.length; j++) {
                node += array[j] * weight[i][j];
            }
            node += getBias(i);
            output[i] = activation.process(node).floatValue();
        }
        return output;
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("VectorLayer{").append('\n');
        sb.append("bias=").append(Arrays.toString(bias));
        sb.append(", weight=").append(Arrays.toString(weight));
        sb.append(", output=").append(output).append('\n');
        sb.append(", input=").append(input).append('\n');
        sb.append(", activation=").append(activation).append('\n');
        sb.append('}');
        return sb.toString();
    }
    
    
}
