package agents.drone;

import java.util.ArrayList;
import sim.util.Double3D;

public class DroneFlyingManager {
	public enum FlyingState {
		IDLE,          		// Drone isn't flying (OperationState != FLYING)
		SEEK_SIGNAL_DIR,	// Seeking which direction to choose
		KEEP_SIGNAL_DIST,	// Normal mode: signal direction found, moving in a straight line to keep signal quality
		WAIT_RECONNECT, 	// Lost connection, waiting for the next drone to come back
		ROLLBACK,     		// Lost connection, rolling back its position until reconnected
	};
	private FlyingState flyingState = FlyingState.IDLE;
	
	private ArrayList<Double3D> trajectoryHistory; // keeps the last main.Constants.HISTORY_DURATION seconds of the drone's position
	
	public void setFlyingState(FlyingState newState) {
		flyingState = newState;
	}
	
	public DroneFlyingManager() {
		
	}
	
	public Double3D stepPos() {
		// TODO Process distance sensors 
		
		// Apply current movement strategy
		switch(flyingState) {
		default:
			break;
		}
		return new Double3D(); //TODO
	}
}
