package agents.drone;

import sim.util.Double2D;

public class CollisionsSensor {
	private float angle; // sensor angle in rad from the drone's front
	
	public CollisionsSensor(float angle) {
		this.angle = angle;
	}
	
	public float getDistance(Double2D position, DroneCommunicator communicator) {
		// TODO
		return 0;
	}
}
