package agents.drone;

import java.util.ArrayList;

import sim.engine.SimState;
import sim.engine.Steppable;
import agents.drone.DroneFlyingManager.FlyingState;
import environment.Environment;
import sim.util.Double3D;

public class DroneAgent implements Steppable {
	/* TODO
	 * 	- Fly manager 
	 *  - Communicator
	 * BUGS
	 * 	- 
	 */ 
	
	public enum DroneState {
		IDLE,   			// Nothing to do, waiting for orders
		ARMED,				// Take off when the next drone is too far away
		FLYING,				// Drone flying and applying it's current FlyingState moving strategy.
		CRASHED				// Game over :(
	};
	private DroneState droneState = DroneState.IDLE; 
	
	private int droneID  = -1; // This drone's ID
	private int leaderID = -1; // ID of the drone to follow
	
	private DroneCommunicator communicator;
	private DroneFlyingManager flyingManager;
	
	private static int idCounter = 0;
	
	public void setDroneState(DroneState newState) {
		droneState = newState;
		if(newState == DroneState.IDLE) // force IDLE on both statuses
			flyingManager.setFlyingState(FlyingState.IDLE); 
	}
	
	public void setFlyingState(FlyingState newState) {
		flyingManager.setFlyingState(newState);
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
	
	public DroneAgent() {
		droneID = idCounter++;
		communicator = new DroneCommunicator(this);
		flyingManager = new DroneFlyingManager(this);
	}
	
	public void step(SimState state) {
		Environment env = (Environment)state;
		
		// Process messages
		
		// Update position
		flyingManager.stepTransform(env);
	}
}
