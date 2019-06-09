package agents.drone.behaviors;

import agents.Communicator;
import agents.drone.DroneAgent;
import agents.drone.DroneFlyingManager.FlyingState;
import main.Constants;
import sim.util.Double3D;

public class RollbackBehavior extends FlyingBehavior {
	public RollbackBehavior(DroneAgent drone) {
		super(drone);
	}
	
	public Double3D stepTransform(Communicator com) {
		return new Double3D();
	}
	
	public FlyingState transitionTo() {
		return FlyingState.ROLLBACK;
	}
}
