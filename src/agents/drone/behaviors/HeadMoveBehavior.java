package agents.drone.behaviors;

import agents.Communicator;
import main.Constants;
import sim.util.Double3D;

public class HeadMoveBehavior extends FlyingBehavior {
	public HeadMoveBehavior() {
		
	}
	
	public Double3D stepTransform(Communicator com) {
		return new Double3D();
	}
}
