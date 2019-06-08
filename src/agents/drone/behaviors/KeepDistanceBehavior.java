package agents.drone.behaviors;

import agents.Communicator;
import main.Constants;
import sim.util.Double3D;

public class KeepDistanceBehavior extends FlyingBehavior {
	public KeepDistanceBehavior() {
		
	}
	
	public Double3D stepTransform(Communicator com) {
		Double3D transform = new Double3D(0, 0, 0);
		// Get signal strength compared to leader
		float strength = -75; //TODO get from communicator
		
		
		if(strength < Constants.KEEP_DIST_GOAL_SIGNAL_STRENGTH - Constants.KEEP_DIST_GOAL_SIGNAL_TOLERANCE)
			transform = transform.add(new Double3D(0.1, 0, 0)); // go forward if signal is too low
		else if(strength > Constants.KEEP_DIST_GOAL_SIGNAL_STRENGTH + Constants.KEEP_DIST_GOAL_SIGNAL_TOLERANCE)
			transform = transform.add(new Double3D(-0.1, 0, 0)); // go forward if signal is too good (probably too close from the leader)
		
		//System.out.println("[KeepDistBehaviour] Got strengh = " + strength + ", chose to move by (" + transform.getX() + ", " + transform.getY() + ", " + transform.getZ() + ")");
		return transform;
	}
}
