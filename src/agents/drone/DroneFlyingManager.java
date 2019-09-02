package agents.drone;

import java.util.ArrayList;

import agents.Communicator;
import agents.DroneMessage;
import agents.drone.DroneAgent.DroneRole;
import agents.drone.DroneAgent.DroneState;
import agents.drone.behaviors.FlyingBehavior;
import agents.drone.behaviors.GotoStraightBehavior;
import agents.drone.behaviors.HeadMoveBehavior;
import agents.drone.behaviors.KeepDistanceBehavior;
import agents.drone.behaviors.RollbackBehavior;
import agents.drone.behaviors.SeekDirectionBehavior;
import agents.drone.behaviors.SeekTunnelBehavior;
import agents.drone.behaviors.WaitReconnectBehavior;
import environment.Environment;
import main.Constants;
import sim.util.Double2D;
import sim.util.Double3D;

public class DroneFlyingManager {
	public enum FlyingState {
		IDLE,          		// Drone isn't flying (OperationState != FLYING)
		HEAD_MOVE,			// Follow the operator's commands
		
		// 2D Mode
		SEEK_SIGNAL_DIR,	// Seeking which direction to choose
		KEEP_SIGNAL_DIST,	// Normal mode: signal direction found, moving in a straight line to keep signal quality
		
		// 1D Mode
		SEEK_TUNNEL_DIR,	// Seeking which direction to choose to follow the tunnel 
		GOTO_STRAIGHT,  	// Normal mode: move in a straight line and try to get to an absolute position in the tunnel
		
		WAIT_RECONNECT, 	// Lost connection, waiting for the next drone to come back
		ROLLBACK,     		// Lost connection, rolling back its position until reconnected
	};
	private FlyingState flyingState;
	
	private double distanceInTunnel = 0;		   // Estimates how far we are in the tunnel. Used as a position system with an Optical Flow sensor.
	private ArrayList<Double3D> trajectoryHistory; // keeps the last main.Constants.HISTORY_DURATION seconds of the drone's position
	
	FlyingBehavior currentBehavior = null;
	boolean forceHover = false; // can be enabled when the follower is seeking
	
	DroneAgent drone;
	
	public void setFlyingState(FlyingState newState) {
		if(newState == flyingState) return;
		flyingState = newState;
		
		// Change current moving strategy that will be applied from now on
		if(currentBehavior != null) currentBehavior.destroy();
		switch(flyingState) {
		case IDLE:
			currentBehavior = new FlyingBehavior(drone);
			break;
		case HEAD_MOVE:
			currentBehavior = new HeadMoveBehavior(drone);
			break;
		case SEEK_SIGNAL_DIR:
			currentBehavior = new SeekDirectionBehavior(drone);
			break;
		case KEEP_SIGNAL_DIST:
			currentBehavior = new KeepDistanceBehavior(drone);
			break;
		case SEEK_TUNNEL_DIR:
			currentBehavior = new SeekTunnelBehavior(drone);
			break;
		case GOTO_STRAIGHT:
			currentBehavior = new GotoStraightBehavior(drone);
			break;
		case WAIT_RECONNECT:
			currentBehavior = new WaitReconnectBehavior(drone);
			break;
		case ROLLBACK:
			currentBehavior = new RollbackBehavior(drone);
			break;
		}
	}
	
	public void setFlyingStateForced(FlyingState newState) {
		flyingState = null;
		setFlyingState(newState);
	}
	
	public DroneFlyingManager(DroneAgent drone) {
		this.drone = drone;
		setFlyingState(FlyingState.IDLE); 
		trajectoryHistory = new ArrayList<Double3D>();
	}
	
	// Refresh the drone's trajectory, used for disconnection rollbacks
	private void updateHistory(Double3D newTransform) {
		if(newTransform.equals(new Double3D(0, 0, 0))) return; // ignore when we're not moving
		trajectoryHistory.add(newTransform);
		distanceInTunnel += newTransform.getX();

		if(trajectoryHistory.size() >= Constants.HISTORY_DURATION)
			trajectoryHistory.remove(0);
	}
	
	private void clearHistory() {
		trajectoryHistory.clear();
	}
	
	public void stepTransform(Communicator com) {
		// Process messages
		ArrayList<DroneMessage> inbox = com.getMessages();
		for(DroneMessage msg : inbox) {
			if(msg.getTitle() == "seek" && msg.getContent() == "start" && msg.getSenderID() == drone.getFollowerID()) forceHover = true;
			if(msg.getTitle() == "seek" && msg.getContent() == "end"   && msg.getSenderID() == drone.getFollowerID()) forceHover = false;
		}
		
		// Apply current movement strategy
		Double3D behaviorTransform = new Double3D();
		if(forceHover == false) behaviorTransform = currentBehavior.stepTransform(com);
		
		// Process distance sensors
		Double3D collisionTransform = new Double3D(0, 0, 0);
		if(currentBehavior.enableCollisions()) collisionTransform = calculateCollisionTransform(com);
		
		// Merge moving decisions for a final transform
		Double3D transform = behaviorTransform.add(collisionTransform.multiply(Constants.DRONE_COLLISION_SENSOR_WEIGHT));

		if (flyingState != FlyingState.ROLLBACK) {
			updateHistory(transform); // Save transform in translation history
		}

		// Potentially switch to a new behavior
		setFlyingState(currentBehavior.transitionTo());

		// Move the drone in the real world
		Environment env = Environment.get();
		env.rotateDrone(drone, (float)transform.z);
		env.translateDrone(drone, new Double2D(transform.x, transform.y));
	}
	
	
	private Double3D calculateCollisionTransform(Communicator com) {
		Double3D collisionTransform = new Double3D(0, 0, 0);

		CollisionsSensor[] sensors = drone.getCollisionSensors();

		for (int i = 1; i < 4; i += 2) {
			CollisionsSensor sensor = sensors[i];
			float distance = (float) sensor.getDistance(com);

			if (distance > 0 && distance <= Constants.DRONE_COLLISION_SENSOR_TRIGGER_DISTANCE) {
				float angle = sensor.getAngle();

				Double3D vector = distance < Constants.DRONE_COLLISION_SENSOR_MINIMUM_DISTANCE ?
						new Double3D(0, 0, 0) :
						//new Double3D(Math.cos((double) angle), Math.sin((double) angle), 0).multiply(-1 / distance / distance);
						new Double3D(Math.cos((double) angle), Math.sin((double) angle), 0).multiply(-1 / distance / distance);

				collisionTransform = collisionTransform.add(vector);
			}
		}
		
		// Braitenberg turning
		collisionTransform = collisionTransform.add(new Double3D(0, 0, (trajectoryHistory.get(trajectoryHistory.size() - 1).x >= 0 ? 1.0 : -1.0) * 
																	   (sensors[1].getDistance(com) - sensors[3].getDistance(com)) * 1.5f));
		
		// Can't go faster than the drone speed in all cases
		if(collisionTransform.length() > Constants.DRONE_SPEED)
			collisionTransform = collisionTransform.normalize().multiply(Constants.DRONE_SPEED);
		
		return collisionTransform;
	}

	public FlyingState getFlyingState() {
		return flyingState;
	}

	public Double3D popTrajectoryHistory() {
		return !trajectoryHistory.isEmpty() ? trajectoryHistory.remove(trajectoryHistory.size() - 1) : null;
	}
	
	public double getDistanceInTunnel() { return distanceInTunnel; }
}
