package agents.drone.behaviors;

import agents.Communicator;
import agents.DroneMessage;
import agents.drone.DroneAgent;
import agents.drone.DroneAgent.DroneRole;
import agents.drone.DroneFlyingManager.FlyingState;
import main.Constants;
import sim.util.Double3D;

public class KeepDistanceBehavior extends FlyingBehavior {
	private static final int MAX_STEPS_NO_MSG = 50;  // number of steps before considering we disconnected
	private static final int MAX_STEPS        = 150; // number of steps before seeking again by default
	long prevStatusStep = -1;
	int noStatusSteps = 0;
	boolean followerLost = false;
	boolean leaderLost = false;
	
	Double3D lastTransform; // If no status received, continue in the same direction (in the hope we find the signal again)
	
	int stepsSinceStart = 0;
	
	public KeepDistanceBehavior(DroneAgent drone) {
		super(drone);
		lastTransform = new Double3D(0, 0, 0);
	}
	
	public Double3D stepTransform(Communicator com) {
		Double3D transform = new Double3D(0, 0, 0);
		// Get signal strength compared to leader
		DroneMessage lastStatus = com.getLastStatusFrom(drone.getLeaderID());
		followerLost = com.getSignalStrength(drone.getFollowerID()) > Constants.DRONE_MAXIMUM_SIGNAL_LOSS;

		if(lastStatus == null || lastStatus.getStep() == prevStatusStep) {
			noStatusSteps++;
			return lastTransform;
		}
		noStatusSteps = 0;
		prevStatusStep = lastStatus.getStep();
		float strength = lastStatus.getStrength();
		
		float goal = (drone.getDroneRole() == DroneRole.RTH) ? Constants.DRONE_RTH_SIGNAL_LOSS : Constants.DRONE_IDEAL_SIGNAL_LOSS;
		if(strength > goal + Constants.KEEP_DIST_GOAL_SIGNAL_TOLERANCE)
			transform = transform.add(new Double3D(Constants.DRONE_SPEED, 0, 0)); // go forward if signal is too low
		else if(strength < goal - Constants.KEEP_DIST_GOAL_SIGNAL_TOLERANCE)
			transform = transform.subtract(new Double3D(Constants.DRONE_SPEED, 0, 0)); // go backward if signal is too good (probably too close from the leader)
		
		//System.out.println("[KeepDistBehaviour, drone=" + drone.getID() + "] Got strength = " + strength + ", chose to move by " + transform);
		stepsSinceStart++;
		lastTransform = transform;
		return !followerLost ? transform : new Double3D();
	}
	
	public FlyingState transitionTo() {
		//if(followerLost) return FlyingState.ROLLBACK;
		if(noStatusSteps > MAX_STEPS_NO_MSG) return FlyingState.SEEK_SIGNAL_DIR;
		if(stepsSinceStart > MAX_STEPS) return FlyingState.SEEK_SIGNAL_DIR;
		return FlyingState.KEEP_SIGNAL_DIST;
	}
	
	public boolean enableCollisions() {
		return true;
	}
}
