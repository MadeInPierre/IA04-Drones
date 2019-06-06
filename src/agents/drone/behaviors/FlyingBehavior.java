package agents.drone.behaviors;

import agents.Communicator;
import sim.util.Double3D;

public class FlyingBehavior {
	public FlyingBehavior() {
		
	}
	
	public Double3D stepTransform(Communicator com) {
		System.out.println("IDLE, not moving");
		return new Double3D(0, 0, 0); // Don't move
	}
}
