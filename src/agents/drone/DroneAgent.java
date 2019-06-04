package agents.drone;

import java.util.ArrayList;

import agents.drone.DroneFlyingManager.FlyingState;
import sim.util.Double3D;

public class DroneAgent {
	/* TODO
	 * 	- Fly manager 
	 *  - Communicator
	 * BUGS
	 * 	- 
	 */
	
	private Double3D secretPosition; // secret position used for simulation management only, unused by the drone's intelligence 
	
	public enum DroneState {
		IDLE,   			// Nothing to do, waiting for orders
		ARMED,				// Take off when the next drone is too far away
		FLYING,				// 
		CRASHED				// Game over. 
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
		communicator = new DroneCommunicator();
		flyingManager = new DroneFlyingManager();
		
		secretPosition = new Double3D(0, 0, 0); // drone doesn't know it's absolute pos, starting at (0, 0)
	}
	
	public void step() {
		// Process messages
		
		// Update position
		secretPosition = flyingManager.stepPos();
		
	}
}
