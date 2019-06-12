package agents.drone;

import java.util.ArrayList;

import agents.Communicator;
import agents.drone.behaviors.FlyingBehavior;
import agents.drone.behaviors.HeadMoveBehavior;
import agents.drone.behaviors.KeepDistanceBehavior;
import agents.drone.behaviors.RollbackBehavior;
import agents.drone.behaviors.SeekDirectionBehavior;
import agents.drone.behaviors.WaitReconnectBehavior;
import environment.Environment;
import main.Constants;
import sim.util.Double2D;
import sim.util.Double3D;

public class DroneFlyingManager {
	public enum FlyingState {
		IDLE,          		// Drone isn't flying (OperationState != FLYING)
		HEAD_MOVE,			// Follow the operator's commands
		SEEK_SIGNAL_DIR,	// Seeking which direction to choose
		KEEP_SIGNAL_DIST,	// Normal mode: signal direction found, moving in a straight line to keep signal quality
		WAIT_RECONNECT, 	// Lost connection, waiting for the next drone to come back
		ROLLBACK,     		// Lost connection, rolling back its position until reconnected
	};
	private FlyingState flyingState;
	
	private ArrayList<Double3D> trajectoryHistory; // keeps the last main.Constants.HISTORY_DURATION seconds of the drone's position
	
	FlyingBehavior currentBehavior;
	
	DroneAgent drone;
	
	public void setFlyingState(FlyingState newState) {
		if(newState == flyingState) return;
		flyingState = newState;
		
		// Change current moving strategy that will be applied from now on
		switch(flyingState) {
		case IDLE:
			currentBehavior = new FlyingBehavior(drone);
			break;
		case HEAD_MOVE:
			currentBehavior = new HeadMoveBehavior(drone);
			break;
		case SEEK_SIGNAL_DIR:
			currentBehavior = new SeekDirectionBehavior(drone);
			break;
		case KEEP_SIGNAL_DIST:
			currentBehavior = new KeepDistanceBehavior(drone);
			break;
		case WAIT_RECONNECT:
			currentBehavior = new WaitReconnectBehavior(drone);
			break;
		case ROLLBACK:
			currentBehavior = new RollbackBehavior(drone);
			break;
		}
	}
	
	public DroneFlyingManager(DroneAgent drone) {
		this.drone = drone;
		setFlyingState(FlyingState.IDLE); 
		trajectoryHistory = new ArrayList<Double3D>();
	}
	
	// Refresh the drone's trajectory, used for disconnection rollbacks
	private void updateHistory(Double3D newTransform) {
		trajectoryHistory.add(newTransform);
		if(trajectoryHistory.size() >= Constants.HISTORY_DURATION)
			trajectoryHistory.remove(0);
	}
	
	private void clearHistory() {
		trajectoryHistory.clear();
	}
	
	public void stepTransform(Environment env, Communicator com) {
		// Process distance sensors
		Double3D collisionTransform = new Double3D(); // TODO
		
		// Apply current movement strategy
		Double3D behaviorTransform = currentBehavior.stepTransform(com); //TODO get com from drone instead of arg
		setFlyingState(currentBehavior.transitionTo()); // potentially switch to a new behavior
		// Merge moving decisions for a final transform
		Double3D transform = behaviorTransform; // TODO add collisions
		
		// Save transform in translation history
		updateHistory(transform);

		// Move the drone in the real world
		env.rotateDrone(drone, (float)transform.z);
		env.translateDrone(drone, new Double2D(transform.x, transform.y));
	}
}
