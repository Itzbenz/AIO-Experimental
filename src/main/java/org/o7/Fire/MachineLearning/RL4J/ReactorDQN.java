package org.o7.Fire.MachineLearning.RL4J;

import org.deeplearning4j.rl4j.learning.sync.qlearning.QLearning;
import org.deeplearning4j.rl4j.learning.sync.qlearning.discrete.QLearningDiscreteDense;
import org.deeplearning4j.rl4j.network.dqn.DQNFactoryStdDense;
import org.deeplearning4j.rl4j.policy.DQNPolicy;
import org.nd4j.linalg.learning.config.RmsProp;
import org.o7.Fire.Experimental.Webhook;
import org.o7.Fire.MachineLearning.Framework.Reactor;

import java.io.File;
import java.io.IOException;

public class ReactorDQN {
    public static File save = new File(ReactorMDPDiscrete.class.getSimpleName() + ".zip");
    
    public static void main(String[] args) {
        Webhook.hook();
        if (!save.exists()) reactorDQN();
        System.out.println(save.getAbsolutePath());
        System.out.println("Finished");
    }
    
    private static DQNPolicy<ReactorMDP.ReactorObserver> reactorDQN() {
        
        // Q learning configuration. Note that none of these are specific to the cartpole problem.
        QLearning.QLConfiguration.QLConfigurationBuilder h = QLearning.QLConfiguration.builder().seed(123)                //Random seed (for reproducability)
                .maxEpochStep(200)        // Max step By epoch
                .maxStep(15000)           // Max step
                .expRepMaxSize(150000)    // Max size of experience replay
                .batchSize(128)            // size of batches
                .targetDqnUpdateFreq(500) // target update (hard)
                .updateStart(10)          // num step noop warmup
                .rewardFactor(0.01)       // reward scaling
                .gamma(0.99)              // gamma
                .errorClamp(1.0)          // /td-error clipping
                .minEpsilon(0.1f)         // min epsilon
                .epsilonNbStep(1000)      // num step for eps greedy anneal
                .doubleDQN(true);      // double DQN
        System.out.println(h);
        QLearning.QLConfiguration CARTPOLE_QL = h.build();
    
        // The neural network used by the agent. Note that there is no need to specify the number of inputs/outputs.
        // These will be read from the gym environment at the start of training.
        DQNFactoryStdDense.Configuration CARTPOLE_NET = DQNFactoryStdDense.Configuration.builder()//
                .l2(0)//
                .updater(new RmsProp(0.000025))//
                .numHiddenNodes(1500)//h
                .numLayer(5)//why yes
                .build();
    
        ReactorMDPDiscrete mdp = new ReactorMDPDiscrete(new Reactor());
    
        //Create the solver.
        QLearningDiscreteDense<ReactorMDP.ReactorObserver> dql = new QLearningDiscreteDense<>(mdp, CARTPOLE_NET, CARTPOLE_QL);
    
        dql.train();
        mdp.close();
    
        try {
            dql.getPolicy().save(save.getAbsoluteFile().getAbsolutePath());
        }catch(IOException e){
            e.printStackTrace();
        }
        return dql.getPolicy(); //return the trained agent.
    }
    
}
