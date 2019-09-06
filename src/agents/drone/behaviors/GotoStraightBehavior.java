package agents.drone.behaviors;

import agents.Communicator;
import agents.DroneMessage;
import agents.drone.CollisionsSensor;
import agents.drone.DroneAgent;
import agents.drone.DroneAgent.DroneRole;
import agents.drone.DroneFlyingManager.FlyingState;
import main.Constants;
import sim.util.Double3D;

public class GotoStraightBehavior extends FlyingBehavior {
	private boolean askForSeek = false;
	
//	private float goalPos = 15;
	
	public GotoStraightBehavior(DroneAgent drone) {
		super(drone);
	}
	
	public Double3D stepTransform(Communicator com) {
		Double3D transform = new Double3D(0, 0, 0);
		
		// Stop going straight if there's an obstacle ahead and seek
		CollisionsSensor[] sensors = drone.getCollisionSensors();
		if(sensors[0].getDistance(com) <= Constants.DRONE_COLLISION_SENSOR_TRIGGER_DISTANCE) askForSeek = true;
		
		// Go forward or backward according to the goal position
//		if     (drone.getDistanceInTunnel() < goalPos - 0.05) transform = transform.add(new Double3D(       Constants.DRONE_SPEED, 0, 0));
//		else if(drone.getDistanceInTunnel() > goalPos + 0.05) transform = transform.add(new Double3D(-1.0 * Constants.DRONE_SPEED, 0, 0));
		
		// Go where the signal is the lowest
		float followerSignal = com.getSignalStrength(drone.getFollowerID());
		float leaderSignal   = com.getSignalStrength(drone.getLeaderID());
		transform = transform.add(new Double3D((followerSignal < leaderSignal ? 1.0 : -1.0) * Constants.DRONE_SPEED, 0, 0));
		
		// Avoid collisions between drones based on signal quality (can't be too good)
		if(transform.getX() < 0 && com.getSignalStrength(drone.getFollowerID()) < Constants.DRONE_BEST_SIGNAL_LOSS)
			transform = new Double3D(0, transform.getY(), transform.getZ());
		if(transform.getX() > 0 && com.getSignalStrength(drone.getLeaderID())   < Constants.DRONE_BEST_SIGNAL_LOSS)
			transform = new Double3D(0, transform.getY(), transform.getZ());
		
		return transform;
	}
	
	public FlyingState transitionTo() {
		//if(askForSeek) return FlyingState.SEEK_TUNNEL_DIR;
		return FlyingState.GOTO_STRAIGHT;
	}
	
	public boolean enableCollisions() {
		return true;
	}
}