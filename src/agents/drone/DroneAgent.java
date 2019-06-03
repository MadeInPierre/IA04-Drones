package agents.drone;

import java.util.ArrayList;

import sim.util.Double2D;

public class DroneAgent {
	/* TODO
	 * 	- Fly manager 
	 *  - Communicator
	 * BUGS
	 * 	- 
	 */
	
	public enum DroneState {
		IDLE,   			// Nothing to do, waiting for orders
		ARMED,				// Take off when the next drone is too far away
		FLYING,				// 
		CRASHED				// Game over. 
	};
	private DroneState droneState = DroneState.IDLE; 
	
	public enum FlyingState {
		IDLE,          		// Drone isn't flying (OperationState != FLYING)
		SEEK_SIGNAL_DIR,	// Seeking which direction to choose
		KEEP_SIGNAL_DIST,	// Normal mode: signal direction found, moving in a straight line to keep signal quality
		WAIT_RECONNECT, 	// Lost connection, waiting for the next drone to come back
		ROLLBACK,     		// Lost connection, rolling back its position until reconnected
	};
	private FlyingState flyingState = FlyingState.IDLE;
	
	private ArrayList<Double2D> trajectoryHistory; // keeps the last main.Constants.HISTORY_DURATION seconds of the drone's position
	
	private int droneID  = -1; // This drone's ID
	private int leaderID = -1; // ID of the drone to follow
	
	private DroneCommunicator communicator;
	private DroneFlyingManager flyingManager;
	
	
	public void setDroneState(DroneState newState) {
		droneState = newState;
		if(newState == DroneState.IDLE) // force IDLE on both statuses
			setFlyingState(FlyingState.IDLE); 
	}
	
	public void setFlyingState(FlyingState newState) {
		flyingState = newState;
		if(newState == FlyingState.IDLE) // force IDLE on both statuses
			setDroneState(DroneState.IDLE);
	}
	
	public int getID() {
		return droneID;
	}
	
	public void setLeaderID(int newID) {
		leaderID = newID;
	}
	
	// MAIN FUNCTIONS
	
	public DroneAgent(int id) {
		droneID = id;
		communicator = new DroneCommunicator();
		flyingManager = new DroneFlyingManager();
	}
	
	public void step() {
		
	}
}
