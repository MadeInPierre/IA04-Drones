package agents.drone.behaviors;

import agents.Communicator;
import agents.DroneMessage;
import agents.DroneMessage.Performative;
import agents.drone.CollisionsSensor;
import agents.drone.DroneAgent;
import agents.drone.DroneAgent.DroneState;
import agents.drone.DroneFlyingManager.FlyingState;
import sim.util.Double3D;
import java.util.ArrayList;
import java.util.Collections;

public class SeekTunnelBehavior extends FlyingBehavior {
	private static final int N_TURN_STEPS = 100; // Number of steps before making a full circle. One signal measure per step.
	
	private enum SeekState {
		IN_CIRCLE,   // Making the turn and collecting signals 
		CHOOSE_DIR,  // Analyse the distances and detect where the tunnel goes
		GOTO_DIR, 	 // Going back to center position when circle finished 
		FINISHED,    // Mark as finished for FlyingManager
	}
	private SeekState seekState = SeekState.IN_CIRCLE;
	
	private int step = 0;
	private ArrayList<Float> distances_front;
	private ArrayList<Float> distances_left;
	
	public SeekTunnelBehavior(DroneAgent drone) {
		super(drone);
		distances_front = new ArrayList<Float>();
		distances_left = new ArrayList<Float>();
	}
	
	// Make a full circle pattern
	public Double3D stepTransform(Communicator com) {
		Double3D transform = new Double3D();
		
		switch(seekState) {
		case IN_CIRCLE: {
			// Get lasers' distances
			CollisionsSensor[] sensors = drone.getCollisionSensors();
			distances_front.add((float)sensors[0].getDistance(com));
			distances_left.add((float)sensors[3].getDistance(com));
			
			transform = transform.add(new Double3D(0, 0, (Math.PI/2.0) / N_TURN_STEPS));
			step++; 
			if(step == N_TURN_STEPS) {
				step = 0;
				seekState = SeekState.CHOOSE_DIR;
			}
			break;
		}
		case CHOOSE_DIR: {
			// Choose new direction 
			float front_choice = Collections.max(distances_front);
			int i_front = distances_front.indexOf(front_choice);
			float left_choice = Collections.max(distances_left);
			int i_left = distances_left.indexOf(left_choice);
			
			// Find middle of corridors if possible
			int i_front_end = i_front++;
			while(i_front_end < distances_front.size() - 1 && distances_front.get(i_front_end) == distances_front.get(i_front)) i_front_end++;
			i_front = (i_front + i_front_end) / 2;
			
			int i_left_end = i_left++;
			while(i_left_end < distances_left.size() - 1 && distances_left.get(i_left_end) == distances_left.get(i_left)) i_left_end++;
			i_left = (i_left + i_left_end) / 2;
			
			// Compare the two sensors and choose how much steps we have to make to face the tunnel (from the original angle before seeking)
			int n_steps_turn = (front_choice >= left_choice) ? i_front : -1 * (N_TURN_STEPS - i_left);
			
			transform = transform.add(new Double3D(0, 0, -1.0 * (Math.PI/2.0)));
			transform = transform.add(new Double3D(0, 0, n_steps_turn * (Math.PI/2.0) / N_TURN_STEPS));
			
			seekState = SeekState.GOTO_DIR;
			break;
		}
		case GOTO_DIR: { // TODO do a proper slow turn to face the direction
			seekState = SeekState.FINISHED;
			break;
		}
		case FINISHED: {
			break;
		}
		}
		
//		System.out.println("[SeekDirectionBehaviour] chose to move by (" + transform.getX() + ", " + transform.getY() + ", " + transform.getZ() + "), step = " + step);
		return transform;
	}
	
	public FlyingState transitionTo() {
		if(seekState == SeekState.FINISHED) return drone.isHead() ? FlyingState.HEAD_MOVE : FlyingState.GOTO_STRAIGHT;
		return FlyingState.SEEK_TUNNEL_DIR;
	}
	
	public boolean enableCollisions() {
		return false;
	}
	
	public void destroy() {
	}
}
