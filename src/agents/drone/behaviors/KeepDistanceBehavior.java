package agents.drone.behaviors;

import agents.Communicator;
import agents.DroneMessage;
import agents.drone.DroneAgent;
import agents.drone.DroneFlyingManager.FlyingState;
import main.Constants;
import sim.util.Double3D;

public class KeepDistanceBehavior extends FlyingBehavior {
	private static final int MAX_STEPS_NO_MSG = 50;  // number of steps before considering we disconnected
	private static final int MAX_STEPS        = 150; // number of steps before seeking again by default
	long prevStatusStep = -1;
	int noStatusSteps = 0;
	
	int stepsSinceStart = 0;
	
	public KeepDistanceBehavior(DroneAgent drone) {
		super(drone);
	}
	
	public Double3D stepTransform(Communicator com) {
		Double3D transform = new Double3D(0, 0, 0);
		
		// Get signal strength compared to leader
		DroneMessage lastStatus = com.getLastStatusFrom(drone.getLeaderID());
		if(lastStatus == null || lastStatus.getStep() == prevStatusStep) {
			noStatusSteps++;
			return transform;
		}
		noStatusSteps = 0;
		prevStatusStep = lastStatus.getStep();
		//System.out.println("Getting strength from drone=" + drone.getID() + " to leader=" + drone.getLeaderID() + ", got strength=" + lastStatus.getStrength());
		float strength = lastStatus.getStrength();
		
		if(strength > Constants.DRONE_IDEAL_SIGNAL_LOSS + Constants.KEEP_DIST_GOAL_SIGNAL_TOLERANCE)
			transform = transform.add(new Double3D(Constants.DRONE_SPEED, 0, 0)); // go forward if signal is too low
		else if(strength < Constants.DRONE_IDEAL_SIGNAL_LOSS - Constants.KEEP_DIST_GOAL_SIGNAL_TOLERANCE)
			transform = transform.subtract(new Double3D(Constants.DRONE_SPEED, 0, 0)); // go backward if signal is too good (probably too close from the leader)
		
		//System.out.println("[KeepDistBehaviour, drone=" + drone.getID() + "] Got strength = " + strength + ", chose to move by " + transform);
		stepsSinceStart++;
		return transform;
	}
	
	public FlyingState transitionTo() {
		if(noStatusSteps > MAX_STEPS_NO_MSG) return FlyingState.SEEK_SIGNAL_DIR;
		if(stepsSinceStart > MAX_STEPS) return FlyingState.SEEK_SIGNAL_DIR;
		return FlyingState.KEEP_SIGNAL_DIST;
	}
}
