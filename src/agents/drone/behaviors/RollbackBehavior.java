package agents.drone.behaviors;

import agents.Communicator;
import main.Constants;
import sim.util.Double3D;

public class RollbackBehavior extends FlyingBehavior {
	public RollbackBehavior() {
		
	}
	
	public Double3D stepTransform(Communicator com) {
		return new Double3D();
	}
}
