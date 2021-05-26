package org.o7.Fire.MachineLearning.RL4J;

import Atom.Utility.Random;
import org.deeplearning4j.gym.StepReply;
import org.deeplearning4j.rl4j.mdp.MDP;
import org.deeplearning4j.rl4j.observation.Observation;
import org.deeplearning4j.rl4j.space.*;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.o7.Fire.MachineLearning.Framework.Reactor;

import java.util.function.Consumer;

public class ReactorMDP implements MDP<ReactorMDP.ReactorObserver, ReactorMDP.ReactorAction, ReactorMDP.ReactorActions> {
    Reactor reactor;
    ReactorObserver observer;
    ObservationSpace<ReactorObserver> observationSpace;
    ReactorActions reactorActions = new ReactorActions();
    
    public ReactorMDP(Reactor r) {
        reactor = r;
        observer = new ReactorObserver(r);
        observationSpace = new ArrayObservationSpace<>(new int[]{r.factor().length});
    }
    
    @Override
    public ObservationSpace<ReactorObserver> getObservationSpace() {
        return observationSpace;
    }
    
    @Override
    public ReactorActions getActionSpace() {
        return reactorActions;
    }
    
    @Override
    public ReactorObserver reset() {
        reactor.reset();
        return observer;
    }
    
    @Override
    public void close() {
    
    }
    
    @Override
    public StepReply<ReactorObserver> step(ReactorAction reactorAction) {
        reactorAction.doReactor(reactor);
        reactor.update();
        return new StepReply<>(observer, reactor.getPowerOutput(), reactor.reactorFuckingExploded(), reactor.about());
    }
    
    @Override
    public boolean isDone() {
        return false;
    }
    
    @Override
    public MDP<ReactorObserver, ReactorAction, ReactorActions> newInstance() {
        return null;
    }
    
    public enum ReactorAction {
        NoOp(r -> {}), RaiseRod(Reactor::raiseControlRod), LowerRod(Reactor::lowerControlRod);
        Consumer<Reactor> doSmth;
        
        ReactorAction(Consumer<Reactor> doSmth) {
            this.doSmth = doSmth;
        }
        
        public void doReactor(Reactor r) {
            doSmth.accept(r);
        }
    }
    
    public static class ReactorObserver implements Encodable {
        Reactor reactor;
        
        public ReactorObserver(Reactor r) {
            reactor = r;
        }
        
        public Observation asObservation() {
            return new Observation(getData());
        }
        
        @Override
        public double[] toArray() {
            return reactor.factor();
        }
        
        @Override
        public boolean isSkipped() {
            return false;
        }
        
        @Override
        public INDArray getData() {
            return Nd4j.create(reactor.factor());
        }
        
        @Override
        public Encodable dup() {
            return new Box(reactor.factor());
        }
    }
    
    public static class ReactorActions implements ActionSpace<ReactorAction> {
        
        @Override
        public ReactorAction randomAction() {
            return Random.getRandom(ReactorAction.values());
        }
        
        @Override
        public Object encode(ReactorAction reactorAction) {
            return reactorAction;
        }
        
        @Override
        public int getSize() {
            return ReactorAction.values().length;
        }
        
        @Override
        public ReactorAction noOp() {
            return ReactorAction.NoOp;
        }
    }
}
