package agents.drone;

import java.util.ArrayList;
import agents.CommunicativeAgent;
import agents.Communicator;
import agents.drone.DroneFlyingManager.FlyingState;
import environment.Environment;
import sim.engine.SimState;
import sim.portrayal.Oriented2D;
import sim.util.Double3D;

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
		FLYING,				// Drone flying and applying it's current FlyingState moving strategy.
		CRASHED				// Game over :(
	};
	private DroneState droneState = DroneState.IDLE;
	
	private int leaderID = -1; // ID of the drone to follow
	
	private CollisionsSensor[] collisionSensors;
	private DroneFlyingManager flyingManager;
	
	
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

	public CollisionsSensor[] getCollisionSensors() {
		return collisionSensors;
	}
	
	public void setLeaderID(int newID) {
		leaderID = newID;
	}
	
	// MAIN FUNCTIONS
	
	public DroneAgent() {
		super();
		droneID = idCounter++;

		flyingManager = new DroneFlyingManager(this);
		flyingManager.setFlyingState(FlyingState.KEEP_SIGNAL_DIST);

		collisionSensors = new CollisionsSensor[4];
		for (int i = 0; i < 4; i++) {
			collisionSensors[i] = new CollisionsSensor(this, (float) (2 * Math.PI * i / 4));
		}
	}

	public void step(SimState state) {
		Environment env = (Environment)state;
		
		// Update position
		flyingManager.stepTransform(env, communicator);
	}
}
