package agents.drone.behaviors;

import agents.Communicator;
import agents.drone.DroneAgent;
import agents.drone.DroneFlyingManager.FlyingState;
import sim.util.Double3D;

public class FlyingBehavior {
	DroneAgent drone;
	
	public FlyingBehavior(DroneAgent drone) {
		this.drone = drone;
	}
	
	public Double3D stepTransform(Communicator com) {
		return new Double3D(0, 0, 0); // Don't move
	}
	
	public FlyingState transitionTo() {
		return FlyingState.IDLE;
	}
	
	public boolean enableCollisions() {
		return true;
	}
	
	public void destroy() {
		
	}
}
