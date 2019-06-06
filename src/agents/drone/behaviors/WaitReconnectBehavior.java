package agents.drone.behaviors;

import agents.Communicator;
import sim.util.Double3D;

public class WaitReconnectBehavior extends FlyingBehavior {
	public WaitReconnectBehavior() {
		
	}
	
	public Double3D stepTransform(Communicator com) {
		return new Double3D();
	}
}
