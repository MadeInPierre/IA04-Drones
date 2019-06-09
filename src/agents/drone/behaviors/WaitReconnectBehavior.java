package agents.drone.behaviors;

import agents.Communicator;
import agents.drone.DroneAgent;
import agents.drone.DroneFlyingManager.FlyingState;
import sim.util.Double3D;

public class WaitReconnectBehavior extends FlyingBehavior {
	public WaitReconnectBehavior(DroneAgent drone) {
		super(drone);
	}
	
	public Double3D stepTransform(Communicator com) {
		return new Double3D();
	}
	
	public FlyingState transitionTo() {
		return FlyingState.WAIT_RECONNECT;
	}
}
