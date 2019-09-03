package agents.drone.behaviors;

import agents.Communicator;
import agents.DroneMessage;
import agents.drone.DroneAgent;
import agents.drone.DroneFlyingManager.FlyingState;
import main.Constants;
import sim.util.Double3D;

public class WaitReconnectBehavior extends FlyingBehavior {
	
	boolean connected = false;
	
	public WaitReconnectBehavior(DroneAgent drone) {
		super(drone);
	}
	
	public Double3D stepTransform(Communicator com) {
		DroneMessage lastStatus = com.getLastStatusFrom(drone.getLeaderID());
		if(lastStatus != null) {
			if(lastStatus.getStrength() < Constants.DRONE_MAXIMUM_SIGNAL_LOSS)
				connected = true;
		}
		
		return new Double3D();
	}
	
	public FlyingState transitionTo() {
		if(connected) return FlyingState.GOTO_STRAIGHT;
		return FlyingState.WAIT_RECONNECT;
	}
	
	public boolean enableCollisions() {
		return false;
	}
}
