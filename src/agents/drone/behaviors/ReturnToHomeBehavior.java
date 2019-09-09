package agents.drone.behaviors;

import agents.Communicator;
import agents.drone.DroneAgent;
import agents.drone.DroneFlyingManager.FlyingState;
import environment.Environment;
import main.Constants;
import sim.util.Double3D;

import java.util.ArrayList;

public class ReturnToHomeBehavior extends FlyingBehavior {

	public ReturnToHomeBehavior(DroneAgent drone) {
		super(drone);
	}
	
	public Double3D stepTransform(Communicator com) {
		Double3D transform = new Double3D(0, 0, 0);
		
		float followerSignal = com.getSignalStrength(drone.getFollowerID());
		if(followerSignal > Constants.DRONE_BEST_SIGNAL_LOSS)
			transform = transform.add(new Double3D(-Constants.DRONE_SPEED, 0, 0));
		
		return new Double3D();
	}
	
	public FlyingState transitionTo() {
		return FlyingState.RTH;
	}
	
	public boolean enableCollisions() {
		return false;
	}
}
