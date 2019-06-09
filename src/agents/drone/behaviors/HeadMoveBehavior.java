package agents.drone.behaviors;

import agents.Communicator;
import agents.drone.DroneAgent;
import agents.drone.DroneFlyingManager.FlyingState;
import main.Constants;
import sim.util.Double3D;

public class HeadMoveBehavior extends FlyingBehavior {
	public HeadMoveBehavior(DroneAgent drone) {
		super(drone);
		System.out.println("New head behavior drone=" + drone.getID());
	}
	
	public Double3D stepTransform(Communicator com) {
		Double3D transform = new Double3D(-0.0, 0, 0);
		return transform;
	}
	
	public FlyingState transitionTo() {
		return FlyingState.HEAD_MOVE;
	}
}
