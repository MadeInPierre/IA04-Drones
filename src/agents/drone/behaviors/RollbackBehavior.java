package agents.drone.behaviors;

import agents.Communicator;
import agents.drone.DroneAgent;
import agents.drone.DroneFlyingManager.FlyingState;
import environment.Environment;
import main.Constants;
import sim.util.Double3D;

import java.util.ArrayList;

public class RollbackBehavior extends FlyingBehavior {
	public boolean emptyHistory = false;
	public boolean connexionRestored = false;

	public RollbackBehavior(DroneAgent drone) {
		super(drone);
	}
	
	public Double3D stepTransform(Communicator com) {

		if(com.getSignalStrength(drone.getFollowerID()) < Constants.DRONE_DANGER_SIGNAL_LOSS - Constants.DRONE_EXPECTED_SIGNAL_STD) {
			connexionRestored = true;
		}

		Double3D history = drone.popTrajectoryHistory();

		emptyHistory = history == null;

		//return !emptyHistory ? history.multiply(-1) : new Double3D();
		return new Double3D(-Constants.DRONE_SPEED, 0, 0);
	}
	
	public FlyingState transitionTo() {
		if (!emptyHistory && !connexionRestored) {
			return FlyingState.ROLLBACK;
		}
		else {
			return drone.isHead() ? FlyingState.HEAD_MOVE : FlyingState.GOTO_STRAIGHT;
		}
	}
	
	public boolean enableCollisions() {
		return false;
	}
}
