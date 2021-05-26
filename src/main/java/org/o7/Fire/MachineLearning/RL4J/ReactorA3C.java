package org.o7.Fire.MachineLearning.RL4J;

import org.deeplearning4j.rl4j.learning.async.a3c.discrete.A3CDiscreteDense;
import org.deeplearning4j.rl4j.learning.configuration.A3CLearningConfiguration;
import org.deeplearning4j.rl4j.network.configuration.ActorCriticDenseNetworkConfiguration;
import org.deeplearning4j.rl4j.policy.ACPolicy;
import org.nd4j.linalg.learning.config.Adam;
import org.o7.Fire.Experimental.Webhook;
import org.o7.Fire.MachineLearning.Framework.Reactor;

import java.io.File;
import java.io.IOException;

public class ReactorA3C {
    public static File value = new File("ReactorA3C-Value");
    public static File policy = new File("ReactorA3C-Policy");
    
    public static void main(String[] args) {
        if (!value.exists()) train();
    }
    
    public static void train() {
        Webhook.hook();
        ReactorMDPDiscrete mdp = new ReactorMDPDiscrete(new Reactor());
        
        A3CLearningConfiguration learningConfiguration = A3CLearningConfiguration.builder().seed(123L).maxEpochStep(200).maxStep(5000).numThreads(Runtime.getRuntime().availableProcessors() / 2).nStep(20).rewardFactor(0.01).gamma(0.99).build();
        
        ActorCriticDenseNetworkConfiguration config = ActorCriticDenseNetworkConfiguration.builder().updater(new Adam(1e-2)).l2(0).numHiddenNodes(16).numLayers(3).build();
        
        //define the training
        A3CDiscreteDense<ReactorMDP.ReactorObserver> a3c = new A3CDiscreteDense<>(mdp, config, learningConfiguration);
        
        a3c.train(); //start the training
        mdp.close();
        
        ACPolicy<ReactorMDP.ReactorObserver> pol = a3c.getPolicy();
        
        try {
            pol.save(value.getAbsoluteFile().getAbsolutePath(), policy.getAbsoluteFile().getAbsolutePath());
        }catch(IOException e){
        
        
        }
        System.out.println("Training Finished");
    }
}
