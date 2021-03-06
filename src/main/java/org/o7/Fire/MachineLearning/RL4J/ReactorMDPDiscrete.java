package org.o7.Fire.MachineLearning.RL4J;

import org.deeplearning4j.gym.StepReply;
import org.deeplearning4j.rl4j.mdp.MDP;
import org.deeplearning4j.rl4j.policy.Policy;
import org.deeplearning4j.rl4j.space.ArrayObservationSpace;
import org.deeplearning4j.rl4j.space.DiscreteSpace;
import org.deeplearning4j.rl4j.space.ObservationSpace;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.o7.Fire.MachineLearning.Framework.Reactor;
import org.o7.Fire.MachineLearning.Reinforcement.ReactorControl;

public class ReactorMDPDiscrete implements MDP<ReactorMDP.ReactorObserver, Integer, DiscreteSpace> {
    ReactorMDP.ReactorObserver observer;
    Reactor reactor;
    DiscreteSpace discreteSpace;
    ObservationSpace<ReactorMDP.ReactorObserver> observationSpace;
    
    public ReactorMDPDiscrete(Reactor r) {
        reactor = r;
        observer = new ReactorMDP.ReactorObserver(r);
        discreteSpace = new DiscreteSpace(r.factor().length);
        observationSpace = new ArrayObservationSpace<>(new int[]{r.factor().length});
        
    }
    
    public static void doAction(Policy<Integer> policy) {
        doAction(policy, ReactorControl.reactor);
    }
    
    public static void doAction(Policy<Integer> policy, Reactor r) {
        INDArray p = Nd4j.create(r.factor(), 1, r.factor().length);
        int i = policy.nextAction(p);
        doAction(r, i);
    }
    
    public static void doAction(Reactor r, int integer) {
        if (integer == 1) r.lowerControlRod();
        else if (integer == 2) r.raiseControlRod();
    }
    
    @Override
    public ObservationSpace<ReactorMDP.ReactorObserver> getObservationSpace() {
        return observationSpace;
    }
    
    @Override
    public DiscreteSpace getActionSpace() {
        return discreteSpace;
    }
    
    @Override
    public ReactorMDP.ReactorObserver reset() {
        reactor.reset();
        return observer;
    }
    
    @Override
    public void close() {
    
    }
    
    @Override
    public StepReply<ReactorMDP.ReactorObserver> step(Integer integer) {
        doAction(reactor, integer);
        reactor.update();
        return new StepReply<>(observer, reactor.getPowerOutput(), reactor.reactorFuckingExploded(), null);
    }
    
    @Override
    public boolean isDone() {
        return reactor.reactorFuckingExploded();
    }
    
    @Override
    public MDP<ReactorMDP.ReactorObserver, Integer, DiscreteSpace> newInstance() {
        return new ReactorMDPDiscrete(new Reactor());
    }
}
