package agents.drone.behaviors;

import sim.util.Double3D;

public class FlyingBehavior {
	public FlyingBehavior() {
		
	}
	
	public Double3D stepTransform() {
		return new Double3D(0, 0, 0); // Don't move
	}
}
